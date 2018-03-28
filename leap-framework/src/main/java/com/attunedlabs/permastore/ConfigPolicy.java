package com.attunedlabs.permastore;

import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName(value = "Policy")
public class ConfigPolicy {
	  private String nodeTree;
	  private String name;
	  private String version;
	  private String group;
	  private ConfigPolicyFacts configPolicyFacts;
	  private ConfigPolicyEvaluation policyEvaluation;
	  private String configData;
	  
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		public String getGroup() {
			return group;
		}
		public void setGroup(String group) {
			this.group = group;
		}
		
		
		public String getNodeTree() {
			return nodeTree;
		}
		public void setNodeTree(String nodeTree) {
			this.nodeTree = nodeTree;
		}
		public ConfigPolicyFacts getConfigPolicyFacts() {
			return configPolicyFacts;
		}
		public void setConfigPolicyFacts(ConfigPolicyFacts configPolicyFacts) {
			this.configPolicyFacts = configPolicyFacts;
		}
		
		public ConfigPolicyEvaluation getPolicyEvaluation() {
			return policyEvaluation;
		}
		public void setPolicyEvaluation(ConfigPolicyEvaluation policyEvaluation) {
			this.policyEvaluation = policyEvaluation;
		}
		
		public String getConfigData() {
			return configData;
		}
		public void setConfigData(String configData) {
			this.configData = configData;
		}
		
		public String toString() {
			return "ConfigPolicy [name=" + name + ", version=" + version + ", group="
					+ group + configPolicyFacts.toString()+policyEvaluation+"configData{"+configData+"}"+"]";
		}
		
	  
}
