import processing.core.*; 
import processing.xml.*; 

import processing.opengl.*; 
import javax.media.opengl.*; 
import javax.media.opengl.glu.*; 
import java.text.SimpleDateFormat; 
import com.sun.opengl.util.texture.*; 
import java.util.*; 
import java.nio.*; 
import java.awt.*; 
import java.text.*; 
import hypermedia.net.*; 
import damkjer.ocd.*; 
import java.io.*; 
import java.io.IOException; 
import java.util.*; 
import java.lang.*; 
import java.net.URL; 
import java.net.*; 
import java.lang.reflect.Method; 
import java.lang.reflect.InvocationTargetException; 
import java.util.Date; 
import java.text.SimpleDateFormat; 
import processing.opengl.*; 
import javax.media.opengl.*; 
import com.sun.opengl.util.BufferUtil; 
import java.nio.*; 
import processing.core.PApplet; 
import java.awt.event.MouseEvent; 
import processing.opengl.*; 
import javax.media.opengl.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class dynviz extends PApplet {

//.64863515 - 70ms










	// UDP Socket
		// Camera

static int SHADER_MODE_COUNTRIES = 0;
static int SHADER_MODE_SELECTION = 1;
static int SHADER_MODE_SELECT_ONE = 2;


//Constants

// CHANGE HERE TO MANIPULATE THE QPS AND QPS HISTORY DISPLAYED
int QPS_MULTIPLIER = 40;

Float fNaN = new Float(0.0f/0.0f);

//timer
long startShowing = 0;
boolean bShowTimingPanel = false;



// KEYS
boolean bCTRL = false;
boolean bALT = false;
boolean bSHIFT = false;
boolean bTAB = false;

long qqp = 0;
long quePS = 1;
// Scene Setup
// 1280 x 720 = HD 720
// 1360 x 768 = Panasonic 32''
// 1920 x 1200 = Dell 27''
// 0 x 0 for auto full screen
int SCREEN_WIDTH = 0;
int SCREEN_HEIGHT = 0;

//Global statistic
float avTimeGl = 0.0f;
float avTimeAs = 0.0f;
float avTimeEu = 0.0f;
float avTimeNA = 0.0f;

float tmpTimeGl = 0.0f;
float tmpTimeAs = 0.0f;
float tmpTimeEu = 0.0f;
float tmpTimeNA = 0.0f;
Integer tmpQPSEu = 0;
Integer tmpQPSAs = 0;
Integer tmpQPSNA = 0;
Integer tmpQPSGl = 0;

Integer qpsGl = 0;
Integer qpsAs = 0;
Integer qpsEu = 0;
Integer qpsNA = 0;
TimeSlider statTimeSlider;
TimeSlider shadTimeSlider;
TimeSlider servTimeSlider;

boolean bCanUseGlobalStat = false;

float angLat = 0.0f;
float angLon = 0.0f;

float MaxPower = 100.0f;
float MaxDist = 1.0f/6.0f*PI;
float Lmax = sqrt(MaxDist*MaxDist+MaxPower*MaxPower);
float QTime1ms = 70.0f/0.64863515f;

//  GradienWheel panel
int gpX = width-300, gpY = 300;
ColorBlock cbQStart, cbQEnd, cbSStart, cbSEnd, cbSZero;
ColorBlock cbSelected, cbHighlighted;
TimeSlider minSlider, maxSlider;
PImage imgColorWheel;

//  fps
boolean bFPS = false;
float fps = 0.0f;
long fpsStartTime;
float now = 0.0f;
float then = 0.0f;

// Wiimote
int m_x = 0;
int m_y = 0;
int m_z = 0;
float m_r = 0;

int tm_x = 0;
int tm_y = 0;
int tm_z = 0;
float tm_r = 0;
Vector3D rc;

// Globals (they change)
double mvMatrix[] = new double[16];
Vector3D camPos = new Vector3D();
float sizeQueries = 4.0f;
float sizeShadow  = 4.0f;
float sizeLines   = 4.0f;

float sizeTail       = 128.0f;
float tailAlphaBegin = 0.0f;
float tailAlphaEnd   = 255.0f;
boolean bTails       = true;

boolean serverSwitch = false;

float burstSpeed = 1.0f;

int CAST_UNI  = 0;
int CAST_ANY  = 1;
int CAST_GRAD = 2;
int CAST_COUNTRY = 3;
int CAST_CIRCLE = -3;
int CAST_BOTH = 4;

final int ZONE_EU = 0;
final int ZONE_NA = 1;
final int ZONE_AS = 2;
final int ZONE_UN = -1;


int CAST = CAST_BOTH;   // 0 = Unicast, 1 = Anycast, 2 = Both

int midX = 0;
int midY = 0;
boolean bListen = true;
boolean bPerlUnicast = true;
boolean bStartPerlAutomatically = false;
boolean bSiteToggle = false;

float sightDist;

HashMap showSites = new HashMap();

//String helpText = "LEFT ... -X Velocity\nRIGHT .. +X Velocity\nDOWN ... -Y Velocity\nUP ..... +Y Velocity\n \na ...... Zoom In\nz ...... Zoom Out\nSPACE .. Breaks\nMOUSE1 . +-XY Velocity\nMOUSE2 . Roll\nMOUSE3 . Yaw / Pitch\n \nG ......... Display Globe\nQ .. Display Query Bursts\nL ... Display Query Lines\nS . Display Query Shadows\nR .......... Reset Camera\n \n1 ...... Legend Panel\n2 ...... Clock Panel\n3 ...... Help Panel (me!)\n";
//String helpText = "LEFT ........ -X Velocity\nRIGHT ....... +X Velocity\nDOWN ........ -Y Velocity\nUP .......... +Y Velocity\n\na ............... Zoom In\nz .............. Zoom Out\nSPACE ............ Breaks\nMOUSE1 .... +-XY Velocity\nMOUSE2 ............. Roll\nMOUSE3 ...... Yaw / Pitch\n\nG ......... Display Globe\nQ .. Display Query Bursts\nL ... Display Query Lines\nS . Display Query Shadows\nR .......... Reset Camera\n\n1 .......... Legend Panel\n2 ........... Clock Panel\n3 ...... Help Panel (me!)\n5 ........... Color Wheel\n6 ......... Servers\' Time\n7 .. Servers\' Stat. Boxes\n8 ...... Global Statistic\n9 . In/Dest. Count./Serv.\n0 .... Swtch Real/Optimal\n\nDELETE .. Compense on/off\nCTRL+1 ... Cntries\' popup\nCTLR+2 .. Reset Statistic\nCTRL+6 ..... Time Configs\n\nD .......... Country mode\nSHIFT+MOUSE1 . Cntry sel.\nCTRL+MOUSE1 ... Srvr sel.\n";
String helpText = "LEFT ........ -X Velocity\nRIGHT ....... +X Velocity\nDOWN ........ -Y Velocity\nUP .......... +Y Velocity\n\n` ........ Mode switching\n\na ............... Zoom In\nz .............. Zoom Out\nSPACE ............ Breaks\nMOUSE1 .... +-XY Velocity\nMOUSE2 ............. Roll\nMOUSE3 ...... Yaw / Pitch\n\nG ......... Display Globe\nQ .. Display Query Bursts\nL ... Display Query Lines\nS . Display Query Shadows\nR .......... Reset Camera\nF . on/off queries' alpha\nH ...... Help Panel (me!)\n\n1 .......... Legend Panel\n2 ........... Clock Panel\n3 ...... Save added Srvrs\n4 .... Global QPS history\n5 ........... Color Wheel\n6 ......... Servers\' Time\n7 .. Servers\' Stat. Boxes\n8 ...... Global Statistic\n9 . In/Dest. Count./Serv.\n0 .... Swtch Real/Optimal\nDELETE .. Compense on/off\nCTRL+1 ... Cntries\' popup\nCTLR+2 .. Reset Statistic\nCTRL+3 ...... Display FPS\nCTRL+4 ..... Time Configs\nSHIFT+MOUSE1 . Cntry sel.\nCTRL+MOUSE1 ... Srvr sel.\nALT+MOUSE1 ..... Srvr add\n\n. .. Dec. gradient radius\n/ .. Inc. gradient radius\n";
/*
LEFT ........ -X Velocity\n
 RIGHT ....... +X Velocity\n
 DOWN ........ -Y Velocity\n
 UP .......... +Y Velocity\n
 \n
 ` ........ Mode switching\n
 \n
 a ............... Zoom In\n
 z .............. Zoom Out\n
 SPACE ............ Breaks\n
 MOUSE1 .... +-XY Velocity\n
 MOUSE2 ............. Roll\n
 MOUSE3 ...... Yaw / Pitch\n
 \n
 G ......... Display Globe\n
 Q .. Display Query Bursts\n
 L ... Display Query Lines\n
 S . Display Query Shadows\n
 R .......... Reset Camera\n
 F . on/off queries' alpha\n
 H ...... Help Panel (me!)\n
 \n
 1 .......... Legend Panel\n
 2 ........... Clock Panel\n
 3 ...... Save added Srvrs\n
 4 .... Global QPS history\n
 5 ........... Color Wheel\n
 6 ......... Servers\' Time\n
 7 .. Servers\' Stat. Boxes\n
 8 ...... Global Statistic\n
 9 . In/Dest. Count./Serv.\n
 0 .... Swtch Real/Optimal\n
 DELETE .. Compense on/off\n
 CTRL+1 ... Cntries\' popup\n
 CTLR+2 .. Reset Statistic\n
 CTRL+3 ...... Display FPS\n
 CTRL+4 ..... Time Configs\n 
 SHIFT+MOUSE1 . Cntry sel.\n
 CTRL+MOUSE1 ... Srvr sel.\n
 ALT+MOUSE1 ..... Srvr add\n
 /n
 . .. Dec. gradient radius\n
 / .. Inc. gradient radius\n
 
 
 
 
 
 */

float helpWidth = 0.0f, helpHeight = 0.0f;

Integer qps = 0;
Integer currentQPS = 0;

float qpsWidth = 200.0f;
float qpsHeight = 230.0f;
float qpsMax = 0.0f, qpsMin = 0.0f;

int qpsAgeWindow = 3600;
int qpsMaxAge = qpsAgeWindow, qpsMinAge = qpsAgeWindow;
//ArrayList qpsHistory;

PFont fontTime, fontDate, fontHelp, fontLabel, fontLabel2;

int panelAlpha = 200;
int statConfPH = 0;
int[] colors;

Vector3D vAxisX = new Vector3D(1, 0, 0);
Vector3D vAxisY = new Vector3D(0, 1, 0);
Vector3D vAxisZ = new Vector3D(0, 0, 1);

int UDP_SERVER_PORT = 24642;
int UDP_CLIENT_PORT = 24642;
String UDP_CLIENT = "127.0.0.1";
Thread t;

// Camera variables

boolean zoomingIn = false;
boolean zoomingOut = false;

HandyCam hcam;

float rotationX = 323.28f;//40;  //43
float rotationY = 5.47f;//-95;  //-72

float targetRotX = rotationX;
float targetRotY = rotationY;
float targetZ = -2000;

float velocityX = 0.0f;
float velocityY = 0.05f;
float velocityZ = 0.0f;

float camX = 0;
float camY = 0;
float camZ = -1750;

//float attenuation_far[] = {1.0f, -0.01f, -0.000001f};	// Points scale with distance
float attenuation_far[] = {
  1.0f, -0.01f, -0.000001f
};	// Points scale with distance

float attenuation_near[] = {
  1.0f, 0.0f, 0.0f
};			// Points keep size - best when zoomed in
float attenuation[] = attenuation_far;

// OpenGL stuff
ArcBall arcball;
float globeRadius = 800;

boolean openGL = true;
PGraphicsOpenGL pgl;
GL gl;
GLU glu;

// Colors wheels
int queryStartColor;
int queryEndColor;
int shaderStartColor;
int shaderEndColor;

// "Server" list
ArrayList servers;
ArrayList adServ;
int fixSrvrs;
int cint = 0;

// Network code
UDP udp;

// Rendering Queue
java.util.List queries;
java.util.List qpsHistory;
java.util.List qpsPrep;

//SHADER
int ProgramID = 0;
int ShaderID = 0;
int Shader2ID = 0;
boolean bUseShader = false;
boolean bShaderDataUpdate = true;
float countryAv[];
float optimalAv[];
float optimalAvT[];
float countryQPS[];
float countryAvT[];
float countryQPST[];
String countriesNames[];
int maskID = 1251;
Textures texs;
int countries;
float minAvT = 25f, maxAvT = 125f;


Vector3D selectionColor = new Vector3D(1.0f, 0.0f, 0.0f);
Vector3D highlightColor = new Vector3D(0.0f, 1.0f, 0.0f);
int selectedCountry = -1;
int highlightedCountry = -1;
int selectedCountries[];
Server selectedServer = null;
int countriesTime = 0;

// User options
boolean bGlobe    = true;
boolean bQueries  = true;
boolean bLines    = true;
boolean bShadows  = false;
boolean bAlpha    = true;
boolean bQPSSolid = false;
boolean bRotationReparation = true;
boolean bCountrySelection = false;

boolean bOptimalHightlight = false;
boolean bOptimalCountry = false;

boolean bPanel1   = true;        //servers list
boolean bPanel2   = true;        //time-date + mode
boolean bPanel3   = false;
boolean bPanel4   = true;        // qps history
boolean bPanel5CW = false;       //color wheel
boolean bPanelSQPS = false;        //servers' curr qps
boolean bPanelSHis = false;        //servers' statistic
boolean bPanelGlSt = false;        // global stat
boolean bPanelC2S = false;        //countries to server list
boolean bCountriesPopup = false;
boolean bStatConfigs = false;
int queryScale    = 400;

String footerDynDNS = "DynDNS Realtime Unicast Traffic";
String footerDynect = "Dynect Realtime Anycast Traffic";
String footerOptima = "Dynect Optimal Anycast Traffic";
String footerDynINC = "Dyn Inc. Realtime Traffic";
String footerGrad = "Dynect Gradient Mode";
String footerCount = "Dynect Country Mode";
String footerOpCount = "Dynect Optimal Country Mode";
String footerCirc = "Circle";

PImage imgLogoDynDNS;
PImage imgLogoDynect;

boolean drawing = false;
boolean needToPauseDrawing = false;

PCAPSender pcapSender = new PCAPSender(this);
boolean canUseSender = true;

// The crux
public void setup()
{
  int ColorSize = 17; //12;
  // Populate Globals
  int[] _colors = new int[] {
    0xffE34F3A,0xff00FF00,0xff0000FF,0xff50FFFF,0xffAF00AF,0xffAFAF00,0xff8CD503,0xffFF5F00,0xff20AF7F,0xff5F00FF,0xffB00A30, 0xff829030,0xff20ABBB,0xff20BBCB,0xff20BCBB,0xff20CBBB,0xff20BABB
  };
  colors = new int[ColorSize];
  for (int i = 0; i< ColorSize; i++)
  {
    int ind = round(random(0,ColorSize - 1));
    while (_colors[ind] == 0xff000000)
      ind = (ind+1)%ColorSize;
    //            int ind = i;
    colors[i] = _colors[ind];
    _colors[ind] = 0xff000000;
  }
  size(SCREEN_WIDTH, SCREEN_HEIGHT, OPENGL);

  imgLogoDynDNS = loadImage("logo-dyndns.png");
  imgLogoDynect = loadImage("logo-dynect.png");
  imgColorWheel = loadImage("ColorWheel.bmp");

  InitTextures();
  gpX = width - 300;
  gpY = 300;
  cbQStart = new ColorBlock(gpX+55, gpY-60, 40, 20, 0xff00FF00); 
  cbQEnd= new ColorBlock(gpX+205, gpY-60, 40, 20, 0xffFF0000);
  cbSStart = new ColorBlock(gpX+55, gpY-60, 40, 20, 0xff00FF00); 
  cbSEnd= new ColorBlock(gpX+205, gpY-60, 40, 20, 0xffFF0000); 
  cbSZero = new ColorBlock(gpX+55, gpY-30, 40, 20, 0xffFFFFFF);
  cbSelected = new ColorBlock(gpX+160, gpY-30, 40, 20, 0xffFF0000); 
  cbHighlighted = new ColorBlock(gpX+255, gpY-30, 40, 20, 0xff00FF00);
  minSlider = new TimeSlider("Min", gpX+60, gpY-90, 150, 10, 0, 25, 125); 
  maxSlider = new TimeSlider("Max", gpX+60, gpY-110, 150, 10, 25, 125, 200);
  statTimeSlider = new TimeSlider("Zones", 10, 10, 200, 15, 1, 2, 60); 
  shadTimeSlider = new TimeSlider("Countries", 10, 10, 200, 15, 1, 2, 60); 
  servTimeSlider = new TimeSlider("Servers", 10, 10, 200, 15, 1, 2, 60);
  servers = new ArrayList();
  adServ = new ArrayList();
  qpsHistory = Collections.synchronizedList(new ArrayList());
  qpsPrep = Collections.synchronizedList(new ArrayList());
  queries = Collections.synchronizedList(new ArrayList());
  // Commented by HexTa for zones checking
  servers.add(new Server(37.444660f,-122.160794f,         -337.592660812806f,  -486.676757393927f,  537.751735634449f,   "Palo Alto, CA", "pao", colors[0]));
  servers.add(new Server(41.884150f, -87.632409f,           21.7921117976071f, -535.096927294307f,  594.303274653263f,   "Chicago, IL",   "ord", colors[1]));
  servers.add(new Server(39.051631f, -77.483151f,          135.08319681082f,   -502.913570974951f,  607.314144466101f,   "Ashburn, VA",   "iad", colors[2]));
  servers.add(new Server(40.731970f, -74.174184f, 0,  0, 0,                                                                    "Newark, NJ",    "ewr", colors[3]));
  servers.add(new Server(50.111512f, 8.680506f,    507.10128626594f,   -613.881367890233f,  -77.4464435893984f,  "Frankfurt",     "fra", colors[4]));
  servers.add(new Server(51.500152f, -0.126236f,   498.008839633618f,  -626.087846655222f,   -1.09723170079356f, "London",        "lon", colors[5]));
  servers.add(new Server(52.373120f, 4.893195f,   486.600749573312f,  -633.629140486068f,  -41.6391983781732f,  "Amsterdam1",     "ams", colors[6]));
  servers.add(new Server(22.396428f, 114.109497f, -302.135916159577f,  -304.810189190118f, -675.133051133106f,   "Hong Kong",     "hkg", colors[7]));
  servers.add(new Server(34.053490f, -118.245319f,         -337.592660812806f,  -486.676757393927f,  537.751735634449f,   "Los Angeles, CA",   "lax", colors[8]));
  servers.add(new Server(40.714550f, -74.007124f,           21.7921117976071f, -535.096927294307f,  594.303274653263f,   "NYC", "nyc", colors[9]));
  servers.add(new Server(52.373120f, 4.893195f,          135.08319681082f,   -502.913570974951f,  607.314144466101f,   "Amsterdam2",         "spl", colors[10]));
  servers.add(new Server(35.670479f, 139.740921f,    507.10128626594f,   -613.881367890233f,  -77.4464435893984f,  "Tokyo",             "tyo", colors[11])); 
  //new servers... just making sure the color wheel has been update
  if(ColorSize > 16)
  {
    servers.add(new Server(25.782648f, -80.193157f,  0,  0, 0, "Miami, FL", "mia", colors[12]));
    servers.add(new Server(32.800325f, -96.820064f,  0,  0, 0, "Dallas, TX", "dal", colors[13]));
    servers.add(new Server(47.614343f, -122.338507f,  0,  0, 0, "Seattle, WA", "sea", colors[14]));
    servers.add(new Server(1.352083f, 103.819836f,  0,  0, 0, "Singapore", "sin", colors[15]));
    servers.add(new Server(-33.920581f, 151.188056f,  0,  0, 0, "Sydney", "syd", colors[16])); 
  }
  else
  {
    println("Need to update colors and ColorSize to support more servers"); 
  }
  //servers.add(new Server(35.670479, 139.740921,    507.10128626594,   -613.881367890233,  -77.4464435893984,  "eu1",             "eu1", (color)#FFFFFF));     //server for tests
  // 37\u00b046\u203245.48\u2033N 122\u00b025\u20329.12\u2033W SFO
  fixSrvrs = servers.size();

  for (Iterator itServer = servers.iterator(); itServer.hasNext();) {
    Server s = (Server)itServer.next();
    showSites.put(s.id, s);
  }

  addMouseWheelListener(new java.awt.event.MouseWheelListener() {
    public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
      mouseWheel(evt.getWheelRotation());
    }
  }
  );

  fontTime =   loadFont("GillSansMT-64.vlw");
  fontDate =   loadFont("GillSansMT-24.vlw");
  fontLabel2 = loadFont("GillSansMT-Bold-14.vlw");
  fontLabel =  loadFont("GillSansMT-12.vlw");
  fontHelp =   loadFont("CourierNewPSMT-11.vlw");

  midX = width / 2;
  midY = height / 2;


  hint(ENABLE_OPENGL_2X_SMOOTH);
  //	smooth();

  frame.setLocation(0,0);

  frameRate(75);
  background(255, 255, 32);
  noCursor();


  pgl = (PGraphicsOpenGL) g;
  glu = new GLU();

  // Set Up camera
  hcam = new HandyCam(0.0f, 0.0f, -camZ);
  hcam.cam = new Camera(this, 0.0f, 0.0f, -camZ, 0.0f, 0.0f, 0.0f, 0.5f, 100000.0f);
  hcam.update();

  pgl.gl.glClearDepth(1.0f);                                      // Depth Buffer Setup
  pgl.gl.glEnable(GL.GL_DEPTH_TEST);                              // Enables Depth Testing
  pgl.gl.glDepthFunc(GL.GL_LEQUAL);

  InitShaders();
  // Set up Sphere / Graphics
  initializeSphere(/*"earth-glass2.png"*/);//"pw_good.bmp");

  texs.BindTextures(pgl.gl);
  //                  

  /*
	if (bStartPerlAutomatically) {
   		killPerl();
   		Runnable r = new BlindThread("perl \"" + System.getenv().get("USERPROFILE") + "/My Documents/Processing/dynviz/data/dynviz.pl\" -s");
   		t = new Thread(r);
   		t.start();
   	}
   	*/

  // Set up UDP socket
  if (canUseSender)
  {
    if(!pcapSender.started && pcapSender.openPCAP())
    {      
      //pcapSender.run();
      println("PCAP opened!");
    } 
    else
    {
      println("PCAP opening failed!");
      pcapSender.dispose();
    }
  }
  else
    if (bListen) {
      String listen[] = loadStrings("receive.txt");
      if (listen.length > 0 && listen[0].length() > 6) UDP_CLIENT = listen[0];

      udp = new UDP(this, UDP_SERVER_PORT, UDP_CLIENT);
      udp.listen(true);
    }
}

