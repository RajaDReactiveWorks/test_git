package com.attunedlabs.leap.entity.leapdata;

public class WhereClauseFilter {
	private String columnName;
	private String operatorType;
	private Object value;
	private boolean isBinary;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WhereClauseFilter [columnName=" + columnName + ", operatorType=" + operatorType + ", value=" + value
				+ ", isBinary=" + isBinary + "]";
	}

	
	/**
	 * @param columnName
	 * @param operatorType
	 * @param value
	 */
	public WhereClauseFilter(String columnName, String operatorType, Object value) {
		this(columnName, operatorType, value, false);
	}


	/**
	 * @param columnName
	 * @param operatorType
	 * @param value
	 * @param isBinary
	 */
	public WhereClauseFilter(String columnName, String operatorType, Object value, boolean isBinary) {
		super();
		this.columnName = columnName;
		this.operatorType = operatorType;
		this.value = value;
		this.isBinary = isBinary;
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
	 * @return the operatorType
	 */
	public String getOperatorType() {
		return operatorType;
	}

	/**
	 * @param operatorType
	 *            the operatorType to set
	 */
	public void setOperatorType(String operatorType) {
		this.operatorType = operatorType;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return the isBinary
	 */
	public boolean isBinary() {
		return isBinary;
	}

	/**
	 * @param isBinary
	 *            the isBinary to set
	 */
	public void setBinary(boolean isBinary) {
		this.isBinary = isBinary;
	}

}
