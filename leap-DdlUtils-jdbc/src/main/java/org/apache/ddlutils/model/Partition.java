package org.apache.ddlutils.model;

import java.io.Serializable;

public class Partition implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5171878174039033864L;
	private String name;
	private String values;
	private String tablespace;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValues() {
		return values;
	}

	/**
	 * @param values
	 *            the value to set
	 */
	public void setValues(String values) {
		this.values = values;
	}

	/**
	 * @return the tablespace
	 */
	public String getTablespace() {
		return tablespace;
	}

	/**
	 * @param tablespace
	 *            the tablespace to set
	 */
	public void setTablespace(String tablespace) {
		this.tablespace = tablespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Partition [name=" + name + ", values=" + values + ", tablespace=" + tablespace + "]";
	}

}
