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

public class AnswerService {
	
	
	public static Map<String,Object> answerService(DispatchContext dctx,Map<String,Object> context){
		LocalDispatcher dispatcher=dctx.getDispatcher();
		
		Delegator delegator=dctx.getDelegator();
		
		Map<String,Object>result=new HashMap<>();
		try {
			String questionIdStr=(String)context.get("questionId");
			String examId=(String)context.get("examId");
			String partyId=(String)context.get("partyId");
			String answer=(String)context.get("answer");
			
			if(questionIdStr==null) {
				return ServiceUtil.returnError("questionId is required");
			}
			
			if(examId==null) {
				return ServiceUtil.returnError("examId is required");
			}
			
			if(partyId==null) {
				return ServiceUtil.returnError("partyId is required");
			}
			
			long questionId=Long.valueOf(questionIdStr);
			
			
			
			//Fetching Question
			GenericValue AnswerQuestion=EntityQuery.use(delegator)
							.from("AnswerMaster")
							.where("examId",examId,"partyId",partyId,"questionId",questionId).queryOne();
			
			Map<String,Object>serviceResult;
			if(AnswerQuestion==null) {
				serviceResult=dispatcher.runSync("CreateAnswer",UtilMisc.toMap("examId",examId,"partyId",partyId,"questionId",questionId,"submittedAnswer",answer));
				if(ServiceUtil.isError(serviceResult)) {
					return ServiceUtil.returnError("Error in Submitting the answer");
				}
				return ServiceUtil.returnSuccess("Answer Submitted SuccessFully");
			}else {
				serviceResult=dispatcher.runSync("UpdateAnswer",UtilMisc.toMap("examId",examId,"partyId",partyId,"questionId",questionId,"submittedAnswer",answer));
				if(ServiceUtil.isError(serviceResult)) {
					return ServiceUtil.returnError("Error in Submitting the update answer");
				}
				return ServiceUtil.returnSuccess("Answer update Submitted SuccessFully");
			}
			
		}catch(GenericServiceException | GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage()) ;
		}
	}
}