private void InitShaders()
{
  GLSLShaderUtil.CheckShaderExtensions(pgl.gl);
  if (!pgl.gl.isExtensionAvailable("GL_ARB_vertex_program"))
  {
    println("GL_ARB_vertex_program extension is not available!!Shutting Down");
    System.exit(-1);
  }
  else
    println("GL_ARB_vertex_program extension is available.");

  ShaderID = GLSLShaderUtil.InitVertexShaderID(pgl.gl);
  GLSLShaderUtil.CompileVertexShaderFromString(pgl.gl, ShaderID, SArr2S(loadStrings("vertexshader.txt")));
  //        GLSLShaderUtil.CompileVertexShader(pgl.gl, ShaderID, "D:\\vertexshader.txt");

  Shader2ID = GLSLShaderUtil.InitFragmentShaderID(pgl.gl);
  if (!GLSLShaderUtil.CompileFragmentShaderFromString(pgl.gl, Shader2ID,SArr2S(loadStrings("fragmentshader.txt"))))
  {
    if (!GLSLShaderUtil.CompileFragmentShaderFromString(pgl.gl, Shader2ID,SArr2S(loadStrings("fragmentshader2.txt"))))
      System.exit(-1);
  }
  
  //        GLSLShaderUtil.CompileFragmentShader(pgl.gl, Shader2ID, "D:\\fragmentshader.txt");

  ProgramID = GLSLShaderUtil.InitShaderProgramID(pgl.gl);        
  GLSLShaderUtil.LinkShaderProgram(pgl.gl, ShaderID, Shader2ID,ProgramID);
}

public void init() {
  try {
    frame.removeNotify();
    frame.setUndecorated(true);
    frame.addNotify();  
    frame.setResizable(true);
  } 
  catch(Exception e) {
  }

  if (SCREEN_WIDTH == 0) SCREEN_WIDTH = screen.width;
  if (SCREEN_HEIGHT == 0) SCREEN_HEIGHT = screen.height;
  /*
	try {
   		Robot robot = new Robot();
   		robot.mouseMove(SCREEN_WIDTH - 290, SCREEN_HEIGHT - 50);
   	} catch (Exception e) {}
   	*/
  super.init();
}

public void mouseWheel(int delta)
{
  velocityZ -= delta * 10;
}

public void draw()
{

  drawing = true;

  background(24);

  handleMouse();

  adjustRotation();

  lightSpecular(255, 255, 255);

  ambientLight(28, 28, 28, 0, 0, 1200);
  spotLight(0, 0, 0, 400, 0, 1400, 0, 0, -1, PI/3, 2);
  directionalLight(128, 128, 128, -.5f, 0, -1);

  pgl.gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
  CalculateVisibleRange();

  pgl.gl.glEnable(GL.GL_MULTISAMPLE);
  float lDist = hcam.pos.z;//2*globeRadius;
  camPos.x = lDist*cos(radians(-rotationX))*sin(radians(-rotationY));
  camPos.z = lDist*cos(radians(-rotationX))*cos(radians(-rotationY));
  camPos.y = -lDist*sin(radians(-rotationX));
  //           
  //            float x = lDist*cos(radians(-rotationX))*sin(radians(-rotationY));
  //            float z = lDist*cos(radians(-rotationX))*cos(radians(-rotationY));
  //            float y = -lDist*sin(radians(-rotationX));
  pgl.gl.glBindProgramARB(GL.GL_VERTEX_PROGRAM_ARB, ShaderID);
  pgl.gl.glBindProgramARB(GL.GL_FRAGMENT_PROGRAM_ARB, Shader2ID);
  pgl.gl.glUseProgram(ProgramID);
  pgl.gl.glActiveTexture(GL.GL_TEXTURE1);
  pgl.gl.glBindTexture(GL.GL_TEXTURE_2D, texs.getTextures()[1]);           
  int loc = pgl.gl.glGetUniformLocationARB(ProgramID, "tex1");            
  pgl.gl.glUniform1iARB(loc, 1);
  loc = pgl.gl.glGetUniformLocationARB(ProgramID, "LightPosition");
  pgl.gl.glUniform3fARB(loc, camPos.x, camPos.y, camPos.z);            
  HighlightCountry();
  if (CAST == CAST_COUNTRY)
  {

    loc = pgl.gl.glGetUniformLocationARB(ProgramID, "ShaderMode");
    pgl.gl.glUniform1iARB(loc, SHADER_MODE_COUNTRIES);
    if (bShaderDataUpdate)
    {
      Vector3D cColor;
      for (int i = 0; i<countries ; i++)
      {
        cColor = GetCountryStatColor(i);
        loc = pgl.gl.glGetUniformLocationARB(ProgramID, "color["+i+"]");
        pgl.gl.glUniform3fARB(loc, cColor.x, cColor.y, cColor.z);
      }
      bShaderDataUpdate = false;
    }
  }
  else
  {
    loc = pgl.gl.glGetUniformLocationARB(ProgramID, "ShaderMode");
    pgl.gl.glUniform1iARB(loc, SHADER_MODE_SELECT_ONE);
    loc = pgl.gl.glGetUniformLocationARB(ProgramID, "SelectOne");
    pgl.gl.glUniform1iARB(loc, selectedCountry);

    //                for (int i = 0; i < countries; i++)
    //                {
    //                    loc = pgl.gl.glGetUniformLocationARB(ProgramID, "SelectedCountry["+i+"]");
    //                    if (selectedServer!= null)
    //                        pgl.gl.glUniform1iARB(loc, selectedServer.countriesInc[i]);        // here must be selected country \u0448ndex
    //                    else
    //                        pgl.gl.glUniform1iARB(loc, 0);
    //                    
    //                }
    loc = pgl.gl.glGetUniformLocationARB(ProgramID, "SelectionColor");
    pgl.gl.glUniform3fARB(loc, selectionColor.x, selectionColor.y, selectionColor.z);        // here must be seloction color
  }
  
  loc = pgl.gl.glGetUniformLocationARB(ProgramID, "HighlightOne");
  if (!bCountriesPopup)
    pgl.gl.glUniform1iARB(loc, -1);
  if (bCountriesPopup)
  {
    pgl.gl.glUniform1iARB(loc, highlightedCountry);    
    loc = pgl.gl.glGetUniformLocationARB(ProgramID, "HighlightColor");
    pgl.gl.glUniform3fARB(loc, highlightColor.x, highlightColor.y, highlightColor.z);
  }
  pgl.gl.glActiveTexture(GL.GL_TEXTURE0);

  if (bRotationReparation) 
  {
    setCompenseGlobe();
    if (bGlobe)
      renderGlobe();
    resetCompenseGlobe();
    setGlobe();
  }
  else
  {
    setGlobe();
    if (bGlobe)
      renderGlobe();
  }


  pgl.gl.glUseProgram(0);
  pgl.gl.glActiveTexture(GL.GL_TEXTURE0);            

  pgl.gl.glDisable(GL.GL_MULTISAMPLE);

  gl = pgl.beginGL();

  if (serverSwitch)
  {
    synchronized(queries)
    {
      for (Iterator itQ = queries.iterator(); itQ.hasNext();)
      {
        Query q = (Query)itQ.next();
        q.CalcPotencial();
      }
    }
    serverSwitch = false;
  }


    drawQueries();

//  beginLines(gl, 3.0f);
//          for (int i = 0; i<servers.size(); i++) {
//  		Server s = (Server)servers.get(i);
//  		if (s.display)
//                  line3d(gl, s.pos.x*sizeTail*2, s.pos.y*sizeTail*2, s.pos.z*sizeTail*2, s.pos.x, s.pos.y, s.pos.z, 1.0, s.getColor(), 255, 255);
//  	}    
//  endLines(gl);


beginLines(gl, 3.0f);
          for (int i = fixSrvrs; i<servers.size(); i++) {
  		Server s = (Server)servers.get(i);
  		if (s.display)
                  line3d(gl, s.pos.x*sizeTail*2, s.pos.y*sizeTail*2, s.pos.z*sizeTail*2, s.pos.x, s.pos.y, s.pos.z, 1.0f, s.getColor(), 255, 255);
  	}    
endLines(gl);

  //beginLines(gl, 3.0f);
  //        for (int i = 100; i < 101; i++) {		
  //                line3d(gl, sphereX[i]*globeRadius*1.5, sphereY[i]*globeRadius*1.5, sphereZ[i]*globeRadius*1.5, sphereX[i]*globeRadius, sphereY[i]*globeRadius, sphereZ[i]*globeRadius, 1.0, (color)#FF10DA, 255, 255);
  //	}    
  //endLines(gl);


  // beginPoints(gl);
  //        for (int i =5000; i>0; i-=20)
  //        {
  //          point3d(gl, i, 0, 0, 3.0, #FF0000, 255);
  //          point3d(gl, 0, i, 0, 3.0, #00FF00, 255);
  //          point3d(gl, 0, 0, i, 3.0, #0000FF, 255);          
  //        }
  //        int maxxx = 1000;
  //        for (int i = maxxx; i>0; i-=10)
  //        {
  //          point3d(gl, maxxx, i, 0, 2.0, #FF0000, 128);//XY
  //          point3d(gl, maxxx, 0, i, 2.0, #FF0000, 128);//XZ
  //          point3d(gl, i, maxxx, 0, 2.0, #00FF00, 128);//YX
  //          point3d(gl, 0, maxxx, i, 2.0, #00FF00, 128);//YZ
  //          point3d(gl, i, 0, maxxx, 2.0, #0000FF, 128);//ZX
  //          point3d(gl, 0, i, maxxx, 2.0, #0000FF, 128);//ZY
  //          
  //        }
  //  endPoints(gl);

  resetGlobe();

  lights();
  noLights();

  camera();

  gl.glDisable(gl.GL_DEPTH_TEST);

  gl.glEnable(gl.GL_BLEND);
  gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);

  pgl.endGL();

  drawHud();

  drawFPS();
  drawCursor();

  gl.glDisable(gl.GL_BLEND);
  drawing = false;
}

public void drawCursor() {
  if (bALT || ((bPanel5CW && mouseX>gpX && mouseY<gpY) || (bStatConfigs && mouseX>(width-370) && mouseY<statConfPH)))
  {
    cursor(CROSS);
    return;
  }
  else
    noCursor();    
  if (abs(mouseX - pmouseX) > 0 ||
    abs(mouseY - pmouseY) > 0) {
    ellipseMode(CENTER);
    fill(0, 255);
    ellipse(mouseX, mouseY, 40, 40);
    fill(255, 255);
    ellipse(mouseX, mouseY, 32, 32);
  }
}

public void stop() {
  /*
	println("JAVA: Sending UDP Client STOP message.");
   	try {
   		while (t.isAlive()) {
   			udp.send( "stop", UDP_CLIENT, UDP_CLIENT_PORT );
   
   			if (t.isAlive()) {
   				try {
   					Thread.currentThread().sleep(1000);
   				} catch (InterruptedException ie) {}
   			}
   		}
   
   		killPerl();
   		println("JAVA: UDP Client successfully exited.");
   	} catch (Exception e) {}
   	*/
  super.stop();
}

public void drawHud() {

  if (bPanel1 || bPanel2 || bPanel3 || bPanel4) {
    strokeWeight(1);
    stroke(0);

    if (bPanel2) {
      drawTime();
      drawTitle();
    }

    if (bPanel4) {
      drawQPS();
    }

    if (bPanel1) {
      noStroke();
      fill(0, panelAlpha);
      rect(-1, 0, 220, (servers.size() * 38) + 34);

      int i = 0;
      int c = 0;

      for (Iterator itServer = servers.iterator(); itServer.hasNext();) {
        i++;
        Server s = (Server)itServer.next();

        int y = 16 + (c * 40);

        if (selectedServer == s)
        {
          noFill();
          stroke(255, 255, 0, 255); 
          rect(1, y-5, 218, 40);
        }

        strokeWeight(1);
        stroke(0);

        fill(s.getColor(), s.display ? 255 : 32);
        rect(15, y, 30, 30);

        textFont(fontDate);
        noStroke();
        fill(255, 255, 255, 240);
        textAlign(LEFT, TOP);
        text(s.name, 60, y + 5);

        textFont(fontLabel2);
        fill(0, 200);
        textAlign(CENTER, CENTER);
        text(i, 30, y + 15);

        c++;
      }
//      for (Iterator itServer = adServ.iterator(); itServer.hasNext();) {
//        i++;
//        Server s = (Server)itServer.next();
//
//        int y = 16 + (c * 40);
//
//        strokeWeight(1);
//        stroke(0);
//
//        fill(s.getColor(), s.display ? 255 : 32);
//        rect(15, y, 30, 30);
//
//        textFont(fontDate);
//        noStroke();
//        fill(255, 255, 255, 240);
//        textAlign(LEFT, TOP);
//        text(s.name, 60, y + 5);
//
//        textFont(fontLabel2);
//        fill(0, 200);
//        textAlign(CENTER, CENTER);
//        text(i, 30, y + 15);
//
//        c++;
//      }
    }
    if (bPanel3) {
      drawHelp();
    }
  }
  if (bPanelGlSt)
  {
    drawGlobalStatistic();
  }
  if (bPanelSQPS)
  {
    drawSQPSPanel();
  }
  if (bPanelSHis)
  {
    drawSHisPanel();
  }
  if (bPanel5CW)
  {
    drawCWPanel();
  }
  if (bPanelC2S)
  {
    drawC2SPanel();
  }
  if (bStatConfigs)
  {
    drawStatConfPanel();
  }
  drawCountrysPopup();
  if (bShowTimingPanel)
    drawTimingPanel();
}

public void drawTimingPanel()
{
  stroke(255);
  fill(0, 255);
  int _left = width-260;
  int _top = 4;
  int _w = 250;
  int _h = 80;
  int _sl = PApplet.parseInt((MaxDist/PI)*PApplet.parseFloat(_w-105));
  rect(_left, _top, _w, _h);
  fill(255, 240);
  line(_left+50, _top+20, _left+_w-50,_top+20);
  line(_left+50, _top+20, _left+50, _top+40);
  line(_left+_w-50, _top+20, _left+_w-50, _top+40);

  line(_left+50+_sl, _top+22, _left+_sl+45, _top+35);
  line(_left+50+_sl, _top+22, _left+_sl+55, _top+35);
  line(_left+_sl+45, _top+35, _left+_sl+55, _top+35);  
  textAlign(CENTER, TOP);
  text(nf(MaxDist*QTime1ms, 1, 1)+"ms", _left+(_w/2), _top+50);
  textAlign(RIGHT, TOP);
  text(nf(PI/36.0f*QTime1ms, 1,1)+"ms", _left+55, _top+50);
  textAlign(LEFT, TOP);
  text(nf(PI*QTime1ms, 1,1)+"ms", _left+_w-60, _top+50);
  if ((System.currentTimeMillis()-startShowing)>1800)
    bShowTimingPanel = false;
}

public void drawStatConfPanel()
{
  int winX = width-370;
  int winY = 0;
  int _y = winY + 50;
  statTimeSlider.MoveTo(winX + 90, _y);
  _y += statTimeSlider.GetHeight() + 10;
  shadTimeSlider.MoveTo(winX + 90, _y);
  _y += shadTimeSlider.GetHeight() + 10;
  servTimeSlider.MoveTo(winX + 90, _y);
  _y += shadTimeSlider.GetHeight() + 20;
  statConfPH = _y;
  noStroke();
  fill (0, panelAlpha);
  rect(winX, winY, 370, _y);
  fill(255, 255, 255, 240);
  textAlign(CENTER, TOP);
  textFont(fontDate);

  text("Statistic time configs", winX + 185, 10);    

  statTimeSlider.Draw();
  shadTimeSlider.Draw();
  servTimeSlider.Draw();
}

public void drawCountrysPopup()
{
  if (!bCountriesPopup)
    return;
  //println("bCountriesPopup == true");
  Vector3D retV = ProjectScreenToGlobe(pgl.gl, mouseX, mouseY, hcam.pos);
  if (retV == null)
    return;
  int country = highlightedCountry;
  //println("Country's index is "+country);
  if (country == -1)
    return;
  int popupW = 155*2;
  int popupH = 25*9;
  int left = mouseX+30;
  int top = mouseY+30;
  if (left+popupW > width)
    left = mouseX - popupW - 30;
  if (top+popupH > height)
    top = mouseY - popupH - 30;

  stroke(230);
  fill(0, panelAlpha);
  rect(left, top, popupW, popupH);
  stroke(250);
  line(left+10, top+35, left+popupW-10, top+35);
  fill(255, 255, 255, 240);
  textAlign(LEFT, TOP);
  textFont(fontLabel2, 14);
  String time;
  if (countryAv[country] == -1 || fNaN.equals(countryAv[country]))
    time = "n/a";
  else
    time = nf(countryAv[country], 1, 1);

  if(bOptimalHightlight)
  {
    if (optimalAv[country] == -1 || fNaN.equals(optimalAv[country]))
      time += "/n/a";
    else
      time +="/"+ nf(optimalAv[country], 1, 1);
  }
  text(countriesNames[country]+". Time: "+time, left+20, top+20);

  int x=left+20, y=top+50;
  int real=0, optimal=0;
  for (Iterator itServer = servers.iterator(); itServer.hasNext();) 
  {      
    Server s = (Server)itServer.next();

    if (CAST == CAST_ANY && bOptimalHightlight)
    {
      real += s.countriesInc[country];
      if (s.countriesInc[country] > 0 || s.countriesOIn[country] > 0)
      {
        if (s.countriesInc[country] > 0 && s.countriesOIn[country] > 0)
        {
          fill(0, 255, 0, 240);

          optimal += s.countriesOIn[country];
        }
        else
          if (s.countriesInc[country] > 0 && !(s.countriesOIn[country] > 0))
            fill(255, 0, 0, 240);
          else
            fill(255,255,0,240);
        text(s.name, x, y);
        y += 24;
        if (y>=(top+popupH-10))
        {
          y = top+50;
          x += 150;
        }
      }
    }
    else
      if (s.countriesInc[country] > 0)
      {
        fill(255, 255, 255, 240);
        text(s.name, x, y);
        y += 24;
        if (y>=(top+popupH-10))
        {
          y = top+50;
          x += 150;
        }
      }
  }
  if (CAST == CAST_ANY && bOptimalHightlight)
  {
    if (real == 0 )
      time = "n/a";
    else
      time = nf((PApplet.parseFloat(optimal)/PApplet.parseFloat(real+optimal))*100.0f, 1, 1)+"%";
    fill(255, 255, 255, 240);
    textAlign(RIGHT, TOP);
    text("Op: "+time, left+popupW-5, top+20);
  }    
  //println("Popup is drawn");
}

