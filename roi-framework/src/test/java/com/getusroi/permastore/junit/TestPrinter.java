package com.getusroi.permastore.junit;

import java.io.Serializable;
/**
 * Test Class Should be in Test Package
 * @author bizruntime
 *
 */
public class TestPrinter implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1406742246562226786L;
	String printerType;
	int lenSize;
	int lenwidth;
	boolean onPremise;
	
	public TestPrinter(String printerType, int lenSize, int lenwidth,
			boolean onPremise) {
		super();
		this.printerType = "Label";
		this.lenSize = lenSize;
		this.lenwidth = lenwidth;
		this.onPremise = onPremise;
	}

	public String getPrinterType() {
		return printerType;
	}

	public void setPrinterType(String printerType) {
		this.printerType = printerType;
	}

	public int getLenSize() {
		return lenSize;
	}

	public void setLenSize(int lenSize) {
		this.lenSize = lenSize;
	}

	public int getLenwidth() {
		return lenwidth;
	}

	public void setLenwidth(int lenwidth) {
		this.lenwidth = lenwidth;
	}

	public boolean isOnPremise() {
		return onPremise;
	}

	public void setOnPremise(boolean onPremise) {
		this.onPremise = onPremise;
	}

	@Override
	public String toString() {
		return "TestPrinter [printerType=" + printerType + ", lenSize="
				+ lenSize + ", lenwidth=" + lenwidth + ", onPremise="
				+ onPremise + "]";
	}
	
}
