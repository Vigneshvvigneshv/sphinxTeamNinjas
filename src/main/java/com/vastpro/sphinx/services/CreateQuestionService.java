package com.vastpro.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

public class CreateQuestionService {
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
//			GenericValue topic =EntityQuery.use(delegator).from("topicMaster").where("topicId",topicId).queryOne();
//			
//			if(topic==null) {
//				return ServiceUtil.returnError("Topic not Found");
//			}
			
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
			
			return result;
		}catch(GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Failed to create Question");
		}
	}
}
