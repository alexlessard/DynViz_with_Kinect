package com.dyndns.dynviz.prop;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

public class AnycastHelper {

	private final List<String> prefixes;

	public AnycastHelper() {
		prefixes = getAnycastPrefixList();
	}

	public boolean isAnycast(String ip) {
		for (String prefix : prefixes) {
			if (ip.startsWith(prefix))
				return true;
		}
		return false;
	}

	private static List<String> getAnycastPrefixList() {
		List<String> lines;
		try {
			lines = IOUtils.readLines(Servers.class.getResourceAsStream("/anycast_ip_prefixes.txt"), Charsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return lines;
	}

}
