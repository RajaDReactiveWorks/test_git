package com.attunedlabs.leap.i18n.utils;

public class LeapI18nUtil {

	private LeapI18nUtil() {
	}

	/**
	 * 
	 * @param source
	 * @return
	 */
	public static String buildString(String source) {
		return source.replaceAll("\\.", "");
	}// ..end of the method
	
	/**
	 * 
	 * @param source
	 * @return
	 */
	public static String buildBundleFileString(String source) {
		return source.replaceAll("\\.", "-");
	}// ..end of the method

	/**
	 * Utility to check string is empty or not
	 * 
	 * @param source
	 * @return
	 */
	public static boolean isEmpty(String source) {
		if (source == null || source.isEmpty()) {
			return true;
		}
		return false;
	}// ..end of the utility

	/**
	 * Utility to check string is empty or not
	 * 
	 * @param source
	 * @return
	 */
	public static boolean isEmpty(int source) {
		if (source == 0) {
			return true;
		}
		return false;
	}// ..end of the utility

}
