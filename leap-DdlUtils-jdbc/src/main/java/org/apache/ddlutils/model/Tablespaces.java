package org.apache.ddlutils.model;

import java.util.List;

public class Tablespaces {
	private List<TableSpace> tableSpaces;

	/**
	 * @return the tableSpaces
	 */
	public List<TableSpace> getTableSpaces() {
		return tableSpaces;
	}

	/**
	 * @param tableSpaces
	 *            the tableSpaces to set
	 */
	public void setTableSpaces(List<TableSpace> tableSpaces) {
		this.tableSpaces = tableSpaces;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Tablespaces [tableSpaces=" + tableSpaces + "]";
	}

}
