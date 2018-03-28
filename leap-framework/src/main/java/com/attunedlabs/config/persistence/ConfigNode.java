package com.attunedlabs.config.persistence;

public class ConfigNode 
{
	public static final String NODETYPE_TENANT="tenant";
	public static final String NODETYPE_SITE="site";
	public static final String NODETYPE_FEATUREGROUP="feature_group";
	public static final String NODETYPE_FEATURE="feature";
	public static final String NODETYPE_IMPLEMENTATION="implementation";
	public static final String NODETYPE_VENDOR="vendor";
	public static final String NODETYPE_VERSION="version";

	public static final int NODELEVEL_TENANT=1;
	public static final int NODELEVEL_SITE=2;
	public static final int NODELEVEL_FEATUREGROUP=3;
	public static final int NODELEVEL_FEATURE=4;
	public static final int NODELEVEL_IMPLEMENTATION=5;
	public static final int NODELEVEL_VENDOR=6;
	
	private Integer nodeId;
	private String nodeName;
	private Boolean isRoot;
	private Boolean hasChildren;
	private Integer parentNodeId;
	private String description;
	private String type;
	private Integer level;
	private String version;
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Integer getNodeId() 
	{
		return nodeId;
	}
	public void setNodeId(Integer nodeId) 
	{
		this.nodeId = nodeId;
	}
	public String getNodeName() 
	{
		return nodeName;
	}
	public void setNodeName(String nodeName) 
	{
		this.nodeName = nodeName;
	}
	public boolean isRoot() 
	{
		return isRoot;
	}
	public void setRoot(Boolean isRoot) 
	{
		this.isRoot = isRoot;
	}
	public Boolean isHasChildren() 
	{
		return hasChildren;
	}
	public void setHasChildren(boolean hasChildren) 
	{
		this.hasChildren = hasChildren;
	}
	public Integer getParentNodeId() 
	{
		return parentNodeId;
	}
	public void setParentNodeId(Integer parentNodeId) 
	{
		this.parentNodeId = parentNodeId;
	}
	public String getDescription() 
	{
		return description;
	}
	public void setDescription(String description) 
	{
		this.description = description;
	}
	public String getType() 
	{
		return type;
	}
	public void setType(String type) 
	{
		this.type = type;
	}
	public Integer getLevel() 
	{
		return level;
	}
	public void setLevel(Integer level) 
	{
		this.level = level;
	}
	
	@Override
	public String toString() 
	{
		return "ConfigNode [nodeId=" + nodeId + ", nodeName=" + nodeName
				+ ", isRoot=" + isRoot + ", hasChildren=" + hasChildren
				+ ", parentNodeId="
				+ parentNodeId + ", description=" + description + ", type="
				+ type + ", level=" + level + ", version="+ version +"]";
	}
}
