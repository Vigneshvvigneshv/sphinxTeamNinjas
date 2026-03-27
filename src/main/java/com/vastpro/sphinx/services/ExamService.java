package com.vastpro.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.LocalDispatcher;

public class ExamService {
	
	public static Map<String,Object> createExam(DispatchContext context,Map<String,Object> input){
		LocalDispatcher dispatcher=context.getDispatcher();
		Delegator delegator=context.getDelegator();
		
		String examId=delegator.getNextSeqId("examMaster");
		GenericValue examMaster = delegator.makeValue("examMaster");
		examId="exam"+examId;
		
		
		
	}

}
