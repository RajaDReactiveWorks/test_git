package org.apache.ddlutils.model;

public class File {
	private String name;
	private String directory;
	private String size;
	private String reuse;
	private String autoExtendNextSize;
	private String maxSize;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getReuse() {
		return reuse;
	}

	public void setReuse(String reuse) {
		this.reuse = reuse;
	}

	public String getAutoExtendNextSize() {
		return autoExtendNextSize;
	}

	public void setAutoExtendNextSize(String autoExtendNextSize) {
		this.autoExtendNextSize = autoExtendNextSize;
	}

	public String getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(String maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	public String toString() {
		return "File [name=" + name + ", directory=" + directory + ", size=" + size + ", reuse=" + reuse
				+ ", autoExtendNextSize=" + autoExtendNextSize + ", maxSize=" + maxSize + "]";
	}

}
