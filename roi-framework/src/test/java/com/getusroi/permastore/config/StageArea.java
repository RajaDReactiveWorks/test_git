package com.getusroi.permastore.config;

import java.io.Serializable;

public class StageArea implements Serializable{
	
	public static final long serialVersionUID = 5271810606200885948L;
	private Integer stageAreaId;
	private String  areaType;
	private String  areaName;
	
	public Integer getStageAreaId() {
		return stageAreaId;
	}
	public void setStageAreaId(Integer stageAreaId) {
		this.stageAreaId = stageAreaId;
	}
	public String getAreaType() {
		return areaType;
	}
	public void setAreaType(String areaType) {
		this.areaType = areaType;
	}
	public String getAreaName() {
		return areaName;
	}
	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}
	
	public String toString() {
		return "StageArea [stageAreaId=" + stageAreaId + ", areaType=" + areaType + ", areaName=" + areaName + "]";
	}
		
}
