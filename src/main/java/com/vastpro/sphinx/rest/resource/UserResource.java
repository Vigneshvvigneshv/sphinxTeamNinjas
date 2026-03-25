package com.vastpro.sphinx.rest.resource;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;

import javax.ws.rs.core.Response;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

	@Context
	private HttpServletRequest request;

	@Context
	private ServletContext servletContext; // ← ADD THIS

	// Helper method to get Delegator
	private Delegator getDelegator() {
		Delegator delegator = (Delegator) servletContext.getAttribute("delegator");
		if (delegator == null) {
			// Fallback — get directly from factory
			delegator = DelegatorFactory.getDelegator("default");
		}
		return delegator;
	}

	// Helper method to get Dispatcher
	private LocalDispatcher getDispatcher() {
		LocalDispatcher dispatcher = (LocalDispatcher) servletContext.getAttribute("dispatcher");
		if (dispatcher == null) {
			// Fallback — get directly from ServiceContainer
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", // must match localDispatcherName in web.xml
					getDelegator());
		}
		return dispatcher;
	}
	

	@POST
	@Path("/login")
	public Response validateUser(Map<String, Object> userInput) {
		try {
			Delegator delegator=getDelegator();
			LocalDispatcher dispatcher = getDispatcher();

			if (dispatcher == null) {
				return Response.status(500).entity(Map.of("error", "Dispatcher is still null")).build();
			}

			
			Map<String, Object> result = dispatcher.runSync("validateUserLoginService", userInput);
		
			return Response.ok(result).build();

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
	
	

	@POST
	@Path("/signup")
//	public static Map<String, Object> signupUser(HttpServletRequest request, HttpServletResponse response) {
		public  Response signupUser(Map<String, Object> userInput) {
		
		try {	
			LocalDispatcher dispatcher = getDispatcher();
			Map<String, Object> result = dispatcher.runSync("userSignUpService", userInput);
			
//			return result;
			return Response.ok(result).build();
		}catch(Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
		
	
	}

}
