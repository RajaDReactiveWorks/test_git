package com.getusroi.policy.config.exp.regex;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.policy.config.PolicyEvaluationConfigurationUnit;
import com.getusroi.policy.config.exp.sqltomvel.PolicyCompiledExpression;
import com.getusroi.policy.jaxb.Eval;
import com.getusroi.policy.jaxb.Evaluation;
import com.getusroi.policy.jaxb.Expression;


public class PolicyREGEXExpressionBuilder {

	final Logger logger = LoggerFactory.getLogger(PolicyREGEXExpressionBuilder.class);

	private static final String[] searchList = { " or ", " OR ", " and ",	" AND " };
	private static final String[] replacementList = { " || ", " || ", " && ",	" && " };

	/**
	 * evaluting REGEX expression
	 * 
	 * @param eval
	 * @return PolicyEvaluationConfigurationUnit
	 */
	public PolicyEvaluationConfigurationUnit buildEvaluation(Evaluation eval) {
		PolicyEvaluationConfigurationUnit policyEvalUnit = new PolicyEvaluationConfigurationUnit();
		policyEvalUnit.setDialect(eval.getEvalDialect().value());
		policyEvalUnit.setSalience(eval.getSalience());

		logger.debug("Dialect=" + eval.getEvalDialect().value() + ", Salience="+ eval.getSalience());
		Map<String, String> expMap = new LinkedHashMap();
		List<Expression> expressionList = eval.getExpression();
		// Convert all expression from SQL dialect to MVEL
		for (Expression exp : expressionList) {
			String expName = exp.getName().trim();

			PolicyCompiledExpression polExp = new PolicyCompiledExpression();
			findRequestVariable(exp.getExpValue(), polExp);
			findPermaStoreVariable(exp.getExpValue(), polExp);
			String expValue=exp.getExpValue();
			expValue=replaceWithPsVarible(expValue, polExp);
			expValue=findRegexANdRepalce(expValue);


			polExp.setExpression(expValue);
			String regexExpValue = polExp.getExpression();
			expMap.put(expName, regexExpValue);
			policyEvalUnit.addPSVar(polExp.getPsVarList());
			policyEvalUnit.addReqVar(polExp.getReqVarList());
		}
		// Handle evaluate Expresiion

		String evalExpression = eval.getEvaluateExp();
		String regexExp = evaluateExpression(evalExpression, expMap);

		logger.debug("regex  final Expressionb  " + regexExp);

		policyEvalUnit.setExpression(regexExp);

		return policyEvalUnit;
	}

	/**
	 * To find request Vairable
	 * 
	 * @param expValue
	 * @param polExp
	 */
	public void findRequestVariable(String expValue,
			PolicyCompiledExpression polExp) {
		logger.debug("inside findRequestVariable method  ");

		String regExPatternStr = "\\$\\w+";
		Pattern r = Pattern.compile(regExPatternStr);
		Matcher m = r.matcher(expValue);
		
		//find one request Varibale 
		while(m.find()) {
			String varName = m.group(0);
			varName = varName.substring(1, varName.length());
			polExp.addReqVar(varName);
			logger.debug("Request variable " + varName);

		}

	}

	

	
	
	/**
	 * to find permastore Varibale from the given Expression
	 * @param expValue
	 * @param polExp
	 * @return 
	 */
	public String findPermaStoreVariable(String expValue,
			PolicyCompiledExpression polExp) {


		logger.debug("inside findPermaStoreVariable method  ");

		String regExPatternStr = "::PS\\(([^)]+)\\)";
		Pattern r = Pattern.compile(regExPatternStr);
		Matcher m = r.matcher(expValue);
		while (m.find()) {
			polExp.addPSVar(m.group(1));

			logger.debug(" PermaStoreVariable " + m.group(1) + "  expressionValue  " + expValue);

		}
		logger.debug(" ----  expressionValue  = " + expValue);

		return expValue;
	}
	
