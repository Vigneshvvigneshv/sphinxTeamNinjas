package com.vastpro.sphinx.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class TopicService {

	public static Map<String, Object> createTopic(DispatchContext dctx, Map<String, Object> params) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher=dctx.getDispatcher();
		Map<String, Object> result = ServiceUtil.returnSuccess("Topic created successfully");
		String topicName=(String)params.get("topicName");
		try {
			
			if(topicName==null||topicName.trim().isEmpty()) {
				return ServiceUtil.returnError("Field cannot be empty");
			}
			GenericValue topicAlreadyExits=EntityQuery.use(delegator).from("topicMaster").where("topicName",params.get("topicName")).queryFirst();
			if(topicAlreadyExits!=null) {
				return ServiceUtil.returnError("Topic Already Exits");
			}
			Map<String,Object> record=dispatcher.runSync("createTopic", params);
			
			
			if(ServiceUtil.isError(record)) {
				return ServiceUtil.returnError("Error, occur during adding the topic");
			}
			
			return result;

		} catch (GenericServiceException | GenericEntityException e) {
			return ServiceUtil.returnError("Error, occur during adding the topic"+ e.getMessage());
		}
	}

	public static Map<String, Object> updateTopic(DispatchContext dctx, Map<String, Object> params) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher=dctx.getDispatcher();
		String topicId = (String) params.get("topicId");
		String topicName=(String)params.get("topicName");
		try {
			
			if(topicName==null||topicName.trim().isEmpty()) {
				return ServiceUtil.returnError("Field cannot be empty");
			}
			GenericValue updateTopicId = EntityQuery.use(delegator).from("topicMaster").where("topicId", topicId)
					.queryOne();

			if (updateTopicId == null) {
				return ServiceUtil.returnError("Topic with ID " + topicId + " not found.");
			}
			Map<String, Object> updateMap=new HashMap<String, Object>();
			updateMap.put("topicId", topicId);
			updateMap.put("topicName", topicName);
			Map<String, Object> result=dispatcher.runSync("updateTopic", updateMap);
			
			if(ServiceUtil.isError(result)) {
				return ServiceUtil.returnError("Error, occur during update the topic");
			}

			return ServiceUtil.returnSuccess("Topic updated successfully");
		}  catch (GenericServiceException | GenericEntityException e) {
			return ServiceUtil.returnError("Error, occur during update the topic"+ e.getMessage());
		}
	}

	public static Map<String, Object> deleteTopic(DispatchContext dctx, Map<String, Object> params) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher=dctx.getDispatcher();
		try {
			String topicId = (String) params.get("topicId");
			GenericValue topicMaster = EntityQuery.use(delegator).from("topicMaster").where("topicId", topicId)
					.queryOne();

			if (topicMaster == null) {
				return ServiceUtil.returnError("Topic with ID " + topicId + " not found.");
			}
			 Map<String, Object> deleteMap=new HashMap<String, Object>();
			 deleteMap.put("topicId", topicId);
			 Map<String, Object> result=dispatcher.runSync("deleteTopic", deleteMap);
			 
			 if(ServiceUtil.isError(result)) {
				 return ServiceUtil.returnError("Error, occur during delete the topic");
			 }
			 
			return ServiceUtil.returnSuccess("Topic deleted successfully");
			
		}  catch (GenericServiceException | GenericEntityException e) {
			return ServiceUtil.returnError("Error, occur during delete the topic"+ e.getMessage());
		}
	}
	
	public static Map<String, Object> getAllTopics(DispatchContext dctx, Map<String, Object> params) {
	    Delegator delegator = dctx.getDelegator();
	    Map<String, Object> result = ServiceUtil.returnSuccess();

	    try {
	        
	        List<GenericValue> topicList = EntityQuery.use(delegator)
	                                                  .from("topicMaster")
	                                                  .queryList();
	        
	        if(topicList.size()==0) {
	        	return ServiceUtil.returnError("No topic available");
	        }
	        
	        result.put("topicList", topicList);
	        
	    } catch (GenericEntityException e) {
	        return ServiceUtil.returnError("Error retrieving topics");
	    }
	    return result;
	}
}
