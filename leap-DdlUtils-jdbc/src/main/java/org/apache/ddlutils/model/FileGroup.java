package org.apache.ddlutils.model;

public class FileGroup {
	private String fileGroupName;
	private String fileName;
	private String location;
	private String maxSize;
	private String size;
	private String fileGrowth;

	public String getFileGroupName() {
		return fileGroupName;
	}

	public void setFileGroupName(String fileGroupName) {
		this.fileGroupName = fileGroupName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(String maxSize) {
		this.maxSize = maxSize;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getFileGrowth() {
		return fileGrowth;
	}

	public void setFileGrowth(String fileGrowth) {
		this.fileGrowth = fileGrowth;
	}

	@Override
	public String toString() {
		return "FileGroup [fileGroupName=" + fileGroupName + ", fileName=" + fileName + ", location=" + location
				+ ", maxSize=" + maxSize + ", size=" + size + ", fileGrowth=" + fileGrowth + "]";
	}

}