public void drawC2SPanel()
{
  fill(0, panelAlpha);
  rect(230, 0, width-230, height-50);
  stroke(255);
  line( 240.0f,23.0f, 420.0f, 24.0f);
  noStroke();
  fill(255, 255, 255, 240);
  textAlign(LEFT, TOP);
  textFont(fontLabel2, 14);
  if (!bCountrySelection)
  {
    if (selectedServer == null)
    {
      text("No server selected", 250, 10);
      return;
    }
    int k = 0;
    int x=260, y=27;
    text("Server "+selectedServer.name, 250, 10);

    for (int i = 0; i < countries; i++)
    {  
      if (selectedServer.countriesInc[i] > 0)
      {                
        text(countriesNames[i], x, y);
        y += 24;
        if (y>=(height-50))
        {
          y = 27;
          x += 150;
        }
      }
    }
  }
  else
  {
    if (selectedCountry == -1)
    {
      text("No country selected", 250, 10);
      return;
    }
    int x=260, y=27;
    text("Country "+countriesNames[selectedCountry], 250, 10);

    for (Iterator itServer = servers.iterator(); itServer.hasNext();) 
    {            
      Server s = (Server)itServer.next();
      if (s.countriesInc[selectedCountry] > 0)
      {
        text(s.name, x, y);
        y += 24;
        if (y>=(height-50))
        {
          y = 27;
          x += 150;
        }
      }
    }
  }
}

public void drawGlobalStatistic()
{

  fill(0, panelAlpha);
  rect(width - qpsWidth - 32, qpsHeight + 32, qpsWidth + 32, 140);


  noStroke();
  fill(255, 255, 255, 240);
  textAlign(CENTER, TOP);
  textFont(fontLabel2, 20);
  text("Global Statistic",width - qpsWidth/2 - 32, qpsHeight + 32);

  textAlign(LEFT, TOP);
  textFont(fontLabel2);

  text("Global",width - qpsWidth - 32, qpsHeight + 90);
  text("North America",width - qpsWidth - 32, qpsHeight + 110);
  text("Europe",width - qpsWidth - 32, qpsHeight + 130);
  text("Asia",width - qpsWidth - 32, qpsHeight + 150);

  textAlign(RIGHT, TOP);
  textFont(fontLabel);
  // text("QPS,%", width - 85, qpsHeight + 60);
  text("av. Time,ms", width - 10, qpsHeight + 60);
  if (!bCanUseGlobalStat)
    return;

  //    text(nf(((float)qpsGl/qpsGl)*100,1,2)+"%",width - 85, qpsHeight + 90);
  //    text(nf(((float)qpsNA/qpsGl)*100,1,2)+"%",width - 85, qpsHeight + 110);
  //    text(nf(((float)qpsEu/qpsGl)*100,1,2)+"%",width - 85, qpsHeight + 130);
  //    text(nf(((float)qpsAs/qpsGl)*100,1,2)+"%",width - 85, qpsHeight + 150);
  //    
  // println("avTimeGl: "+nf(avTimeGl,1,2) +"   "+avTimeGl);

  String s;
  if (avTimeGl > 0)
    s = nf(avTimeGl,1,2);
  else s = "0";
  text(s+"ms",width - 10, qpsHeight + 90);
  if (avTimeNA > 0)
    s = nf(avTimeNA,1,2);
  else s = "0";
  text(s+"ms",width - 10, qpsHeight + 110);
  if (avTimeEu > 0)
    s = nf(avTimeEu,1,2);
  else s = "0";
  text(s+"ms",width - 10, qpsHeight + 130);
  if (avTimeAs > 0)
    s = nf(avTimeAs,1,2);
  else s = "0";
  text(s+"ms",width - 10, qpsHeight + 150);
}

public void drawSHisPanel()
{

  int i=0;
  int dx = 120;
  int dy = 100;
  noStroke();
  fill(0, panelAlpha);
  rect(290, 20, 3*dx+120,((servers.size()-1)/4)*dy+dy+50);
  for (Iterator itServer = servers.iterator(); itServer.hasNext();) 
  {
    i++;
    Server s = (Server)itServer.next();
    s.drawQPSHistory(((i-1)%4)*dx+330,((i-1)/4)*dy+40,50,50);
  }
}

public void drawSQPSPanel()
{

  noStroke();
  fill(0, panelAlpha);
  rect(220, 0, 50, (servers.size() * 38) + 34);
  rect(272, 0, 70, (servers.size() * 38) + 34);
  noStroke();
  textFont(fontLabel);
  fill(255, 255, 255, 240);
  textAlign(LEFT, TOP);
  text("Load", 225, 5);
  text("AvgT", 302, 5);
  Integer qpps = 0;

  for (Iterator itServer = servers.iterator(); itServer.hasNext();) 
  {
    Server s = (Server)itServer.next();
    qpps += s.currentQPS;
  }
  int i = 0;
  int c = 0;

  for (Iterator itServer = servers.iterator(); itServer.hasNext();) 
  {
    i++;

    Server s = (Server)itServer.next();

    int y = 16 + (c * 40);

    strokeWeight(1);
    stroke(0);

    float sqps= s.currentQPS, gqps = qpps;

    float perc;
    if (gqps == 0.0f || fNaN.equals(gqps) || fNaN.equals(sqps))
      perc = 0.0f;
    else
      perc = sqps/gqps*100;
    if (fNaN.equals(perc))
      perc = 0.0f;
    noStroke();
    fill(255, 255, 255, 240);
    textAlign(RIGHT, TOP);
    String st = nf(perc, 1, 1);
    text(st+"%", 250, y + 5);
    //percent = df.format(s.avTime);
    if (fNaN.equals(s.avTime))
      st = "0.0";
    else
      st = nf(s.avTime, 1, 1);
    text(st+"ms", 332, y + 5);
    c++;
    //println(GetServerByID("lon").avTime);
  }
}

public void drawCWPanel()
{
  int winX = width-300, winY = 0;

  noStroke();
  fill(0, panelAlpha);
  rect(gpX, -1, width+1, gpY);


  textFont(fontDate);
  fill(255, 255, 255, 240);
  textAlign(CENTER, CENTER);
  text("Gradient color", gpX+150, 20);

  image(imgColorWheel, gpX+150-imgColorWheel.width/2, 50);

  textFont(fontLabel2);
  textAlign(LEFT, TOP);
  fill(255, 255, 255, 240);
  text("Start", cbQStart.Left-40, cbQStart.Top+5);
  text("End", cbQEnd.Left-40, cbQEnd.Top+5);


  text("Highl.", cbHighlighted.Left-45, cbHighlighted.Top+5);

  text("Sel.", cbSelected.Left-35, cbSelected.Top+5);

  if (CAST == CAST_COUNTRY)
  {
    cbSStart.Draw();
    cbSEnd.Draw();
    textFont(fontLabel2);
    textAlign(LEFT, TOP);
    fill(255, 255, 255, 240);
    text("Zero", cbSZero.Left-40, cbSZero.Top+5);
    cbSZero.Draw();
    minSlider.Draw();
    maxSlider.Draw();
  }
  else
  {
    cbQStart.Draw();
    cbQEnd.Draw();
  }
  cbHighlighted.Draw();
  cbSelected.Draw();
}

public void drawTitle() {
}

public void drawTime() {
  Date date = new Date();

  String suffix = dayExt(day());
  Format formatter = new SimpleDateFormat("EEEE, MMMM d'" + suffix + "', yyyy");
  String logDate = formatter.format(date);

  formatter = new SimpleDateFormat("HH:mm:ss 'UTC'");
  ((DateFormat)formatter).setTimeZone(TimeZone.getTimeZone("UTC"));
  String logTime = formatter.format(date);
  logTime.trim();

  int startX = 15;
  if (logTime.charAt(0) == '1') {
    startX = 5;
  }

  textFont(fontDate);
  float dw = textWidth(logDate);

  textFont(fontTime);
  float tw = textWidth(logTime);
  float fw = dw > tw ? dw : tw;

  fill(0, panelAlpha);
  rect(-1, height - 118, width + 2, 119);

  textAlign(LEFT, BOTTOM);
  textFont(fontDate);
  fill(255, 255, 0, 220);
  text(logDate, 15, height - 75);

  textFont(fontTime);
  fill(255, 255, 0, 240);
  text(logTime, startX, height - 9);

  textAlign(RIGHT, BOTTOM);
  fill(255, 240);

  if (CAST == CAST_UNI) {
    text(footerDynDNS, width - 21, height - 21);
    image(imgLogoDynDNS, width - textWidth(footerDynDNS) - imgLogoDynDNS.width - 35, height - 95);
  } 
  else if (CAST == CAST_ANY) {

    String s;
    if (bOptimalHightlight)
      s = footerOptima;
    else
      s = footerDynect;
    text(s, width - 21, height - 21);
    image(imgLogoDynect, width - textWidth(s) - imgLogoDynect.width - 35, height - 95);
  } 
  else if (CAST == CAST_BOTH) {
    text(footerDynINC, width - 21, height - 21);
    image(imgLogoDynDNS, width - textWidth(footerDynINC) - imgLogoDynDNS.width - 35, height - 95);
  }
  else if (CAST == CAST_GRAD) {
    text(footerGrad, width - 21, height - 21);
    image(imgLogoDynect, width - textWidth(footerGrad) - imgLogoDynect.width - 35, height - 95);
  }
  else if (CAST == CAST_COUNTRY && !bOptimalCountry) {
    text(footerCount, width - 21, height - 21);
    image(imgLogoDynect, width - textWidth(footerCount) - imgLogoDynect.width - 35, height - 95);
  }
  else if (CAST == CAST_COUNTRY && bOptimalCountry) {
    text(footerOpCount, width - 21, height - 21);
    image(imgLogoDynect, width - textWidth(footerOpCount) - imgLogoDynect.width - 35, height - 95);
  }
  else if (CAST == CAST_CIRCLE) {
    text(footerCirc, width - 21, height - 21);
    image(imgLogoDynect, width - textWidth(footerCirc) - imgLogoDynect.width - 35, height - 95);
  }

  /*
	if (bPerlUnicast) {
   
   	} else {
   		text(footerDynect, width - 21, height - 21);
   		image(imgLogoDynect, width - textWidth(footerDynect) - imgLogoDynect.width - 35, height - 95);
   	}
   	*/


  //image(imgLogoDynect, width - 948, height - 95);
}

public void drawHelp() {
  textFont(fontHelp);
  fill(255, 255, 255, 240);
  if (helpWidth == 0) {
    helpWidth = textWidth(helpText);
  }

  fill(0, panelAlpha);
  rect(width - helpWidth - 32, -1, helpWidth + 32, 510);

  fill(255, 240);
  textAlign(RIGHT, TOP);
  text(helpText, width - 15, 15);
}

public void drawQPS() {
  textFont(fontTime);
  fill(0, panelAlpha);
  rect(width - qpsWidth - 32, -1, qpsWidth + 32, qpsHeight + 32);

  fill(255, 240);
  textAlign(LEFT, TOP);

  DecimalFormat df = new DecimalFormat("#,##0");
  String qpsPretty = df.format(currentQPS);

  if (String.valueOf(currentQPS).charAt(0) == '1') {
    text(qpsPretty, width - qpsWidth - 29, 174);
  } 
  else {
    text(qpsPretty, width - qpsWidth - 18, 174);
  }


  textFont(fontDate);
  textAlign(LEFT, TOP);
  text("queries per second", width - qpsWidth - 17, 225);

  int x = 0;
  Integer newMin = -1, newMax = 0;

  noFill();
  strokeWeight(1);
  stroke(100);

  line(width - qpsWidth - 15, 30, width - 15, 30);
  line(width - qpsWidth - 15, 150, width - 15, 150);

  stroke(200);
  for (int i = 0; i <= 200; i += 20) {
    line(width - 15 - i, 28, width - 15 - i, 32);
    line(width - 15 - i, 148, width - 15 - i, 152);
  }

  fill(0, 200);
  noStroke();
  rect(width - qpsWidth - 15, 30, 200, 120);

  fill(255);

  textFont(fontLabel);

  textAlign(LEFT, TOP);
  text("-10s", width - 215, 15);

  textAlign(RIGHT, TOP);
  text(PApplet.parseInt(qpsMax), width - 15, 15);

  textAlign(RIGHT, TOP);
  text(PApplet.parseInt(qpsMin), width - 15, 155);

  stroke(255);
  strokeWeight(1);
  noFill();

  if (bQPSSolid) {
    beginShape(LINES);
  } 
  else {
    beginShape();
  }

  Integer qpsNum = 0;
  float scaled = 0.0f;
  float freshest = 0.0f;

  synchronized(qpsHistory)
  {
    for (Iterator itQPSHistory = qpsHistory.iterator(); itQPSHistory.hasNext() && x < 200;)
    {
      qpsNum = (Integer)itQPSHistory.next();

      scaled = map(qpsNum, qpsMin, qpsMax, -120, 0);
      if (scaled < -120) scaled = -120;
      if (scaled > 0) scaled = 0;

      if (freshest == 0.0f) freshest = scaled;

      if (bQPSSolid) {
        stroke(255 - x);
        vertex(width - x - 15, 150);
      } 
      else {
        stroke(240 - x);
      }

      vertex(width - x - 15, abs(scaled) + 30);

      if (qpsNum > newMax) newMax = qpsNum;
      if (qpsNum < newMin || newMin == -1) newMin = qpsNum;

      x++;
    }
  }

  endShape();

  fill(255);
  noStroke();
  ellipseMode(CENTER);
  ellipse(width - 15, abs(freshest) + 30, 3, 3);

  Integer roundTo = newMax / 10;

  if (roundTo <= 10) roundTo = 1;
  else if (roundTo <= 100) roundTo = 10;
  else if (roundTo <= 1000) roundTo = 100;
  else if (roundTo <= 10000) roundTo = 1000;
  else if (roundTo <= 100000) roundTo = 10000;

  qpsMaxAge++;
  qpsMinAge++;

  newMax = roundUp(newMax, roundTo);
  newMin = roundDown(newMin, roundTo);

  if (qpsMaxAge > qpsAgeWindow || newMax >= qpsMax) {
    qpsMax = newMax;
    qpsMaxAge = 0;
  }

  if (qpsMinAge > qpsAgeWindow || newMin <= qpsMin || qpsHistory.size() < 50) {
    qpsMin = newMin;
    qpsMinAge = 0;
  }
}

public void point3d(GL gl, float x, float y, float z, float size, int col, int a)
{
  int r = col >> 16 & 0xFF;
  int g = col >> 8 & 0xFF;
  int b = col & 0xFF;

  point3d(gl, x, y, z, size, r, g, b, a);
}

public void point3d(GL gl, float x, float y, float z, float size, int r, int g, int b, int a)
{
  if (size > 0) {
    gl.glColor4ub((byte)r, (byte)g, (byte)b, (byte)a);
    gl.glVertex3f(x, y, z);
  }
}

public void beginPoints(GL gl) {
  beginPoints(gl, 8.0f);
}

public void beginPoints(GL gl, float pointSize) {

  gl.glEnable(gl.GL_POINT_SMOOTH);
  gl.glHint(gl.GL_POINT_SMOOTH_HINT, gl.GL_NICEST);

  // Glowy transparency
  //gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);  // Standard transparency

  //  derived_size = clamp(size * sqrt(1 / (a + b * d + c * d ^ 2)))
  //gl.glPointParameterfv(gl.GL_POINT_DISTANCE_ATTENUATION, attenuation, 0);

  /*
	float quadratic[] = { 0.0f, 0.0f, 0.000002f };
   
   	gl.glPointParameterfv( gl.GL_POINT_DISTANCE_ATTENUATION, quadratic, 0 );
   	gl.glPointParameterf(  gl.GL_POINT_FADE_THRESHOLD_SIZE, 60.0f );
   	gl.glPointParameterf(  gl.GL_POINT_SIZE_MIN, 1.0f );
   	gl.glPointParameterf(  gl.GL_POINT_SIZE_MAX, 15.0f );
   	*/
  gl.glPointSize(pointSize);


  if (bAlpha) {
    gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE);
  } 
  else {
    gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
  }

  gl.glEnable(gl.GL_BLEND);
  gl.glBegin(gl.GL_POINTS);
}

public void endPoints(GL gl) {
  gl.glEnd();

  gl.glDisable(gl.GL_POINT_SMOOTH);
  gl.glDisable(gl.GL_BLEND);

  gl.glBlendFunc(gl.GL_ONE, gl.GL_ZERO);
}

public void beginLines(GL gl) {
  beginLines(gl, 2.0f);
}

public void beginLines(GL gl, float pointSize) {
  stroke(0);
  noStroke();

  gl.glLineWidth(pointSize);

  if (bAlpha) {
    gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE);
  } 
  else {
    gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
  }

  gl.glEnable(gl.GL_BLEND);

  gl.glBegin(gl.GL_LINES);
}

public void endLines(GL gl) {
  gl.glEnd();

  gl.glDisable(gl.GL_BLEND);
}
public void line3d(GL gl, float x1, float y1, float z1, float x2, float y2, float z2, float size, int col, float a1, float a2)
{
  line3d(gl, x1, y1, z1, x2, y2, z2, size, (byte)red(col), (byte)green(col), (byte)blue(col), ( byte)a1, (byte)a2);
}

public void line3d(GL gl, float x1, float y1, float z1, float x2, float y2, float z2, float size, byte r, byte g, byte b,  byte a1,  byte a2)
{
  // 0.0 is invalid
  if (size > 0) {

    gl.glColor4ub(r, g, b, a1);
    gl.glVertex3f(x1, y1, z1);

    gl.glColor4ub(r, g, b, a2);
    gl.glVertex3f(x2, y2, z2);
  }
}

public void setGlobe()
{
  pushMatrix();
  rotateX(radians(-rotationX));
  rotateY(radians(rotationY));
  pgl.gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, mvMatrix, 0);

  //        println();
  //        println(mvMatrix[0]+"\t"+mvMatrix[1]+"\t"+mvMatrix[2]+"\t"+mvMatrix[3]);
  //        println(mvMatrix[4]+"\t"+mvMatrix[5]+"\t"+mvMatrix[6]+"\t"+mvMatrix[7]);
  //        println(mvMatrix[8]+"\t"+mvMatrix[9]+"\t"+mvMatrix[10]+"\t"+mvMatrix[11]);
  //        println(mvMatrix[12]+"\t"+mvMatrix[13]+"\t"+mvMatrix[14]+"\t"+mvMatrix[15]);
  //        println();
  //printMatrix();
}

public void resetGlobe()
{
  popMatrix();
}

public void setCompenseGlobe()
{
  pushMatrix();
  rotateX(radians(rotationX));
  rotateY(radians(rotationY));
}

public void resetCompenseGlobe()
{
  popMatrix();
}

public void renderGlobe()
{
  fill(0);
  noStroke();

  CullingCCW(gl);
  texturedSphere(globeRadius - 2);

  //	CullingCW(gl);
  //        sDetail++;
  //        sDetail--;
  //	texturedSphere(globeRadius-1);

  ResetCulling(gl);

  // Fancy shiny lighting

  /*
	noLights();
   
   	fill(255, 128);
   	ambient(0);
   	shininess(5.0);
   	emissive(255);
   	specular(0);
   
   	//directionalLight(0, 0, 0, -.5, 0, -1);
   
   	sphere(globeRadius + 5);
   
   	emissive(0);
   	specular(0);
   	*/
}

