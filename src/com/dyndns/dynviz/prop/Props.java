package com.dyndns.dynviz.prop;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Props {

	public static final String SCREEN_WIDTH = "screen_width";
	public static final String SCREEN_HEIGHT = "screen_height";
	public static final String QUERY_SCALE = "query_scale";
	public static final String QPS_MULTIPLIER = "qps_multiplier";
	public static final String USE_BLUR = "use_blur";
	public static final String USE_PCAP_URL = "use_pcap_url";
	public static final String PCAP_FILE = "pcap_file";
	public static final String PCAP_URL = "pcap_url";
	public static final String PCAP_URL_INTERVAL = "pcap_url_interval"; // in minutes
	public static final String USE_KINECT = "use_kinect";
	public static final String AUTO_CAST_SWITCH = "auto_cast_switch";
	public static final String AUTO_CAST_SWITCH_INTERVAL = "auto_cast_switch_interval"; // in seconds

	public static final String SHOW_STARS_IN_BACKGROUND = "show_stars_in_background";
	public static final String USE_NATURAL_EARTH_TEXTURE = "use_natural_earth_texture";

	public static final String SHOW_BORDERS = "show_borders";
	public static final String BORDERS_COLOR = "borders_color";
	public static final String SHOW_BOTTOM_PANEL = "show_bottom_panel";
	public static final String SHOW_GLOBAL_STATS_PANEL = "show_global_stats_panel";
	public static final String SHOW_SERVERS_FLAGS = "show_servers_flags";

	private static final int TYPE_STRING = 0;
	private static final int TYPE_INT = 1;
	private static final int TYPE_BOOLEN = 2;
	private static final int TYPE_COLOR = 3;

	@SuppressWarnings("serial")
	private static final Map<String, Integer> PROPS = new HashMap<String, Integer>() {
		{
			put(SCREEN_WIDTH, TYPE_INT);
			put(SCREEN_HEIGHT, TYPE_INT);
			put(QUERY_SCALE, TYPE_INT);
			put(QPS_MULTIPLIER, TYPE_INT);
			put(USE_BLUR, TYPE_BOOLEN);
			put(USE_PCAP_URL, TYPE_BOOLEN);
			put(PCAP_FILE, TYPE_STRING);
			put(PCAP_URL, TYPE_STRING);
			put(PCAP_URL_INTERVAL, TYPE_INT);
			put(USE_KINECT, TYPE_BOOLEN);
			put(AUTO_CAST_SWITCH, TYPE_BOOLEN);
			put(AUTO_CAST_SWITCH_INTERVAL, TYPE_INT);
			put(SHOW_STARS_IN_BACKGROUND, TYPE_BOOLEN);
			put(USE_NATURAL_EARTH_TEXTURE, TYPE_BOOLEN);
			put(SHOW_BORDERS, TYPE_BOOLEN);
			put(BORDERS_COLOR, TYPE_COLOR);
			put(SHOW_BOTTOM_PANEL, TYPE_BOOLEN);
			put(SHOW_GLOBAL_STATS_PANEL, TYPE_BOOLEN);
			put(SHOW_SERVERS_FLAGS, TYPE_BOOLEN);
		}
	};

	private static final Map<String, Object> VALUES = new HashMap<String, Object>();

	static {
		InputStream in = Props.class.getClassLoader().getResourceAsStream("default.properties");
		loadProperties(in, "Cannot load default properties");
	}

	public static void loadProperties(String path) {
		String error = "Error loading properties from file: " + path;
		try {
			loadProperties(new FileInputStream(path), error);
		} catch (Exception e) {
			System.err.println(error);
			return;
		}
	}

	public static String string(String key) {
		return checkValue(key, VALUES.get(key));
	}

	public static int integer(String key) {
		Integer value = checkValue(key, VALUES.get(key));
		return value == null ? 0 : value;
	}

	public static boolean bool(String key) {
		Boolean value = checkValue(key, VALUES.get(key));
		return value == null ? false : value;
	}

	@SuppressWarnings("unchecked")
	private static <T> T checkValue(String key, Object value) {
		if (value == null)
			System.err.println("Value for key '" + key + "' is not defined");
		return (T) value;
	}

	private static void loadProperties(InputStream in, String errorMsg) {
		Properties props = new Properties();
		try {
			props.load(in);
		} catch (Exception e) {
			System.err.println(errorMsg);
			return;
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (Exception ignored) {
				}
		}

		for (String key : PROPS.keySet()) {
			VALUES.put(key, getValue(props, key, VALUES.get(key), PROPS.get(key)));
		}
	}

	private static Object getValue(Properties props, String key, Object defaultValue, int type) {
		String value = props.getProperty(key);
		switch (type) {
		case TYPE_STRING:
			return value == null ? defaultValue : value;
		case TYPE_INT:
			return ParseUtils.parseInt(value, defaultValue == null ? 0 : (Integer) defaultValue);
		case TYPE_BOOLEN:
			return ParseUtils.parseBoolean(value, defaultValue == null ? false : (Boolean) defaultValue);
		case TYPE_COLOR:
			return ParseUtils.parseColor(value, defaultValue == null ? 0x00000000 : (Integer) defaultValue);
		default:
			throw new RuntimeException("Unknown property type");
		}
	}

}
