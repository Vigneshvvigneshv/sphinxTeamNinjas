package com.vastpro.sphinx.rest.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.transaction.GenericTransactionException;
import org.apache.ofbiz.entity.transaction.TransactionUtil;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/exam-topic")
public class ExamTopicResource {
	
	
	@POST
	@Path("/create-topic-in-exam")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	
	public Response createTopicInExam(@Context HttpServletRequest request,@Context HttpServletResponse response) {
		
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Map<String, Object> result = new HashMap<>();
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", (Delegator) request.getAttribute("delegator"));
		}

		Delegator delegator = (Delegator) request.getAttribute("delegator");
		
		try {
			
		
		String examId = (String) request.getAttribute("examId");
		List<Map<String, Object>> topicList = (List<Map<String, Object>>) request.getAttribute("topics");

		int currentPercentage = 0;
		List<String> sameTopicCheck = new ArrayList<>();

		for (Map<String, Object> topic : topicList) {
			String percentageStr = String.valueOf(topic.get("percentage"));
			String passPercentageStr = String.valueOf(topic.get("topicPassPercentage"));

			if (passPercentageStr.trim().isEmpty() || passPercentageStr == null) {
				return Response.status(400).entity(ServiceUtil.returnError("Passpercentage cannot be empty")).build();
			}
			// checking same topic exist and
			if (!sameTopicCheck.contains(String.valueOf((topic.get("topicId"))))) {

				sameTopicCheck.add(String.valueOf((topic.get("topicId"))));

				currentPercentage += Integer.parseInt(percentageStr);
			} else {
				return Response.status(400).entity(ServiceUtil.returnError("Topic cannot be same")).build();
			}
		}
		
		if (currentPercentage > 100) {
			return Response.ok().entity(ServiceUtil.returnError("percentage must be 100 ")).build();
		}
		
		TransactionUtil.begin();
		for (Map<String, Object> topic : topicList) {
			Map<String, Object> input = new HashMap<String, Object>();

			input.put("examId", examId);
			input.put("topicId", topic.get("topicId"));
			input.put("percentage", topic.get("percentage"));
			input.put("topicPassPercentage", topic.get("topicPassPercentage"));

			if (input.get("examId") == null || input.get("topicId") == null) {
				return Response.status(400).entity(ServiceUtil.returnError("examId and topicId are required")).build();
			}

			//
			GenericValue examTopic = EntityQuery.use(delegator).from("ExamTopicMapping")
							.where("examId", examId, "topicId", topic.get("topicId")).queryOne();

			Map<String, Object> serviceResult = null;
			if (examTopic == null) {
				serviceResult = dispatcher.runSync("createExamTopic", input);
			} else {
				serviceResult = dispatcher.runSync("updateExamTopic", input);
			}

			if (ServiceUtil.isError(serviceResult)) {
				TransactionUtil.rollback();
				result.put("status", "ERROR");
				result.put("errorMessage", ServiceUtil.getErrorMessage(serviceResult));

				return Response.status(500).entity(result).build();
			}
			
			TransactionUtil.commit();
			result.put("status", "success");
			result.put("successMessage",serviceResult.get("successMessage") );
		}
		return Response.ok().entity(result).build();
		
	}catch( GenericServiceException | GenericEntityException e) {
		try {
			TransactionUtil.rollback();
		} catch (GenericTransactionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		e.printStackTrace();
		result.put("status", "ERROR");
		result.put("errorMessage", e.getMessage());
		return Response.status(400).entity(result).build();
	}
		
	}
	
	
	
	
	
	@GET
	@Path("/get-topicby-examid")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getTopicByExamId(@Context HttpServletRequest request,@QueryParam("examId") String examId ) {
		
		 //getting dispatcher from request
  		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
  		if(dispatcher==null) {
  			dispatcher=ServiceContainer.getLocalDispatcher("sphinx", (Delegator)request.getAttribute("delegator"));
  		}
  		
  		Map<String,Object>result=new HashMap<>();
  		
  		try {
  			if(examId==null || examId.trim().isEmpty()) {
				result.put("status","ERROR");
				result.put("message","ExamId is Required");
				return Response.status(400).entity(result).build();
			}
  			
  			Map<String,Object>input=new HashMap<>();
  			
  			input.put("examId", examId);
  			
  			Map<String,Object>serviceResult=dispatcher.runSync("getTopicByExamId",input);
  			
  			if(ServiceUtil.isError(serviceResult)) {
				result.put("responseMessage", "ERROR");
				result.put("message", ServiceUtil.getErrorMessage(serviceResult));
				return Response.status(500).entity(result).build();
			}
  			
  			result.put("responseMessage", "SUCCESS");
  			result.put("examId", serviceResult.get("examId"));
  			result.put("examName",serviceResult.get("examName"));
  			result.put("topicList", serviceResult.get("topicList"));
  			result.put("message","success");
  			return Response.ok().entity(result).build();
  			
  		}catch(GenericServiceException e) {
  			e.printStackTrace();
  			result.put("responseMessage", "ERROR");
  			result.put("message", "Failed to fetch Topics");
  			return Response.status(500).entity(result).build();
  		}
	}
	
	
	//Delete Topic in ExamTopic
	
	@DELETE
	@Path("/delete-topic-in-exam-topic")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteTopicinExamTopic(@Context HttpServletRequest request,@Context HttpServletResponse response) {
		
		 //getting dispatcher from request
  		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
  		if(dispatcher==null) {
  			dispatcher=ServiceContainer.getLocalDispatcher("sphinx", (Delegator)request.getAttribute("delegator"));
  		}
		
  			Map<String,Object>result=new HashMap<String, Object>();
  		try {
  			
  			String topicId=(String)request.getAttribute("topicId");
  			String examId=(String)request.getAttribute("examId");
  			
  			Map<String,Object>input=new HashMap<>();
  			input.put("examId", examId);
  			input.put("topicId", topicId);
  			
  			Map<String,Object>serviceResult=dispatcher.runSync("deleteTopicInExamTopic",input);
  			
  			if(ServiceUtil.isError(serviceResult)) {
  				result.put("responseMessge", "ERROR");
  				result.put("message", ServiceUtil.getErrorMessage(serviceResult));	
  				return Response.status(500).entity(result).build();
  			}
  			
  			result.put("responseMessage", "success");
  			result.put("message","Topic Deleted SuccesFully");
  			
  			return Response.ok().entity(result).build();
  		}catch(GenericServiceException e) {
  			
  			e.printStackTrace();
  			result.put("message",e.getMessage());	
  			return Response.status(500).entity(result).build();
  		}
	}
}
