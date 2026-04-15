package com.vastpro.sphinx.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntity;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.transaction.GenericTransactionException;
import org.apache.ofbiz.entity.transaction.TransactionUtil;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vastpro.sphinx.util.QuestionColumnConfigUtil;
import com.vastpro.sphinx.util.QuestionColumnConfigUtil.ColumnConfig;

public class QuestionService {
	public static Map<String, Object> createQuestionService(DispatchContext dctx, Map<String, Object> questions) {
		LocalDispatcher dispatcher = dctx.getDispatcher();
		try {
			Delegator delegator = dctx.getDelegator();

			String topicId = (String) questions.get("topicId");
			String questionDetail = (String) questions.get("questionDetail");

			String answer = (String) questions.get("answer");
			String optionA = (String) questions.get("optionA");
			String optionB = (String) questions.get("optionB");
			String optionC = (String) questions.get("optionC");
			String optionD = (String) questions.get("optionD");
			String questionTypeId = (String) questions.get("questionTypeId");

			// questions.put("negativeMarkValue", 0.0);

			if (topicId == null || questionDetail == null || answer == null) {
				return ServiceUtil.returnError("topic Id and quetionDetail and answer are required");
			}

			String type=questionTypeId.trim();
			if ("SINGLE_CHOICE".equals(type) ||"MULTI_CHOICE".equals(type)) {
				if (optionA.isEmpty() || optionB.isEmpty() || optionC.isEmpty() || optionD.isEmpty()) {
					return ServiceUtil.returnError("options can not be empty for SINGLE_CHOICE and Multi Choice Question");
				}

			} 

			// check topic exists
			GenericValue topic = EntityQuery.use(delegator).from("topicMaster").where("topicId", topicId).queryOne();

			if (topic == null) {
				return ServiceUtil.returnError("Topic not Found");
			}

			
			//creating the next sequecence id
			String questionId = delegator.getNextSeqId("questionMaster");

			questions.put("questionId", questionId);
			// GenericValue question =delegator.makeValue("questionMaster");
			// question.set("questionId", questionId);
			// question.set("questionDetail", questionDetail);
			// question.set("optionA", questions.getOrDefault("optionA", ""));
			// question.set("optionB", questions.getOrDefault("optionB", ""));
			// question.set("optionC", questions.getOrDefault("optionC", ""));
			// question.set("optionD", questions.getOrDefault("optionD", ""));
			// question.set("optionE", questions.getOrDefault("optionE", ""));
			// question.set("answer", answer);
			// question.set("numAnswers", questions.getOrDefault("numAnswers", 1L));
			// question.set("questionTypeId", questionTypeId);
			// question.set("difficultyLevel", questions.getOrDefault("difficultyLevel", 1L));
			// question.set("answerValue", questions.getOrDefault("answerValue", 1.0));
			// question.set("topicId", topicId);
			// question.set("negativeMarkValue",questions.getOrDefault("negativeMarkValue", 0.0));
			// delegator.create(question);

			Map<String, Object> serviceResult = dispatcher.runSync("createQuestion", questions);

			Map<String, Object> result = ServiceUtil.returnSuccess("Question created Successfully");

			if (ServiceUtil.isError(serviceResult)) {
				result.put("message", "Failed to create questions");
				result.put("errorMessage", "ERROR");
				return result;
			}

			result.put("message", "Question created Successfully");
			result.put("questionId", questionId);
			return result;
		} catch (GenericEntityException | GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Failed to create Question");
		}
	}

