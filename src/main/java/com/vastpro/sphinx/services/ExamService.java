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

public class ExamService {

	public static Map<String, Object> createExam(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator = context.getDelegator();
		try {
			if (input.get("examName") != null) {
				GenericValue examAlreadyExits = EntityQuery.use(delegator).from("ExamMaster")
						.where("examName", input.get("examName")).queryFirst();
				if (examAlreadyExits!=null) {
					return ServiceUtil.returnError("Exam Already Exits");
				}
			}else {
				return ServiceUtil.returnError("Exam name cannot be empty");
			}
			String examId = delegator.getNextSeqId("ExamMaster");
			examId = "exam" + examId;
			Map<String, Object> createMap = new HashMap<String, Object>();
			createMap.put("examId", examId);
			createMap.put("examName", input.get("examName"));
			createMap.put("description", input.get("description"));
			createMap.put("noOfQuestions", Long.valueOf((String) input.get("noOfQuestions")));
			createMap.put("duration", Long.valueOf((String) input.get("duration")));
			createMap.put("passPercentage", Long.valueOf((String) input.get("passPercentage")));

			Map<String, Object> result = dispatcher.runSync("createExam", createMap);

			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError("Error, occur during creating the Exam");
			}

			return ServiceUtil.returnSuccess("Exam create successfully");

		} catch (GenericServiceException | GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Error, occur during creating exam" + e.getMessage());
		}
	}

	public static Map<String, Object> deleteExam(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator = context.getDelegator();
		try {
			GenericValue examId = EntityQuery.use(delegator).from("ExamMaster").where("examId", input.get("examId"))
					.queryFirst();

			if (examId == null) {
				return ServiceUtil.returnSuccess("Exam not found");
			}

			Map<String, Object> result = dispatcher.runSync("deleteExam", input);

			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError((String) result.get("errorMessage"));
			}

			return ServiceUtil.returnSuccess("Exam deleted successfully");
		} catch (GenericServiceException | GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ServiceUtil.returnError("Error, occur during delete exam" + e.getMessage());
		}
	}
	
	public static Map<String, Object> updateExam(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator = context.getDelegator();
		try {
			
				GenericValue examIdExits = EntityQuery.use(delegator).from("ExamMaster")
						.where("examId", input.get("examId")).queryFirst();
				if (examIdExits!=null) {
					return ServiceUtil.returnError("Exam Already Exits");
				}
			
			Map<String, Object> createMap = new HashMap<String, Object>();
			createMap.put("examId", input.get("examId"));
			createMap.put("examName", input.get("examName"));
			createMap.put("description", input.get("description"));
			createMap.put("noOfQuestions", Long.valueOf((String) input.get("noOfQuestions")));
			createMap.put("duration", Long.valueOf((String) input.get("duration")));
			createMap.put("passPercentage", Long.valueOf((String) input.get("passPercentage")));

			Map<String, Object> result = dispatcher.runSync("updateExam", createMap);

			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError("Error, occur during update the Exam");
			}

			return ServiceUtil.returnSuccess("Exam update successfully");

		} catch (GenericServiceException | GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Error, occur during update exam" + e.getMessage());
		}
	}

	public static Map<String, Object> getAllExam(DispatchContext context, Map<String, Object> input) {
		Delegator delegator = context.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try {
			List<GenericValue> examList = EntityQuery.use(delegator).from("ExamMaster").queryList();
			result.put("examList", examList);
			return result;

		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ServiceUtil.returnError("Error, occur during getting list of exam" + e.getMessage());
		}
	}

}
