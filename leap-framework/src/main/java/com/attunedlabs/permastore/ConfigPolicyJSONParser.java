package com.attunedlabs.permastore;

import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
/**
 * JSON Parcer class for parcing the policy json String
 * @author bizruntime
 *
 */
public class ConfigPolicyJSONParser {
	private String jsonStr;
	private JSONObject jsonObject;
	
	public ConfigPolicyJSONParser(String jsonStr) {
		this.jsonStr = jsonStr;
		JSONParser parser = new JSONParser();
		try {

			// System.out.println(str);
			this.jsonObject= (JSONObject)parser.parse(jsonStr);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}
	
	public ConfigPolicy getConfigPolicy(){
		ConfigPolicy policy=new ConfigPolicy();
		//System.out.println(jsonObject.get("Policy").getClass());
		JSONObject policyJson=(JSONObject)jsonObject.get("Policy");
		policy.setName((String) policyJson.get("Name"));
		policy.setVersion((String) policyJson.get("Version"));
		policy.setGroup( (String)policyJson.get("Group"));
		
		ConfigPolicyFacts facts=new ConfigPolicyFacts();
		JSONObject factsJson=(JSONObject)policyJson.get("Facts");
		System.out.println("Fact"+factsJson);
		Set<String> factKeySet=factsJson.keySet();
		for(String factKey:factKeySet){
			//System.out.println("FactKey"+factKey);
			String value=(String)factsJson.get(factKey);
			facts.addFact(factKey, value);
		}
		policy.setConfigPolicyFacts(facts);
		
		ConfigPolicyEvaluation policyEval=new ConfigPolicyEvaluation();
		JSONObject expressionJson=(JSONObject)policyJson.get("WhenApplicable");
		String eval=(String)expressionJson.get("Evaluation");
		policyEval.setEvaluation(eval);
		Set<String> expressionKeySet=expressionJson.keySet();
		for(String expKey:expressionKeySet){
			if(expKey.equalsIgnoreCase("Evaluation"))
					continue;
			//System.out.println("FactKey"+factKey);
			String value=(String)expressionJson.get(expKey);
			policyEval.addExpression(expKey, value);
			
		}
		policy.setPolicyEvaluation(policyEval);
		
		//PolicyData
		JSONObject policyDataJson=(JSONObject)policyJson.get("PolicyData");
		policy.setConfigData(policyDataJson.toJSONString());
		
		return policy;
		
		
		
	}
	
}