public void drawQueries()
{

  noStroke();
  noFill();

  gl.glDisable(gl.GL_DEPTH_TEST);

  synchronized(queries)
  {
    beginPoints(gl, sizeQueries);
    //   !!!!!!!!!!!!!!!!!!!!!!!!!!!         println("qu " +queries.size()+" qps "+currentQPS/QPS_MULTIPLIER);
    for (Iterator itQueries = queries.iterator(); itQueries.hasNext();)
    {
      Query q = (Query)itQueries.next();
      Server s = q.resolver;
      if (s!=null && q.getAge() >= queryScale)
      {
        itQueries.remove();
      } 
      else if (bQueries && CAST != CAST_COUNTRY)
        if (s != null  && ((s.display == true && CAST == CAST_BOTH || CAST == (q.anycast ? CAST_ANY : CAST_UNI)) || 
            (CAST!= CAST_BOTH && CAST!=CAST_ANY && CAST!=CAST_UNI))) 
        {
          int age = q.getAge();

          if (age < queryScale)
          {
            float sX = screenX(q.pos.x, q.pos.y, q.pos.z);
            float sY = screenY(q.pos.x, q.pos.y, q.pos.z);
            //					float sZ = modelZ(q.pos.x, q.pos.y, q.pos.z);
            float sZ = GetModelZ(q);


            if (sX > 0 && sY > 0 && sX < width && sY < height && IsPointAtThatSide(q))
            {

              float alf_dist = sZ - sightDist;
              if (alf_dist > sightDist) alf_dist = sightDist;
              alf_dist = (alf_dist / sightDist) * 255.0f;

              float death_clock = queryScale - age;
              float alf_age =  (death_clock < 255.0f) ? death_clock : 255;

              float alf = min(alf_dist, alf_age);

              if (alf > 0.0f) {
                point3d(gl, q.pos.x, q.pos.y, q.pos.z, 8.0f, GetColor(q), PApplet.parseInt(alf));
              }
            }
          }
        }

      q.age();
    }
    endPoints(gl);

    if (bLines && CAST != CAST_COUNTRY) {
      beginLines(gl, sizeLines);

      for (Iterator itQueries = queries.iterator(); itQueries.hasNext();)
      {
        Query q = (Query)itQueries.next();
        Server s = q.resolver;

        float sZ = GetModelZ(q);
        if (s != null  && ((s.display == true && CAST == CAST_BOTH || CAST == (q.anycast ? CAST_ANY : CAST_UNI)) || (CAST!= CAST_BOTH && CAST!=CAST_ANY && CAST!=CAST_UNI))/*(s.display == true || CAST==CAST_GRAD || CAST==CAST_ANY) && IsPointAtThatSide(q) && CAST == CAST_BOTH || CAST == (q.anycast ? CAST_ANY : CAST_UNI) || CAST == CAST_GRAD*/) {

          int age = q.getAge();

          float alf_dist = sZ - sightDist;
          if (alf_dist > sightDist) alf_dist = sightDist;
          alf_dist = (alf_dist / sightDist) * 255.0f;

          float death_clock = queryScale - age;
          float alf_age =  (death_clock < 255.0f) ? death_clock : 255;

          float alf = min(alf_dist, alf_age);

          if (alf > 0.0f) {
            if (bTails) {
              line3d(gl, q.tail.x, q.tail.y, q.tail.z, q.pos.x, q.pos.y, q.pos.z,       1.0f, GetColor(q), tailAlphaBegin, alf);
            } 
            else {
              line3d(gl, q.origin.x, q.origin.y, q.origin.z, q.pos.x, q.pos.y, q.pos.z, 1.0f, GetColor(q), tailAlphaBegin, alf);
            }
          }
        }
      }

      endLines(gl);
    }

    if (bShadows && CAST != CAST_COUNTRY) {
      beginPoints(gl, sizeShadow);

      for (Iterator itQueries = queries.iterator(); itQueries.hasNext();)
      {
        Query q = (Query)itQueries.next();
        Server s = q.resolver;
        if (s != null && ((s.display == true && CAST == CAST_BOTH || CAST == (q.anycast ? CAST_ANY : CAST_UNI)) || (CAST!= CAST_BOTH && CAST!=CAST_ANY && CAST!=CAST_UNI))/*(s.display == true || CAST==CAST_GRAD || CAST==CAST_ANY)*/ && IsPointAtThatSide(q)) {
          point3d(gl, q.origin.x, q.origin.y, q.origin.z, 8.0f, GetColor(q), 128 - q.getAge());
        }
      }

      endPoints(gl);
    }
  }

  gl.glEnable(gl.GL_DEPTH_TEST);
}

public void CalculateVisibleRange()
{
  float sinGl = globeRadius/hcam.pos.z;
  float cosGl = (float)Math.sqrt(1-sinGl*sinGl);
  float distToXYThruGlobe = hcam.pos.z/cosGl;
  float distToGlobe = hcam.pos.z*cosGl;
  sightDist = (1-distToGlobe/distToXYThruGlobe)*hcam.pos.z;
}

public boolean IsPointAtThatSide(Query q)
{
  float z_ = GetModelZ(q);

  if (z_>sightDist)
    return true;
  return false;
}

public float GetModelZ(Query q)
{
  float z = (float)(-q.pos.x*Math.sin(radians(rotationY))+q.pos.z*Math.cos(radians(rotationY)));
  //float x = (float)(q.pos.x*Math.cos(radians(rotationY))+q.pos.z*Math.sin(radians(rotationY)));
  float y = q.pos.y;

  float z_ = (float)(y*Math.sin(radians(rotationX))+z*Math.cos(radians(rotationX)));
  return z_;
}

public boolean IsAllServersOff()
{
  boolean retB = true;

  int sC = servers.size();
  for (int i = 0; i< sC; i++)
    if (((Server)servers.get(i)).display)
      retB = false;
  
  return retB;
}

public int GetColor(Query q)
{

  if (CAST==CAST_GRAD)
  {  
    //Server s = null;//GetNearestServer(q);
    if (!IsAllServersOff())
      return GetGradientColor(q);
    else
      return (int) 0xffFF0000;
  }
  if (CAST == CAST_CIRCLE)
  {
    //Server s = GetNearestServer(q);
    if (!IsAllServersOff())
    {
      if (q.potencial < 1.0f/3.0f)
      {
        return (int)0xff00FF00;
      }
      if (q.potencial <= 2.0f/3.0f)
      {
        return (int)0xff559900;
      }
      if (q.potencial < 1.0f)
      {
        return (int) 0xffAA4400;
      }
      return (int)0xffFF0000;
    }
    else
      return (int) 0xffFF0000;
  }
  if (CAST==CAST_ANY)
  {
    Server s = GetNearestServer(q);
    if (s!=null)
    {
      if (bOptimalHightlight)
      {
        if (s == q.resolver)
          return (int) s.getColor();
        else
          return (int) 0xffFFFFFF;
      }
      else
        return s.getColor();
    }
    else
      return (int) 0xffFF0000;
  }  
  return q.resolver.getColor();
}

public void CalcServersDistCenter()
{
  Vector3D center = new Vector3D(0,0,0);
  Server s;
  int servCount = 0;
  for (Iterator itServ = servers.iterator(); itServ.hasNext();)
  {
    s = (Server) itServ.next();
    if (!s.display)
      continue;
    center.add(s.pos);
    servCount++;
  }
  if (servCount>0)
    center.over(servCount);
  else
    center = new Vector3D(0,0,0);
  rc = center;
}

public float GetSphereDistance(Vector3D p1, Vector3D p2)
{
  float cosTh1, cosTh2, sinTh1, sinTh2, cosGa1, cosGa2, sinGa1, sinGa2;
  float flatRadius = (float)Math.sqrt(p1.z*p1.z+p1.y*p1.y);
  Vector3D z = new Vector3D(0,0,1);
  cosTh1 = cos(p1.angleWith(z));
  sinTh1 = sin(p1.angleWith(z));
  flatRadius = (float)Math.sqrt(p2.z*p2.z+p2.y*p2.y);
  cosTh2 = cos(p2.angleWith(z));
  sinTh2 = sin(p2.angleWith(z));

  flatRadius = (float)Math.sqrt(p1.x*p1.x+p1.y*p1.y);
  cosGa1 = p1.x/flatRadius;
  sinGa1 = p1.y/flatRadius;
  flatRadius = (float)Math.sqrt(p2.x*p2.x+p2.y*p2.y);
  cosGa2 = p2.x/flatRadius;
  sinGa2 = p2.y/flatRadius;

  float L = globeRadius*acos(cosTh1*cosTh2+sinTh1*sinTh2*(cosTh1*cosTh2+sinGa1*sinGa2));
  return L;
}

public Server GetFarestServer(Query q)
{
  Server s = null;
  Server maxServ = null;
  if (true)//!q.resolver.display)
  {
    float distance, maxdistance = 0;

    for (Iterator itServ = servers.iterator(); itServ.hasNext();)
    {
      s = (Server) itServ.next();
      if (!s.display)
        continue;
      distance = AngDistToSrvr(s, q);
      if (distance > maxdistance)
      {
        maxdistance = distance;
        maxServ = s;
      }
    }
  }
  else
    maxServ = q.resolver;
  return maxServ;
}

public Server GetNearestServer(Query q)
{
  Server s = null;
  Server minServ = null;
  if (true)//!q.resolver.display)
  {
    float distance, mindistance = 2*PI;

    for (int i = 0; i < servers.size(); i++)
    {
      s = (Server) servers.get(i);
      if (!s.display)
        continue;
      distance = AngDistToSrvr(s, q);
      if (distance < mindistance)
      {
        mindistance = distance;
        minServ = s;
      }
    }
  }
  else
    minServ = q.resolver;
  return minServ;
}

public Server[] Get2NearestServers(Query q)
{
  Server minS[] = new Server[2];  
  Server s = null;
  minS[0] = GetNearestServer(q);
  //Server minServ = null;
  float distance, mindistance = 2*PI;
  for (int i = 0; i < servers.size(); i++)
  {
    s = (Server) servers.get(i);
    if (!s.display)
    {  
      continue;
    }
    distance = AngDistToSrvr(s, q);
    if (distance < mindistance)
    {
      mindistance = distance;
      minS[1] = s;
    }
  }
  if (minS[1]==null)
    minS[1] = minS[0];
  return minS;
}

public float DistToQuery(Server s1, Server s2, Query q)
{
  if (s1 == s2)
  {
    return PI;
  }

  float distanceToQ;
  Vector3D toQ, h, toS2, Sh;

  Vector3D angQ = new Vector3D(q.ang), angS2 = new Vector3D(s2.ang), angS1 = new Vector3D(s1.ang);//, vX = new Vector3D(1,0,0),vZ = new Vector3D(0,0,1), vY = new Vector3D(0,1,0);

  toQ = angQ.minus(angS1);
  toS2 = angS2.minus(angS1);  

  Sh = toS2.over(2);


  toQ = toQ.getNormalized().multiplyBy(Sh.length());
  if (toQ.x  > PI/2)
    toQ.x -= PI;
  toQ.x +=angS1.x;
  if (toQ.x  > PI/2)
    toQ.x -= PI;

  if (toQ.y  > 2*PI)
    toQ.y -= 2*PI;
  toQ.y += angS1.y;
  if (toQ.y  > 2*PI)
    toQ.y -= 2*PI;
  Sh.x = sin(toQ.x)*cos(toQ.y);
  Sh.y = sin(toQ.x)*sin(toQ.y);
  Sh.z = cos(toQ.x);
  distanceToQ = s1.pos.angleWith(Sh);

  return distanceToQ;
}

public float AngDistToSrvr(Server s, Query q)
{
  return s.pos.angleWith(q.pos);
}

public float ColorIt(Query q)
{
  float dista =0;
  int srvrs=0;
  Server s;
  for (Iterator itServ = servers.iterator(); itServ.hasNext();)
  {
    s = (Server) itServ.next();
    if (!s.display)
    {  
      continue;
    }
    srvrs ++;
    dista += 1.0f/AngDistToSrvr(s, q);
  }
  dista /=(float)srvrs;
  return dista;
}




public int GetGradientColor(Query q)
{
  int _r = (cbQStart.CurrentColor & 0xffFF0000)>>16 ;
  int _g = (cbQStart.CurrentColor & 0xff00FF00)>>8 ;
  int _b = (cbQStart.CurrentColor & 0xff0000FF) ;

  int r = (cbQEnd.CurrentColor & 0xffFF0000)>>16;
  int g = (cbQEnd.CurrentColor & 0xff00FF00)>>8;
  int b = (cbQEnd.CurrentColor & 0xff0000FF);

  r = round(_r-((float)(_r-r))*q.potencial);
  g = round(_g-((float)(_g-g))*q.potencial);
  b = round(_b-((float)(_b-b))*q.potencial);
  int c = r<<16 | g<<8 | b;

  return c;
}


public void adjustRotation()
{
  //rotationX = (rotationX + targetRotX) / 2;
  //rotationY = (rotationY + targetRotY) / 2;

  rotationX += velocityX;
  rotationY -= velocityY;

  if (rotationX >360.0f)
    rotationX -= 360.0f;
  if (rotationX < 0)
    rotationX += 360.0f;
  if (rotationY >360.0f)
    rotationY -= 360.0f;
  if (rotationY < 0)
    rotationY += 360.0f;

  float dist = hcam.pos.z;
  /*
	if (dist < globeRadius + 256) {
   		attenuation = attenuation_near;
   	} else {
   		attenuation = attenuation_far;
   	}
   	*/
  if (zoomingIn) {
    velocityZ += 8;
  } 
  else if (zoomingOut) {
    velocityZ -= 8;
  } 
  else {
    velocityZ *= 0.65f;
  }

  if (dist - velocityZ <= globeRadius + 1 + 160)
  {
    velocityZ = 0;
    hcam.craneTo(new Vector3D(0.0f, 0.0f, globeRadius + 1 + 160));
  }
  else
  {
    hcam.crane(PApplet.parseInt(-velocityZ));
  }

  hcam.cam.feed();
}

public void CWPanelMouseHandle()
{
  int _x = gpX+150-imgColorWheel.width/2;
  int _y = 50;
  if (mouseX>_x && mouseX< _x + imgColorWheel.width && mouseY>50 && mouseY<_y + imgColorWheel.height)
  {
    ColorBlock cb = null;
    if (CAST == CAST_COUNTRY)
    {
      if (cbSStart.Checked)
        cb = cbSStart;
      if (cbSEnd.Checked)
        cb = cbSEnd;
      if (cbSZero.Checked)
        cb = cbSZero;
    }
    else
    {
      if (cbQStart.Checked)
        cb = cbQStart;
      if (cbQEnd.Checked)
        cb = cbQEnd;
    }
    if (cbHighlighted.Checked)
      cb = cbHighlighted;
    if (cbSelected.Checked)
      cb = cbSelected;

    if (cb!=null)    
      cb.CurrentColor = imgColorWheel.get(mouseX-_x, mouseY-_y);
    if (cbHighlighted.Checked)
      highlightColor = cb.GetNormColor();
    if (cbSelected.Checked)
      selectionColor = cb.GetNormColor();
  }
  else
  {
    if (CAST == CAST_COUNTRY)
    {
      cbSStart.Click(mouseX, mouseY);
      cbSEnd.Click(mouseX, mouseY);
      cbSZero.Click(mouseX, mouseY);
      if (minSlider.Click(mouseX, mouseY))
      {
        maxSlider.SetMinTime(minSlider.GetCurrentTime());
        minAvT = (float) minSlider.GetCurrentTime();
        //minSlider.SetMaxTime(maxSlider.GetCurrentTime());
      }
      if (maxSlider.Click(mouseX, mouseY))
      {
        minSlider.SetMaxTime(maxSlider.GetCurrentTime());
        maxAvT = (float) maxSlider.GetCurrentTime();
        //maxSlider.SetMinTime(minSlider.GetCurrentTime());
      }
    }
    else
    {
      cbQStart.Click(mouseX, mouseY);
      cbQEnd.Click(mouseX, mouseY);
    }
    cbHighlighted.Click(mouseX, mouseY);
    cbSelected.Click(mouseX, mouseY);
  }
  if (CAST == CAST_COUNTRY)
  {
    cbSStart.Draw();
    cbSEnd.Draw();
    cbSZero.Draw();
  }
  else
  {
    cbQStart.Draw();
    cbQEnd.Draw();
  }
  cbHighlighted.Draw();
  cbSelected.Draw();
}

public void keyReleased()
{
  if (keyCode == KeyEvent.VK_CONTROL)
  {
    bCTRL = false;
    //println("CTRL released");
  }
  if (keyCode == KeyEvent.VK_SHIFT)
    bSHIFT = false;
  if (keyCode == KeyEvent.VK_ALT)
  {
    bALT = false;
  }
  if (keyCode == KeyEvent.VK_TAB)
  {
    bTAB = true;
    if (bPanelC2S)
    {
      if (bTAB) bCountrySelection = !bCountrySelection;
      bTAB = false;
    }
  }
}

public void mouseReleased()
{
  if (bPanel1 && mouseButton == LEFT && keyEvent != null && bCTRL)
  {
    if (mouseX >0 && mouseX < 220)
    {
      int ind = (mouseY - 16)/40;
      if (ind < servers.size())
      {
        Server tmpS = (Server) servers.get(ind);
        if (tmpS != selectedServer)
          selectedServer = tmpS;
        else
          selectedServer = null;
      }
      //println("Selected server index: "+ind);
    }
  }
  if (bALT && mouseButton == LEFT)
  {
    Vector3D retV = ProjectScreenToGlobe(pgl.gl, mouseX, mouseY, hcam.pos);
    if (retV == null)
    {
     // highlightedCountry = -1;
      return;
    }
    String sName;
    if (highlightedCountry >= 0)
    {
      sName = countriesNames[highlightedCountry];// + "'s Server";
    }
    else
      sName = "New Server";
    servers.add(new Server(0.f, 0.0f, -337.592660812806f,  -486.676757393927f,  537.751735634449f,  sName, "new", (int)0xffFFFFFF));
    Server s = (Server)servers.get(servers.size()-1);
    s.pos =  retV;
    serverSwitch = true;
    ResetGlobalStatistic();
  }
  if (bSHIFT && mouseButton == LEFT)
  {
    selectedCountry = highlightedCountry;

    //        servers.add(new Server(0., 0.0, -337.592660812806,  -486.676757393927,  537.751735634449,   "!!", "!!", (color)#FFFFFF));
    //        Server s = (Server)servers.get(servers.size()-1);
    //s.pos =  retV;
    return;
  }
}

public void handleMouse()
{
  if (mousePressed)
  {
    if (bStatConfigs && mouseButton == LEFT && mouseX>(width-370) && mouseY<statConfPH)
    {
      statTimeSlider.Click(mouseX, mouseY);
      shadTimeSlider.Click(mouseX, mouseY);
      servTimeSlider.Click(mouseX, mouseY);
    }
    else
      if (bPanel5CW && mouseX>gpX && mouseY<gpY && mouseButton == LEFT)
      {
        CWPanelMouseHandle();
      }
      else            
    {               
      if (mouseButton == LEFT )
      {
        if (bSHIFT && bALT)
        {

          //                            servers.add(new Server(0., 0.0, -337.592660812806,  -486.676757393927,  537.751735634449,   "!!", "!!", (color)#FFFFFF));
          //                            Server s = (Server)servers.get(servers.size()-1);
          //                            s.pos =  ProjectScreenToGlobe(pgl.gl, mouseX, mouseY, hcam.pos);
          return;
        }
        velocityX *= 0.95f;
        velocityY *= 0.95f;

        float dx = (mouseX - pmouseX);
        float dy =0.0f;

        if (bRotationReparation)
          dy = -(mouseY - pmouseY);
        else
          dy = (mouseY - pmouseY);


        float side = floor(rotationX / 90) % 4;

        if (side < 0) side = 4 + side;

        if (abs(side) == 0 || abs(side) == 3) {
          velocityY -= dx * 0.01f;
        } 
        else {
          velocityY += dx * 0.01f;
        }

        velocityX += dy * 0.01f;
      }
      else if (mouseButton == RIGHT)
      {
        hcam.cam.roll(radians(mouseX - pmouseX));
      }
      else if (mouseButton == CENTER)
      {
        hcam.cam.look(radians(mouseX - pmouseX) / 2.0f, radians(mouseY - pmouseY) / 2.0f);
      }
    }
  }

  if(keyPressed)
  {
    if (key == CODED)
    {
      if (keyCode == LEFT)
      {
        velocityY += .1f;
      }
      else if (keyCode == RIGHT)
      {
        velocityY -= .1f;
      }
      else if (keyCode == UP)
      {
        if (bRotationReparation)
          velocityX += .1f;
        else
          velocityX -= .1f;
      }
      else if (keyCode == DOWN)
      {
        if (bRotationReparation)
          velocityX -= .1f;
        else
          velocityX += .1f;
      }
    }
    else if (key == 'a')
    {
      velocityZ += 3;
    }
    else if (key == 'z')
    {
      velocityZ -= 3;
    }
    else if (key == ' ')
    {
      velocityX *= 0.95f;
      velocityY *= 0.95f;
    }
  }
}

