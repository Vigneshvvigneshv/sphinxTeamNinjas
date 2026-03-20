package com.vastpro.sphinx.events;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;

public class UserLoginEvents {


	public static String validateUserLoginEvents(HttpServletRequest request, HttpServletResponse response) {
	    LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
	    GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");

	    String userName = request.getParameter("userName");
	    String password = request.getParameter("password");

	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");

	    PrintWriter out = null;

	    try {
	        out = response.getWriter();

	        // ✅ Validation
	        if (UtilValidate.isEmpty(userName) || UtilValidate.isEmpty(password)) {
	            out.write("{\"status\":\"error\",\"message\":\"Username or password is empty\"}");
	            return "none";
	        }

	        // ✅ Call service
	        dispatcher.runSync("validateUserLoginService",
	                UtilMisc.toMap("userName", userName, "password", password, "userLogin", userLogin));

	        // ✅ Success response
	        out.write("{\"status\":\"success\",\"message\":\"Login success\"}");

	    } catch (Exception e) {
	        e.printStackTrace();

	        try {
	            out.write("{\"status\":\"error\",\"message\":\"Server error\"}");
	        } catch (Exception ignore) {}
	    }

	    return "none"; // 🔥 VERY IMPORTANT
	}
}
