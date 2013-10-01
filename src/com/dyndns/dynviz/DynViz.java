package com.dyndns.dynviz;

import hypermedia.net.UDP;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

import com.dyndns.dynviz.kinect.KinectWrapper;
import com.dyndns.dynviz.kinect.KinectWrapper.Kinect;
import com.dyndns.dynviz.pcap.PCAPDownloader;
import com.dyndns.dynviz.pcap.PCAPDownloader.OnFileLoadedListener;
import com.dyndns.dynviz.pcap.PCAPSender;
import com.dyndns.dynviz.prop.AnycastHelper;
import com.dyndns.dynviz.prop.Props;
import com.dyndns.dynviz.prop.Servers;
import com.dyndns.dynviz.prop.Servers.ServerInfo;
import com.dyndns.dynviz.ui.GLSLShaderUtil;
import com.dyndns.dynviz.ui.Globe;
import com.dyndns.dynviz.ui.HandyCam;
import com.dyndns.dynviz.ui.Textures;
import com.dyndns.dynviz.ui.Vector3D;

import damkjer.ocd.Camera;

public class DynViz extends PApplet {

    private static final long serialVersionUID = 1L;

    static int SHADER_MODE_COUNTRIES = 0;
    static int SHADER_MODE_SELECTION = 1;
    static int SHADER_MODE_SELECT_ONE = 2;

    float kinectX = 0;
    float kinectY = 0;
    float pkinectX = 0;
    float pkinectY = 0;

    boolean kinectClicked = false;
    boolean kinectReset = false;
    boolean kinectSet = false;
    boolean kinectGrasp = false;
    int kinectSwipe = KinectWrapper.SWIPE_NONE;

    // Constants

    // CHANGE HERE TO MANIPULATE THE QPS AND QPS HISTORY DISPLAYED
    // int QPS_MULTIPLIER = 40;

    Float fNaN = new Float(0.0f / 0.0f);

    // timer
    long startShowing = 0;
    boolean bShowTimingPanel = false;

    // KEYS
    boolean bCTRL = false;
    boolean bALT = false;
    boolean bSHIFT = false;
    boolean bTAB = false;

    long qqp = 0;
    long quePS = 1;

    // Global statistic
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
    float MaxDist = 1.0f / 6.0f * PI;
    float Lmax = sqrt(MaxDist * MaxDist + MaxPower * MaxPower);
    float QTime1ms = 70.0f / 0.64863515f;

    int appWidth, appHeight;
    PImage imgBackground;

    // GradienWheel panel
    int gpX = width - 300, gpY = 300;
    ColorBlock cbQStart, cbQEnd, cbSStart, cbSEnd, cbSZero;
    ColorBlock cbSelected, cbHighlighted;
    TimeSlider minSlider, maxSlider;
    PImage imgColorWheel;

    // borders
    private boolean isShowBorders;
    private int bordersColor;
    ColorBlock cbCountryBorder;

    // fps
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
    float sizeShadow = 4.0f;
    float sizeLines = 4.0f;

    float sizeTail = 128.0f;
    float tailAlphaBegin = 0.0f;
    float tailAlphaEnd = 255.0f;
    boolean bTails = true;

    boolean serverSwitch = false;

    float burstSpeed = 1.0f;

    int CAST_UNI = 0;
    int CAST_ANY = 1;
    int CAST_GRAD = 2;
    int CAST_COUNTRY = 3;
    int CAST_CIRCLE = -3;
    int CAST_BOTH = 4;

    final int ZONE_EU = 0;
    final int ZONE_NA = 1;
    final int ZONE_AS = 2;
    final int ZONE_UN = -1;

    int CAST = CAST_BOTH; // 0 = Unicast, 1 = Anycast, 2 = Both

    Timer modeSwitchTimer;

    boolean bListen = true;
    boolean bPerlUnicast = true;
    boolean bStartPerlAutomatically = false;
    boolean bSiteToggle = false;

    float sightDist;

    Map<String, Server> showSites = new HashMap<String, Server>();

    // @formatter:off
    private final String HELP_TEXT = "LEFT ........ -X Velocity\n" + "RIGHT ....... +X Velocity\n" + "DOWN ........ -Y Velocity\n"
            + "UP .......... +Y Velocity\n" + "\n" + "` ........ Mode switching\n" + "ALT+` .. Auto mode switch\n" + "\n"
            + "a ............... Zoom In\n" + "z .............. Zoom Out\n" + "SPACE ............ Breaks\n" + "MOUSE1 .... +-XY Velocity\n"
            + "MOUSE2 ............. Roll\n" + "MOUSE3 ...... Yaw / Pitch\n" + "\n" + "G ......... Display Globe\n"
            + "Q .. Display Query Bursts\n" + "L ... Display Query Lines\n" + "S . Display Query Shadows\n" + "R .......... Reset Camera\n"
            + "F . on/off queries' alpha\n" + "H ...... Help Panel (me!)\n" + "O . switch selected serv.\n" + "\n"
            + "1 .......... Legend Panel\n" + "2 ........... Clock Panel\n" + "3 ...... Save added Srvrs\n" + "4 .... Global QPS history\n"
            + "5 ........... Color Wheel\n" + "6 ......... Servers\' Time\n" + "7 .. Servers\' Stat. Boxes\n"
            + "8 ...... Global Statistic\n" + "9 . In/Dest. Count./Serv.\n" + "0 .... Swtch Real/Optimal\n" + "BCKSPC ... Switch servers\n"
            + "ALT+BCKSPC . Switch flags\n" + "CTRL+~ . Countries legend\n" + "\n" + "SHIFT+MOUSE1 . Cntry sel.\n"
            + "CTRL+MOUSE1 ... Srvr sel.\n" + "ALT+MOUSE1 ..... Srvr add\n" + "\n" + "DELETE .. Compense on/off\n"
            + "CTRL+1 ... Cntries\' popup\n" + "CTLR+2 .. Reset Statistic\n" + "CTRL+3 ...... Display FPS\n"
            + "CTRL+4 ..... Time Configs\n" + "\n" + ". .. Dec. gradient radius\n" + "/ .. Inc. gradient radius\n";
    // @formatter:on

    float helpWidth = 0.0f, helpHeight = 0.0f;

    Integer qps = 0;
    Integer currentQPS = 0;

    float qpsWidth = 200.0f;
    float qpsHeight = 230.0f;
    float qpsMax = 0.0f, qpsMin = 0.0f;

    float serversWidth = 280.0f;

    int qpsAgeWindow = 3600;
    int qpsMaxAge = qpsAgeWindow, qpsMinAge = qpsAgeWindow;

    PFont fontTime, fontDate, fontHelp, fontLabel, fontLabel2;

    int panelAlpha = 200;
    int statConfPH = 0;

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

    private float rotationX = 323.28f;
    private float rotationY = 5.47f;

    float targetRotX = rotationX;
    float targetRotY = rotationY;
    float targetZ = -2000;

    float velocityX = 0.0f;
    float velocityY = 0.05f;
    float velocityZ = 0.0f;

    float camX = 0;
    float camY = 0;
    float camZ = -1750;

    // float attenuation_far[] = {1.0f, -0.01f, -0.000001f}; // Points scale with distance
    float attenuation_far[] = { 1.0f, -0.01f, -0.000001f }; // Points scale with distance

    float attenuation_near[] = { 1.0f, 0.0f, 0.0f }; // Points keep size - best when zoomed in
    float attenuation[] = attenuation_far;

    // OpenGL stuff
    Globe globe = new Globe(this);
    private static final float GLOBE_RADIUS = 800;

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
    List<Server> servers;
    int mainServersCount;
    int cint = 0;
    private AnycastHelper anycastHelper;

    // Network code
    UDP udp;

    // Rendering Queue
    List<Query> queries;
    LinkedList<Integer> qpsHistory;

    // SHADER
    int shaderProgramID = 0;
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
    public int countries;
    float minAvT = 25f, maxAvT = 125f;

    Vector3D selectionColor = new Vector3D(1.0f, 0.0f, 0.0f);
    Vector3D highlightColor = new Vector3D(0.0f, 1.0f, 0.0f);
    int selectedCountry = -1;
    int highlightedCountry = -1;
    int selectedCountries[];
    Server selectedServer = null;
    int countriesTime = 0;

    // User options
    boolean bGlobe = true;
    boolean bQueries = true;
    boolean bLines = true;
    boolean bShadows = false;
    boolean bAlpha = true;
    boolean bQPSSolid = false;
    boolean bRotationReparation = true;
    boolean bCountrySelection = false;

    boolean bOptimalHightlight = false;
    boolean bOptimalCountry = false;

    boolean bPanel1 = true; // servers list
    boolean bPanelBottom = true; // time-date + mode
    boolean bPanel3 = false;
    boolean bPanel4 = true; // qps history
    boolean bPanel5CW = false; // color wheel
    boolean bPanel6 = true; // kinect outline
    boolean bPanelSQPS = false; // servers' curr qps
    boolean bPanelSHis = false; // servers' statistic
    boolean bPanelGlobalStats = false; // global stat
    boolean bPanelC2S = false; // countries to server list
    boolean bPanelCCL = false; // countries color legend
    boolean bCountriesPopup = false;
    boolean bStatConfigs = false;

    private boolean isDrawServersFlags;

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

    PCAPSender pcapSender;
    boolean canUseSender = true;

    KinectWrapper kinectWrapper;
    private boolean isUseKinect;

    private boolean isInititalized;

    @Override
    public void init() {
        super.init();
        try {
            frame.removeNotify();
            frame.setUndecorated(true);
            frame.addNotify();
            frame.setResizable(true);
        } catch (Exception e) {
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setup() {
        int screenW = Props.integer(Props.SCREEN_WIDTH), screenH = Props.integer(Props.SCREEN_HEIGHT);
        appWidth = screenW == 0 ? screen.width : screenW;
        appHeight = screenH == 0 ? screen.height : screenH;
        size(appWidth, appHeight, OPENGL);

        hint(ENABLE_OPENGL_4X_SMOOTH);
        // smooth();

        frame.setLocation(0, 0);

        frameRate(75);
        background(255, 255, 32);
        noCursor();
    }

    private void initializeProgramIfNeeded() {
        if (isInititalized) return;

        isUseKinect = Props.bool(Props.USE_KINECT);
        isShowBorders = Props.bool(Props.SHOW_BORDERS);
        bordersColor = Props.integer(Props.BORDERS_COLOR);
        bPanelBottom = Props.bool(Props.SHOW_BOTTOM_PANEL);
        bPanelGlobalStats = Props.bool(Props.SHOW_GLOBAL_STATS_PANEL);
        isDrawServersFlags = Props.bool(Props.SHOW_SERVERS_FLAGS);

        if (isUseKinect) kinectWrapper = new KinectWrapper(this);

        texs = new Textures(this);
        texs.LoadTextures();
        initResources();

        initCountries();
        initBasicValues();

        initUiWidgets();
        initGlStuff();

        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                mouseWheel(evt.getWheelRotation());
            }
        });

        initDataSource();

        if (Props.bool(Props.AUTO_CAST_SWITCH)) startAutoCastModeSwitcher();

