package com.dyndns.dynviz;

import com.dyndns.dynviz.prop.Props;

import processing.core.PApplet;

public class Main {

	public static void main(String args[]) {
		if (args != null && args.length > 0) {
			System.out.println("Loading custom properties: " + args[0]);
			Props.loadProperties(args[0]);
		}

		PApplet.main(new String[] { "--present", "com.dyndns.dynviz.DynViz" });
	}

}
