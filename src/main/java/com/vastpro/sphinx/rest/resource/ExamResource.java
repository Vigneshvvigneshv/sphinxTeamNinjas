package com.vastpro.sphinx.rest.resource;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
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
	@Path("/create-question")
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
	    
		Map<String, Object> responseError = new HashMap<>();
	    try {
	        Delegator delegator      = getDelegator();
	        LocalDispatcher dispatcher = getDispatcher();

	        // questionId must be sent by frontend
	        
	        
	        String questionId = (String) params.get("questionId");
	        params.put("questionId", questionId);
	        if (questionId == null) {
	        	responseError.put("status",  "ERROR");
	        	responseError.put("message", "questionId is required");
	            return Response.status(400).entity(responseError).build();
	        }

	       
	        // Call service
	        Map<String, Object> serviceResult  = dispatcher.runSync("updateQuestionMaster", params);

	        if (ServiceUtil.isError(serviceResult)) {
	        	responseError.put("status",  "ERROR");
	        	responseError.put("message", ServiceUtil.getErrorMessage(serviceResult ));
	            return Response.status(500).entity(responseError ).build();
	        }

	        responseError.put("status",     "SUCCESS");
	        responseError.put("message",    "Question updated successfully");
	        responseError.put("questionId", serviceResult.get("questionId"));
	        return Response.ok(responseError).build();

	    } catch (Exception e) {
	    	responseError.put("status",  "ERROR");
	    	responseError.put("message", e.getMessage());
	        return Response.status(500).entity(responseError).build();
	    }
	}
}
