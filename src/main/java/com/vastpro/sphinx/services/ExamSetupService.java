package com.vastpro.sphinx.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class ExamSetupService {

	
	public static Map<String,Object>examSetupService(DispatchContext dctx,Map<String,Object>context){
		
		Delegator delegator=dctx.getDelegator();
		LocalDispatcher dispatcher=dctx.getDispatcher();
		String examId=(String)context.get("examId");
		
		List<String>errors=new ArrayList<String>();
		
		try {
			
			GenericValue exam=EntityQuery.use(delegator).from("ExamMaster").where("examId",examId).queryOne();
			
			if(exam==null) {
				return ServiceUtil.returnError("Exam not Found");
			}
			
			 Long setupProper = exam.getLong("examSetupProper");
	            if (setupProper != null && setupProper == 1L) {
	                return ServiceUtil.returnError( exam.getString("examName") + "] is already finalized and locked.");
	            }
			//TopicQuestionPercentage
			List<GenericValue> topicMapping=EntityQuery.use(delegator)
											.from("ExamTopicMapping")
											.where("examId",examId)
											.queryList();
			
			if(topicMapping==null || topicMapping.isEmpty()) {
				return ServiceUtil.returnError("No Topic in Exam");
			}
			double totalPercentage=0;
			double topicPercentage=0;
			for(GenericValue e:topicMapping) {
				topicPercentage=e.getDouble("percentage");
				totalPercentage=totalPercentage+topicPercentage;
			}
			
			
			if(totalPercentage!=100) {
				return ServiceUtil.returnError((int)totalPercentage +"% only assigned to this exam. Need "+(int)(100-totalPercentage)+"% more to setup" );
			}
				
			
			
			Long noOfQuestions = exam.getLong("noOfQuestions");
			
			if(noOfQuestions==0 ||noOfQuestions == null) {
				return ServiceUtil.returnError("Number Of Questions in exam is cannot be 0");
			}
			
			//calculating the questions from eachTopic
			 for (GenericValue mapping : topicMapping) {
	                String topicId    = mapping.getString("topicId");
	                Double percentage = mapping.getDouble("percentage");
	                
	                
	                GenericValue topic=EntityQuery.use(delegator).from("topicMaster").where("topicId",topicId).queryOne();
	                
	                List<GenericValue> questionList = EntityQuery.use(delegator)
	    							.from("questionMaster")
	    							.where("topicId", topicId)
	    							.queryList();
	                
	                
	                int count = (int) Math.floor((percentage / 100) * noOfQuestions);
//	                int remaining=count-questionList.size();
	                
	                
	                
	                if(count>questionList.size()) {
	    				errors.add("Not enough questions in "+topic.getString("topicName")+" add "+(count-questionList.size()+1) +" more questions" );
	    			}   
	              
			 }
			 
			 if(!errors.isEmpty()) {
				 Map<String, Object> errorResult = ServiceUtil.returnError("Exam setup validation failed. Please fix the errors below.");
				 errorResult.put("errorMessageList",errors);
				 return errorResult;
			 }
			 
			 
			 
			Map<String,Object>updateExamResult= dispatcher.runSync("updateExam", UtilMisc.toMap("examId",examId,"examSetupProper",1L));
			
			if(ServiceUtil.isError(updateExamResult)){
				 return ServiceUtil.returnError("Error in UpdateExamSetupProper");
			 }
			
			 
//			 Map<String,Object>questionServiceResult=dispatcher.runSync("generateQuestionsService", UtilMisc.toMap("examId",examId));
//			 if(ServiceUtil.isError(questionServiceResult)) {
//				 return ServiceUtil.returnError((String)questionServiceResult.get("errorMessage"));
//			 }
			 
			  Map<String, Object> result = ServiceUtil.returnSuccess("Exam setup completed successfully"); 
			 
			 return result;
			 
		}catch(Exception e) {
			e.printStackTrace();
			return  ServiceUtil.returnError("Error in Setting the Exam "+e.getMessage());
		}
	}
}
