package org.apache.ddlutils.model;

import java.util.List;

public class TableSpace {
	private String name;
	private String type;
	private List<File> file;

	
	/**
	 * @return the file
	 */
	public List<File> getFile() {
		return file;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(List<File> file) {
		this.file = file;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "TableSpace [name=" + name + ", type=" + type + ", file=" + file + "]";
	}

}
