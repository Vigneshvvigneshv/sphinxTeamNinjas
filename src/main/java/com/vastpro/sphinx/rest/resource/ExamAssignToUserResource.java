package com.vastpro.sphinx.rest.resource;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


@Path("/exam-assign")
public class ExamAssignToUserResource {
	
	@POST
	@Path("/assign-exam")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response assignExam(@Context HttpServletRequest request) {
		LocalDispatcher dispatcher=(LocalDispatcher) request.getAttribute("dispatcher");
		try {
			Map<String,Object> input=new HashMap<String, Object>();
//			input.put("partyId", request.getAttribute("partyId"));
//			input.put("examId", request.getAttribute("examId"));
//			input.put("allowedAttempts", request.getAttribute("allowedAttempts"));
//			input.put("timeoutDays", request.getAttribute("timeoutDays"));
			input.put("examId", request.getAttribute("examId"));
			input.put("assignedUserList", request.getAttribute("assignedUserList"));
			Map<String,Object> result=dispatcher.runSync("assignExamOwn",input);
			return Response.ok(result).build();
		}catch(Exception e) {
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
	
	@DELETE
	@Path("/remove-assigned-exam")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeAssignedExam(@Context HttpServletRequest request) {
		LocalDispatcher dispatcher=(LocalDispatcher) request.getAttribute("dispatcher");
		try {
			Map<String,Object> input=new HashMap<String, Object>();
			input.put("partyId", request.getAttribute("partyId"));
			input.put("examId", request.getAttribute("examId"));
			Map<String,Object> result=dispatcher.runSync("removeAssignExamOwn",input);
			return Response.ok(result).build();
		}catch(Exception e) {
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
	@PUT
	@Path("/update-assigned-exam")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateAssignedExam(@Context HttpServletRequest request,@Context HttpServletResponse response) {
		LocalDispatcher dispatcher=(LocalDispatcher) request.getAttribute("dispatcher");
		try {
			Map<String,Object> input=new HashMap<String, Object>();
			input.put("partyId", request.getAttribute("partyId"));
			input.put("examId", request.getAttribute("examId"));
			input.put("allowedAttempts", request.getAttribute("allowedAttempts"));
			input.put("timeoutDays", request.getAttribute("timeoutDays"));
			Map<String,Object> result=dispatcher.runSync("updateAssignedExamOwn", input);
			return Response.ok(result).build();
			
		}catch(Exception e) {
			return Response.status(500).entity(Map.of("error",e.getMessage())).build();
		}
	}
	
	
	
	@PUT
	@Path("/increase-attempts")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response increaseAttempts(@Context HttpServletRequest request,@Context HttpServletResponse response){
		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
		try {
			
			Map<String,Object> input=new HashMap<String, Object>();
			input.put("partyId", request.getAttribute("partyId"));
			input.put("examId", request.getAttribute("examId"));
			Map<String,Object> result=dispatcher.runSync("increaseAttemptsOwn", input);
			return Response.ok(result).build();
			
		}catch(Exception e) {
			return Response.status(500).entity(Map.of("error",e.getMessage())).build();
		}
	} 
	
	@GET
	@Path("/get-assigned-user")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAssignedUser(@Context HttpServletRequest request) {
		LocalDispatcher dispatcher=(LocalDispatcher) request.getAttribute("dispatcher");
		try {
			Map<String,Object> input=new HashMap<String, Object>();
			input.put("examId", request.getAttribute("examId"));
			Map<String,Object> result=dispatcher.runSync("getAssignedUser",input);
			return Response.ok(result).build();
		}catch(Exception e) {
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
}
