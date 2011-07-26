import processing.opengl.*;
import javax.media.opengl.*;
import com.sun.opengl.util.BufferUtil;
import java.nio.*;


public class Textures
{
   
    public void LoadTextures()
    {
        
        this.globeTex = loadImage(this.GTname);
        //println("globeTex "+globeTex);
        this.maskTex = loadImage(this.MTname);
        
        
        bTexsLoaded = true;
    }
    public void BindTextures(GL gl)
    {
        if (initMultitexture(gl))
            println("Multitexture init successfull!");
        else
        {            
            println("Multitexture init FAIL!");
            System.exit(-1);
        }
        println("Texels "+maxTexelUnits[0]);
        
        this.globeTex.loadPixels();        
        this.globePixels = IntBuffer.wrap(this.globeTex.pixels);
        this.globePixels.rewind();
        
        this.maskTex.loadPixels();
        this.maskPixels = IntBuffer.wrap(maskTex.pixels);
        this.maskPixels.rewind();
        
        this.textures = new int[2];
        gl.glGenTextures(2, this.textures, 0);
        //Globe Texture
        gl.glBindTexture(GL.GL_TEXTURE_2D, this.textures[0]);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, this.globeTex.width, this.globeTex.height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, this.globePixels);
        //Mask texture
        gl.glBindTexture(GL.GL_TEXTURE_2D, this.textures[1]);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, this.maskTex.width, this.maskTex.height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, this.maskPixels);
    }
    
    private boolean initMultitexture(GL gl) {

        String extensions;
        extensions = gl.glGetString(GL.GL_EXTENSIONS);                // Fetch Extension String

        int multiTextureAvailable = extensions.indexOf("GL_ARB_multitexture");
        int textureEnvCombineAvailable = extensions.indexOf("GL_ARB_texture_env_combine");
        if (multiTextureAvailable != -1             // Is Multitexturing Supported?
                && __ARB_ENABLE                                            // Override-Flag
                && textureEnvCombineAvailable != -1) {// Is texture_env_combining Supported?
            gl.glGetIntegerv(GL.GL_MAX_TEXTURE_UNITS, maxTexelUnits, 0);
            return true;
        }
        multitextureEnabled = false;                                        // We Can't Use It If It Isn't Supported!
        return false;
    }
    
    
    
    // Accessors
    public final PImage getGlobeTex()
    {
        return this.globeTex;
    }
    public final PImage getMaskTex()
    {
        return this.maskTex;
    }
    public final IntBuffer getGlobePixels()
    {
        
        return this.globePixels;
    }
    public final IntBuffer getMaskPixels()
    {
       
        return this.maskPixels;
    }
    public final boolean isTexsLoaded()
    {
        return this.bTexsLoaded;
    }
    public final int[] getTextures()
    {
        return textures;
    }
    
    
    //data
    private boolean multitextureEnabled;
    private boolean __ARB_ENABLE = true;
    private boolean bTexsLoaded;
    private PImage globeTex;
    private PImage maskTex;
    private IntBuffer globePixels;
    private IntBuffer maskPixels;
    private int[] textures;
    private int[] maxTexelUnits = new int[1];
    
    //Some constants
    private final String GTname = "earth-glass2.png";
    private final String MTname = "pw.bmp";
}
