package com.vastpro.sphinx.rest.resource;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.service.LocalDispatcher;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

public class ExamAssignToUserResource {
	
	@POST
	@Path("assignexam")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response assignExam(@Context HttpServletRequest request) {
		LocalDispatcher dispatcher=(LocalDispatcher) request.getAttribute("dispatcher");
		try {
			Map<String,Object> input=new HashMap<String, Object>();
			input.put("partyId", request.getAttribute("partyId"));
			input.put("examId", request.getAttribute("examId"));
			Map<String,Object> result=dispatcher.runSync("assignExamOwn",input);
			return Response.ok(result).build();
		}catch(Exception e) {
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
	
}
