package com.vastpro.sphinx.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.transaction.TransactionUtil;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class ExamSubmitService {
	public static Map<String, Object> submitExam(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();

		try {
			String partyId = (String) context.get("partyId");
			String examId = (String) context.get("examId");

			if (partyId == null || partyId.trim().isEmpty()) {
				return ServiceUtil.returnError("partyId is required");
			}
			if (examId == null || examId.trim().isEmpty()) {
				return ServiceUtil.returnError("examId is required");
			}

			TransactionUtil.begin();

			GenericValue exam = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId).queryOne();
			if (exam == null) {
				return ServiceUtil.returnError("Exam not found for examId: " + examId);
			}

			GenericValue partyExamRel = EntityQuery.use(delegator).from("PartyExamRelationship").where("partyId", partyId, "examId", examId)
							.queryOne();
			if (partyExamRel == null) {
				return ServiceUtil.returnError("User is not assigned to this exam. partyId: " + partyId);
			}

			Long allowedAttempts = partyExamRel.getLong("allowedAttempts");
			Long noOfAttempts = partyExamRel.getLong("noOfAttempts");

			if (allowedAttempts != null && noOfAttempts != null && noOfAttempts > allowedAttempts) {
				TransactionUtil.rollback();
				return ServiceUtil.returnError("No attempts remaining. Allowed: " + allowedAttempts + ", Used: " + noOfAttempts);
			}

			GenericValue inProgress = EntityQuery.use(delegator).from("InProgressParty").where("partyId", partyId, "examId", examId)
							.queryOne();
			if (inProgress == null || inProgress.getLong("isExamActive") == null || inProgress.getLong("isExamActive") != 1L) {
				TransactionUtil.rollback();
				return ServiceUtil.returnError("No active exam session found for partyId: " + partyId);
			}

			// ── Load saved answers into a map ─────────────────────────────────────
			List<GenericValue> savedAnswers = EntityQuery.use(delegator).from("AnswerMaster").where("partyId", partyId, "examId", examId)
							.queryList();

			Map<Long, String> answerMap = new HashMap<>();
			for (GenericValue saved : savedAnswers) {
				Long qId = saved.getLong("questionId");
				String ans = saved.getString("submittedAnswer");
				answerMap.put(qId, ans != null ? ans.trim() : "");
			}

			// ── Load per-topic pass percentages from ExamTopicMapping ─────────────
			List<GenericValue> topicMappings = EntityQuery.use(delegator).from("ExamTopicMapping").where("examId", examId).queryList();

			// topicId → required pass percentage
			Map<String, Double> topicPassPercentageMap = new HashMap<>();
			for (GenericValue tm : topicMappings) {
				String topicId = tm.getString("topicId");
				Double passPercent = tm.getDouble("topicPassPercentage");
				if (topicId != null && passPercent != null) {
					topicPassPercentageMap.put(topicId, passPercent);
				}
			}

			boolean allowNegative = exam.getLong("allowNegativeMarks") != null && exam.getLong("allowNegativeMarks") == 1L;

			// ── Per-topic accumulators ────────────────────────────────────────────
			// topicId → [score, totalMarks, correct, wrong, skipped]
			Map<String, double[]> topicStats = new HashMap<>();

			double score = 0.0;
			double totalMarks = 0.0;
			int correctCount = 0;
			int wrongCount = 0;
			int skippedCount = 0;

			List<GenericValue> examQuestions = EntityQuery.use(delegator).from("QuestionBankMasterB").where("examId", examId).queryList();

			for (GenericValue qbm : examQuestions) {
				Long questionId = qbm.getLong("questionId");
				String topicId = qbm.getString("topicId");
				BigDecimal answerValue = qbm.getBigDecimal("answerValue");
				BigDecimal negMarkValue = qbm.getBigDecimal("negativeMarkValue");
				String correctAnswer = qbm.getString("answer");

				if (answerValue == null)
					answerValue = BigDecimal.ZERO;
				if (negMarkValue == null)
					negMarkValue = BigDecimal.ZERO;

				double markForQ = answerValue.doubleValue();
				double negForQ = negMarkValue.doubleValue();
				String submitted = answerMap.getOrDefault(questionId, "");

				// Initialise topic bucket if first time seeing this topic
				topicStats.putIfAbsent(topicId, new double[] { 0, 0, 0, 0, 0 });
				// indices: 0=score, 1=totalMarks, 2=correct, 3=wrong, 4=skipped
				double[] ts = topicStats.get(topicId);

				totalMarks += markForQ;
				ts[1] += markForQ; // topic total marks

				if (submitted.isEmpty()) {
					skippedCount++;
					ts[4]++;

				} else if (correctAnswer != null && correctAnswer.trim().equalsIgnoreCase(submitted)) {
					score += markForQ;
					ts[0] += markForQ; // topic score
					correctCount++;
					ts[2]++;

				} else {
					wrongCount++;
					ts[3]++;
					if (allowNegative) {
						score -= negForQ;
						ts[0] -= negForQ;
					}
				}
			}

			// ── Overall percentage ────────────────────────────────────────────────
			double percentage = totalMarks > 0
							? BigDecimal.valueOf((score / totalMarks) * 100).setScale(2, RoundingMode.HALF_UP).doubleValue()
							: 0.0;

			// ── Per-topic pass/fail check ─────────────────────────────────────────
			boolean passed = true;
			String failedTopicId = null;

			Long overallPassPct = exam.getLong("passPercentage");
			// First check overall pass percentage
			if (overallPassPct != null && percentage < overallPassPct.doubleValue()) {
				passed = false;
			}

			// Then check each topic — if ANY topic fails, exam is failed
			if (passed) {
				for (Map.Entry<String, double[]> entry : topicStats.entrySet()) {
					String tId = entry.getKey();
					double[] ts = entry.getValue();
					double tScore = ts[0];
					double tTotal = ts[1];

					Double requiredPct = topicPassPercentageMap.get(tId);
					if (requiredPct == null)
						continue; // no rule = skip

					double topicPct = tTotal > 0
									? BigDecimal.valueOf((tScore / tTotal) * 100).setScale(2, RoundingMode.HALF_UP).doubleValue()
									: 0.0;

					if (topicPct < requiredPct) {
						passed = false;
						failedTopicId = tId; // first failing topic
						break;
					}
				}
			}

			// ── Deactivate exam session ───────────────────────────────────────────
			Map<String, Object> serviceResult2 = dispatcher.runSync("ExamActive",
							UtilMisc.toMap("examId", examId, "partyId", partyId, "isExamActive", 0L));
			if (ServiceUtil.isError(serviceResult2)) {
				TransactionUtil.rollback();
				return ServiceUtil.returnError("Error updating ExamActive");
			}

			// ── Save / update ExamResult ──────────────────────────────────────────
			Map<String, Object> examResultInput = new HashMap<>();
			examResultInput.put("partyId", partyId);
			examResultInput.put("examId", examId);
			examResultInput.put("score", new BigDecimal(score));
			examResultInput.put("totalMarks", new BigDecimal(totalMarks));
			examResultInput.put("percentage", percentage);
			examResultInput.put("passed", passed ? 1L : 0L);
			examResultInput.put("correctCount", (long) correctCount);
			examResultInput.put("wrongCount", (long) wrongCount);
			examResultInput.put("skippedCount", (long) skippedCount);
			examResultInput.put("attemptNo", noOfAttempts);
			examResultInput.put("submittedDate", new Timestamp(System.currentTimeMillis()));

			GenericValue existingResult = EntityQuery.use(delegator).from("ExamResult").where("examId", examId, "partyId", partyId)
							.queryOne();

			Map<String, Object> examResultOut;
			if (existingResult == null) {
				examResultOut = dispatcher.runSync("createExamResult", examResultInput);
			} else {
				examResultInput.put("resultId", existingResult.getString("resultId"));
				examResultOut = dispatcher.runSync("updateExamResult", examResultInput);
			}

			if (ServiceUtil.isError(examResultOut)) {
				TransactionUtil.rollback();
				return ServiceUtil.returnError("Failed to save exam result: " + ServiceUtil.getErrorMessage(examResultOut));
			}

			// ── Cleanup QuestionBankMasterB ───────────────────────────────────────
			delegator.removeByAnd("QuestionBankMasterB", UtilMisc.toMap("examId", examId));

			TransactionUtil.commit();

			// ── Build response ────────────────────────────────────────────────────
			Map<String, Object> result = ServiceUtil.returnSuccess("Exam submitted successfully");
			result.put("score", score);
			result.put("totalMarks", totalMarks);
			result.put("percentage", percentage);
			result.put("passed", passed);
			result.put("correctCount", correctCount);
			result.put("wrongCount", wrongCount);
			result.put("skippedCount", skippedCount);
			if (failedTopicId != null) {
				result.put("failedTopicId", failedTopicId); // tells caller which topic caused failure
			}
			return result;

		} catch (GenericEntityException | GenericServiceException e) {
			e.printStackTrace();
			try {
				TransactionUtil.rollback();
			} catch (Exception ignore) {
			}
			return ServiceUtil.returnError("Database error during exam submission: " + e.getMessage());
		}
	}
}