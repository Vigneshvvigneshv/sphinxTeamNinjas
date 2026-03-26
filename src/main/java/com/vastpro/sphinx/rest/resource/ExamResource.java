package com.vastpro.sphinx.rest.resource;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/exam")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExamResource {
	
	
	@Context
	private HttpServletRequest request;

	@Context
	private ServletContext servletContext; // ← ADD THIS

	// Helper method to get Delegator
	private Delegator getDelegator() {
		Delegator delegator = (Delegator) servletContext.getAttribute("delegator");
		if (delegator == null) {
			// Fallback — get directly from factory
			delegator = DelegatorFactory.getDelegator("default");
		}
		return delegator;
	}
	
	
	private LocalDispatcher getDispatcher() {
		LocalDispatcher dispatcher = (LocalDispatcher) servletContext.getAttribute("dispatcher");
		if (dispatcher == null) {
			// Fallback — get directly from ServiceContainer
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", // must match localDispatcherName in web.xml
					getDelegator());
		}
		return dispatcher;
	}
	
	@POST
	@Path("/createquestion")
	public Response createQuestion(Map<String,Object> question) {
		try {	
			LocalDispatcher dispatcher = getDispatcher();
			
			
			
			Map<String, Object> result = dispatcher.runSync("createQuestionService", question);
			
			return Response.ok(result).build();
		}catch(Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
		
		
		
	}
	
	
	@PUT
	@Path("/update")
	public Response updateQuestion(Map<String, Object> params) {
	    
		Map<String, Object> result = new HashMap<>();
	    try {
	        Delegator delegator = getDelegator();
	        LocalDispatcher dispatcher = getDispatcher();

	        
	        
	        // questionId must be sent by frontend
	           
	        String questionIdStr = (String) params.get("questionId");
	        if (questionIdStr == null) {
	        	result.put("status",  "ERROR");
	        	result.put("message", "questionId is required");
	        	return Response.status(400).entity(result).build();
	        }
	        Long questionId = Long.valueOf(questionIdStr);
	        params.put("questionId", questionId);

	       
	        // Call service
	        Map<String, Object> serviceResult  = dispatcher.runSync("updateQuestionMaster", params);

	        if (ServiceUtil.isError(serviceResult)) {
	        	result.put("status",  "ERROR");
	        	result.put("message", ServiceUtil.getErrorMessage(serviceResult));
	            return Response.status(500).entity(result ).build();
	        }

	        result.put("status",     "SUCCESS");
	        result.put("message",    "Question updated successfully");
	        result.put("questionId", serviceResult.get("questionId"));
	        return Response.ok(result).build();

	    } catch (Exception e) {
	    	result.put("status",  "ERROR");
	    	result.put("message", e.getMessage());
	        return Response.status(500).entity(result).build();
	    }
	}
	
	@DELETE
	@Path("/delete")
	public Response deleteQuestion(Map<String,Object>param) {
	    Map<String, Object> result = new HashMap<>();
	    
	    LocalDispatcher dispatcher = getDispatcher();
	    try {
	        String questionIdStr=(String)param.get("questionId");
	        
	        if (questionIdStr == null || questionIdStr.trim().isEmpty()) {
	        	result.put("status",  "ERROR");
	        	result.put("message", "questionId is required");
	        	return Response.status(400).entity(result).build();
	        }
	        
	        Long questionId=Long.valueOf(questionIdStr);
	        
	        // Validate

	       

	        // Call service
	        Map<String, Object> serviceResult = dispatcher.runSync("deleteQuestionMaster", UtilMisc.toMap("questionId", questionId));

	        if (ServiceUtil.isError(serviceResult)) {
	            result.put("status",  "ERROR");
	            result.put("message", ServiceUtil.getErrorMessage(serviceResult));
	            return Response.status(500).entity(result).build();
	        }

	        result.put("status","SUCCESS");
	        result.put("message","Question deleted successfully");      
	        return Response.ok(result).build();

	    } catch (Exception e) {
	        result.put("status",  "ERROR");
	        result.put("message", e.getMessage());
	        return Response.status(500).entity(result).build();
	    }
	}
	
	@GET
	@Path("/getQuestionsbytopic")
	public Response getQuestionsByTopic(Map<String,Object> params) {
		LocalDispatcher dispatcher = getDispatcher();
		Map<String,Object>result=new HashMap<>();
		try {
			
			String topicId=(String)params.get("topicId");
			
			if(topicId==null || topicId.trim().isEmpty()) {
				result.put("status","ERROR");
				result.put("message","TopicId is Required");
				return Response.status(400).entity(result).build();
			}
			Map<String,Object>serviceResult=dispatcher.runSync("getQuestionsByTopic", UtilMisc.toMap("topicId",topicId));
			
			
			if(ServiceUtil.isError(serviceResult)) {
				result.put("status", "ERROR");
				result.put("message", ServiceUtil.getErrorMessage(serviceResult));
				return Response.status(500).entity(result).build();
			}
			
			result.put("status", "SUCCESS");
			result.put("topicId", serviceResult.get("topicId"));
			result.put("topicName", serviceResult.get("topicName"));
			result.put("totalCount", serviceResult.get("totalCount"));
			result.put("questionsList", serviceResult.get("questionsList"));
			return Response.ok(result).build();
			
			
		}catch(Exception e) {
			result.put("status", "ERROR");
			result.put("message", e.getMessage());
			return Response.status(500).entity(result).build();
		}
	}
}
