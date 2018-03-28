package org.apache.ddlutils.model;

import java.io.Serializable;
import java.util.List;

public class PartitionType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6472403190148231613L;

	private String columnName;
	private String type;
	private List<Partition> partitions;
	private String partitionCount;
	private String schema;

	/**
	 * @return the partitionCount
	 */
	public String getPartitionCount() {
		return partitionCount;
	}

	/**
	 * @param partitionCount
	 *            the partitionCount to set
	 */
	public void setPartitionCount(String partitionCount) {
		this.partitionCount = partitionCount;
	}

	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @param columnName
	 *            the columnName to set
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the partitions
	 */
	public List<Partition> getPartitions() {
		return partitions;
	}

	/**
	 * @param partitions
	 *            the partitions to set
	 */
	public void setPartitions(List<Partition> partitions) {
		this.partitions = partitions;
	}

	/**
	 * @return the schema
	 */
	public String getSchema() {
		return schema;
	}

	/**
	 * @param schema
	 *            the schema to set
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PartitionType [columnName=" + columnName + ", type=" + type + ", partitions=" + partitions
				+ ", partitionCount=" + partitionCount + ", schema=" + schema + "]";
	}

}
