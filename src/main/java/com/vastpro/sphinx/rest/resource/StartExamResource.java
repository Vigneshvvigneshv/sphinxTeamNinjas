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

import org.apache.ofbiz.service.LocalDispatcher;

@Path("start-exam")
public class StartExamResource {
	
	@POST
	@Path("exam-start")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startExam(@Context HttpServletRequest request,@Context HttpServletResponse response) {
		LocalDispatcher dispatcher=(LocalDispatcher) request.getAttribute("dispatcher");
		try {
			Map<String,Object> input=new HashMap<String, Object>();
			input.put("partyId", request.getAttribute("partyId"));
			input.put("examId", request.getAttribute("examId"));
			Map<String,Object> result=dispatcher.runSync("startExamOwn", input);
			return Response.ok(result).build();
			
		}catch(Exception e) {
			return Response.status(500).entity(Map.of("error",e.getMessage())).build();
		}
	}
	
}
