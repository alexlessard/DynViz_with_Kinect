//package GL.JOGL;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL;
import com.sun.opengl.util.BufferUtil;

/*
 * GLSLShaderUtil.java
 * ShaderUtil Component
 * Created on May 17, 2006, 8:05 AM
 */

/**
 * @author Chris "Crash0veride007" Brown
 * crash0veride007@gmail.com
 * https://jglmark.dev.java.net/
 */

public class GLSLShaderUtil {
    private static IntBuffer programBuffer = BufferUtil.newIntBuffer(1);
    private static ByteBuffer fileBuffer = BufferUtil.newByteBuffer(1024 * 10);
    
    public static void CheckShaderExtensions(GL gl) {
        if ( !gl.isExtensionAvailable("GL_ARB_vertex_shader") && !gl.isExtensionAvailable("GL_ARB_fragment_shader") ) {
            System.out.println("ARB_vertex_shader extension is not supported.");
            System.out.println("ARB_fragment_shader extension is not supported.");
            System.exit(-1);
        } else if ( gl.isExtensionAvailable("GL_ARB_vertex_shader") && gl.isExtensionAvailable("GL_ARB_vertex_shader") ) {
            System.out.println("ARB_vertex_shader extension is supported continuing!");
            System.out.println("ARB_fragment_shader extension is supported continuing!");
        }
    }
    
    public static int InitVertexShaderID(GL gl) {
        int AssignedID;
        AssignedID = gl.glCreateShaderObjectARB(GL.GL_VERTEX_SHADER_ARB);
        System.out.println("Vertex Shader has been assigned ID: "+AssignedID);
        return AssignedID;
    }
    
    public static int InitFragmentShaderID(GL gl) {
        int AssignedID;
        AssignedID = gl.glCreateShaderObjectARB(GL.GL_FRAGMENT_SHADER_ARB);
        System.out.println("Fragment Shader has been assigned ID: "+AssignedID);
        return AssignedID;
    }
    
    public static int InitShaderProgramID(GL gl) {
        int AssignedID;
        AssignedID = gl.glCreateProgramObjectARB();
        System.out.println("Shader Program has been assigned ID: "+AssignedID);
        return AssignedID;
    }
    
    public static void CompileVertexShader(GL gl, int VertexShaderID, String VertexShaderSourceFile) {
        String vshadersource = ReadShaderSourceFile(VertexShaderSourceFile);
        gl.glShaderSourceARB(VertexShaderID,1,new String[]{vshadersource},new int[]{vshadersource.length()},0);
        gl.glCompileShaderARB(VertexShaderID);
        CheckShaderObjectInfoLog(gl, VertexShaderID);
        int[] compilecheck = new int[1];
        gl.glGetObjectParameterivARB(VertexShaderID,GL.GL_OBJECT_COMPILE_STATUS_ARB,compilecheck,0);
        if ( compilecheck[0] == GL.GL_FALSE ) {
            System.out.println("A compilation error occured in the Vertex Shader Source File: "+VertexShaderSourceFile);
            System.exit(-1);

        } else
            System.out.println("Vertex Shader Source File: "+VertexShaderSourceFile+" Compiled Successfully");
    }
    
    
    public static boolean CompileVertexShaderFromString(GL gl, int VertexShaderID, String VertexShaderSource) {
        //String vshadersource = ReadShaderSourceFile(VertexShaderSourceFile);
        gl.glShaderSourceARB(VertexShaderID,1,new String[]{VertexShaderSource},new int[]{VertexShaderSource.length()},0);
        gl.glCompileShaderARB(VertexShaderID);
        CheckShaderObjectInfoLog(gl, VertexShaderID);
        int[] compilecheck = new int[1];
        gl.glGetObjectParameterivARB(VertexShaderID,GL.GL_OBJECT_COMPILE_STATUS_ARB,compilecheck,0);
        if ( compilecheck[0] == GL.GL_FALSE ) {
            System.out.println("A compilation error occured in the Vertex Shader Source!");
            //System.exit(-1);
            return false;
        } else
            System.out.println("Vertex Shader Source Compiled Successfully");
        return true;
    }
    
