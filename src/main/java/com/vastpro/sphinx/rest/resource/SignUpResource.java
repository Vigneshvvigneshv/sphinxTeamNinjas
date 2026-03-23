package com.vastpro.sphinx.rest.resource;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;

import javax.ws.rs.core.Context;

@Path("/signup")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SignUpResource {
	
	@Context
	private HttpServletRequest request;
	
	@Context
	private ServletContext servletContext;
	
	
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
	
	

		//signup Resource
		@POST
		@Path("/signup")
		public Response signupUser(Map<String, Object> params) {
			
			try {	
				LocalDispatcher dispatcher = getDispatcher();
				Map<String, Object> result = dispatcher.runSync("userSignUpService", params);
				
				return Response.ok(result).build();
			}catch(Exception e) {
				e.printStackTrace();
				return Response.status(500).entity(Map.of("error", e.getMessage())).build();
			}
			
		
		}	
}
