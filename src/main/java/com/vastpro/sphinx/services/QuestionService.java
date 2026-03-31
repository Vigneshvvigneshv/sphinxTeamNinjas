package com.vastpro.sphinx.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class QuestionService {
	public static Map<String,Object>createQuestionService(DispatchContext dctx, Map<String, Object> questions){
		LocalDispatcher dispatcher=dctx.getDispatcher();
		try {
			Delegator delegator = dctx.getDelegator();
			
			String topicId=(String)questions.get("topicId");
			String questionDetail=(String)questions.get("questionDetail");
			String questionTypeId=(String)questions.get("questionTypeId");
			String answer = (String)questions.get("answer");
			
			
			
			if(topicId==null || questionDetail==null || answer==null  ) {
				return ServiceUtil.returnError("topic Id and quetionDetail and answer are required");
			}
			
			//check topic exists
			GenericValue topic =EntityQuery.use(delegator).from("topicMaster").where("topicId",topicId).queryOne();
			
			if(topic==null) {
				return ServiceUtil.returnError("Topic not Found");
			}
			
		String questionId=delegator.getNextSeqId("questionMaster");
			
//			GenericValue question =delegator.makeValue("questionMaster");
//			question.set("questionId",       questionId);
//            question.set("questionDetail",   questionDetail);
//            question.set("optionA",          questions.getOrDefault("optionA", ""));
//            question.set("optionB",          questions.getOrDefault("optionB", ""));
//            question.set("optionC",          questions.getOrDefault("optionC", ""));
//            question.set("optionD",          questions.getOrDefault("optionD", ""));
//            question.set("optionE",          questions.getOrDefault("optionE", ""));
//            question.set("answer",           answer);
//            question.set("numAnswers",       questions.getOrDefault("numAnswers", 1L));
//            question.set("questionTypeId",   questionTypeId);
//            question.set("difficultyLevel",  questions.getOrDefault("difficultyLevel", 1L));
//            question.set("answerValue",      questions.getOrDefault("answerValue", 1.0));
//            question.set("topicId",          topicId);
//            question.set("negativeMarkValue",questions.getOrDefault("negativeMarkValue", 0.0));
//            delegator.create(question);
			
			dispatcher.runSync("createQuestion", questions);
            
			Map<String, Object> result = ServiceUtil.returnSuccess("Question created Successfully");
            result.put("responseMessage", "Question created Successfully");
            result.put("questionId", questionId);
			return result;
		}catch(GenericEntityException | GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Failed to create Question");
		}
	}
	
	//Update Questions
	public static Map<String, Object> updateQuestion(DispatchContext dctx, Map<String, Object> context) {

	    Delegator delegator = dctx.getDelegator();
	    LocalDispatcher dispatcher=dctx.getDispatcher();

	    try {
	        // get questionId from context (sent by frontend)
	    	Long questionId = (Long) context.get("questionId"); 

	        if (questionId == null) {
	            return ServiceUtil.returnError("questionId is required for update");
	        }

	        // check question exists in DB
	        GenericValue question = EntityQuery.use(delegator).from("questionMaster")
	        				.where("questionId", questionId).queryOne();

	        if (question == null) {
	            return ServiceUtil.returnError("Question not found for questionId: " + questionId);
	        }

	        // get all fields from context
	        String questionDetail = (String)context.get("questionDetail");
	        String optionA = (String)context.get("optionA");
	        String optionB = (String)context.get("optionB");
	        String optionC = (String)context.get("optionC");
	        String optionD = (String)context.get("optionD");
	        String answer = (String)context.get("answer");
	        Long numAnswers = (Long)context.get("numAnswers");
	        String questionTypeId = (String)context.get("questionTypeId");
	        Long difficultyLevel = (Long)context.get("difficultyLevel");
	        BigDecimal answerValue = (BigDecimal)context.get("answerValue");
	        String topicId = (String)context.get("topicId");
	        BigDecimal negativeMarkValue = (BigDecimal)context.get("negativeMarkValue");

	       
	       
	        Map<String,Object> updateQuestion=new HashMap<>();
	        
	        
	        
	        updateQuestion.put("questionId", questionId);
	        updateQuestion.put("questionTypeId", questionTypeId);
	        updateQuestion.put("questionDetail", questionDetail);
	        updateQuestion.put("optionA", optionA);
	        updateQuestion.put("optionB", optionB);
	        updateQuestion.put("optionC", optionC);
	        updateQuestion.put("optionD", optionD);
	        updateQuestion.put("answer", answer);
	        updateQuestion.put("numAnswers", numAnswers);
	        updateQuestion.put("difficultyLevel", difficultyLevel);
	        updateQuestion.put("answerValue", answerValue);
	        updateQuestion.put("topicId", topicId);
	        updateQuestion.put("negativeMarkValue", negativeMarkValue);
	        
	        
	       
	        if (topicId != null) {
	            GenericValue topic = EntityQuery.use(delegator)
	                .from("topicMaster")
	                .where("topicId", topicId)
	                .queryOne();

	            if (topic == null) {
	                return ServiceUtil.returnError("Topic not found for topicId: " + topicId);
	            }
	            
	        }

	        if (questionTypeId!= null) {
	            GenericValue questionType = EntityQuery.use(delegator)
	                .from("Enumeration")
	                .where("enumId", questionTypeId, "enumTypeId", "QUESTION_TYPE")
	                .queryOne();

	            if (questionType == null) {
	                return ServiceUtil.returnError("Invalid questionTypeId: " + questionTypeId);
	            }
	            question.set("questionTypeId", questionTypeId);
	        }

	      
	   Map<String,Object>result=  dispatcher.runSync("updateQuestion", updateQuestion);
	        
	        
	   if(ServiceUtil.isError(result)) {
			return ServiceUtil.returnError((String)result.get("errorMessage"));
		}
	   return ServiceUtil.returnSuccess("Question updated successfully");
	        
	        
	    } catch (GenericEntityException | GenericServiceException e) {
	        return ServiceUtil.returnError("Error updating question: " + e.getMessage());
	    }
	}
	
	
	//DeleteQuestionservice
	public static Map<String,Object> deleteQuestion(DispatchContext dctx, Map<String, Object> context){
		LocalDispatcher dispatcher=dctx.getDispatcher();
		Delegator delegator = dctx.getDelegator();
		try {
			 Long questionId = (Long) context.get("questionId");
			 
			 if (questionId == null) {
		            return ServiceUtil.returnError("questionId is required");
		        }
			 
			 GenericValue question = EntityQuery.use(delegator)
					            .from("questionMaster")
					            .where("questionId", questionId)
					            .queryOne();
			 
			 if (question == null) {
		            return ServiceUtil.returnError("Question not found for questionId: ");	      
		        }
			 
			Map<String,Object>result= dispatcher.runSync("deleteQuestion", context);
			
			if(ServiceUtil.isError(result)) {
				return ServiceUtil.returnError((String)result.get("errorMessage"));
			}
			
			return ServiceUtil.returnSuccess("Question deleted successfully");
		  
			 
		}catch(GenericEntityException | GenericServiceException e) {
			return ServiceUtil.returnError("Error deleting question: " + e.getMessage());
		}
	}
	
	public static Map<String,Object> getQuestionsByTopic(DispatchContext dctx,Map<String,Object> context){
		Delegator delegator=dctx.getDelegator();
		
		try {
			
			String topicId=(String)context.get("topicId");
			Integer pageNo=(Integer)context.get("pageNo");
			Integer pageSize=(Integer)context.get("pageSize");
			
			
			
			if(topicId==null || topicId.trim().isEmpty()) {
				return ServiceUtil.returnError("topicId is Required");
			}
			
			if(pageNo==null || pageNo<1) {
				pageNo=1;
			}
			if(pageSize==null || pageSize<1) {
				pageSize=10;
			}
			
			GenericValue topic=EntityQuery.use(delegator).from("topicMaster").where("topicId",topicId).queryOne();
			
			if(topic == null) {
				return ServiceUtil.returnError("Topic not Found");
			}
			
			
			long totalCount=EntityQuery.use(delegator).from("questionMaster").where("topicId",topicId).queryCount();
			
			int totalPages=(int) Math.ceil((double)totalCount/pageSize);
			int offset=(pageNo-1)*pageSize;
			
			
			List<GenericValue> questions=EntityQuery.use(delegator)
							.from("questionMaster")
							.where("topicId",topicId)
							.orderBy("questionId")
							.cursorScrollInsensitive()
							.maxRows(pageSize)
							.queryList();
			
			if(offset>questions.size()) {
				questions=new ArrayList<>();
			}else {
				questions=questions.subList(offset,
								Math.min(offset+pageSize,questions.size()));
			}
			
			List<Map<String,Object>> questionList=new ArrayList<>();
			
			for(GenericValue q:questions) {
				Map<String,Object> qMap=new HashMap<>();
				
				qMap.put("questionId", q.getLong("questionId"));
				qMap.put("questionDetail", q.getString("questionDetail"));
				qMap.put("optionA", q.getString("optionA"));
				qMap.put("optionB", q.getString("optionB"));
				qMap.put("optionC", q.getString("optionC"));
				qMap.put("optionD", q.getString("optionD"));
				qMap.put("numAnswers", q.getLong("numAnswers"));
				qMap.put("questionTypeId", q.getString("questionTypeId"));
				qMap.put("difficultyLevel", q.getString("difficultyLevel"));
				qMap.put("topicId", q.getString("topicId"));
				qMap.put("negativeMarkValue", q.getBigDecimal("negativeMarkValue"));
				
				
				questionList.add(qMap);
			}
			
			Map<String,Object> result=ServiceUtil.returnSuccess();
			
			result.put("topicId", topic.getString("topicId"));
			result.put("topicName", topic.getString("topicName"));
			result.put("totalCount", totalCount);
			result.put("questionList", questionList);
			
			
			result.put("pageNo",pageNo);
			result.put("pageSize",pageSize);
			result.put("totalPages", totalPages);
			result.put("hasNext", pageNo<totalPages);
			result.put("hasPrevious",pageNo>1);
			
			
			return result;
		}catch(GenericEntityException e) {
			return ServiceUtil.returnError("Error fetching questions By topic: "+e.getMessage());
		}
	}
}
