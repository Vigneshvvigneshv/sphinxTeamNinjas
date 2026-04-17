package com.vastpro.sphinx.services;

import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class UserService {
	public static Map<String, Object> getAllUser(DispatchContext context, Map<String, Object> input) {
		Delegator delegator = context.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try {

			List<GenericValue> userList = EntityQuery.use(delegator).from("PartyPersonalInfo")
							.where("partyTypeId", "PERSON", "statusId", "PARTY_ENABLED", "roleTypeId", "SPHINX_USER").orderBy("userLoginId").queryList();
			// List<GenericValue> userList=EntityQuery.use(delegator).from("UserLogin").queryList();
//
//			if (!(userList.size() > 0)) {
//				return ServiceUtil.returnSuccess("No user Found");
//			}
			result.put("userList", userList);
			return result;
		} catch (GenericEntityException e) {
			Debug.logError(e.getMessage(), UserService.class.getName());
			return ServiceUtil.returnError("Error,Occur during get the user records" + e.getMessage());
		}
	}

	// this method is used to disable the user
	public static Map<String, Object> disableUser(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator=context.getDelegator();
		try {
			input.put("statusId","PARTY_DISABLED");
			Map<String, Object> result =dispatcher.runSync("disableUser", input);
			if(ServiceUtil.isError(result)) {
				return ServiceUtil.returnError("Error,Occur during delete the user");
			}
			// delete the assigned exam delete the user
			delegator.removeByAnd("PartyExamRelationship", UtilMisc.toMap("partyId",input.get("partyId")));
			return ServiceUtil.returnSuccess("User deleted");
		}catch (GenericEntityException | GenericServiceException e) {
			Debug.logError(e.getMessage(), UserService.class.getName());
			return ServiceUtil.returnError("Error,Occur during delete the user");
		}
	}
}
