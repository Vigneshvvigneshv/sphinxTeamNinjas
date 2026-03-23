package com.vastpro.sphinx.services;


import java.util.Map;



import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;


public class UserSignUpService {
	public static Map<String,Object> signUpService(DispatchContext dctx, Map<String, Object> params){
		 Map<String, Object> result = ServiceUtil.returnSuccess();
	        try {
	        	
	        		Delegator delegator = dctx.getDelegator();
	
				
	            String username  = (String) params.get("username");
	            String password  = (String) params.get("password");
	            String firstName = (String) params.get("firstName");
	            String lastName  = (String) params.get("lastName");		           

	            // Check if username already exists
	            GenericValue existingUser = EntityQuery.use(delegator)
	                .from("UserLogin")
	                .where("userLoginId", username)
	                .queryOne();

	            if (existingUser != null) {
	            		return ServiceUtil.returnError("Username already exists");
	               
	            }

	            // Step 1 — Create Party
	            String partyId = delegator.getNextSeqId("Party");
	            GenericValue party = delegator.makeValue("Party");
	            party.set("partyId",     partyId);
	            party.set("partyTypeId", "PERSON");
	            party.set("statusId",    "PARTY_DISABLED");  // pending approval
	           
	            delegator.create(party);

	            // Step 2 — Create Person
	            GenericValue person = delegator.makeValue("Person");
	            person.set("partyId",   partyId);
	            person.set("firstName", firstName);
	            person.set("lastName",  lastName);
//	            person.set("email",     email);
	            delegator.create(person);

	            // Step 3 — Create UserLogin with user's own password
	            GenericValue userLogin = delegator.makeValue("UserLogin");
	            userLogin.set("userLoginId",     username);
//	            userLogin.set("currentPassword", PasswordUtil.hashPassword(getDelegator(), password));
	            userLogin.set("enabled",         "N");       // disabled until admin approves
	            userLogin.set("partyId",         partyId);
	            delegator.create(userLogin);

	            // Step 4 — Assign Role
	            GenericValue partyRole = delegator.makeValue("PartyRole");
	            partyRole.set("partyId",    partyId);
	            partyRole.set("roleTypeId", "END_USER_CUSTOMER");
	            delegator.create(partyRole);

	            result.put("responseMessage","success");
	            result.put("responseMessage", "Registration successful. Waiting for admin approval.");
	            result.put("partyId", partyId);

	        } catch (GenericEntityException e) {
	            return  ServiceUtil.returnError(e.getMessage());
	        }
	       return result;
	}
	}

