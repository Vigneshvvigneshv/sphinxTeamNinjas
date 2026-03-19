package com.vastpro.sphinx.events;
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

        if (UtilValidate.isEmpty(userName) || UtilValidate.isEmpty(password)) {
            String errMsg = "user name and password are required fields on the form and can't be empty.";
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        }
        
        try {
           dispatcher.runSync("validateUserLoginService", UtilMisc.toMap("userName", userName, "password", password, "userLogin", userLogin));
        } catch (GenericServiceException e) {
            String errMsg = "Unable to check the record: " + e.toString();
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        }
        request.setAttribute("_EVENT_MESSAGE_", "Login success.");
        return "success";
    }
}
