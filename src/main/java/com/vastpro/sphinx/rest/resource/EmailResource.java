package com.vastpro.sphinx.rest.resource;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;

public class EmailResource {

	public Response emailResource(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Map<String, Object> result = new HashMap<>();
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", (Delegator) request.getAttribute("delegator"));
		}
		try {
			
			String partyId = (String) request.getAttribute("partyId");
			
			String examId = (String) request.getAttribute("examId");
			
			Map<String,Object> input=new HashMap<String, Object>();
			
			input.put("partyId", partyId);
			input.put("examId",examId);
			
			Map<String,Object>serviceResult=dispatcher.runSync("EmailService",input );
			return null;
		}catch(Exception e) {
			return null;
		}

		
		
		
	}
}
