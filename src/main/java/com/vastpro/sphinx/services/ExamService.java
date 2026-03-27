package com.vastpro.sphinx.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class ExamService {
	
	public static Map<String,Object> createExam(DispatchContext context,Map<String,Object> input){
//		LocalDispatcher dispatcher=context.getDispatcher();
		Delegator delegator=context.getDelegator();
		try {
			
		GenericValue examAlreadyExits= EntityQuery.use(delegator).from("examMaster").where("examName",input.get("examName")).queryFirst();
		if(examAlreadyExits!=null) {
			return ServiceUtil.returnError("Exam Already Exits");
		}
		
		String examId=delegator.getNextSeqId("examMaster");
		GenericValue examMaster = delegator.makeValue("examMaster");
		examId="exam"+examId;
		examMaster.set("examId", examId);
		examMaster.setNonPKFields(input);
		
			delegator.create(examMaster);
			return ServiceUtil.returnSuccess("Exam create successfully");
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Error, occur during creating exam");
		}
	}

}
