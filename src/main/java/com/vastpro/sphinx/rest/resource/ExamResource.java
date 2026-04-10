package com.vastpro.sphinx.rest.resource;

import java.util.HashMap;
import java.util.List;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.PathParam;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;

@Path("/exam")
public class ExamResource {
	@POST
	@Path("/create-exam")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
//	public Response createExam(Map<String,Object> input,@Context HttpServletRequest request,@Context HttpServletResponse response) {
		public Response createExam(@Context HttpServletRequest request,@Context HttpServletResponse response) {

		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
//		if(dispatcher==null) {
//			dispatcher=ServiceContainer.getLocalDispatcher("sphinx", (Delegator)request.getAttribute("delegator"));
//		}
		
		try {
			Map<String,Object> input= new HashMap<String, Object>();
			input.put("partyId",request.getAttribute("partyId"));
			input.put("examName",request.getAttribute("examName"));
			input.put("description",request.getAttribute("description"));
			input.put("noOfQuestions",request.getAttribute("noOfQuestions"));
			input.put("duration",request.getAttribute("duration"));
			input.put("passPercentage",request.getAttribute("passPercentage"));
			
			Map<String,Object> result=dispatcher.runSync("createExamOwn", input);
			return Response.ok(result).build();
			
		}catch(Exception e) {
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
	
	
	@DELETE
	@Path("/delete-exam")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
//	public Response deleteExam(Map<String,Object> input,@Context HttpServletRequest request,@Context HttpServletResponse response) {
		public Response deleteExam(@Context HttpServletRequest request,@Context HttpServletResponse response) {

		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
		if(dispatcher==null) {
			dispatcher=ServiceContainer.getLocalDispatcher("sphinx", (Delegator)request.getAttribute("delegator"));
		}
		
		try {
			Map<String,Object> input= new HashMap<String, Object>();
			input.put("examId",request.getAttribute("examId"));
			input.put("partyId",request.getAttribute("partyId"));
			
			Map<String,Object> result=dispatcher.runSync("deleteExamOwn", input);
			return Response.ok(result).build();
			
		}catch(Exception e) {
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
	
	
	@PUT
	@Path("/update-exam")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateExam(@Context HttpServletRequest request,@Context HttpServletResponse response){
		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
		try {
			Map<String,Object> input=new HashMap<String, Object>();
			input.put("examId",request.getAttribute("examId"));
			input.put("examName",request.getAttribute("examName"));
			input.put("description",request.getAttribute("description"));
			input.put("noOfQuestions",request.getAttribute("noOfQuestions"));
			input.put("duration",request.getAttribute("duration"));
			input.put("passPercentage",request.getAttribute("passPercentage"));
			Map<String,Object> result=dispatcher.runSync("updateExamOwn", input);
			return Response.ok(result).build();
		}catch(Exception e) {
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
	
	
	
	@GET
	@Path("/getall-exam")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAllExam(@Context HttpServletRequest request,@Context HttpServletResponse response){
		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
		try {
			Map<String,Object> result=dispatcher.runSync("getAllExam", new HashMap<String, Object>());
			return Response.ok(result).build();
		}catch(Exception e) {
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
	
	@GET
	@Path("/getexam/{examId}")
	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
	public Response getExamById(@PathParam("examId") String examId,@Context HttpServletRequest request){
		
		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
		try {
			Map<String,Object> input=new HashMap<String, Object>();
			input.put("examId", examId);
			Map<String,Object> result=dispatcher.runSync("getExamById",input );
			return Response.ok(result).build();
		}catch(Exception e) {
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
	
}
