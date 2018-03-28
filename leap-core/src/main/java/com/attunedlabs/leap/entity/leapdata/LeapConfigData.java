package com.attunedlabs.leap.entity.leapdata;

import java.io.Serializable;

import org.json.JSONArray;

public class LeapConfigData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JSONArray metaData;
	private JSONArray data;

	/**
	 * @return the metaData
	 */
	public JSONArray getMetaData() {
		return metaData;
	}

	/**
	 * @param metaData
	 *            the metaData to set
	 */
	public void setMetaData(JSONArray metaData) {
		this.metaData = metaData;
	}

	/**
	 * @return the data
	 */
	public JSONArray getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(JSONArray data) {
		this.data = data;
	}

	/**
	 * @param metaData
	 * @param data
	 */
	public LeapConfigData(JSONArray metaData, JSONArray data) {
		super();
		this.metaData = metaData;
		this.data = data;
	}

	/**
	 * 
	 */
	public LeapConfigData() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "metaData=" + metaData + ", data=" + data;
	}

}
