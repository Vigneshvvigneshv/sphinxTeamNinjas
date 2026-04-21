package com.vastpro.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

public class ExamQuestionService {
	
public static Map<String,Object> getAllExamQuestion(DispatchContext dctx, Map<String,Object> context) {
		
		Delegator delegator=dctx.getDelegator();
		Map<String,Object>result=ServiceUtil.returnSuccess("");
		try {
			String examId=(String)context.get("examId");
			Integer pageNo = (Integer) context.get("pageNo");
			Integer pageSize = (Integer) context.get("pageSize");
			
			if (pageNo == null || pageNo < 1) {
				pageNo = 1;
			}
			if (pageSize == null || pageSize < 1) {
				pageSize = 1;
			}
			
			long totalCount=EntityQuery.use(delegator).from("QuestionBankMasterB").where("examId",examId).queryCount();
			
			int totalPages = (int) Math.ceil((double) totalCount / pageSize);
			int offset = (pageNo - 1) * pageSize;
			
			if(totalCount==0) {
				return ServiceUtil.returnError("No Question Availbale");
			}
			
			
			GenericValue question = EntityQuery.use(delegator).from("QuestionBankMasterB").where("examId",examId).orderBy("-lastUpdatedStamp")
							.cursorScrollInsensitive().offset(offset)
						    .limit(pageSize).queryOne();
			
//			List<Map<String,Object>>questionList=new ArrayList<Map<String,Object>>();
			
			GenericValue exam=EntityQuery.use(delegator).from("ExamMaster").where("examId",examId).queryOne();
			
			result.put("totalCount", totalCount);
			result.put("question", question);
			result.put("pageNo", pageNo);
			result.put("pageSize", pageSize);
			result.put("totalPages", totalPages);
			result.put("hasNext", pageNo < totalPages);
			result.put("hasPrevious", pageNo > 1);
			result.put("totalCount", totalCount);		
			result.put("duration",exam.getLong("duration"));
			return result;
			
		}catch(Exception e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Failed to get Question");
		}
		
	}
}
