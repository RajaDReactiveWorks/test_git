package com.attunedlabs.leap.entity.leapdata;

import java.io.Serializable;

public class Data implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String primitive;
	private Object value;

	public Data() {
		super();
	}

	public Data(String name, String primitive, Object value) {
		super();
		this.name = name;
		this.primitive = primitive;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrimitive() {
		return primitive;
	}

	public void setPrimitive(String primitive) {
		this.primitive = primitive;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "name=" + name + ", primitive=" + primitive + ", value=" + value ;
	}



}