public void keyPressed()
{
  //if (key == '\\' || key == '|') saveFrame("screenshots\\dynviz-########.png");
  serverSwitch = false;
  //        bCTRL = false;
  //        bALT = false;
  //        bSHIFT = false;
  if (!bCTRL && keyCode == KeyEvent.VK_CONTROL)
  {
    bCTRL = true;
    //println("CTRL pressed");
  }
  if (!bALT && keyCode == KeyEvent.VK_ALT)
  {
    bALT = true;
  }
  if (!bSHIFT && keyCode == KeyEvent.VK_SHIFT)
    bSHIFT = true;

  if (!bCTRL)
    if (key == CODED) {
      if (keyCode == 112) {
        try {
          Server s = GetServerByID("pao");
          s.switchDisp();
          serverSwitch = true;
          //println("f1");
        } 
        catch (Exception e) {
        }
      } 
      else if (keyCode == 113) {
        try {
          Server s = GetServerByID("ord");
          s.switchDisp();
          serverSwitch = true;
          //println("f2");
        } 
        catch (Exception e) {
        }
      } 
      else if (keyCode == 114) {
        try {
          Server s = GetServerByID("iad");
          s.switchDisp();
          serverSwitch = true;
          //println("f3");
        } 
        catch (Exception e) {
        }
      } 
      else if (keyCode == 115) {
        try {
          Server s = GetServerByID("ewr");
          s.switchDisp();
          serverSwitch = true;
          //println("f4");
        } 
        catch (Exception e) {
        }
      } 
      else if (keyCode == 116) {
        try {
          Server s = GetServerByID("fra");
          s.switchDisp();
          serverSwitch = true;
          //println("f5");
        } 
        catch (Exception e) {
        }
      } 
      else if (keyCode == 117) {
        try {
          Server s = GetServerByID("lon");
          s.switchDisp();
          serverSwitch = true;
          //println("f6");
        } 
        catch (Exception e) {
        }
      } 
      else if (keyCode == 118) {
        try {
          Server s = GetServerByID("ams");
          s.switchDisp();
          serverSwitch = true;
          //println("f7");
        } 
        catch (Exception e) {
        }
      } 
      else if (keyCode == 119) {
        try {
          Server s = GetServerByID("hkg");
          s.switchDisp();
          serverSwitch = true;
          //println("f8");
        } 
        catch (Exception e) {
        }
      }
      else if (keyCode == 120) {
        try {
          Server s = GetServerByID("lax");
          s.switchDisp();
          serverSwitch = true;
          //println("f9");
        } 
        catch (Exception e) {
        }
      }
      else if (keyCode == 121) {
        try {
          Server s = GetServerByID("nyc");
          s.switchDisp();
          serverSwitch = true;
          //println("f10");
        } 
        catch (Exception e) {
        }
      }
      else if (keyCode == 122) {
        try {
          Server s = GetServerByID("spl");
          s.switchDisp();
          serverSwitch = true;
          //println("f11");
        } 
        catch (Exception e) {
        }
      }
      else if (keyCode == 123) {
        try {
          Server s = GetServerByID("tyo");
          s.switchDisp();
          serverSwitch = true;
          //println("f12");
        } 
        catch (Exception e) {
        }
      }
      //                println("COODEED!!");
    }

  if (!bCTRL && (key == BACKSPACE)) {
    for (Iterator itServer = servers.iterator(); itServer.hasNext();) {
      Server s = (Server)itServer.next();
      s.display = bSiteToggle;
    }
    bSiteToggle = !bSiteToggle;
  }

  if (!bCTRL && (key == 'g' || key == 'G')) bGlobe   = !bGlobe;
  if (!bCTRL && (key == 'q' || key == 'Q')) bQueries = !bQueries;
  if (!bCTRL && (key == 'l' || key == 'L')) bLines   = !bLines;
  if (!bCTRL && (key == 's' || key == 'S')) bShadows = !bShadows;
  if (!bCTRL && (key == 'f' || key == 'f')) bAlpha   = !bAlpha;        
  if (!bCTRL && (key == 'p' || key == 'p')) bQPSSolid = !bQPSSolid;
  if (!bCTRL && (key == DELETE)) bRotationReparation = !bRotationReparation;

  // Camera Position
  if (!bCTRL && (key == 'c' || key == 'C')) resetCamera(40, -95, false);
  if (!bCTRL && (key == 'm' || key == 'M')) resetCamera(43, -72, false);
  
  if (!bCTRL && (key == 'o' || key == 'O')) 
    if (selectedServer!= null)
      selectedServer.switchDisp();

  //Shader switcher
  //if (!bCTRL && (key == 'D' || key == 'd')) bUseShader = !bUseShader;

  //Help message
  if (!bCTRL && key == 'h') bPanel3 = !bPanel3;


  if (!bCTRL && key == '1') bPanel1 = !bPanel1;
  if (!bCTRL && key == '2') bPanel2 = !bPanel2;
  if (!bCTRL && key == '3')
  {
    println("bee");
    if (servers.size() > fixSrvrs)
    {
      String sList[] = new String[servers.size()-fixSrvrs];
      for (int i = fixSrvrs; i < servers.size(); i++)
      {
        
        Server s = (Server) servers.get(i);        
        String st = s.pos.toString() + " "+ s.name;
        sList[i-fixSrvrs] = st;        
      }
      saveStrings("newServers.txt", sList);
      println("bu");
    }
  }
  if (!bCTRL && key == '4') bPanel4 = !bPanel4;
  if (!bCTRL && key == '5') bPanel5CW = !bPanel5CW;
  if (!bCTRL && key == '6') bPanelSQPS = !bPanelSQPS;
  if (!bCTRL && key == '7') bPanelSHis = !bPanelSHis;
  if (!bCTRL && key == '8') bPanelGlSt = !bPanelGlSt;
  if (!bCTRL && key == '9') bPanelC2S = !bPanelC2S;
  if (!bCTRL && key == '0' && CAST == CAST_ANY) bOptimalHightlight = !bOptimalHightlight;
  if (!bCTRL && key == '0' && CAST == CAST_COUNTRY) bOptimalCountry = !bOptimalCountry;
  

  if (bCTRL && key == '1') bCountriesPopup = !bCountriesPopup;
  //Reset statistic
  if (bCTRL && key == '2') 
  {
    ResetGlobalStatistic();
  }
  if (bCTRL && key == '3') bFPS = !bFPS;
  if (bCTRL && key == '4') bStatConfigs = !bStatConfigs;
  

  if (!bCTRL && key == '=') sizeQueries += 0.2f;
  if (!bCTRL && key == ']') sizeLines   += 0.2f;
  if (!bCTRL && key == '\'') sizeShadow += 0.2f;


  //This part for tests
  //        if (key == 'u') 
  //        {
  //            Server s = GetServerByID("eu1");
  //            s.lon+= .01;
  //            s.RecalculatePos();
  //        }
  //        if (key == 'o') 
  //        {
  //            Server s = GetServerByID("eu1");
  //            s.lon-= .01;
  //            s.RecalculatePos();
  //        }
  //        if (key == 'i') 
  //        {
  //            Server s = GetServerByID("eu1");
  //            s.lat+= .01;
  //            s.RecalculatePos();
  //        }
  //        if (key == 'k') 
  //        {
  //            Server s = GetServerByID("eu1");
  //            s.lat-= .01;
  //            s.RecalculatePos();
  //        }
  //        if (angLat>PI*2) angLat -=2*PI;
  //        if (angLon>PI) angLon -=PI;
  //!This part for tests

  if (!bCTRL && (key == '-' && sizeQueries > 0.4f)) sizeQueries -= 0.2f;
  if (!bCTRL && (key == '[' && sizeLines > 0.4f)) sizeLines -= 0.2f;
  if (!bCTRL && (key == ';' && sizeShadow > 0.4f)) sizeShadow -= 0.2f;
  if (!bCTRL && (key == '.') && CAST == CAST_GRAD)
  {
    startShowing = System.currentTimeMillis(); 
    bShowTimingPanel = true;
    if (MaxDist > PI/36.0f)
    {
      //println(".");
      MaxDist -= PI/36.0f;
      if(MaxDist < PI/36.0f)
        MaxDist = PI/36.0f;
      Lmax = sqrt(MaxDist*MaxDist+MaxPower*MaxPower);
      serverSwitch = true;
    }
  }
  if (!bCTRL && (key == '/') && CAST == CAST_GRAD)
  {
    startShowing = System.currentTimeMillis();
    bShowTimingPanel = true;
    if (MaxDist <= 35.0f/36.0f*PI)
    {
      //println("/");
      MaxDist += PI/36.0f;
      if (MaxDist > PI)
        MaxDist = PI;
      Lmax = sqrt(MaxDist*MaxDist+MaxPower*MaxPower);
      serverSwitch = true;
    }
  }
  if (!bCTRL && (key == '`')) switchCast();

  // Reset camera
  if (!bCTRL && (key == 'r' || key == 'R')) resetCamera(323.28f, 5.47f, true);

  // Reset lines, queries, shadows
  if (!bCTRL && (key =='t' || key == 'T')) {
    sizeQueries = 4.0f;
    sizeShadow  = 4.0f;
    sizeLines   = 4.0f;
  }
}

public void switchCast() {
  CAST++;
  if (CAST > CAST_BOTH) CAST = CAST_UNI;

  //println("CAST = " + CAST);
}

public void resetCamera(float lat, float lon, boolean resetCam) {
  zoomingIn = false;
  zoomingOut = false;

  rotationX = lat;
  rotationY = lon;

  if (resetCam) {

    targetRotX = rotationX;
    targetRotY = rotationY;
    targetZ = -2000;

    velocityX = 0.0f;
    velocityY = 0.05f;
    velocityZ = 0.0f;

    camX = 0;
    camY = 0;
    camZ = -1750;

    hcam.pos.set(0.0f, 0.0f, -camZ);
    hcam.update();

    hcam.cam.aim(0.0f, 0.0f, 0.0f);

    float[] attitude = hcam.cam.attitude();

    hcam.cam.roll(-attitude[2]);
  }
}

public void CullingCCW(GL gl)
{
  gl = pgl.beginGL();
  gl.glFrontFace(GL.GL_CCW);
  gl.glEnable(GL.GL_CULL_FACE);
  pgl.endGL();
}

public void CullingCW(GL gl) {
  gl = pgl.beginGL();
  gl.glFrontFace(GL.GL_CW);
  gl.glEnable(GL.GL_CULL_FACE);
  pgl.endGL();
}

public void ResetCulling(GL gl)
{
  gl = pgl.beginGL();
  gl.glDisable(GL.GL_CULL_FACE);
  pgl.endGL();
}


public Server GetServerByID(String inID)
{
  return (Server)showSites.get(inID);
}


public void roundRect(int x, int y, int w, int h, int r) {
  rectMode(CORNER);

  int  ax, ay, hr;

  ax=x+w-1;
  ay=y+h-1;
  hr = r/2;

  rect(x, y, w, h);
  arc(x, y, r, r, radians(180.0f), radians(270.0f));
  arc(ax, y, r,r, radians(270.0f), radians(360.0f));
  arc(x, ay, r,r, radians(90.0f), radians(180.0f));
  arc(ax, ay, r,r, radians(0.0f), radians(90.0f));
  rect(x, y-hr, w, hr);
  rect(x-hr, y, hr, h);
  rect(x, y+h, w, hr);
  rect(x+w,y,hr, h);
}

public String dayExt(int day) {
  if (day == 11 || day == 12 || day == 13) {
    return("th");
  } 
  else {
    int d = day % 10;
    if (d == 1) {
      return("st");
    } 
    else if (d == 2) {
      return("nd");
    } 
    else if (d == 3) {
      return("rd");
    } 
    else {
      return("th");
    }
  }
}

public void killPerl() {
  try {
    String image = "perl.exe";
    try {
      Process p = Runtime.getRuntime().exec("tasklist");

      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line;
      while ( ( line = reader.readLine() ) != null ) {
        if ( line.matches(".*"+image+".*") ) {
          Process p2 = Runtime.getRuntime().exec("taskkill /f /im "+image);
          BufferedReader reader2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
          String line2;
          while ( (line2 = reader2.readLine()) != null) {
            println(line);
          }
          reader2.close();

          int exitVal2 = p2.waitFor();
        }
      }
      reader.close();

      int exitVal = p.waitFor();
    } 
    catch ( InterruptedException e ) {
    }
  }
  catch ( IOException e ) {
  }
}

public Integer roundUp(Integer base, Integer num) {
  return ceil(PApplet.parseFloat(base) / num) * num;
}

public Integer roundDown(Integer base, Integer num) {
  return floor(PApplet.parseFloat(base) / num) * num;
}


/*
 *
 *  NETWORK CODE
 *
 */

long clockStart = System.currentTimeMillis();
long graphStart = System.currentTimeMillis();
long zonesStart = System.currentTimeMillis();
long countStart = System.currentTimeMillis();

public void receive(byte[] data)
{
  String message = new String(data);
  String[] strQueries = message.split("\n");

  ArrayList tempArray = new ArrayList();

  for (int i = 0; i < strQueries.length; i++)
  {

    strQueries[i].trim();
    String[] strQueryData = strQueries[i].split("\t");
    
    if (strQueryData.length > 0)
    {
      {
        try 
        {
          qps++;
          qqp++;
          long clockElapsed = System.currentTimeMillis() - clockStart;

          if (clockElapsed >= 1000) {
            if (qpsHistory.size() > 0) {
              currentQPS = (Integer)qpsHistory.get(0);
              //println(" qps "+qqp*QPS_MULTIPLIER);
              //quePS = qqp*QPS_MULTIPLIER;

              qqp = 0;
            }


            clockStart = System.currentTimeMillis();
          }
          clockElapsed = System.currentTimeMillis() - zonesStart;
          if (clockElapsed >= statTimeSlider.GetCurrentTime()*1000)
          {
            CalcZones();
            zonesStart = System.currentTimeMillis();
          }
          clockElapsed = System.currentTimeMillis() - countStart;
          if (clockElapsed >= shadTimeSlider.GetCurrentTime()*1000)
          {
            for (Iterator itServer = servers.iterator(); itServer.hasNext();) 
            {
              Server s = (Server)itServer.next();
              s.ReleaseCountries();
            }
            CountriesStat();
            countStart = System.currentTimeMillis();
          }

          long graphElapsed = System.currentTimeMillis() - graphStart;

          if (graphElapsed >= 50) {
            synchronized(qpsPrep)
            {
              for (int j = 0; j < 20 && j < qpsPrep.size(); j++) {
                qpsPrep.set(j, (Integer)qpsPrep.get(j) + qps);
              }

              if (qpsHistory.size() > 200) {
                qpsHistory.subList(200, qpsHistory.size()).clear();
              }

              qpsPrep.add(0, qps);

              if (qpsPrep.size() >= 20) {
                qpsHistory.add(0, (Integer)qpsPrep.get(19) * QPS_MULTIPLIER/*QPS_MULTIPLIER*/);
              }
              //println("prepq "+qpsHistory);
            }

            qps = 0;

            graphStart = System.currentTimeMillis();
          }


          float inX = Float.parseFloat(strQueryData[0]);
          float inY = Float.parseFloat(strQueryData[1]);
          float inZ = Float.parseFloat(strQueryData[2]);

          String inIP = strQueryData[3];

          Server inServer = GetServerByID(strQueryData[4]);
          Server nearest = null;
          if (inServer != null)
          {
            boolean inAnycast = false;

            if (inIP.startsWith("204.13.250.") ||
              inIP.startsWith("204.13.251.") ||
              inIP.startsWith("208.78.70.") //||
            //inIP.startsWith("208.78.71.")
            // The above line references the unicast subnet in the Dynect pool.
            ) inAnycast = true;

            Query q = new Query(inX, inY, inZ, 1, inServer, inAnycast);
            AddQueryToZone(q);            
            int qCountry = GetCountryIndex(q.pos);
            if ( CAST == CAST_GRAD || (CAST == CAST_ANY && bOptimalHightlight) || (CAST == CAST_COUNTRY && bOptimalCountry))
            {
              nearest = GetNearestServer(q);
              if (nearest!=null)
              {
                nearest.addQPS(q);
                nearest.AddOpCountry(qCountry);
                q.resolver = nearest;                
                AddCountryQuery(q);
                AddOptimalQuery(q, nearest);
                nearest.AddInCountry(qCountry);
              }
            }
            else
            {
              AddCountryQuery(q);
              inServer.addQPS(q);
              inServer.AddInCountry(qCountry);
            }
            
            synchronized (queries) {
              queries.add(q);
            }
          }
        } 
        catch (Exception InvocationTargetException) {
        }
      }
    }
  }
}

/*
 *
 *  CLASSES
 *
 */

public class Query
{
  public Vector3D origin;
  public Vector3D origin_safe;
  public Vector3D pos;
  public Vector3D vel;
  public Vector3D tail;
  public Vector3D tail_vec;
  public Vector3D unit_vec;
  public Vector3D ang;

  public Server resolver;

  private int age = 0;
  private int density = 0;

  public float potencial;

  public boolean anycast = false;

  public Query(float inX, float inY, float inZ, int inDensity, Server inServer, boolean inAnycast)
  {
    origin = new Vector3D(inX, inY, inZ);
    origin_safe = new Vector3D(inX, inY, inZ);
    pos = new Vector3D(origin);

    unit_vec = pos.unit();
    vel      = unit_vec.times(burstSpeed);
    tail_vec = unit_vec.times(sizeTail);

    origin_safe.add(vel);

    resolver = inServer;
    density = inDensity;

    anycast = inAnycast;
    ang = new Vector3D();
    CalculateAng();
    CalcPotencial();
  }

  public void age()
  {
    // Acceleration here
    /*
		if (vel.length() < 4.0f) {
     			vel.multiplyBy(1.05);
     		}
     		*/

    this.pos.add(vel);
    if (this.age > sizeTail) {
      this.tail = this.pos.minus(this.tail_vec);
    } 
    else {
      this.tail = this.origin;
    }

    this.age++;
  }

  public void CalcPotencial()
  {          
    //Server s = null;
    //Vector3D poten = new Vector3D();
    //Vector3D tmpV = null;
    float tmpF = 0;



    Server maxServ = null;
    //float angle = 0, maxAngl = 0;          

    maxServ = GetNearestServer(this);
    if (maxServ == null)
    {
      this.potencial = 0.0f;
      //println("Query::potencial == 0");
      return;
    }

    try
    {
      tmpF = maxServ.pos.angleWith(this.pos);

      if (tmpF>MaxDist)
      {
        //              println("Lmax "+Lmax);
        //              println("MaxDist "+MaxDist);
        //              println("tmpF "+tmpF);
        potencial = 1.0f;
        return;
      }

      potencial = sqrt(tmpF*tmpF+(1.0f-tmpF*MaxPower/MaxDist)*(1.0f-tmpF*MaxPower/MaxDist))/Lmax;


      // potencial = 1- this.pos.angleWith(this.pos.plus(poten))/maxServ.pos.angleWith(this.pos);
    }
    catch (Exception e)
    {
      println("Query::CalcPotencial(), result potencial calculating error!");
    }
    //          if (abs(potencial) > 1.0f)
    //            println("|potencial| > 1");
    //println("angle: "+angle+" potencial: "+potencial+" | ");
  }

  public void CalculateAng()
  {
    Vector3D vX = new Vector3D(1,0,0),vZ = new Vector3D(0,0,1), vY = new Vector3D(0,-1,0);

    Vector3D tmpV = new Vector3D(this.pos);
    tmpV.y = 0;
    ang.x = this.pos.angleWith(vY);
    ang.y = tmpV.angleWith(vZ);
    if (tmpV.angleWith(vX) > PI/2)
      ang.y = 2*PI-ang.y;
    ang.z = 0;
  }

  public void setAge(int inAge)
  {
    this.age = inAge;
  }

  public int getAge()
  {
    return this.age;
  }

  public void setDensity(int inDensity)
  {
    this.density = inDensity;
  }

  public int getDensity()
  {
    return this.density;
  }
}

public class Server
{
  public Vector3D pos;
  public Vector3D unit_vec;
  public Vector3D ang;

  public String name;
  public String id;

  private Integer qps = 0;
  private long clockStart;
  private long graphStart;
  private float qpsMax = 0.0f, qpsMin = 0.0f;
  private float time = 0.0f;
  public float avTime = 0.0f;
  public int countriesInc[];
  public int countriesOIn[];
  private int tmpCountriesOIn[];
  private int tmpCountriesInc[];

  private int qpsAgeWindow = 50;
  private int qpsMaxAge = this.qpsAgeWindow, qpsMinAge = this.qpsAgeWindow;

  public Integer currentQPS = 0;
  //public long prevQPS;


  private int col = 0xffffffff;

  private java.util.List qpsHistory;
  private java.util.List qpsPrep;

  public float lat, lon;

  public boolean display;

  public Server(float inLat, float inLon, float inX, float inY, float inZ, String inName, String inID, int inCol)
  {
    this.countriesInc = new int[countries];
    this.tmpCountriesInc = new int[countries];
    this.countriesOIn = new int[countries];
    this.tmpCountriesOIn = new int[countries];
    for (int i = 0; i < countries; i++)
    {
      this.countriesInc[i] = 0;
      this.tmpCountriesInc[i] = 0;
      this.countriesOIn[i] = 0;
      this.tmpCountriesOIn[i] = 0;
    }
    this.qps = 0;
    this.lat = inLat;
    this.lon = inLon;

    this.name = inName;
    this.id = inID;
    this.col = inCol;

    this.pos = new Vector3D();

    this.lat = radians(inLat);
    this.lon = radians(-inLon);

    this.pos.setX( globeRadius * cos(lon) * cos(lat));
    this.pos.setY(-globeRadius * sin(lat));
    this.pos.setZ( globeRadius * cos(lat) * sin(lon));

    this.unit_vec = pos.unit();

    this.display = true;
    this.ang = new Vector3D();
    this.CalculateAng();

    this.clockStart = System.currentTimeMillis();
    this.graphStart = System.currentTimeMillis();

    this.qpsHistory = Collections.synchronizedList(new ArrayList());
    //this.qpsPrep = Collections.synchronizedList(new ArrayList());                
    //prevTime = System.currentTimeMillis();
  }

  public void AddInCountry(int cIndex)
  {
    this.tmpCountriesInc[cIndex] ++;
  }

  public void AddOpCountry(int cIndex)
  {
    this.tmpCountriesOIn[cIndex] ++;
  }

