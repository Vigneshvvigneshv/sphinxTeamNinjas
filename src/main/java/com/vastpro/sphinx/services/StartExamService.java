package com.vastpro.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.transaction.GenericTransactionException;
import org.apache.ofbiz.entity.transaction.TransactionUtil;
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
			//validate the user to attend the exam
//			String userName=(String) input.get("userName");
//			if(UtilValidate.isEmpty(userName)){
//				return ServiceUtil.returnError("User name cannot be empty");
//			}
			
			String password=(String) input.get("password");
			if(UtilValidate.isEmpty(password)){
				return ServiceUtil.returnError("Password cannot be empty");
			}

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
			
			//Validate the exam before start the exam
			String checkPassword=examExits.getString("passwordChangesAuto");
			if(!(password.equals(checkPassword))) {
				return ServiceUtil.returnError("Password is incorrect");
			}
			
			
			GenericValue exam=EntityQuery.use(delegator).from("ExamMaster").where("examId",examId).queryFirst();
			if(exam==null) {
				Debug.logError("Exam is not Found",StartExamService.class.getName());
				return ServiceUtil.returnError("Error, contact the Admin");
			}
			TransactionUtil.begin();
			Map<String,Object> resultFromInceaseAttempts=dispatcher.runSync("increaseAttemptsOwn",UtilMisc.toMap("partyId",partyId,"examId",examId)); 
			if(ServiceUtil.isError(resultFromInceaseAttempts)) {
				handleTransaction();
				Debug.logError(resultFromInceaseAttempts.get("errorMessage").toString(),StartExamService.class.getName());
				return resultFromInceaseAttempts;
			}
			
			
			// if the user already in the inProgssParty so we just update the attempts
			GenericValue alreadyExamStart=EntityQuery.use(delegator).from("InProgressParty").where("partyId",partyId,"examId",examId).queryFirst();
			if(alreadyExamStart==null) {
			input.put("isExamActive",1);
			input.put("remainingTime",String.valueOf(exam.getLong("duration")));
			input.put("totalAnswered",0);
			input.put("totalRemaining",String.valueOf(exam.getLong("noOfQuestions")));
			Map<String,Object> result=dispatcher.runSync("startExam", input);
			if(ServiceUtil.isError(result)) {
				handleTransaction();
				Debug.logError(result.get("errorMessage").toString(),StartExamService.class.getName());
				return ServiceUtil.returnError("Error, occur during start exam");			}
			}else {
				//this is used to start the again so we set the iExamActive as 1
				input.put("isExamActive",1);
				Map<String,Object> setIsExamActive =dispatcher.runSync("updateInprogress", input);
				if(ServiceUtil.isError(setIsExamActive)) {
					handleTransaction();
					Debug.logError(setIsExamActive.get("errorMessage").toString(),StartExamService.class.getName());
					return ServiceUtil.returnError("Error, occur during start exam");			}
			}
			TransactionUtil.commit();
			return ServiceUtil.returnSuccess("Exam Started"); 
			
		}catch(GenericEntityException |GenericServiceException e) {
			handleTransaction();
			Debug.logError(e.getMessage(),StartExamService.class.getName());
			return ServiceUtil.returnError("Something Went wrong, Please try again later!");
		}
	}
	private static void handleTransaction(){
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Debug.logError(e.getMessage(),StartExamService.class.getName());
		}
	}
}