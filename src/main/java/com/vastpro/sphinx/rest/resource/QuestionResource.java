package com.vastpro.sphinx.rest.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.vastpro.sphinx.util.QuestionColumnConfigUtil;
import com.vastpro.sphinx.util.QuestionColumnConfigUtil.ColumnConfig;


@Path("/question")

public class QuestionResource {
	
	
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
	@Path("/createquestion")
	public Response createQuestion(Map<String,Object> question) {
		try {	
			LocalDispatcher dispatcher = getDispatcher();
			
			
			
			Map<String, Object> result = dispatcher.runSync("createQuestionService", question);
			
			return Response.ok(result).build();
		}catch(Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
		
		
		
	}
	
	
	@PUT
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateQuestion(Map<String, Object> params) {
	    
		Map<String, Object> result = new HashMap<>();
	    try {
	        Delegator delegator = getDelegator();
	        LocalDispatcher dispatcher = getDispatcher();

	        
	        
	        // questionId must be sent by frontend
	           
	        String questionIdStr = (String) params.get("questionId");
	        if (questionIdStr == null) {
	        	result.put("status",  "ERROR");
	        	result.put("message", "questionId is required");
	        	return Response.status(400).entity(result).build();
	        }
	        Long questionId = Long.valueOf(questionIdStr);
	        params.put("questionId", questionId);

	       
	        // Call service
	        Map<String, Object> serviceResult  = dispatcher.runSync("updateQuestionMaster", params);

	        if (ServiceUtil.isError(serviceResult)) {
	        	result.put("status",  "ERROR");
	        	result.put("message", ServiceUtil.getErrorMessage(serviceResult));
	            return Response.status(500).entity(result ).build();
	        }

	        result.put("status",     "SUCCESS");
	        result.put("message",    "Question updated successfully");
	        result.put("questionId", serviceResult.get("questionId"));
	        return Response.ok(result).build();

	    } catch (Exception e) {
	    	result.put("status",  "ERROR");
	    	result.put("message", e.getMessage());
	        return Response.status(500).entity(result).build();
	    }
	}
	
	@DELETE
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteQuestion(Map<String,Object>param) {
	    Map<String, Object> result = ServiceUtil.returnSuccess("Question successfully Deleted");
	    
	    LocalDispatcher dispatcher = getDispatcher();
	    try {
	        String questionIdStr=(String)param.get("questionId");
	        
	        if (questionIdStr == null || questionIdStr.trim().isEmpty()) {
	        	result.put("status",  "ERROR");
	        	result.put("message", "questionId is required");
	        	return Response.status(400).entity(result).build();
	        }
	        
	        Long questionId=Long.valueOf(questionIdStr);
	        
	        // Validate

	       

	        // Call service
	        Map<String, Object> serviceResult = dispatcher.runSync("deleteQuestionMaster", UtilMisc.toMap("questionId", questionId));

	        if (ServiceUtil.isError(serviceResult)) {
	            result.put("status",  "ERROR");
	            result.put("message", ServiceUtil.getErrorMessage(serviceResult));
	            return Response.status(500).entity(result).build();
	        }

	        result.put("status","SUCCESS");
	        result.put("message","Question deleted successfully");      
	        return Response.ok(result).build();

	    } catch (Exception e) {
	        result.put("status",  "ERROR");
	        result.put("message", e.getMessage());
	        return Response.status(500).entity(result).build();
	    }
	}
	
	@GET
	@Path("/getQuestionsbytopic")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getQuestionsByTopic(Map<String,Object> params) {
		LocalDispatcher dispatcher = getDispatcher();
		Map<String,Object>result=new HashMap<>();
		try {
			
			String topicId=(String)params.get("topicId");
			
			if(topicId==null || topicId.trim().isEmpty()) {
				result.put("status","ERROR");
				result.put("message","TopicId is Required");
				return Response.status(400).entity(result).build();
			}
			Map<String,Object>serviceResult=dispatcher.runSync("getQuestionsByTopic", UtilMisc.toMap("topicId",topicId));
			
			
			if(ServiceUtil.isError(serviceResult)) {
				result.put("status", "ERROR");
				result.put("message", ServiceUtil.getErrorMessage(serviceResult));
				return Response.status(500).entity(result).build();
			}
			
			result.put("status", "SUCCESS");
			result.put("topicId", serviceResult.get("topicId"));
			result.put("topicName", serviceResult.get("topicName"));
			result.put("totalCount", serviceResult.get("totalCount"));
			result.put("questionList", serviceResult.get("questionList"));
			return Response.ok(result).build();
			
			
		}catch(Exception e) {
			result.put("status", "ERROR");
			result.put("message", e.getMessage());
			return Response.status(500).entity(result).build();
		}
	}
	
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadQuestions(@FormDataParam("file") InputStream file,@FormDataParam("file") FormDataContentDisposition fileDetail) {
		
		 if (file == null || fileDetail == null) {
		        return Response.status(400)
		                .entity(ServiceUtil.returnError("File not received. Check Postman key name."))
		                .build();
		    }
		String fileName=fileDetail.getFileName();
		
		if(!fileName.toLowerCase().endsWith(".xlsx")) {
			return Response.status(400).entity(ServiceUtil.returnError("only files with .xlsx are allowed")).build();
		}
		
		try {
			Workbook workbook = WorkbookFactory.create(file);
			Sheet sheet = workbook.getSheetAt(0);
			
			List<Map<String, Object>> questions = new ArrayList<>();
			
			for(int i=1;i<=sheet.getLastRowNum();i++) {
				Row row = sheet.getRow(i);
				if (row == null)
					continue;
				
				Map<String, Object> question = new HashMap<>();
				
				List<ColumnConfig> columns=QuestionColumnConfigUtil.getColumnConfigs();
				
				for (ColumnConfig col : columns) {
					Cell cell = row.getCell(col.index);


					if (col.required && (cell == null || cell.getCellType() == CellType.BLANK)) {
						return Response.status(400)
										.entity(ServiceUtil.returnError(
														"Row " + i + ", Column " + col.index + " " + col.label + " is required", null))
										.build();
					}

					if (cell == null) {
						question.put(col.field, null);
						continue;
					}


					switch (cell.getCellType()) {

						case NUMERIC:
							double numVal = cell.getNumericCellValue();

						    if ("Number".equalsIgnoreCase(col.type)) {

						        if ("answerValue".equals(col.field) || "negativeMarkValue".equals(col.field)) {
						            question.put(col.field, numVal); // Double
						        } else {
						            question.put(col.field, (long) numVal); // Long
						        }

						    } else {
						        question.put(col.field, String.valueOf((long) numVal));
						    }
						    break;

						case STRING:
							String strVal = cell.getStringCellValue();
							question.put(col.field, strVal != null ? strVal.trim() : null);
							break;

						case BOOLEAN:
							question.put(col.field, cell.getBooleanCellValue());
							break;

						case BLANK:
							question.put(col.field, null);
							break;
						default:
							question.put(col.field, null);
							break;
					}
				}
				questions.add(question);
			}
			
			for (Map<String, ? extends Object> question : questions) {
				getDispatcher().runSync("createQuestionService", question);
			}
			
			return Response.status(201).entity(ServiceUtil.returnSuccess("Question uploaded successfully")).build();
			
		}catch(EncryptedDocumentException  e) {
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}catch(IOException e) {
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}catch(GenericServiceException e) {
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
		
	}
}
