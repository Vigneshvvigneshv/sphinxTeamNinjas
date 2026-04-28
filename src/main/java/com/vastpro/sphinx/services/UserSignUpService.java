package com.vastpro.sphinx.services;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilMisc;
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

import com.vastpro.sphinx.util.FormValidation;
import com.vastpro.sphinx.util.PasswordUtil;


public class UserSignUpService {
	public static Map<String, Object> signUpService(DispatchContext dctx, Map<String, Object> params) {
		Map<String, Object> result = ServiceUtil.returnSuccess("success");
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		try {

			String username = (String) params.get("userName");
			String password = (String) params.get("password");
			String firstName = (String) params.get("firstName");
			String lastName = (String) params.get("lastName");
			String email = (String) params.get("email");
			String role = (String) params.get("roleTypeId");

			if (!FormValidation.validateUsername(username)) {
				return ServiceUtil.returnError("Username must be at least 5 characters");
			}

			if (!FormValidation.validateEmail(email)) {
				return ServiceUtil.returnError("Enter a valid email");
			}
			if ("SPHINX_ADMIN".equals(role)) {

				if (!FormValidation.validatePassword(password)) {
					return ServiceUtil.returnError("Password must be strong (8+ chars, upper, lower, number, special)");
				}
			}

			if (!FormValidation.validateFirstName(firstName)) {
				return ServiceUtil.returnError("Firstname must be letters");
			}

			if (!FormValidation.validateLastName(lastName)) {
				return ServiceUtil.returnError("Lastname must be letters");
			}
			if (role == null && ("SPHINX_USER".equals(role) || "SPHINX_ADMIN".equals(role))) {
				return ServiceUtil.returnError("Invalid role");
			}

			// Check if username already exists
			GenericValue existingUser = EntityQuery.use(delegator).from("UserLogin").where("userLoginId", username).queryOne();

			if (existingUser != null) {
				return ServiceUtil.returnError("Username already exists");
			}

			// Create Party
			String partyId = delegator.getNextSeqId("Party");
			partyId = "USR_" + partyId;
			String contactMechId = delegator.getNextSeqId("ContactMech");
			// GenericValue party = delegator.makeValue("Party");
			// party.set("partyId", partyId);
			// party.set("partyTypeId", "PERSON");
			// party.set("statusId", "PARTY_DISABLED"); // pending approval
			// delegator.create(party);

			// Create Person
			// GenericValue person = delegator.makeValue("Person");
			// person.set("partyId", partyId);
			// person.set("firstName", firstName);
			// person.set("lastName", lastName);
			// delegator.create(person);

			// to Store the email of the user in the contectMech

			// GenericValue contactMech = delegator.makeValue("ContactMech");
			// contactMech.set("contactMechId", contactMechId);
			// contactMech.set("infoString", email);
			// contactMech.set("contactMechTypeId", "EMAIL_ADDRESS");
			// delegator.create(contactMech);

			// GenericValue partyContactMech = delegator.makeValue("PartyContactMech");
			// partyContactMech.set("contactMechId", contactMechId);
			// partyContactMech.set("partyId", partyId);
			// partyContactMech.set("fromDate", UtilDateTime.nowTimestamp());
			// delegator.create(partyContactMech);

			// Create UserLogin with user's own password
			// GenericValue userLogin = delegator.makeValue("UserLogin");
			// userLogin.set("userLoginId", username);
			// userLogin.set("currentPassword", PasswordHashing.encryptPassword(password));
			// userLogin.set("enabled", "N"); // disabled until admin approves
			// userLogin.set("partyId", partyId);
			// delegator.create(userLogin);

			// Assign Role
			// GenericValue partyRole = delegator.makeValue("PartyRole");
			// partyRole.set("partyId", partyId);
			// partyRole.set("roleTypeId", "SPHINX_USER");
			// delegator.create(partyRole);

			TransactionUtil.begin();

			// create party
			Map<String, Object> partyInput = new HashMap<>();
			partyInput.put("partyId", partyId);
			partyInput.put("partyTypeId", "PERSON");
			partyInput.put("statusId", "PARTY_ENABLED");
			Map<String, Object> partyResult = dispatcher.runSync("createParty", partyInput);
			if (ServiceUtil.isError(partyResult))
				return handleTransaction();

			// create role
			Map<String, Object> partyRoleInput = new HashMap<>();
			partyRoleInput.put("partyId", partyId);
			partyRoleInput.put("roleTypeId", role);
			Map<String, Object> partyRoleResult = dispatcher.runSync("createPartyRole", partyRoleInput);
			if (ServiceUtil.isError(partyRoleResult))
				return handleTransaction();

			// create person
			Map<String, Object> personInput = new HashMap<>();
			personInput.put("partyId", partyId);
			personInput.put("firstName", firstName);
			personInput.put("lastName", lastName);
			Map<String, Object> personResult = dispatcher.runSync("createPerson", personInput);
			if (ServiceUtil.isError(personResult))
				return handleTransaction();

			// create user login
//			Map<String, Object> userLoginInput = new HashMap<>();
//			userLoginInput.put("userLoginId", username);
//
			if ("SPHINX_USER".equals(role) && password != null) {
				password = PasswordUtil.generatePassword();
				Debug.log("_____password-------"+password);
//				userLoginInput.put("currentPassword", PasswordUtil.encryptPassword(password));
			} 
//			else {
//				
//			}
//
//			userLoginInput.put("enabled", "N");
//			userLoginInput.put("partyId", partyId);
//			Map<String, Object> userLoginResult = dispatcher.runSync("createUserLogin", userLoginInput);
//			if (ServiceUtil.isError(userLoginResult))
//				return handleTransaction();
//
			GenericValue naRole = EntityQuery.use(delegator).from("PartyRole").where("partyId", partyId, "roleTypeId", "_NA_").queryOne();
			if (naRole != null) {
				naRole.remove();
			}
			
			// OOTB
			
//			GenericValue userLogin=EntityQuery.use(delegator).from("UserLogin").where("partyId",params.get("partyId")).queryFirst();	
			
			Map<String,Object> userLoginResult=dctx.getDispatcher().runSync("createUserLogin",
							UtilMisc.toMap("userLoginId", username, "currentPassword", password, "currentPasswordVerify",
											password, "partyId", partyId,
											"enabled", "Y", "requirePasswordChange", "N", "userLogin",

											params.get("userLogin")
											));
			
			if(ServiceUtil.isError(userLoginResult)) {
				Debug.logError("Error while create the user using thr createUserLogin out box service"+(String)userLoginResult.get("errorMessage"),UserSignUpService.class.getName());
				return handleTransaction();
			}
//
//			Map<String,Object> setAdminPartyResult=dctx.getDispatcher().runSync("addUserLoginToSecurityGroup",
//							UtilMisc.toMap("userLoginId", username, "groupId", "SPHINX_ADMIN_GROUP", "fromDate",
//											Timestamp.valueOf(LocalDateTime.now()),
//											"userLogin", 
//											params.get("userLogin")
//											));
//			if(ServiceUtil.isError(setAdminPartyResult)) {
//				Debug.logError("Error while create the user using the addUserLoginToSecurityGroup out box service"+(String)userLoginResult.get("errorMessage"),UserSignUpService.class.getName());
//				return handleTransaction();
//			}
			

//			
			GenericValue secGroup = delegator.makeValue("UserLoginSecurityGroup");
			secGroup.set("userLoginId", username);
			secGroup.set("groupId", "SPHINX_ADMIN_GROUP");
			secGroup.set("fromDate", Timestamp.valueOf(LocalDateTime.now()));
			delegator.create(secGroup);
//			
			if (!("SPHINX_USER".equals(role))) {
				GenericValue set = delegator.makeValue("UserLoginSecurityGroup");
				secGroup.set("userLoginId", username);
				secGroup.set("groupId", "PARTYADMIN");
				secGroup.set("fromDate", Timestamp.valueOf(LocalDateTime.now()));
				delegator.create(secGroup);
//				Map<String,Object> setPartyToCreateUserResult=dctx.getDispatcher().runSync("addUserLoginToSecurityGroup",
//								UtilMisc.toMap("userLoginId", username, "groupId", "PARTYADMIN", "fromDate",
//												Timestamp.valueOf(LocalDateTime.now()),
//												"userLogin", 
//												params.get("userLogin")
//												));
//				if(ServiceUtil.isError(setPartyToCreateUserResult)) {
//					Debug.logError("Error while create the user using the addUserLoginToSecurityGroup out box service when set the PARTYADMIN"+(String)userLoginResult.get("errorMessage"),UserSignUpService.class.getName());
//					return handleTransaction();
//				}
			}


			// create contact
			Map<String, Object> contactMechInput = new HashMap<>();
			contactMechInput.put("contactMechId", contactMechId);
			contactMechInput.put("infoString", email);
			contactMechInput.put("contactMechTypeId", "EMAIL_ADDRESS");
			Map<String, Object> contactMechResult = dispatcher.runSync("createContactMech", contactMechInput);
			if (ServiceUtil.isError(contactMechResult))
				return handleTransaction();

			Map<String, Object> partyContactMechInput = new HashMap<>();
			partyContactMechInput.put("partyId", partyId);
			partyContactMechInput.put("contactMechId", contactMechId);
			partyContactMechInput.put("fromDate", UtilDateTime.nowTimestamp());
			Map<String, Object> partyContactMechResult = dispatcher.runSync("createPartyContactMech", partyContactMechInput);
			if (ServiceUtil.isError(partyContactMechResult))
				return handleTransaction();

			TransactionUtil.commit();
			// result.put("partyId", partyId);
			return ServiceUtil.returnSuccess("Registration Successfull");
		} catch (GenericEntityException | GenericServiceException e) {

			try {
				TransactionUtil.rollback();
			} catch (GenericTransactionException te) {
				// TODO Auto-generated catch block
				Debug.logError(te.getMessage(), UserSignUpService.class.getName());
			}
			// return ServiceUtil.returnError(e.getMessage());
			Debug.logError(e.getMessage(), UserSignUpService.class.getName());
			return ServiceUtil.returnError("Error, Occur during create user");
		}
	}

	private static Map<String, Object> handleTransaction() {
		try {
			TransactionUtil.rollback();
		} catch (GenericTransactionException e) {
			Debug.logError(e.getMessage(), UserSignUpService.class.getName());
		}
		return ServiceUtil.returnError("Unexcepted error during create the user");
		// return ServiceUtil.returnError((String)input.get("errorMessage"));
	}

	
}
