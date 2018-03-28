package com.attunedlabs.leap.entity.leapdata;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONObject;

public class LeapData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JSONObject dataSource;
	private JSONArray metaData;
	private JSONObject data;

	public LeapData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LeapData(JSONObject dataSource, JSONArray metaData, JSONObject data) {
		super();
		this.dataSource = dataSource;
		this.metaData = metaData;
		this.data = data;
	}

	public JSONArray getMetaData() {
		return metaData;
	}

	public void setMetaData(JSONArray metaDataArray) {
		metaData = metaDataArray;
	}

	public JSONObject getData() {
		return data;
	}

	public void setData(JSONObject jsonObject) {
		data = jsonObject;
	}

	public JSONObject getDataSource() {
		return dataSource;
	}

	public void setDataSource(JSONObject dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public String toString() {
		return " metaData=" + metaData + ", data=" + data ;
	}

}
