package com.vastpro.sphinx.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.transaction.GenericTransactionException;
import org.apache.ofbiz.entity.transaction.TransactionUtil;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityOperator;
/**
 * this class is used to assign the user to the exam
 */
public class ExamAssignToUserService {

	/**
	 * this method is used to assign the exam to the user
	 * 
	 * @param context
	 *            (used to get the delegator and dispatcher)
	 * @param input
	 *            (it contain the examId and list of Map, Map contain the user details like timeoutDays, noOfAttempts, allowedAttempts)
	 * @return error and success
	 */
	public static Map<String, Object> assignExam(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator=context.getDelegator();
		String examId = (String) input.get("examId");

		List<Map<String, Object>> assignedUser = (List<Map<String, Object>>) input.get("assignedUserList");
		if (assignedUser != null && assignedUser.size() > 0) {
			try {
				TransactionUtil.begin();
				for (Map<String, Object> userMap : assignedUser) {

					try {
						// GenericValue partyIdAlreadyExits = EntityQuery.use(delegator).from("UserLogin").where("partyId",
						// userMap.get("partyId"))
						// .queryFirst();
						// if (partyIdAlreadyExits == null) {
						// rollBackTransaction();
						// return ServiceUtil.returnError("User not found");
						// }
						// GenericValue examIdAlreadyExits = EntityQuery.use(delegator).from("ExamMaster").where("examId",
						// userMap.get("examId"))
						// .queryFirst();
						// if (examIdAlreadyExits == null) {
						// rollBackTransaction();
						// return ServiceUtil.returnError("Exam not found");
						// }
						
						 GenericValue examAlreadyAssigned = EntityQuery.use(delegator).from("PartyExamRelationship").where("examId",
						examId,"partyId",userMap.get("partyId"))
						 .queryFirst();
						 
						 if(examAlreadyAssigned!=null) {
							 continue;
						 }
						
						userMap.put("examId", examId);
						String attempts=(String) userMap.get("allowedAttempts");
						if(attempts!=null && attempts.isEmpty()) {
							return ServiceUtil.returnError("Allowed attempts cannot be empty");
						} 
						try {
							userMap.put("allowedAttempts", Long.valueOf(attempts));
						} catch (NumberFormatException e) {
							rollBackTransaction();
							return ServiceUtil.returnError("Attempts should be number");
						}
						String days=(String) userMap.get("timeoutDays");
						if(days!=null && days.isEmpty()) {
							return ServiceUtil.returnError("Allowed attempts cannot be empty");
						} 
						try {
							userMap.put("timeoutDays", Long.valueOf(days));
						} catch (NumberFormatException e) {
							rollBackTransaction();
							return ServiceUtil.returnError("Days should be number");
						}
						userMap.put("noOfAttempts", 0);
						Map<String, Object> result = dispatcher.runSync("assignExam", userMap);

						if (ServiceUtil.isError(result)) {
							rollBackTransaction();
							return ServiceUtil.returnError("Error, occur during assing the Exam to the user");
						}

					} catch (GenericEntityException |GenericServiceException e) {

						rollBackTransaction();
						Debug.logError(e.getMessage(), ExamAssignToUserService.class.getName());
						return ServiceUtil.returnError("Error, occur during assing the Exam to the user" + e.getMessage());
					}
				}
				TransactionUtil.commit();
			} catch (GenericTransactionException e) {
				rollBackTransaction();
				Debug.logError(e.getMessage(), ExamAssignToUserService.class.getName());
				return ServiceUtil.returnError("Users can't assign to the exam, Please Try again later");
			}
			return ServiceUtil.returnSuccess("Users Assigned to the exam successfully");
		}
		return ServiceUtil.returnError("Select the user to assign to the exam");
	}

