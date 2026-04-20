package com.vastpro.sphinx.rest.resource;

import java.util.HashMap;
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
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/submit-exam")
public class ExamSubmitResource {
	
	@POST
    @Path("/submit-exam")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
	public Response submitExam(@Context HttpServletRequest request,@Context HttpServletResponse response) {
		
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Map<String,Object>result=new HashMap<>();

		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", (Delegator) request.getAttribute("delegator"));
		}
		
		try {
			String partyId=(String)request.getAttribute("partyId");
			String examId=(String)request.getAttribute("examId");
			
			Map<String,Object>input=new HashMap<>();
			input.put("examId",examId);
			input.put("partyId", partyId);
			
			
			 Map<String, Object> serviceResult =dispatcher.runSync("submitExam", input);
			 
			 if(ServiceUtil.isError(serviceResult)) {
				 result.put("responseMessage", "ERROR");
	             result.put("message", ServiceUtil.getErrorMessage(serviceResult));
	              return Response.status(500).entity(result).build(); 
			 }
			 
				result.put("responseMessage", "SUCCESS");
				result.put("message", "Exam submitted successfully");
				result.put("score", serviceResult.get("score"));
				result.put("totalMarks", serviceResult.get("totalMarks"));
				result.put("percentage", serviceResult.get("percentage"));
				result.put("passed", serviceResult.get("passed"));
				result.put("correctCount", serviceResult.get("correctCount"));
				result.put("wrongCount", serviceResult.get("wrongCount"));
				result.put("skippedCount", serviceResult.get("skippedCount"));
	            
	            return Response.ok(result).build();
			
		}catch(GenericServiceException e) {
			result.put("responseMessage", "ERROR");
            result.put("message", "Service error: " + e.getMessage());
            return Response.status(500).entity(result).build();
		}
		
	}
}
