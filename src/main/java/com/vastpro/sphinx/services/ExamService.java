package com.vastpro.sphinx.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
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

/**
 * This class is used to perform operation in the exam for to create, delete, update, and find the exam ExamMaster table is used to store
 * and get the data
 */
public class ExamService {

	/**
	 * CreateExam is used to create the exam, and store details in the ExamMaster table
	 * 
	 * @param context
	 *            (context used to get the dispatcher and delegator)
	 * @param input
	 *            (input is the method it carry the exam data)
	 * @return map(map contains the success and error message)
	 */
	public static Map<String, Object> createExam(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator = context.getDelegator();
		try {

			// before update we check the examName is present or not in the particular admin
			if (input.get("examName") != null) {
				List<GenericValue> examAlreadyExits = EntityQuery.use(delegator).from("ExamMaster").where("examName", input.get("examName"))
								.queryList();
				for(GenericValue examData:examAlreadyExits) {
				if (examAlreadyExits != null) {
					GenericValue adminExamAlreadyExits=EntityQuery.use(delegator).from("AdminPartyExamRel").where("examId",examData.getString("examId"),"partyId",input.get("partyId")).queryFirst();
					if(adminExamAlreadyExits!=null) {
					return ServiceUtil.returnError("Exam Already Exits");
					}
				}
				}
			} else {
				return ServiceUtil.returnError("Exam name cannot be empty");
			}

			// we create the own exam id and set into the table
			// it will get the current serial number (getNextSeqId)
			String examId = delegator.getNextSeqId("ExamMaster");

			// get the current serial number and add the exam word in front of it
			examId = "exam" + examId;
			Map<String, Object> createMap = new HashMap<String, Object>();
			createMap.put("examId", examId);
			
			//validate the exam name length is less than 20 character
			String examName=(String) input.get("examName");
			if(examName.length()>20) {
				return ServiceUtil.returnError("Exam name length should be less than 20 character");
			}
			createMap.put("examName", examName);
			
			//validate the description is less than 255 character
			String description=(String) input.get("description");
			if(description.length()>255) {
				return ServiceUtil.returnError("Description length should be less than 255 character");
			}
			createMap.put("description", description);
			
			try {
				Long noOfQuestions = Long.valueOf((String) input.get("noOfQuestions"));
				if (noOfQuestions < 0) {
					return ServiceUtil.returnError("Number of question should be in greater than 0");
				}
				createMap.put("noOfQuestions", noOfQuestions);
			} catch (NumberFormatException e) {
				Debug.logError(e.getMessage(), ExamService.class.getName());
				return ServiceUtil.returnError("Number of question should be in number");
			}
			try {
				Long duration = Long.valueOf((String) input.get("duration"));
				if (duration <= 0 && duration > 180) {
					return ServiceUtil.returnError("duration of question should be betweet 1 to 180");
				}
				createMap.put("duration", duration);
			} catch (NumberFormatException e) {
				Debug.logError(e.getMessage(), ExamService.class.getName());
				return ServiceUtil.returnError("Duration should be in number");
			}
			try {

				Long passPercentage = Long.valueOf((String) input.get("passPercentage"));
				if (passPercentage < 20 && passPercentage > 100) {
					return ServiceUtil.returnError("passPercentage of question should be between 20 to 100");
				}
				createMap.put("passPercentage", passPercentage);

			} catch (NumberFormatException e) {
				Debug.logError(e.getMessage(), ExamService.class.getName());
				return ServiceUtil.returnError("Pass Percentage should be in number");
			}

			TransactionUtil.begin();
			// call the entity-auto service it will create the record
			Map<String, Object> result = dispatcher.runSync("createExam", createMap);

			// from the entity-auto service return the map the map contain success or error
			// to add the data
			// we validate the it gives the error or success and show the message id it is
			// error
			if (ServiceUtil.isError(result)) {
				TransactionUtil.rollback();
				return ServiceUtil.returnError("Error, occur during creating the Exam");
			}

			// this statement is create the relation between admin and exam
			// it create the relation when the admin create the exam
			Map<String, Object> result1 = dispatcher.runSync("createAdminPartyExamRel",
							Map.of("partyId", input.get("partyId"), "examId", examId));

			// from the entity-auto service return the map the map contain success or error
			// to add the data
			// we validate the it gives the error or success and show the message id it is
			// error
			if (ServiceUtil.isError(result1)) {
				TransactionUtil.rollback();
				return ServiceUtil.returnError("Error, occur during creating the Exam");
			}

			TransactionUtil.commit(); // if success then return the success message
			return ServiceUtil.returnSuccess("Exam create successfully");

		} catch (GenericServiceException | GenericEntityException e) {
			// e.printStackTrace();
			try {
				TransactionUtil.rollback();
			} catch (GenericTransactionException e1) {
				// TODO Auto-generated catch block
				// e1.printStackTrace();
				Debug.logError(e1.getMessage(), ExamService.class.getName());
				return ServiceUtil.returnError("Error, occur during creating exam" + e1.getMessage());
			}
			Debug.logError(e.getMessage(), ExamService.class.getName());
			return ServiceUtil.returnError("Error, occur during creating exam" + e.getMessage());
		}
	}

