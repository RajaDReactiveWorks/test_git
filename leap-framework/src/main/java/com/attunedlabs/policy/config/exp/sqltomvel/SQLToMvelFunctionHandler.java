package com.attunedlabs.policy.config.exp.sqltomvel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.policy.config.exp.sqltomvel.PolicySQLDialectExpressionBuilder.IndividualExpEval;
import com.attunedlabs.policy.config.exp.sqltomvel.PolicySQLDialectExpressionBuilder.IndvidualExpEntry;
/**
 * Converts SQL Dialect Function to MVEL Function.
 * @author amit
 *
 */
public class SQLToMvelFunctionHandler {
	final static Logger logger = LoggerFactory.getLogger(SQLToMvelFunctionHandler.class);
	private static final String EqualsFunc="LV == (RV)";
	private static final String INFunc="RV contains (LV)";
	private static final String NOTEqualFunc="LV != (RV)";
	private static final String LIKEFunc="LV.matches([RV])";
	private static final String ISNULLFunc="LV == nil";
	private static final String ISNOTNULLFunc="LV != nil";

	private static final String NOTINFunc="!(LV contains RV)";
	private static final String GRAETERFunc="LV > RV";
	private static final String GREATEREQFunc="LV >= RV";
	private static final String LESSERTHANFunc="LV < RV";
	private static final String LESSERTHANEQFunc="LV <= RV";



	public static String equalsFuncHandler(IndividualExpEval IndCond) {
		
		IndvidualExpEntry operatorEntry = IndCond.getOperator();
		IndvidualExpEntry leftEntry = IndCond.getLeftEntry();
		IndvidualExpEntry rightEntry = IndCond.getRightEntry();
		StringBuffer expBuffer = new StringBuffer();
		String finalExp=EqualsFunc.replace("RV", rightEntry.value.trim());
		finalExp=finalExp.replace("LV", leftEntry.value.trim());
		//logger.debug("Final = Expression =>"+finalExp);
		return finalExp;
	}

	public static String InFuncHandler(IndividualExpEval IndCond) {
		IndvidualExpEntry operatorEntry = IndCond.getOperator();
		IndvidualExpEntry leftEntry = IndCond.getLeftEntry();
		IndvidualExpEntry rightEntry = IndCond.getRightEntry();
		String finalExp=INFunc.replace("RV", rightEntry.value.trim());
		finalExp=finalExp.replace("LV", leftEntry.value.trim());
		//logger.debug("Final IN Expression =>"+finalExp);
		return finalExp;
	}

	public static String NotEqualFuncHandler(IndividualExpEval IndCond){
		IndvidualExpEntry leftEntry = IndCond.getLeftEntry();
		IndvidualExpEntry rightEntry = IndCond.getRightEntry();
	
		
		String finalExp=NOTEqualFunc.replace("RV", rightEntry.value.trim());
		finalExp=finalExp.replace("LV", leftEntry.value.trim());
		return finalExp;
	}
	
	public static String LikeFuncHandler(IndividualExpEval IndCond){
		IndvidualExpEntry leftEntry = IndCond.getLeftEntry();
		IndvidualExpEntry rightEntry = IndCond.getRightEntry();
	
		
		String finalExp=LIKEFunc.replace("RV", rightEntry.value.trim());
		finalExp=finalExp.replace("LV", leftEntry.value.trim());
		return finalExp;
	}
	

	public static String ISNULLFuncHandler(IndividualExpEval IndCond){
		IndvidualExpEntry leftEntry = IndCond.getLeftEntry();
	
		
		String finalExp=ISNULLFunc.replace("LV", leftEntry.value.trim());
		
		return finalExp;
	}
	
	public static String ISNOTNULLFuncHandler(IndividualExpEval IndCond){
		IndvidualExpEntry leftEntry = IndCond.getLeftEntry();
	
		
		String finalExp=ISNOTNULLFunc.replace("LV", leftEntry.value.trim());
		
		return finalExp;
	}
	
	public static String NOTINFuncFuncHandler(IndividualExpEval IndCond){
		IndvidualExpEntry leftEntry = IndCond.getLeftEntry();
		IndvidualExpEntry rightEntry = IndCond.getRightEntry();
	
		
		String finalExp=NOTINFunc.replace("RV", rightEntry.value.trim());
		finalExp=finalExp.replace("LV", leftEntry.value.trim());
	
		return finalExp;
	}
	
	public static String GRAETERFuncHandler(IndividualExpEval IndCond){
		IndvidualExpEntry leftEntry = IndCond.getLeftEntry();
		IndvidualExpEntry rightEntry = IndCond.getRightEntry();
	
		
		String finalExp= GRAETERFunc.replace("RV", rightEntry.value.trim());
		finalExp=finalExp.replace("LV", leftEntry.value.trim());
	return finalExp;
	}
	public static String GREATEREQFuncHandler(IndividualExpEval IndCond){
		IndvidualExpEntry leftEntry = IndCond.getLeftEntry();
		IndvidualExpEntry rightEntry = IndCond.getRightEntry();
	
		
		String finalExp= GREATEREQFunc.replace("RV", rightEntry.value.trim());
		finalExp=finalExp.replace("LV", leftEntry.value.trim());
	
	
		return finalExp;
	}
	
	public static String LESSERTHANFuncHandler(IndividualExpEval IndCond){
		IndvidualExpEntry leftEntry = IndCond.getLeftEntry();
		IndvidualExpEntry rightEntry = IndCond.getRightEntry();
	
		
		String finalExp= LESSERTHANFunc.replace("RV", rightEntry.value.trim());
		finalExp=finalExp.replace("LV", leftEntry.value.trim());
	
	
		return finalExp;
	}
	public static String LESSERTHANEQFuncHandler(IndividualExpEval IndCond){
		IndvidualExpEntry leftEntry = IndCond.getLeftEntry();
		IndvidualExpEntry rightEntry = IndCond.getRightEntry();
	
		
		String finalExp= LESSERTHANEQFunc.replace("RV", rightEntry.value.trim());
		finalExp=finalExp.replace("LV", leftEntry.value.trim());
	
	
		return finalExp;
	}
	
	public static String LIKEFuncHandler(IndividualExpEval IndCond){
		IndvidualExpEntry leftEntry = IndCond.getLeftEntry();
		IndvidualExpEntry rightEntry = IndCond.getRightEntry();
	
		String reghtEntryValue=replaceSqlLikeValuesWithRegExValue(rightEntry.value.trim());
		String finalExp= LESSERTHANEQFunc.replace("RV",reghtEntryValue);
		finalExp=finalExp.replace("LV", leftEntry.value.trim());
	
	
		return finalExp;
	}
	
	
	private static String replaceSqlLikeValuesWithRegExValue(String sqlValue){
		String replaceValue="";
		String charValue="";
		if(sqlValue!=null){
			for(int i=0;i<sqlValue.length();i++){
				charValue="";
				charValue=charValue+sqlValue.charAt(i);
				if(charValue.equalsIgnoreCase("%")){
					
				}else{
					replaceValue=replaceValue;
				}
			}
		}
		
	return null;
	}
	
	
}
