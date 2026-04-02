package com.vastpro.sphinx.services;

import java.util.List;
import java.util.Map;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

public class UserService {
	public static Map<String,Object> getAllUser(DispatchContext context,Map<String,Object> input) {
		Delegator delegator=context.getDelegator();
		Map<String,Object> result=ServiceUtil.returnSuccess();
		try {
			
			 List<GenericValue> userList = EntityQuery.use(delegator).from("PartyPersonalInfo")
						.where("partyTypeId", "PERSON", "statusId", "PARTY_ENABLED", "roleTypeId", "SPHINX_USER").queryList();
//			List<GenericValue> userList=EntityQuery.use(delegator).from("UserLogin").queryList();
			
			 if(!(userList.size()>0)) {
				return ServiceUtil.returnSuccess("No user Found");
			}
			result.put("userList",userList);
			return result;
		}catch(GenericEntityException e) {
			return ServiceUtil.returnError("Error,Occur during get the user records"+e.getMessage());
		}
	}
}