	/**
	 * This method is used to delete the exam in the ExamMaster Table
	 * 
	 * @param context
	 *            (context used to get the dispatcher and delegator)
	 * @param input
	 *            (input is the method it carry the exam data)
	 * @return map(map contains the success and error message)
	 */

	public static Map<String, Object> deleteExam(DispatchContext context, Map<String, Object> param) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator = context.getDelegator();
		String partyId = (String) param.get("partyId");
		List<String> deleteList = (List<String>) param.get("deleteList");
		for (String input : deleteList) {
			try {
				// before update we check the examId is present or not
				// GenericValue examId = EntityQuery.use(delegator).from("ExamMaster").where("examId", input.get("examId")).queryFirst();
				//
				// if (examId == null) {
				// return ServiceUtil.returnSuccess("Exam not found");
				// }

				// List<GenericValue> examPresentInExamTopic = EntityQuery.use(delegator).from("ExamTopicMapping")
				// .where("examId", input.get("examId")).queryList();
				//
				// if (examPresentInExamTopic.size() > 0) {
				// // Map<String, Object> result1 = dispatcher.runSync("deleteTopicInExamTopicMaster", input);
				// // if (ServiceUtil.isError(result1)) {
				// // return ServiceUtil.returnError((String) result1.get("errorMessage"));
				// // }
				// for (GenericValue data : examPresentInExamTopic) {
				// data.remove();
				// }
				// }

				TransactionUtil.begin();

				// when the exam delete, delete the records from the another table that are related to the exam(deleted exam)
				delegator.removeByAnd("ExamTopicMapping", UtilMisc.toMap("examId", input));
				delegator.removeByAnd("QuestionBankMasterB", UtilMisc.toMap("examId", input));
				delegator.removeByAnd("PartyExamRelationship", UtilMisc.toMap("examId", input));
				// this statement is delete the relation between admin and exam
				// it remove the relation when the admin delete the exam
				Map<String, Object> result1 = dispatcher.runSync("deleteAdminPartyExamRel", Map.of("partyId", partyId, "examId", input));

				// from the entity-auto service return the map the map contain success or error
				// to add the data
				// we validate the it gives the error or success and show the message id it is
				// error
				if (ServiceUtil.isError(result1)) {
					TransactionUtil.rollback();
					return ServiceUtil.returnError("Error, occur during delete the Exam");
					// return ServiceUtil.returnError((String) result1.get("errorMessage"));
				}

				Map<String, Object> result = dispatcher.runSync("deleteExam", UtilMisc.toMap("examId", input));

				if (ServiceUtil.isError(result)) {
					TransactionUtil.rollback();
					return ServiceUtil.returnError("Error, occur during delete the Exam");
					// return ServiceUtil.returnError((String) result.get("errorMessage"));
				}
				TransactionUtil.commit();

			} catch (GenericServiceException | GenericEntityException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				try {
					TransactionUtil.rollback();
				} catch (GenericTransactionException e1) {
					// TODO Auto-generated catch block
					// e1.printStackTrace();
					Debug.logError(e1.getMessage(), ExamService.class.getName());
					return ServiceUtil.returnError("Error, occur during delete exam" + e1.getMessage());
				}
				Debug.logError(e.getMessage(), ExamService.class.getName());
				return ServiceUtil.returnError("Error, occur during delete exam" + e.getMessage());
			}
		}
		return ServiceUtil.returnSuccess("Exam deleted successfully");
	}

	/**
	 * This method is used to update the exam in the ExamMaster Table
	 * 
	 * @param context
	 *            (context used to get the dispatcher and delegator)
	 * @param input
	 *            (input is the method it carry the exam data)
	 * @return map(map contains the success and error message)
	 */

	public static Map<String, Object> updateExam(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator = context.getDelegator();
		try {
			// before update we check the examId is present or not
			GenericValue examIdExits = EntityQuery.use(delegator).from("ExamMaster").where("examId", input.get("examId")).queryFirst();
			if (examIdExits == null) {
				return ServiceUtil.returnError("Exam not found");
			}

			Map<String, Object> createMap = new HashMap<String, Object>();
			createMap.put("examId", input.get("examId"));
			
			//validate the exam name length is less than 20 character
			String examName=(String) input.get("examName");
			if(examName.length()>20) {
				return ServiceUtil.returnError("Exam name length should be less than 20 character");
			}
			createMap.put("examName", examName);
			
			//validate the description is less than 255 character
			String description=(String) input.get("description");
			if(description.length()>255) {
				return ServiceUtil.returnError("Description length should be less than 255 character");
			}
			createMap.put("description", description);
			
			try {
				Long noOfQuestions = Long.valueOf((String) input.get("noOfQuestions"));
				if (noOfQuestions < 0) {
					return ServiceUtil.returnError("Number of question should be in greater than 0");
				}
				createMap.put("noOfQuestions", noOfQuestions);
			} catch (NumberFormatException e) {
				Debug.logError(e.getMessage(), ExamService.class.getName());
				return ServiceUtil.returnError("Number of question should be in number");
			}
			try {
				Long duration = Long.valueOf((String) input.get("duration"));
				if (duration <= 0 && duration > 180) {
					return ServiceUtil.returnError("duration of question should be betweet 1 to 180");
				}
				createMap.put("duration", duration);
			} catch (NumberFormatException e) {
				Debug.logError(e.getMessage(), ExamService.class.getName());
				return ServiceUtil.returnError("Duration should be in number");
			}
			try {

				Long passPercentage = Long.valueOf((String) input.get("passPercentage"));
				if (passPercentage < 20 && passPercentage > 100) {
					return ServiceUtil.returnError("passPercentage of question should be between 20 to 100");
				}
				createMap.put("passPercentage", passPercentage);

			} catch (NumberFormatException e) {
				Debug.logError(e.getMessage(), ExamService.class.getName());
				return ServiceUtil.returnError("Pass Percentage should be in number");
			}

			Map<String, Object> result = dispatcher.runSync("updateExam", createMap);

			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError("Error, occur during update the Exam");
			}

			return ServiceUtil.returnSuccess("Exam update successfully");

		} catch (GenericServiceException | GenericEntityException e) {
			// e.printStackTrace();
			Debug.logError(e.getMessage(), ExamService.class.getName());
			return ServiceUtil.returnError("Error, occur during update exam" + e.getMessage());
		}
	}

	/**
	 * This method is used to get all the exam from the ExamMaster Table
	 * 
	 * @param context
	 *            (context used to get the dispatcher and delegator)
	 * @param input
	 *            (input is the method it carry the exam data)
	 * @return map(map contains the success and error message)
	 */
	public static Map<String, Object> getAllExam(DispatchContext context, Map<String, Object> input) {
		Delegator delegator = context.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try {
			List<GenericValue> examList = EntityQuery.use(delegator).from("AdminExamRelationInfo").where("partyId", input.get("partyId"))
							.orderBy("examName").queryList();
			result.put("examList", examList);
			return result;

		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			Debug.logError(e.getMessage(), ExamService.class.getName());
			return ServiceUtil.returnError("Error, occur during getting list of exam" + e.getMessage());
		}
	}

	/**
	 * This method is used to get the exam based on the examId
	 * 
	 * @param context
	 *            (context used to get the dispatcher and delegator)
	 * @param input
	 *            (input is the method it carry the exam data)
	 * @return map(map contains the success and error message)
	 */

	public static Map<String, Object> getExamById(DispatchContext context, Map<String, Object> input) {
		Delegator delegator = context.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try {
			// before update we check the examId is present or not
			GenericValue examData = EntityQuery.use(delegator).from("ExamMaster").where("examId", input.get("examId")).queryFirst();
			if (examData == null) {
				return ServiceUtil.returnError("Exam not found");
			}
			result.put("examList", examData);
			return result;
		} catch (GenericEntityException e) {
			Debug.logError(e.getMessage(), ExamService.class.getName());
			return ServiceUtil.returnError("Error, occur during get exam by id" + e.getMessage());
		}
	}

	// get the exam by the user(using the partyId) incomplete exam
	public static Map<String, Object> getExamByPartyId(DispatchContext context, Map<String, Object> input) {
		Delegator delegator = context.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try {
			// before update we check the examId is present or not
			List<GenericValue> examData = EntityQuery.use(delegator).from("ExamPartyRelationInfo").where("partyId", input.get("partyId"))
							.queryList();

			List<GenericValue> filteredExamData = new ArrayList<>();

			for (GenericValue exam : examData) {
				Long allowedAttempts = exam.getLong("allowedAttempts");
				Long noOfAttempts = exam.getLong("noOfAttempts");
				if ((allowedAttempts - noOfAttempts) > 0) {
					filteredExamData.add(exam);
				}
			}
			result.put("examList", filteredExamData);
			return result;
		} catch (GenericEntityException e) {
			Debug.logError(e.getMessage(), ExamService.class.getName());
			return ServiceUtil.returnError("Error, occur during get exam" + e.getMessage());
		}
	}

	// get the exam by the user(using the partyId) completed exam
	public static Map<String, Object> getCompletedExamByPartyId(DispatchContext context, Map<String, Object> input) {
		Delegator delegator = context.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try {
			// before update we check the examId is present or not
			List<GenericValue> examData = EntityQuery.use(delegator).from("ExamPartyRelationInfo").where("partyId", input.get("partyId"))
							.queryList();

			List<GenericValue> filteredExamData = new ArrayList<>();

			for (GenericValue exam : examData) {
				

				GenericValue inInProgressParty = EntityQuery.use(delegator).from("InProgressParty").where("partyId", input.get("partyId"),"examId",exam.getString("examId")
//								,"isExamActive",0
								)
								.queryFirst();
				if (inInProgressParty != null) {
						filteredExamData.add(exam);
				}
			}
			result.put("completedExamList", filteredExamData);
			return result;
		} catch (GenericEntityException e) {
			Debug.logError(e.getMessage(), ExamService.class.getName());
			return ServiceUtil.returnError("Error, occur during get exam" + e.getMessage());
		}
	}

}
