package com.attunedlabs.leap.entity.leapdata;

import java.io.Serializable;

public class LeapDataSource implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String tabelName;
	private String schemaName;
	private String dbType;

	public LeapDataSource() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LeapDataSource(String tabelName, String schemaName, String dbType) {
		super();
		this.tabelName = tabelName;
		this.schemaName = schemaName;
		this.dbType = dbType;
	}

	public String getTabelName() {
		return tabelName;
	}

	public void setTabelName(String tabelName) {
		this.tabelName = tabelName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	@Override
	public String toString() {
		return "LeapDataSource [tabelName=" + tabelName + ", schemaName=" + schemaName + ", dbType=" + dbType + "]";
	}

}
