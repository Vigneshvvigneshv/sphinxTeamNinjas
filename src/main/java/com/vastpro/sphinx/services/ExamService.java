package com.vastpro.sphinx.services;

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

			// before update we check the examName is present or not
			if (input.get("examName") != null) {
				GenericValue examAlreadyExits = EntityQuery.use(delegator).from("ExamMaster").where("examName", input.get("examName"))
								.queryFirst();
				if (examAlreadyExits != null) {
					return ServiceUtil.returnError("Exam Already Exits");
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
			createMap.put("examName", input.get("examName"));
			createMap.put("description", input.get("description"));
			try {
				createMap.put("noOfQuestions", Long.valueOf((String) input.get("noOfQuestions")));
			}catch(NumberFormatException e) {
				Debug.logError(e.getMessage(),ExamService.class.getName());
				return ServiceUtil.returnError("Number of question should be in number");
			}
			try {
				createMap.put("duration", Long.valueOf((String) input.get("duration")));
			}catch(NumberFormatException e) {
				Debug.logError(e.getMessage(),ExamService.class.getName());
				return ServiceUtil.returnError("Duration should be in number");
			}
			try {
				createMap.put("passPercentage", Long.valueOf((String) input.get("passPercentage")));
			}catch(NumberFormatException e) {
				Debug.logError(e.getMessage(),ExamService.class.getName());
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

	public static Map<String, Object> deleteExam(DispatchContext context, Map<String, Object> input) {
		LocalDispatcher dispatcher = context.getDispatcher();
		Delegator delegator = context.getDelegator();
		try {
			// before update we check the examId is present or not
			GenericValue examId = EntityQuery.use(delegator).from("ExamMaster").where("examId", input.get("examId")).queryFirst();

			if (examId == null) {
				return ServiceUtil.returnSuccess("Exam not found");
			}

			List<GenericValue> examPresentInExamTopic = EntityQuery.use(delegator).from("ExamTopicMapping")
							.where("examId", input.get("examId")).queryList();

			if (examPresentInExamTopic.size() > 0) {
				// Map<String, Object> result1 = dispatcher.runSync("deleteTopicInExamTopicMaster", input);
				// if (ServiceUtil.isError(result1)) {
				// return ServiceUtil.returnError((String) result1.get("errorMessage"));
				// }
				for (GenericValue data : examPresentInExamTopic) {
					data.remove();
				}
			}
			delegator.removeByAnd("QuestionBankMasterB", UtilMisc.toMap("examId", input.get("examId")));

			TransactionUtil.begin();
			// this statement is delete the relation between admin and exam
			// it remove the relation when the admin delete the exam
			Map<String, Object> result1 = dispatcher.runSync("deleteAdminPartyExamRel",
							Map.of("partyId", input.get("partyId"), "examId", examId.getString("examId")));

			// from the entity-auto service return the map the map contain success or error
			// to add the data
			// we validate the it gives the error or success and show the message id it is
			// error
			if (ServiceUtil.isError(result1)) {
				TransactionUtil.rollback();
				return ServiceUtil.returnError("Error, occur during delete the Exam");
				// return ServiceUtil.returnError((String) result1.get("errorMessage"));
			}

			Map<String, Object> result = dispatcher.runSync("deleteExam", input);

			if (ServiceUtil.isError(result)) {
				TransactionUtil.rollback();
				return ServiceUtil.returnError("Error, occur during delete the Exam");
				// return ServiceUtil.returnError((String) result.get("errorMessage"));
			}
			TransactionUtil.commit();
			return ServiceUtil.returnSuccess("Exam deleted successfully");
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
			createMap.put("examName", input.get("examName"));
			createMap.put("description", input.get("description"));
			try {
				createMap.put("noOfQuestions", Long.valueOf((String) input.get("noOfQuestions")));
			}catch(NumberFormatException e) {
				Debug.logError(e.getMessage(),ExamService.class.getName());
				return ServiceUtil.returnError("Number of question should be in number");
			}
			try {
				createMap.put("duration", Long.valueOf((String) input.get("duration")));
			}catch(NumberFormatException e) {
				Debug.logError(e.getMessage(),ExamService.class.getName());
				return ServiceUtil.returnError("Duration should be in number");
			}
			try {
				createMap.put("passPercentage", Long.valueOf((String) input.get("passPercentage")));
			}catch(NumberFormatException e) {
				Debug.logError(e.getMessage(),ExamService.class.getName());
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
			List<GenericValue> examList = EntityQuery.use(delegator).from("ExamMaster").queryList();
			result.put("examList", examList);
			return result;

		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
}