	public static Map<String, Object> removeAssignedExam(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator = context.getDelegator();

		try {

			String partyId = (String) input.get("partyId");
			String examId = (String) input.get("examId");
			if (partyId == null || partyId.isEmpty()) {
				return ServiceUtil.returnError("user cannot be empty");
			}
			if (examId == null || examId.isEmpty()) {
				return ServiceUtil.returnError("exam cannot be empty");
			}
//			GenericValue partyIdAlreadyExits = EntityQuery.use(delegator).from("PartyExamRelationship").where("partyId", partyId)
//							.queryFirst();
//			if (partyIdAlreadyExits == null) {
//				return ServiceUtil.returnError("User not found");
//			}
//			GenericValue examIdAlreadyExits = EntityQuery.use(delegator).from("PartyExamRelationship").where("examId", examId).queryFirst();
//			if (examIdAlreadyExits == null) {
//				return ServiceUtil.returnError("Exam not found");
//			}
//
//			GenericValue examAssignId = EntityQuery.use(delegator).from("PartyExamRelationship").where("partyId", partyId, "examId", examId)
//							.queryFirst();
//			if (examAssignId == null) {
//				return ServiceUtil.returnError("Exam is already not assigned to the user");
//			}
			Map<String, Object> result = dispatcher.runSync("removeAssignedValue", input);

			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError("Error, occur during remove the assigned Exam");
				// return ServiceUtil.returnError((String)result.get("errorMessage"));
			}
			return ServiceUtil.returnSuccess("Exam removed successfully");

		} catch ( GenericServiceException e) {
			Debug.logError(e.getMessage(), ExamAssignToUserService.class.getName());
			return ServiceUtil.returnError("Error, occur during remove the assigned Exam" + e.getMessage());
		}
	}

	// update assigned exam
	public static Map<String, Object> updateAssignedExam(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		try {
			String partyId = (String) input.get("partyId");
			String examId = (String) input.get("examId");
			if (partyId == null || partyId.isEmpty()) {
				return ServiceUtil.returnError("user cannot be empty");
			}
			if (examId == null || examId.isEmpty()) {
				return ServiceUtil.returnError("exam cannot be empty");
			}

			try {
				input.put("allowedAttempts", Long.valueOf((String) input.get("allowedAttempts")));
			} catch (NumberFormatException e) {
				rollBackTransaction();
				return ServiceUtil.returnError("Attempts should be number");
			}

			try {
				input.put("timeoutDays", Long.valueOf((String) input.get("timeoutDays")));
			} catch (NumberFormatException e) {
				rollBackTransaction();
				return ServiceUtil.returnError("Days should be number");
			}

			Map<String, Object> result = dispatcher.runSync("updateAssignedExam", input);
			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError("Error, occur during update the assigned user");
			}
			return ServiceUtil.returnSuccess("update the assigned user successfully");
		} catch (GenericServiceException e) {
			Debug.logError(e.getMessage(), ExamAssignToUserService.class.getName());
			return ServiceUtil.returnError("Error, occur during update the assigned user");
		}
	}

	public static Map<String, Object> increaseAttempts(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator = context.getDelegator();

		try {
//			GenericValue partyIdAlreadyExits = EntityQuery.use(delegator).from("PartyExamRelationship")
//							.where("partyId", input.get("partyId")).queryFirst();
//			if (partyIdAlreadyExits == null) {
//				return ServiceUtil.returnError("User not found");
//			}
//			GenericValue examIdAlreadyExits = EntityQuery.use(delegator).from("PartyExamRelationship").where("examId", input.get("examId"))
//							.queryFirst();
//			if (examIdAlreadyExits == null) {
//				return ServiceUtil.returnError("Exam not found");
//			}
//
			GenericValue getRecords = EntityQuery.use(delegator).from("PartyExamRelationship")
							.where("partyId", input.get("partyId"), "examId", input.get("examId")).queryFirst();
			if (getRecords == null) {
				return ServiceUtil.returnError("Exam is already not assigned to the user");
			}
			Long numberOfAttempts = getRecords.getLong("noOfAttempts");
			Long allowAttempts = getRecords.getLong("allowedAttempts");

			if (numberOfAttempts < allowAttempts) {
				input.put("noOfAttempts", (numberOfAttempts + 1));
			} else {
				return ServiceUtil.returnError("User reached a maximum attempts");
			}

			Map<String, Object> result = dispatcher.runSync("increaseAttempts", input);

			if (ServiceUtil.isError(result)) {
				Debug.logError((String) result.get("errorMessage"), ExamAssignToUserService.class.getName());
				return ServiceUtil.returnError("Error, occur during launch the exam");
			}
			return ServiceUtil.returnSuccess("exam Launched Successfully");
		} catch (GenericEntityException | GenericServiceException e) {
			Debug.logError(e.getMessage(), ExamAssignToUserService.class.getName());
			return ServiceUtil.returnError("Error, occur during launch the exam");
		}
	}

	public static Map<String, Object> getAssignedUser(DispatchContext context, Map<String, Object> input) {
		Delegator delegator = context.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess("User getted successfully");
		try {
			List<GenericValue> assignedUserList = EntityQuery.use(delegator).from("ExamAssignedUser")
							.where("examId", input.get("examId")).queryList();
//			if (assignedUserList.size() > 0) {
//				return ServiceUtil.returnError("No user assigned to the exam");
//			}
			result.put("assignedUsers", assignedUserList);
		} catch (GenericEntityException e) {
			Debug.logError(e.getMessage(), ExamAssignToUserService.class.getName());
			return ServiceUtil.returnError("Error, occur during getting the assigned user");
		}
		return result;
	}
	
	public static Map<String, Object> getUnassignedUser(DispatchContext context, Map<String, Object> input) {
		Delegator delegator = context.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess("User getted successfully");
		try {
			List<EntityCondition> conditions = new ArrayList<>();

			// Fixed filters from PPI table
			conditions.add(EntityCondition.makeCondition("partyTypeId", EntityOperator.EQUALS, "PERSON"));
			conditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "PARTY_ENABLED"));
			conditions.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "SPHINX_USER"));

			// examId OR null check
			conditions.add(
			    EntityCondition.makeCondition(
			        EntityCondition.makeCondition("examId", EntityOperator.EQUALS, input.get("examId")),
			        EntityOperator.OR,
			        EntityCondition.makeCondition("examId", EntityOperator.EQUALS, null)
			    )
			);

			// NULL on PER side = unassigned
			conditions.add(EntityCondition.makeCondition("examPartyId", EntityOperator.EQUALS, null));

			List<GenericValue> unassignedUserList = EntityQuery.use(delegator)
			        .from("ExamUnassignedUser")
			        .where(EntityCondition.makeCondition(conditions, EntityOperator.AND))
			        .queryList();
//			if (assignedUserList.size() > 0) {
//				return ServiceUtil.returnError("No user assigned to the exam");
//			}
			result.put("unassignedUsers", unassignedUserList);
		} catch (GenericEntityException e) {
			Debug.logError(e.getMessage(), ExamAssignToUserService.class.getName());
			return ServiceUtil.returnError("Error, occur during getting the assigned user");
		}
		return result;
	}

	private static void rollBackTransaction() {
		try {
			TransactionUtil.rollback();
		} catch (GenericTransactionException e) {
			Debug.logError(e.getMessage(), ExamAssignToUserService.class.getName());
		}
	}
}
