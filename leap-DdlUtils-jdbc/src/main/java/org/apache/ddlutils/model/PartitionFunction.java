package org.apache.ddlutils.model;

import java.util.List;

public class PartitionFunction {
	private String name;
	private String columnType;
	private String rangeType;
	private String values;
	private List<FileGroup> fileGroup;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public String getRangeType() {
		return rangeType;
	}

	public void setRangeType(String rangeType) {
		this.rangeType = rangeType;
	}

	public String getValues() {
		return values;
	}

	public void setValues(String values) {
		this.values = values;
	}

	/**
	 * @return the fileGroup
	 */
	public List<FileGroup> getFileGroup() {
		return fileGroup;
	}

	/**
	 * @param fileGroup the fileGroup to set
	 */
	public void setFileGroup(List<FileGroup> fileGroup) {
		this.fileGroup = fileGroup;
	}

	@Override
	public String toString() {
		return "PartitionFunction [name=" + name + ", columnType=" + columnType + ", rangeType=" + rangeType
				+ ", values=" + values + ", fileGroup=" + fileGroup + "]";
	}

}