	// Update Questions
	public static Map<String, Object> updateQuestion(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();

		try {
			// get questionId from context (sent by frontend)
			Long questionId = (Long) context.get("questionId");
			
			if (questionId == null) {
				return ServiceUtil.returnError("questionId is required for update");
			}

			// check question exists in DB
			GenericValue question = EntityQuery.use(delegator).from("questionMaster").where("questionId", questionId).queryOne();

			if (question == null) {
				return ServiceUtil.returnError("Question not found for questionId: " + questionId);
			}

			// getting all fields from context
			String questionDetail = (String) context.get("questionDetail");
			String optionA = (String) context.get("optionA");
			String optionB = (String) context.get("optionB");
			String optionC = (String) context.get("optionC");
			String optionD = (String) context.get("optionD");
			String answer = (String) context.get("answer");
			Long numAnswers = (Long) context.get("numAnswers");
			String questionTypeId = (String) context.get("questionTypeId");
			Long difficultyLevel = (Long) context.get("difficultyLevel");
			BigDecimal answerValue = (BigDecimal) context.get("answerValue");
			String topicId = (String) context.get("topicId");
			BigDecimal negativeMarkValue = (BigDecimal) context.get("negativeMarkValue");

			if(questionTypeId == null) {
				return ServiceUtil.returnError("QuestionTypeId is null");
			}else {
				if (questionTypeId.trim().equals("SINGLE_CHOICE") || questionTypeId.trim().equals("MULTI_CHOICE")) {
					if (optionA == null || optionA.isEmpty()  ||optionB == null || optionB.isEmpty() ||optionC == null || optionC.isEmpty() ||optionD == null || optionD.isEmpty()) {
						return ServiceUtil.returnError("options can not be empty for SINGLE_CHOICE and Multi Choice Question");
					}
					
				}else if (questionTypeId.trim().equals("TRUE_FALSE")) {
					if ( optionA == null || optionB == null||optionA.isEmpty() || optionB.isEmpty() ) {
						return ServiceUtil.returnError("options can not be empty for True_FALSE");
					}
				}
			}
			

			Map<String, Object> updateQuestion = new HashMap<>();

			updateQuestion.put("questionId", questionId);
			updateQuestion.put("questionTypeId", questionTypeId);
			updateQuestion.put("questionDetail", questionDetail);
			updateQuestion.put("optionA", optionA);
			updateQuestion.put("optionB", optionB);
			updateQuestion.put("optionC", optionC);
			updateQuestion.put("optionD", optionD);
			updateQuestion.put("answer", answer);
			updateQuestion.put("numAnswers", numAnswers);
			updateQuestion.put("difficultyLevel", difficultyLevel);
			updateQuestion.put("answerValue", answerValue);
			updateQuestion.put("topicId", topicId);
			updateQuestion.put("negativeMarkValue", negativeMarkValue);

			if (topicId != null) {
				GenericValue topic = EntityQuery.use(delegator).from("topicMaster").where("topicId", topicId).queryOne();

				if (topic == null) {
					return ServiceUtil.returnError("Topic not found for topicId: " + topicId);
				}

			}

			if (questionTypeId != null) {
				GenericValue questionType = EntityQuery.use(delegator).from("Enumeration")
								.where("enumId", questionTypeId, "enumTypeId", "QUESTION_TYPE").queryOne();

				if (questionType == null) {
					return ServiceUtil.returnError("Invalid questionTypeId: " + questionTypeId);
				}
				question.set("questionTypeId", questionTypeId);
			}

			// updating the question in entity-auto
			Map<String, Object> result = dispatcher.runSync("updateQuestion", updateQuestion);

			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError((String) result.get("errorMessage"));
			}
			return ServiceUtil.returnSuccess("Question updated successfully");

		} catch (GenericEntityException | GenericServiceException e) {
			return ServiceUtil.returnError("Error updating question: " + e.getMessage());
		}
	}

	// DeleteQuestionservice
	public static Map<String, Object> deleteQuestion(DispatchContext dctx, Map<String, Object> context) {
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Delegator delegator = dctx.getDelegator();
		
		//getting the list of questionIds
		List<Long> questionIds = (List<Long>) context.get("questionIds");
		
		if (questionIds == null || questionIds.isEmpty()) {
			return ServiceUtil.returnError("questionIds list is required");
		}
		
		List<Long>failedIds = new ArrayList<>();
		Map<Long, String> errors = new HashMap<>();
		int deletedCount = 0;
		try {
			TransactionUtil.begin();
			 
			
			for (Long questionId : questionIds) {
	            if (questionId == null) {
	                continue; 
	            }
	        
	            
	            
	    try {   
			GenericValue question = EntityQuery.use(delegator).
										from("questionMaster").
										where("questionId", questionId).queryOne();

			if (question == null) {
				 failedIds.add(questionId);
				 errors.put(questionId, "Question not found for questionId: " + questionId);
				 continue;
			}

			Map<String, Object> serviceResult = dispatcher.runSync("deleteQuestion", UtilMisc.toMap("questionId", questionId));
			
			if(ServiceUtil.isSuccess(serviceResult)) {	
				deletedCount++;
			}else {
				failedIds.add(questionId);
				errors.put(questionId, "Delete failed: " + ServiceUtil.getErrorMessage(serviceResult));
			}
					
		} catch (GenericEntityException | GenericServiceException e) {
			failedIds.add(questionId);
			errors.put(questionId, "Delete failed: " + e.getMessage());
			}	    
	}
			
			
			//checking All deleted question if no question deleted rollback
		    if (deletedCount == 0 && !failedIds.isEmpty()) {
	            TransactionUtil.rollback();
	            Map<String, Object> errorResult = ServiceUtil.returnError("All deletes failed — transaction rolled back");
	            errorResult.put("deletedCount", 0);
	            errorResult.put("failedIds", failedIds);
	            errorResult.put("errors", errors);
	            return errorResult;
	        }
		    
		    //partially deleted or fully deleted
		    TransactionUtil.commit();
		    Map<String, Object> result = ServiceUtil.returnSuccess(deletedCount + " question(s) deleted successfully");
		            result.put("deletedCount", deletedCount);
		            result.put("failedIds", failedIds);
		            result.put("errors", errors);
		            return result;
		     
		}catch(GenericTransactionException e) {
			e.printStackTrace();
			  try { TransactionUtil.rollback(); } catch (Exception ignore) {}
			  return ServiceUtil.returnError("Transaction error during bulk delete: " + e.getMessage());
		}
	}
	
	
	
	public Map<String,Object>getAllQuestion(DispatchContext dctx,Map<String,Object>context){
		Delegator delegator = dctx.getDelegator();
		Map<String,Object>result=ServiceUtil.returnSuccess("");
		try {
			Integer pageNo = (Integer) context.get("pageNo");
			Integer pageSize = (Integer) context.get("pageSize");
			
			if (pageNo == null || pageNo < 1) {
				pageNo = 1;
			}
			if (pageSize == null || pageSize < 1) {
				pageSize = 10;
			}
			
			
			long totalCount=EntityQuery.use(delegator).from("questionMaster").queryCount();
			
			
			int totalPages = (int) Math.ceil((double) totalCount / pageSize);
			int offset = (pageNo - 1) * pageSize;
			
			if(totalCount==0) {
				return ServiceUtil.returnError("No Question Availbale");
			}
			
			List<GenericValue> questions = EntityQuery.use(delegator).from("questionMaster").orderBy("questionId")
							.cursorScrollInsensitive().offset(offset)
						    .limit(pageSize).queryList();

			
			List<Map<String,Object>>questionList=new ArrayList<Map<String,Object>>();
			
			for(GenericValue q:questions) {
				Map<String, Object> qMap = new HashMap<>();

				//getting topic Name
				GenericValue topicName=EntityQuery.use(delegator).from("topicMaster").where("topicId",q.getString("topicId")).queryOne();
				
				qMap.put("topicName", topicName.getString("topicName"));
				qMap.put("questionId", q.getLong("questionId"));
				qMap.put("questionDetail", q.getString("questionDetail"));
				qMap.put("optionA", q.getString("optionA"));
				qMap.put("optionB", q.getString("optionB"));
				qMap.put("optionC", q.getString("optionC"));
				qMap.put("optionD", q.getString("optionD"));
				qMap.put("numAnswers", q.getLong("numAnswers"));
				qMap.put("questionTypeId", q.getString("questionTypeId"));
				qMap.put("difficultyLevel", q.getString("difficultyLevel"));
				qMap.put("topicId", q.getString("topicId"));
				qMap.put("negativeMarkValue", q.getBigDecimal("negativeMarkValue"));
				qMap.put("answer", q.getString("answer"));
				
				questionList.add(qMap);
			}
			result.put("totalCount", totalCount);
			result.put("questionList", questionList);

			result.put("pageNo", pageNo);
			result.put("pageSize", pageSize);
			result.put("totalPages", totalPages);
			result.put("hasNext", pageNo < totalPages);
			result.put("hasPrevious", pageNo > 1);
			
			return result;
		}catch(Exception e) {
			e.printStackTrace();
			result.put("error", e.getMessage());
			return result;
		}
	}
	
	//getQuestionByTopic service
	public static Map<String, Object> getQuestionsByTopic(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();

		try {

			String topicId = (String) context.get("topicId");
			Integer pageNo = (Integer) context.get("pageNo");
			Integer pageSize = (Integer) context.get("pageSize");

			if (topicId == null || topicId.trim().isEmpty()) {
				return ServiceUtil.returnError("topicId is Required");
			}

			if (pageNo == null || pageNo < 1) {
				pageNo = 1;
			}
			if (pageSize == null || pageSize < 1) {
				pageSize = 10;
			}

			GenericValue topic = EntityQuery.use(delegator).from("topicMaster").where("topicId", topicId).queryOne();

			if (topic == null) {
				return ServiceUtil.returnError("Topic not Found");
			}

			long totalCount = EntityQuery.use(delegator).from("questionMaster").where("topicId", topicId).queryCount();

			int totalPages = (int) Math.ceil((double) totalCount / pageSize);
			int offset = (pageNo - 1) * pageSize;

			List<GenericValue> questions = EntityQuery.use(delegator).from("questionMaster").where("topicId", topicId).orderBy("questionId")
							.cursorScrollInsensitive()
						    .maxRows(offset + pageSize)
						    .queryList();

			if (offset > questions.size()) {
				questions = new ArrayList<>();
			} else {
				questions = questions.subList(offset, Math.min(offset + pageSize, questions.size()));
			}

			List<Map<String, Object>> questionList = new ArrayList<>();

			for (GenericValue q : questions) {
				Map<String, Object> qMap = new HashMap<>();

				qMap.put("questionId", q.getLong("questionId"));
				qMap.put("questionDetail", q.getString("questionDetail"));
				qMap.put("optionA", q.getString("optionA"));
				qMap.put("optionB", q.getString("optionB"));
				qMap.put("optionC", q.getString("optionC"));
				qMap.put("optionD", q.getString("optionD"));
				qMap.put("numAnswers", q.getLong("numAnswers"));
				qMap.put("questionTypeId", q.getString("questionTypeId"));
				qMap.put("difficultyLevel", q.getString("difficultyLevel"));
				qMap.put("topicId", q.getString("topicId"));
				qMap.put("negativeMarkValue", q.getBigDecimal("negativeMarkValue"));
				qMap.put("answer", q.getString("answer"));

				questionList.add(qMap);
			}

			Map<String, Object> result = ServiceUtil.returnSuccess();

			result.put("topicId", topic.getString("topicId"));
			result.put("topicName", topic.getString("topicName"));
			result.put("totalCount", totalCount);
			result.put("questionList", questionList);

			result.put("pageNo", pageNo);
			result.put("pageSize", pageSize);
			result.put("totalPages", totalPages);
			result.put("hasNext", pageNo < totalPages);
			result.put("hasPrevious", pageNo > 1);

			return result;
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError("Error fetching questions By topic: " + e.getMessage());
		}
	}

	public Map<String, Object> getQuestionById(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();

		try {

			Long questionId = Long.valueOf(String.valueOf(context.get("questionId")));

			if (questionId == null) {
				return ServiceUtil.returnError("QuestionId is required");
			}

			GenericValue question = EntityQuery.use(delegator).from("questionMaster").where("questionId", questionId).queryOne();

			if (question == null) {
				return ServiceUtil.returnError("question Not Found");
			}

			Map<String, Object> input = new HashMap<>();

			String answerValueStr = question.getString("answerValue");
			int answerValue = (int) Double.parseDouble(answerValueStr);

			input.put("questionId", (question.getLong("questionId")));
			input.put("questionDetail", (question.getString("questionDetail")));
			input.put("optionA", (question.getString("optionA")));
			input.put("optionB", (question.getString("optionB")));
			input.put("optionC", (question.getString("optionC")));
			input.put("optionD", (question.getString("optionD")));
			input.put("answer", question.getString("answer"));
			input.put("numAnswers", (question.getLong("numAnswers")));
			input.put("questionTypeId", (question.get("questionTypeId")));
			input.put("difficultyLevel", (question.get("difficultyLevel")));
			input.put("answerValue", answerValue);
			input.put("topicId", (question.getString("topicId")));
			input.put("negativeMarkValue", (question.getBigDecimal("negativeMarkValue")));

			Map<String, Object> result = new HashMap<>();
			result.put("question", input);
			return result;

		} catch (Exception e) {

			return ServiceUtil.returnError("Failed to Fetch Question");
		}

	}

	public Map<String, ? extends Object> getTemplateDocument(DispatchContext dctx, Map<String, ? extends Object> context) {

		try {

			Map<String, Object> result = ServiceUtil.returnSuccess();

			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Questions");

			Row header = sheet.createRow(0);

			List<ColumnConfig> columns = QuestionColumnConfigUtil.getColumnConfigs();

			for (ColumnConfig col : columns) {
				Cell cell = header.createCell(col.index);
				String imp = col.required ? "*" : "";
				cell.setCellValue(col.label + " " + imp);
			}

			// Style the header
			CellStyle headerStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setBold(true);
			headerStyle.setFont(font);
			headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			for (Cell cell : header)
				cell.setCellStyle(headerStyle);

			ByteArrayOutputStream out = new ByteArrayOutputStream();

			workbook.write(out);

			byte[] bytes = out.toByteArray();

			out.close();

			workbook.close();

			result.put("bytes", bytes);

			return result;

		} catch (IOException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}

	}

	public Map<String, ? extends Object> uploadBulkQuestion(DispatchContext dctx, Map<String, ? extends Object> context) {

		// process the excel file

		Delegator delegator=dctx.getDelegator();
		try {

			ByteBuffer buffer = (ByteBuffer) context.get("file");

			byte[] bytes = new byte[buffer.remaining()];

			buffer.get(bytes);

			InputStream is = new ByteArrayInputStream(bytes);

			// InputStream file = (InputStream) context.get("file");

			Map<String, Object> result = ServiceUtil.returnSuccess();

			Workbook workbook = WorkbookFactory.create(is);
			Sheet sheet = workbook.getSheetAt(0);

			// list of questions map
			List<Map<String, Object>> questions = new ArrayList<>();

			int totalRows = sheet.getLastRowNum();

			// first row considered as Header
			if (totalRows <= 1) {
				return ServiceUtil.returnError("Please fill the details and upload the file");
			}

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {

				Row row = sheet.getRow(i);

				if (row == null)
					continue;

				Map<String, Object> question = new HashMap<>();
				List<ColumnConfig> columns = QuestionColumnConfigUtil.getColumnConfigs();

				for (ColumnConfig col : columns) {
					Cell cell = row.getCell(col.index);

					if (col.required && (cell == null || cell.getCellType() == CellType.BLANK)) {
						return ServiceUtil.returnError("Row " + i + ", Column " + col.index + " " + col.label + " is required");
					}

					if (cell == null) {
						question.put(col.field, null);
						continue;
					}

					switch (cell.getCellType()) {

					case NUMERIC:
						double numVal = cell.getNumericCellValue();

						switch (col.field) {

						case "numAnswers":
						case "difficultyLevel":
							question.put(col.field, (long) numVal); // Long
							break;

						case "answerValue":
						case "negativeMarkValue":
							question.put(col.field, BigDecimal.valueOf(numVal)); // BigDecimal
							break;

						default:

							question.put(col.field, String.valueOf((long) numVal)); // String
							break;
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
				question.put("negativeMarkValue", 0.0);
				questions.add(question);
			}

			// Transaction BEGIN
			TransactionUtil.begin();

			for (Map<String, ? extends Object> question : questions) {
				
				
//				GenericEntity entity = (GenericEntity) question;
//				String topicId = entity.getString("topicId");
//				
//				
//				if(topicId==null) {
//					continue;
//				}
				

				Map<String, Object> serviceResult = dctx.getDispatcher().runSync("createQuestionService", question);

				if (serviceResult.get("responseMessage") != null && serviceResult.get("responseMessage").equals("error")) {
					Map<String, Object> errorResult = ServiceUtil.returnError((String) serviceResult.get("errorMessage"));

					// Transaction ROLL BACK

					TransactionUtil.rollback(); // Here we rolled back, because the service returns error;
					return errorResult;

				}
			}

			// Transaction COMMIT
			TransactionUtil.commit();

			result.put("successMessage", "Questions uploaded successfully");

			return result;

		} catch (EncryptedDocumentException | IOException | GenericServiceException | GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Unexpected error occured , try again after sometime!");
		}

	}

	
}