	/**
	 * to find permastore Varibale from the given Expression
	 * @param expValue
	 * @param polExp
	 * @return 
	 */
	public String findRegexANdRepalce(String expValue) {


		logger.debug("inside findRegex method  ");

		String regExPatternStr = "REGEX\\(([^)]+)";
		Pattern r = Pattern.compile(regExPatternStr);
		Matcher m = r.matcher(expValue);
		while (m.find()) {
			

			logger.debug(" REGEX " + m.group(1) + "  expressionValue  " + expValue);
			expValue=	replaceWithRegex(expValue, m.group(1));
		}
		logger.debug(" ----  expressionValue  = " + expValue);

		return expValue;
	}
	
	/**
	 * repalce  given RegexExpression with proper Regex value
	 * @param expValue
	 * @param replacebleRegex
	 * @return
	 */

	private String replaceWithRegex(String expValue,String regex) {

		logger.debug("inside replaceRegex method  ");

		if (regex == null && expValue==null)
			return expValue;

		expValue = StringUtils.replace(expValue, "REGEX("+regex+")", regex);

		return expValue;

	}
	
	private String replaceWithPsVarible(String expValue,PolicyCompiledExpression polExp){
		List<String> psVarList = polExp.getPsVarList();
		if (psVarList == null)
			return expValue;
		for (String psVarName : psVarList) {
			expValue = StringUtils.replace(expValue, "(::PS(" + psVarName + "))", "$" + psVarName);
		}

		logger.debug("replaceRegexVariable ---->" + expValue);
return expValue;
		
	}
/**
 * Add or concat multiple Expression with AND OR condtion if have multiple Expression based on Given EvalEXpression
 * @param evalExpression
 * @param expMap
 * @return 
 */
	private String evaluateExpression(String evalExpression,
			Map<String, String> expMap) {
		Set<String> keySet = expMap.keySet();

		logger.debug("inside evaluateExpression method  with evalExpression   "+ evalExpression + " expMap " + expMap);
		evalExpression = StringUtils.replaceEachRepeatedly(evalExpression,searchList, replacementList);

		logger.debug(" after replaceing  AND  OR oprator expression is "+ evalExpression);
		for (String expName : keySet) {
			String expression = expMap.get(expName);
			logger.debug("mvelExp  value before evaluting Expression "+ expression);
			logger.debug("expName  value before evaluting Expression "+ expName);

			evalExpression = StringUtils.replace(evalExpression, expName, expression );

			logger.debug("after evalatingExpression the expValue is "+ evalExpression);
		}
		return evalExpression;
	}

	public static void main(String[] args) {
		// check empty value $dstare ^\\s*$
		// check range value exmple 10 to 29 $platform ([1-2][0-9])+
		// match word in given string $dstare ($dstare) (::PS(GetStagingAreas))
	// regex() matches $varaible

		
	 Evaluation eval=new Evaluation(); 
	 eval.setSalience(1);
	 eval.setEvalDialect(Eval.valueOf("REGEX")); Expression expression=new
	 Expression(); expression.setName("validStageArea");
	 expression.setExpValue("  REGEX(^\\s*$)  matches  $dstare && REGEX(^\\s*$)  matches  $dstare  && REGEX(^\\s*$)  matches  $dstare  && REGEX($dstare$dgood)  matches  (::PS(GetStagingAreasOne)) || REGEX($dstare$dgood)  matches  (::PS(GetStagingAreasOne)) ");
	 
	 Expression expressionTwo=new Expression();
	 expressionTwo.setName("validPlatformArea");
	 expressionTwo.setExpValue(
	 "  REGEX(([1-2][0-9])+)  matches   $platform  ");
	 
	 Expression expressionThree=new Expression();
	 expressionThree.setName("OprandThree"); expressionThree.setExpValue(
	 "REGEX($dstare$dgood)  matches  (::PS(GetStagingAreasOne)) ");
	 
	 
	 
	 eval.getExpression().add(expression); //
	 //eval.getExpression().add(expressionTwo);
	// eval.getExpression().add(expressionThree);
	 String expValue="REGEX(^\\s*$)  matches  $dstare && REGEX(^\\s*$)  matches  $dstare  && REGEX(^\\s*$)  matches  $dstare  && REGEX($dstare$dgood)  matches  (::PS(GetStagingAreasOne)) || REGEX($dstare$dgood)  matches  (::PS(GetStagingAreasOne)) ";
			 
	 eval.setEvaluateExp(
	 " validStageArea ");
	 
	
	
	}
}
