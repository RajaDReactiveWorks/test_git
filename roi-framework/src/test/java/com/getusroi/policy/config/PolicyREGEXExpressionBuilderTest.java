package com.getusroi.policy.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.config.util.GenericTestConstant;
import com.getusroi.policy.PolicyTestConstant;
import com.getusroi.policy.config.exp.regex.PolicyREGEXExpressionBuilder;
import com.getusroi.policy.jaxb.Evaluation;
import com.getusroi.policy.jaxb.Policies;
import com.getusroi.policy.jaxb.Policy;

public class PolicyREGEXExpressionBuilderTest {

	final Logger logger = LoggerFactory.getLogger(PolicyREGEXExpressionBuilderTest.class);
	private List<Policy> policyConfigList;
	private PolicyREGEXExpressionBuilder policyRegexChecker;

	private Policies getPolicyConfiguration()
			throws PolicyConfigXMLParserException {
		PolicyConfigXMLParser parser = new PolicyConfigXMLParser();
		InputStream inputstream = PolicyConfigXMLParser.class.getClassLoader().getResourceAsStream(PolicyTestConstant.policyconfigfileToParse);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line=null;
		try {
			while ((line = reader.readLine()) != null) {
			    out1.append(line);
			    
			}
			reader.close();
		} catch (IOException e) {
		throw new PolicyConfigXMLParserException("policy file doesnot exist in classpath",e);
		}
		
		String policeConfigxml=out1.toString();
		Policies policies = parser.marshallXMLtoObject(policeConfigxml);

		return policies;
	}

	@Before
	public void loadConfigurations() throws PolicyConfigurationException,
			ConfigPersistenceException, PolicyConfigXMLParserException {
		Policies policyConfig = getPolicyConfiguration();
		this.policyConfigList = policyConfig.getPolicy();
		this.policyRegexChecker = new PolicyREGEXExpressionBuilder();
	}

	/**
	 * test for check empty for giveString using given Regex
	 * 
	 * */
	@Test
	public void testREgexEmptyValue() {
		String regex = null;
		Policy policy = getPolicyByName("PolicyDefinedFactAttDateTestFive");
		List<Evaluation> evalList = policy.getPolicyEvaluation().getEvaluation();
		for (Evaluation eval : evalList) {
			if (eval.getEvalDialect().value().equalsIgnoreCase("REGEX")) {
				PolicyEvaluationConfigurationUnit polEvalConfigUnit = this.policyRegexChecker.buildEvaluation(eval);
				regex = polEvalConfigUnit.getExpression();
				logger.debug("Final PolicyEvaluationConfigurationUnit is="+ polEvalConfigUnit);
				logger.debug("Final testREgexEmptyValue is="+ polEvalConfigUnit.getExpression());

			}
		}

		if (regex != null) {
			String givenREgexExpression = "";
			String givenValue = "";
			// trim to replace open and closed Pranathisis
			regex = regex.trim();

			regex = regex.trim();

			// split given Expression

			String[] regexArray = regex.split("matches");
			Assert.assertNotNull("regexArray is should NOT be Null", regexArray);

			// regexArray legnth should be greater than 1
			Assert.assertTrue(regexArray.length > 1);

			givenREgexExpression = regexArray[0].trim();

			givenValue = regexArray[1].replace("$dstare", "");

			logger.debug("given regex  " + givenREgexExpression);
			logger.debug("given input   " + givenValue);

			boolean isMatched = Pattern.matches(givenREgexExpression,givenValue);
			Assert.assertTrue(isMatched);

		} else {

			Assert.fail("genrated REGEX Expressiion is Null for given Policy name = "
					+ policy.getPolicyName());
		}

	}

	/**
	 * test to check given input word matches in given string using Regex
	 * 
	 * */
	@Test
	public void testREgexMathcWordValue() {
		String regex = null;
		Policy policy = getPolicyByName("PolicyDefinedFactAttDateTestSeven");
		List<Evaluation> evalList = policy.getPolicyEvaluation().getEvaluation();
		for (Evaluation eval : evalList) {
			if (eval.getEvalDialect().value().equalsIgnoreCase("REGEX")) {
				PolicyEvaluationConfigurationUnit polEvalConfigUnit = this.policyRegexChecker.buildEvaluation(eval);
				regex = polEvalConfigUnit.getExpression();
				logger.debug("Final PolicyEvaluationConfigurationUnit is="
						+ polEvalConfigUnit);
				logger.debug("Final testREgexMathcWordValue is="
						+ polEvalConfigUnit.getExpression());

			}
		}

		if (regex != null) {
			boolean isMatched = checkRegexForWordmatching(regex);
			Assert.assertTrue(isMatched);

		} else {
			Assert.fail("genrated REGEX Expressiion is Null for given Policy name = "	+ policy.getPolicyName());
		}

	}

