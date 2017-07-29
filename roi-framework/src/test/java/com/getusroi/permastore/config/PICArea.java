package com.getusroi.permastore.config;

import java.io.Serializable;

public class PICArea implements Serializable{
	private static final long serialVersionUID = 1201954339298425853L;
	private String picAreaName;
	private String picAreaAddr;
	
	
	public PICArea(String picAreaName, String picAreaAddr) {
		this.picAreaName = picAreaName;
		this.picAreaAddr = picAreaAddr;
	}
	public String getPicAreaName() {
		return picAreaName;
	}
	public void setPicAreaName(String picAreaName) {
		this.picAreaName = picAreaName;
	}
	public String getPicAreaAddr() {
		return picAreaAddr;
	}
	public void setPicAreaAddr(String picAreaAddr) {
		this.picAreaAddr = picAreaAddr;
	}
	
	
	
}