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
	
	
	public static Map<String,Object> generateQuestionsService(DispatchContext dctx,Map<String,Object> context){
		
		Delegator delegator=dctx.getDelegator();
		
		LocalDispatcher dispatcher=dctx.getDispatcher();
		
		
		
		try {
			String examId=(String)context.get("examId");
			List<Map<String,Object>> topicList=(List<Map<String, Object>>) context.get("TopicList");
			
			//getting the exam
			GenericValue exam=EntityQuery.use(delegator).from("ExamMaster").where("examId",examId).queryOne();
			
			Long noOfQuestions=exam.getLong("noOfQuestions");
			
			//storing final questions to add in the quetionBankMaster B
			List<GenericValue> finalQuestions = new ArrayList<>();
			
			//adding number of questions based on each topic percentage
			Map<String,Integer>topicQuestionCount= new HashMap<>();
			
			
			int assingedTotal=0;
			
			
			
			//this loop for adding the each topic questions
			for(Map<String,Object> topic:topicList) {
				String topicId=(String) topic.get("topicId");
				String percentageStr=(String.valueOf(topic.get("percentage")));
				
				Double percentage=Double.valueOf(percentageStr);
				int count=(int)Math.floor((percentage/100)*noOfQuestions);
				
				topicQuestionCount.put(topicId, count);
				
				assingedTotal += count;;		
			}
			
				//Remaining question 
				int remaining=(int) (noOfQuestions-assingedTotal);
				
				List <String> topicIds=new ArrayList<>();
				
				for(Map<String,Object> topic:topicList) {
					topicIds.add((String)topic.get("topicId"));
				}
				
				//Generating Random Topic To fetch question from question Master based on topic
				Random random=new Random();
				while (remaining > 0) {
				    int randomIndex = random.nextInt(topicIds.size());
				    String randomTopicId = topicIds.get(randomIndex);
    
				 
				    topicQuestionCount.put(randomTopicId,topicQuestionCount.get("randomTopicId")+1);

				    remaining--;
				}
				
				
				 //Fetch & pick questions from question master
				for (Map<String, Object> topic : topicList) {

				    String topicId = (String) topic.get("topicId");
				    int requiredCount = topicQuestionCount.get(topicId);

				    List<GenericValue> questionList = EntityQuery.use(delegator)
				            .from("questionMaster")
				            .where("topicId", topicId)
				            .queryList();

				    // Shuffle questions
				    Collections.shuffle(questionList, random);

				    int limit = Math.min(requiredCount, questionList.size());

				    for (int i = 0; i < limit; i++) {
				        finalQuestions.add(questionList.get(i));
				    }
				}
				
				if (finalQuestions.size() < noOfQuestions) {

				    List<GenericValue> allQuestions = EntityQuery.use(delegator)
				            .from("questionMaster")
				            .queryList();

				    Collections.shuffle(allQuestions, random);

				    for (GenericValue q : allQuestions) {
				        if (finalQuestions.size() >= noOfQuestions) break;

				        if (!finalQuestions.contains(q)) {
				            finalQuestions.add(q);
				        }
				    }
				}

				// If somehow exceeded (safety)
				if (finalQuestions.size() > noOfQuestions) {
				    finalQuestions = finalQuestions.subList(0, noOfQuestions.intValue());
				}
				
				
				for(GenericValue question:finalQuestions) {
					//addQuestionInMasterB;
					
					Map<String,Object>input=new HashMap<>();
					
					input.put("examId", examId);
					input.put("questionId",(question.get("questionId")));
					input.put("questionDetail",(question.get("questionDetail")));
					input.put("optionA",(question.get("optionA")));
					input.put("optionB",(question.get("optionB")));
					input.put("optionC",(question.get("optionC")));
					input.put("optionD",(question.get("optionD")));
					input.put("answer",(question.get("answer")));
					input.put("numAnswers",(question.get("numAnswers")));
					input.put("questionTypeId",(question.get("questionTypeId")));
					input.put("difficultyLevel",(question.get("difficultyLevel")));
					input.put("answerValue",(question.get("answerValue")));	
					input.put("topicId",(question.get("topicId")));	
					input.put("negativeMarkValue",(question.get("negativeMarkValue")));
					
					
					Map<String,Object> serviceResult=dispatcher.runSync("addQuestionInMasterB", input);
					
					if(ServiceUtil.isError(serviceResult)) {
						return ServiceUtil.returnError((String)serviceResult.get("errorMessage"));
					}	
				}
				return ServiceUtil.returnSuccess("Questions added in questionBankMasterB successFully ");
				
				
		}catch(GenericServiceException | GenericEntityException e  ) {
			return ServiceUtil.returnError("Failed to create Topic");
		}catch(Exception e) {
			return ServiceUtil.returnError("Failed to create Topic");
		}
	}
}
