package com.vastpro.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

public class TopicService {

	public static Map<String, Object> createTopic(DispatchContext dctx, Map<String, Object> params) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess("Topic created successfully");
		try {
			String topicId = String.valueOf(delegator.getNextSeqId("topicMaster"));
			GenericValue topicMaster = delegator.makeValue("topicMaster");
			topicMaster.set("topicId", topicId);
			topicMaster.set("topicName", params.get("topicName"));
			delegator.create(topicMaster);
			return result;

		} catch (GenericEntityException e) {
			return ServiceUtil.returnError("Error, occur during adding the topic");
		}
	}

	public static Map<String, Object> updateTopic(DispatchContext dctx, Map<String, Object> params) {
		Delegator delegator = dctx.getDelegator();
		try {
			String topicId = (String) params.get("topicId");
			GenericValue topicMaster = EntityQuery.use(delegator).from("TopicMaster").where("topicId", topicId)
					.queryOne();

			if (topicMaster == null) {
				return ServiceUtil.returnError("Topic with ID " + topicId + " not found.");
			}
			topicMaster.set("topicName", params.get("topicName"));
			topicMaster.store();

			return ServiceUtil.returnSuccess("Topic updated successfully");
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError("Error occurred during updating the topic: ");
		}
	}

	public static Map<String, Object> deleteTopic(DispatchContext dctx, Map<String, Object> params) {
		Delegator delegator = dctx.getDelegator();
		try {
			String topicId = (String) params.get("topicId");
			GenericValue topicMaster = EntityQuery.use(delegator).from("TopicMaster").where("topicId", topicId)
					.queryOne();

			if (topicMaster == null) {
				return ServiceUtil.returnError("Topic with ID " + topicId + " not found.");
			}
			topicMaster.remove();
			return ServiceUtil.returnSuccess("Topic updated successfully");
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError("Error occurred during updating the topic: ");
		}
	}
	
	public static Map<String, Object> getTopic(DispatchContext dctx, Map<String, Object> params) {
		Delegator delegator = dctx.getDelegator();
		try {
			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError("Error occurred during updating the topic: ");
		}
	}
}
