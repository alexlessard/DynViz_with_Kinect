package com.dyndns.dynviz.ui;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.media.opengl.GL;

/*
 * GLSLShaderUtil.java
 * ShaderUtil Component
 * Created on May 17, 2006, 8:05 AM
 */

/**
 * @author Chris "Crash0veride007" Brown crash0veride007@gmail.com https://jglmark.dev.java.net/
 */

public class GLSLShaderUtil {
	public static void CheckShaderExtensions(GL gl) {
		System.out.println("Extensions: \n" + gl.glGetString(GL.GL_EXTENSIONS) + "\n");
		if (!gl.isExtensionAvailable("GL_ARB_vertex_program") && !gl.isExtensionAvailable("GL_ARB_fragment_program")) {
			System.out.println("ARB_vertex_shader extension is not supported.");
			System.out.println("ARB_fragment_shader extension is not supported.");
			System.exit(-1);
		} else if (gl.isExtensionAvailable("GL_ARB_vertex_shader") && gl.isExtensionAvailable("GL_ARB_vertex_shader")) {
			System.out.println("ARB_vertex_shader extension is supported continuing!");
			System.out.println("ARB_fragment_shader extension is supported continuing!");
		}
	}

	public static int InitVertexShaderID(GL gl) {
		int AssignedID;
		AssignedID = gl.glCreateShader(GL.GL_VERTEX_SHADER);
		CheckGLError(gl, "InitVertexShaderID", "glCreateShaderObjectARB");
		System.out.println("Vertex Shader has been assigned ID: " + AssignedID);
		return AssignedID;
	}

	public static int InitFragmentShaderID(GL gl) {
		int AssignedID;
		AssignedID = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
		CheckGLError(gl, "InitFragmentShaderID", "glCreateShaderObjectARB");
		System.out.println("Fragment Shader has been assigned ID: " + AssignedID);
		return AssignedID;
	}

	public static int InitShaderProgramID(GL gl) {
		int AssignedID;
		AssignedID = gl.glCreateProgram();
		CheckGLError(gl, "InitShaderProgramID", "glCreateProgramObjectARB");
		System.out.println("Shader Program has been assigned ID: " + AssignedID);
		return AssignedID;
	}

	public static void CompileVertexShader(GL gl, int VertexShaderID, String VertexShaderSourceFile) {
		String vshadersource = ReadShaderSourceFile(VertexShaderSourceFile);
		gl.glShaderSourceARB(VertexShaderID, 1, new String[] { vshadersource }, new int[] { vshadersource.length() }, 0);
		CheckGLError(gl, "CompileVertexShader", "glShaderSource");
		gl.glCompileShaderARB(VertexShaderID);
		CheckGLError(gl, "CompileVertexShader", "glCompileShader");
		CheckShaderObjectInfoLog(gl, VertexShaderID);
		IntBuffer compilecheck = IntBuffer.wrap(new int[1]);
		gl.glGetShaderiv(VertexShaderID, GL.GL_COMPILE_STATUS, compilecheck);
		CheckGLError(gl, "CompileVertexShader", "gl.glGetShaderiv(VertexShaderID, GL.GL_COMPILE_STATUS, compilecheck);");
		if (compilecheck.array()[0] == GL.GL_FALSE) {
			System.out.println("A compilation error occured in the Vertex Shader Source!");
			System.exit(-1);
		} else
			System.out.println("Vertex Shader Source Compiled Successfully");
	}

	public static boolean CompileVertexShaderFromString(GL gl, int VertexShaderID, String VertexShaderSource) {
		// String vshadersource = ReadShaderSourceFile(VertexShaderSourceFile);
		gl.glShaderSource(VertexShaderID, 1, new String[] { VertexShaderSource }, new int[] { VertexShaderSource.length() }, 0);
		CheckGLError(gl, "CompileVertexShaderFromString", "glShaderSource");
		gl.glCompileShader(VertexShaderID);
		CheckGLError(gl, "CompileVertexShaderFromString", "glCompileShader");
		CheckShaderObjectInfoLog(gl, VertexShaderID);
		IntBuffer compilecheck = IntBuffer.wrap(new int[1]);
		gl.glGetShaderiv(VertexShaderID, GL.GL_COMPILE_STATUS, compilecheck);
		CheckGLError(gl, "CompileVertexShaderFromString", "glGetShaderiv");
		if (compilecheck.array()[0] == GL.GL_FALSE) {
			System.out.println("A compilation error occured in the Vertex Shader Source!");
			// System.exit(-1);
			return false;
		} else
			System.out.println("Vertex Shader Source Compiled Successfully");
		return true;
	}

