package com.vastpro.sphinx.services;

import java.util.Map;

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
        
        Map<String, Object> error=ServiceUtil.returnError("Login Failed");
//    {	"responseMessage": "success",
//        "successMessage": "User Login Successfully"}
    	
//    	Map<String,Object> result=new HashMap<String, Object>();
        
        Delegator delegator = dctx.getDelegator();
        try {
           GenericValue user=delegator.findOne("UserLogin", true,Map.of("userLoginId",context.get("userName")));
           if(user==null) {
        	   error.put("userName","Username is incorrect");
        	   return error;
           }

           if(PasswordHashing.checkPassword(String.valueOf(context.get("password")),user.getString("currentPassword"))) {
        	   GenericValue roleType = EntityQuery.use(delegator).from("PartyRole").where("partyId",user.get("partyId"))
        						.queryFirst();
        	           	   
//        	   result.put("successMessage", "User Login Successfully");
        	   result.put("role",roleType.getString("roleTypeId"));
        	   result.put("partyId",user.getString("partyId"));
        	   return result;
           }else {
        	   error.put("password","password is incorrect");
        	   return error;
           }

        } catch (GenericEntityException e) {
        	e.printStackTrace();
        	return error;
        }
        
    }
} 
