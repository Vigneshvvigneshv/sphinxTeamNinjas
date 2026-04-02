package com.vastpro.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class ExamAssignToUserService {

	public static Map<String, Object> assignExam(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator = context.getDelegator();

		try {
			GenericValue partyIdAlreadyExits = EntityQuery.use(delegator).from("UserLogin")
					.where("partyId", input.get("partyId")).queryFirst();
			if (partyIdAlreadyExits == null) {
				return ServiceUtil.returnError("User not found");
			}
			GenericValue examIdAlreadyExits = EntityQuery.use(delegator).from("ExamMaster")
					.where("examId", input.get("examId")).queryFirst();
			if (examIdAlreadyExits == null) {
				return ServiceUtil.returnError("Exam not found");
			}
			try {
			input.put("allowedAttempts",Long.valueOf((String) input.get("allowedAttempts")));
			}catch(NumberFormatException e) {
				return ServiceUtil.returnError("Attempts should be number");
			}
			
			try {
				input.put("timeoutDays",Long.valueOf((String) input.get("timeoutDays")));
				}catch(NumberFormatException e) {
					return ServiceUtil.returnError("Days should be number");
				}
			input.put("noOfAttempts", 0);
			Map<String, Object> result = dispatcher.runSync("assignExam", input);

			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError("Error, occur during assing the Exam");
			}
			return ServiceUtil.returnSuccess("User Assigned to the exam successfully");

		} catch (GenericEntityException | GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Debug.logError(e.getMessage(), ExamAssignToUserService.class.getName());
			return ServiceUtil.returnError("Error, occur during assing the Exam" + e.getMessage());
		}
	}

	
	public static Map<String, Object> removeAssignedExam(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator = context.getDelegator();

		try {

			String partyId = (String) input.get("partyId");
			String examId = (String) input.get("examId");
			GenericValue partyIdAlreadyExits = EntityQuery.use(delegator).from("PartyExamRelationship").where("partyId", partyId)
					.queryFirst();
			if (partyIdAlreadyExits == null) {
				return ServiceUtil.returnError("User not found");
			}
			GenericValue examIdAlreadyExits = EntityQuery.use(delegator).from("PartyExamRelationship").where("examId", examId)
					.queryFirst();
			if (examIdAlreadyExits == null) {
				return ServiceUtil.returnError("Exam not found");
			}

			GenericValue examAssignId = EntityQuery.use(delegator).from("PartyExamRelationship")
					.where("partyId", partyId, "examId", examId).queryFirst();
			if (examAssignId == null) {
				return ServiceUtil.returnError("Exam is already not assigned to the user");
			}
			Map<String, Object> result = dispatcher.runSync("removeAssignedValue",input );

			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError("Error, occur during remove the assigned Exam");
//				return ServiceUtil.returnError((String)result.get("errorMessage"));
			}
			return ServiceUtil.returnSuccess("Exam removed successfully");

		} catch (GenericEntityException | GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Error, occur during remove the assigned Exam" + e.getMessage());
		}
	}
	
	public static Map<String,Object> increaseAttempts(DispatchContext context,Map<String,Object> input){
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator = context.getDelegator();

		try {
			GenericValue partyIdAlreadyExits = EntityQuery.use(delegator).from("PartyExamRelationship")
					.where("partyId", input.get("partyId")).queryFirst();
			if (partyIdAlreadyExits == null) {
				return ServiceUtil.returnError("User not found");
			}
			GenericValue examIdAlreadyExits = EntityQuery.use(delegator).from("PartyExamRelationship")
					.where("examId", input.get("examId")).queryFirst();
			if (examIdAlreadyExits == null) {
				return ServiceUtil.returnError("Exam not found");
			}
			
			GenericValue getRecords=EntityQuery.use(delegator).from("PartyExamRelationship")
					.where("partyId",input.get("partyId"),"examId", input.get("examId")).queryFirst();
			if(getRecords==null) {
				return ServiceUtil.returnError("Exam is already not assigned to the user");
			}
			
			Long numberOfAttempts = getRecords.getLong("noOfAttempts");
			Long allowAttempts =getRecords.getLong("allowedAttempts");
		
			if(numberOfAttempts<allowAttempts) {
				input.put("noOfAttempts", (numberOfAttempts+1));
			}else {
				return ServiceUtil.returnError("User reached a maximum attempts");
			}
			
			Map<String,Object> result=dispatcher.runSync("increaseAttempts", input);
			
			if(ServiceUtil.isError(result)) {
				Debug.logError((String)result.get("errorMessage"), ExamAssignToUserService.class.getName());
				return ServiceUtil.returnError("Error, occur during launch the exam"); 
			}
			return ServiceUtil.returnSuccess("exam Launched Successfully");
		}catch (GenericEntityException | GenericServiceException e) {
			Debug.logError(e.getMessage(), ExamAssignToUserService.class.getName());
			return ServiceUtil.returnError("Error, occur during launch the exam");
		}
	}
}
