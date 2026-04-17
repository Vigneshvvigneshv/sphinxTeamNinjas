package com.vastpro.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.common.login.LoginServices;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.ofbiz.webapp.control.LoginWorker;

import com.vastpro.sphinx.util.PasswordUtil;


public class UserLoginService {

	public static Map<String, Object> validateUserLoginService(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess("User Login Successfully");

		Map<String, Object> error = ServiceUtil.returnError("Invalid credentials");
		// { "responseMessage": "success",
		// "successMessage": "User Login Successfully"}

		// Map<String,Object> result=new HashMap<String, Object>();
		
		Delegator delegator = dctx.getDelegator();
		try {
			GenericValue user = delegator.findOne("UserLogin", true, Map.of("userLoginId", context.get("userName")));
			if (user == null) {
				return error;
			}

			if (PasswordUtil.checkPassword(String.valueOf(context.get("password")), user.getString("currentPassword"))) {
				// if (String.valueOf(context.get("password")).equals(user.getString("currentPassword"))) {
				GenericValue roleType = EntityQuery.use(delegator).from("PartyRole").where("partyId", user.get("partyId")).queryFirst();

				
				// result.put("successMessage", "User Login Successfully");
				result.put("role", roleType.getString("roleTypeId"));
				result.put("partyId", user.getString("partyId"));
				return result;
			} else {
				return error;
			}

		} catch (

		GenericEntityException e) {
			// e.printStackTrace();
			Debug.logError(e.getMessage(), UserLoginService.class.getName());
			return ServiceUtil.returnError("Login Failed, Try Again");
		}

	}
}