	/**
	 * test to check given input word matches in given string and check given
	 * number is in specfied Range using Regex
	 * 
	 * */
	@Test
	public void testREgexMathcWordAndRangeValue() {
		String regex = null;
		Policy policy = getPolicyByName("PolicyDefinedFactAttDateTestEight");
		List<Evaluation> evalList = policy.getPolicyEvaluation().getEvaluation();
		for (Evaluation eval : evalList) {
			if (eval.getEvalDialect().value().equalsIgnoreCase("REGEX")) {
				PolicyEvaluationConfigurationUnit polEvalConfigUnit = this.policyRegexChecker.buildEvaluation(eval);
				regex = polEvalConfigUnit.getExpression();
				logger.debug("Final PolicyEvaluationConfigurationUnit is="+ polEvalConfigUnit);
				logger.debug("Final testREgexMathcWordValue is="	+ polEvalConfigUnit.getExpression());

			}
		}

		if (regex != null) {
			// split given Expression
			String regexExpressionArray[] = regex.split("&&");

			Assert.assertNotNull("RegexExpressionArray  should not be null ",regexExpressionArray);
			Assert.assertTrue("RegexExpressionArray  length greater than 1",regexExpressionArray.length > 1);

			String regexOne = regexExpressionArray[0];

			String regexTwo = regexExpressionArray[1];

			boolean isRangeMatched = checkRegexForRangeVlue(regexTwo);
			boolean isWordMatched = checkRegexForWordmatching(regexOne);
			Assert.assertTrue(isRangeMatched);
			Assert.assertTrue(isWordMatched);

		} else {
			Assert.fail("genrated REGEX Expressiion is Null for given Policy name = "+ policy.getPolicyName());
		}

	}

	/**
	 * test to check given input is in given range using Regex
	 * 
	 * */
	@Test
	public void testREgexRangeValue() {
		String regex = null;
		Policy policy = getPolicyByName("PolicyDefinedFactAttDateTestSix");
		List<Evaluation> evalList = policy.getPolicyEvaluation().getEvaluation();
		for (Evaluation eval : evalList) {
			if (eval.getEvalDialect().value().equalsIgnoreCase("REGEX")) {
				PolicyEvaluationConfigurationUnit polEvalConfigUnit = this.policyRegexChecker.buildEvaluation(eval);
				regex = polEvalConfigUnit.getExpression();
				logger.debug("Final PolicyEvaluationConfigurationUnit is="+ polEvalConfigUnit);
				logger.debug("Final testREgexRangeValue is="+ polEvalConfigUnit.getExpression());

			}
		}

		if (regex != null) {
			boolean isMatched = checkRegexForRangeVlue(regex);
			Assert.assertTrue(isMatched);

		} else {
			Assert.fail("genrated REGEX Expressiion is Null for given Policy name = "
					+ policy.getPolicyName());
		}

	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	private Policy getPolicyByName(String name) {
		logger.debug("inside getPolicyByName() of PolicyConfigurationServiceTest");
		for (Policy pol : policyConfigList) {
			if (pol.getPolicyName().equalsIgnoreCase(name))
				return pol;
		}
		return null;
	}

	private boolean checkRegexForRangeVlue(String regex) {
		// split given Expression
		String givenREgexExpression = "";
		String givenValue = "";
		// trim to replace open and closed Pranathisis

		regex = regex.trim();



		String[] regexArray = regex.split("matches");
		Assert.assertNotNull("regexArray is should NOT be Null", regexArray);
		// regexArray legnth should be greater than 1
		Assert.assertTrue(regexArray.length > 1);

		givenREgexExpression = regexArray[0].trim();
		givenValue = regexArray[1].replace("$platform", "29");

		logger.debug("given regex  " + givenREgexExpression);
		logger.debug("given input   " + givenValue);

		boolean isMatched = Pattern.matches(givenREgexExpression,givenValue.trim());
		return isMatched;

	}

	private boolean checkRegexForWordmatching(String regex) {
		// split given Expression
		String givenREgexExpression = "";
		String givenValue = "";
	

		regex = regex.trim();

		String[] regexArray = regex.split("matches");
		Assert.assertNotNull("regexArray is should NOT be Null", regexArray);
		// regexArray legnth should be greater than 1
		Assert.assertTrue(regexArray.length > 1);

		givenREgexExpression = regexArray[0].replace("$dstare", "welcome");

		givenValue = regexArray[1].replace("$GetStagingAreas","hello welcome  ");

		logger.debug("given regex  " + givenREgexExpression);
		logger.debug("given input   " + givenValue);

		Pattern pattern = Pattern.compile(givenREgexExpression);

		Matcher matcher = pattern.matcher(givenValue);

		boolean isMatched = matcher.find();
		return isMatched;
	}

}
