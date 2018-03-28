package com.attunedlabs.permastore;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigPolicyEvaluation {
	private Map<String,String> expression;
	private String evaluation;
	
	public ConfigPolicyEvaluation(){
		expression=new LinkedHashMap<>();
	}

	public Map<String, String> getExpression() {
		return expression;
	}

	public void setExpression(Map<String, String> expression) {
		this.expression = expression;
	}
	public void addExpression(String expressionKey,String expression) {
		this.expression.put(expressionKey,expression);
	}
	
	public String getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(String evaluation) {
		this.evaluation = evaluation;
	}

	@Override
	public String toString() {
		return "ConfigPolicyEvaluation [expression=" + expression
				+ ", evaluation=" + evaluation + "]";
	}
	
	
}
