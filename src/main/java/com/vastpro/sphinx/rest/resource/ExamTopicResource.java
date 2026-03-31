package com.vastpro.sphinx.rest.resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/examtopic")
public class ExamTopicResource {
	
	@POST
	@Path("/createexamtopic")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createExamTopic(@Context HttpServletRequest request,@Context HttpServletResponse response ) {
		
		
		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
		Map<String,Object>result=new HashMap<>();
		if(dispatcher==null) {
			dispatcher=ServiceContainer.getLocalDispatcher("sphinx", (Delegator)request.getAttribute("delegator"));
		}
		
		try {
			Map<String,Object>input=new HashMap<String,Object>();
			
			input.put("examId",request.getAttribute("examId"));
			input.put("topicId",request.getAttribute("topicId"));
			input.put("percentage",request.getAttribute("percentage"));
			input.put("topicPassPercentage",request.getAttribute("topicPassPercentage"));
		
			
			if (input.get("examId") == null || input.get("topicId") == null) {
			    return Response.status(400)
			        .entity(ServiceUtil.returnError("examId and topicId are required"))
			        .build();
			}
			
			
			Map<String,Object>serviceResult=dispatcher.runSync("createExamTopic", input);
			
			
			if(ServiceUtil.isError(serviceResult)) {	
				result.put("status", "ERROR");
				result.put("errorMessage", ServiceUtil.getErrorMessage(serviceResult));
				return Response.status(500).entity(result).build();
			}
			
			result.put("status", "success");
			result.put("message", serviceResult.get("successMessage"));
			return Response.ok().entity(result).build();
			
		}catch(GenericServiceException e) {
			e.printStackTrace();
			result.put("status","ERROR");
			result.put("message", e.getMessage());
			return Response.status(500).entity(result).build();
		}
		
	}
	
	@GET
	@Path("/gettopicbyexamid")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getTopicByExamId(@Context HttpServletRequest request,@QueryParam("examId") String examId ) {
		
		 //getting dispatcher from request
  		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
  		if(dispatcher==null) {
  			dispatcher=ServiceContainer.getLocalDispatcher("sphinx", (Delegator)request.getAttribute("delegator"));
  		}
  		
  		Map<String,Object>result=new HashMap<>();
  		
  		try {
  			if(examId==null || examId.trim().isEmpty()) {
				result.put("status","ERROR");
				result.put("message","ExamId is Required");
				return Response.status(400).entity(result).build();
			}
  			
  			Map<String,Object>input=new HashMap<>();
  			
  			input.put("examId", examId);
  			
  			Map<String,Object>serviceResult=dispatcher.runSync("getTopicByExamId",input);
  			
  			if(ServiceUtil.isError(serviceResult)) {
				result.put("status", "ERROR");
				result.put("message", ServiceUtil.getErrorMessage(serviceResult));
				return Response.status(500).entity(result).build();
			}
  			
  			result.put("status", "SUCCESS");
  			result.put("examId", serviceResult.get("examId"));
  			result.put("examName",serviceResult.get("examName"));
  			result.put("topicList", serviceResult.get("topicList"));
  			result.put("message","success");
  			return Response.ok().entity(result).build();
  			
  		}catch(GenericServiceException e) {
  			e.printStackTrace();
  			result.put("message", e.getMessage());
  			return Response.status(500).entity(result).build();
  		}
	}
	
	
	//Delete Topic in ExamTopic
	
	@DELETE
	@Path("/deletetopicinexamtopic")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteTopicinExamTopic(@Context HttpServletRequest request,@Context HttpServletResponse response) {
		
		 //getting dispatcher from request
  		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
  		if(dispatcher==null) {
  			dispatcher=ServiceContainer.getLocalDispatcher("sphinx", (Delegator)request.getAttribute("delegator"));
  		}
		
  			Map<String,Object>result=new HashMap<String, Object>();
  		try {
  			
  			String topicId=(String)request.getAttribute("topicId");
  			String examId=(String)request.getAttribute("examId");
  			
  			Map<String,Object>input=new HashMap<>();
  			input.put("examId", examId);
  			input.put("topicId", topicId);
  			
  			Map<String,Object>serviceResult=dispatcher.runSync("deleteTopicInExamTopic",input);
  			
  			if(ServiceUtil.isError(serviceResult)) {
  				result.put("status", "ERROR");
  				result.put("message", ServiceUtil.getErrorMessage(serviceResult));	
  				return Response.status(500).entity(result).build();
  			}
  			
  			result.put("status", "success");
  			result.put("message","Topic Deleted SuccesFully");
  			
  			return Response.ok().entity(result).build();
  		}catch(GenericServiceException e) {
  			
  			e.printStackTrace();
  			result.put("message",e.getMessage());	
  			return Response.status(500).entity(result).build();
  		}
	}
}