  public void ReleaseCountries()
  {
    ReleaseInCountries();
    ReleaseOInCountries();
  }

  public void ReleaseInCountries()
  {
    for (int i = 0; i < countries; i++)
    {
      this.countriesInc[i] = this.tmpCountriesInc[i];
      this.tmpCountriesInc[i] = 0;
      //this.time = 0.0f;
    }
  }

  public void ReleaseOInCountries()
  {
    for(int i = 0; i< countries; i++)
    {
      this.countriesOIn[i] = this.tmpCountriesOIn[i];
      this.tmpCountriesOIn[i] = 0;
    }
  }

  public void resetQPS()
  {
    this.qpsHistory.clear();
    this.avTime = 0.0f;
    this.currentQPS = 0;
    this.qps = 0;
    this.time = 0;
    this.clockStart = System.currentTimeMillis();
  }

  public void addQPS(Query q)
  {
    this.qps++;

    if (fNaN.equals(this.time))
      this.time = 0.0f;
    this.time += this.pos.angleWith(q.pos)*QTime1ms;
    //println(this.name+" ang " + this.pos.angleWith(q.pos)+" QTime1ms "+QTime1ms+" Exp.time " +this.pos.angleWith(q.pos)*QTime1ms+" result "+this.time);           

    long clockElapsed = System.currentTimeMillis() - this.clockStart;

    if (clockElapsed >= servTimeSlider.GetCurrentTime()*1000) {
      this.qpsHistory.add(0, (Integer)qps * QPS_MULTIPLIER);
      if (this.qpsHistory.size()>100)
        this.qpsHistory.subList(100, this.qpsHistory.size()).clear();                
      this.currentQPS = qps;//(Integer)this.qpsHistory.get(0)+(Integer)this.qpsHistory.get(1);
      if (this.currentQPS == 0)
        this.avTime = 0;
      else
        this.avTime = this.time/this.currentQPS;
      this.qps = 0;
      this.time = 0;
      this.clockStart = System.currentTimeMillis();
    }
  }

  public void RecalculatePos(float lat, float lon)
  {
    this.lat = lat;
    this.lon = lon;
    this.pos.setX( globeRadius * cos(ang.y) * cos(ang.x));
    this.pos.setY(-globeRadius * sin(ang.x));
    this.pos.setZ( globeRadius * cos(ang.x) * sin(ang.y));
  }

  public void RecalculatePos()
  {
    //            this.lat = radians(Lat);
    //	    this.lon = radians(-Lon);
    //            if (this.lon > TWOPI)
    //                this.lon -= TWOPI;
    //            if (this.lon < 0f)
    //                this.lon += TWOPI;

    //            if (this.lat
    this.pos.setX( globeRadius * cos(this.lon) * cos(this.lat));
    this.pos.setY(-globeRadius * sin(this.lat));
    this.pos.setZ( globeRadius * cos(this.lat) * sin(this.lon));
    CalculateAng();
    //            if (this.name == "eu1")
    //            {
    //            println("Eu1 coords: "+this.lat+", "+this.lon);
    //            println("Eu1 ang: "+this.ang);
    //            println("color :"+GetCountryIndex(this.pos));
    //
    //            }
  }

  public void drawQPSHistory(int _left, int _top, int _width, int _height)
  {
    int x = 0;
    Integer newMin = -1, newMax = 0;
    Integer qpsNum = 0;
    float scaled = 0.0f;
    float freshest = 0.0f;

    noFill();            
    strokeWeight(1);
    stroke(100);

    line(_left, _top, _left+_width, _top);
    line(_left, _top+_height, _left+_width, _top+_height);

    stroke(200);
    for (int i = 0; i <= _width; i += 20) 
    {
      line(_left+_width - i, _top-2, _left+_width - i, _top+2);
      line(_left+_width - i, _top+_height-2, _left+_width - i, _top+_height+2);
    }

    fill(0, 200);
    noStroke();
    rect(_left, _top, _width, _height);

    fill(255);

    textFont(fontLabel);


    //	    textAlign(LEFT, TOP);
    //	    text("-"+_width/2+"s", _left-15, _top-15);

    textAlign(RIGHT, TOP);
    text(PApplet.parseInt(this.qpsMax), _left+_width, _top-15);

    textAlign(RIGHT, TOP);
    text(PApplet.parseInt(this.qpsMin), _left+_width, _top+_height+5);

    textFont(fontLabel2, 16);
    textAlign(CENTER, TOP);
    //textSize(20);
    if (!this.display)
      fill(0xffFF0000,240);
    text(this.name, _left+_width/2, _top+_height+15);
    //textSize(12);
    fill(255);
    textFont(fontLabel);

    stroke(255);
    strokeWeight(1);
    noFill();

    if (bQPSSolid) {
      beginShape(LINES);
    } 
    else 
    {
      beginShape();
    }

    Integer allQPS=0, qpsCount=0;
    synchronized(this.qpsHistory)
    {        
      if (this.qpsHistory!=null)
        if (this.qpsHistory.size()>0)
          for (Iterator itQPSHistory = this.qpsHistory.iterator(); itQPSHistory.hasNext() && x < _width && x<100;)
          {

            qpsNum = (Integer)itQPSHistory.next();
            allQPS += qpsNum;                            
            qpsCount++;
            scaled = map(qpsNum, this.qpsMin, this.qpsMax, -_height, 0);
            if (scaled < -_height) scaled = -_height;
            if (scaled > 0) scaled = 0;


            if (freshest == 0.0f) freshest = scaled;

            //			    if (bQPSSolid) 
            //                            {
            //				stroke(255 - x);
            //				vertex(_left+_width-x, _top+_height);
            //			    } else 
            //                            {
            //				stroke(240 - x);
            //			    }
            //
            //			    vertex(_left+_width-x, abs(scaled) + _top);

            if (qpsNum > newMax) newMax = qpsNum;
            if (qpsNum < newMin || newMin == -1) newMin = qpsNum;

            x++;
          }
    }


    endShape();


    Integer roundTo = newMax / 10;

    if (roundTo <= 10) roundTo = 1;
    else if (roundTo <= 100) roundTo = 10;
    else if (roundTo <= 1000) roundTo = 100;
    else if (roundTo <= 10000) roundTo = 1000;
    else if (roundTo <= 100000) roundTo = 10000;

    this.qpsMaxAge++;
    this.qpsMinAge++;

    newMax = roundUp(newMax, roundTo);
    newMin = roundDown(newMin, roundTo);

    if (this.qpsMaxAge > this.qpsAgeWindow || newMax >= this.qpsMax) 
    {
      this.qpsMax = newMax;
      this.qpsMaxAge = 0;
    }

    if (this.qpsMinAge > this.qpsAgeWindow || newMin <= this.qpsMin || this.qpsHistory.size() < 50) 
    {
      this.qpsMin = newMin;
      this.qpsMinAge = 0;
    }


    float akk = 0;

    float medQPS;
    if (qpsCount>0)
      medQPS = PApplet.parseFloat(allQPS/qpsCount);
    else
      medQPS = 0;

    if ( this.qpsMin>= 0 && this.qpsMax>0 && this.qpsMax!=this.qpsMin)
      akk = PApplet.parseFloat(_height)/(this.qpsMax-this.qpsMin);

    if (akk<0)
      akk = 0;
    strokeWeight(1);
    noFill();
    stroke(0xffFFFFFF,240);   
    int def = PApplet.parseInt(akk*(medQPS -this.qpsMin));

    int yy = _top+ _height - def;
    int yU = _top+ (_height - def)/2;

    line(_left+_width/2, _top, _left+_width/2, yU);        // whiskers
    line(_left+_width/2, _top+_height, _left+_width/2, yy+def/2);    //whiskers

    rect(_left+_width/5, yU, 3*_width/5, yy-yU+def/2);    // box
    line(_left+_width/5, yy, _left+4*_width/5, yy);        // median


    stroke(0xffFF0000,255);
    yy = _top+ _height - PApplet.parseInt(akk*(this.currentQPS-this.qpsMin));
    line(_left+_width/3, yy, _left+2*_width/3, yy);
    noStroke();
    //            if (qpsCount > 0 && float(allQPS/qpsCount)<newMin)
    //                println(this.name +" : allQPS == "+allQPS+" | qpsCount == " + qpsCount + " | allQPS/qpsCount == "+((qpsCount>0)?float(allQPS/qpsCount):0)+" | qpsMax == "+this.qpsMax+" | qpsMin == "+this.qpsMin + " | akk == "+akk +
    //                " | _height/int(this.qpsMax-this.qpsMin) == "+( ((this.qpsMax-this.qpsMin)>0)?float(_height)/( this.qpsMax-this.qpsMin):0));
  }

  public void switchDisp()
  {
    this.display = !this.display;
    //          this.prevQPS = 0;
    this.currentQPS = 0;
    this.avTime = 0;
    if (CAST == CAST_GRAD)
    {
      this.resetQPS();
    }
    this.qpsHistory.add(0, 0);
  }

  public void CalculateAng()
  {
    Vector3D vX = new Vector3D(1,0,0),vZ = new Vector3D(0,0,1), vY = new Vector3D(0,-1,0);

    Vector3D tmpV = new Vector3D(this.pos);
    tmpV.y = 0;
    ang.x = this.pos.angleWith(vY);
    ang.y = tmpV.angleWith(vZ);
    if (tmpV.angleWith(vX) > PI/2)
      ang.y = 2*PI-ang.y;
    ang.z = 0;
  }

  public void setColor(int inCol)
  {
    this.col = inCol;
  }

  public int getColor()
  {
    return this.col;
  }
}

public class Ray
{
  public Vector3D p1;
  public Vector3D p2;

  public Ray(float x1, float y1, float z1, float x2, float y2, float z2)
  {
    p1 = new Vector3D(x1,y1,z1);
    p2 = new Vector3D(x2,y2,z2);
  }
}

public class ColorBlock
{
  public int CurrentColor;
  public int Left, Top;
  public int Width, Height;
  public boolean Checked;

  public ColorBlock(int _left, int _top, int _width, int _height, int _col)
  {
    CurrentColor = _col;
    Left = _left;
    Top = _top;
    Width = _width;
    Height = _height;
    Checked = false;
  }
  public void Draw()
  {
    noStroke();
    if (Checked)
    {      
      fill(255,255,0,255);
      rect(Left-1, Top-1, Width+2, Height+2);
      fill(CurrentColor, 255);
      rect(Left+1, Top+1, Width-2, Height-2);
      return;
    }
    fill(CurrentColor, 255);
    rect(Left-1, Top-1, Width+2, Height+2);
    fill(255);
  }
  public boolean Click(int x, int y)
  {
    if (x>Left && x<Left+Width && y>Top && y<Top+Height)
    {
      Checked = true;
    }
    else
      Checked = false;
    return Checked;
  }

  public Vector3D GetNormColor()
  {
    return new Vector3D(red(this.CurrentColor)/255.0f, green(this.CurrentColor)/255.0f, blue(this.CurrentColor)/255.0f);
  }
}

public class TimeSlider
{
  public int currentTime;
  private int width, height;
  private int left, top;
  private int minTime, maxTime;
  private int sliderPos;
  private String caption;
  public TimeSlider(String _caption, int _left, int _top, int _width, int _height, int _minT, int _currTime,  int _maxT)
  {
    this.currentTime = _currTime;
    this.minTime = _minT;
    this.maxTime = _maxT;
    this.left = _left;
    this.top = _top;
    this.width = _width;
    this.height = _height;
    this.caption = _caption;
    if (this.maxTime-this.minTime == 0.0f)
      this.sliderPos = 0;
    else
      this.sliderPos = (int)abs(PApplet.parseFloat(this.currentTime-this.minTime)/PApplet.parseFloat(this.maxTime-this.minTime)*(PApplet.parseFloat(this.width)));
  }
  public int GetCurrentTime()
  {
    return this.currentTime;
  }
  public int GetMinTime()
  {
    return this.minTime;
  }
  public int GetMaxTime()
  {
    return this.maxTime;
  }
  public void SetCurrentTime(int t)
  {
    if (t < this.minTime)
      t = this.minTime+1;
    if (t < this.maxTime)
      this.currentTime = t;
    else
      this.currentTime = this.maxTime-1;
    if (this.maxTime-this.minTime == 0.0f)
      this.sliderPos = 0;
    else
      this.sliderPos = (int)abs(PApplet.parseFloat(this.currentTime-this.minTime)/PApplet.parseFloat(this.maxTime-this.minTime)*(PApplet.parseFloat(this.width)));
  }
  public void SetMinTime(int t)
  {
    this.minTime = t;
    if (this.currentTime < this.minTime)
      this.currentTime = this.minTime+1;
    if (this.maxTime-this.minTime == 0.0f)
      this.sliderPos = 0;
    else
      this.sliderPos = (int)abs(PApplet.parseFloat(this.currentTime-this.minTime)/PApplet.parseFloat(this.maxTime-this.minTime)*(PApplet.parseFloat(this.width)));
  }
  public void SetMaxTime(int t)
  {
    this.maxTime = t;
    if (this.currentTime > this.maxTime)
      this.currentTime = this.maxTime-1;
    if (this.maxTime-this.minTime == 0.0f)
      this.sliderPos = 0;
    else
      this.sliderPos = (int)abs(PApplet.parseFloat(this.currentTime-this.minTime)/PApplet.parseFloat(this.maxTime-this.minTime)*(PApplet.parseFloat(this.width)));
  }

  public int GetLeft()
  {
    return this.left;
  }

  public int GetTop()
  {
    return this.top;
  }

  public void MoveTo(int _left, int _top)
  {
    this.left = _left;
    this.top = _top;
  }

  public int GetWidth()
  {
    return this.width;
  }

  public int GetHeight()
  {
    return this.height;
  }

  public boolean Click(int x, int y)
  {
    x = x - this.left;
    y = y - this.top;
    if (x >= 0 && x <= this.width && y >= 0 && y <= this.height)
    {
      this.SetSliderPos(x);
      return true;
    }
    return false;
  }
  public void Draw()
  {
    noFill();
    stroke(0xffFFFFFF, 255);
    rect(this.left, this.top, this.width, this.height);
    fill(255, 255, 255, 240);
    rect(this.left+this.sliderPos-2, this.top, 4, this.height);
    textFont(fontLabel);
    fill(255, 255, 255, 240);
    textAlign(RIGHT, CENTER);
    text(this.minTime, this.left-6, this.top+this.height/2);
    text(this.caption, this.left-6-textWidth(nfs(this.minTime,0))-5, this.top+this.height/2);
    textAlign(LEFT, CENTER);
    text(this.maxTime, this.left+this.width+6, this.top+this.height/2);
    text(this.currentTime, this.left+this.width+6+textWidth(nfs(this.maxTime,0))+12, this.top+this.height/2);
  }

  private void SetSliderPos(int x)
  {
    int tmpI = (int)abs(PApplet.parseFloat(x)/(PApplet.parseFloat(this.width))*((float)(this.maxTime-this.minTime)))+this.minTime;
    this.currentTime = tmpI;
    this.sliderPos = x;
  }
}

// ADDITIONAL FUNCTIONS FOR SOMETHING ))

public int GetQueryZone(Query q)
{
  if (q.ang.x < 0.96079654f && q.ang.x > 0.3107915f && q.ang.y < 2.5807953f && q.ang.y > 1.390796f)
    return ZONE_EU;
  if (q.ang.x < 1.3707962f && q.ang.x > 0.4907916f && (q.ang.y > 5.163984f || q.ang.y < 0.6507965f))
    return ZONE_NA;
  if (q.ang.x < 1.4007963f && q.ang.x > 0.28079706f && q.ang.y < 4.7307935f && q.ang.y > 2.6107953f)
    return ZONE_AS;
  return ZONE_UN;
}

public void AddQueryToZone(Query q)
{
  int zone = GetQueryZone(q);
  //    if (zone == ZONE_UN)
  //        return;
  Server s;
  if ( CAST == CAST_GRAD)
  {
    s = GetNearestServer(q);
  }
  else
    s = q.resolver;

  if (s!=null)
    s.addQPS(q);
  else
    return;

  float time = s.pos.angleWith(q.pos)*QTime1ms;
  if (fNaN.equals(time))
    time = 0;
  //println("ang: "+s.pos.angleWith(q.pos)+"\t\ttime: "+time+"\t\tS: "+s.pos+"\tQ: "+q.pos);
  switch (zone)
  {
  case ZONE_EU:
    tmpTimeEu += time;
    tmpQPSEu++;
    break;
  case ZONE_NA:
    tmpTimeNA += time;
    tmpQPSNA++;
    break;
  case ZONE_AS:
    tmpTimeAs += time;
    tmpQPSAs++;
  }
  tmpTimeGl += time;

  //if (str(tmpTimeGl) == "NaN")

  //println("T"+time+"\t\tS"+s+"\t\tQ"+q);

  tmpQPSGl += 1;
}

public void CalcZones()
{
  bCanUseGlobalStat = false;

  if (tmpQPSEu!=0)
    avTimeEu = tmpTimeEu/tmpQPSEu;
  else
    avTimeEu = 0.0f;

  if (tmpQPSNA!=0)
    avTimeNA = tmpTimeNA/tmpQPSNA;
  else
    avTimeNA = 0.0f;      

  if (tmpQPSAs!=0)
    avTimeAs = tmpTimeEu/tmpQPSAs;
  else
    avTimeAs = 0.0f;    
  //  println("tmpQPSGl: "+tmpQPSGl+ "  tmpTimeGl: "+tmpTimeGl);

  if (tmpQPSGl!=0 && !fNaN.equals(tmpTimeGl)&& !fNaN.equals(tmpQPSGl))
    avTimeGl = tmpTimeGl/tmpQPSGl;
  else
  {
    avTimeGl = 0.0f;
    //println(avTimeGl);
  }
  qpsGl = tmpQPSGl;
  qpsEu = tmpQPSEu;
  qpsAs = tmpQPSAs;
  qpsNA = tmpQPSNA;


  //    print("!"+tmpQPSGl+" "+tmpTimeGl);
  tmpTimeGl = 0.0f;
  tmpTimeAs = 0.0f;
  tmpTimeNA = 0.0f;
  tmpTimeEu = 0.0f;
  tmpQPSAs = 0;
  tmpQPSEu = 0;
  tmpQPSNA = 0;
  tmpQPSGl = 0;
  bCanUseGlobalStat = true;
}

public void ResetGlobalStatistic()
{
  bCanUseGlobalStat = false;

  tmpTimeGl = 0.0f;
  tmpTimeAs = 0.0f;
  tmpTimeNA = 0.0f;
  tmpTimeEu = 0.0f;
  tmpQPSAs = 0;
  tmpQPSEu = 0;
  tmpQPSNA = 0;
  tmpQPSGl = 0;

  avTimeGl = 0.0f;
  avTimeAs = 0.0f;
  avTimeEu = 0.0f;
  avTimeNA = 0.0f;

  qpsGl = tmpQPSGl;
  qpsEu = tmpQPSEu;
  qpsAs = tmpQPSAs;
  qpsNA = tmpQPSNA;

  for (Iterator itServer = servers.iterator(); itServer.hasNext();) 
  {
    Server s = (Server)itServer.next();
    s.resetQPS();
  }

  bCanUseGlobalStat = true;
}

public int GetCountryIndex(Vector3D pos)
{
  return GetCountryIndex(pos.x, pos.y, pos.z);
}

public int GetCountryIndex(float x, float y, float z)
{
  Vector3D vX = new Vector3D(-1,0,0),vZ = new Vector3D(0,0,1), vY = new Vector3D(0,-1,0);
  Vector3D ang = new Vector3D();
  Vector3D pos = new Vector3D(x, y, z);
  Vector3D tmpV = new Vector3D(x, y, z);
  tmpV.y = 0;
  ang.x = pos.angleWith(vY);
  ang.y = tmpV.angleWith(vX);
  if (tmpV.angleWith(vZ) > PI/2)
    ang.y = 2*PI-ang.y;
  ang.z = 0;
  PImage maskT = texs.getMaskTex();

  int retC = maskT.get(PApplet.parseInt(ang.y/TWOPI*maskT.width), PApplet.parseInt((ang.x*2)/TWOPI*maskT.height));
  if (green(retC) == 255 && blue(retC) == 0)
    return (int)red(retC);
  else
    return -1;
}

public void AddCountryQuery(Query q)
{
  float time = q.resolver.pos.angleWith(q.pos)*QTime1ms;
  int ind = GetCountryIndex(q.pos);
  countryQPST[ind] ++;
  if (fNaN.equals(time))
    return;
  countryAvT[ind] +=time;
}

public void AddOptimalQuery(Query q, Server nearest)
{
  float time = nearest.pos.angleWith(q.pos)*QTime1ms;
  int ind = GetCountryIndex(q.pos);
  //countryQPST[ind] ++;
  if (fNaN.equals(time))
    return;
  optimalAvT[ind] +=time;
}

