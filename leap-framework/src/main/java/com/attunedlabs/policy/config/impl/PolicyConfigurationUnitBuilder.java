package com.attunedlabs.policy.config.impl;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.policy.config.PolicyConfigurationException;
import com.attunedlabs.policy.config.PolicyConfigurationUnit;
import com.attunedlabs.policy.config.PolicyEvaluationConfigurationUnit;
import com.attunedlabs.policy.config.exp.regex.PolicyREGEXExpressionBuilder;
import com.attunedlabs.policy.config.exp.sqltomvel.PolicySQLDialectExpressionBuilder;
import com.attunedlabs.policy.jaxb.Evaluation;
import com.attunedlabs.policy.jaxb.Policy;
import com.attunedlabs.policy.jaxb.PolicyDefinedFact;
import com.attunedlabs.policy.jaxb.PolicyResponse;
import com.attunedlabs.policy.jaxb.PolicyResponseType;

/**
 *  Policy Configuration Builder Helper class. 
 * @author bizruntime
 *
 */
public class PolicyConfigurationUnitBuilder {
	final Logger logger = LoggerFactory.getLogger(PolicyConfigurationUnitBuilder.class);
	
	public void buildPolicyConfigUnit(Policy policy,PolicyConfigurationUnit psConfigUnit) throws PolicyConfigurationException, PolicyFactBuilderException{
		List<Evaluation> evalList=policy.getPolicyEvaluation().getEvaluation();
		for(Evaluation eval:evalList){
			String expDialect=eval.getEvalDialect().value();
			if(expDialect.equalsIgnoreCase("SQL")){
				PolicyEvaluationConfigurationUnit polEvalConfigUnit=buildForSQLDialect(eval);
				psConfigUnit.addEvaluationUnit(polEvalConfigUnit);
				
			}else if(expDialect.equalsIgnoreCase("REGEX")){
				PolicyEvaluationConfigurationUnit polEvalConfigUnit=buildForRegex(eval);
				psConfigUnit.addEvaluationUnit(polEvalConfigUnit);
				
			}	else{
				throw new PolicyConfigurationException("Policy Dialect "+expDialect+" Not Supported");
			}
		}
		//Build Response
		PolicyResponse polResponse=policy.getPolicyResponse();
		String policyResType=polResponse.getType().value();
		logger.debug(".buildPolicyConfigUnit()...policyResType="+policyResType);
		if(polResponse.getType().equals(PolicyResponseType.POLICY_DEFINED_FACT)){
			psConfigUnit.setReponseType(PolicyResponseType.POLICY_DEFINED_FACT.value());
			Serializable responseToCache=buildPolicyResponseForPolDefFact(polResponse.getPolicyDefinedFact());
			psConfigUnit.setConfigData(responseToCache);
		}else if(polResponse.getType().equals(PolicyResponseType.MAPPED_FACT)){
			psConfigUnit.setReponseType(PolicyResponseType.MAPPED_FACT.value());
			String psMappedName=buildPolicyResponseForMappedFact(polResponse.getFactMapping());
			psConfigUnit.setConfigData(psMappedName);
		}else{
			throw new PolicyConfigurationException("PolicyResponse type "+policyResType+" Not Supported");
		}
	}
		
	private PolicyEvaluationConfigurationUnit buildForSQLDialect(Evaluation eval){
		PolicySQLDialectExpressionBuilder policySqlBuilder=new PolicySQLDialectExpressionBuilder();		
		PolicyEvaluationConfigurationUnit polEvalConfigUnit=policySqlBuilder.buildEvaluation(eval);
		String mvelExp=polEvalConfigUnit.getExpression();
		logger.debug(".buildForSQLDialect() mvelExp is="+mvelExp);
		return polEvalConfigUnit;
	}

	
	private PolicyEvaluationConfigurationUnit buildForRegex(Evaluation eval){
		
		PolicyREGEXExpressionBuilder policyRegexChecker=new PolicyREGEXExpressionBuilder();
		PolicyEvaluationConfigurationUnit pConfigurationUnit=policyRegexChecker.buildEvaluation(eval);
		
		logger.debug(".buildForRegex() RegEx is="+pConfigurationUnit.getExpression());
		return pConfigurationUnit;
	}

	private Serializable buildPolicyResponseForPolDefFact(PolicyDefinedFact polDefinedFact) throws PolicyFactBuilderException{
		PolicyDefinedFactBuilder factBuilder=new PolicyDefinedFactBuilder();
		Serializable factBuilderObject=factBuilder.buildPolicyDefinedFact(polDefinedFact);
		return factBuilderObject;
	}
	
	private String buildPolicyResponseForMappedFact(String mappedFactStr)throws PolicyConfigurationException{
		String regExPatternStr = "::PS\\(([^)]+)\\)";
		String permaStoreVariableName=null;
      Pattern r = Pattern.compile(regExPatternStr);
      Matcher m = r.matcher(mappedFactStr);
	   if(m.find( )) {
	      	permaStoreVariableName=(m.group(1));
	   }else{
	   	throw new PolicyConfigurationException("PolicyResponse- Invalid FactMapping should be of format (::PS(VARIABLE_NAME)) instead of "+mappedFactStr);
	   }
		return permaStoreVariableName;
	}
}
