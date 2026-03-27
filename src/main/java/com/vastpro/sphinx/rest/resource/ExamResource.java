package com.vastpro.sphinx.rest.resource;

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
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;

@Path("/exam")
public class ExamResource {
	@POST
	@Path("/createexam")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createExam(Map<String,Object> input,@Context HttpServletRequest request,@Context HttpServletResponse response) {
		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
		if(dispatcher==null) {
			dispatcher=ServiceContainer.getLocalDispatcher("sphinx", (Delegator)request.getAttribute("delegator"));
		}
		
		try {
			Map<String,Object> result=dispatcher.runSync("createExamOwn", input);
			return Response.ok(result).build();
			
		}catch(Exception e) {
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}

}
