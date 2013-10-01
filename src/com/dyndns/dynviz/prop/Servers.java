package com.dyndns.dynviz.prop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

public class Servers {

	public static List<ServerInfo> getServersList() {
		List<String> lines;
		try {
			lines = IOUtils.readLines(Servers.class.getResourceAsStream("/servers.txt"), Charsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		List<ServerInfo> servers = new ArrayList<ServerInfo>();

		int i = 0;
		String[] serverInfoStrs = new String[5];

		for (String line : lines) {
			if (line == null || line.length() == 0)
				continue;
			serverInfoStrs[i] = line;
			i++;
			if (i == 5) {
				i = 0;
				servers.add(parseServer(serverInfoStrs));
			}
		}

		return servers;
	}

	private static ServerInfo parseServer(String[] lines) {
		float lat = ParseUtils.parseFloat(lines[2], 0.0f);
		float lon = ParseUtils.parseFloat(lines[3], 0.0f);
		int color = ParseUtils.parseColor(lines[4], 0xFFFFFFFF);
		return new ServerInfo(lines[1], lines[0], lat, lon, color);
	}

	public static class ServerInfo {

		public final String id;
		public final String name;
		public final float lat, lon;
		public final int color;

		public ServerInfo(String id, String name, float lat, float lon, int color) {
			this.id = id;
			this.name = name;
			this.lat = lat;
			this.lon = lon;
			this.color = color;
		}

	}

}
