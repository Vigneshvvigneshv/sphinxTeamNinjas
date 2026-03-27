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
public class UserResource {

	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response validateUser(Map<String,Object> userInput, @Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
//			LocalDispatcher dispatcher = getDispatcher();
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
			if (dispatcher == null) {
				dispatcher = ServiceContainer.getLocalDispatcher("sphinx",
						(Delegator) request.getAttribute("delegator"));
			}
//			String username = request.getParameter("userName");
//			String password = request.getParameter("password");
//
//			Map<String, Object> userInput = new HashMap<>();
//			userInput.put("userName", username);
//			userInput.put("password", password);

			Map<String, Object> result = dispatcher.runSync("validateUserLoginService", userInput);

//			return result;
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
	
	

	@POST
	@Path("/signup")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response signupUser(Map<String,Object> userInput,@Context HttpServletRequest request, @Context HttpServletResponse response) {
//		public  Response signupUser(Map<String, Object> userInput) {

		try {
//			LocalDispatcher dispatcher = getDispatcher();
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
			if (dispatcher == null) {
				dispatcher = ServiceContainer.getLocalDispatcher("sphinx",
						(Delegator) request.getAttribute("delegator"));
			}
//			String username = request.getParameter("userName");
//			String password = request.getParameter("password");
//			String firstName = request.getParameter("firstName");
//			String lastName = request.getParameter("lastName");
//			String email = request.getParameter("email");
//
//			Map<String, Object> userInput = new HashMap<>();
//			userInput.put("userName", username);
//			userInput.put("password", password);
//			userInput.put("firstName", firstName);
//			userInput.put("lastName", lastName);
//			userInput.put("email", email);

			Map<String, Object> result = dispatcher.runSync("userSignUpService", userInput);

//			return result;
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
}
