package com.vastpro.sphinx.services;


import java.util.Map;

import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

import com.vastpro.sphinx.util.FormValidation;
import com.vastpro.sphinx.util.PasswordHashing;

public class UserSignUpService {
	public static Map<String, Object> signUpService(DispatchContext dctx, Map<String, Object> params) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try {

			Delegator delegator = dctx.getDelegator();

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
			
			GenericValue party = delegator.makeValue("Party");
			party.set("partyId", partyId);
			party.set("partyTypeId", "PERSON");
			party.set("statusId", "PARTY_DISABLED"); // pending approval
			delegator.create(party);

			// Create Person
			GenericValue person = delegator.makeValue("Person");
			person.set("partyId", partyId);
			person.set("firstName", firstName);
			person.set("lastName", lastName);
			delegator.create(person);

			// to Store the email of the user in the contectMech
			String contactMechId = delegator.getNextSeqId("ContactMech");
			GenericValue contactMech = delegator.makeValue("ContactMech");
			contactMech.set("contactMechId", contactMechId);
			contactMech.set("infoString", email);
			contactMech.set("contactMechTypeId", "EMAIL_ADDRESS");
			
			delegator.create(contactMech);

			GenericValue partyContactMech = delegator.makeValue("PartyContactMech");
			partyContactMech.set("contactMechId", contactMechId);
			partyContactMech.set("partyId", partyId);
			partyContactMech.set("fromDate", UtilDateTime.nowTimestamp());
			delegator.create(partyContactMech);

			// Create UserLogin with user's own password
			GenericValue userLogin = delegator.makeValue("UserLogin");
			userLogin.set("userLoginId", username);
			userLogin.set("currentPassword", PasswordHashing.encryptPassword(password));
			userLogin.set("enabled", "N"); // disabled until admin approves
			userLogin.set("partyId", partyId);
			delegator.create(userLogin);

			// Assign Role
			GenericValue partyRole = delegator.makeValue("PartyRole");
			partyRole.set("partyId", partyId);
			partyRole.set("roleTypeId", "SPHINX_USER");
			delegator.create(partyRole);

			result.put("responseMessage","Registration successful. Waiting for admin approval.");
//			result.put("partyId", partyId);

		} catch (GenericEntityException e) {
//			return ServiceUtil.returnError(e.getMessage());
			return ServiceUtil.returnError("Try Again, Later");
		}
		return result;
	}
}
