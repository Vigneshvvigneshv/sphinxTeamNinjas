package com.vastpro.sphinx.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

public class CertificateService {

    public static Map<String, Object> generateCertificate(
            DispatchContext dctx, Map<String, Object> context) {

        Delegator delegator = dctx.getDelegator();

        String examId  = (String) context.get("examId");
        String partyId = (String) context.get("partyId");

        try {
            if (UtilValidate.isEmpty(examId) || UtilValidate.isEmpty(partyId)) {
                return ServiceUtil.returnError("Sorry,Try again later or contact the admin");
            }

            GenericValue exam = EntityQuery.use(delegator)
                    .from("ExamMaster").where("examId", examId).queryOne();

            GenericValue person = EntityQuery.use(delegator)
                    .from("Person").where("partyId", partyId).queryOne();

            GenericValue examResultDetails = EntityQuery.use(delegator)
                    .from("ExamResult")
                    .where("partyId", partyId, "examId", examId).queryOne();

            if (exam == null || person == null || examResultDetails == null) {
                return ServiceUtil.returnError("Invalid exam or user");
            }

            String candidateName = person.getString("firstName") + " "
                    + (UtilValidate.isNotEmpty(person.getString("lastName"))
                            ? person.getString("lastName") : "");

            String examName = exam.getString("examName");
            String date     = new SimpleDateFormat("dd MMMM yyyy").format(new Date());
            String score    = (String) examResultDetails.get("score");

            // Derive pass/fail status from score
            String status = "FAIL";
            if(examResultDetails.getLong("passed")!=0) {
            	status="PASS";
            }
              
            

            byte[] pdfBytes = generateWithFop(
                    candidateName.trim(), examName, date, score, status);

            String filename = "Sphinx_Certificate.pdf";

            Map<String, Object> result = ServiceUtil.returnSuccess();
            result.put("pdfBytes", pdfBytes);
            result.put("filename", filename);
            return result;

        } catch (Exception e) {
            Debug.logError("Error occur in the generte cerificate", CertificateService.class.getName());
            return ServiceUtil.returnError(e.getMessage());
        }
    }

    private static byte[] generateWithFop(
            String candidateName, String examName,
            String date, String score, String status) throws Exception {

        String ofbizHome = System.getProperty("ofbiz.home");

        String templatePath = Paths
                .get(ofbizHome, "plugins", "sphinx", "src",
                        "main", "java", "certificate.fo.xml")
                .toString();

        Debug.logInfo("Looking for FO template at: " + templatePath, CertificateService.class.getName());

        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            throw new Exception("Template file NOT FOUND at: " + templatePath);
        }

        Debug.logInfo("Template found!", CertificateService.class.getName());

        InputStream foTemplate = new FileInputStream(templateFile);
        String foContent = new String(foTemplate.readAllBytes())
                .replace("${candidateName}", candidateName)
                .replace("${examName}", examName)
                .replace("${date}",date)
                .replace("${score}",score)
                .replace("${status}",status);
        foTemplate.close();

        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, baos);

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();

        Source src    = new StreamSource(new StringReader(foContent));
        Result result = new SAXResult(fop.getDefaultHandler());

        transformer.transform(src, result);

        byte[] pdfBytes = baos.toByteArray();

        if (pdfBytes.length == 0) {
            throw new Exception(
                    "FOP produced 0 bytes — check certificate.fo.xml for XML errors");
        }

        Debug.logInfo("PDF generated, size: " + pdfBytes.length + " bytes", CertificateService.class.getName());
        return pdfBytes;
    }
}