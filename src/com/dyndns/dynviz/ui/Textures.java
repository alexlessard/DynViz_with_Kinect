package com.dyndns.dynviz.ui;

import java.nio.IntBuffer;

import javax.media.opengl.GL;

import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

import com.dyndns.dynviz.DynViz;
import com.dyndns.dynviz.prop.Props;

public class Textures {

	public static final int GLOBE = 0;
	public static final int MASK = 1;
	public static final int CCOLORS = 2;
	public static final int CBORDERS = 3;
	public static final int DYNLOGO = 4;

	private DynViz parent;

	public Textures(DynViz parent) {
		this.parent = parent;
	}

	public void LoadTextures() {
		String globeTexture = Props.bool(Props.USE_NATURAL_EARTH_TEXTURE) ? GTnaturalName : GTdefaultName;
		this.globeTex = parent.loadImage(globeTexture);
		this.maskTex = parent.loadImage(this.MTname);
		this.dynLogoTex = parent.loadImage(this.LTname);
		this.bordersTex = parent.loadImage(this.BTname);
		bTexsLoaded = true;
	}

	public void BindTextures(PGraphicsOpenGL pgl) {
		if (initMultitexture(pgl.gl))
			System.out.println("Multitexture init successfull!");
		else {
			System.out.println("Multitexture init FAIL!");
			System.exit(-1);
		}
		System.out.println("Texels " + maxTexelUnits[0]);
		int texturesCount = 5;
		// TODO: good texture class
		this.globeTex.loadPixels();
		this.globePixels = IntBuffer.wrap(this.globeTex.pixels);
		this.globePixels.rewind();

		this.maskTex.loadPixels();
		this.maskPixels = IntBuffer.wrap(maskTex.pixels);
		this.maskPixels.rewind();

		this.dynLogoTex.loadPixels();
		this.dynLogoPixels = IntBuffer.wrap(dynLogoTex.pixels);
		this.dynLogoPixels.rewind();

		this.bordersTex.loadPixels();
		this.bordersPixels = IntBuffer.wrap(bordersTex.pixels);
		this.bordersPixels.rewind();

		this.textures = new int[texturesCount];
		pgl.gl.glEnable(GL.GL_TEXTURE_2D);
		pgl.gl.glGenTextures(texturesCount, this.textures, 0);

		// Globe Texture
		pgl.gl.glBindTexture(GL.GL_TEXTURE_2D, this.textures[GLOBE]);
		pgl.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		pgl.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		pgl.gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, this.globeTex.width, this.globeTex.height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE,
				this.globePixels);

		// Mask texture
		pgl.gl.glBindTexture(GL.GL_TEXTURE_2D, this.textures[MASK]);
		pgl.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		pgl.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		pgl.gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		pgl.gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
		pgl.gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, this.maskTex.width, this.maskTex.height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE,
				this.maskPixels);

		// Dyn logo
		pgl.gl.glBindTexture(GL.GL_TEXTURE_2D, this.textures[DYNLOGO]);
		pgl.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		pgl.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		pgl.gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, this.dynLogoTex.width, this.dynLogoTex.height, 0, GL.GL_BGRA,
				GL.GL_UNSIGNED_BYTE, this.dynLogoPixels);

		// Borders
		pgl.gl.glBindTexture(GL.GL_TEXTURE_2D, this.textures[CBORDERS]);
		pgl.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		pgl.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		pgl.gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		pgl.gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
		pgl.gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, this.bordersTex.width, this.bordersTex.height, 0, GL.GL_BGRA,
				GL.GL_UNSIGNED_BYTE, this.bordersPixels);

		pgl.gl.glDisable(GL.GL_TEXTURE_2D);

		// this.maskTex = null;
		this.globeTex = null;
		this.globePixels = null;
		this.dynLogoTex = null;
		this.dynLogoPixels = null;
		this.bordersTex = null;
		this.bordersPixels = null;
	}

	public boolean InitCountries(GL gl) {
		// create 1D texture for countries colors
		// Countries colors
		System.out.println("gl: " + gl);
		this.countriesPixels = IntBuffer.wrap(new int[parent.countries]);
		for (int i = 0; i < parent.countries; i++) {
			countriesPixels.array()[i] = 0xFF000000;
		}

		this.countriesPixels.clear();
		// gl.glEnable(GL.GL_TEXTURE_1D);
		gl.glBindTexture(GL.GL_TEXTURE_1D, this.textures[CCOLORS]);
		gl.glTexParameteri(GL.GL_TEXTURE_1D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_1D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		gl.glTexImage1D(GL.GL_TEXTURE_1D, 0, GL.GL_RGBA, parent.countries, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, countriesPixels);

		// gl.glDisable(GL.GL_TEXTURE_1D);
		return true;
	}

	public boolean CommitCountriesColors(GL gl, int[] cColors) {

		this.countriesPixels = IntBuffer.wrap(cColors);

		IntBuffer bf = IntBuffer.wrap(new int[1]);
		// gl.glEnable(GL.GL_TEXTURE_1D);
		gl.glBindTexture(GL.GL_TEXTURE_1D, this.textures[CCOLORS]);
		gl.glGetTexLevelParameteriv(GL.GL_TEXTURE_1D, 0, GL.GL_TEXTURE_WIDTH, bf);
		gl.glTexSubImage1D(GL.GL_TEXTURE_1D, 0, 0, bf.array()[0], GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, this.countriesPixels);
		// gl.glDisable(GL.GL_TEXTURE_1D);
		return true;
	}

	private boolean initMultitexture(GL gl) {

		String extensions;
		extensions = gl.glGetString(GL.GL_EXTENSIONS); // Fetch Extension String

		int multiTextureAvailable = extensions.indexOf("GL_ARB_multitexture");
		int textureEnvCombineAvailable = extensions.indexOf("GL_ARB_texture_env_combine");
		if (multiTextureAvailable != -1 // Is Multitexturing Supported?
				&& __ARB_ENABLE // Override-Flag
				&& textureEnvCombineAvailable != -1) {// Is texture_env_combining Supported?
			gl.glGetIntegerv(GL.GL_MAX_TEXTURE_UNITS, maxTexelUnits, 0);
			return true;
		}
		return false;
	}

	// Accessors
	public final PImage getGlobeTex() {
		return this.globeTex;
	}

	public final PImage getMaskTex() {
		return this.maskTex;
	}

	public final IntBuffer getGlobePixels() {

		return this.globePixels;
	}

	public final IntBuffer getMaskPixels() {

		return this.maskPixels;
	}

	public final boolean isTexsLoaded() {
		return this.bTexsLoaded;
	}

	public final int getTexture(int textureId) {
		return textures[textureId];
	}

	// data
	private boolean __ARB_ENABLE = true;
	private boolean bTexsLoaded;
	private PImage globeTex;
	private PImage maskTex;
	private PImage dynLogoTex;
	private PImage bordersTex;

	private IntBuffer globePixels;
	private IntBuffer maskPixels;
	private IntBuffer dynLogoPixels;
	private IntBuffer countriesPixels;
	private IntBuffer bordersPixels;
	private int[] textures;
	private int[] maxTexelUnits = new int[1];

	// Some constants
	private final String GTdefaultName = "texture-earth-glass.jpg";
	private final String GTnaturalName = "texture-earth-natural.jpg";
	private final String MTname = "texture-countries-mask.png";
	private final String LTname = "pole_flag.png";
	private final String BTname = "texture-countries-borders.png";
}
