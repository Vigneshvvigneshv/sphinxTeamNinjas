package com.vastpro.sphinx.services;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

public class UserLoginService {

    public static Map<String, Object> validateUserLoginService(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        try {
           GenericValue user=delegator.findOne("userLogin", true,Map.of("user_login_id",context.get("user_login_id")));
           if(user==null) {
        	   return ServiceUtil.returnError("Unable to find the user");
           }
           if(user.getString("current_password").equals(context.get("current_password"))) {
        	   result.put("message", "login successfully");
        	   return result;
           }

        } catch (GenericEntityException e) {
        	e.printStackTrace();
        	return ServiceUtil.returnError("login failed");
        }
        return ServiceUtil.returnError("login failed");
    }
} 
