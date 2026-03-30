package com.vastpro.sphinx.rest.resource;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
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

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;

import com.vastpro.sphinx.dto.TopicDTO;


@Path("/topic")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TopicResource {
	@POST
	@Path("/createtopic")
	public  Response createTopic(@Context HttpServletRequest request) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx",
					(Delegator) request.getAttribute("delegator"));
		}
		
		try {
			Map<String, Object> input=new HashMap<String, Object>();
			input.put("topicName",request.getAttribute("topicName"));
			request.getParameterMap();
			Map<String, Object> result = dispatcher.runSync("createTopicOwn", input);
			
			return Response.ok(result).build();
		}catch(Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
	
	@PUT
	@Path("/updatetopic")
	public  Response updateTopic(@Context HttpServletRequest request) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx",
					(Delegator) request.getAttribute("delegator"));
		}
		
		try {	
			
			Map<String, Object> input=new HashMap<String, Object>();
			input.put("topicId",request.getAttribute("topicId"));
			input.put("topicName",request.getAttribute("topicName"));
			Map<String, Object> result = dispatcher.runSync("updateTopicOwn", input);
			
			return Response.ok(result).build();
		}catch(Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
	
	@DELETE
	@Path("/deletetopic")
	public  Response deleteTopic(@Context HttpServletRequest request) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx",
					(Delegator) request.getAttribute("delegator"));
		}
		
		try {	
			
			Map<String, Object> input=new HashMap<String, Object>();
			input.put("topicId",request.getAttribute("topicId"));
			Map<String, Object> result = dispatcher.runSync("deleteTopicOwn", input);
			
			return Response.ok(result).build();
		}catch(Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
	
	
	@GET
	@Path("/getalltopic")
	public  Response getTopic(@Context HttpServletRequest request) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx",
					(Delegator) request.getAttribute("delegator"));
		}
		Map<String, Object> input = new HashMap<>();
		try {	
			Map<String, Object> result = dispatcher.runSync("getAllTopics", input);
			
			return Response.ok(result).build();
		}catch(Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}

}
