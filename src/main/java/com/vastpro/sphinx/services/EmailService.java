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

public class EmailService {

	public static Map<String, Object> sendExamNotification(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();

		LocalDispatcher dispatcher = dctx.getDispatcher();

		try {

			String examId = (String) context.get("examId");
			List<String> partyIdList = (List<String>) context.get("partyIdList");

			if (partyIdList.size() == 0) {
				return ServiceUtil.returnError("PartyList is Empty");
			}

			if (examId == null) {
				return ServiceUtil.returnError("examId is required");
			}

			GenericValue examRecord = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId).queryOne();

			if (examRecord == null) {
				return ServiceUtil.returnError("Invalid Exam Details! No Records found!");
			}

			String examName = examRecord.getString("examName");

			for (String partyId : partyIdList) {

				GenericValue passwordGet = EntityQuery.use(delegator).from("PartyExamRelationship")
								.where("examId", examId, "partyId", partyId).queryOne();

				String password = passwordGet.getString("passwordChangesAuto");

				// try {
				// dispatcher.runAsync("PasswordChangesAuto",
				// UtilMisc.toMap("examId",examId,"partyId",partyId,"passwordChangesAuto",password));
				// }catch(GenericServiceException e) {
				// e.printStackTrace();
				// return ServiceUtil.returnError("Failed To Send an Exam Notification "+e.getMessage());
				// }

				List<GenericValue> assignedUsersListsWithEmails = EntityQuery.use(delegator).from("UserDetailsForEmail")
								.where("contactMechTypeId", "EMAIL_ADDRESS", "examId", examRecord.get("examId")).queryList();

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
					emailContext.put("body", String.format(emailBody, assignedUser.getString("userLoginId"), password));
				}

				try {
					dispatcher.runAsync("sendMail", emailContext);
				} catch (GenericServiceException e) {
					e.printStackTrace();
					return ServiceUtil.returnError("Failed To Send an Exam Notification " + e.getMessage());
				}
			}

			return ServiceUtil.returnSuccess("Mail Notificaiton Initiated! The Users will recieve the Email shortly!");
		} catch (Exception e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	public static Map<String, Object> sendUserNotification(DispatchContext dctx, Map<String, Object> context){


		

		LocalDispatcher dispatcher = dctx.getDispatcher();

		try {

			String userName = (String) context.get("userName");
			String password = (String) context.get("password");
			String email = (String) context.get("email");
			
			
			

			
			

				Map<String, Object> emailContext = new HashMap<>();
				emailContext.put("subject", "Exam Assignment and Access Details");

				String emailBody = "Dear Candidate,\n\n"
						+ "Welcome! Your account has been successfully created in the Sphinx application.\n\n"
						+ "Please find your login credentials below:\n\n"
						+ "Username: %s\n"
						+ "Password: %s\n\n"
						+ "Kindly use the above credentials to log in to the Sphinx application.\n\n"
						+ "For security purposes, we recommend changing your password upon first login.\n\n"
						+ "Should you require any assistance, please do not hesitate to contact the administrator.\n\n"
						+ "Best regards,\n"
						+ "Sphinx Administrator";

				emailContext.put("contentType", "text/plain");
				emailContext.put("body", String.format(emailBody, userName, password));
				emailContext.put("sendTo",email);	
			

				try {
					dispatcher.runAsync("sendMail", emailContext);
				} catch (GenericServiceException e) {
					e.printStackTrace();
					return ServiceUtil.returnError("Failed To Send Notification " + e.getMessage());
				}
			

			return ServiceUtil.returnSuccess("User credentials sended!");
		} catch (Exception e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
	
	}
}
