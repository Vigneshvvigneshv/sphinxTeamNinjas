package com.vastpro.sphinx.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

/**
 * This class is used for perform the operation in the topic like create, delete,...
 * 
 * Data stored in the topicMaster table
 */

public class TopicService {
	/**
	 * This method is used to create the record in the topic in the topicMaster table
	 * 
	 * @param dctx
	 *            (DispatchContext is used to get the dispatcher and delegator)
	 * @param params
	 *            (input data to insert into the table)
	 * @return success or error message
	 */

	public static Map<String, Object> createTopic(DispatchContext dctx, Map<String, Object> params) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Map<String, Object> result = ServiceUtil.returnSuccess("Topic created successfully");
		String topicName = (String) params.get("topicName");
		try {
			// validate the topic Name is empty or not
			// if it is empty return the error message
			if (topicName == null || topicName.trim().isEmpty()) {
				return ServiceUtil.returnError("Field cannot be empty");
			}
			// validate the topic name is already exits or not in the topicMaster table
			GenericValue topicAlreadyExits = EntityQuery.use(delegator).from("topicMaster").where("topicName", params.get("topicName"))
							.queryFirst();
			if (topicAlreadyExits != null) {
				return ServiceUtil.returnError("Topic Already Exits");
			}
			// if the topicName is not empty and not present in the table then insert the topic in the table
			Map<String, Object> record = dispatcher.runSync("createTopic", params);
			// any error occur in the insert the record by using entityAuto then return the error message
			if (ServiceUtil.isError(record)) {
				return ServiceUtil.returnError("Error, occur during adding the topic");
			}

			return result;

		} catch (GenericServiceException | GenericEntityException e) {
			Debug.logError(e.getMessage(), TopicService.class.getName());
			return ServiceUtil.returnError("Error, occur during adding the topic" + e.getMessage());
		}
	}

	/**
	 * This method is used to update the topic in the topicMaster table
	 * 
	 * @param dctx
	 *            (DispatchContext is used to get the dispatcher and delegator)
	 * @param params
	 *            (input data to update into the table) get the topicId and topicName to update in the topicId
	 * @return success or error message
	 */
	public static Map<String, Object> updateTopic(DispatchContext dctx, Map<String, Object> params) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String topicId = (String) params.get("topicId");
		String topicName = (String) params.get("topicName");
		try {

			// Validate the topicId and topicName
			if (topicId == null || topicId.trim().isEmpty()) {
				return ServiceUtil.returnError("Topic Id cannot be empty");
			}
			if (topicName == null || topicName.trim().isEmpty()) {
				return ServiceUtil.returnError("Topic name cannot be empty");
			}
			// validate the topicId is present or not in the table
			GenericValue updateTopicId = EntityQuery.use(delegator).from("topicMaster").where("topicId", topicId).queryOne();

			if (updateTopicId == null) {
				return ServiceUtil.returnError("Topic with ID " + topicId + " not found.");
			}

			// validate the topicName is already exits if it is exits then return the error message
			GenericValue topicNameAlreadyExits = EntityQuery.use(delegator).from("topicMaster").where("topicName", topicName).queryOne();
			if (topicNameAlreadyExits != null) {
				return ServiceUtil.returnError("Topic already exits");
			}

//			Map<String, Object> updateMap = new HashMap<String, Object>();
//			updateMap.put("topicId", topicId);
//			updateMap.put("topicName", topicName);
			// update the topic by using the entity-auto
			Map<String, Object> result = dispatcher.runSync("updateTopic", params);
			// if the service return the error then return the error message
			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError("Error, occur during update the topic");
			}
			// if the service return the success then return the success message
			return ServiceUtil.returnSuccess("Topic updated successfully");
		} catch (GenericServiceException | GenericEntityException e) {
			Debug.logError(e.getMessage(), TopicService.class.getName());
			return ServiceUtil.returnError("Error, occur during update the topic" + e.getMessage());
		}
	}

	/**
	 * This method is used to delete the topic in the topicMaster table
	 * 
	 * @param dctx
	 *            (DispatchContext is used to get the dispatcher and delegator)
	 * @param params
	 *            (input data to delete into the table) get the topicId to delete the record
	 * @return success or error message
	 */
	public static Map<String, Object> deleteTopic(DispatchContext dctx, Map<String, Object> params) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		try {
			String topicId = (String) params.get("topicId");
			// validate the topicId
			if (topicId == null || topicId.trim().isEmpty()) {
				return ServiceUtil.returnError("Topic Id cannot be empty");
			}
			// validate the topicId is present or not in the table
			GenericValue topicMaster = EntityQuery.use(delegator).from("topicMaster").where("topicId", topicId).queryOne();

			if (topicMaster == null) {
				return ServiceUtil.returnError("Topic with ID " + topicId + " not found.");
			}
//			Map<String, Object> deleteMap = new HashMap<String, Object>();
//			deleteMap.put("topicId", topicId);
			// the deleteTopic is delete the record based on the topicId in the topicMaster Table
			Map<String, Object> result = dispatcher.runSync("deleteTopic", params);
			// if the entity-auto return the error then return the error message
			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError("Error, occur during delete the topic");
			}
			// else return the success message
			return ServiceUtil.returnSuccess("Topic deleted successfully");

		} catch (GenericServiceException | GenericEntityException e) {
			Debug.logError(e.getMessage(), TopicService.class.getName());
			return ServiceUtil.returnError("Error, occur during delete the topic" + e.getMessage());
		}
	}

	/**
	 * This method is used to get all the topic
	 * 
	 * @param dctx
	 *            (DispatchContext is used to get the dispatcher and delegator)
	 * @param params
	 *            (Empty Map)
	 * @return topic list
	 */

	public static Map<String, Object> getAllTopics(DispatchContext dctx, Map<String, Object> params) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();

		try {
			// this statement used to get all the topic as list
			List<GenericValue> topicList = EntityQuery.use(delegator).from("topicMaster").queryList();
			// if the list size is 0 then return the no topic found message
			if (topicList.size() == 0) {
				return ServiceUtil.returnError("No topic available");
			}
			// are else return the list
			result.put("topicList", topicList);

		} catch (GenericEntityException e) {
			Debug.logError(e.getMessage(), TopicService.class.getName());
			return ServiceUtil.returnError("Error retrieving topics");
		}
		// return the list
		return result;
	}

	/**
	 * This method is used to get the topic by topicId
	 * 
	 * @param dctx
	 *            (DispatchContext is used to get the dispatcher and delegator)
	 * @param params
	 *            (Empty Map)
	 * @return single topic record
	 */

	public static Map<String, Object> getTopicById(DispatchContext context, Map<String, Object> input) {
		Delegator delegator = context.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try {

			String topicId = (String) input.get("topicId");
			// validate the topicId
			if (topicId == null || topicId.trim().isEmpty()) {
				return ServiceUtil.returnError("Topic Id cannot be empty");
			}
			// get the record based on the topicId
			GenericValue topicData = EntityQuery.use(delegator).from("topicMaster").where("topicId", topicId).queryOne();
			// check the topicId is present or not
			if (topicData == null) {
				return ServiceUtil.returnError("topic not found");
			}
			// if it is present in the table then put it in the map and return the map
			result.put("topicList", topicData);
			return result;
		} catch (GenericEntityException e) {
			Debug.logError(e.getMessage(), TopicService.class.getName());
			return ServiceUtil.returnError("Error, occur during get topic by id" + e.getMessage());
		}
	}

}