public void CountriesStat()
{
  for(int i = 0; i< countries; i++)
  {
    countryQPS[i] = countryQPST[i];
    if (countryQPS[i]>0.0f && !fNaN.equals(countryQPS[i]))
    {
      countryAv[i] = countryAvT[i]/countryQPS[i];
      optimalAv[i] = optimalAvT[i]/countryQPS[i];
    }
    else
    {
      countryAv[i] = -1.0f;
      optimalAv[i] = -1.0f;
    }
    countryAvT[i] = 0.0f;
    countryQPST[i] = 0.0f;
    optimalAvT[i] = 0.0f;
  }
  bShaderDataUpdate = true;
}

public Vector3D GetCountryStatColor(int country)
{
  if (countryAv[country] == -1.0f)
    return new Vector3D(red(cbSZero.CurrentColor)/255f,green(cbSZero.CurrentColor)/255f,blue(cbSZero.CurrentColor)/255f);
  float scal = 0f;
  if (countryAv[country]> maxAvT)
    scal = 1;
  if (countryAv[country] > minAvT)
    scal = (countryAv[country]-minAvT)/(maxAvT-minAvT);

  float _r = red(cbSStart.CurrentColor);
  float _g = green(cbSStart.CurrentColor);
  float _b = blue(cbSStart.CurrentColor);

  float r = red(cbSEnd.CurrentColor);
  float g = green(cbSEnd.CurrentColor);
  float b = blue(cbSEnd.CurrentColor);


  r = (_r-(_r-r)*scal);
  g = (_g-(_g-g)*scal);
  b = (_b-(_b-b)*scal);
  return new Vector3D(r/255f, g/255f, b/255f);
}

public void InitTextures()
{
  texs = new Textures();
  texs.LoadTextures();

  PImage mTex = texs.getMaskTex();
  String tmpS[] = loadStrings("ColorsAssign.txt");
  //        ArrayList arr = new ArrayList();
  int clr[] = new int[tmpS.length];
  countriesNames = new String[tmpS.length];
  for (int i = 0; i < tmpS.length; i++)
  {
    clr[i] = (int)unhex("FF"+tmpS[i].substring(0,6));
    //            println(hex(unhex("FF"+tmpS[i].substring(0,6))));
    countriesNames[i] = tmpS[i].substring(7);
  }
  //        int offs = 0;
  //        for (int i =0; i< mTex.width; i++)
  //            for (int j = 0; j< mTex.height; j++)
  //            {
  //                int k = 0;
  //                color c = mTex.get(i, j);
  //                while (k< offs && c!=(color)#FFFFFF && c!=(color)#000000 && c!= clr[k] )
  //                    k++;
  //                if (k==offs && c!=clr[k])
  //                {
  //                    clr[offs++] = c;
  //                }                
  //            }
  //        countries = offs;
  countries = tmpS.length;
  for (int i = 0; i< mTex.width; i++)
    for(int j =0; j< mTex.height; j++)
    {
      int k = 0;
      int c = mTex.get(i, j);
      while (k < countries && c!=clr[k])
        k++;
      if (k < countries && c == clr[k] && c!=(int)0xffFFFFFF && c!=(int)0xff000000)
        mTex.set(i, j, color(k,255,0));
    }
  countryAv = new float[countries];
  countryQPS = new float[countries];
  optimalAv = new float[countries];
  countryAvT = new float[countries];
  countryQPST = new float[countries];
  optimalAvT = new float[countries];
  //selectedCountries = new int[countries];
  for (int i = 0; i < countries; i++)
  {
    countryAv[i] = -1.0f;
  }
}

public String SArr2S(String[] sarr)
{
  if (sarr.length ==0)
    return null;
  String retS = sarr[0];
  for (int i = 1; i < sarr.length; i++)
    retS += "\n"+sarr[i];
  return retS;
}

public Vector3D ProjectScreenToGlobe(GL gl, int x, int y, Vector3D eyePos)
{
  //gl.glClear(GL.GL_COLOR_BUFFER_BIT);
  int viewport[] = new int[4];

  double mvmatrix[] = new double[16];
  double projmatrix[] = new double[16];
  int realy = 0;// GL y coord pos
  double wcoord[] = new double[4];// wx, wy, wz;// returned xyz coords
  Vector3D retV;


  gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
  //    println("===================================================");
  //    PrintMatrix("viewport",viewport, 1, 4);
  gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, mvmatrix, 0);
  //    PrintMatrix("mvmatrix",mvmatrix, 4, 4);
  gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projmatrix, 0);
  //    PrintMatrix("projmatrix",projmatrix, 4, 4);
  //    println("===================================================");
  /* note viewport[3] is height of window in pixels */
  realy = viewport[3] - (int) y - 1;
  //    System.out.println("Coordinates at cursor are (" + x + ", " + realy);
  glu.gluUnProject((double) x, (double) realy, -1.0f, //
  mvmatrix, 0,
  projmatrix, 0, 
  viewport, 0, 
  wcoord, 0);

  Vector3D z0 = new Vector3D((float)wcoord[0], (float)wcoord[1], (float)wcoord[2]);
  glu.gluUnProject((double) x, (double) realy, 1.0f, //
  mvmatrix, 0,
  projmatrix, 0,
  viewport, 0, 
  wcoord, 0);
  Vector3D z1 = new Vector3D((float)wcoord[0], (float)wcoord[1], (float)wcoord[2]);

  z1.subtract(z0);
  z1.normalize();


  Vector3D V = new Vector3D(z1);
  Vector3D O = new Vector3D(0.0f, 0.0f, 0.0f);
  Vector3D P0 = new Vector3D(eyePos);
  Vector3D L = O.minus(P0);
  float tca = L.dot(V);
  if (tca < 0)
    return null;
  float Dd = L.dot(L)-tca*tca;
  if (Dd > globeRadius*globeRadius)
    return null;
  float thc = sqrt(globeRadius*globeRadius - Dd);
  float t = tca-thc;

  gl.glFlush();
  retV = P0.plus(V.times(t));
  retV = UseModelTransform(new double[] {
    retV.x, retV.y, retV.z
  }
  );
  return retV;//P0.plus(V.times(t));
}

public Vector3D UseModelTransform(double coords[])
{
  double afterX[] = new double[3];
  double afterY[] = new double[3];
  //    double rotMat[] = {cos(radians(-rotationY)), 0, sin(radians(-rotationY)),//
  //                        sin(radians(-rotationX))*sin(radians(-rotationY)), cos(radians(-rotationX)), -sin(radians(-rotationX))*cos(radians(-rotationY)), //
  //                        -cos(radians(-rotationX))*sin(radians(-rotationY)), sin(radians(-rotationX)), cos(radians(-rotationX))*cos(radians(-rotationY)) };
  double rotX[] = {
    1.0f, 0.0f, 0.0f,//
    0.0f, cos(radians(-rotationX)), -sin(radians(-rotationX)),//
    0.0f, sin(radians(-rotationX)), cos(radians(-rotationX))
    };

  double rotY[] = {
    cos(radians(rotationY)), 0.0f, -sin(radians(rotationY)),//
    0.0f, 1.0f, 0.0f,//
    sin(radians(rotationY)), 0.0f, cos(radians(rotationY))
    };
    //    rotMat[0] = cos(radians(rotationY))
    //println("NewCoords: ");
    for (int i=0; i < 3; i++)
    {
      afterX[i] = 0.0f;
      for (int j = 0; j < 3; j++)
      {
        afterX[i] += rotX[i*3+j]*coords[j];
      }
      //println(tmpC[i]);
    }
  for (int i=0; i < 3; i++)
  {
    afterY[i] = 0.0f;
    for (int j = 0; j < 3; j++)
    {
      afterY[i] += rotY[i*3+j]*afterX[j];
    }
    //println(tmpC[i]);
  }   

  return new Vector3D((float)afterY[0], (float)afterY[1], (float)afterY[2]);
}

public void HighlightCountry()
{
  Vector3D retV = ProjectScreenToGlobe(pgl.gl, mouseX, mouseY, hcam.pos);
  if (retV == null)
  {
    highlightedCountry = -1;
    return;
  }
  highlightedCountry = GetCountryIndex(retV);
}

public void drawFPS()
{

  now += 1;
  long fpsCurrTime = System.currentTimeMillis();
  if (fpsCurrTime - fpsStartTime > 1000.0f )
  {
    fps = now;
    now = 0;
    fpsStartTime = System.currentTimeMillis();
  }

  if (bFPS)
  {
    textFont(fontDate);
    fill(255, 0, 10, 250);
    textAlign(LEFT, BOTTOM);
    text("FPS: "+fps,2,height-1 );
  }
}

public void PrintMatrix(String mName, int[] mas, int rows, int lines)
{
  println(mName);
  String lineout = new String();
  for (int i = 0; i<lines; i++)
  {
    for (int j = 0; j< rows; j++)
    {
      lineout += mas[j*rows+i];
      lineout += "\t";
    }
    println(lineout);
    lineout = "";
  }
  println();
}

public void PrintMatrix(String mName, float[] mas, int rows, int lines)
{
  println(mName);
  String lineout = new String();
  for (int i = 0; i<lines; i++)
  {
    for (int j = 0; j< rows; j++)
    {
      lineout += mas[j*rows+i];
      lineout += "\t";
    }
    println(lineout);
    lineout = "";
  }
  println();
}

public void PrintMatrix(String mName, double[] mas, int rows, int lines)
{
  println(mName);
  String lineout = new String();
  for (int i = 0; i<lines; i++)
  {
    for (int j = 0; j< rows; j++)
    {
      lineout += mas[j*rows+i];
      lineout += "\t\t";
    }
    println(lineout);
    lineout = "";
  }
  println();
}

public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)value,
                (byte)(value >>> 8),
                (byte)(value >>> 16),
                (byte)(value >>> 24)};
}

public static final int byteArray2ToInt(byte [] b) {
        return 0+((b[1] & 0xFF) << 8)
                + (b[0] & 0xFF);
}

public static final int byteArrayToInt(byte [] b) {
        return (b[3] << 24)
                + ((b[2] & 0xFF) << 16)
                + ((b[1] & 0xFF) << 8)
                + (b[0] & 0xFF);
}

//public static final short byteArrayToShort(byte [] b) {
//        return (b[1] << 8)
//                + (short)(b[0] & 0xFF);
//}













public class PCAPSender implements Runnable
{
  public PCAPSender(Object _owner)
  {
    this.owner = _owner;

    //    try {
    //			if ( owner instanceof PApplet ) 
    //				((PApplet)owner).registerDispose( this );
    //		}
    //		catch( NoClassDefFoundError e ) {;}
  }

  private void callReceiveHandler( byte[] data ) 
  throws NoSuchMethodException {

    Class[] types;		// arguments class types
    Object[] values;	// arguments values
    Method method;

    try {
      types	= new Class[] { 
        data.getClass()
        };
      values	= new Object[] { 
        data
      };
      method	= owner.getClass().getDeclaredMethod(receiveHandler, types);
      method.invoke( owner, values );
    }
    catch( IllegalAccessException e ) { 
      ;
    }
    catch( InvocationTargetException e ) { 
      e.printStackTrace();
    }
  }


  private boolean isUDP() throws IOException
  {
    try
    {
      curFPos = pktOffs;
      packet = new byte[ethOffs+ipOffs];
      byte[] tmp = new byte[3];
      if ((ethOffs+ipOffs)> this.in.read(packet, 0, ethOffs+ipOffs))
        throw new IOException("EOF");
      curFPos += ethOffs+ipOffs;
      System.arraycopy(packet, ethOffs+0x0C, tmp, 0,  2);
      if (tmp[0] == (byte)0x08 && tmp[1] == (byte)0x00 )
      {
        int step = pktOffs+ethOffs+ipOffs+ipInPrtOffs-curFPos;
        if (step > this.in.skip(step))
          throw new IOException("EOF");
        if (this.in.read(tmp, 0, 1) <1)
          throw new IOException("EOF");
        curFPos += step+1;
        if (tmp[0] == (byte)0x11)
        {
          isudp = true; 
          return true;
        }
      }
    }
    catch(Exception e)
    {
      println("PCAPSender.isUDP(), error: "+e);
      throw new IOException("EOF");
    }
    isudp = false;
    return false;
  }

  private byte[] get_pkt() throws IOException
  {
    if (isudp)
    {
      int c = 0;
      byte[] retA= new byte[currPktLen-dataOffs+ethOffs];
      try
      {
        if (curFPos < pktOffs+dataOffs)    
        {    
          if ((pktOffs+dataOffs-curFPos)> this.in.skip(pktOffs+dataOffs-curFPos))
            throw new IOException("EOF");
          curFPos+=pktOffs+dataOffs-curFPos;
        }
        //println("curFPos: "+hex(curFPos)+"\npktOffs: "+hex(pktOffs)+"\ndata offs "+hex(dataOffs));
        c = this.in.read(retA);
        if (c > currPktLen-dataOffs+ethOffs)
          throw new IOException("EOF");
        //println("curpos: "+hex(curFPos));
        //println("dataLen: "+hex(currPktLen-(ipOffs+udpOffs+udpDtaOffs)));
        //        for (int i = 0; i<retA.length; i++)
        //          print(char(retA[i]));
        //println();
        if (c == -1)
          return null;
        curFPos += c;
        // //println(char(retA));
        return retA;
      }
      catch(Exception e)
      {
        println("PCAPSender.get_pkt, error: "+e);
        return null;
      }
    }
    return null;
  }

  private boolean read_pkt_hdr() throws IOException
  {
    isUDP();
    try
    {

      byte[] tmp = new byte[4];
      System.arraycopy(packet, 0, tmp, 0, 4);
      hiTS = byteArrayToInt(tmp);
      //println("hiTS : "+hiTS);
      System.arraycopy(packet, 4, tmp, 0,  4);
      loTS = byteArrayToInt(tmp);
      //println("loTS : "+loTS);
      System.arraycopy(packet, 8, tmp, 0,  4);
      currPktLen = byteArrayToInt(tmp);
      //println("currPktLen : "+currPktLen);
      nextTime = startTime + (hiTS-shiTS)*1000+(loTS-sloTS)/1000;
      //println("nextTime : "+nextTime);
    }
    catch(Exception e)
    {
      println("PCAPSender.read_pkt_hdr(), error: "+e);
    }


    return isudp;
  }

  private void find_next_pkt() throws IOException
  {
    do
    {      
      pktOffs += currPktLen+ethOffs;
      if (curFPos < pktOffs)
      {
        try
        {
          if ((pktOffs-curFPos)> this.in.skip(pktOffs-curFPos))
            throw new IOException("EOF");
          //println("next packet searching. curFPos: "+hex(curFPos)+"\npktOffs: "+hex(pktOffs)+"\nskipping: "+hex(pktOffs-curFPos));
        }
        catch(Exception e)
        {
          println("En error appeared while next pkt searching!! "+e);
          break;
        }
        curFPos += pktOffs-curFPos;
        //println("Next packet, curFPos: "+hex(curFPos)+"\npktOffs at new pkt: "+hex(pktOffs));
      }
      ////println("find pkt :"+pktOffs);
      //println("Searching packet...");
    }
    while(!read_pkt_hdr());
    //println("Packet Got");
  }

  public void run()
  {
    started =true;  
    //      this.thread = new Thread(this);
    //      thread.start();
    startTime = System.currentTimeMillis();
    nextTime = startTime;

    long tme;
    while (true)
    {      
      try
      {
        tme = System.currentTimeMillis();
        try
        {
          ////println("te : "+tme);
          if(tme<nextTime-50)
          {
            tme = nextTime- System.currentTimeMillis() - 40;
            thread.sleep(tme);
          }
        }
        catch(Exception e)
        {
          //println("PAUSE");
        }
        if (tme >= nextTime)
        {
          //println("Thread, sending");
          byte[] sendP = get_pkt();
          if (sendP == null)
            continue;
          //println("Sending packet...");
          try
          {          
            callReceiveHandler(sendP);
          }
          catch(NoSuchMethodException e)
          {
            println("NO "+receiveHandler+" method in dinvyz class!!!");
          }

          find_next_pkt();
        }
      }
      catch(IOException ioe)
      {

        println("Exception: "+ioe);
        if (this.repeat)
        {
          println("Reloading the file");
          this.openPCAP();
          startTime = System.currentTimeMillis();
          nextTime = startTime;
        }
        else        
          return;
      }
    }
    
  }

  public void dispose()
  {
    try
    {
      this.thread.interrupt();
      this.in.close();
      this.in = null;
      this.thread = null;
    }
    catch(Exception e)
    {
      println("PCAPSender.dispose() throwed exception "+e);
    }
  }

  private boolean openPCAP()
  {
    try
    {
      if (this.in != null)
      {        
        this.curFPos = 0;
        this.pktOffs = 0x18;
        this.in.close();
        this.in = null;
        //this.thread.interrupt();
        //println("PCAPSender.openPCAP, thread interrupted");
        //this.thread = null;
        //println("PCAPSender.openPCAP, thread nulled");
      }
      this.in = new FileInputStream(pcapFile);
      println("PCAPSender.openPCAP, PCAP file loaded: "+this.in);
      byte[] bbb = new byte[4];
      in.read(bbb);
      int magic = byteArrayToInt(bbb);
      curFPos += bbb.length;
      ////println("magic "+hex(magic));
      if (magic != 0xA1B2C3D4)
      {
        println("PCAPSender.openPCAP, wrong magic number!");
        //bytes = null;
        
        return false;
      }
      println("PCAPSender.openPCAP, correct magic number!");
      in.skip(pktOffs-curFPos);
      //println("skipped "+hex(pktOffs-curFPos)+" bytes(+4 -cPos)");
      curFPos = pktOffs;

      println("PCAPSender.openPCAP, reading header");
      read_pkt_hdr();
      shiTS= hiTS;
      sloTS = loTS;
      println("PCAPSender.openPCAP, header has read");
    }
    catch (Exception e)
    {
      println("PCAPSender.openPCAP, Exception: "+e);
      return false;
    }
    if (!this.started)
    {
      this.thread = new Thread(this);
      this.thread.start();
    }
    println("GoodFile");
    return true;
  }

  // private byte[] bytes;
  Thread thread	= null;
  private byte[] packet;
  private boolean isudp = false;
  private Object owner = null;

  private final String pcapFile = "C:\\Users\\kgray\\Desktop\\My Stuff\\DynViz\\sample_data.pcap";

  private int curFPos = 0;
  private int pktOffs = 0x18;
  private  int ethOffs = 0x10;
  private int ipOffs = 0x0E;
  private int udpOffs = 0x14;
  private int ipInPrtOffs = 0x09;
  private int udpDtaOffs = 0x08;
  private int udpPktLengthAt = 0x04;
  private int currPktLen = 0;
  private  int dataOffs = ethOffs+ipOffs+udpOffs+udpDtaOffs;

  private String receiveHandler = "receive";

  private long nextTime = 0;
  private int hiTS = 0;
  private int loTS = 0;

  private int shiTS = 0;
  private int sloTS = 0;
  private long startTime;
  public boolean started = false;
  
  // PCAP file send looping
  public boolean repeat = true;

  private FileInputStream in = null;
}







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



public class ArcBall {

  PApplet parent;
  
  float center_x, center_y, center_z, radius;
  public Vec3 v_down, v_drag;
  Quat q_now, q_down, q_drag;
  Vec3[] axisSet;
  int axis;

  /** defaults to radius of min(width/2,height/2) and center_z of -radius */
  public ArcBall(PApplet parent) {
    this(parent.g.width/2.0f,parent.g.height/2.0f,-PApplet.min(parent.g.width/2.0f,parent.g.height/2.0f),PApplet.min(parent.g.width/2.0f,parent.g.height/2.0f), parent);
  }

  public ArcBall(float center_x, float center_y, float center_z, float radius, PApplet parent) {

    this.parent = parent;

    parent.registerMouseEvent(this);
    parent.registerPre(this);

    this.center_x = center_x;
    this.center_y = center_y;
    this.center_z = center_z;
    this.radius = radius;

    v_down = new Vec3();
    v_drag = new Vec3();

    q_now = new Quat();
    q_down = new Quat();
    q_drag = new Quat();

    axisSet = new Vec3[] { 
		new Vec3(1.0f, 0.0f, 0.0f), new Vec3(0.0f, 1.0f, 0.0f), new Vec3(0.0f, 0.0f, 1.0f) };
		axis = -1;  // no constraints...
	}
	
	public void setOrigin(float center_x, float center_y, float center_z, float radius) {
	    this.center_x = center_x;
	    this.center_y = center_y;
	    this.center_z = center_z;
	    this.radius = radius;
	}

	public void mouseEvent(MouseEvent event) {
		int id = event.getID();
		if (id == MouseEvent.MOUSE_DRAGGED) {
			mouseDragged();
		} 
		else if (id == MouseEvent.MOUSE_PRESSED) {
			mousePressed();
		}
	}

