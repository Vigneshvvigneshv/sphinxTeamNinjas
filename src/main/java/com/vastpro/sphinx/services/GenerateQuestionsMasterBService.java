package com.vastpro.sphinx.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class GenerateQuestionsMasterBService {

	public static Map<String, Object> generateQuestionsService(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();

		try {
			String examId = (String) context.get("examId");

			// Get the exam
			GenericValue exam = EntityQuery.use(delegator)
							.from("ExamMaster")
							.where("examId", examId)
							.queryOne();

			if (exam == null) {
				return ServiceUtil.returnError("Exam not found");
			}

			Long noOfQuestions = exam.getLong("noOfQuestions");

			if (noOfQuestions == null || noOfQuestions == 0) {
				return ServiceUtil.returnError("Invalid number of questions in exam");
			}

			List<GenericValue> topicList = EntityQuery.use(delegator)
							.from("ExamTopicMapping")
							.where("examId", examId)
							.queryList();

			if (topicList == null || topicList.isEmpty()) {
				return ServiceUtil.returnError("No topics found for this exam");
			}

			// Final questions list to insert into QuestionBankMasterB
			List<GenericValue> finalQuestions = new ArrayList<>();

			// Calculate how many questions per topic based on percentage
			Map<String, Integer> topicQuestionCount = new HashMap<>();
			int assignedTotal = 0;

			for (GenericValue topic : topicList) {
				String topicId = topic.getString("topicId");
				Double percentage = topic.getDouble("percentage");

				if (percentage == null) percentage = 0.0;

				List<GenericValue> questionList = EntityQuery.use(delegator)
								.from("questionMaster")
								.where("topicId", topicId)
								.queryList();

				if (percentage > 0 && (questionList == null || questionList.isEmpty())) {
					return ServiceUtil.returnError("No Questions found in " + topic.get("topicName"));
				}

				int count = (int) Math.floor((percentage / 100.0) * noOfQuestions);
				topicQuestionCount.put(topicId, count);
				assignedTotal += count;
			}

			// Distribute remaining questions randomly across topics
			int remaining = (int) (noOfQuestions - assignedTotal);
			List<String> topicIds = new ArrayList<>();
			for (GenericValue topic : topicList) {
				topicIds.add(topic.getString("topicId"));
			}

			Random random = new Random();
			while (remaining > 0) {
				String randomTopicId = topicIds.get(random.nextInt(topicIds.size()));
				topicQuestionCount.put(randomTopicId, topicQuestionCount.getOrDefault(randomTopicId, 0) + 1);
				remaining--;
			}

			// Fetch and pick questions per topic with hard cap and proper deduplication
			for (GenericValue topic : topicList) {
				String topicId = topic.getString("topicId");
				int requiredCount = topicQuestionCount.getOrDefault(topicId, 0);

				if (requiredCount == 0) continue;

				List<GenericValue> questionList = EntityQuery.use(delegator)
								.from("questionMaster")
								.where("topicId", topicId)
								.queryList();

				Collections.shuffle(questionList, random);

				int limit = Math.min(requiredCount, questionList.size());

				for (int i = 0; i < limit; i++) {
					// Hard cap — never exceed noOfQuestions
					if (finalQuestions.size() >= noOfQuestions) break;

					GenericValue q = questionList.get(i);
					Long qId = q.getLong("questionId");

					// Deduplicate by questionId (not object reference)
					boolean alreadyAdded = finalQuestions.stream()
									.anyMatch(f -> qId.equals(f.getLong("questionId")));

					if (!alreadyAdded) {
						finalQuestions.add(q);
					}
				}
			}

			// Fallback: fill remaining slots from all questions if still short
			if (finalQuestions.size() < noOfQuestions) {
				List<GenericValue> allQuestions = EntityQuery.use(delegator)
								.from("questionMaster")
								.queryList();

				Collections.shuffle(allQuestions, random);

				for (GenericValue q : allQuestions) {
					if (finalQuestions.size() >= noOfQuestions) break;

					Long qId = q.getLong("questionId");
					boolean alreadyAdded = finalQuestions.stream()
									.anyMatch(f -> qId.equals(f.getLong("questionId")));

					if (!alreadyAdded) {
						finalQuestions.add(q);
					}
				}
			}

			// Safety trim (should never trigger with fixes above)
			if (finalQuestions.size() > noOfQuestions) {
				finalQuestions = finalQuestions.subList(0, noOfQuestions.intValue());
			}

			// Insert into QuestionBankMasterB
			for (GenericValue question : finalQuestions) {

				// Skip if already exists in QuestionBankMasterB
				GenericValue alreadyExists = EntityQuery.use(delegator)
								.from("QuestionBankMasterB")
								.where("questionId", question.get("questionId"), "examId", examId, "topicId", question.get("topicId"))
								.queryOne();

				if (alreadyExists != null) continue;

				Map<String, Object> input = new HashMap<>();
				input.put("examId", examId);
				input.put("questionId", question.get("questionId"));
				input.put("questionDetail", question.get("questionDetail"));
				input.put("optionA", question.get("optionA"));
				input.put("optionB", question.get("optionB"));
				input.put("optionC", question.get("optionC"));
				input.put("optionD", question.get("optionD"));
				input.put("answer", question.get("answer"));
				input.put("numAnswers", question.get("numAnswers"));
				input.put("questionTypeId", question.get("questionTypeId"));
				input.put("difficultyLevel", question.get("difficultyLevel"));
				input.put("answerValue", question.get("answerValue"));
				input.put("topicId", question.get("topicId"));
				input.put("negativeMarkValue", question.get("negativeMarkValue"));

				Map<String, Object> serviceResult = dispatcher.runSync("addQuestionInMasterB", input);

				if (ServiceUtil.isError(serviceResult)) {
					return ServiceUtil.returnError("Error in adding the Question");
				}
			}

			return ServiceUtil.returnSuccess("Questions added in QuestionBankMasterB successfully");

		} catch (GenericServiceException | GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Failed to generate questions: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Unexpected error: " + e.getMessage());
		}
	}
}