package com.vastpro.sphinx.rest.resource;

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
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;
import org.apache.ofbiz.service.ServiceUtil;


@Path("/email")
public class EmailResource {

	
	@POST
	@Path("/send-email")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response emailResource(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Map<String, Object> result = new HashMap<>();
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", (Delegator) request.getAttribute("delegator"));
		}
		try {
			
			List<String> partyIdList = (List<String>) request.getAttribute("partyIdList");
			
			String examId = (String) request.getAttribute("examId");
			
			Map<String,Object> input=new HashMap<String, Object>();
			
			input.put("partyIdList", partyIdList);
			input.put("examId",examId);
			
			Map<String,Object>serviceResult=dispatcher.runSync("SendEmailService",input);
			
			if(ServiceUtil.isError(serviceResult)) {
				result.put("status", "Error");
				result.put("errorMessage", ServiceUtil.getErrorMessage(serviceResult));

				return Response.status(200).entity(result).build();
				}
				return Response.ok().entity(serviceResult).build();
		}catch(GenericServiceException e) {
			e.printStackTrace();
			result.put("status", "Error");
			result.put("errorMessage", e.getMessage());

			return Response.status(200).entity(result).build();
		}	
	}
}