    public static void CompileFragmentShader(GL gl, int FragmentShaderID, String FragmentShaderSourceFile) {
        String fshadersource = ReadShaderSourceFile(FragmentShaderSourceFile);
        gl.glShaderSourceARB(FragmentShaderID,1,new String[]{fshadersource},new int[]{fshadersource.length()},0);
        gl.glCompileShaderARB(FragmentShaderID);
        CheckShaderObjectInfoLog(gl, FragmentShaderID);
        int[] compilecheck = new int[1];
        gl.glGetObjectParameterivARB(FragmentShaderID,GL.GL_OBJECT_COMPILE_STATUS_ARB,compilecheck,0);
        if ( compilecheck[0] == GL.GL_FALSE ) {
            System.out.println("A compilation error occured in the Fragment Shader Source File: "+FragmentShaderSourceFile);
            System.exit(-1);
        } else
            System.out.println("Fragment Shader Source File: "+FragmentShaderSourceFile+" Compiled Successfully");
    }
    
    public static boolean CompileFragmentShaderFromString(GL gl, int FragmentShaderID, String FragmentShaderSource) {
        //String fshadersource = ReadShaderSourceFile(FragmentShaderSourceFile);
        gl.glShaderSourceARB(FragmentShaderID,1,new String[]{FragmentShaderSource},new int[]{FragmentShaderSource.length()},0);
        gl.glCompileShaderARB(FragmentShaderID);
        CheckShaderObjectInfoLog(gl, FragmentShaderID);
        int[] compilecheck = new int[1];
        gl.glGetObjectParameterivARB(FragmentShaderID,GL.GL_OBJECT_COMPILE_STATUS_ARB,compilecheck,0);
        if ( compilecheck[0] == GL.GL_FALSE ) {
            System.out.println("A compilation error occured in the Fragment Shader Source!");
            //System.exit(-1);
            return false;
        } else
            System.out.println("Fragment Shader Source Compiled Successfully");
        return true;
    }
    
    public static void LinkShaderProgram(GL gl, int VertexShaderID, int FragmentShaderID, int ShaderProgramID) {
        gl.glAttachObjectARB(ShaderProgramID, VertexShaderID);
        gl.glAttachObjectARB(ShaderProgramID, FragmentShaderID);
        gl.glLinkProgramARB(ShaderProgramID);
        CheckShaderObjectInfoLog(gl, ShaderProgramID);
        int[] linkcheck = new int[1];
        gl.glGetObjectParameterivARB(ShaderProgramID,GL.GL_OBJECT_LINK_STATUS_ARB,linkcheck,0);
        if ( linkcheck[0] == GL.GL_FALSE ) {
            System.out.println("A Linking error occured in the Shader program ID: "+ShaderProgramID);
            System.exit(-1);
        } else
            System.out.println("Shader Program ID:"+ShaderProgramID+" Linked Successfully");
    }
    
    public static String ReadShaderSourceFile(String path) {
        String shadersource = null;
        try {
            File f = new File(path);
            FileReader fr = new FileReader(f);
            int size = (int) f.length();
            char buff[] = new char[size];
            int len =  fr.read(buff);
            shadersource = new String(buff,0,len);
            fr.close();
    
        } catch(IOException e) {
            System.err.println(e);
            System.exit(-1);
        }
        return shadersource;
    }
    
    public static void CheckShaderObjectInfoLog(GL gl, int ID) {
        int[] check = new int[1];
        gl.glGetObjectParameterivARB(ID,GL.GL_OBJECT_INFO_LOG_LENGTH_ARB,check,0);
        int logLength = check[0];
        if ( logLength <= 1 ) {
            System.out.println("Shader Object Info Log is Clean");
            return;
        }
        byte[] compilecontent = new byte[logLength+1];
        gl.glGetInfoLogARB(ID,logLength,check,0,compilecontent,0);
        String infolog = new String(compilecontent);
        System.out.println("\nInfo Log of Shader Object ID: " + ID);
        System.out.println("--------------------------");
        System.out.println(infolog);
        System.out.println("--------------------------");
    }
    
    public static int AllocateUniform(GL gl, int ID, String name) {
        int uloc = gl.glGetUniformLocationARB(ID, name);
        if ( uloc == -1 ){
            throw new IllegalArgumentException("The uniform \"" + name + "\" does not exist in the Shader Program.");
        } else
            System.out.println("Uniform " + name + " was found in the Shader Program and allocated.");
        return uloc;
    }
}