	public static void CompileFragmentShader(GL gl, int FragmentShaderID, String FragmentShaderSourceFile) {
		String fshadersource = ReadShaderSourceFile(FragmentShaderSourceFile);
		gl.glShaderSourceARB(FragmentShaderID, 1, new String[] { fshadersource }, new int[] { fshadersource.length() }, 0);
		CheckGLError(gl, "CompileFragmentShader", "glShaderSourceARB");
		gl.glCompileShaderARB(FragmentShaderID);
		CheckGLError(gl, "CompileFragmentShader", "glCompileShaderARB");
		CheckShaderObjectInfoLog(gl, FragmentShaderID);
		IntBuffer compilecheck = IntBuffer.wrap(new int[1]);
		gl.glGetShaderiv(FragmentShaderID, GL.GL_COMPILE_STATUS, compilecheck);
		CheckGLError(gl, "CompileFragmentShader", "glGetShaderiv");
		if (compilecheck.array()[0] == GL.GL_FALSE) {
			System.out.println("A compilation error occured in the Fragment Shader Source!");
			System.exit(-1);
		} else
			System.out.println("Fragment Shader Source Compiled Successfully");
	}

	public static boolean CompileFragmentShaderFromString(GL gl, int FragmentShaderID, String FragmentShaderSource) {
		gl.glShaderSource(FragmentShaderID, 1, new String[] { FragmentShaderSource }, new int[] { FragmentShaderSource.length() }, 0);
		CheckGLError(gl, "CompileFragmentShaderFromString", "glShaderSource");
		gl.glCompileShader(FragmentShaderID);
		CheckGLError(gl, "CompileFragmentShaderFromString", "glCompileShader");
		CheckShaderObjectInfoLog(gl, FragmentShaderID);
		IntBuffer compilecheck = IntBuffer.wrap(new int[1]);
		gl.glGetShaderiv(FragmentShaderID, GL.GL_COMPILE_STATUS, compilecheck);
		CheckGLError(gl, "CompileFragmentShaderFromString", "glGetShaderiv");
		if (compilecheck.array()[0] == GL.GL_FALSE) {
			System.out.println("A compilation error occured in the Fragment Shader Source!");
			// System.exit(-1);
			return false;
		} else
			System.out.println("Fragment Shader Source Compiled Successfully");
		return true;
	}

	public static void LinkShaderProgram(GL gl, int VertexShaderID, int FragmentShaderID, int ShaderProgramID) {
		gl.glAttachShader(ShaderProgramID, VertexShaderID);
		CheckGLError(gl, "LinkShaderProgram", "gl.glAttachObjectARB(ShaderProgramID, VertexShaderID);");
		gl.glAttachShader(ShaderProgramID, FragmentShaderID);
		CheckGLError(gl, "LinkShaderProgram", "gl.glAttachObjectARB(ShaderProgramID, FragmentShaderID);");
		gl.glLinkProgram(ShaderProgramID);
		CheckGLError(gl, "LinkShaderProgram", "gl.glLinkProgramARB(ShaderProgramID);");
		CheckShaderObjectInfoLog(gl, ShaderProgramID);
		// IntBuffer compilecheck = IntBuffer.wrap(new int[1]);
		// gl.glGetProgramiv(ShaderProgramID, GL.GL_COMPILE_STATUS, compilecheck);
		// CheckGLError(gl, "LinkShaderProgram","glGetShaderiv");
		// if ( compilecheck.array()[0] == GL.GL_FALSE ) {
		// System.out.println("A compilation error occured in the Program Source!");
		// System.exit(-1);
		// } else
		// System.out.println("Program Source Compiled Successfully");
	}

	public static String ReadShaderSourceFile(String path) {
		String shadersource = null;
		try {
			File f = new File(path);
			FileReader fr = new FileReader(f);
			int size = (int) f.length();
			char buff[] = new char[size];
			int len = fr.read(buff);
			shadersource = new String(buff, 0, len);
			fr.close();

		} catch (IOException e) {
			System.err.println(e);
			System.exit(-1);
		}
		return shadersource;
	}

	public static void CheckShaderObjectInfoLog(GL gl, int ID) {
		int[] check = new int[1];
		gl.glGetObjectParameterivARB(ID, GL.GL_OBJECT_INFO_LOG_LENGTH_ARB, check, 0);
		int logLength = check[0];
		if (logLength <= 1) {
			System.out.println("Shader Object Info Log is Clean");
			return;
		}
		byte[] compilecontent = new byte[logLength + 1];
		gl.glGetInfoLogARB(ID, logLength, check, 0, compilecontent, 0);
		String infolog = new String(compilecontent);
		System.out.println("\nInfo Log of Shader Object ID: " + ID);
		System.out.println("--------------------------");
		System.out.println(infolog);
		System.out.println("--------------------------");
	}

	public static int AllocateUniform(GL gl, int ID, String name) {
		int uloc = gl.glGetUniformLocationARB(ID, name);
		if (uloc == -1) {
			throw new IllegalArgumentException("The uniform \"" + name + "\" does not exist in the Shader Program.");
		} else
			System.out.println("Uniform " + name + " was found in the Shader Program and allocated.");
		return uloc;
	}

	public static void CheckGLError(GL gl, String callFunc, String glFunction) {
		int err = gl.glGetError();
		if (err != GL.GL_NO_ERROR) {
			System.out.println(callFunc + ": " + glFunction + " error: " + Integer.toString(err));
			System.exit(-1);
		}
	}
}
