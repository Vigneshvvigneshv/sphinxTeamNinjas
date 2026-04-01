package com.vastpro.sphinx.rest.resource;

import java.util.HashMap;
import java.util.List;
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
import org.apache.ofbiz.service.ServiceUtil;
import org.eclipse.jgit.transport.ReceiveCommand.Result;

@Path("/generatequestions")
public class GenerateQuestionResource {
	
	
	@POST
	@Path("/generatequestion")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateQuestionsByTopic(@Context HttpServletRequest request,@Context HttpServletResponse reponse) {
		
		LocalDispatcher dispatcher=(LocalDispatcher)request.getAttribute("dispatcher");
		Map<String,Object>result=new HashMap<>();
		if(dispatcher==null) {
			dispatcher=ServiceContainer.getLocalDispatcher("sphinx", (Delegator)request.getAttribute("delegator"));
		}
		
		try {
			
			String examId=(String) request.getAttribute("examId");
			List<Map<String,Object>> topicList=(List<Map<String,Object>>)request.getAttribute("topics");
			
			
					
			
			 
			for(Map<String,Object> topic:topicList) {
				Map<String,Object>input=new HashMap<String,Object>();
				
				input.put("examId",examId);
				input.put("topicId",topic.get("topicId"));
				input.put("percentage",topic.get("percentage"));
				input.put("topicPassPercentage",topic.get("topicPassPercentage"));
				
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
				
				
			
				Map<String,Object> listTopics=new HashMap<String, Object>();
				
				listTopics.put("TopicList", topicList);
				listTopics.put("examId",examId);
				
				Map<String,Object>serviceResultgenerate=dispatcher.runSync("genarateQuestionsSerice",listTopics);
				
				if(ServiceUtil.isError(serviceResult)) {	
					result.put("status", "ERROR");
					result.put("errorMessage", ServiceUtil.getErrorMessage(serviceResult));
					return Response.status(500).entity(result).build();
				}
				
				result.put("status", "success");
				result.put("message", serviceResult.get("successMessage"));
				
				return Response.ok().entity(result).build();
			}
		
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
}
