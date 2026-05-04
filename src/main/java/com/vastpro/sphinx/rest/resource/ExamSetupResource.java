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

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;
import org.apache.ofbiz.service.ServiceUtil;


@Path("/exam-setup")
public class ExamSetupResource {
	
	
	@POST
	@Path("/setup-exam")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response examSetupResource(@Context HttpServletRequest request,@Context HttpServletResponse response) {
		
		
		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
		if(dispatcher==null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", (Delegator) request.getAttribute("delegator"));
		}
			String examId=(String)request.getAttribute("examId");
			Map<String,Object>result=new HashMap<String, Object>();
			
		try {
			
			Map<String,Object>serviceResult=dispatcher.runSync("SetupExamService" ,UtilMisc.toMap("examId",examId));
			
			if(ServiceUtil.isError(serviceResult)) {
				result.put("responseMessage","Error");
				result.put("errorMessage", serviceResult.get("errorMessage"));
				result.put("errors",serviceResult.get("errorMessageList"));
				return Response.status(500).entity(result).build();
			}
			
			return Response.ok(serviceResult).build();
		}catch(Exception e) {
			e.printStackTrace();
			result.put("errorMessage",e.getMessage());
			
			return Response.status(500).entity(result).build();
		}
		
		
	}
}
