package com.getusroi.policy.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mvel2.MVEL;
import org.mvel2.PropertyAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.persistence.ConfigPersistenceException;
import com.getusroi.policy.PolicyTestConstant;
import com.getusroi.policy.config.exp.sqltomvel.PolicySQLDialectExpressionBuilder;
import com.getusroi.policy.jaxb.Evaluation;
import com.getusroi.policy.jaxb.Policies;
import com.getusroi.policy.jaxb.Policy;

public class PolicySQLDialectExpressionBuilderTest {
	final Logger logger = LoggerFactory.getLogger(PolicyConfigurationServiceTest.class);
	private List<Policy> policyConfigList;
	private PolicySQLDialectExpressionBuilder policySqlBuilder;

	private Policies getPolicyConfiguration() throws PolicyConfigXMLParserException {
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
	public void loadConfigurations() throws PolicyConfigurationException, ConfigPersistenceException, PolicyConfigXMLParserException {
		Policies policyConfig = getPolicyConfiguration();
		this.policyConfigList = policyConfig.getPolicy();
		this.policySqlBuilder = new PolicySQLDialectExpressionBuilder();
	}

	@Test
	// to check contains and not contains value in mvel Expression
	public void testContainsAndNotContians() {
		String mvelExp = null;
		Policy policy = getPolicyByName("PolicyDefinedFactTest");
		List<Evaluation> evalList = policy.getPolicyEvaluation().getEvaluation();
		for (Evaluation eval : evalList) {
			if (eval.getEvalDialect().value().equalsIgnoreCase("SQL")) {
				PolicyEvaluationConfigurationUnit polEvalConfigUnit = this.policySqlBuilder.buildEvaluation(eval);
				mvelExp = polEvalConfigUnit.getExpression();
				logger.debug("Final PolicyEvaluationConfigurationUnit is=" + polEvalConfigUnit);
				logger.debug("Final  testContainsAndNotContians  mvelExp is=" + polEvalConfigUnit.getExpression());
			}
		}

		// ( ($GetStagingAreas).contains($dstare) && $ndstare.contains("Wood") )
		// && ( $platform.equals("WOOD") && $platform.equals("WOOD") ||
		// $platform.equals($good) )
		// test the MVEL CREATED
		// mvelExp=" $GetStagingAreas contains $dstare ";
		// mvelExp=" $platform == (\"WOOD\")";
		logger.debug("MVEL String is=" + mvelExp);
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("$dstare", "ABC");
		map.put("$ndstare", "Wood");
		map.put("$platform", "WOOD");
		map.put("$good", "WOOD");
		List<String> stageAreaList = new ArrayList<String>();
		stageAreaList.add("XYZ");
		stageAreaList.add("ABC");
		map.put("$GetStagingAreas", stageAreaList);

		// Serializable compiledmvelExp=MVEL.compileExpression(mvelExp);
		// logger.debug("Compliled MVEL Exp is="+compiledmvelExp);
		boolean isTrue = evaluateMvelExpression(mvelExp, map);
		logger.debug("MVEL .Result is=" + isTrue);

	}

	@Test
	// to check equal and not equal in mvel Expression
	public void testNotEqualAndEqualMvelScript() {

		String mvelExp = null;
		Policy policy = getPolicyByName("PolicyDefinedFactAttDateTestOne");
		List<Evaluation> evalList = policy.getPolicyEvaluation().getEvaluation();
		for (Evaluation eval : evalList) {
			if (eval.getEvalDialect().value().equalsIgnoreCase("SQL")) {
				PolicyEvaluationConfigurationUnit polEvalConfigUnit = this.policySqlBuilder.buildEvaluation(eval);
				mvelExp = polEvalConfigUnit.getExpression();
				logger.debug("Final PolicyEvaluationConfigurationUnit is=" + polEvalConfigUnit);
				logger.debug("Final  testNotEqualAndEqualMvelScript mvelExp  is=" + polEvalConfigUnit.getExpression());
			}
		}

		logger.debug("MVEL String is=" + mvelExp);

		Map<String, Object> map = new HashMap<String, Object>();
		// checking both Not equal, equal
		map.put("$dstare", "ABC");
		map.put("$platform", "wood");

		map.put("$GetStagingAreas", "xyz");
		Object mvelResult = MVEL.eval((String) mvelExp, map);
		boolean sucess = (boolean) mvelResult;

		Assert.assertTrue(sucess);
	}

	@Test
	// testing using <> sql function (not equal) function in mvel
	public void testNotEqualMvelScript() {

		String mvelExp = null;
		Policy policy = getPolicyByName("FactMappingTest");
		List<Evaluation> evalList = policy.getPolicyEvaluation().getEvaluation();
		for (Evaluation eval : evalList) {
			if (eval.getEvalDialect().value().equalsIgnoreCase("SQL")) {
				PolicyEvaluationConfigurationUnit polEvalConfigUnit = this.policySqlBuilder.buildEvaluation(eval);
				mvelExp = polEvalConfigUnit.getExpression();
				logger.debug("Final PolicyEvaluationConfigurationUnit is=" + polEvalConfigUnit);
				logger.debug("Final  testNotEqualMvelScript mvelExp is=" + polEvalConfigUnit.getExpression());
			}
		}

		logger.debug("MVEL String is=" + mvelExp);

		Map<String, Object> map = new HashMap<String, Object>();
		// checking both Not equal, equal
		map.put("$platform", "ABC");

		map.put("$GetStagingAreas", "xyz");
		Object mvelResult = MVEL.eval((String) mvelExp, map);
		boolean sucess = (boolean) mvelResult;

		Assert.assertTrue(sucess);
	}

	@Test
	// check mvelExpression for Null value and Not Null value
	public void testIsNUllandNotNullMvelExpression()

	{

		String mvelExp = null;
		Policy policy = getPolicyByName("PolicyDefinedFactAttDateTestTwo");
		List<Evaluation> evalList = policy.getPolicyEvaluation().getEvaluation();
		for (Evaluation eval : evalList) {
			if (eval.getEvalDialect().value().equalsIgnoreCase("SQL")) {
				PolicyEvaluationConfigurationUnit polEvalConfigUnit = this.policySqlBuilder.buildEvaluation(eval);
				mvelExp = polEvalConfigUnit.getExpression();
				logger.debug("Final PolicyEvaluationConfigurationUnit is=" + polEvalConfigUnit);
				logger.debug("Final testIsNUllandNotNullMvelExpression mvelExp is=" + polEvalConfigUnit.getExpression());
			}
		}
		// String mvelExp = " ($dstare == nil)";

		logger.debug("MVEL String is=" + mvelExp);

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("$dstare", null);
		map.put("$platform", "test");

		Object mvelResult = MVEL.eval((String) mvelExp, map);
		boolean sucess = (Boolean) mvelResult;

		Assert.assertTrue(sucess);
	}

	@Test
	// check greaterthan and lesserThan in mvelExressin
	public void testGreaterThenAndLesserThenInMvelExpression() {

		String mvelExp = null;
		Policy policy = getPolicyByName("PolicyDefinedFactAttDateTestThree");
		List<Evaluation> evalList = policy.getPolicyEvaluation().getEvaluation();
		for (Evaluation eval : evalList) {
			if (eval.getEvalDialect().value().equalsIgnoreCase("SQL")) {
				PolicyEvaluationConfigurationUnit polEvalConfigUnit = this.policySqlBuilder.buildEvaluation(eval);
				mvelExp = polEvalConfigUnit.getExpression();
				logger.debug("Final PolicyEvaluationConfigurationUnit is=" + polEvalConfigUnit);
				logger.debug("Final testGreaterThenAndLesserThenInMvelExpression mvelExp is=" + polEvalConfigUnit.getExpression());
			}
		}

		// String mvelExp = "!($dstare contains ($GetStagingAreas))";

		logger.debug("MVEL String is=" + mvelExp);

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("$dstare", 20);
		map.put("$ndstare", "Wood");
		map.put("$platform", 10);
		map.put("$good", "WOOD");

		Object mvelResult = MVEL.evalToBoolean(mvelExp, map);
		boolean sucess = (Boolean) mvelResult;
		Assert.assertTrue(sucess);
	}

	@Test
	// to check GreaterThanEq and LesserThenEqMvel
	public void testGreaterThenEqAndLesserThenEQInMvelExpression() {

		String mvelExp = null;
		Policy policy = getPolicyByName("PolicyDefinedFactAttDateTestFour");
		List<Evaluation> evalList = policy.getPolicyEvaluation().getEvaluation();
		for (Evaluation eval : evalList) {
			if (eval.getEvalDialect().value().equalsIgnoreCase("SQL")) {
				PolicyEvaluationConfigurationUnit polEvalConfigUnit = this.policySqlBuilder.buildEvaluation(eval);
				mvelExp = polEvalConfigUnit.getExpression();
				logger.debug("Final PolicyEvaluationConfigurationUnit is=" + polEvalConfigUnit);
				logger.debug("Final testGreaterThenEqAndLesserThenEQInMvelExpression mvelExp is=" + polEvalConfigUnit.getExpression());
			}
		}

		// String mvelExp = "!($dstare contains ($GetStagingAreas))";

		logger.debug("MVEL String is=" + mvelExp);

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("$dstare", 20);
		map.put("$ndstare", 10);
		map.put("$platform", 10);
		map.put("$good", 30);

		Object mvelResult = MVEL.evalToBoolean(mvelExp, map);
		boolean sucess = (Boolean) mvelResult;
		Assert.assertTrue(sucess);
	}

	public void testLikeMvelExpression() {

		// name matches ".A.*"
		// String mvelExp = "($dstare.matches('[*b*]'))";

		String mvelExp = "( $dstare > ($platform) ) && ( $platform < ($dstare) )";
		logger.debug("MVEL String is=" + mvelExp);

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("$dstare", 20);

		Object mvelResult = MVEL.eval(mvelExp, map);
		System.out.println("matches  " + mvelResult);
		boolean sucess = (Boolean) mvelResult;

		Assert.assertTrue(sucess);
	}

	private boolean evaluateMvelExpression(Serializable mvelExp, Map<String, Object> requestFactMap) {
		try {

			logger.debug(".evaluateMvelExpression()...MVELeXP=" + mvelExp + "...reqMap=" + requestFactMap);
			// Object mvelResult = MVEL.evalToBoolean (mvelExp , requestFactMap);
			Object mvelResult = MVEL.eval((String) mvelExp, requestFactMap);
			logger.debug("evaluateMvelExpression()...Result=" + mvelResult);
			if (mvelResult instanceof Boolean) {
				return ((Boolean) mvelResult).booleanValue();
			}
		} catch (PropertyAccessException exp) {
			// Its normal for mvel evaluation to fail.. Log it and eating is ok.
			logger.debug("MVEL Evaluation failed with error", exp);
		}
		return false;
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
}
