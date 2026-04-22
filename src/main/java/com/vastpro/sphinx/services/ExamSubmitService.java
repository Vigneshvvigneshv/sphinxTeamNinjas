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
	 public static Map<String, Object> submitExam(DispatchContext dctx,Map<String, Object> context) {
		 Delegator delegator = dctx.getDelegator();
		 LocalDispatcher dispatcher=dctx.getDispatcher();
		 
		 try {
			 String partyId = (String) context.get("partyId");
		     String examId  = (String) context.get("examId");
		     
		     
		     if (partyId == null || partyId.trim().isEmpty()) {
		    	 return ServiceUtil.returnError("partyId is required");		    	 
		     }
		     if (examId == null || examId.trim().isEmpty()) {
		        	return ServiceUtil.returnError("examId is required");	
		        }
		     
		    TransactionUtil.begin();
	            GenericValue exam = EntityQuery.use(delegator)
	            							.from("ExamMaster")
	            							.where("examId", examId)
	                    					.queryOne();
	            
	            if (exam == null) {
//	                TransactionUtil.rollback();
	                return ServiceUtil.returnError("Exam not found for examId: " + examId);
	            }
		        
	            
	            GenericValue partyExamRel = EntityQuery.use(delegator)
	                            .from("PartyExamRelationship")
	                            .where("partyId", partyId, "examId", examId)
	                            .queryOne();
	            if (partyExamRel == null) {
//	                TransactionUtil.rollback();
	                return ServiceUtil.returnError("User is not assigned to this exam. partyId: " + partyId);
	            }
	            
	            
	            Long allowedAttempts = partyExamRel.getLong("allowedAttempts");
	            Long noOfAttempts    = partyExamRel.getLong("noOfAttempts");
	            
	            if(allowedAttempts != null && noOfAttempts != null && noOfAttempts >= allowedAttempts) {
	                 TransactionUtil.rollback();
	                return ServiceUtil.returnError("No attempts remaining. Allowed: " + allowedAttempts + ", Used: " + noOfAttempts);
	             }
	            
	            
	            GenericValue inProgress = EntityQuery.use(delegator)
	                            .from("InProgressParty")
	                            .where("partyId", partyId, "examId", examId)
	                            .queryOne();
	            
	            
	            if(inProgress == null || inProgress.getLong("isExamActive") == null || inProgress.getLong("isExamActive") != 1L) {
	                TransactionUtil.rollback();
	               return ServiceUtil.returnError("No active exam session found for partyId: " + partyId);
	             }
	            
	            List<GenericValue> savedAnswers = EntityQuery.use(delegator)
	                            .from("AnswerMaster")
	                            .where("partyId", partyId, "examId", examId)
	                            .queryList();
	            
	            
	            // Build questionId → submittedAnswer lookup map
	            Map<Long, String> answerMap = new HashMap<>();
	            for (GenericValue saved : savedAnswers) {
	                Long qId = saved.getLong("questionId");
	                String ans = saved.getString("submittedAnswer");
	                answerMap.put(qId, ans != null ? ans.trim() : "");
	            }
	            
	            boolean allowNegative = exam.getLong("allowNegativeMarks") != null && exam.getLong("allowNegativeMarks") == 1L;
	            
	            double score = 0.0;
	            double totalMarks = 0.0;
	            int correctCount = 0;
	            int wrongCount = 0;
	            int skippedCount = 0;
	            
	            List<GenericValue> examQuestions = EntityQuery.use(delegator)
	                            .from("QuestionBankMasterB")
	                            .where("examId", examId)
	                            .queryList();
	            
	            
	            for (GenericValue qbm : examQuestions) {
	                Long questionId = qbm.getLong("questionId");
	                BigDecimal answerValue = qbm.getBigDecimal("answerValue");
	                BigDecimal negativeMarkValue = qbm.getBigDecimal("negativeMarkValue");
	                String correctAnswer = qbm.getString("answer");
	 
	                if (answerValue == null) {
	                	answerValue = BigDecimal.ZERO;
	                }
	                if (negativeMarkValue == null) {
	                	negativeMarkValue = BigDecimal.ZERO;
	                }
	 
	                totalMarks += answerValue.doubleValue();
	 
	                // default to empty if user never visited this question
	                String submitted = answerMap.getOrDefault(questionId, "");
	 
	                if (submitted.isEmpty()) {
	                    skippedCount++;
	                } else if (correctAnswer != null
	                        && correctAnswer.trim().equalsIgnoreCase(submitted)) {
	                    score += answerValue.doubleValue();
	                    correctCount++;
	                } else {
	                    wrongCount++;
	                    if (allowNegative) {
	                        score -= negativeMarkValue.doubleValue();
	                    }
	                }
	            }
	            
	            // 7. Pass / fail 
	            Long passPercentage = exam.getLong("passPercentage");
	            double percentage   = totalMarks > 0
	                    ? BigDecimal.valueOf((score / totalMarks) * 100)
	                              .setScale(2, RoundingMode.HALF_UP)
	                              .doubleValue() : 0.0;
	            boolean passed = passPercentage != null
	                    && percentage >= passPercentage.doubleValue();
	 
	         Long updatedAttempts = (noOfAttempts != null ? noOfAttempts : 0L) + 1L;
            Map<String,Object>serviceResult=dispatcher.runSync("updateNoOfAttempts", 
            						UtilMisc.toMap("examId",examId,"partyId",partyId,"noOfAttempts",updatedAttempts,
            										"lastPerformanceDate",  new Timestamp(System.currentTimeMillis())));

            
            if(ServiceUtil.isError(serviceResult)) {
            	TransactionUtil.rollback();
            	
            	return ServiceUtil.returnError("Error Updating the No of Attempts");
            }
            
            
            Map<String,Object>serviceResult2=dispatcher.runSync("ExamActive", 
    						UtilMisc.toMap("examId",examId,"partyId",partyId,"isExamActive", 0L));

    
			if (ServiceUtil.isError(serviceResult2)) {
				TransactionUtil.rollback();

				return ServiceUtil.returnError("Error Updating the No ExamActive");
			}
			
			
			// ── Save ExamResult via entity-auto service ───────────────────────────────
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
			examResultInput.put("attemptNo", updatedAttempts);
			examResultInput.put("submittedDate", new Timestamp(System.currentTimeMillis()));

			GenericValue isUser=EntityQuery.use(delegator).from("ExamResult").where("examId",examId,"partyId",partyId).queryOne();
			Map<String, Object> examResultOut=null;
			
			if(isUser==null) {
				examResultOut = dispatcher.runSync("createExamResult", examResultInput);
			}else {
				
				examResultInput.put("resultId",isUser.getString("resultId"));
				examResultOut = dispatcher.runSync("updateExamResult", examResultInput);
			}
			

			if (ServiceUtil.isError(examResultOut)) {
			    TransactionUtil.rollback();
			    return ServiceUtil.returnError("Failed to save exam result: "+ ServiceUtil.getErrorMessage(examResultOut));
			}

			TransactionUtil.commit();
			
			  Map<String, Object> result = ServiceUtil.returnSuccess("Exam submitted successfully");
			  result.put("score",score);
			  result.put("totalMarks", totalMarks);
			  result.put("percentage", percentage);
			  result.put("passed", passed);
			  result.put("correctCount", correctCount);
			  result.put("wrongCount", wrongCount);
			  result.put("skippedCount", skippedCount);
			  return result;
			  
		} catch (GenericEntityException | GenericServiceException e) {
			e.printStackTrace();
            try { TransactionUtil.rollback(); 
            }catch (Exception ignore) {}
            return ServiceUtil.returnError(
                    "Database error during exam submission: " + e.getMessage());
        }
	 }
}
