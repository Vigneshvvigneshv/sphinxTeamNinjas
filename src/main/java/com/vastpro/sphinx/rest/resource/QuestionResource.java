package com.vastpro.sphinx.rest.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;
import org.apache.ofbiz.service.ServiceUtil;

import com.vastpro.sphinx.util.ConvertValue;

@Path("/question")
public class QuestionResource {

	@Context
	private HttpServletRequest request;

	@Context
	private ServletContext servletContext;

	@POST
	@Path("/create-question")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createQuestion(@Context HttpServletRequest request, @Context HttpServletResponse response) {

		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", (Delegator) request.getAttribute("delegator"));
		}

		try {

			Map<String, Object> input = new HashMap<String, Object>();

			input.put("questionDetail", request.getAttribute("questionDetail"));
			input.put("optionA", request.getAttribute("optionA"));
			input.put("optionB", request.getAttribute("optionB"));
			input.put("optionC", request.getAttribute("optionC"));
			input.put("optionD", request.getAttribute("optionD"));
			input.put("answer", request.getAttribute("answer"));
			input.put("numAnswers", request.getAttribute("numAnswers"));
			input.put("questionTypeId", request.getAttribute("questionTypeId"));
			input.put("difficultyLevel", request.getAttribute("difficultyLevel"));
			input.put("answerValue", request.getAttribute("answerValue"));
			input.put("topicId", request.getAttribute("topicId"));
			input.put("negativeMarkValue", request.getAttribute("negativeMarkValue"));

			Map<String, Object> result = dispatcher.runSync("createQuestionService", input);
			return Response.ok(result).build();

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}

	}

	@PUT
	@Path("/update-question")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateQuestion(@Context HttpServletRequest request, @Context HttpServletResponse response) {

		// getting dispatcher from request
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", (Delegator) request.getAttribute("delegator"));
		}

		Map<String, Object> result = new HashMap<>();
		try {

		
			String questionIdStr = (String) request.getAttribute("questionId");
			if (questionIdStr == null) {
				result.put("status", "ERROR");
				result.put("message", "questionId is required");
				return Response.status(400).entity(result).build();
			}

			Long questionId = Long.valueOf(questionIdStr);

			Map<String, Object> input = new HashMap<String, Object>();

			input.put("questionId", questionId);
			input.put("questionDetail", request.getAttribute("questionDetail"));
			input.put("optionA", request.getAttribute("optionA"));
			input.put("optionB", request.getAttribute("optionB"));
			input.put("optionC", request.getAttribute("optionC"));
			input.put("optionD", request.getAttribute("optionD"));
			input.put("answer", request.getAttribute("answer"));
			input.put("numAnswers", request.getAttribute("numAnswers"));
			input.put("questionTypeId", request.getAttribute("questionTypeId"));
			input.put("difficultyLevel", request.getAttribute("difficultyLevel"));
			input.put("answerValue", request.getAttribute("answerValue"));
			input.put("topicId", request.getAttribute("topicId"));
			input.put("negativeMarkValue", request.getAttribute("negativeMarkValue"));

			System.out.println("negativeMarkValue" + request.getAttribute("negativeMarkValue"));
			System.out.println("questionTypeId" + request.getAttribute("questionTypeId"));

			// Call service
			Map<String, Object> serviceResult = dispatcher.runSync("updateQuestionMaster", input);

			if (ServiceUtil.isError(serviceResult)) {
				result.put("errorMessage", "ERROR");
				result.put("message", ServiceUtil.getErrorMessage(serviceResult));
				return Response.status(500).entity(result).build();
			}

			result.put("successMessage", "SUCCESS");
			result.put("message", "Question updated successfully");
			return Response.ok(result).build();

		} catch (Exception e) {
			result.put("responseMessage", "ERROR");
			result.put("message", e.getMessage());
			return Response.status(500).entity(result).build();
		}
	}

	@DELETE
	@Path("/delete-question")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteQuestion(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		Map<String, Object> result = new HashMap<>();

		// getting dispatcher from request
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", (Delegator) request.getAttribute("delegator"));
		}

		try {
			
			List<Integer> questionIdList =(List<Integer>)request.getAttribute("questionIds");

			if (questionIdList == null || questionIdList.isEmpty()) {
				result.put("responseMessage", "ERROR");
				result.put("message", "questionIds is required and must not be empty");
				return Response.status(400).entity(result).build();
			}

			 List<Long> questionIds = new ArrayList<>();
			 
			 for(Integer question:questionIdList) {
				 if (question < 0) {
					 continue;
				 }
				 try {
		                questionIds.add(Long.valueOf(question.toString()));
		            } catch (NumberFormatException e) {
		                result.put("responseMessage", "ERROR");
		                result.put("message", "Invalid questionId value: " + question);
		                return Response.status(400).entity(result).build();
		            }
			 }
		
			 if (questionIds.isEmpty()) {
		            result.put("responseMessage", "ERROR");
		            result.put("message", "No valid questionIds provided");
		            return Response.status(400).entity(result).build();
		        }

			 
			//Call Service
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("questionIds", questionIds);

			// Call service
			Map<String, Object> serviceResult = dispatcher.runSync("deleteQuestionMaster", input);

			if (ServiceUtil.isError(serviceResult)) {
				result.put("responseMessage", "ERROR");
				result.put("message", ServiceUtil.getErrorMessage(serviceResult));
				return Response.status(500).entity(result).build();
			}

			 List<Long> failedIds = (List<Long>) serviceResult.get("failedIds");
			 int deletedCount = ((Number) serviceResult.get("deletedCount")).intValue();
			 
			 
			 //partialy questions deleted successfully
			 if (failedIds != null && !failedIds.isEmpty()) {
		            result.put("responseMessage", "PARTIAL");
		            result.put("message", deletedCount + " question(s) deleted; "+ failedIds.size() + " failed");
		            result.put("deletedCount", deletedCount);
		            result.put("failedIds", failedIds);
		            result.put("errors", serviceResult.get("errors"));
		            return Response.status(207).entity(result).build(); // 207 Multi-Status
		        }
			 
			 //all questions deleted successfully
		        result.put("responseMessage", "SUCCESS");
		        result.put("message", deletedCount + " question(s) deleted successfully");
		        result.put("deletedCount", deletedCount);
		        result.put("failedIds", List.of());
		        return Response.ok(result).build();
			

		} catch (GenericServiceException e) {
			e.printStackTrace();
			result.put("responseMessage", "ERROR");
			result.put("message","Unexpected Error "+ e.getMessage());
			return Response.status(500).entity(result).build();
		}
	}

	@GET
	@Path("/getquestions-by-topic")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getQuestionsByTopic(@Context HttpServletRequest request, @QueryParam("topicId") String topicId,
					@QueryParam("pageNo") String pageNoStr, @QueryParam("pageSize") String pageSizeStr) {

		// getting dispatcher from request
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", (Delegator) request.getAttribute("delegator"));
		}

		Map<String, Object> result = new HashMap<>();
		try {

			if (topicId == null || topicId.trim().isEmpty()) {
				result.put("responseMessage", "ERROR");
				result.put("message", "TopicId is Required");
				return Response.status(400).entity(result).build();
			}

			Integer pageNo = ConvertValue.toInteger(pageNoStr);
			Integer pageSize = ConvertValue.toInteger(pageSizeStr);

			Map<String, Object> serviceCtx = new HashMap<>();

			serviceCtx.put("topicId", topicId);
			serviceCtx.put("pageNo", pageNo);
			serviceCtx.put("pageSize", pageSize);

			Map<String, Object> serviceResult = dispatcher.runSync("getQuestionsByTopic", serviceCtx);

			if (ServiceUtil.isError(serviceResult)) {
				result.put("responseMessage", "ERROR");
				result.put("message", ServiceUtil.getErrorMessage(serviceResult));
				return Response.status(500).entity(result).build();
			}

			result.put("responseMessage", "SUCCESS");
			result.put("topicId", serviceResult.get("topicId"));
			result.put("topicName", serviceResult.get("topicName"));
			result.put("totalCount", serviceResult.get("totalCount"));
			result.put("questionList", serviceResult.get("questionList"));

			result.put("pageNo", serviceResult.get("pageNo"));
			result.put("pageSize", serviceResult.get("pageSize"));
			result.put("totalPages", serviceResult.get("totalPages"));
			result.put("hasNext", serviceResult.get("hasNext"));
			result.put("hasPrevious", serviceResult.get("hasPrevious"));

			return Response.ok(result).build();

		} catch (GenericServiceException e) {
			result.put("responseMessage", "ERROR");
			result.put("message", e.getMessage());
			e.printStackTrace();
			return Response.status(500).entity(result).build();
		}
	}

	@GET
	@Path("/download-Template")
	@Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	public Response downloadTemplate() {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

		Map<String, ? extends Object> result;
		try {
			result = dispatcher.runSync("downloadTemplateDocument", UtilMisc.toMap());
			if (result.get("responseMessage") != null && result.get("responseMessage").equals("success")) {
				return Response.ok((Byte[]) result.get("bytes"))
								.header("Content-Disposition", "attachment; filename=questions_template.xlsx")
								.type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").build();
			} else {
				return null;
			}

		} catch (GenericServiceException e) {
			return null;
		}

	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadQuestions(@Context HttpServletRequest request, @Context HttpServletResponse response) {

		InputStream file;
		Part filePart;
		ByteBuffer buffer;
		try {
			filePart = request.getPart("file");

			if (filePart == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity(ServiceUtil.returnError("File was not found on Request!"))
								.build();
			}

			// file name
			String fileName = filePart.getSubmittedFileName();

			if (!fileName.endsWith(".xlsx")) {
				return Response.status(Response.Status.BAD_REQUEST).entity(ServiceUtil.returnError("Only Excel file are allowed!")).build();
			}

			byte[] bytes = filePart.getInputStream().readAllBytes();

			buffer = ByteBuffer.wrap(bytes);

			System.out.println("Uploaded: " + fileName);

		} catch (IOException | ServletException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ServiceUtil.returnError("Unexpected error occured!"))
							.build();
		}

		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

		try {
			Map<String, Object> result = dispatcher.runSync("uploadBulkQuestion", UtilMisc.toMap("file", buffer));
			if (result.get("responseMessage") != null && result.get("responseMessage").equals("error")) {
				return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
			} else {
				result.put("successMessage", "Questions uploaded successfully!");
			}

			return Response.ok().entity(result).build();

		} catch (GenericServiceException e) {

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
							.entity(ServiceUtil.returnError("Unexpected error occured, try again after sometime!")).build();
		}

	}

	@GET
	@Path("/getquestion-by-id")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQuetionsById(@QueryParam("questionId") String questionIdStr, @Context HttpServletRequest request,
					@Context HttpServletResponse response) {

		// getting dispatcher from request
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", (Delegator) request.getAttribute("delegator"));
		}

		Map<String, Object> result = new HashMap<>();

		try {
			// String questionIdStr=(String)request.getAttribute("questionId");

			Long questionId = Long.valueOf(questionIdStr);

			Map<String, Object> input = new HashMap<>();

			input.put("questionId", questionId);

			Map<String, Object> ServiceResult = dispatcher.runSync("getQuestionById", input);

			if (ServiceUtil.isError(ServiceResult)) {
				result.put("responseMessage", "ERROR");
				result.put("message", ServiceUtil.getErrorMessage(ServiceResult));
				result.put("question", null);

				return Response.status(500).entity(result).build();
			}

			result.put("responseMessage", "Success");
			result.put("message", "Question Fetched SuccessFully");
			result.put("question", ServiceResult.get("question"));
			return Response.ok().entity(result).build();
		} catch (GenericServiceException e) {
			result.put("responseMesage", "ERROR");
			result.put("message", "Failed to Fetch the question");

			return Response.ok().entity(result).build();

		}

	}

	
	@GET
	@Path("/getall-questions")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllQuestions(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("sphinx", (Delegator) request.getAttribute("delegator"));
		}

		try {
			String pageNo=(String)request.getParameter("pageNo");
			String pageSize=(String)request.getParameter("pageSize");
			
			Map<String,Object>input=new HashMap<>();
			input.put("pageNo",pageNo);
			input.put("pageSize",pageSize);
			Map<String, Object> ServiceResult = dispatcher.runSync("getallquestion",input);
			
		
			if(ServiceUtil.isError(ServiceResult)){
				return Response.status(400).entity(ServiceUtil.getErrorMessage(ServiceResult)).build();
			}
			
			
			return Response.ok().entity(ServiceResult).build();
			
			}catch(GenericServiceException e) {
			e.printStackTrace();
			return Response.status(500).entity(Map.of("error",e.getMessage())).build();
		}
		
	}
}
