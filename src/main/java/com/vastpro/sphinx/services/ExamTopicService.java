package com.vastpro.sphinx.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
			return ServiceUtil.returnError("");
		}
	}
	
	
	//Getting topicBy ExamId
	public static Map<String,Object> getTopicByExamIdService(DispatchContext dctx,Map<String,Object>context){
		
		
		Delegator delegator=dctx.getDelegator();	
		
		
		try {
			
			String examId=(String)context.get("examId");
			
			if(examId==null || examId.trim().isEmpty()) {
				return ServiceUtil.returnError("examId is Required");
			}
			
			GenericValue exam=EntityQuery.use(delegator).from("ExamMaster").where("examId",examId).queryOne();
			
			if(exam == null) {
				return ServiceUtil.returnError("Exam not Found");
			}
			
			List<GenericValue> exam1=EntityQuery.use(delegator).from("ExamTopicMapping").where("examId",examId).queryList();
			
		
			List<Map<String,Object>> topicList=new ArrayList<>();
			
			for(GenericValue e:exam1) {
				
				Map<String,Object> tMap=new HashMap<>();
				String topicId=e.getString("topicId");
				GenericValue topic =EntityQuery.use(delegator).from("topicMaster").where("topicId",topicId).queryOne();
				GenericValue topicPercentage=EntityQuery.use(delegator).from("ExamTopicMapping").where("examId",examId,"topicId",topicId).queryOne();
				String topicName=topic.getString("topicName");
				String percent=topicPercentage.getString("percentage");
				String topicPassPercentage=topicPercentage.getString("topicPassPercentage");
				
				tMap.put("topicId",topicId);
				tMap.put("topicName",topicName);
				tMap.put("percentage",percent);
				tMap.put("topicPassPercentage",topicPassPercentage);
				topicList.add(tMap);
				
			}
			
			
			
			Map<String,Object> result=ServiceUtil.returnSuccess();
			
			result.put("examId", exam.getString("examId"));
			result.put("examName", exam.getString("examName"));
			result.put("topicList", topicList);
			
			
			return result;
			
		}catch(Exception e) {
			return ServiceUtil.returnError("Error Fetching in Topic "+e.getMessage());
		}
	}
	
	public static Map<String,Object> deleteTopicInExamTopic(DispatchContext dctx,Map<String,Object> context){
		
	
		LocalDispatcher dispatcher=dctx.getDispatcher();
		try {
			String topicId=(String)context.get("topicId");
			String examId=(String)context.get("examId");
			
			if(topicId==null || topicId.trim().isEmpty() || examId==null || examId.trim().isEmpty()) {
				return ServiceUtil.returnError("topicId and examId is required");
			}
			
			
			Map<String, Object>result=dispatcher.runSync("deleteTopicInExamTopicDB", context);
			
			if(ServiceUtil.isError(result)) {
				return ServiceUtil.returnError((String)result.get("errorMessage"));
			}
			
			return ServiceUtil.returnSuccess("Topic Deleted Successfully");
		}catch(GenericServiceException e) {
			e.printStackTrace();
			
			return ServiceUtil.returnError("Failed to Delete Topic");
		}
	}
}
