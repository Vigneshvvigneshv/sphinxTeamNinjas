package com.vastpro.sphinx.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

public class ExamResultService {
	
	public static Map<String,Object> getExamResult(DispatchContext context,Map<String,Object> input){
		Delegator delegator=context.getDelegator();
		try {
			String partyId=(String) input.get("partyId");
			if(UtilValidate.isEmpty(partyId)) {
				Debug.logError("Party id is empty",ExamResultService.class.getName());
				return ServiceUtil.returnError("Error, Get the result contact Admin");
			}
			String examId=(String) input.get("partyId");
			if(UtilValidate.isEmpty(examId)) {
				Debug.logError("exam id is empty",ExamResultService.class.getName());
				return ServiceUtil.returnError("Error, Get the result contact Admin");
			}
			GenericValue resultList=EntityQuery.use(delegator).from("ExamResult").where("partyId",partyId,"examId",examId).queryFirst();
			Map<String,Object> result=ServiceUtil.returnSuccess("Exam Result Getted successfully");
			result.put("resultList", result);
			return result;
		}catch(GenericEntityException e) {
			Debug.logError(e.getMessage(),ExamResultService.class.getName());
			return ServiceUtil.returnError("Error, Get the result");
			
		}
	}
}
