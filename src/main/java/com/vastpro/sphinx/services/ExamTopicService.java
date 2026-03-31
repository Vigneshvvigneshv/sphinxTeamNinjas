package com.vastpro.sphinx.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class ExamTopicService {
	
	public static Map<String,Object> createExamTopicService(DispatchContext dctx,Map<String,Object> context){
		
		Delegator delegator=dctx.getDelegator();
		
		LocalDispatcher dispatcher=dctx.getDispatcher();
		
		try {
			String examId=(String)context.get("examId");
			String topicId=(String)context.get("topicId");
			String percentageStr=(String) context.get("percentage");
			String topicPassPercentageStr=(String)context.get("topicPassPercentage");
			
			
			
			
			GenericValue exam=EntityQuery.use(delegator).from("ExamMaster").where("examId",examId).queryOne();
			
			if(exam==null) {
				return ServiceUtil.returnError("exam not found");
			}
			GenericValue topic=EntityQuery.use(delegator).from("topicMaster").where("topicId",topicId).queryOne();
			
			if(topic==null) {
				return ServiceUtil.returnError("Topic Not Found");
			}
			
			if(percentageStr==null || topicPassPercentageStr==null) {
				return ServiceUtil.returnError("percenatge and topicPassPercentage are required");
			}
			
			
			Double percentage=Double.parseDouble(percentageStr);
			Double topicPassPercentage=Double.parseDouble(topicPassPercentageStr);
			
			Map<String, Object>input=new HashMap<String,Object>();
			
			input.put("examId",examId);
			input.put("topicId",topicId);
			input.put("percentage",percentage);
			input.put("topicPassPercentage",topicPassPercentage); 			
			
			Map<String,Object>serviceResult=dispatcher.runSync("createExamTopicService", input);
			
			if(ServiceUtil.isError(serviceResult)) {
				return ServiceUtil.returnError((String)serviceResult.get("errorMessage"));
			}
			
			return ServiceUtil.returnSuccess("Exam TopicMaster Created SuccessFully");
			
		}catch(GenericServiceException | GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
}
