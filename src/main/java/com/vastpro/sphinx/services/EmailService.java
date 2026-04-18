package com.vastpro.sphinx.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

import com.vastpro.sphinx.util.PasswordUtil;

public class EmailService {

public static Map<String,Object>sendExamNotification(DispatchContext dctx,Map<String,Object>context){
		
		Delegator delegator=dctx.getDelegator();
		
		LocalDispatcher dispatcher = dctx.getDispatcher();
		
		try {
			
			String examId=(String) context.get("examId");
			String partyId=(String)context.get("partyId");
			
			if(examId==null) {
				return ServiceUtil.returnError("examId is required");
			}	
			
			if(partyId==null) {
				return ServiceUtil.returnError("partyId is required");
			}
			
			GenericValue examRecord=EntityQuery.use(delegator).from("ExamMaster").where("examId",examId).queryOne();
			
			if(examRecord==null) {
				return ServiceUtil.returnError("Invalid Exam Details! No Records found!");
			}
			
			
			String examName=examRecord.getString("examName");
			
			String password=PasswordUtil.generatePassword();
			
			try {
				dispatcher.runAsync("PasswordChangesAuto", UtilMisc.toMap("examId",examId,"partyId",partyId,"passwordChangesAuto",password));
			}catch(GenericServiceException e) {
				e.printStackTrace();
				return ServiceUtil.returnError("Failed To Send an Exam Notification "+e.getMessage());
			}
			
			List<GenericValue> assignedUsersListsWithEmails = EntityQuery.use(delegator).from("UserDetailsForEmail")
							.where("contactMechTypeId", "EMAIL_ADDRESS", "examId", examRecord.get("examId"))
							.queryList();
			
			
			if (assignedUsersListsWithEmails.isEmpty()) {
				return ServiceUtil.returnError("No assigned users found for the exam.");
			}
			
			
			Map<String, Object> emailContext = new HashMap<>();
			emailContext.put("subject", "Exam Assignment and Access Details");
			
			String emailBody = "Dear Candidate,\n\n" + "You have been successfully assigned to the following examination: " + examName
							+ "\n\nPlease find your login credentials below: \n\n" + "Username:  %s \n" + "Security Code: %s \n\n"
							+ "Kindly use the above credentials to access the Sphinx application and commence your examination.\n\nShould you require any assistance, please do not hesitate to contact the administrator."
							+ "Best regards,\nSphinx Administrator";
 
			emailContext.put("contentType", "text/plain");
			
			
			for (GenericValue assignedUser : assignedUsersListsWithEmails) {
				
				emailContext.put("sendTo", assignedUser.getString("infoString"));
				emailContext.put("body",
								String.format(emailBody, assignedUser.getString("userLoginId"), password));
			}
			
			try {
				dispatcher.runAsync("sendMail", emailContext);
			}catch(GenericServiceException e) {
				e.printStackTrace();
				return ServiceUtil.returnError("Failed To Send an Exam Notification "+e.getMessage());
			}
			
			return ServiceUtil.returnSuccess("Mail Notificaiton Initiated! The Users will recieve the Email shortly!");
		}catch(Exception e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
	}
}
