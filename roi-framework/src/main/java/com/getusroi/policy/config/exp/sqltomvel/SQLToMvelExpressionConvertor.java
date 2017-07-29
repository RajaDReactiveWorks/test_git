package com.getusroi.policy.config.exp.sqltomvel;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.policy.config.exp.sqltomvel.PolicySQLDialectExpressionBuilder.IndividualExpEval;
import com.getusroi.policy.config.exp.sqltomvel.PolicySQLDialectExpressionBuilder.IndividualExpHolder;
import com.getusroi.policy.config.exp.sqltomvel.PolicySQLDialectExpressionBuilder.IndvidualExpEntry;
import com.getusroi.policy.config.exp.sqltomvel.PolicySQLDialectExpressionBuilder.TotalExpressionHolder;

public class SQLToMvelExpressionConvertor {
	final Logger logger = LoggerFactory.getLogger(SQLToMvelExpressionConvertor.class);
	
	public String handleExpConversion(TotalExpressionHolder totalExpHolder){
		logger.debug("inside handleExpConversion method with totalExpHolder  "+totalExpHolder);
		logger.debug("handleExpConversion() begins"); 
		List<IndividualExpHolder> indidualExpList=totalExpHolder.getIndExpHolderList();
		StringBuffer expression=new StringBuffer();
		for(IndividualExpHolder indExpHolder:indidualExpList){
			int type =indExpHolder.getType();
			//Its an Expression
			if(type==1){
				String exp=convertIndividualConditionInExp(indExpHolder);
				
				logger.debug("Its an Expression  "+exp);

				expression.append(exp);
			}if(type==2){	//Its an Operator
				String operator=convertExpOperatorInExp(indExpHolder);
				
				logger.debug("Its an  Operator "+operator);

				expression.append(operator);
			}
		}
		return expression.toString();
	}
	public String convertExpOperatorInExp(IndividualExpHolder indExpHolder){
		//logger.debug("Operator indExpHolder="+indExpHolder.getValue());
		String finalOperator=" "+indExpHolder.getValue()+" ";
		return finalOperator;		
	}
	
	public String convertIndividualConditionInExp(IndividualExpHolder indExpHolder){
		
		logger.debug("insde convertIndividualConditionInExp  method ");
		logger.debug("Expression indExpHolder="+indExpHolder.getValue());
		List<IndividualExpEval> indiConditionList=indExpHolder.getIndExpEvalList();
		logger.debug("Expression indExpHolder BEFORE fOR ITEM ARE "+indiConditionList.size());
		StringBuffer strbuffer=new StringBuffer();
		for(IndividualExpEval indiCondition:indiConditionList){
			String individualFunction=sqlFunctionMapper(indiCondition);
			strbuffer.append(individualFunction);
			logger.debug("Expression indExpHolder sql="+individualFunction);
		}
		logger.debug("Expression indExpHolder OUT OF fOR");
		return strbuffer.toString();
	}
	public String sqlFunctionMapper(IndividualExpEval IndCond){
		
		logger.debug("insde sqlFunctionMapper  method ");

		IndvidualExpEntry operatorEntry=IndCond.getOperator();
		String convertedMvlExp=null;
		String operator=operatorEntry.value;
		logger.debug("Operator is="+operator);
		if(operatorEntry.value.equalsIgnoreCase("=")){
			convertedMvlExp=SQLToMvelFunctionHandler.equalsFuncHandler(IndCond);
			
			logger.debug("convertedMvlExp value   ------- "+convertedMvlExp);

		}else if(operatorEntry.value.equalsIgnoreCase("IN")){
			convertedMvlExp=SQLToMvelFunctionHandler.InFuncHandler(IndCond);
			
			logger.debug("convertedMvlExp value   ------- "+convertedMvlExp);

		}else if(operatorEntry.value.equalsIgnoreCase("NOT IN")){
			convertedMvlExp=SQLToMvelFunctionHandler.NOTINFuncFuncHandler(IndCond);
			logger.debug("convertedMvlExp value   ------- "+convertedMvlExp);

		}
		else if(operatorEntry.value.equalsIgnoreCase("< >")){
			convertedMvlExp=SQLToMvelFunctionHandler.NotEqualFuncHandler(IndCond);
			logger.debug("convertedMvlExp value   ------- "+convertedMvlExp);

		}
		
	
		
		else if(operatorEntry.value.equalsIgnoreCase("IS NOT NULL")){
			convertedMvlExp=SQLToMvelFunctionHandler.ISNOTNULLFuncHandler(IndCond);
			logger.debug("convertedMvlExp value   ------- "+convertedMvlExp);

		}
		//Todo 
		else if(operatorEntry.value.equalsIgnoreCase("BETWEEN")){

		} 
		else if(operatorEntry.value.equalsIgnoreCase("IS NULL")){
			convertedMvlExp=SQLToMvelFunctionHandler.ISNULLFuncHandler(IndCond);
			logger.debug("convertedMvlExp value   ------- "+convertedMvlExp);

		} 
		else if(operatorEntry.value.equalsIgnoreCase("!=")){
			convertedMvlExp=SQLToMvelFunctionHandler.NotEqualFuncHandler(IndCond);
			logger.debug("convertedMvlExp value   ------- "+convertedMvlExp);

		} else if(operatorEntry.value.equalsIgnoreCase(">")){
			convertedMvlExp=SQLToMvelFunctionHandler.GRAETERFuncHandler(IndCond);
			logger.debug("convertedMvlExp value   ------- "+convertedMvlExp);

		}
		else if(operatorEntry.value.equalsIgnoreCase(">=")){
			convertedMvlExp=SQLToMvelFunctionHandler.GREATEREQFuncHandler(IndCond);
			logger.debug("convertedMvlExp value   ------- "+convertedMvlExp);

		}else if(operatorEntry.value.equalsIgnoreCase("<")){
			convertedMvlExp=SQLToMvelFunctionHandler.LESSERTHANFuncHandler(IndCond);
			logger.debug("convertedMvlExp value   ------- "+convertedMvlExp);

		}else if(operatorEntry.value.equalsIgnoreCase("<=")){
			convertedMvlExp=SQLToMvelFunctionHandler.LESSERTHANEQFuncHandler(IndCond);
			logger.debug("convertedMvlExp value   ------- "+convertedMvlExp);

		}
		
		else if(operatorEntry.value.equalsIgnoreCase("LIKE")){
			convertedMvlExp=SQLToMvelFunctionHandler.LESSERTHANEQFuncHandler(IndCond);
			logger.debug("convertedMvlExp value   ------- "+convertedMvlExp);

		}
		return convertedMvlExp;
	}

}
 