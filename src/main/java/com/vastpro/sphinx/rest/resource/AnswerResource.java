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

@Path("/answer-submit")
public class AnswerResource {

	@POST
	@Path("/answer-value-submit")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response AnswerMaster(@Context HttpServletRequest request,@Context HttpServletResponse response) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", (Delegator) request.getAttribute("delegator"));
		}
		Map<String,Object>result=new HashMap<>();
		try {
			String questionId=(String)request.getAttribute("questionId");
			String examId=(String)request.getAttribute("examId");
			String partyId=(String)request.getAttribute("partyId");
			String answer=(String)request.getAttribute("Answer");
			
			
			Map<String,Object>input=new HashMap<>();
			
			input.put("questionId", questionId);
			input.put("examId", examId);
			input.put("partyId", partyId);
			input.put("answer", answer);
			
			
			
			Map<String,Object>serviceResult=dispatcher.runSync("AnswerService", input);
			
			if(ServiceUtil.isError(serviceResult)) {
				result.put("status", "Error");
				result.put("errorMessage", ServiceUtil.getErrorMessage(serviceResult));
				
				return Response.status(400).entity(result).build();
			}
			
			result.put("status", "success");
			result.put("successMessage",serviceResult.get("successMessage"));
			
			
			return Response.ok().entity(result).build();
			
			
		}catch(GenericServiceException e) {
			e.printStackTrace();
			result.put("status", "Error");
			result.put("errorMessage", e.getMessage());
			
			return Response.status(400).entity(result).build();
		}
		
	}
}
