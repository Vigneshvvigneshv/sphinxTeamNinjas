package com.vastpro.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class ExamAssignToUserService {

	public static Map<String, Object> assignExam(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator = context.getDelegator();

		try {
			GenericValue partyIdAlreadyExits = EntityQuery.use(delegator).from("UserLogin")
					.where("partyId", input.get("partyId")).queryFirst();
			if (partyIdAlreadyExits == null) {
				return ServiceUtil.returnError("User not found");
			}
			GenericValue examIdAlreadyExits = EntityQuery.use(delegator).from("ExamMaster")
					.where("examId", input.get("examId")).queryFirst();
			if (examIdAlreadyExits == null) {
				return ServiceUtil.returnError("Exam not found");
			}

			String examAssignId = delegator.getNextSeqId("ExamAssignMaster");
			input.put("examAssignId", examAssignId);
			Map<String, Object> result = dispatcher.runSync("assignExam", input);

			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError("Error, occur during assing the Exam");
			}
			return result;

		} catch (GenericEntityException | GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ServiceUtil.returnError("Error, occur during assing the Exam" + e.getMessage());
		}
	}

	public static Map<String, Object> removeAssignedExam(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator = context.getDelegator();

		try {

			String partyId = (String) input.get("partyId");
			String examId = (String) input.get("examId");
			GenericValue partyIdAlreadyExits = EntityQuery.use(delegator).from("UserLogin").where("partyId", partyId)
					.queryFirst();
			if (partyIdAlreadyExits == null) {
				return ServiceUtil.returnError("User not found");
			}
			GenericValue examIdAlreadyExits = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId)
					.queryFirst();
			if (examIdAlreadyExits == null) {
				return ServiceUtil.returnError("Exam not found");
			}

			GenericValue examAssignId = EntityQuery.use(delegator).from("ExamAssignMaster")
					.where("partyId", partyId, "examId", examId).queryFirst();
			if (examAssignId == null) {
				return ServiceUtil.returnError("Exam is already not assigned to the user");
			}
			String assignId = examAssignId.getString("examAssignId");
			Map<String, Object> result = dispatcher.runSync("removeAssignedValue", Map.of("examAssignId", assignId));

			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError("Error, occur during remove the assigned Exam");
//				return ServiceUtil.returnError((String)result.get("errorMessage"));
			}
			return ServiceUtil.returnSuccess("Exam removed successfully");

		} catch (GenericEntityException | GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Error, occur during remove the assigned Exam" + e.getMessage());
		}
	}

}
