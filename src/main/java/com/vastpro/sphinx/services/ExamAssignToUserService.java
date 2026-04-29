package com.vastpro.sphinx.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilValidate;
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

import com.vastpro.sphinx.util.PasswordUtil;

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
						 String userName = EntityQuery.use(delegator).from("UserLogin").where("partyId",userMap.get("partyId"))
											 .queryFirst().getString("userLoginId");
						userMap.put("examId", examId);
						String attempts=(String) userMap.get("allowedAttempts");
						if(attempts!=null && attempts.isEmpty()) {
							return ServiceUtil.returnError("Allowed attempts cannot be empty in "+userName);
						} 
						try {
							Long allowedAttempts= Long.valueOf(attempts);
							if(allowedAttempts<0) {
								return ServiceUtil.returnError("Attempts should be greate than 0 in "+userName);
							}
							input.put("allowedAttempts",allowedAttempts );
						} catch (NumberFormatException e) {
							rollBackTransaction();
							return ServiceUtil.returnError("Attempts should be number in "+userName);
						}
						String days=(String) userMap.get("timeoutDays");
						if(days!=null && days.isEmpty()) {
							return ServiceUtil.returnError("Days cannot be empty in "+userName);
						} 
						try {
							Long timeoutDays= Long.valueOf(days);
							if(timeoutDays<0) {
								return ServiceUtil.returnError("Days should be greate than 0 in "+userName);
							}
							input.put("timeoutDays",timeoutDays );
						} catch (NumberFormatException e) {
							rollBackTransaction();
							return ServiceUtil.returnError("Days should be number in "+userName);
						}
						userMap.put("noOfAttempts", 0);
						userMap.put("passwordChangesAuto",PasswordUtil.examGeneratePassword());
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
		return ServiceUtil.returnError("Select the user to assign the exam");
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
			return ServiceUtil.returnSuccess("User removed successfully from the exam");

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
			
			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("user cannot be empty");
			}
			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("exam cannot be empty");
			}

			String attempts=(String) input.get("allowedAttempts");
			if(attempts!=null && attempts.isEmpty()) {
				return ServiceUtil.returnError("Allowed attempts cannot be empty");
			} 
			try {
				Long allowedAttempts= Long.valueOf(attempts);
				if(allowedAttempts<0) {
					return ServiceUtil.returnError("Attempts should be greate than 0");
				}
				input.put("allowedAttempts",allowedAttempts );
			} catch (NumberFormatException e) {
				rollBackTransaction();
				return ServiceUtil.returnError("Attempts should be number");
			}

			String days=(String) input.get("timeoutDays");
			if(days!=null && days.isEmpty()) {
				return ServiceUtil.returnError("Allowed attempts cannot be empty");
			} 
			try {
				Long timeoutDays= Long.valueOf(days);
				if(timeoutDays<0) {
					return ServiceUtil.returnError("Days should be greate than 0");
				}
				input.put("timeoutDays",timeoutDays );
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
							.where("examId", input.get("examId")).orderBy("userLoginId").queryList();
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
	    Map<String, Object> result = ServiceUtil.returnSuccess("User fetched successfully");
	    try {
	        String examId = (String) input.get("examId");

	      
	        List<GenericValue> assignedList = EntityQuery.use(delegator)
	                .from("PartyExamRelationship")
	                .where("examId", examId)
	                .queryList();

	        List<String> assignedPartyIds = assignedList.stream()
	                .map(gv -> gv.getString("partyId"))
	                .collect(Collectors.toList());

	        List<EntityCondition> conditions = new ArrayList<>();

	        conditions.add(EntityCondition.makeCondition("partyTypeId", EntityOperator.EQUALS, "PERSON"));
	        conditions.add(EntityCondition.makeCondition("statusId",    EntityOperator.EQUALS, "PARTY_ENABLED"));
	        conditions.add(EntityCondition.makeCondition("roleTypeId",  EntityOperator.EQUALS, "SPHINX_USER"));

	        
	        if (!assignedPartyIds.isEmpty()) {
	            conditions.add(EntityCondition.makeCondition(
	                "partyId", EntityOperator.NOT_IN, assignedPartyIds
	            ));
	        }
	        
	        List<GenericValue> unassignedUserList = EntityQuery.use(delegator)
	                .from("PartyPersonalInfo")
	                .where(EntityCondition.makeCondition(conditions, EntityOperator.AND)).orderBy("userLoginId")
	                .queryList();

	        result.put("unassignedUsers", unassignedUserList);

	    } catch (GenericEntityException e) {
	        Debug.logError(e.getMessage(), ExamAssignToUserService.class.getName());
	        return ServiceUtil.returnError("Error occurred while getting unassigned users");
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
