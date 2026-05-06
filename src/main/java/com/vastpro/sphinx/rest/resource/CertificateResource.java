package com.vastpro.sphinx.rest.resource;

import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.ofbiz.base.util.UtilMisc;

import java.io.ByteArrayOutputStream;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/certificate")
public class CertificateResource {

    @POST
    @Path("/generate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/pdf")
    public Response generateCertificate(@Context HttpServletRequest request) {
        try {
            LocalDispatcher dispatcher = (LocalDispatcher)
                request.getAttribute("dispatcher");

            // Call the OFBiz service defined in services.xml
            Map<String, Object> serviceContext = UtilMisc.toMap(
               "partyId",request.getAttribute("partyId"),
               "examId",request.getAttribute("examId")            );

            Map<String, Object> result = dispatcher.runSync(
                "generateSphinxCertificate", serviceContext
            );

            if (ServiceUtil.isError(result)) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ServiceUtil.getErrorMessage(result)))
                    .build();
            }

            ByteArrayOutputStream pdfStream =
                (ByteArrayOutputStream) result.get("pdfBytes");

            String filename = "sphinx-certificate.pdf";

            return Response.ok(pdfStream.toByteArray())
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "application/pdf")
                .header("Access-Control-Allow-Origin", "*")
                .build();

        } catch (Exception e) {
            return Response.status(500)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }


  
}