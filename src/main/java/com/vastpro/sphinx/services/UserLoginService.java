package com.vastpro.sphinx.services;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

import com.vastpro.sphinx.util.PasswordHashing;

public class UserLoginService {

    public static Map<String, Object> validateUserLoginService(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess("User Login Successfully");
        Delegator delegator = dctx.getDelegator();
        try {
           GenericValue user=delegator.findOne("UserLogin", true,Map.of("userLoginId",context.get("userName")));
           if(user==null) {
        	   return ServiceUtil.returnError("Unable to find the user");
           }

           if(PasswordHashing.checkPassword(String.valueOf(context.get("password")),user.getString("currentPassword"))) {
        	   GenericValue roleType = EntityQuery.use(delegator).from("RoleType").where("roleTypeId",user.get("partyId"))
        						.queryOne();
        	   result.put("role", roleType);
        	   return result;
           }

        } catch (GenericEntityException e) {
        	e.printStackTrace();
        	return ServiceUtil.returnError("login failed");
        }
        return ServiceUtil.returnError("login failed");
    }
} 
