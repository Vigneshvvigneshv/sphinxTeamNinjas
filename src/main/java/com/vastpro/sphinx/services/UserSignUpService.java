package com.vastpro.sphinx.services;


import java.util.HashMap;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilDateTime;
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

import org.apache.ofbiz.base.util.Debug;
import com.vastpro.sphinx.util.FormValidation;
import com.vastpro.sphinx.util.PasswordHashing;

public class UserSignUpService {
	public static Map<String, Object> signUpService(DispatchContext dctx, Map<String, Object> params) {
		Map<String, Object> result = ServiceUtil.returnSuccess("Registration successful. Waiting for admin approval.");
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher=dctx.getDispatcher();
		try {

			
			
			String username = (String) params.get("userName");
			String password = (String) params.get("password");
			String firstName = (String) params.get("firstName");
			String lastName = (String) params.get("lastName");
			String email = (String) params.get("email");
			
			
			
				if(!FormValidation.validateUsername(username)) {
					return ServiceUtil.returnError("Username must be at least 5 characters");
				}
				
				if(!FormValidation.validateEmail(email)) {
					return ServiceUtil.returnError("Enter a valid email");
				}
				
				if(!FormValidation.validatePassword(password)) {
					return ServiceUtil.returnError("Password must be strong (8+ chars, upper, lower, number, special)");
				}
				
				if(!FormValidation.validateFirstName(firstName)) {
					return ServiceUtil.returnError("Firstname must be letters");
				}
				
				if(!FormValidation.validateLastName(lastName)) {
					return ServiceUtil.returnError("Lastname must be letters");
				}
				
			
			// Check if username already exists
			GenericValue existingUser = EntityQuery.use(delegator).from("UserLogin").where("userLoginId", username)
					.queryOne();

			if (existingUser != null) {
				return ServiceUtil.returnError("Username already exists");
			}

			// Create Party
			String partyId = delegator.getNextSeqId("Party");
			partyId = "USR_" + partyId;
			
//			GenericValue party = delegator.makeValue("Party");
//			party.set("partyId", partyId);
//			party.set("partyTypeId", "PERSON");
//			party.set("statusId", "PARTY_DISABLED"); // pending approval
//			delegator.create(party);
			
			Map<String,Object> input=new HashMap<String, Object>();
			input.put("partyId",partyId);
			input.put("partyTypeId","PERSON");
			input.put("statusId","PARTY_DISABLED");
			input.put("firstName",firstName);
			input.put("lastName",lastName);
			input.put("infoString",email);
			input.put("contactMechTypeId","EMAIL_ADDRESS");
			// Create Person
//			GenericValue person = delegator.makeValue("Person");
//			person.set("partyId", partyId);
//			person.set("firstName", firstName);
//			person.set("lastName", lastName);
//			delegator.create(person);

			// to Store the email of the user in the contectMech
			String contactMechId = delegator.getNextSeqId("ContactMech");
//			GenericValue contactMech = delegator.makeValue("ContactMech");
//			contactMech.set("contactMechId", contactMechId);
//			contactMech.set("infoString", email);
//			contactMech.set("contactMechTypeId", "EMAIL_ADDRESS");
//			delegator.create(contactMech);
			input.put("contactMechId",contactMechId);
			input.put("infoString",email);
			input.put("contactMechTypeId","EMAIL_ADDRESS");

//			GenericValue partyContactMech = delegator.makeValue("PartyContactMech");
//			partyContactMech.set("contactMechId", contactMechId);
//			partyContactMech.set("partyId", partyId);
//			partyContactMech.set("fromDate", UtilDateTime.nowTimestamp());
//			delegator.create(partyContactMech);

			input.put("fromDate", UtilDateTime.nowTimestamp());
			
			// Create UserLogin with user's own password
//			GenericValue userLogin = delegator.makeValue("UserLogin");
//			userLogin.set("userLoginId", username);
//			userLogin.set("currentPassword", PasswordHashing.encryptPassword(password));
//			userLogin.set("enabled", "N"); // disabled until admin approves
//			userLogin.set("partyId", partyId);
//			delegator.create(userLogin);
			
			input.put("userLoginId", username);
			input.put("currentPassword", PasswordHashing.encryptPassword(password));
			input.put("enabled", "N");

			// Assign Role
//			GenericValue partyRole = delegator.makeValue("PartyRole");
//			partyRole.set("partyId", partyId);
//			partyRole.set("roleTypeId", "SPHINX_USER");
//			delegator.create(partyRole);
			input.put("roleTypeId", "SPHINX_USER");
			
			TransactionUtil.begin();
			
			
			Map<String,Object> party=dispatcher.runSync("createParty", input);
			if(ServiceUtil.isError(party)) {
				return handleTransaction(party);
			}
			Map<String,Object> person=dispatcher.runSync("createPerson", input);
			if(ServiceUtil.isError(person)) {
				return handleTransaction(person);
			}
			
			Map<String,Object> partyRole=dispatcher.runSync("createPartyRole", input);
			if(ServiceUtil.isError(partyRole)) {
				return handleTransaction(partyRole);
			}
			
			Map<String,Object> contactMech=dispatcher.runSync("createContactMech", input);
			if(ServiceUtil.isError(contactMech)) {
				return handleTransaction(contactMech);
			}
			Map<String,Object> partyContactMech=dispatcher.runSync("createPartyContactMech", input);
			if(ServiceUtil.isError(partyContactMech)) {
				return handleTransaction(partyContactMech);
			}
			Map<String,Object> userLogin=dispatcher.runSync("createUserLogin", input);
			if(ServiceUtil.isError(userLogin)) {
				return handleTransaction(userLogin);
			}
			
			
			
			TransactionUtil.commit();
//			result.put("partyId", partyId);

		} catch (GenericEntityException |GenericServiceException e) {
			
			try {
				TransactionUtil.rollback();
			} catch (GenericTransactionException te) {
				// TODO Auto-generated catch block
				Debug.logError(te.getMessage(),UserSignUpService.class.getName());
			}
//			return ServiceUtil.returnError(e.getMessage());
			Debug.logError(e.getMessage(),UserSignUpService.class.getName());
			return ServiceUtil.returnError("Error, Occur during create user");
		}
		return result;
	}
	
	private static Map<String,Object> handleTransaction(Map<String,Object> input){
		try {
			TransactionUtil.rollback();
		} catch (GenericTransactionException e) {
				Debug.logError(e.getMessage(),UserSignUpService.class.getName());
		}
		return ServiceUtil.returnError("Unexcepted error during create the user");
//		return ServiceUtil.returnError((String)input.get("errorMessage"));
	}
}

