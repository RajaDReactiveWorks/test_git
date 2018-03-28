package com.attunedlabs.config.util;

public class ConfigUtil {
	public static int conversionOfLongToIntSetup(Object value) {
		if (value instanceof Integer) {
			return (int) value;
		} else {
			return (int) (long) value;
		}
	}
}
