package com.vastpro.sphinx.rest.resource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

@Path("/generate-questions")
public class GenerateQuestionResource {

	@POST
	@Path("/generate-question")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateQuestionsByTopic(@Context HttpServletRequest request, @Context HttpServletResponse reponse) {

		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Map<String, Object> result = new HashMap<>();
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", (Delegator) request.getAttribute("delegator"));
		}
		
		try {
			String examId = (String) request.getAttribute("examId");

		    if (examId == null || examId.isEmpty()) {
	            result.put("status", "ERROR");
	            result.put("message", "examId is required");
	            return Response.status(400).entity(result).build();
	        }
		    TransactionUtil.begin();
		    Map<String, Object> serviceInput = new HashMap<>();
	        serviceInput.put("examId", examId);
	        
	        
	        Map<String, Object> serviceResult =
	                        dispatcher.runSync("generateQuestionsService", serviceInput);
		   

		        if (ServiceUtil.isError(serviceResult)) {
		            TransactionUtil.rollback();
		            result.put("status", "ERROR");
		            result.put("errorMessage", ServiceUtil.getErrorMessage(serviceResult));
		            return Response.status(500).entity(result).build();
		        }
		    

		    TransactionUtil.commit();
		    result.put("status", "SUCCESS");
		    result.put("message", "Questions generated successfully");
		    return Response.ok(result).build();

		} catch (Exception e) {
		    try {
		        TransactionUtil.rollback();
		    } catch (GenericTransactionException e1) {
		        e1.printStackTrace();
		    }
		    e.printStackTrace();

		    result.put("status", "ERROR");
		    result.put("message", "Error in generating questions");
		    return Response.status(500).entity(result).build();
		}

	}
}
