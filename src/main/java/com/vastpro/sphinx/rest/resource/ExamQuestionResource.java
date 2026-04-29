package com.vastpro.sphinx.rest.resource;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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


@Path("/exam-question")
public class ExamQuestionResource {
	

	@GET
	@Path("/get-exam-questions")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllExamQuestions(@Context HttpServletRequest request,@Context HttpServletResponse response) {
		
		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
		if(dispatcher==null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", (Delegator) request.getAttribute("delegator"));
		}
		Map<String,Object>result=new HashMap<>();
		try {
			
			if(dispatcher==null) {
				return Response.status(400).entity(ServiceUtil.returnError("dispatcher is null")).build();
			}
			String examId=(String)request.getParameter("examId");
			String pageNo=(String)request.getParameter("pageNo");
			String pageSize=(String)request.getParameter("pageSize");
			
			
			Map<String,Object>input=new HashMap<>();
			input.put("examId", examId);
			if (pageNo != null) {
			    input.put("pageNo", Integer.parseInt(pageNo));
			}
			
			if (pageSize != null) {
			    input.put("pageSize", Integer.parseInt(pageSize));
			}

			
			Map<String,Object> serviceResult=dispatcher.runSync("getallExamQuestions", input);
			
			if(ServiceUtil.isError(serviceResult)) {
				result.put("status", "Error");
				result.put("errorMessage",ServiceUtil.getErrorMessage(serviceResult));
				
				return Response.status(400).entity(result).build();
			}
			
			return Response.ok().entity(serviceResult).build();
			
		}catch(GenericServiceException e) {
			e.printStackTrace();
			result.put("status", "Error");
			result.put("errorMessage",e.getMessage());
			
			return Response.status(400).entity(result).build();
		}
	}
	
}
