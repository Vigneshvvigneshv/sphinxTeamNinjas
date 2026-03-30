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
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;

@Path("/examtopic")
public class ExamTopicResource {
	
	@POST
	@Path("/createexamtopic")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createExamTopic(@Context HttpServletRequest request,@Context HttpServletResponse response ) {
		
		
		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
		
		if(dispatcher==null) {
			dispatcher=ServiceContainer.getLocalDispatcher("sphinx", (Delegator)request.getAttribute("delegator"));
		}
		
		try {
			Map<String,Object>input=new HashMap<String, Object>();
			
			input.put("examId",request.getAttribute("examId"));
			input.put("topicId",request.getAttribute("topicId"));
			input.put("percentage",request.getAttribute("percentage"));
			input.put("topicPassPercentage",request.getAttribute("topicPassPercentage"));
			
			
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
