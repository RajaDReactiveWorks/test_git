package com.attunedlabs.config.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigurationTreeNode implements Serializable {
	private static final long serialVersionUID = 2495356995980083645L;

	private List<ConfigurationTreeNode> childNodes;

	private ConfigurationTreeNode parentNode;
	private Integer nodeId;
	private String nodeName;
	transient private String type;
	transient private int level;
	private Integer parentNodeId;
	private String version;
	private int primaryFeatureId;
	private boolean hasChildern;
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isHasChildern() {
		return hasChildern;
	}

	public void setHasChildern(boolean hasChildern) {
		this.hasChildern = hasChildern;
	}

	public ConfigurationTreeNode(Integer nodeId, String nodeName, String type, int level) {
		this.nodeId = nodeId;
		this.nodeName = nodeName;
		this.type = type;
		this.level = level;
	}

	public ConfigurationTreeNode(Integer nodeId, String nodeName, String type, int level, String version) {
		this.nodeId = nodeId;
		this.nodeName = nodeName;
		this.type = type;
		this.level = level;
		this.version = version;
	}

	public ConfigurationTreeNode(Integer nodeId, String nodeName, String type, int level, String version,
			int primaryFeatureId) {
		this.nodeId = nodeId;
		this.nodeName = nodeName;
		this.type = type;
		this.level = level;
		this.version = version;
		this.primaryFeatureId = primaryFeatureId;
	}

	public ConfigurationTreeNode(String nodeName, String type, int level, Integer parentNodeId, String version,
			int primaryFeatureId, boolean hasChildern, String description) {
		super();
		this.nodeName = nodeName;
		this.type = type;
		this.level = level;
		this.parentNodeId = parentNodeId;
		this.version = version;
		this.primaryFeatureId = primaryFeatureId;
		this.hasChildern = hasChildern;
		this.description = description;
	}

	public ConfigurationTreeNode() {

	}

	public boolean deleteChildren(ConfigurationTreeNode childNode) {
		if (childNodes == null)
			return false;

		// If Child Node has this as Parent than remove
		if (childNode.parentNodeId.intValue() == this.nodeId.intValue()) {
			childNodes.remove(childNode);
			return true;
		} else {

			for (ConfigurationTreeNode parentNode : childNodes) {
				boolean isRemoved = parentNode.deleteChildren(childNode);
				if (isRemoved)
					return true;
			}

		}
		return false;
	}

	public boolean addChildren(ConfigurationTreeNode childNode) {
		if (childNodes == null)
			childNodes = new ArrayList<>();
		// If Child Node has this as Parent than add other wise iterate through
		// the List of Child and than Add
		if (childNode.parentNodeId != null && this.nodeId != null)
			if (childNode.parentNodeId.intValue() == this.nodeId.intValue()) {
				childNodes.add(childNode);
				childNode.setParentNode(this);
				// System.out.println("ChildNode-Added
				// ChildNodeId="+childNode.getNodeId()+"---to Parent
				// NodeId="+nodeId);
				return true;
			} else {

				for (ConfigurationTreeNode parentNode : childNodes) {
					boolean isAdded = parentNode.addChildren(childNode);
					if (isAdded)
						return true;
				}

			}
		return false;
	}

	public List<ConfigurationTreeNode> getChildNodes() {
		return childNodes;
	}

	public void setChildNodes(List<ConfigurationTreeNode> childNodes) {
		this.childNodes = childNodes;
	}

	public ConfigurationTreeNode getParentNode() {
		return parentNode;
	}

	public void setParentNode(ConfigurationTreeNode parentNode) {
		this.parentNode = parentNode;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Integer getParentNodeId() {
		return parentNodeId;
	}

	public void setParentNodeId(Integer parentNodeId) {
		this.parentNodeId = parentNodeId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getPrimaryFeatureId() {
		return primaryFeatureId;
	}

	public void setPrimaryFeatureId(int primaryFeatureId) {
		this.primaryFeatureId = primaryFeatureId;
	}

	public void getConfigTreeNodeAsJSONString(StringBuffer jsonStrBuffer) {
		if (jsonStrBuffer == null)
			jsonStrBuffer = new StringBuffer();
		/*
		 * "nodeName": "Printing Policy", "nodeId": "4", "type": "policyGroup",
		 * "hasChildren": "false", "children": []
		 */
		boolean hasChildren = true;
		if (this.childNodes == null || this.childNodes.isEmpty()) {
			hasChildren = false;
		}

		jsonStrBuffer.append("{\"nodeName\":\"" + this.nodeName + "\",");
		jsonStrBuffer.append("\"nodeId\":\"" + this.nodeId + "\",");
		jsonStrBuffer.append("\"type\":\"" + this.type + "\",");
		jsonStrBuffer.append("\"version\":\"" + this.version + "\",");
		jsonStrBuffer.append("\"primaryFeatureId\":\"" + this.primaryFeatureId + "\",");
		jsonStrBuffer.append("\"hasChildren\":\"" + hasChildren + "\",");
		jsonStrBuffer.append("\"children\":[");
		// Get from Child Nodes
		if (this.childNodes != null) {
			int i = 0;
			for (ConfigurationTreeNode childNode : childNodes) {
				if (i > 0) {
					jsonStrBuffer.append(",");
				}
				childNode.getConfigTreeNodeAsJSONString(jsonStrBuffer);

				i++;

			}
			jsonStrBuffer.append("]");
			jsonStrBuffer.append("}");
		} else {
			jsonStrBuffer.append("]");
			jsonStrBuffer.append("}");
		}

	}

	public boolean equals(Object objtoCompare) {
		if (objtoCompare instanceof ConfigurationTreeNode) {
			ConfigurationTreeNode treeNodeToCompare = (ConfigurationTreeNode) objtoCompare;
			if (this.nodeId.intValue() == treeNodeToCompare.getNodeId().intValue()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "Node [ nodeId=" + nodeId + ", nodeName=" + nodeName + ", type=" + type + ", level=" + level
				+ ", parentNodeId=" + parentNodeId + ", version=" + version + ", primaryFeatureId=" + primaryFeatureId
				+ "{" + childNodes + "]]";
	}

}