	public void mousePressed() {
		/*
		float s_x = screenX(globeRadius / 2, 0, 0);
		float s_y = screenY(0, globeRadius / 2, 0);

		println("s_x : (" + s_x + ")");

		float m_x = map(parent.mouseX, 0, width, -s_x, s_x);
		float m_y = map(parent.mouseY, 0, height, -s_y, s_y);
		*/
		v_down = mouse_to_sphere(parent.mouseX, parent.mouseY);

		q_down.set(q_now);
		q_drag.reset();
	}

	public void mouseDragged() {
		v_drag = mouse_to_sphere(parent.mouseX, parent.mouseY);
		q_drag.set(Vec3.dot(v_down, v_drag), Vec3.cross(v_down, v_drag));
	}

	public void pre() {
		parent.translate(center_x, center_y, center_z);
		q_now = Quat.mul(q_drag, q_down);
		applyQuat2Matrix(q_now);
		parent.translate(-center_x, -center_y, -center_z);

		parent.pushMatrix();
			//println("v_drag: (" + v_drag.x + ", " + v_drag.y + ", " + v_drag.z + ")");
			parent.translate(v_drag.x, v_drag.y, v_drag.z);
			parent.fill(255, 0, 0);
			parent.sphere(20);
		parent.popMatrix();
	}

	public Vec3 mouse_to_sphere(float x, float y) {
		Vec3 v = new Vec3();
		v.x = (x - center_x) / radius;
		v.y = (y - center_y) / radius;

		float mag = v.x * v.x + v.y * v.y;
		if (mag > 1.0f) {
			  v.normalize();
		}
		else {
			  v.z = PApplet.sqrt(1.0f - mag);
		}

		return (axis == -1) ? v : constrain_vector(v, axisSet[axis]);
	}

	public Vec3 constrain_vector(Vec3 vector, Vec3 axis) {
		Vec3 res = new Vec3();
		res.sub(vector, Vec3.mul(axis, Vec3.dot(axis, vector)));
		res.normalize();
		return res;
	}

	public void applyQuat2Matrix(Quat q) {
		// instead of transforming q into a matrix and applying it...

		float[] aa = q.getValue();
		parent.rotate(aa[0], aa[1], aa[2], aa[3]);
	}
}

static class Vec3 {
	float x, y, z;

	Vec3() {}

	Vec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void normalize() {
		float length = length();
		x /= length;
		y /= length;
		z /= length;
	}

	public float length() {
		return PApplet.mag(x,y,z);
	}

	public static Vec3 cross(Vec3 v1, Vec3 v2) {
		Vec3 res = new Vec3();
		res.x = v1.y * v2.z - v1.z * v2.y;
		res.y = v1.z * v2.x - v1.x * v2.z;
		res.z = v1.x * v2.y - v1.y * v2.x;
		return res;
	}

	public static float dot(Vec3 v1, Vec3 v2) {
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}

	public static Vec3 mul(Vec3 v, float d) {
		Vec3 res = new Vec3();
		res.x = v.x * d;
		res.y = v.y * d;
		res.z = v.z * d;
		return res;
	}

	public void sub(Vec3 v1, Vec3 v2) {
		x = v1.x - v2.x;
		y = v1.y - v2.y;
		z = v1.z - v2.z;
	}

} // Vec3

static class Quat {
		
	float w, x, y, z;

	Quat() {
		reset();
	}

	Quat(float w, float x, float y, float z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void reset() {
		w = 1.0f;
		x = 0.0f;
		y = 0.0f;
		z = 0.0f;
	}

	public void set(float w, Vec3 v) {
		this.w = w;
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public void set(Quat q) {
		w = q.w;
		x = q.x;
		y = q.y;
		z = q.z;
	}

	public static Quat mul(Quat q1, Quat q2) {
		Quat res = new Quat();
		res.w = q1.w * q2.w - q1.x * q2.x - q1.y * q2.y - q1.z * q2.z;
		res.x = q1.w * q2.x + q1.x * q2.w + q1.y * q2.z - q1.z * q2.y;
		res.y = q1.w * q2.y + q1.y * q2.w + q1.z * q2.x - q1.x * q2.z;
		res.z = q1.w * q2.z + q1.z * q2.w + q1.x * q2.y - q1.y * q2.x;
		return res;
	}

	public float[] getValue() {
		float[] res = new float[4];

		float sa = (float) Math.sqrt(1.0f - w * w);
		if (sa < PApplet.EPSILON) {
		sa = 1.0f;
		}

		res[0] = (float) Math.acos(w) * 2.0f;
		res[1] = x / sa;
		res[2] = y / sa;
		res[3] = z / sa;

		return res;
	}

} // Quat


class HandyCam {
	public Camera cam;
	public Vector3D pos;
	public Vector3D unit;
	
	HandyCam (float x, float y, float z) {
		this.pos = new Vector3D(x, y, z);
		this.unit = this.pos.unit();
	}
	
	public void update() { 
		if (this.cam != null) {
			this.cam.jump(this.pos.x, this.pos.y, this.pos.z);
		}
	}
	
	public void crane(float units) {
		Vector3D offset = this.unit.times(units);
		this.pos.add(offset);
		this.update();
	}
	
	public void craneTo(Vector3D v) {
		this.pos.set(v);
		this.update();
	}
}



float SINCOS_PRECISION = 0.01f;
int SINCOS_LENGTH = PApplet.parseInt(360.0f / SINCOS_PRECISION);

float TWOPI = 6.283185308f;


// Sphere stuff
PImage texmap;

// Sphere stuff
int sDetail = 30; //Sphere detail setting

int imgPadding = 00;

int tw = 0, th = 0;

float texCoords[] = new float[1860*2];

float[] cx, cz, sphereX, sphereY, sphereZ;
int vertCount;
float sinLUT[];
float cosLUT[];

public void initializeSphere(/*String mapfile*/)
{
	sinLUT = new float[SINCOS_LENGTH];
	cosLUT = new float[SINCOS_LENGTH];
	
	for (int i = 0; i < SINCOS_LENGTH; i++) {
		sinLUT[i] = (float) Math.sin(i * DEG_TO_RAD * SINCOS_PRECISION);
		cosLUT[i] = (float) Math.cos(i * DEG_TO_RAD * SINCOS_PRECISION);
	}
	
	float delta = (float)SINCOS_LENGTH / sDetail;
	float[] cx = new float[sDetail];
	float[] cz = new float[sDetail];
	
	// Calc unit circle in XZ plane
	for (int i = 0; i < sDetail; i++) {
		cx[i] = -cosLUT[(int) (i * delta) % SINCOS_LENGTH];
		cz[i] = sinLUT[(int) (i * delta) % SINCOS_LENGTH];
	}
	
	// Computing vertexlist vertexlist starts at south pole
	vertCount = sDetail * (sDetail - 1) + 2;
	int currVert = 0;
	
	// Re-init arrays to store vertices
	sphereX = new float[vertCount];
	sphereY = new float[vertCount];
	sphereZ = new float[vertCount];
	float angle_step = (SINCOS_LENGTH * 0.5f) / sDetail;
	float angle = angle_step;
	
	// Step along Y axis
	for (int i = 1; i < sDetail; i++) {
		float curradius = sinLUT[(int) angle % SINCOS_LENGTH];
		float currY = -cosLUT[(int) angle % SINCOS_LENGTH];
		for (int j = 0; j < sDetail; j++) {
			sphereX[currVert] = cx[j] * curradius;
			sphereY[currVert] = currY;
			sphereZ[currVert++] = cz[j] * curradius;
		}
		angle += angle_step;
	}
	
//	texmap = loadImage(mapfile);
//	tw = texmap.width ;
//	th = texmap.height ;
	
	//println("Actual image width: " + tw);
	//println("Actual image height: " + th);
        calcTexCoords();
}

// Generic routine to draw textured sphere
public void texturedSphere(float r)
{
	
	noStroke();
	fill(255);
        specular(0);
//	specular(255, 255, 255);
//	shininess(6.0f);
//	
	int v1, v11, v2;
	
	// Add the Northern cap

        int texElem = 0;
    
        float vtx[] = {0.0f,0.0f,0.0f};
        GL gl = pgl.gl;
        gl.glClearColor ( 0.0f, 0.0f, 0.0f, 1.0f );
        gl.glColor4ub((byte)255, (byte)255, (byte)255, (byte)255);
        gl.glEnable( GL.GL_DEPTH_TEST );
        // TEXTURE-UNIT #0
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texs.getTextures()[0]);
        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE);
        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_COMBINE);
        // TEXTURE-UNIT #1:
//        gl.glActiveTexture(GL.GL_TEXTURE1);
//        gl.glEnable(GL.GL_TEXTURE_2D);
//        gl.glBindTexture(GL.GL_TEXTURE_2D, texs.getTextures()[1]);
//        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE);
//        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_COMBINE);
        gl.glPushMatrix();
        gl.glTranslatef(0f,0f,-hcam.pos.z);
        gl.glRotatef(-rotationX, 0.1f,0.0f,0.0f);
        gl.glRotatef(rotationY, 0.0f,0.1f,0.0f);
        
	gl.glBegin(GL.GL_TRIANGLE_STRIP);
		for (int i = 0; i < sDetail; i++)
		{
		
			vtx[0] = 0;
			vtx[1] = -r;
			vtx[2] = 0;
		        
                        gl.glNormal3f(vtx[0], vtx[1], vtx[2]);
                        gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem+1]);
//                        gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem], texCoords[texElem+1]);
                        texElem+=2;
                        gl.glVertex3f(vtx[0], vtx[1], vtx[2]);
			
			
			vtx[0] = sphereX[i] * r;
			vtx[1] = sphereY[i] * r;
			vtx[2] = sphereZ[i] * r;
			
                        gl.glNormal3f(vtx[0], vtx[1], vtx[2]);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem+1]);
//                        gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem], texCoords[texElem+1]);
                        texElem+=2;
                        gl.glVertex3f(vtx[0], vtx[1], vtx[2]);

		}
		
		vtx[0] = 0;
		vtx[1] = -r;
		vtx[2] = 0;
		
                gl.glNormal3f(vtx[0], vtx[1], vtx[2]);
		gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem+1]);
//                gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem], texCoords[texElem+1]);
                texElem+=2;
                gl.glVertex3f(vtx[0], vtx[1], vtx[2]);
		
		vtx[0] = sphereX[0] * r;
		vtx[1] = sphereY[0] * r;
		vtx[2] = sphereZ[0] * r;
		
                gl.glNormal3f(vtx[0], vtx[1], vtx[2]);
		gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem+1]);
//                gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem], texCoords[texElem+1]);
                texElem+=2;
                gl.glVertex3f(vtx[0], vtx[1], vtx[2]);
        gl.glEnd();
     
	
	
	// Middle rings
	int voff = 0;
        
	for(int i = 2; i < sDetail; i++)
	{
		v1 = v11 = voff;
		voff += sDetail;
		v2 = voff;
                   
		   gl.glBegin(GL.GL_TRIANGLE_STRIP);
			for(int j = 0; j < sDetail; j++)
			{
				vtx[0] = sphereX[v1] * r;
				vtx[1] = sphereY[v1] * r;
				vtx[2] = sphereZ[v1] * r;
				
                                gl.glNormal3f(vtx[0], vtx[1], vtx[2]);
				gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem+1]);
//                                gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem], texCoords[texElem+1]);
                                texElem+=2;
                                gl.glVertex3f(vtx[0], vtx[1], vtx[2]);
				
				v1++;
				
				vtx[0] = sphereX[v2] * r;
				vtx[1] = sphereY[v2] * r;
				vtx[2] = sphereZ[v2] * r;
				
                                gl.glNormal3f(vtx[0], vtx[1], vtx[2]);
				gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem+1]);
//                                gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem], texCoords[texElem+1]);
                                texElem+=2;
                                gl.glVertex3f(vtx[0], vtx[1], vtx[2]);
				
				v2++;
			}
			
			// Close each ring
			v1 = v11;
			v2 = voff;
			
			vtx[0] = sphereX[v1] * r;
			vtx[1] = sphereY[v1] * r;
			vtx[2] = sphereZ[v1] * r;

                        gl.glNormal3f(vtx[0], vtx[1], vtx[2]);			
			gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem+1]);
//                        gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem], texCoords[texElem+1]);
                        texElem+=2;
                        gl.glVertex3f(vtx[0], vtx[1], vtx[2]);
			
			vtx[0] = sphereX[v2] * r;
			vtx[1] = sphereY[v2] * r;
			vtx[2] = sphereZ[v2] * r;
			
                        gl.glNormal3f(vtx[0], vtx[1], vtx[2]);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem+1]);
//                        gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem], texCoords[texElem+1]);
                        texElem+=2;
                        gl.glVertex3f(vtx[0], vtx[1], vtx[2]);
		gl.glEnd();
                
	}
	
	// Add the Southern cap
       
	gl.glBegin(GL.GL_TRIANGLE_STRIP);
		for(int i = 0; i < sDetail; i++)
		{
			v2 = voff + i;
			
			vtx[0] = sphereX[v2] * r;
			vtx[1] = sphereY[v2] * r;
			vtx[2] = sphereZ[v2] * r;

			gl.glNormal3f(vtx[0], vtx[1], vtx[2]);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem+1]);
//                        gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem], texCoords[texElem+1]);
                        texElem+=2;
                        gl.glVertex3f(vtx[0], vtx[1], vtx[2]);
                        
			gl.glNormal3f(0f,r, 0f);
                        gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem+1]);
//                        gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem], texCoords[texElem+1]);
                        texElem+=2;
                        gl.glVertex3f(0f,r, 0f);
			

		}
		
		vtx[0] = sphereX[voff] * r;
		vtx[1] = sphereY[voff] * r;
		vtx[2] = sphereZ[voff] * r;

		gl.glNormal3f(vtx[0], vtx[1], vtx[2]);
		gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem+1]);
//                gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem], texCoords[texElem+1]);
                texElem+=2;
                gl.glVertex3f(vtx[0], vtx[1], vtx[2]);	
                
                vtx[0] = 0;
		vtx[1] =  r;
		vtx[2] = 0;

//		gl.glNormal3f(vtx[0], vtx[1], vtx[2]);
//		gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem+1]);
////                gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem], texCoords[texElem+1]);
//                texElem+=2;
//                gl.glVertex3f(vtx[0], vtx[1], vtx[2]);	
	gl.glEnd();
	
        gl.glPopMatrix();
//        gl.glActiveTexture(GL.GL_TEXTURE1);
//        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glDisable(GL.GL_TEXTURE_2D);

//	specular(0);
//	shininess(0);
//	emissive(0);
       
	
}

public void calcTexCoords()
{
    	int v1, v11, v2;
	
	// Add the Northern cap
	
	float iu = (float)1 / (sDetail);
	float iv = (float)1 / (sDetail);
	
	float u = 0;
	float v = 0;
        
        int texElem = 0;
        for (int i = 0; i < sDetail; i++)
        {
            texCoords[texElem] = u;
            texCoords[texElem+1] = 0;
	    texCoords[texElem+2] = u;
	    texCoords[texElem+3] = v;
	    texElem+=4;
            u+=iu;
        }
	texCoords[texElem] = u;
        texCoords[texElem+1] = 0;
	texCoords[texElem+2] = u;
	texCoords[texElem+3] = v;
	texElem+=4;

	int voff = 0;
	v =  iv;
	for(int i = 2; i < sDetail; i++)
	{
		v1 = v11 = voff;
		voff += sDetail;
		v2 = voff;
		u = imgPadding;
		for(int j = 0; j < sDetail; j++)
		{
				if (j == sDetail - 1) {
					u -= imgPadding;
				}
			texCoords[texElem] = u;
			texCoords[texElem+1] = v;
			v1++;
			texCoords[texElem+2] = u;
			texCoords[texElem+3] = v+iv;
			texElem+=4;
			v2++;
			u+=iu;
		}
		v1 = v11;
		v2 = voff;
		texCoords[texElem] = u;
		texCoords[texElem+1] = v;
		texCoords[texElem+2] = u;
		texCoords[texElem+3] = v+iv;
		texElem+=4;
		v+=iv;
	}
	u = 0;
	for(int i = 0; i < sDetail; i++)
	{
		v2 = voff + i;
		texCoords[texElem] = u;
		texCoords[texElem+1] = v;
		texCoords[texElem+2] = u;
		texCoords[texElem+3] = v+iv;
		texElem+=4;
		u+=iu;
	}
	
	texCoords[texElem] = u;
	texCoords[texElem+1] = v;
	texCoords[texElem+2] = u;
	texCoords[texElem+3] = v+iv;
	
}


class BlindThread implements Runnable {
	public boolean cancel = false;
	public String command;
	
	public BlindThread(String inCommand) {
		command = inCommand;
	}
	
	public void run() {
		try {
			final Process trace = Runtime.getRuntime().exec(command);
			final BufferedReader br = new BufferedReader(new InputStreamReader(trace.getInputStream()));
			String line;
			while((line = br.readLine()) != null && cancel == false) {
				println(line);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
public class Vector3D {

	public Vector3D(float X, float Y, float Z) {
		x = X;
		y = Y;
		z = Z;
	}

	public Vector3D() {
		x = 0.0F;
		y = 0.0F;
		z = 0.0F;
	}

	public Vector3D(Vector3D p) {
		x = p.x;
		y = p.y;
		z = p.z;
	}

	public final float z() {
		return z;
	}

	public final float y() {
		return y;
	}

	public final float x() {
		return x;
	}

	public final void setX(float X) {
		x = X;
	}
 
		public final void setY(float Y) {
		y = Y;
	}

	public final void setZ(float Z) {
		z = Z;
	}

	public final void set(float X, float Y, float Z) {
		x = X;
		y = Y;
		z = Z;
	}

	public final void set(Vector3D p) {
		x = p.x;
		y = p.y;
		z = p.z;
	}

	public final Vector3D add(Vector3D p) {
		x += p.x;
		y += p.y;
		z += p.z;
		return this;
	}

	public final void subtract(Vector3D p) {
		x -= p.x;
		y -= p.y;
		z -= p.z;
	}

	public final void add(float a, float b, float c) {
		x += a;
		y += b;
		z += c;
	}
        
        public final void divide(float a, float b, float c)
        {
            x /= a;
            y /= b;
            z /= c;
        }
        
        public final void divide(float a)
        {
            x /= a;
            y /= a;
            z /= a;
        }

	public final Vector3D plus(Vector3D p) {
		return new Vector3D(x + p.x, y + p.y, z + p.z);
	}

	public final Vector3D times(float f) {
		return new Vector3D(x * f, y * f, z * f);
	}

	public final Vector3D over(float f) {
		return new Vector3D(x / f, y / f, z / f);
	}

	public final Vector3D minus(Vector3D p) {
		return new Vector3D(x - p.x, y - p.y, z - p.z);
	}

	public final Vector3D multiplyBy(float f) {
		x *= f;
		y *= f;
		z *= f;
		return this;
	}
	
	public final Vector3D negate() {
		return new Vector3D(-x, -y, -z);
	}

	public final float distanceTo(Vector3D p) {
		float dx = x - p.x;
		float dy = y - p.y;
		float dz = z - p.z;
		return (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public final float distanceTo(float x, float y, float z) {
		float dx = this.x - x;
		float dy = this.y - y;
		float dz = this.z - z;
		return 1.0F / fastInverseSqrt(dx * dx + dy * dy + dz * dz);
	}

	public final float dot(Vector3D p) {
		return x * p.x + y * p.y + z * p.z;
	}

	public final float length() {
		return (float)Math.sqrt(x * x + y * y + z * z);
	}

	public final Vector3D unit() {
		float l = length();
		return l != 0.0F ? over(l) : new Vector3D();
	}

	public final void clear() {
		x = 0.0F;
		y = 0.0F;
		z = 0.0F;
	}
        
        public final Vector3D getNormalized()
        {
          return this.over(this.length());
        }
        
        public final void normalize()
        {
            this.divide(this.length());
        }

	public final String toString() {
		return new String("(" + x + ", " + y + ", " + z + ")");
	}
	
	public final Vector3D cross(Vector3D p) {
		return new Vector3D(y * p.z - z * p.y, x * p.z - z * p.x, x * p.y - y * p.x);
	}
	
	public final float angleWith(Vector3D b) {
		float aDotb = this.dot(b);
		float ab = this.length() * b.length();
                if (ab == 0)
                    return 0.0f;
		return acos(aDotb / ab);
	}
        
        public final float projectionOn(Vector3D b)
        {
                float aDotb = this.dot(b);
		float ab = this.length() * b.length();
                if (ab == 0)
                  return 0;
                return this.length()*aDotb/ab;
        }
        

	float x;
	float y;
	float z;
}

public static float fastInverseSqrt(float x) {
	float half = 0.5F * x;
	int i = Float.floatToIntBits(x);
	i = 0x5f375a86 - (i >> 1);
	x = Float.intBitsToFloat(i);
	return x * (1.5F - half * x * x);
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "dynviz" });
  }
}
