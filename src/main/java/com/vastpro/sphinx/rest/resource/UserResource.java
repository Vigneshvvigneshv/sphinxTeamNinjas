package com.vastpro.sphinx.rest.resource;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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

public class UserResource {

	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response validateUser(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
//			LocalDispatcher dispatcher = getDispatcher();
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
			if (dispatcher == null) {
				dispatcher = ServiceContainer.getLocalDispatcher("sphinx",
						(Delegator) request.getAttribute("delegator"));
			}

			Map<String, Object> userInput = new HashMap<>();
			userInput.put("userName", request.getAttribute("userName"));
			userInput.put("password", request.getAttribute("password"));

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
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response signupUser(@Context HttpServletRequest request, @Context HttpServletResponse response) {
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
			Map<String, Object> userInput = new HashMap<>();
			userInput.put("userName", request.getAttribute("userName"));
			userInput.put("password", request.getAttribute("password"));
			userInput.put("firstName", request.getAttribute("firstName"));
			userInput.put("lastName", request.getAttribute("lastName"));
			userInput.put("email", request.getAttribute("email"));
			userInput.put("roleTypeId",request.getAttribute("roleTypeId"));
			Map<String, Object> result = dispatcher.runSync("userSignUpServiceOwn", userInput);

//			return result;
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}

	@GET
	@Path("/getalluser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllUser(@Context HttpServletRequest request) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		try {
			Map<String, Object> result = dispatcher.runSync("getAllUser", new HashMap<String, Object>());
			return Response.ok(result).build();
		} catch (Exception e) {
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}
}
