package com.vastpro.sphinx.services;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

public class QuestionService {
	public static Map<String,Object>createQuestionService(DispatchContext dctx, Map<String, Object> questions){
		Map<String, Object> result = ServiceUtil.returnSuccess();
		
		try {
			Delegator delegator = dctx.getDelegator();
			
			String topicId=(String)questions.get("topicId");
			String questionDetail=(String)questions.get("questionDetail");
			String questionTypeId=(String)questions.get("questionTypeId");
			String  answer = (String)questions.get("answer");
			
			if(topicId==null || questionDetail==null || answer==null  ) {
				return ServiceUtil.returnError("topic Id and quetionDetail and answer are required");
			}
			
			//check topic exists
			GenericValue topic =EntityQuery.use(delegator).from("topicMaster").where("topicId",topicId).queryOne();
			
			if(topic==null) {
				return ServiceUtil.returnError("Topic not Found");
			}
			
			String questionId=delegator.getNextSeqId("questionMaster");
			
			GenericValue question =delegator.makeValue("questionMaster");
			question.set("questionId",       questionId);
            question.set("questionDetail",   questionDetail);
            question.set("optionA",          questions.getOrDefault("optionA", ""));
            question.set("optionB",          questions.getOrDefault("optionB", ""));
            question.set("optionC",          questions.getOrDefault("optionC", ""));
            question.set("optionD",          questions.getOrDefault("optionD", ""));
            question.set("optionE",          questions.getOrDefault("optionE", ""));
            question.set("answer",           answer);
            question.set("numAnswers",       questions.getOrDefault("numAnswers", 1L));
            question.set("questionTypeId",   questionTypeId);
            question.set("difficultyLevel",  questions.getOrDefault("difficultyLevel", 1L));
            question.set("answerValue",      questions.getOrDefault("answerValue", 1.0));
            question.set("topicId",          topicId);
            question.set("negativeMarkValue",questions.getOrDefault("negativeMarkValue", 0.0));
            delegator.create(question);
			
            
            result.put("responseMessage", "Question created Successfully");
            result.put("questionId", questionId);
			return result;
		}catch(GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Failed to create Question");
		}
	}
	
	//Update Questions
	public static Map<String, Object> updateQuestion(DispatchContext dctx, Map<String, Object> context) {

	    Delegator delegator = dctx.getDelegator();

	    try {
	        // Step 1 — Get questionId from context (sent by frontend)
	    	Long questionId = (Long) context.get("questionId"); 

	        if (questionId == null) {
	            return ServiceUtil.returnError("questionId is required for update");
	        }

	        // Step 2 — Check question exists in DB
	        GenericValue question = EntityQuery.use(delegator).from("questionMaster")
	        				.where("questionId", questionId).queryOne();

	        if (question == null) {
	            return ServiceUtil.returnError("Question not found for questionId: " + questionId);
	        }

	        // Step 3 — Get all fields from context
	        String     questionDetail  = (String)     context.get("questionDetail");
	        String     optionA         = (String)     context.get("optionA");
	        String     optionB         = (String)     context.get("optionB");
	        String     optionC         = (String)     context.get("optionC");
	        String     optionD         = (String)     context.get("optionD");
	        String     optionE         = (String)     context.get("optionE");
	        String     answer          = (String)     context.get("answer");
	        Long       numAnswers      = (Long)        context.get("numAnswers");
	        String     questionTypeId  = (String)     context.get("questionTypeId");
	        Long       difficultyLevel = (Long)        context.get("difficultyLevel");
	        BigDecimal answerValue     = (BigDecimal) context.get("answerValue");
	        String     topicId         = (String)     context.get("topicId");
	        BigDecimal negMarkValue    = (BigDecimal) context.get("negativeMarkValue");

	        // Step 4 — Update only fields that are passed
	        //          if null → keep existing value in DB
	        if (questionDetail != null) {
	        	question.set("questionDetail",questionDetail);
	        }
	        
	        if (optionA != null) {
	        	question.set("optionA",optionA);
	        }
	        
	        if (optionB != null) {
	        	question.set("optionB",optionB);
	        }
	        if (optionC != null) {
	        	question.set("optionC",optionC);
	        }
	        if (optionD != null) {
	        	question.set("optionD",optionD);
	        }
	        if (optionE != null) {
	        	question.set("optionE",optionE);
	        }
	        if (answer != null) {
	        	question.set("answer",answer);
	        }
	        if (numAnswers != null) {
	        	question.set("numAnswers",        numAnswers);
	        }
	        if (difficultyLevel!= null) {
	        	question.set("difficultyLevel",   difficultyLevel);
	        }
	        if (answerValue != null) {
	        	question.set("answerValue",       answerValue);
	        }
	        if (negMarkValue != null) {
	        	question.set("negativeMarkValue", negMarkValue);
	        }

	       
	        if (topicId != null) {
	            GenericValue topic = EntityQuery.use(delegator)
	                .from("topicMaster")
	                .where("topicId", topicId)
	                .queryOne();

	            if (topic == null) {
	                return ServiceUtil.returnError("Topic not found for topicId: " + topicId);
	            }
	            question.set("topicId", topicId);
	        }

	       
	        if (questionTypeId != null) {
	            GenericValue questionType = EntityQuery.use(delegator)
	                .from("Enumeration")
	                .where("enumId", questionTypeId, "enumTypeId", "QUESTION_TYPE")
	                .queryOne();

	            if (questionType == null) {
	                return ServiceUtil.returnError("Invalid questionTypeId: " + questionTypeId);
	            }
	            question.set("questionTypeId", questionTypeId);
	        }

	        // Step 7 — Save to DB
	        question.store();

	        Map<String, Object> result = ServiceUtil.returnSuccess("Question updated successfully");
	        result.put("questionId", questionId);
	        return result;

	    } catch (GenericEntityException e) {
	        return ServiceUtil.returnError("Error updating question: " + e.getMessage());
	    }
	}
	
	
	
}
