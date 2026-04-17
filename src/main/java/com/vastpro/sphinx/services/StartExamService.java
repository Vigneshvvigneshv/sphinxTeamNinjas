package com.vastpro.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class StartExamService {
	public static Map<String, Object> startExam(DispatchContext context,Map<String, Object> input){
		LocalDispatcher dispatcher=context.getDispatcher();
		Delegator delegator=context.getDelegator();
		try {
			String partyId=(String) input.get("partyId");
			if(UtilValidate.isEmpty(partyId)){
				Debug.logError("partyId is null or empty",StartExamService.class.getName());
				return ServiceUtil.returnError("Something Went wrong, Please try again later!");
			}
			String examId=(String) input.get("examId");
			if(UtilValidate.isEmpty(examId)){
				Debug.logError("ExamId is null or empty",StartExamService.class.getName());
				return ServiceUtil.returnError("Something Went wrong, Please try again later!");
			}
			
			GenericValue examExits=EntityQuery.use(delegator).from("PartyExamRelationship").where("partyId",partyId,"examId",examId).queryFirst();
			if(examExits==null) {
				Debug.logError("Exam is not Assigned to the user",StartExamService.class.getName());
				return ServiceUtil.returnError("Error, contact the Admin");
			}
			GenericValue exam=EntityQuery.use(delegator).from("ExamMaster").where("examId",examId).queryFirst();
			if(exam==null) {
				Debug.logError("Exam is not Found",StartExamService.class.getName());
				return ServiceUtil.returnError("Error, contact the Admin");
			}
			
			input.put("isExamActive",1);
			input.put("remainingTime",String.valueOf(exam.getLong("duration")));
			input.put("totalAnswered",0);
			input.put("totalRemaining",String.valueOf(exam.getLong("noOfQuestions")));
			Map<String,Object> result=dispatcher.runSync("startExam", input);
			if(ServiceUtil.isError(result)) {
				return ServiceUtil.returnError("Error, occur during start exam"+result.get("errorMessage"));
			}
			return ServiceUtil.returnSuccess("Exam Started"); 
		}catch(GenericEntityException |GenericServiceException e) {
			Debug.logError(e.getMessage(),StartExamService.class.getName());
			return ServiceUtil.returnError("Something Went wrong, Please try again later!");
		}
	}

}