        isInititalized = true;
    }

    private void initCountries() {
        countries = 255;
        countriesNames = new String[countries];

        countryAv = new float[countries];
        countryQPS = new float[countries];
        optimalAv = new float[countries];
        countryAvT = new float[countries];
        countryQPST = new float[countries];
        optimalAvT = new float[countries];
        // selectedCountries = new int[countries];
        for (int i = 0; i < countries; i++) {
            countryAv[i] = -1.0f;
            countryQPS[i] = 0;
            optimalAv[i] = -1.0f;
            countryAvT[i] = 0;
            countryQPST[i] = 0;
            optimalAvT[i] = 0;
            countriesNames[i] = "noname";
        }

        String tmpS[] = loadStrings("countries.txt");
        for (int i = 0; i < tmpS.length; i++) {
            String numberStr = tmpS[i].substring(0, tmpS[i].indexOf(":"));
            String name = tmpS[i].substring(tmpS[i].indexOf(":") + 1, tmpS[i].length());
            countriesNames[Integer.parseInt(numberStr)] = name;
        }
    }

    private void initBasicValues() {
        qpsHistory = new LinkedList<Integer>();
        queries = new LinkedList<Query>();

        List<ServerInfo> serverInfos = Servers.getServersList();
        servers = new ArrayList<Server>();
        for (ServerInfo info : serverInfos) {
            servers.add(new Server(info.lat, info.lon, info.name, info.id, info.color));
        }

        mainServersCount = servers.size();

        for (Iterator<Server> itServer = servers.iterator(); itServer.hasNext();) {
            Server s = itServer.next();
            showSites.put(s.id, s);
        }

        anycastHelper = new AnycastHelper();
    }

    private void initResources() {
        if (Props.bool(Props.SHOW_STARS_IN_BACKGROUND)) {
            imgBackground = loadImage("stars_background.jpg");
            imgBackground.resize(appWidth, appHeight);
        }

        imgLogoDynDNS = loadImage("logo-dyndns.png");
        imgLogoDynect = loadImage("logo-dynect.png");
        imgColorWheel = loadImage("ColorWheel.bmp");

        fontTime = loadFont("GillSansMT-64.vlw");
        fontDate = loadFont("GillSansMT-24.vlw");
        fontLabel2 = loadFont("GillSansMT-Bold-14.vlw");
        fontLabel = loadFont("GillSansMT-12.vlw");
        fontHelp = loadFont("CourierNewPSMT-11.vlw");
    }

    private void initGlStuff() {
        pgl = (PGraphicsOpenGL) g;
        glu = new GLU();

        // Set Up camera
        hcam = new HandyCam(0.0f, 0.0f, -camZ);
        hcam.cam = new Camera(this, 0.0f, 0.0f, -camZ, 0.0f, 0.0f, 0.0f, 0.5f, 100000.0f);
        hcam.update();

        pgl.gl.glClearDepth(1.0f); // Depth Buffer Setup
        pgl.gl.glEnable(GL.GL_DEPTH_TEST); // Enables Depth Testing
        pgl.gl.glDepthFunc(GL.GL_LEQUAL);

        initShaders();
        // Set up Sphere / Graphics
        globe.initializeSphere();

        texs.BindTextures(pgl);

        try {
            int tClr[] = new int[countries];
            for (int i = 0; i < countries; i++) {
                tClr[i] = GetCountryStatColor(i);
            }

            texs.InitCountries(pgl.gl);
            texs.CommitCountriesColors(pgl.gl, tClr);
        } catch (Exception e) {
            System.out.println("DynViz.initGlStuff() / Exception: " + e.toString());
        }
    }

    private void initShaders() {
        // GLSLShaderUtil.CheckShaderExtensions(pgl.gl);
        if (!pgl.gl.isExtensionAvailable("GL_ARB_vertex_program")) {
            println("GL_ARB_vertex_program extension is not available!!Shutting Down");
            System.exit(-1);
        } else
            println("GL_ARB_vertex_program extension is available.");

        int vertexShaderID = GLSLShaderUtil.InitVertexShaderID(pgl.gl);
        try {
            GLSLShaderUtil.CompileVertexShaderFromString(pgl.gl, vertexShaderID, SArr2S(loadStrings("shader_vertex.txt")));
        } catch (Exception e) {
            println("An exception in initShaders: " + e.toString());
        }

        int fragmentShaderID = GLSLShaderUtil.InitFragmentShaderID(pgl.gl);
        if (!GLSLShaderUtil.CompileFragmentShaderFromString(pgl.gl, fragmentShaderID, SArr2S(loadStrings("shader_fragment.txt")))) {
            System.exit(-1);
        }

        shaderProgramID = GLSLShaderUtil.InitShaderProgramID(pgl.gl);
        GLSLShaderUtil.LinkShaderProgram(pgl.gl, vertexShaderID, fragmentShaderID, shaderProgramID);
    }

    private void initUiWidgets() {
        gpX = width - 300;
        gpY = 300;
        // ColorBlocks for Gradient mode
        // Gradient start
        cbQStart = new ColorBlock(gpX + 160, gpY - 60, 40, 20, 0xFF00FF00);
        // Gradient end
        cbQEnd = new ColorBlock(gpX + 255, gpY - 60, 40, 20, 0xFFFF0000);

        // ColorBlocks for Country mode
        // Country minimal average time
        cbSStart = new ColorBlock(gpX + 160, gpY - 60, 40, 20, 0xFF00FF00);
        // Country maximal average time
        cbSEnd = new ColorBlock(gpX + 255, gpY - 60, 40, 20, 0xFFFF0000);
        // Country no-average time
        cbSZero = new ColorBlock(gpX + 60, gpY - 30, 40, 20, 0xFFFFFFFF);

        // ColorBlocks for Globe's country selection, highlight and borders
        cbSelected = new ColorBlock(gpX + 160, gpY - 30, 40, 20, 0xFFFF0000);
        cbHighlighted = new ColorBlock(gpX + 255, gpY - 30, 40, 20, 0xFF00FF00);
        cbCountryBorder = new ColorBlock(gpX + 60, gpY - 60, 40, 20, bordersColor);

        // Sliders
        minSlider = new TimeSlider("Min", gpX + 60, gpY - 90, 150, 10, 0, 25, 125);
        maxSlider = new TimeSlider("Max", gpX + 60, gpY - 110, 150, 10, 25, 125, 200);
        statTimeSlider = new TimeSlider("Zones", 10, 10, 200, 15, 1, 2, 60);
        shadTimeSlider = new TimeSlider("Countries", 10, 10, 200, 15, 1, 2, 60);
        servTimeSlider = new TimeSlider("Servers", 10, 10, 200, 15, 1, 2, 60);
    }

    private boolean startPcapSender(String pcapFilePath) {
        if (pcapSender != null) pcapSender.dispose();
        if (pcapFilePath == null) return false;

        pcapSender = new PCAPSender(DynViz.this, pcapFilePath);
        if (pcapSender.openPCAP()) {
            System.out.println("PCAP opened!");
            return true;
        } else {
            System.out.println("PCAP opening failed!");
            pcapSender.dispose();
            pcapSender = null;
            return false;
        }
    }

    private void initDataSource() {
        // Set up UDP socket
        if (canUseSender) {
            String pcapUrl = Props.string(Props.PCAP_URL);
            if (Props.bool(Props.USE_PCAP_URL) && pcapUrl != null && pcapUrl.length() > 0) {
                new PCAPDownloader(pcapUrl, new OnFileLoadedListener() {
                    private String lastPcapFile;

                    @Override
                    public void onFileLoaded(String nextPcapFile) {
                        if (nextPcapFile == null || !nextPcapFile.equals(lastPcapFile)) {
                            boolean started = startPcapSender(nextPcapFile);
                            lastPcapFile = started ? nextPcapFile : null;
                        }
                    }
                }).startLoading(Props.integer(Props.PCAP_URL_INTERVAL) * 60 * 1000);
            } else {
                startPcapSender(Props.string(Props.PCAP_FILE));
            }
        } else if (bListen) {
            String listen[] = loadStrings("receive.txt");
            if (listen.length > 0 && listen[0].length() > 6) UDP_CLIENT = listen[0];

            udp = new UDP(this, UDP_SERVER_PORT, UDP_CLIENT);
            udp.listen(true);
        }
    }

    void mouseWheel(int delta) {
        velocityZ -= delta * 10;
    }

    @Override
    public void draw() {
        initializeProgramIfNeeded();

        loadQueuedMessages();

        drawing = true;

        if (isUseKinect) kinectWrapper.kinect.drawScene();

        handleMouse();

        adjustRotation();

        lightSpecular(255, 255, 255);

        ambientLight(28, 28, 28, 0, 0, 1200);
        spotLight(0, 0, 0, 400, 0, 1400, 0, 0, -1, PI / 3, 2);
        directionalLight(128, 128, 128, -0.5f, 0, -1);

        pgl.gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        if (Props.bool(Props.SHOW_STARS_IN_BACKGROUND)) {
            background(imgBackground);
        } else {
            background(24);
        }

        CalculateVisibleRange();

        pgl.gl.glEnable(GL.GL_MULTISAMPLE);
        pgl.gl.glEnable(GL.GL_TEXTURE_1D);
        pgl.gl.glEnable(GL.GL_TEXTURE_2D);
        float lDist = hcam.pos.z;// 2*globeRadius;
        camPos.x = lDist * cos(radians(-rotationX)) * sin(radians(-rotationY));
        camPos.z = lDist * cos(radians(-rotationX)) * cos(radians(-rotationY));
        camPos.y = -lDist * sin(radians(-rotationX));
        pgl.gl.glUseProgram(shaderProgramID);

        // sending globe texture to the shader
        setShaderTexture(Textures.GLOBE, "globeTex");
        // sending country map to the shader
        setShaderTexture(Textures.MASK, "countryMap");
        // countries colors
        setShaderTexture(Textures.CCOLORS, "countryColor");
        // borders
        setShaderTexture(Textures.CBORDERS, "bordersTex");

        int loc;

        loc = pgl.gl.glGetUniformLocation(shaderProgramID, "BordersColor");
        Vector3D bColor = cbCountryBorder.GetNormColor();
        pgl.gl.glUniform4f(loc, bColor.x, bColor.y, bColor.z, 1.0f);

        loc = pgl.gl.glGetUniformLocation(shaderProgramID, "UseBlur");
        pgl.gl.glUniform1f(loc, Props.bool(Props.USE_BLUR) ? 1.0f : 0.0f);

        loc = pgl.gl.glGetUniformLocation(shaderProgramID, "ShowBorders");
        pgl.gl.glUniform1f(loc, isShowBorders ? 1.0f : 0.0f);

        // countries count
        loc = pgl.gl.glGetUniformLocation(shaderProgramID, "CountriesCount");
        pgl.gl.glUniform1f(loc, (float) (countries));

        // light position
        loc = pgl.gl.glGetUniformLocation(shaderProgramID, "LightPosition");
        pgl.gl.glUniform3f(loc, camPos.x, camPos.y, camPos.z);

        HighlightCountry();

        // Shader mode will be changed
        if (CAST == CAST_COUNTRY) {

            if (bShaderDataUpdate) {
                int tCl[] = new int[countries];
                // tCl[0] = 0xFF000000;
                for (int i = 0; i < countries; i++) {
                    tCl[i] = GetCountryStatColor(i);
                }
                pgl.gl.glActiveTexture(GL.GL_TEXTURE0 + Textures.CCOLORS);
                texs.CommitCountriesColors(pgl.gl, tCl);
                loc = pgl.gl.glGetUniformLocation(shaderProgramID, "CountryMode");
                pgl.gl.glUniform1f(loc, 1.0f);

                bShaderDataUpdate = false;
            }
        } else {
            loc = pgl.gl.glGetUniformLocation(shaderProgramID, "CountryMode");
            pgl.gl.glUniform1f(loc, 0.0f);
        }

        loc = pgl.gl.glGetUniformLocation(shaderProgramID, "SelectedCountry");
        pgl.gl.glUniform1f(loc, (float) selectedCountry);

        loc = pgl.gl.glGetUniformLocation(shaderProgramID, "SelectionColor");
        pgl.gl.glUniform4f(loc, selectionColor.x, selectionColor.y, selectionColor.z, 1.0f); // here must be selection color

        loc = pgl.gl.glGetUniformLocation(shaderProgramID, "HighlightOne");
        if (!bCountriesPopup) pgl.gl.glUniform1f(loc, 0.0f);
        if (bCountriesPopup) {
            pgl.gl.glUniform1f(loc, (float) highlightedCountry);
            loc = pgl.gl.glGetUniformLocation(shaderProgramID, "HighlightColor");
            pgl.gl.glUniform4f(loc, highlightColor.x, highlightColor.y, highlightColor.z, 1.0f);
        }

        pgl.gl.glActiveTexture(GL.GL_TEXTURE0);

        if (bRotationReparation) {
            setCompenseGlobe();
            if (bGlobe) renderGlobe();
            resetCompenseGlobe();
            setGlobe();
        } else {
            setGlobe();
            if (bGlobe) renderGlobe();
        }

        pgl.gl.glUseProgram(0);
        pgl.gl.glActiveTexture(GL.GL_TEXTURE0);

        pgl.gl.glDisable(GL.GL_MULTISAMPLE);
        pgl.gl.glDisable(GL.GL_TEXTURE_1D);
        pgl.gl.glDisable(GL.GL_TEXTURE_2D);
        gl = pgl.beginGL();

        if (serverSwitch) {
            for (Query q : queries) {
                q.CalcPotencial();
            }
            serverSwitch = false;
        }

        // long start = System.currentTimeMillis();
        drawQueries();
        // System.out.println("drawing q: " + (System.currentTimeMillis() - start) + " / " + queries.size());

        if (isDrawServersFlags) drawServers();

        resetGlobe();

        lights();
        noLights();

        camera();

        gl.glDisable(GL.GL_DEPTH_TEST);

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        pgl.endGL();

        drawHud();

        drawFPS();
        drawCursor();

        gl.glDisable(GL.GL_BLEND);

        drawing = false;
    }

    private void setShaderTexture(int textureId, String shaderVar) {
        pgl.gl.glActiveTexture(GL.GL_TEXTURE0 + textureId);
        pgl.gl.glBindTexture(GL.GL_TEXTURE_2D, texs.getTexture(textureId));
        int loc = pgl.gl.glGetUniformLocation(shaderProgramID, shaderVar);
        pgl.gl.glUniform1i(loc, textureId);
    }

    void drawCursor() {
        if (bALT || ((bPanel5CW && mouseX > gpX && mouseY < gpY) || (bStatConfigs && mouseX > (width - 370) && mouseY < statConfPH))) {
            cursor(CROSS);
            return;
        } else
            noCursor();
        if (abs(mouseX - pmouseX) > 0 || abs(mouseY - pmouseY) > 0) {
            ellipseMode(CENTER);
            fill(0, 255);
            ellipse(mouseX, mouseY, 40, 40);
            fill(255, 255);
            ellipse(mouseX, mouseY, 32, 32);
        }
    }

    public void stop() {
        /*
         * println("JAVA: Sending UDP Client STOP message."); try { while (t.isAlive()) { udp.send( "stop", UDP_CLIENT, UDP_CLIENT_PORT );
         * 
         * if (t.isAlive()) { try { Thread.currentThread().sleep(1000); } catch (InterruptedException ie) {} } }
         * 
         * killPerl(); println("JAVA: UDP Client successfully exited."); } catch (Exception e) {}
         */
        super.stop();
    }

    void drawHud() {

        if (bPanel1 || bPanelBottom || bPanel3 || bPanel4) {
            strokeWeight(1);
            stroke(0);

            if (bPanelBottom) {
                drawTime();
            }

            if (bPanel4) {
                drawQPS();
            }

            if (bPanel4) {
                drawKinectOutline();
            }

            if (bPanel1) {
                noStroke();
                fill(0, panelAlpha);
                rect(-1, 0, serversWidth, (servers.size() * 40) + 16);

                int i = 0;
                int c = 0;

                for (Iterator<Server> itServer = servers.iterator(); itServer.hasNext();) {
                    i++;
                    Server s = (Server) itServer.next();

                    int y = 16 + (c * 40);

                    if (selectedServer == s) {
                        noFill();
                        stroke(255, 255, 0, 255);
                        rect(1, y - 5, serversWidth - 2, 40);
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
            }
            if (bPanel3) {
                drawHelp();
            }
        }
        if (bPanelGlobalStats) {
            drawGlobalStatistic();
        }
        if (bPanelSQPS) {
            drawSQPSPanel();
        }
        if (bPanelSHis) {
            drawSHisPanel();
        }
        if (bPanelCCL) {
            drawCountryColorLegend();
        }
        if (bPanel5CW) {
            drawCWPanel();
        }
        if (bPanelC2S) {
            drawC2SPanel();
        }
        if (bStatConfigs) {
            drawStatConfPanel();
        }
        drawCountrysPopup();
        if (bShowTimingPanel) drawTimingPanel();
    }

    void drawTimingPanel() {
        stroke(255);
        fill(0, 255);
        int _left = width - 260;
        int _top = 4;
        int _w = 250;
        int _h = 80;
        int _sl = (int) ((MaxDist / PI) * (float) (_w - 105));
        rect(_left, _top, _w, _h);
        fill(255, 240);
        line(_left + 50, _top + 20, _left + _w - 50, _top + 20);
        line(_left + 50, _top + 20, _left + 50, _top + 40);
        line(_left + _w - 50, _top + 20, _left + _w - 50, _top + 40);

        line(_left + 50 + _sl, _top + 22, _left + _sl + 45, _top + 35);
        line(_left + 50 + _sl, _top + 22, _left + _sl + 55, _top + 35);
        line(_left + _sl + 45, _top + 35, _left + _sl + 55, _top + 35);
        textAlign(CENTER, TOP);
        text(nf(MaxDist * QTime1ms, 1, 1) + "ms", _left + (_w / 2), _top + 50);
        textAlign(RIGHT, TOP);
        text(nf(PI / 36.0f * QTime1ms, 1, 1) + "ms", _left + 55, _top + 50);
        textAlign(LEFT, TOP);
        text(nf(PI * QTime1ms, 1, 1) + "ms", _left + _w - 60, _top + 50);
        if ((System.currentTimeMillis() - startShowing) > 1800) bShowTimingPanel = false;
    }

    void drawStatConfPanel() {
        int winX = width - 370;
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
        fill(0, panelAlpha);
        rect(winX, winY, 370, _y);
        fill(255, 255, 255, 240);
        textAlign(CENTER, TOP);
        textFont(fontDate);

        text("Statistic time configs", winX + 185, 10);

        statTimeSlider.Draw();
        shadTimeSlider.Draw();
        servTimeSlider.Draw();
    }

    void drawCountrysPopup() {
        if (!bCountriesPopup) return;
        // println("bCountriesPopup == true");
        Vector3D retV = ProjectScreenToGlobe(pgl.gl, mouseX, mouseY, hcam.pos);
        if (retV == null) return;
        int country = highlightedCountry;
        // println("Country's index is "+country);
        if (country == -1) return;
        int popupW = 155 * 2;
        int popupH = 25 * 9;
        int left = mouseX + 30;
        int top = mouseY + 30;
        if (left + popupW > width) left = mouseX - popupW - 30;
        if (top + popupH > height) top = mouseY - popupH - 30;

        stroke(230);
        fill(0, panelAlpha);
        rect(left, top, popupW, popupH);
        stroke(250);
        line(left + 10, top + 35, left + popupW - 10, top + 35);
        fill(255, 255, 255, 240);
        textAlign(LEFT, TOP);
        textFont(fontLabel2, 14);
        String time;
        if (countryAv[country] == -1 || fNaN.equals(countryAv[country]))
            time = "n/a";
        else
            time = nf(countryAv[country], 1, 1);

        if (bOptimalHightlight) {
            if (optimalAv[country] == -1 || fNaN.equals(optimalAv[country]))
                time += "/n/a";
            else
                time += "/" + nf(optimalAv[country], 1, 1);
        }
        text(countriesNames[country] + ". Time: " + time, left + 20, top + 20);

        int x = left + 20, y = top + 50;
        int real = 0, optimal = 0;
        for (Iterator<Server> itServer = servers.iterator(); itServer.hasNext();) {
            Server s = itServer.next();

            if (CAST == CAST_ANY && bOptimalHightlight) {
                real += s.countriesInc[country];
                if (s.countriesInc[country] > 0 || s.countriesOIn[country] > 0) {
                    if (s.countriesInc[country] > 0 && s.countriesOIn[country] > 0) {
                        fill(0, 255, 0, 240);

                        optimal += s.countriesOIn[country];
                    } else if (s.countriesInc[country] > 0 && !(s.countriesOIn[country] > 0))
                        fill(255, 0, 0, 240);
                    else
                        fill(255, 255, 0, 240);
                    text(s.name, x, y);
                    y += 24;
                    if (y >= (top + popupH - 10)) {
                        y = top + 50;
                        x += 150;
                    }
                }
            } else if (s.countriesInc[country] > 0) {
                fill(255, 255, 255, 240);
                text(s.name, x, y);
                y += 24;
                if (y >= (top + popupH - 10)) {
                    y = top + 50;
                    x += 150;
                }
            }
        }
        if (CAST == CAST_ANY && bOptimalHightlight) {
            if (real == 0)
                time = "n/a";
            else
                time = nf(((float) (optimal) / (float) (real + optimal)) * 100.0f, 1, 1) + "%";
            fill(255, 255, 255, 240);
            textAlign(RIGHT, TOP);
            text("Op: " + time, left + popupW - 5, top + 20);
        }
        // println("Popup is drawn");
    }

    void drawC2SPanel() {
        fill(0, panelAlpha);
        rect(230, 0, width - 230, height - 50);
        stroke(255);
        line(240.0f, 23.0f, 420.0f, 24.0f);
        noStroke();
        fill(255, 255, 255, 240);
        textAlign(LEFT, TOP);
        textFont(fontLabel2, 14);
        if (!bCountrySelection) {
            if (selectedServer == null) {
                text("No server selected", 250, 10);
                return;
            }
            int x = 260, y = 27;
            text("Server " + selectedServer.name, 250, 10);

            for (int i = 0; i < countries; i++) {
                if (selectedServer.countriesInc[i] > 0) {
                    text(countriesNames[i], x, y);
                    y += 24;
                    if (y >= (height - 50)) {
                        y = 27;
                        x += 150;
                    }
                }
            }
        } else {
            if (selectedCountry == -1) {
                text("No country selected", 250, 10);
                return;
            }
            int x = 260, y = 27;
            text("Country " + countriesNames[selectedCountry], 250, 10);

            for (Iterator<Server> itServer = servers.iterator(); itServer.hasNext();) {
                Server s = (Server) itServer.next();
                if (s.countriesInc[selectedCountry] > 0) {
                    text(s.name, x, y);
                    y += 24;
                    if (y >= (height - 50)) {
                        y = 27;
                        x += 150;
                    }
                }
            }
        }
    }

    void drawGlobalStatistic() {

        fill(0, panelAlpha);
        rect(width - qpsWidth - 32, qpsHeight + 32, qpsWidth + 32, 140);

        noStroke();
        fill(255, 255, 255, 240);
        textAlign(CENTER, TOP);
        textFont(fontLabel2, 20);
        text("Global Statistic", width - qpsWidth / 2 - 32, qpsHeight + 32);

        textAlign(LEFT, TOP);
        textFont(fontLabel2);

        text("Global", width - qpsWidth - 32, qpsHeight + 90);
        text("North America", width - qpsWidth - 32, qpsHeight + 110);
        text("Europe", width - qpsWidth - 32, qpsHeight + 130);
        text("Asia", width - qpsWidth - 32, qpsHeight + 150);

        textAlign(RIGHT, TOP);
        textFont(fontLabel);
        // text("QPS,%", width - 85, qpsHeight + 60);
        text("av. Time,ms", width - 10, qpsHeight + 60);
        if (!bCanUseGlobalStat) return;

        // text(nf(((float)qpsGl/qpsGl)*100,1,2)+"%",width - 85, qpsHeight + 90);
        // text(nf(((float)qpsNA/qpsGl)*100,1,2)+"%",width - 85, qpsHeight + 110);
        // text(nf(((float)qpsEu/qpsGl)*100,1,2)+"%",width - 85, qpsHeight + 130);
        // text(nf(((float)qpsAs/qpsGl)*100,1,2)+"%",width - 85, qpsHeight + 150);
        //
        // println("avTimeGl: "+nf(avTimeGl,1,2) +"   "+avTimeGl);

        String s;
        if (avTimeGl > 0)
            s = nf(avTimeGl, 1, 2);
        else
            s = "0";
        text(s + "ms", width - 10, qpsHeight + 90);
        if (avTimeNA > 0)
            s = nf(avTimeNA, 1, 2);
        else
            s = "0";
        text(s + "ms", width - 10, qpsHeight + 110);
        if (avTimeEu > 0)
            s = nf(avTimeEu, 1, 2);
        else
            s = "0";
        text(s + "ms", width - 10, qpsHeight + 130);
        if (avTimeAs > 0)
            s = nf(avTimeAs, 1, 2);
        else
            s = "0";
        text(s + "ms", width - 10, qpsHeight + 150);
    }

    void drawSHisPanel() {

        int i = 0;
        int dx = 120;
        int dy = 100;
        noStroke();
        fill(0, panelAlpha);
        rect(290, 20, 3 * dx + 120, ((servers.size() - 1) / 4) * dy + dy + 50);
        for (Iterator<Server> itServer = servers.iterator(); itServer.hasNext();) {
            i++;
            Server s = (Server) itServer.next();
            s.drawQPSHistory(((i - 1) % 4) * dx + 330, ((i - 1) / 4) * dy + 40, 50, 50);
        }
    }

    void drawSQPSPanel() {

        noStroke();
        fill(0, panelAlpha);
        rect(serversWidth + 1, 0, 50, (servers.size() * 40) + 16);
        rect(serversWidth + 53, 0, 70, (servers.size() * 40) + 16);
        noStroke();
        textFont(fontLabel);
        fill(255, 255, 255, 240);
        textAlign(LEFT, TOP);
        text("Load", serversWidth + 11, 5);
        text("AvgT", serversWidth + 75, 5);
        Integer qpps = 0;

        for (Iterator<Server> itServer = servers.iterator(); itServer.hasNext();) {
            qpps += itServer.next().currentQPS;
        }
        int c = 0;

        for (Iterator<Server> itServer = servers.iterator(); itServer.hasNext();) {
            Server s = (Server) itServer.next();

            int y = 22 + (c * 40);

            strokeWeight(1);
            stroke(0);

            float sqps = s.currentQPS, gqps = qpps;

            float perc;
            if (gqps == 0.0 || fNaN.equals(gqps) || fNaN.equals(sqps))
                perc = 0.0f;
            else
                perc = sqps / gqps * 100;
            if (fNaN.equals(perc)) perc = 0.0f;
            noStroke();
            fill(255, 255, 255, 240);
            textAlign(RIGHT, TOP);
            String st = nf(perc, 1, 1);
            text(st + "%", serversWidth + 36, y + 5);
            // percent = df.format(s.avTime);
            if (fNaN.equals(s.avTime))
                st = "0.0";
            else
                st = nf(s.avTime, 1, 1);
            text(st + "ms", serversWidth + 105, y + 5);
            c++;
            // println(GetServerByID("lon").avTime);
        }
    }

    void drawCountryColorLegend() {
        int winSizeX = 250;
        int winSizeY = 200;
        int winX = width - winSizeX, winY = 300;

        noStroke();
        fill(0, panelAlpha);
        rect(winX, winY, width + 1, winSizeY);

        textFont(fontDate);
        fill(255, 255, 255, 240);
        textAlign(CENTER, TOP);
        text("Country Mode\n Color Legend", winX + winSizeX / 2, winY + 7);

        int boxStartX = winX + 30;
        int boxStartY = winY + 80;
        int boxSizeX = 40;
        int boxSizeY = 20;
        int textStartX = boxStartX + boxSizeX + 15;
        int textStartY = boxSizeY / 2 + boxStartY;
        int spaceBetweenBoxesY = 15;
        int stepY = spaceBetweenBoxesY + boxSizeY;

        textFont(fontLabel2, 12);
        textAlign(LEFT, CENTER);

        // Label for start color
        int ypos = textStartY;
        text("Minimal average time\nfor query", textStartX, ypos);
        // Label for end color
        ypos += stepY;
        text("Maximal average time\nfor query", textStartX, ypos);
        // Lable for zero color
        ypos += stepY;
        text("No average time", textStartX, ypos);

        // Color sample for start color
        ypos = boxStartY;
        fill(cbSStart.CurrentColor, 255);
        rect(boxStartX, ypos, boxSizeX, boxSizeY);
        // Color sample for end color
        ypos += stepY;
        fill(cbSEnd.CurrentColor, 255);
        rect(boxStartX, ypos, boxSizeX, boxSizeY);
        // Color sample for zero color
        ypos += stepY;
        fill(cbSZero.CurrentColor, 255);
        rect(boxStartX, ypos, boxSizeX, boxSizeY);
    }

    void drawCWPanel() {
        noStroke();
        fill(0, panelAlpha);
        rect(gpX, -1, width + 1, gpY);

        textFont(fontDate);
        fill(255, 255, 255, 240);
        textAlign(CENTER, CENTER);
        text("Gradient color", gpX + 150, 20);

        image(imgColorWheel, gpX + 150 - imgColorWheel.width / 2, 50);

        textFont(fontLabel2);
        textAlign(LEFT, TOP);
        fill(255, 255, 255, 240);
        text("Start", cbQStart.Left - 40, cbQStart.Top + 5);
        text("End", cbQEnd.Left - 40, cbQEnd.Top + 5);

        text("Highl.", cbHighlighted.Left - 45, cbHighlighted.Top + 5);

        text("Sel.", cbSelected.Left - 35, cbSelected.Top + 5);

        if (CAST == CAST_COUNTRY) {
            text("Borders", cbCountryBorder.Left - 55, cbCountryBorder.Top + 5);
            cbSStart.Draw();
            cbSEnd.Draw();
            textFont(fontLabel2);
            textAlign(LEFT, TOP);
            fill(255, 255, 255, 240);
            text("Zero", cbSZero.Left - 40, cbSZero.Top + 5);
            cbSZero.Draw();
            minSlider.Draw();
            maxSlider.Draw();

            cbCountryBorder.Draw();
        } else {
            cbQStart.Draw();
            cbQEnd.Draw();

        }
        cbHighlighted.Draw();
        cbSelected.Draw();
    }

    void drawTime() {
        Date date = new Date();

        String suffix = dayExt(day());
        Format formatter = new SimpleDateFormat("EEEE, MMMM d'" + suffix + "', yyyy", Locale.US);
        ((DateFormat) formatter).setTimeZone(TimeZone.getTimeZone("UTC"));
        String logDate = formatter.format(date);

        formatter = new SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US);
        ((DateFormat) formatter).setTimeZone(TimeZone.getTimeZone("UTC"));
        String logTime = formatter.format(date);
        logTime.trim();

        int startX = 15;
        if (logTime.charAt(0) == '1') {
            startX = 5;
        }

        textFont(fontDate);
        textFont(fontTime);

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
        } else if (CAST == CAST_ANY) {

            String s;
            if (bOptimalHightlight)
                s = footerOptima;
            else
                s = footerDynect;
            text(s, width - 21, height - 21);
            image(imgLogoDynect, width - textWidth(s) - imgLogoDynect.width - 35, height - 95);
        } else if (CAST == CAST_BOTH) {
            text(footerDynINC, width - 21, height - 21);
            image(imgLogoDynDNS, width - textWidth(footerDynINC) - imgLogoDynDNS.width - 35, height - 95);
        } else if (CAST == CAST_GRAD) {
            text(footerGrad, width - 21, height - 21);
            image(imgLogoDynect, width - textWidth(footerGrad) - imgLogoDynect.width - 35, height - 95);
        } else if (CAST == CAST_COUNTRY && !bOptimalCountry) {
            text(footerCount, width - 21, height - 21);
            image(imgLogoDynect, width - textWidth(footerCount) - imgLogoDynect.width - 35, height - 95);
        } else if (CAST == CAST_COUNTRY && bOptimalCountry) {
            text(footerOpCount, width - 21, height - 21);
            image(imgLogoDynect, width - textWidth(footerOpCount) - imgLogoDynect.width - 35, height - 95);
        } else if (CAST == CAST_CIRCLE) {
            text(footerCirc, width - 21, height - 21);
            image(imgLogoDynect, width - textWidth(footerCirc) - imgLogoDynect.width - 35, height - 95);
        }

        /*
         * if (bPerlUnicast) {
         * 
         * } else { text(footerDynect, width - 21, height - 21); image(imgLogoDynect, width - textWidth(footerDynect) - imgLogoDynect.width
         * - 35, height - 95); }
         */

        // image(imgLogoDynect, width - 948, height - 95);
    }

    void drawHelp() {
        textFont(fontHelp);
        fill(255, 255, 255, 240);
        if (helpWidth == 0) {
            helpWidth = textWidth(HELP_TEXT);
        }

        int helpHeight_ = 580;
        fill(0, panelAlpha);
        rect(width - helpWidth - 32, 0, helpWidth + 32, helpHeight_);

        fill(255, 240);
        textAlign(RIGHT, TOP);
        text(HELP_TEXT, width - 15, 15);
    }

    void drawKinectOutline() {
        if (isUseKinect) {
            noStroke();
            fill(0, panelAlpha);
            rect(-1, height - 40, 110, 50);
            kinectWrapper.kinect.drawScene();
        }
    }

    void drawQPS() {
        textFont(fontTime);
        fill(0, panelAlpha);
        rect(width - qpsWidth - 32, -1, qpsWidth + 32, qpsHeight + 32);

        fill(255, 240);
        textAlign(LEFT, TOP);

        DecimalFormat df = new DecimalFormat("#,##0");
        String qpsPretty = df.format(currentQPS);

        if (String.valueOf(currentQPS).charAt(0) == '1') {
            text(qpsPretty, width - qpsWidth - 29, 174);
        } else {
            text(qpsPretty, width - qpsWidth - 18, 174);
        }

        textFont(fontDate);
        textAlign(LEFT, TOP);
        text("queries per second", width - qpsWidth - 17, 225);

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
        text((int) (qpsMax), width - 15, 15);

        textAlign(RIGHT, TOP);
        text((int) (qpsMin), width - 15, 155);

        stroke(255);
        strokeWeight(1);
        noFill();

        if (bQPSSolid) {
            beginShape(LINES);
        } else {
            beginShape();
        }

        Integer qpsNum = 0;
        float scaled = 0.0f;
        float freshest = 0.0f;
        int size = qpsHistory.size();

        for (int x = 0; x < size; x++) {
            qpsNum = qpsHistory.get(size - x - 1);

            scaled = map(qpsNum, qpsMin, qpsMax, -120, 0);
            if (scaled < -120) scaled = -120;
            if (scaled > 0) scaled = 0;

            if (freshest == 0.0f) freshest = scaled;

            if (bQPSSolid) {
                stroke(255 - x);
                vertex(width - x - 15, 150);
            } else {
                stroke(240 - x);
            }

            vertex(width - x - 15, abs(scaled) + 30);

            if (qpsNum > newMax) newMax = qpsNum;
            if (qpsNum < newMin || newMin == -1) newMin = qpsNum;
        }

        endShape();

        fill(255);
        noStroke();
        ellipseMode(CENTER);
        ellipse(width - 15, abs(freshest) + 30, 3, 3);

        Integer roundTo = newMax / 10;

        if (roundTo <= 10)
            roundTo = 1;
        else if (roundTo <= 100)
            roundTo = 10;
        else if (roundTo <= 1000)
            roundTo = 100;
        else if (roundTo <= 10000)
            roundTo = 1000;
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

    void point3d(GL gl, float x, float y, float z, float size, int col, int a) {
        int r = col >> 16 & 0xFF;
        int g = col >> 8 & 0xFF;
        int b = col & 0xFF;

        point3d(gl, x, y, z, size, r, g, b, a);
    }

    void point3d(GL gl, float x, float y, float z, float size, int r, int g, int b, int a) {
        if (size > 0) {
            gl.glColor4ub((byte) r, (byte) g, (byte) b, (byte) a);
            gl.glVertex3f(x, y, z);
        }
    }

    void beginPoints(GL gl) {
        beginPoints(gl, 8.0f);
    }

    void beginPoints(GL gl, float pointSize) {

        gl.glEnable(GL.GL_POINT_SMOOTH);
        gl.glHint(GL.GL_POINT_SMOOTH_HINT, GL.GL_NICEST);

        // Glowy transparency
        // gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA); // Standard transparency

        // derived_size = clamp(size * sqrt(1 / (a + b * d + c * d ^ 2)))
        // gl.glPointParameterfv(gl.GL_POINT_DISTANCE_ATTENUATION, attenuation, 0);

        /*
         * float quadratic[] = { 0.0f, 0.0f, 0.000002f };
         * 
         * gl.glPointParameterfv( gl.GL_POINT_DISTANCE_ATTENUATION, quadratic, 0 ); gl.glPointParameterf( gl.GL_POINT_FADE_THRESHOLD_SIZE,
         * 60.0f ); gl.glPointParameterf( gl.GL_POINT_SIZE_MIN, 1.0f ); gl.glPointParameterf( gl.GL_POINT_SIZE_MAX, 15.0f );
         */
        gl.glPointSize(pointSize);

        if (bAlpha) {
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
        } else {
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        }

        gl.glEnable(GL.GL_BLEND);
        gl.glBegin(GL.GL_POINTS);
    }

    void endPoints(GL gl) {
        gl.glEnd();

        gl.glDisable(GL.GL_POINT_SMOOTH);
        gl.glDisable(GL.GL_BLEND);

        gl.glBlendFunc(GL.GL_ONE, GL.GL_ZERO);
    }

    void beginLines(GL gl) {
        beginLines(gl, 2.0f);
    }

    void beginLines(GL gl, float pointSize) {
        stroke(0);
        noStroke();

        gl.glLineWidth(pointSize);

        if (bAlpha) {
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
        } else {
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        }

        gl.glEnable(GL.GL_BLEND);

        gl.glBegin(GL.GL_LINES);
    }

    void endLines(GL gl) {
        gl.glEnd();

        gl.glDisable(GL.GL_BLEND);
    }

    void line3d(GL gl, float x1, float y1, float z1, float x2, float y2, float z2, float size, int col, float a1, float a2) {
        line3d(gl, x1, y1, z1, x2, y2, z2, size, (byte) red(col), (byte) green(col), (byte) blue(col), (byte) a1, (byte) a2);
    }

    void line3d(GL gl, float x1, float y1, float z1, float x2, float y2, float z2, float size, byte r, byte g, byte b, byte a1, byte a2) {
        // 0.0 is invalid
        if (size > 0) {

            gl.glColor4ub(r, g, b, a1);
            gl.glVertex3f(x1, y1, z1);

            gl.glColor4ub(r, g, b, a2);
            gl.glVertex3f(x2, y2, z2);
        }
    }

    void setGlobe() {
        pushMatrix();
        rotateX(radians(-rotationX));
        rotateY(radians(rotationY));
        pgl.gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, mvMatrix, 0);

        // println();
        // println(mvMatrix[0]+"\t"+mvMatrix[1]+"\t"+mvMatrix[2]+"\t"+mvMatrix[3]);
        // println(mvMatrix[4]+"\t"+mvMatrix[5]+"\t"+mvMatrix[6]+"\t"+mvMatrix[7]);
        // println(mvMatrix[8]+"\t"+mvMatrix[9]+"\t"+mvMatrix[10]+"\t"+mvMatrix[11]);
        // println(mvMatrix[12]+"\t"+mvMatrix[13]+"\t"+mvMatrix[14]+"\t"+mvMatrix[15]);
        // println();
        // printMatrix();
    }

    void resetGlobe() {
        popMatrix();
    }

    void setCompenseGlobe() {
        pushMatrix();
        rotateX(radians(rotationX));
        rotateY(radians(rotationY));
    }

    void resetCompenseGlobe() {
        popMatrix();
    }

    void renderGlobe() {
        fill(0);
        noStroke();

        CullingCCW(gl);
        globe.texturedSphere(pgl.gl, texs.getTexture(Textures.GLOBE), GLOBE_RADIUS - 2, rotationX, rotationY, hcam.pos.z);

        ResetCulling(gl);
    }

    void drawServers() {
        boolean useLines = false;
        if (useLines) {
            beginLines(gl, 3.0f);
            for (int i = 0; i < servers.size(); i++) {
                Server s = (Server) servers.get(i);
                if (s.display)
                    line3d(gl, s.pos.x * sizeTail * 2, s.pos.y * sizeTail * 2, s.pos.z * sizeTail * 2, s.pos.x, s.pos.y, s.pos.z, 1.0f,
                            s.getColor(), 255, 255);
            }
            endLines(gl);
        } else {
            // Here we will draw Dyn's logos
            for (int i = 0; i < servers.size(); i++) {
                Server s = (Server) servers.get(i);
                s.DrawOnGlobe(gl);
            }
        }
    }

    void drawQueries() {

        noStroke();
        noFill();

        gl.glDisable(GL.GL_DEPTH_TEST);

        beginPoints(gl, sizeQueries);
        int queryScale = Props.integer(Props.QUERY_SCALE);

        for (Iterator<Query> itQueries = queries.iterator(); itQueries.hasNext();) {
            Query q = itQueries.next();
            if (q.getAge() >= queryScale) {
                itQueries.remove();
                recycleQueryObject(q);
            } else if (bQueries && CAST != CAST_COUNTRY && q.resolver.display) {
                if ((CAST == CAST_BOTH || CAST == (q.anycast ? CAST_ANY : CAST_UNI))
                        || (CAST != CAST_BOTH && CAST != CAST_ANY && CAST != CAST_UNI)) {

                    float sX = screenX(q.pos.x, q.pos.y, q.pos.z);
                    float sY = screenY(q.pos.x, q.pos.y, q.pos.z);
                    float sZ = GetModelZ(q);

                    if (sX > 0 && sY > 0 && sX < width && sY < height && IsPointAtThatSide(q)) {

                        float alf_dist = sZ - sightDist;
                        if (alf_dist > sightDist) alf_dist = sightDist;
                        alf_dist = (alf_dist / sightDist) * 255.0f;

                        float death_clock = queryScale - q.getAge();
                        float alf_age = (death_clock < 255.0f) ? death_clock : 255;

                        float alf = min(alf_dist, alf_age);

                        if (alf > 0.0f) {
                            point3d(gl, q.pos.x, q.pos.y, q.pos.z, 8.0f, GetColor(q), (int) alf);
                        }
                    }
                }
            }
            q.age();
        }
        endPoints(gl);

        if (bLines && CAST != CAST_COUNTRY) {
            beginLines(gl, sizeLines);

            for (Query q : queries) {
                if (!q.resolver.display) continue;
                if ((CAST == CAST_BOTH || CAST == (q.anycast ? CAST_ANY : CAST_UNI))
                        || (CAST != CAST_BOTH && CAST != CAST_ANY && CAST != CAST_UNI)) {

                    float sZ = GetModelZ(q);
                    float alf_dist = sZ - sightDist;
                    if (alf_dist > sightDist) alf_dist = sightDist;
                    alf_dist = (alf_dist / sightDist) * 255.0f;

                    float death_clock = queryScale - q.getAge();
                    float alf_age = (death_clock < 255.0f) ? death_clock : 255;

                    float alf = min(alf_dist, alf_age);

                    if (alf > 0.0f) {
                        if (bTails) {
                            line3d(gl, q.tail.x, q.tail.y, q.tail.z, q.pos.x, q.pos.y, q.pos.z, 1.0f, GetColor(q), tailAlphaBegin, alf);
                        } else {
                            line3d(gl, q.origin.x, q.origin.y, q.origin.z, q.pos.x, q.pos.y, q.pos.z, 1.0f, GetColor(q), tailAlphaBegin,
                                    alf);
                        }
                    }
                }
            }

            endLines(gl);
        }

        if (bShadows && CAST != CAST_COUNTRY) {
            beginPoints(gl, sizeShadow);

            for (Query q : queries) {
                if (!q.resolver.display) continue;
                if (((CAST == CAST_BOTH || CAST == (q.anycast ? CAST_ANY : CAST_UNI)) || (CAST != CAST_BOTH && CAST != CAST_ANY && CAST != CAST_UNI))
                        && IsPointAtThatSide(q)) {
                    point3d(gl, q.origin.x, q.origin.y, q.origin.z, 8.0f, GetColor(q), 128 - q.getAge());
                }
            }

            endPoints(gl);
        }

        gl.glEnable(GL.GL_DEPTH_TEST);
    }

    void CalculateVisibleRange() {
        float sinGl = GLOBE_RADIUS / hcam.pos.z;
        float cosGl = (float) Math.sqrt(1 - sinGl * sinGl);
        float distToXYThruGlobe = hcam.pos.z / cosGl;
        float distToGlobe = hcam.pos.z * cosGl;
        sightDist = (1 - distToGlobe / distToXYThruGlobe) * hcam.pos.z;
    }

    boolean IsPointAtThatSide(Query q) {
        float z_ = GetModelZ(q);

        if (z_ > sightDist) return true;
        return false;
    }

    float GetModelZ(Query q) {
        float z = (float) (-q.pos.x * Math.sin(radians(rotationY)) + q.pos.z * Math.cos(radians(rotationY)));
        // float x = (float)(q.pos.x*Math.cos(radians(rotationY))+q.pos.z*Math.sin(radians(rotationY)));
        float y = q.pos.y;

        float z_ = (float) (y * Math.sin(radians(rotationX)) + z * Math.cos(radians(rotationX)));
        return z_;
    }

    boolean IsAllServersOff() {
        boolean retB = true;

        int sC = servers.size();
        for (int i = 0; i < sC; i++)
            if (((Server) servers.get(i)).display) retB = false;

        return retB;
    }

    int GetColor(Query q) {

        if (CAST == CAST_GRAD) {
            // Server s = null;//GetNearestServer(q);
            if (!IsAllServersOff())
                return GetGradientColor(q);
            else
                return 0xFFFF0000;
        }
        if (CAST == CAST_CIRCLE) {
            // Server s = GetNearestServer(q);
            if (!IsAllServersOff()) {
                if (q.potencial < 1.0f / 3.0f) {
                    return 0xFF00FF00;
                }
                if (q.potencial <= 2.0f / 3.0f) {
                    return 0xFF559900;
                }
                if (q.potencial < 1.0f) {
                    return 0xFFAA4400;
                }
                return 0xFFFF0000;
            } else
                return 0xFFFF0000;
        }
        if (CAST == CAST_ANY) {
            Server s = q.nearest;
            if (s != null) {
                if (bOptimalHightlight) {
                    if (s == q.resolver)
                        return s.getColor();
                    else
                        return 0xFFFFFFFF;
                } else
                    return s.getColor();
            } else
                return 0xFFFF0000;
        }
        return q.resolver.getColor();
    }

    void CalcServersDistCenter() {
        Vector3D center = new Vector3D(0, 0, 0);
        Server s;
        int servCount = 0;
        for (Iterator<Server> itServ = servers.iterator(); itServ.hasNext();) {
            s = itServ.next();
            if (!s.display) continue;
            center.add(s.pos);
            servCount++;
        }
        if (servCount > 0) {
            center.divide(servCount);
        } else {
            center.set(0, 0, 0);
        }
        rc = center;
    }

    float GetSphereDistance(Vector3D p1, Vector3D p2) {
        float cosTh1, cosTh2, sinTh1, sinTh2, sinGa1, sinGa2;
        float flatRadius = (float) Math.sqrt(p1.z * p1.z + p1.y * p1.y);
        Vector3D z = new Vector3D(0, 0, 1);
        cosTh1 = cos(p1.angleWith(z));
        sinTh1 = sin(p1.angleWith(z));
        flatRadius = (float) Math.sqrt(p2.z * p2.z + p2.y * p2.y);
        cosTh2 = cos(p2.angleWith(z));
        sinTh2 = sin(p2.angleWith(z));

        flatRadius = (float) Math.sqrt(p1.x * p1.x + p1.y * p1.y);
        sinGa1 = p1.y / flatRadius;
        flatRadius = (float) Math.sqrt(p2.x * p2.x + p2.y * p2.y);
        sinGa2 = p2.y / flatRadius;

        float L = GLOBE_RADIUS * acos(cosTh1 * cosTh2 + sinTh1 * sinTh2 * (cosTh1 * cosTh2 + sinGa1 * sinGa2));
        return L;
    }

    Server GetFarestServer(Query q) {
        Server s = null;
        Server maxServ = null;
        float distance, maxdistance = 0;

        for (Iterator<Server> itServ = servers.iterator(); itServ.hasNext();) {
            s = itServ.next();
            if (!s.display) continue;
            distance = AngDistToSrvr(s, q);
            if (distance > maxdistance) {
                maxdistance = distance;
                maxServ = s;
            }
        }
        return maxServ;
    }

    Server GetNearestServer(Query q) {
        Server s = null;
        Server minServ = null;
        float distance, mindistance = 2 * PI;

        for (int i = 0; i < servers.size(); i++) {
            s = (Server) servers.get(i);
            if (!s.display) continue;
            distance = AngDistToSrvr(s, q);
            if (distance < mindistance) {
                mindistance = distance;
                minServ = s;
            }
        }
        return minServ;
    }

    float AngDistToSrvr(Server s, Query q) {
        return s.pos.angleWith(q.pos);
    }

    float ColorIt(Query q) {
        float dista = 0;
        int srvrs = 0;
        Server s;
        for (Iterator<Server> itServ = servers.iterator(); itServ.hasNext();) {
            s = itServ.next();
            if (!s.display) {
                continue;
            }
            srvrs++;
            dista += 1.0f / AngDistToSrvr(s, q);
        }
        dista /= (float) srvrs;
        return dista;
    }

    int GetGradientColor(Query q) {
        int _r = (cbQStart.CurrentColor & 0xFFFF0000) >> 16;
        int _g = (cbQStart.CurrentColor & 0xFF00FF00) >> 8;
        int _b = (cbQStart.CurrentColor & 0xFF0000FF);

        int r = (cbQEnd.CurrentColor & 0xFFFF0000) >> 16;
        int g = (cbQEnd.CurrentColor & 0xFF00FF00) >> 8;
        int b = (cbQEnd.CurrentColor & 0xFF0000FF);

        r = round(_r - ((float) (_r - r)) * q.potencial);
        g = round(_g - ((float) (_g - g)) * q.potencial);
        b = round(_b - ((float) (_b - b)) * q.potencial);
        int c = r << 16 | g << 8 | b;

        return c;
    }

    void adjustRotation() {
        // rotationX = (rotationX + targetRotX) / 2;
        // rotationY = (rotationY + targetRotY) / 2;

        rotationX += velocityX;
        rotationY -= velocityY;

        if (rotationX > 360.0f) rotationX -= 360.0f;
        if (rotationX < 0) rotationX += 360.0f;
        if (rotationY > 360.0f) rotationY -= 360.0f;
        if (rotationY < 0) rotationY += 360.0f;

        float dist = hcam.pos.z;
        /*
         * if (dist < globeRadius + 256) { attenuation = attenuation_near; } else { attenuation = attenuation_far; }
         */
        if (zoomingIn) {
            velocityZ += 8;
        } else if (zoomingOut) {
            velocityZ -= 8;
        } else {
            velocityZ *= 0.65;
        }

        if (dist - velocityZ <= GLOBE_RADIUS + 1 + 160) {
            velocityZ = 0;
            hcam.craneTo(new Vector3D(0.0f, 0.0f, GLOBE_RADIUS + 1 + 160));
        } else {
            hcam.crane((int) -velocityZ);
        }

        hcam.cam.feed();
    }

    void CWPanelMouseHandle() {
        int _x = gpX + 150 - imgColorWheel.width / 2;
        int _y = 50;
        if (mouseX > _x && mouseX < _x + imgColorWheel.width && mouseY > 50 && mouseY < _y + imgColorWheel.height) {
            ColorBlock cb = null;
            if (CAST == CAST_COUNTRY) {
                if (cbSStart.Checked) cb = cbSStart;
                if (cbSEnd.Checked) cb = cbSEnd;
                if (cbSZero.Checked) cb = cbSZero;
                if (cbCountryBorder.Checked) cb = cbCountryBorder;
            } else {
                if (cbQStart.Checked) cb = cbQStart;
                if (cbQEnd.Checked) cb = cbQEnd;
            }
            if (cbHighlighted.Checked) cb = cbHighlighted;
            if (cbSelected.Checked) cb = cbSelected;

            if (cb != null) cb.CurrentColor = imgColorWheel.get(mouseX - _x, mouseY - _y);
            if (cbHighlighted.Checked) highlightColor = cb.GetNormColor();
            if (cbSelected.Checked) selectionColor = cb.GetNormColor();
        } else {
            if (CAST == CAST_COUNTRY) {
                cbSStart.Click(mouseX, mouseY);
                cbSEnd.Click(mouseX, mouseY);
                cbSZero.Click(mouseX, mouseY);
                cbCountryBorder.Click(mouseX, mouseY);
                if (minSlider.Click(mouseX, mouseY)) {
                    maxSlider.SetMinTime(minSlider.GetCurrentTime());
                    minAvT = (float) minSlider.GetCurrentTime();
                    // minSlider.SetMaxTime(maxSlider.GetCurrentTime());
                }
                if (maxSlider.Click(mouseX, mouseY)) {
                    minSlider.SetMaxTime(maxSlider.GetCurrentTime());
                    maxAvT = (float) maxSlider.GetCurrentTime();
                    // maxSlider.SetMinTime(minSlider.GetCurrentTime());
                }
            } else {
                cbQStart.Click(mouseX, mouseY);
                cbQEnd.Click(mouseX, mouseY);
            }
            cbHighlighted.Click(mouseX, mouseY);
            cbSelected.Click(mouseX, mouseY);
        }
        if (CAST == CAST_COUNTRY) {
            cbSStart.Draw();
            cbSEnd.Draw();
            cbSZero.Draw();
        } else {
            cbQStart.Draw();
            cbQEnd.Draw();
        }
        cbHighlighted.Draw();
        cbSelected.Draw();
    }

    public void keyReleased() {
        if (keyCode == KeyEvent.VK_CONTROL) {
            bCTRL = false;
            // println("CTRL released");
        }
        if (keyCode == KeyEvent.VK_SHIFT) bSHIFT = false;
        if (keyCode == KeyEvent.VK_ALT) {
            bALT = false;
        }
        if (keyCode == KeyEvent.VK_TAB) {
            bTAB = true;
            if (bPanelC2S) {
                if (bTAB) bCountrySelection = !bCountrySelection;
                bTAB = false;
            }
        }
    }

    public void mouseReleased() {
        if (bPanel1 && mouseButton == LEFT && keyEvent != null && bCTRL) {
            if (mouseX > 0 && mouseX < serversWidth) {
                int ind = (mouseY - 16) / 40;
                if (ind < servers.size()) {
                    Server tmpS = (Server) servers.get(ind);
                    if (tmpS != selectedServer)
                        selectedServer = tmpS;
                    else
                        selectedServer = null;
                }
                // println("Selected server index: "+ind);
            }
        }
        if (bALT && mouseButton == LEFT) {
            Vector3D retV = ProjectScreenToGlobe(pgl.gl, mouseX, mouseY, hcam.pos);
            if (retV == null) {
                // highlightedCountry = -1;
                return;
            }
            String sName;
            if (highlightedCountry >= 0) {
                sName = countriesNames[highlightedCountry];// + "'s Server";
            } else
                sName = "New Server";
            servers.add(new Server(0.0f, 0.0f, sName, "new", 0xFFFFFFFF));
            Server s = (Server) servers.get(servers.size() - 1);
            s.pos = retV;
            serverSwitch = true;
            ResetGlobalStatistic();
        }
        if (bSHIFT && mouseButton == LEFT) {
            selectedCountry = highlightedCountry;

            // servers.add(new Server(0., 0.0, -337.592660812806, -486.676757393927, 537.751735634449, "!!", "!!", (color)0xFFFFFFFF));
            // Server s = (Server)servers.get(servers.size()-1);
            // s.pos = retV;
            return;
        }
    }

    void handleMouse() {
        if (isUseKinect) {
            Kinect kinect = kinectWrapper.kinect;
            kinectX = kinect.getXPos();
            kinectY = kinect.getYPos();
            pkinectX = kinect.getPreviousXPos();
            pkinectY = kinect.getPreviousYPos();

            kinectGrasp = kinect.getGrasp();
            kinectClicked = kinect.getClicked();
            kinectReset = kinect.getReset();

            kinectSwipe = kinect.getSwiped();

            if (kinect.getZoom() != 0) {
                velocityZ += kinect.getZoom();
                kinect.setZoom(0);
            }

            if (!kinectReset) {
                kinectSet = false;

                if (kinect.getStopped()) {
                    velocityX = 0;
                    velocityY = 0;

                    kinect.setStopped(false);
                } else if (kinectSwipe != KinectWrapper.SWIPE_NONE) {
                    println("kinectSwipe: " + kinectSwipe);
                    switch (kinectSwipe) {
                        case KinectWrapper.SWIPE_RIGHT:
                            velocityY -= 5;
                            break;
                        case KinectWrapper.SWIPE_LEFT:
                            velocityY += 5;
                            break;
                        case KinectWrapper.SWIPE_UP:
                            velocityX += 5;
                            break;
                        case KinectWrapper.SWIPE_DOWN:
                            velocityX -= 5;
                            break;
                    }
                    kinect.setSwiped(KinectWrapper.SWIPE_NONE);
                } else {
                    if (kinect.getWaved()) {
                        bPanel1 = !bPanel1;
                        bPanelBottom = !bPanelBottom;
                        bPanel4 = !bPanel4;
                    }

                    if (kinectClicked && kinectGrasp) {
                        velocityX *= 0.95;
                        velocityY *= 0.95;

                        float dx = -(kinectX - pkinectX);
                        float dy = 0.0f;

                        if (bRotationReparation)
                            dy = (kinectY - pkinectY);
                        else
                            dy = -(kinectY - pkinectY);

                        float side = floor(rotationX / 90) % 4;

                        if (side < 0) side = 4 + side;

                        if (abs(side) == 0 || abs(side) == 3) {
                            velocityY -= dx * 0.10;
                        } else {
                            velocityY += dx * 0.10;
                        }

                        velocityX += dy * 0.10;
                    }
                }
            } else if (kinectSet != kinectReset) {
                kinectSet = true;
                resetCamera(323.28f, 5.47f, true);
            }

        }

        if (mousePressed) {
            if (bStatConfigs && mouseButton == LEFT && mouseX > (width - 370) && mouseY < statConfPH) {
                statTimeSlider.Click(mouseX, mouseY);
                shadTimeSlider.Click(mouseX, mouseY);
                servTimeSlider.Click(mouseX, mouseY);
            } else if (bPanel5CW && mouseX > gpX && mouseY < gpY && mouseButton == LEFT) {
                CWPanelMouseHandle();
            } else {
                if (mouseButton == LEFT) {
                    if (bSHIFT && bALT) {
                        // servers.add(new Server(0., 0.0, -337.592660812806, -486.676757393927, 537.751735634449, "!!", "!!",
                        // (color)0xFFFFFFFF));
                        // Server s = (Server)servers.get(servers.size()-1);
                        // s.pos = ProjectScreenToGlobe(pgl.gl, mouseX, mouseY, hcam.pos);
                        return;
                    }
                    velocityX *= 0.95;
                    velocityY *= 0.95;

                    float dx = (mouseX - pmouseX);
                    float dy = 0.0f;

                    if (bRotationReparation)
                        dy = -(mouseY - pmouseY);
                    else
                        dy = (mouseY - pmouseY);

                    float side = floor(rotationX / 90) % 4;

                    if (side < 0) side = 4 + side;

                    if (abs(side) == 0 || abs(side) == 3) {
                        velocityY -= dx * 0.01;
                    } else {
                        velocityY += dx * 0.01;
                    }

                    velocityX += dy * 0.01;
                } else if (mouseButton == RIGHT) {
                    hcam.cam.roll(radians(mouseX - pmouseX));
                } else if (mouseButton == CENTER) {
                    hcam.cam.look(radians(mouseX - pmouseX) / 2.0f, radians(mouseY - pmouseY) / 2.0f);
                }
            }
        }

        if (keyPressed) {
            if (key == CODED) {
                if (keyCode == LEFT) {
                    velocityY += .1;
                } else if (keyCode == RIGHT) {
                    velocityY -= .1;
                } else if (keyCode == UP) {
                    if (bRotationReparation)
                        velocityX += .1;
                    else
                        velocityX -= .1;
                } else if (keyCode == DOWN) {
                    if (bRotationReparation)
                        velocityX -= .1;
                    else
                        velocityX += .1;
                }
            } else if (key == 'a') {
                velocityZ += 3;
            } else if (key == 'z') {
                velocityZ -= 3;
            } else if (key == ' ') {
                velocityX *= 0.95;
                velocityY *= 0.95;
            }
        }
    }

    public void keyPressed() {
        // if (key == '\\' || key == '|') saveFrame("screenshots\\dynviz-########.png");
        serverSwitch = false;
        // bCTRL = false;
        // bALT = false;
        // bSHIFT = false;
        if (!bCTRL && keyCode == KeyEvent.VK_CONTROL) {
            bCTRL = true;
            // println("CTRL pressed");
        }
        if (!bALT && keyCode == KeyEvent.VK_ALT) {
            bALT = true;
        }
        if (!bSHIFT && keyCode == KeyEvent.VK_SHIFT) bSHIFT = true;

        if (!bCTRL) if (key == CODED) {
            if (keyCode == 112) {
                try {
                    Server s = GetServerByID("pao");
                    s.switchDisp();
                    serverSwitch = true;
                    // println("f1");
        } catch (Exception e) {
        }
    } else if (keyCode == 113) {
        try {
            Server s = GetServerByID("ord");
            s.switchDisp();
            serverSwitch = true;
            // println("f2");
        } catch (Exception e) {
        }
    } else if (keyCode == 114) {
        try {
            Server s = GetServerByID("iad");
            s.switchDisp();
            serverSwitch = true;
            // println("f3");
        } catch (Exception e) {
        }
    } else if (keyCode == 115) {
        try {
            Server s = GetServerByID("ewr");
            s.switchDisp();
            serverSwitch = true;
            // println("f4");
        } catch (Exception e) {
        }
    } else if (keyCode == 116) {
        try {
            Server s = GetServerByID("fra");
            s.switchDisp();
            serverSwitch = true;
            // println("f5");
        } catch (Exception e) {
        }
    } else if (keyCode == 117) {
        try {
            Server s = GetServerByID("lon");
            s.switchDisp();
            serverSwitch = true;
            // println("f6");
        } catch (Exception e) {
        }
    } else if (keyCode == 118) {
        try {
            Server s = GetServerByID("ams");
            s.switchDisp();
            serverSwitch = true;
            // println("f7");
        } catch (Exception e) {
        }
    } else if (keyCode == 119) {
        try {
            Server s = GetServerByID("hkg");
            s.switchDisp();
            serverSwitch = true;
            // println("f8");
        } catch (Exception e) {
        }
    } else if (keyCode == 120) {
        try {
            Server s = GetServerByID("lax");
            s.switchDisp();
            serverSwitch = true;
            // println("f9");
        } catch (Exception e) {
        }
    } else if (keyCode == 121) {
        try {
            Server s = GetServerByID("nyc");
            s.switchDisp();
            serverSwitch = true;
            // println("f10");
        } catch (Exception e) {
        }
    } else if (keyCode == 122) {
        try {
            Server s = GetServerByID("spl");
            s.switchDisp();
            serverSwitch = true;
            // println("f11");
        } catch (Exception e) {
        }
    } else if (keyCode == 123) {
        try {
            Server s = GetServerByID("tyo");
            s.switchDisp();
            serverSwitch = true;
            // println("f12");
        } catch (Exception e) {
        }
    }
    // println("COODEED!!");
}

        if (!bALT && !bCTRL && (key == BACKSPACE)) {
            for (Iterator<Server> itServer = servers.iterator(); itServer.hasNext();) {
                itServer.next().display = bSiteToggle;
            }
            bSiteToggle = !bSiteToggle;
        }

        if (!bCTRL && (key == 'g' || key == 'G')) bGlobe = !bGlobe;
        if (!bCTRL && (key == 'q' || key == 'Q')) bQueries = !bQueries;
        if (!bCTRL && (key == 'l' || key == 'L')) bLines = !bLines;
        if (!bCTRL && (key == 's' || key == 'S')) bShadows = !bShadows;
        if (!bCTRL && (key == 'f' || key == 'f')) bAlpha = !bAlpha;
        if (!bCTRL && (key == 'p' || key == 'p')) bQPSSolid = !bQPSSolid;
        if (!bCTRL && (key == DELETE)) bRotationReparation = !bRotationReparation;

        // Camera Position
        if (!bCTRL && (key == 'c' || key == 'C')) resetCamera(40, -95, false);
        if (!bCTRL && (key == 'm' || key == 'M')) resetCamera(43, -72, false);

        if (!bCTRL && (key == 'o' || key == 'O')) if (selectedServer != null) selectedServer.switchDisp();

        // Shader switcher
        // if (!bCTRL && (key == 'D' || key == 'd')) bUseShader = !bUseShader;

        // Help message
        if (!bCTRL && key == 'h') bPanel3 = !bPanel3;

        if (!bCTRL && key == '1') bPanel1 = !bPanel1;
        if (!bCTRL && key == '2') bPanelBottom = !bPanelBottom;
        if (!bCTRL && key == '3') {
            println("bee");
            if (servers.size() > mainServersCount) {
                String sList[] = new String[servers.size() - mainServersCount];
                for (int i = mainServersCount; i < servers.size(); i++) {
                    Server s = servers.get(i);
                    sList[i - mainServersCount] = s.pos.toString() + " " + s.name;
                }
                saveStrings("newServers.txt", sList);
                println("bu");
            }
        }
        if (!bCTRL && key == '4') bPanel4 = !bPanel4;
        if (!bCTRL && key == '5') bPanel5CW = !bPanel5CW;
        if (!bCTRL && key == '6') bPanelSQPS = !bPanelSQPS;
        if (!bCTRL && key == '7') bPanelSHis = !bPanelSHis;
        if (!bCTRL && key == '8') bPanelGlobalStats = !bPanelGlobalStats;
        if (!bCTRL && key == '9') bPanelC2S = !bPanelC2S;
        if (!bCTRL && key == '0' && CAST == CAST_ANY) bOptimalHightlight = !bOptimalHightlight;
        if (!bCTRL && key == '0' && CAST == CAST_COUNTRY) bOptimalCountry = !bOptimalCountry;
        if (!bALT && bCTRL && key == '`' && CAST == CAST_COUNTRY) bPanelCCL = !bPanelCCL;

        if (!bALT && bCTRL && key == '1') bCountriesPopup = !bCountriesPopup;
        // Servers
        if (bALT && (key == BACKSPACE)) {
            println("drawServersFlags: " + isDrawServersFlags);
            isDrawServersFlags = !isDrawServersFlags;
        }
        // Reset statistic
        if (bCTRL && key == '2') {
            ResetGlobalStatistic();
        }
        if (bCTRL && key == '3') bFPS = !bFPS;
        if (bCTRL && key == '4') bStatConfigs = !bStatConfigs;

        if (!bCTRL && key == '=') sizeQueries += 0.2f;
        if (!bCTRL && key == ']') sizeLines += 0.2f;
        if (!bCTRL && key == '\'') sizeShadow += 0.2f;

        if (!bCTRL && (key == '-' && sizeQueries > 0.4f)) sizeQueries -= 0.2f;
        if (!bCTRL && (key == '[' && sizeLines > 0.4f)) sizeLines -= 0.2f;
        if (!bCTRL && (key == ';' && sizeShadow > 0.4f)) sizeShadow -= 0.2f;
        if (!bCTRL && (key == '.') && CAST == CAST_GRAD) {
            startShowing = System.currentTimeMillis();
            bShowTimingPanel = true;
            if (MaxDist > PI / 36.0f) {
                // println(".");
                MaxDist -= PI / 36.0f;
                if (MaxDist < PI / 36.0f) MaxDist = PI / 36.0f;
                Lmax = sqrt(MaxDist * MaxDist + MaxPower * MaxPower);
                serverSwitch = true;
            }
        }
        if (!bCTRL && (key == '/') && CAST == CAST_GRAD) {
            startShowing = System.currentTimeMillis();
            bShowTimingPanel = true;
            if (MaxDist <= 35.0f / 36.0f * PI) {
                // println("/");
                MaxDist += PI / 36.0f;
                if (MaxDist > PI) MaxDist = PI;
                Lmax = sqrt(MaxDist * MaxDist + MaxPower * MaxPower);
                serverSwitch = true;
            }
        }
        if (!bCTRL && !bALT && key == '`') switchCast();
        if (!bCTRL && bALT && key == '`') toggleAutoCastModeSwitcher();

        // Reset camera
        if (!bCTRL && (key == 'r' || key == 'R')) resetCamera(323.28f, 5.47f, true);

        // Reset lines, queries, shadows
        if (!bCTRL && (key == 't' || key == 'T')) {
            sizeQueries = 4.0f;
            sizeShadow = 4.0f;
            sizeLines = 4.0f;
        }

    }

    void switchCast() {
        CAST++;
        if (CAST > CAST_BOTH) CAST = CAST_UNI;

        // println("CAST = " + CAST);
    }

    private boolean isAutoCastModeSwitcherEnabled() {
        return modeSwitchTimer != null;
    }

    private void toggleAutoCastModeSwitcher() {
        if (isAutoCastModeSwitcherEnabled()) {
            stopAutoCastModeSwitcher();
        } else {
            startAutoCastModeSwitcher();
        }
    }

    private void startAutoCastModeSwitcher() {
        if (isAutoCastModeSwitcherEnabled()) return;

        long interval = Props.integer(Props.AUTO_CAST_SWITCH_INTERVAL) * 1000L;
        modeSwitchTimer = new Timer();
        modeSwitchTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                CAST = (CAST == CAST_UNI) ? CAST_ANY : CAST_UNI;
            }
        }, 0, interval);
    }

    private void stopAutoCastModeSwitcher() {
        if (modeSwitchTimer != null) {
            modeSwitchTimer.cancel();
            modeSwitchTimer = null;
        }
    }

    void resetCamera(float lat, float lon, boolean resetCam) {
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

    void CullingCCW(GL gl) {
        gl = pgl.beginGL();
        gl.glFrontFace(GL.GL_CCW);
        gl.glEnable(GL.GL_CULL_FACE);
        pgl.endGL();
    }

    void CullingCW(GL gl) {
        gl = pgl.beginGL();
        gl.glFrontFace(GL.GL_CW);
        gl.glEnable(GL.GL_CULL_FACE);
        pgl.endGL();
    }

    void ResetCulling(GL gl) {
        gl = pgl.beginGL();
        gl.glDisable(GL.GL_CULL_FACE);
        pgl.endGL();
    }

    public Server GetServerByID(String inID) {
        return (Server) showSites.get(inID);
    }

    void roundRect(int x, int y, int w, int h, int r) {
        rectMode(CORNER);

        int ax, ay, hr;

        ax = x + w - 1;
        ay = y + h - 1;
        hr = r / 2;

        rect(x, y, w, h);
        arc(x, y, r, r, radians(180.0f), radians(270.0f));
        arc(ax, y, r, r, radians(270.0f), radians(360.0f));
        arc(x, ay, r, r, radians(90.0f), radians(180.0f));
        arc(ax, ay, r, r, radians(0.0f), radians(90.0f));
        rect(x, y - hr, w, hr);
        rect(x - hr, y, hr, h);
        rect(x, y + h, w, hr);
        rect(x + w, y, hr, h);
    }

    String dayExt(int day) {
        if (day == 11 || day == 12 || day == 13) {
            return ("th");
        } else {
            int d = day % 10;
            if (d == 1) {
                return ("st");
            } else if (d == 2) {
                return ("nd");
            } else if (d == 3) {
                return ("rd");
            } else {
                return ("th");
            }
        }
    }

    void killPerl() {
        try {
            String image = "perl.exe";
            Process p = Runtime.getRuntime().exec("tasklist");

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.matches(".*" + image + ".*")) {
                    Process p2 = Runtime.getRuntime().exec("taskkill /f /im " + image);
                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
                    String line2;
                    while ((line2 = reader2.readLine()) != null) {
                        println(line2);
                    }
                    reader2.close();
                }
            }
            reader.close();
        } catch (IOException e) {
        }
    }

    Integer roundUp(Integer base, Integer num) {
        return ceil((float) base / num) * num;
    }

    Integer roundDown(Integer base, Integer num) {
        return floor((float) base / num) * num;
    }

    /*
     * 
     * NETWORK CODE
     */

    long clockStart = 0;
    long graphStart = 0;
    long zonesStart = 0;
    long countStart = 0;

    private static final int QPS_RESOLUTION = 20;// times per second
    private static final int QPS_RESOLUTION_TIME = 1000 / QPS_RESOLUTION;// millis between measurements
    private int[] qpsPrep = new int[QPS_RESOLUTION];
    private long[] qpsPrepTimes = new long[QPS_RESOLUTION];
    private int qpsPrepIndex = 0;
    private long qpsAccumulatedError = 0;

    private final Queue<String> receivedData = new LinkedList<String>();
    private final Queue<Long> receivedTime = new LinkedList<Long>();

    public void receive(byte[] data) {
        String message = new String(data);
        synchronized (receivedData) {
            receivedData.offer(message);
            receivedTime.offer(System.currentTimeMillis());
        }
    }

    private void loadQueuedMessages() {
        synchronized (receivedData) {
            while (!receivedData.isEmpty()) {
                parseReceivedMessage(receivedData.poll(), receivedTime.poll());
            }
        }
    }

    private void parseReceivedMessage(String message, long receivedTime) {
        String[] strQueries = message.split("\n");

        if (clockStart == 0) clockStart = receivedTime;
        if (graphStart == 0) graphStart = receivedTime;
        if (zonesStart == 0) zonesStart = receivedTime;
        if (countStart == 0) countStart = receivedTime;

        for (int i = 0; i < strQueries.length; i++) {

            strQueries[i].trim();
            String[] strQueryData = strQueries[i].split("\t");

            if (strQueryData.length > 0) {
                try {
                    qps++;
                    qqp++;
                    long clockElapsed = receivedTime - clockStart;

                    if (clockElapsed >= 1000) {
                        if (qpsHistory.size() > 0) {
                            currentQPS = qpsHistory.peekLast();
                            qqp = 0;
                        }

                        clockStart = receivedTime;
                    }
                    clockElapsed = receivedTime - zonesStart;
                    if (clockElapsed >= statTimeSlider.GetCurrentTime() * 1000) {
                        CalcZones();
                        zonesStart = receivedTime;
                    }
                    clockElapsed = receivedTime - countStart;
                    if (clockElapsed >= shadTimeSlider.GetCurrentTime() * 1000) {
                        for (Iterator<Server> itServer = servers.iterator(); itServer.hasNext();) {
                            itServer.next().ReleaseCountries();
                        }
                        CountriesStat();
                        countStart = receivedTime;
                    }

                    long graphElapsed = receivedTime - graphStart;

                    if (graphElapsed >= QPS_RESOLUTION_TIME) {
                        qpsAccumulatedError += graphElapsed - QPS_RESOLUTION_TIME;

                        qpsPrep[qpsPrepIndex] = qps;
                        qpsPrepTimes[qpsPrepIndex] = graphElapsed;
                        qpsPrepIndex = (qpsPrepIndex + 1) % QPS_RESOLUTION;
                        qps = 0;

                        int historyQps = 0;
                        long time = 0;
                        for (int j = 0; j < QPS_RESOLUTION; j++) {
                            historyQps += qpsPrep[j];
                            time += qpsPrepTimes[j];
                        }

                        historyQps = (int) (historyQps * 1000 / time);

                        // interpolating missing values
                        int points = 1 + (int) (qpsAccumulatedError / QPS_RESOLUTION_TIME);
                        qpsAccumulatedError -= (points - 1) * QPS_RESOLUTION_TIME;
                        int qpsStart = currentQPS;
                        int qpsEnd = historyQps * Props.integer(Props.QPS_MULTIPLIER);

                        for (int k = 1; k <= points; k++) {
                            int qps = qpsStart + (qpsEnd - qpsStart) * k / points;
                            qpsHistory.offer(qps);
                        }

                        while (qpsHistory.size() > 200) {
                            qpsHistory.poll();
                        }

                        graphStart = receivedTime;
                    }

                    float inX = Float.parseFloat(strQueryData[0]);
                    float inY = Float.parseFloat(strQueryData[1]);
                    float inZ = Float.parseFloat(strQueryData[2]);

                    String inIP = strQueryData[3];

                    Server inServer = GetServerByID(strQueryData[4]);
                    if (inServer != null) {
                        boolean inAnycast = anycastHelper.isAnycast(inIP);

                        Query q = newQueryInstance();
                        q.init(inX, inY, inZ, inServer, inAnycast);

                        AddQueryToZone(q);
                        int countryInd = GetCountryIndex(q.pos);

                        if (countryInd >= 0 && countryInd < countryQPST.length) {
                            if (CAST == CAST_GRAD || (CAST == CAST_ANY && bOptimalHightlight) || (CAST == CAST_COUNTRY && bOptimalCountry)) {
                                if (q.nearest != null) {
                                    q.nearest.addQPS(q);
                                    q.nearest.AddOpCountry(countryInd);
                                    q.resolver = q.nearest;
                                    AddCountryQuery(q, countryInd);
                                    AddOptimalQuery(q, q.nearest, countryInd);
                                    q.nearest.AddInCountry(countryInd);
                                }
                            } else {
                                AddCountryQuery(q, countryInd);
                                inServer.addQPS(q);
                                inServer.AddInCountry(countryInd);
                            }
                        }

                        queries.add(q);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Queue<Query> queriesRecycler = new LinkedList<Query>();

    private Query newQueryInstance() {
        Query q = queriesRecycler.poll();
        return q == null ? new Query() : q;
    }

    private void recycleQueryObject(Query q) {
        queriesRecycler.offer(q);
    }

    /*
     * 
     * CLASSES
     */

    public class Query {
        private static final long QUERY_AGE_FPS = 60;

        public Vector3D origin = new Vector3D();
        public Vector3D pos = new Vector3D();
        public Vector3D vel = new Vector3D();
        public Vector3D tail = new Vector3D();
        public Vector3D tail_vec = new Vector3D();
        public Vector3D unit_vec = new Vector3D();
        public Vector3D ang = new Vector3D();

        public Server resolver;
        public Server nearest;

        private int age;

        public float potencial;

        public boolean anycast = false;

        private long startTime;

        public void init(float inX, float inY, float inZ, Server inServer, boolean inAnycast) {
            origin.set(inX, inY, inZ);
            pos.set(origin);

            unit_vec.set(pos).normalize();

            vel.set(unit_vec);
            vel.multiply(burstSpeed);

            tail.set(origin);

            tail_vec.set(unit_vec);
            tail_vec.multiply(sizeTail);

            ang.clear();

            resolver = inServer;
            nearest = GetNearestServer(this);

            age = 0;
            potencial = 0f;
            anycast = inAnycast;
            startTime = System.currentTimeMillis();

            CalculateAng();
            CalcPotencial();
        }

        public void age() {
            long duration = System.currentTimeMillis() - this.startTime;
            int nextAge = (int) (duration / QUERY_AGE_FPS);

            while (this.age < nextAge) {
                this.pos.add(vel);

                if (this.age > sizeTail) {
                    this.tail.set(this.pos).subtract(this.tail_vec);
                } else {
                    this.tail.set(this.origin);
                }

                this.age++;
            }
        }

        public void CalcPotencial() {
            float tmpF = 0;

            Server maxServ = null;

            maxServ = this.nearest;
            if (maxServ == null) {
                this.potencial = 0.0f;
                return;
            }

            try {
                tmpF = maxServ.pos.angleWith(this.pos);

                if (tmpF > MaxDist) {
                    potencial = 1.0f;
                    return;
                }

                potencial = sqrt(tmpF * tmpF + (1.0f - tmpF * MaxPower / MaxDist) * (1.0f - tmpF * MaxPower / MaxDist)) / Lmax;

            } catch (Exception e) {
                println("Query::CalcPotencial(), result potencial calculating error!");
            }
        }

        public void CalculateAng() {
            Vector3D vX = new Vector3D(1, 0, 0), vZ = new Vector3D(0, 0, 1), vY = new Vector3D(0, -1, 0);

            Vector3D tmpV = new Vector3D(this.pos);
            tmpV.y = 0;
            ang.x = this.pos.angleWith(vY);
            ang.y = tmpV.angleWith(vZ);
            if (tmpV.angleWith(vX) > PI / 2) ang.y = 2 * PI - ang.y;
            ang.z = 0;
        }

        public void setAge(int inAge) {
            this.age = inAge;
        }

        public int getAge() {
            return this.age;
        }
    }

    public class Server {
        public Vector3D pos;
        public Vector3D unit_vec;
        public Vector3D ang;
        // For pole-flag
        private Vector3D _stand, _direct, _normal;

        public String name;
        public String id;

        private Integer qps = 0;
        private long clockStart;
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

        private int col = 0xFFffffff;

        private final LinkedList<Integer> qpsHistory = new LinkedList<Integer>();

        public float lat, lon;

        public boolean display;

        public Server(float inLat, float inLon, String inName, String inID, int inCol) {
            this.countriesInc = new int[countries];
            this.tmpCountriesInc = new int[countries];
            this.countriesOIn = new int[countries];
            this.tmpCountriesOIn = new int[countries];

            for (int i = 0; i < countries; i++) {
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

            this.pos.setX(GLOBE_RADIUS * cos(lon) * cos(lat));
            this.pos.setY(-GLOBE_RADIUS * sin(lat));
            this.pos.setZ(GLOBE_RADIUS * cos(lat) * sin(lon));

            this.unit_vec = new Vector3D(pos).normalize();

            this.display = true;
            this.ang = new Vector3D();
            this.CalculateAng();

            this.clockStart = System.currentTimeMillis();

            makeDirection(inLat, inLon);
        }

        private void makeDirection(float inLat, float inLon) {
            _stand = new Vector3D(pos).normalize();
            float mD = _stand.x * pos.x + _stand.y * pos.y + _stand.z * pos.z;
            // here we define random dLat and dLon

            randomSeed(millis() * millis());

            float dx = random(6f) - 3f;
            float dy = random(6f) - 3f;
            float dz = random(6f) - 3f;

            float rLat = radians(inLat);
            float rLon = radians(-inLon);
            Vector3D tDir = new Vector3D(GLOBE_RADIUS * cos(rLon) * cos(rLat) + dx, -GLOBE_RADIUS * sin(rLat) + dy, GLOBE_RADIUS
                    * cos(rLat) * sin(rLon) + dz).normalize();
            float t = mD / _stand.dot(tDir);
            tDir.multiply(t);
            _direct = new Vector3D(tDir).subtract(pos).normalize();
            _normal = new Vector3D(tDir).crossWith(pos);
        }

        public void AddInCountry(int cIndex) {
            this.tmpCountriesInc[cIndex]++;
        }

        public void AddOpCountry(int cIndex) {
            this.tmpCountriesOIn[cIndex]++;
        }

        public void ReleaseCountries() {
            ReleaseInCountries();
            ReleaseOInCountries();
        }

        public void ReleaseInCountries() {
            for (int i = 0; i < countries; i++) {
                this.countriesInc[i] = this.tmpCountriesInc[i];
                this.tmpCountriesInc[i] = 0;
                // this.time = 0.0f;
            }
        }

        public void ReleaseOInCountries() {
            for (int i = 0; i < countries; i++) {
                this.countriesOIn[i] = this.tmpCountriesOIn[i];
                this.tmpCountriesOIn[i] = 0;
            }
        }

        public void resetQPS() {
            this.qpsHistory.clear();
            this.avTime = 0;
            this.currentQPS = 0;
            this.qps = 0;
            this.time = 0;
            this.clockStart = System.currentTimeMillis();
        }

        public void addQPS(Query q) {
            this.qps++;

            if (Float.isNaN(this.time)) this.time = 0.0f;
            this.time += this.pos.angleWith(q.pos) * QTime1ms;

            long clockElapsed = System.currentTimeMillis() - this.clockStart;

            if (clockElapsed >= servTimeSlider.GetCurrentTime() * 1000) {
                int qpsMultiplier = Props.integer(Props.QPS_MULTIPLIER);
                this.qpsHistory.offer(qps * qpsMultiplier);
                if (this.qpsHistory.size() >= 100) this.qpsHistory.poll();

                this.currentQPS = qps;

                if (this.currentQPS == 0) {
                    this.avTime = 0;
                } else {
                    this.avTime = this.time / this.currentQPS;
                }

                this.qps = 0;
                this.time = 0;
                this.clockStart = System.currentTimeMillis();
            }
        }

        public void RecalculatePos(float lat, float lon) {
            this.lat = lat;
            this.lon = lon;
            this.pos.setX(GLOBE_RADIUS * cos(ang.y) * cos(ang.x));
            this.pos.setY(-GLOBE_RADIUS * sin(ang.x));
            this.pos.setZ(GLOBE_RADIUS * cos(ang.x) * sin(ang.y));
        }

        public void RecalculatePos() {
            this.pos.setX(GLOBE_RADIUS * cos(this.lon) * cos(this.lat));
            this.pos.setY(-GLOBE_RADIUS * sin(this.lat));
            this.pos.setZ(GLOBE_RADIUS * cos(this.lat) * sin(this.lon));
            CalculateAng();
        }

        public void drawQPSHistory(int _left, int _top, int _width, int _height) {
            Integer newMin = -1, newMax = 0;
            float scaled = 0.0f;
            float freshest = 0.0f;

            noFill();
            strokeWeight(1);
            stroke(100);

            line(_left, _top, _left + _width, _top);
            line(_left, _top + _height, _left + _width, _top + _height);

            stroke(200);
            for (int i = 0; i <= _width; i += 20) {
                line(_left + _width - i, _top - 2, _left + _width - i, _top + 2);
                line(_left + _width - i, _top + _height - 2, _left + _width - i, _top + _height + 2);
            }

            fill(0, 200);
            noStroke();
            rect(_left, _top, _width, _height);

            fill(255);

            textFont(fontLabel);

            // textAlign(LEFT, TOP);
            // text("-"+_width/2+"s", _left-15, _top-15);

            textAlign(RIGHT, TOP);
            text((int) this.qpsMax, _left + _width, _top - 15);

            textAlign(RIGHT, TOP);
            text((int) this.qpsMin, _left + _width, _top + _height + 5);

            textFont(fontLabel2, 16);
            textAlign(CENTER, TOP);
            // textSize(20);
            if (!this.display) fill(0xFFFF0000, 240);
            text(this.name, _left + _width / 2, _top + _height + 15);
            // textSize(12);
            fill(255);
            textFont(fontLabel);

            stroke(255);
            strokeWeight(1);
            noFill();

            if (bQPSSolid) {
                beginShape(LINES);
            } else {
                beginShape();
            }

            Integer allQPS = 0, qpsCount = 0;
            int size = this.qpsHistory.size();
            Integer qpsNum = 0;

            for (int x = 0; x < size && x < _width; x++) {
                qpsNum = this.qpsHistory.get(size - x - 1);
                allQPS += qpsNum;
                qpsCount++;
                scaled = map(qpsNum, this.qpsMin, this.qpsMax, -_height, 0);
                if (scaled < -_height) scaled = -_height;
                if (scaled > 0) scaled = 0;

                if (freshest == 0.0f) freshest = scaled;

                if (qpsNum > newMax) newMax = qpsNum;
                if (qpsNum < newMin || newMin == -1) newMin = qpsNum;

                x++;
            }

            endShape();

            Integer roundTo = newMax / 10;

            if (roundTo <= 10)
                roundTo = 1;
            else if (roundTo <= 100)
                roundTo = 10;
            else if (roundTo <= 1000)
                roundTo = 100;
            else if (roundTo <= 10000)
                roundTo = 1000;
            else if (roundTo <= 100000) roundTo = 10000;

            this.qpsMaxAge++;
            this.qpsMinAge++;

            newMax = roundUp(newMax, roundTo);
            newMin = roundDown(newMin, roundTo);

            if (this.qpsMaxAge > this.qpsAgeWindow || newMax >= this.qpsMax) {
                this.qpsMax = newMax;
                this.qpsMaxAge = 0;
            }

            if (this.qpsMinAge > this.qpsAgeWindow || newMin <= this.qpsMin || this.qpsHistory.size() < 50) {
                this.qpsMin = newMin;
                this.qpsMinAge = 0;
            }

            float akk = 0;

            float medQPS;
            if (qpsCount > 0)
                medQPS = (float) (allQPS / qpsCount);
            else
                medQPS = 0;

            if (this.qpsMin >= 0 && this.qpsMax > 0 && this.qpsMax != this.qpsMin) akk = (float) (_height) / (this.qpsMax - this.qpsMin);

            if (akk < 0) akk = 0;
            strokeWeight(1);
            noFill();
            stroke(0xFFFFFFFF, 240);
            int def = (int) (akk * (medQPS - this.qpsMin));

            int yy = _top + _height - def;
            int yU = _top + (_height - def) / 2;

            line(_left + _width / 2, _top, _left + _width / 2, yU); // whiskers
            line(_left + _width / 2, _top + _height, _left + _width / 2, yy + def / 2); // whiskers

            rect(_left + _width / 5, yU, 3 * _width / 5, yy - yU + def / 2); // box
            line(_left + _width / 5, yy, _left + 4 * _width / 5, yy); // median

            stroke(0xFFFF0000, 255);
            yy = _top + _height - (int) (akk * (this.currentQPS - this.qpsMin));
            line(_left + _width / 3, yy, _left + 2 * _width / 3, yy);
            noStroke();
        }

        public void switchDisp() {
            this.display = !this.display;
            // this.prevQPS = 0;
            this.currentQPS = 0;
            this.avTime = 0;
            if (CAST == CAST_GRAD) {
                this.resetQPS();
            }
            this.qpsHistory.offer(0);
            if (this.qpsHistory.size() >= 100) this.qpsHistory.poll();
        }

        public void CalculateAng() {
            Vector3D vX = new Vector3D(1, 0, 0), vZ = new Vector3D(0, 0, 1), vY = new Vector3D(0, -1, 0);

            Vector3D tmpV = new Vector3D(this.pos);
            tmpV.y = 0;
            ang.x = this.pos.angleWith(vY);
            ang.y = tmpV.angleWith(vZ);
            if (tmpV.angleWith(vX) > PI / 2) ang.y = 2 * PI - ang.y;
            ang.z = 0;
        }

        public void DrawOnGlobe(GL gl) {
            if (this.display) {
                gl.glEnable(GL.GL_TEXTURE_2D);
                pgl.gl.glActiveTexture(GL.GL_TEXTURE0);
                pgl.gl.glBindTexture(GL.GL_TEXTURE_2D, texs.getTexture(Textures.DYNLOGO));
                float mp = sizeTail / GLOBE_RADIUS;
                float xp = pos.x * (1f + mp), yp = pos.y * (1f + mp), zp = pos.z * (1f + mp);
                beginLines(gl, 1.0f);
                line3d(gl, xp, yp, zp, pos.x, pos.y, pos.z, 1.0f, 0xFF8A8A8A, 255, 255);
                endLines(gl);

                gl.glBegin(GL.GL_TRIANGLES);
                gl.glNormal3f(_normal.x, _normal.y, _normal.z);
                gl.glTexCoord2f(0f, 0f);
                gl.glVertex3f(xp, yp, zp);

                xp = pos.x * (1f + mp / 2f);
                yp = pos.y * (1f + mp / 2f);
                zp = pos.z * (1f + mp / 2f);

                gl.glNormal3f(_normal.x, _normal.y, _normal.z);
                gl.glTexCoord2f(0f, 1f);
                gl.glVertex3f(xp, yp, zp);

                xp = pos.x * (1f + mp * 3 / 4f);
                yp = pos.y * (1f + mp * 3 / 4f);
                zp = pos.z * (1f + mp * 3 / 4f);

                gl.glNormal3f(_normal.x, _normal.y, _normal.z);
                gl.glTexCoord2f(1f, 0.5f);
                gl.glVertex3f(xp + _direct.x * sizeTail / 2f, yp + _direct.y * sizeTail / 2f, zp + _direct.z * sizeTail / 2f);

                gl.glEnd();
                pgl.gl.glActiveTexture(GL.GL_TEXTURE0);
                gl.glDisable(GL.GL_TEXTURE_2D);
            }

        }

        public void setColor(int inCol) {
            this.col = inCol;
        }

        public int getColor() {
            return this.col;
        }
    }

    public class Ray {
        public Vector3D p1;
        public Vector3D p2;

        public Ray(float x1, float y1, float z1, float x2, float y2, float z2) {
            p1 = new Vector3D(x1, y1, z1);
            p2 = new Vector3D(x2, y2, z2);
        }
    }

    public class ColorBlock {
        public int CurrentColor;
        public int Left, Top;
        public int Width, Height;
        public boolean Checked;

        public ColorBlock(int _left, int _top, int _width, int _height, int _col) {
            CurrentColor = _col;
            Left = _left;
            Top = _top;
            Width = _width;
            Height = _height;
            Checked = false;
        }

        public void Draw() {
            noStroke();
            if (Checked) {
                fill(255, 255, 0, 255);
                rect(Left - 1, Top - 1, Width + 2, Height + 2);
                fill(CurrentColor, 255);
                rect(Left + 1, Top + 1, Width - 2, Height - 2);
                return;
            }
            fill(CurrentColor, 255);
            rect(Left - 1, Top - 1, Width + 2, Height + 2);
            fill(255);
        }

        public boolean Click(int x, int y) {
            if (x > Left && x < Left + Width && y > Top && y < Top + Height) {
                Checked = true;
            } else
                Checked = false;
            return Checked;
        }

        public Vector3D GetNormColor() {
            return new Vector3D(red(this.CurrentColor) / 255.0f, green(this.CurrentColor) / 255.0f, blue(this.CurrentColor) / 255.0f);
        }
    }

    public class TimeSlider {
        public int currentTime;
        private int width, height;
        private int left, top;
        private int minTime, maxTime;
        private int sliderPos;
        private String caption;

        public TimeSlider(String _caption, int _left, int _top, int _width, int _height, int _minT, int _currTime, int _maxT) {
            this.currentTime = _currTime;
            this.minTime = _minT;
            this.maxTime = _maxT;
            this.left = _left;
            this.top = _top;
            this.width = _width;
            this.height = _height;
            this.caption = _caption;
            if (this.maxTime - this.minTime == 0.0)
                this.sliderPos = 0;
            else
                this.sliderPos = (int) abs((float) (this.currentTime - this.minTime) / (float) (this.maxTime - this.minTime)
                        * ((float) (this.width)));
        }

        public int GetCurrentTime() {
            return this.currentTime;
        }

        public int GetMinTime() {
            return this.minTime;
        }

        public int GetMaxTime() {
            return this.maxTime;
        }

        public void SetCurrentTime(int t) {
            if (t < this.minTime) t = this.minTime + 1;
            if (t < this.maxTime)
                this.currentTime = t;
            else
                this.currentTime = this.maxTime - 1;
            if (this.maxTime - this.minTime == 0.0)
                this.sliderPos = 0;
            else
                this.sliderPos = (int) abs((float) (this.currentTime - this.minTime) / (float) (this.maxTime - this.minTime)
                        * ((float) (this.width)));
        }

        public void SetMinTime(int t) {
            this.minTime = t;
            if (this.currentTime < this.minTime) this.currentTime = this.minTime + 1;
            if (this.maxTime - this.minTime == 0.0)
                this.sliderPos = 0;
            else
                this.sliderPos = (int) abs((float) (this.currentTime - this.minTime) / (float) (this.maxTime - this.minTime)
                        * ((float) (this.width)));
        }

        public void SetMaxTime(int t) {
            this.maxTime = t;
            if (this.currentTime > this.maxTime) this.currentTime = this.maxTime - 1;
            if (this.maxTime - this.minTime == 0.0)
                this.sliderPos = 0;
            else
                this.sliderPos = (int) abs((float) (this.currentTime - this.minTime) / (float) (this.maxTime - this.minTime)
                        * ((float) (this.width)));
        }

        public int GetLeft() {
            return this.left;
        }

        public int GetTop() {
            return this.top;
        }

        public void MoveTo(int _left, int _top) {
            this.left = _left;
            this.top = _top;
        }

        public int GetWidth() {
            return this.width;
        }

        public int GetHeight() {
            return this.height;
        }

        public boolean Click(int x, int y) {
            x = x - this.left;
            y = y - this.top;
            if (x >= 0 && x <= this.width && y >= 0 && y <= this.height) {
                this.SetSliderPos(x);
                return true;
            }
            return false;
        }

        public void Draw() {
            noFill();
            stroke(0xFFFFFFFF, 255);
            rect(this.left, this.top, this.width, this.height);
            fill(255, 255, 255, 240);
            rect(this.left + this.sliderPos - 2, this.top, 4, this.height);
            textFont(fontLabel);
            fill(255, 255, 255, 240);
            textAlign(RIGHT, CENTER);
            text(this.minTime, this.left - 6, this.top + this.height / 2);
            text(this.caption, this.left - 6 - textWidth(nfs(this.minTime, 0)) - 5, this.top + this.height / 2);
            textAlign(LEFT, CENTER);
            text(this.maxTime, this.left + this.width + 6, this.top + this.height / 2);
            text(this.currentTime, this.left + this.width + 6 + textWidth(nfs(this.maxTime, 0)) + 12, this.top + this.height / 2);
        }

        private void SetSliderPos(int x) {
            int tmpI = (int) abs((float) (x) / ((float) (this.width)) * ((float) (this.maxTime - this.minTime))) + this.minTime;
            this.currentTime = tmpI;
            this.sliderPos = x;
        }
    }

    // ADDITIONAL FUNCTIONS FOR SOMETHING ))

    int GetQueryZone(Query q) {
        if (q.ang.x < 0.96079654 && q.ang.x > 0.3107915 && q.ang.y < 2.5807953 && q.ang.y > 1.390796) return ZONE_EU;
        if (q.ang.x < 1.3707962 && q.ang.x > 0.4907916 && (q.ang.y > 5.163984 || q.ang.y < 0.6507965)) return ZONE_NA;
        if (q.ang.x < 1.4007963 && q.ang.x > 0.28079706 && q.ang.y < 4.7307935 && q.ang.y > 2.6107953) return ZONE_AS;
        return ZONE_UN;
    }

    void AddQueryToZone(Query q) {
        int zone = GetQueryZone(q);
        // if (zone == ZONE_UN)
        // return;
        Server s;
        if (CAST == CAST_GRAD) {
            s = q.nearest;
        } else {
            s = q.resolver;
        }

        if (s != null)
            s.addQPS(q);
        else
            return;

        float time = s.pos.angleWith(q.pos) * QTime1ms;
        if (fNaN.equals(time)) time = 0;
        // println("ang: "+s.pos.angleWith(q.pos)+"\t\ttime: "+time+"\t\tS: "+s.pos+"\tQ: "+q.pos);
        switch (zone) {
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

        // if (str(tmpTimeGl) == "NaN")

        // println("T"+time+"\t\tS"+s+"\t\tQ"+q);

        tmpQPSGl += 1;
    }

    void CalcZones() {
        bCanUseGlobalStat = false;

        if (tmpQPSEu != 0)
            avTimeEu = tmpTimeEu / tmpQPSEu;
        else
            avTimeEu = 0.0f;

        if (tmpQPSNA != 0)
            avTimeNA = tmpTimeNA / tmpQPSNA;
        else
            avTimeNA = 0.0f;

        if (tmpQPSAs != 0)
            avTimeAs = tmpTimeEu / tmpQPSAs;
        else
            avTimeAs = 0.0f;
        // println("tmpQPSGl: "+tmpQPSGl+ "  tmpTimeGl: "+tmpTimeGl);

        if (tmpQPSGl != 0 && !fNaN.equals(tmpTimeGl) && !fNaN.equals(tmpQPSGl))
            avTimeGl = tmpTimeGl / tmpQPSGl;
        else {
            avTimeGl = 0.0f;
            // println(avTimeGl);
        }
        qpsGl = tmpQPSGl;
        qpsEu = tmpQPSEu;
        qpsAs = tmpQPSAs;
        qpsNA = tmpQPSNA;

        // print("!"+tmpQPSGl+" "+tmpTimeGl);
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

    void ResetGlobalStatistic() {
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

        for (Iterator<Server> itServer = servers.iterator(); itServer.hasNext();) {
            itServer.next().resetQPS();
        }

        bCanUseGlobalStat = true;
    }

    int GetCountryIndex(Vector3D pos) {
        return GetCountryIndex(pos.x, pos.y, pos.z);
    }

    private int GetCountryIndex(float x, float y, float z) {
        Vector3D vX = new Vector3D(-1, 0, 0), vZ = new Vector3D(0, 0, 1), vY = new Vector3D(0, -1, 0);
        Vector3D ang = new Vector3D();
        Vector3D pos = new Vector3D(x, y, z);
        Vector3D tmpV = new Vector3D(x, y, z);
        tmpV.y = 0;
        ang.x = pos.angleWith(vY);
        ang.y = tmpV.angleWith(vX);
        if (tmpV.angleWith(vZ) > PI / 2) ang.y = 2 * PI - ang.y;
        ang.z = 0;
        PImage maskT = texs.getMaskTex();
        int retCountry = -1;
        int retC = maskT.get((int) (ang.y / (PI * 2) * maskT.width), (int) ((ang.x * 2) / (PI * 2) * maskT.height));
        if (green(retC) > 0 && green(retC) < 255) {
            retCountry = (int) green(retC);
        } else if (red(retC) > 0 && red(retC) < 255) {
            retCountry = (int) red(retC);
        }

        // ((float)highlightedCountry/255.0f) *(countries-1)
        // retCountry = (int)((float)retCountry/255.0f *(float)(countries));
        return retCountry;
    }

    private void AddCountryQuery(Query q, int countryInd) {
        float time = q.resolver.pos.angleWith(q.pos) * QTime1ms;

        countryQPST[countryInd]++;
        if (fNaN.equals(time)) return;
        countryAvT[countryInd] += time;
    }

    private void AddOptimalQuery(Query q, Server nearest, int countryInd) {
        float time = nearest.pos.angleWith(q.pos) * QTime1ms;
        // countryQPST[ind] ++;
        if (fNaN.equals(time)) return;
        optimalAvT[countryInd] += time;
    }

    void CountriesStat() {
        for (int i = 0; i < countries; i++) {
            countryQPS[i] = countryQPST[i];
            if (countryQPS[i] > 0.0 && !fNaN.equals(countryQPS[i])) {
                countryAv[i] = countryAvT[i] / countryQPS[i];
                optimalAv[i] = optimalAvT[i] / countryQPS[i];
            } else {
                countryAv[i] = -1.0f;
                optimalAv[i] = -1.0f;
            }
            countryAvT[i] = 0.0f;
            countryQPST[i] = 0.0f;
            optimalAvT[i] = 0.0f;
        }
        bShaderDataUpdate = true;
    }

    int GetCountryStatColor(int country) {
        // if (countriesNames[country] != null &&
        // countriesNames[country].equals("Russia")) {
        // println("countryAv[Russia]: "+countryAv[country]);
        // }
        if (countryAv[country] == -1.0) return color(red(cbSZero.CurrentColor), green(cbSZero.CurrentColor), blue(cbSZero.CurrentColor));
        float scal = 0f;
        if (countryAv[country] >= maxAvT) scal = 1;
        if (countryAv[country] > minAvT) scal = (countryAv[country] - minAvT) / (maxAvT - minAvT);

        float _r = red(cbSStart.CurrentColor);
        float _g = green(cbSStart.CurrentColor);
        float _b = blue(cbSStart.CurrentColor);

        float r = red(cbSEnd.CurrentColor);
        float g = green(cbSEnd.CurrentColor);
        float b = blue(cbSEnd.CurrentColor);

        r = (_r - (_r - r) * scal);
        g = (_g - (_g - g) * scal);
        b = (_b - (_b - b) * scal);
        return color(r, g, b);
    }

    String SArr2S(String[] sarr) {
        if (sarr.length == 0) return null;
        String retS = sarr[0];
        for (int i = 1; i < sarr.length; i++)
            retS += "\n" + sarr[i];
        return retS;
    }

    Vector3D ProjectScreenToGlobe(GL gl, int x, int y, Vector3D eyePos) {
        // gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        int viewport[] = new int[4];

        double mvmatrix[] = new double[16];
        double projmatrix[] = new double[16];
        int realy = 0;// GL y coord pos
        double wcoord[] = new double[4];// wx, wy, wz;// returned xyz coords

        gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
        // println("===================================================");
        // PrintMatrix("viewport",viewport, 1, 4);
        gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, mvmatrix, 0);
        // PrintMatrix("mvmatrix",mvmatrix, 4, 4);
        gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projmatrix, 0);
        // PrintMatrix("projmatrix",projmatrix, 4, 4);
        // println("===================================================");
        /* note viewport[3] is height of window in pixels */
        realy = viewport[3] - (int) y - 1;
        // System.out.println("Coordinates at cursor are (" + x + ", " + realy);
        glu.gluUnProject((double) x, (double) realy, -1.0, //
                mvmatrix, 0, projmatrix, 0, viewport, 0, wcoord, 0);

        Vector3D z0 = new Vector3D((float) wcoord[0], (float) wcoord[1], (float) wcoord[2]);
        glu.gluUnProject((double) x, (double) realy, 1.0, //
                mvmatrix, 0, projmatrix, 0, viewport, 0, wcoord, 0);
        Vector3D z1 = new Vector3D((float) wcoord[0], (float) wcoord[1], (float) wcoord[2]);

        z1.subtract(z0).normalize();

        Vector3D V = new Vector3D(z1);
        Vector3D O = new Vector3D().subtract(eyePos);
        float tca = O.dot(V);
        if (tca < 0) return null;
        float Dd = O.dot(O) - tca * tca;
        if (Dd > GLOBE_RADIUS * GLOBE_RADIUS) return null;
        float thc = sqrt(GLOBE_RADIUS * GLOBE_RADIUS - Dd);
        float t = tca - thc;

        gl.glFlush();
        Vector3D retV = new Vector3D(eyePos).add(V.multiply(t));
        retV = UseModelTransform(new double[] { retV.x, retV.y, retV.z });
        return retV;
    }

    Vector3D UseModelTransform(double coords[]) {
        double afterX[] = new double[3];
        double afterY[] = new double[3];
        // double rotMat[] = {cos(radians(-rotationY)), 0, sin(radians(-rotationY)),//
        // sin(radians(-rotationX))*sin(radians(-rotationY)), cos(radians(-rotationX)), -sin(radians(-rotationX))*cos(radians(-rotationY)),
        // //
        // -cos(radians(-rotationX))*sin(radians(-rotationY)), sin(radians(-rotationX)), cos(radians(-rotationX))*cos(radians(-rotationY))
        // };
        double rotX[] = { 1.0, 0.0, 0.0,//
                0.0, cos(radians(-rotationX)), -sin(radians(-rotationX)),//
                0.0, sin(radians(-rotationX)), cos(radians(-rotationX)) };

        double rotY[] = { cos(radians(rotationY)), 0.0, -sin(radians(rotationY)),//
                0.0, 1.0, 0.0,//
                sin(radians(rotationY)), 0.0, cos(radians(rotationY)) };
        // rotMat[0] = cos(radians(rotationY))
        // println("NewCoords: ");
        for (int i = 0; i < 3; i++) {
            afterX[i] = 0.0;
            for (int j = 0; j < 3; j++) {
                afterX[i] += rotX[i * 3 + j] * coords[j];
            }
            // println(tmpC[i]);
        }
        for (int i = 0; i < 3; i++) {
            afterY[i] = 0.0;
            for (int j = 0; j < 3; j++) {
                afterY[i] += rotY[i * 3 + j] * afterX[j];
            }
            // println(tmpC[i]);
        }

        return new Vector3D((float) afterY[0], (float) afterY[1], (float) afterY[2]);
    }

    private void HighlightCountry() {
        Vector3D retV = ProjectScreenToGlobe(pgl.gl, mouseX, mouseY, hcam.pos);
        if (retV == null) {
            highlightedCountry = -1;
            return;
        }

        highlightedCountry = GetCountryIndex(retV);
    }

    void drawFPS() {

        now += 1;
        long fpsCurrTime = System.currentTimeMillis();
        if (fpsCurrTime - fpsStartTime > 1000.0) {
            fps = now;
            now = 0;
            fpsStartTime = System.currentTimeMillis();
        }

        if (bFPS) {
            textFont(fontDate);
            fill(255, 0, 10, 250);
            textAlign(LEFT, BOTTOM);
            text("FPS: " + fps, 2, height - 1);
        }
    }

    void PrintMatrix(String mName, int[] mas, int rows, int lines) {
        println(mName);
        String lineout = new String();
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < rows; j++) {
                lineout += mas[j * rows + i];
                lineout += "\t";
            }
            println(lineout);
            lineout = "";
        }
        println();
    }

    void PrintMatrix(String mName, float[] mas, int rows, int lines) {
        println(mName);
        String lineout = new String();
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < rows; j++) {
                lineout += mas[j * rows + i];
                lineout += "\t";
            }
            println(lineout);
            lineout = "";
        }
        println();
    }

    void PrintMatrix(String mName, double[] mas, int rows, int lines) {
        println(mName);
        String lineout = new String();
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < rows; j++) {
                lineout += mas[j * rows + i];
                lineout += "\t\t";
            }
            println(lineout);
            lineout = "";
        }
        println();
    }

}
