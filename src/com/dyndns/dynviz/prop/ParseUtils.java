package com.dyndns.dynviz.prop;

public class ParseUtils {

	public static boolean parseBoolean(String value, boolean defaultValue) {
		return value == null || value.length() == 0 ? defaultValue : Boolean.parseBoolean(value);
	}

	public static int parseInt(String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static float parseFloat(String value, float defaultValue) {
		try {
			return Float.parseFloat(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static int parseColor(String value, int defaultValue) {
		if (value == null || value.length() == 0) {
			return defaultValue;
		}

		if (value.charAt(0) != '#') {
			System.err.println("Color value '" + value + "' should starts with '#' char");
			return defaultValue;
		}

		value = value.substring(1);
		if (value.length() == 6) {
			value = "FF" + value;
		}

		if (value.length() != 8) {
			System.err.println("Color value '" + value + "' should consist of 6 or 8 [0-9A-F] chars");
			return defaultValue;
		}

		try {
			return (int) Long.parseLong(value, 16);
		} catch (Exception e) {
			e.printStackTrace();
			return defaultValue;
		}
	}

}
