package org.palettelabs.iumls.utils;

public class NumberConverter {

	public static int toInteger(String asString) {
		return Integer.parseInt(asString);
	}

	public static int toInteger(String asString, int defaultValue) {
		try {
			return Integer.parseInt(asString);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static long toLong(String asString) {
		return Long.parseLong(asString);
	}

	public static long toLong(String asString, long defaultValue) {
		try {
			return Long.parseLong(asString);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static double toDouble(String asString) {
		return Double.parseDouble(asString);
	}

	public static double toDouble(String asString, double defaultValue) {
		try {
			return Double.parseDouble(asString);
		} catch (Exception e) {
			return defaultValue;
		}
	}
}
