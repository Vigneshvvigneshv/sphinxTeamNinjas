package com.vastpro.sphinx.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class ExamService {
	
	public static Map<String,Object> createExam(DispatchContext context,Map<String,Object> input){
		LocalDispatcher dispatcher=context.getDispatcher();
		Delegator delegator=context.getDelegator();
		try {
			
		GenericValue examAlreadyExits= EntityQuery.use(delegator).from("examMaster").where("examName",input.get("examName")).queryFirst();
		if(examAlreadyExits!=null) {
			return ServiceUtil.returnError("Exam Already Exits");
		}
		
		String examId=delegator.getNextSeqId("examMaster");
		examId="exam"+examId;
		Map<String,Object> createMap=new HashMap<String, Object>();
		createMap.put("examId", examId);
		createMap.put("examName",input.get("examName"));
		createMap.put("description",input.get("description"));
		createMap.put("noOfQuestions",Long.valueOf((String) input.get("noOfQuestions")));
		createMap.put("duration",Long.valueOf((String) input.get("duration")));
		createMap.put("passPercentage",Long.valueOf((String) input.get("passPercentage")));
		Map<String,Object>result=dispatcher.runSync("createExam", createMap);
		
		if(ServiceUtil.isError(result)) {
			return ServiceUtil.returnError("Error, occur during creating the Exam");
		}
		
		return ServiceUtil.returnSuccess("Exam create successfully");
		
		} catch (GenericServiceException | GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Error, occur during creating exam"+e.getMessage());
		}
	}

}
