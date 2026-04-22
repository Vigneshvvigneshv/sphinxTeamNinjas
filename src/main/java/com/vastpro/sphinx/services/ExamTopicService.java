package com.vastpro.sphinx.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.transaction.TransactionUtil;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class ExamTopicService {

	public static Map<String, Object> createExamTopicService(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();

		LocalDispatcher dispatcher = dctx.getDispatcher();

		try {
			String examId = (String) context.get("examId");
			String topicId = (String) context.get("topicId");
			String percentageStr = (String) context.get("percentage");
			String topicPassPercentageStr = (String) context.get("topicPassPercentage");

			GenericValue exam = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId).queryOne();

			if (exam == null) {
				return ServiceUtil.returnError("exam not found");
			}
			
			if(topicId.trim().isEmpty()) {
				return ServiceUtil.returnError("Topic is Empty");
			}
			GenericValue topic = EntityQuery.use(delegator).from("topicMaster").where("topicId", topicId).queryOne();

			if (topic == null) {
				return ServiceUtil.returnError("Topic Not Available");
			}

			if (percentageStr == null || topicPassPercentageStr == null) {
				return ServiceUtil.returnError("percenatge and topicPassPercentage are required");
			}

			Double percentage = Double.parseDouble(percentageStr);
			Double topicPassPercentage = Double.parseDouble(topicPassPercentageStr);

			Map<String, Object> input = new HashMap<String, Object>();

			input.put("examId", examId);
			input.put("topicId", topicId);
			input.put("percentage", percentage);
			input.put("topicPassPercentage", topicPassPercentage);

			
			//TopicQuestionPercentage
			List<GenericValue> examMapping=EntityQuery.use(delegator)
											.from("ExamTopicMapping")
											.where("examId",examId)
											.queryList();
			
			double totalPercentage=0;
			double topicPercentage=0;
			for(GenericValue e:examMapping) {
				topicPercentage=e.getDouble("percentage");
				totalPercentage=totalPercentage+topicPercentage;
			}
			
			
			if((totalPercentage+percentage)>100) {
				return ServiceUtil.returnError("Total Question Percentage Should not be more than 100");
			}
			
			GenericValue examNoOFQuestions = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId).queryOne();
			if (examNoOFQuestions == null) {
			    return ServiceUtil.returnError("Exam not found");
			}
			
			Long noOfQuestions = exam.getLong("noOfQuestions");
			
			if(noOfQuestions==0) {
				return ServiceUtil.returnError("Number Of Questions in exam is cannot be 0");
			}
			
			List<GenericValue> questionList = EntityQuery.use(delegator)
							.from("questionMaster")
							.where("topicId", topicId)
							.queryList();
			
			if (percentage > 0 && (questionList == null || questionList.isEmpty())) {
				return ServiceUtil.returnError("No Questions found in " + (String) topic.get("topicName"));
			}
			
			int count = (int) Math.floor((percentage / 100) * noOfQuestions);
			
			int remaining=count-questionList.size()+1;
			
			if(count>questionList.size()) {
				return ServiceUtil.returnError("Not Enough Question to Add the topic "+topic.getString("topicName")+" add "+remaining +" more questions" );
			}
			
			
			Map<String, Object> serviceResult = dispatcher.runSync("createExamTopicService", input);

			if (ServiceUtil.isError(serviceResult)) {
				return ServiceUtil.returnError("Error Occured in Creating Exam Topic");
			}

			return ServiceUtil.returnSuccess("Exam TopicMaster Created SuccessFully");

		} catch (GenericServiceException | GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Error Occured in Creating Exam Topic");
		}
	}

	public static Map<String, Object> updateExamTopicService(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		try {
			String examId = (String) context.get("examId");
			String topicId = (String) context.get("topicId");
			String percentageStr = (String) context.get("percentage");
			String topicPassPercentageStr = (String) context.get("topicPassPercentage");

			GenericValue exam = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId).queryOne();

			if (exam == null) {
				return ServiceUtil.returnError("exam not found");
			}

			GenericValue topic = EntityQuery.use(delegator).from("topicMaster").where("topicId", topicId).queryOne();

			if (topic == null) {
				return ServiceUtil.returnError("Topic Not Found");
			}

			if (percentageStr == null || topicPassPercentageStr == null) {
				return ServiceUtil.returnError("percenatge and topicPassPercentage are required");
			}

			Double percentage = Double.parseDouble(percentageStr);
			Double topicPassPercentage = Double.parseDouble(topicPassPercentageStr);

			Map<String, Object> input = new HashMap<String, Object>();

			input.put("examId", examId);
			input.put("topicId", topicId);
			input.put("percentage", percentage);
			input.put("topicPassPercentage", topicPassPercentage);

			
			
			//TopicQuestionPercentage
			List<GenericValue> examMapping=EntityQuery.use(delegator)
											.from("ExamTopicMapping")
											.where("examId",examId)
											.queryList();
			
			double totalPercentage=0;
			for(GenericValue e:examMapping) {
				
				String existingTopicId=e.getString("topicId");
				
				if (!existingTopicId.equals(topicId)) {
			        totalPercentage += e.getDouble("percentage");
			    }
			}
			totalPercentage += percentage;
			
			
			if(totalPercentage>100) {
				return ServiceUtil.returnError("Total Question Percentage Should not be more than 100");
			}
			
			GenericValue examNoOFQuestions = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId).queryOne();
			if (examNoOFQuestions == null) {
			    return ServiceUtil.returnError("Exam not found");
			}
			
			Long noOfQuestions = exam.getLong("noOfQuestions");
			
			if(noOfQuestions==0) {
				return ServiceUtil.returnError("Number Of Questions in exam is cannot be 0");
			}
			
			List<GenericValue> questionList = EntityQuery.use(delegator)
							.from("questionMaster")
							.where("topicId", topicId)
							.queryList();
			
			if (percentage > 0 && (questionList == null || questionList.isEmpty())) {
				return ServiceUtil.returnError("No Questions found in " + (String) topic.get("topicName"));
			}
			
			int count = (int) Math.floor((percentage / 100) * noOfQuestions);
			
			int remaining=count-questionList.size()+1;
			
			if(count>questionList.size()) {
				return ServiceUtil.returnError("Not Enough Question to Add the topic "+topic.get("topicName")+" add "+remaining+" more Questions");
			}
			
			Map<String, Object> serviceResult = dispatcher.runSync("updateExamTopicService", input);

			if (ServiceUtil.isError(serviceResult)) {

				return ServiceUtil.returnError("Error Occured in Updating the Exam Topic");
			}

			return ServiceUtil.returnSuccess("Exam TopicMaster updated SuccessFully");

		} catch (GenericServiceException | GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Error Occured in Updating the Exam Topic");
		}
	}

	// Getting topicBy ExamId
	public static Map<String, Object> getTopicByExamIdService(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();

		try {

			String examId = (String) context.get("examId");

			if (examId == null || examId.trim().isEmpty()) {
				return ServiceUtil.returnError("examId is Required");
			}

			GenericValue exam = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId).queryOne();

			if (exam == null) {
				return ServiceUtil.returnError("Exam not Found");
			}

			
			List<GenericValue> exam1 = EntityQuery.use(delegator).from("ExamTopicMapping").where("examId", examId).queryList();

			List<Map<String, Object>> topicList = new ArrayList<>();

			for (GenericValue e : exam1) {

				Map<String, Object> tMap = new HashMap<>();
				String topicId = e.getString("topicId");
				
				//for returning topicPassPercentage
				GenericValue topic = EntityQuery.use(delegator).from("topicMaster").where("topicId", topicId).queryOne();

				GenericValue topicPercentage = EntityQuery.use(delegator).from("ExamTopicMapping")
								.where("examId", examId, "topicId", topicId).queryOne();
				String topicName = topic.getString("topicName");

				String percent = topicPercentage.getString("percentage");

				int percentage = (int) Double.parseDouble(percent);

				String topicPassPercentage = topicPercentage.getString("topicPassPercentage");
				int passPercentage = (int) Double.parseDouble(topicPassPercentage);

				tMap.put("topicId", topicId);
				tMap.put("topicName", topicName);
				tMap.put("percentage", percentage);
				tMap.put("topicPassPercentage", passPercentage);
				topicList.add(tMap);
			}

			Map<String, Object> result = ServiceUtil.returnSuccess();

			result.put("examId", exam.getString("examId"));
			result.put("examName", exam.getString("examName"));
			result.put("topicList", topicList);

			return result;

		} catch (Exception e) {
			return ServiceUtil.returnError("Error Fetching in Topic " + e.getMessage());
		}
	}

	public static Map<String, Object> deleteTopicInExamTopic(DispatchContext dctx, Map<String, Object> context) {

		LocalDispatcher dispatcher = dctx.getDispatcher();
		try {
			String topicId = (String) context.get("topicId");
			String examId = (String) context.get("examId");

			if (topicId == null || topicId.trim().isEmpty() || examId == null || examId.trim().isEmpty()) {
				return ServiceUtil.returnError("topicId and examId is required");
			}
			
			
			Map<String, Object> result = dispatcher.runSync("deleteTopicInExamTopicDB", context);

			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError((String) result.get("errorMessage"));
			}

			return ServiceUtil.returnSuccess("Topic Deleted Successfully");
		} catch (GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Failed to Delete Topic");
		}
	}
}
