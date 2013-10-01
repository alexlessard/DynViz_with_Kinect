package com.dyndns.dynviz.kinect;

import java.util.ArrayList;
import java.util.TimerTask;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import SimpleOpenNI.SimpleOpenNI;
import SimpleOpenNI.XnPoint3D;
import SimpleOpenNI.XnVCircle;
import SimpleOpenNI.XnVCircleDetector;
import SimpleOpenNI.XnVFlowRouter;
import SimpleOpenNI.XnVHandPointContext;
import SimpleOpenNI.XnVPushDetector;
import SimpleOpenNI.XnVSessionManager;
import SimpleOpenNI.XnVSwipeDetector;

import com.dyndns.dynviz.DynViz;

public class KinectWrapper {

	// swipe constants
	public final static int SWIPE_RIGHT = 0;
	public final static int SWIPE_LEFT = 1;
	public final static int SWIPE_UP = 2;
	public final static int SWIPE_DOWN = 3;
	public final static int SWIPE_NONE = 4;

	// Grasp constants
	private final static int HAND_PLAY = 100;
	private final static int HAND_PLAY_Z = 25;
	private final static int GRASP_THRESHOLD = 20;

	public Kinect kinect;

	private DynViz parent;

	// the main kinect context
	private SimpleOpenNI context;

	// kinect state variables
	boolean handsTrackFlag = false;
	PVector handVec = new PVector();
	ArrayList<PVector> handVecList = new ArrayList<PVector>();
	int handVecListSize = 30;
	String lastGesture = "";

	// Scene size global
	PImage tmpImage = null;

	// used to catch first circle gesture
	boolean bZoom = true;

	// coordinate tracking variables
	float oldX = 0.0f;
	float oldY = 0.0f;

	float newX = 0.0f;
	float newY = 0.0f;

	float kX = 0;
	float kY = 0;

	float startZ = 0.0f;

	float totalSwipeX = 0;
	float totalSwipeY = 0;
	boolean swipeLeft = false;
	boolean swipeUp = false;

	final static int SWIPE_TOLERANCE = 300;
	final static int SWIPE_SPEED = 100;

	// timer related variables
	boolean resetWarning = false;
	boolean resetting = false;
	boolean coolDown = false;
	boolean stopDelay = false;
	boolean swipeDelay = false;
	boolean calibrateDelay = true;

	java.util.Timer resetWarningTimer = new java.util.Timer();
	java.util.Timer resetTimer = new java.util.Timer();
	java.util.Timer coolDownTimer = new java.util.Timer();
	java.util.Timer delayTimer = new java.util.Timer();
	java.util.Timer swipeTimer = new java.util.Timer();
	java.util.Timer calibrateTimer = new java.util.Timer();
	java.util.Timer zoomTimer = new java.util.Timer();

	// notification box colors
	final static int KINECT_BLACK = 0xFF030303;
	final static int KINECT_BLUE = 0xFF0A03FF;
	final static int KINECT_WHITE = 0xFFFFFFFF;
	final static int KINECT_YELLOW = 0xFFFFEB03;
	final static int KINECT_RED = 0xFFFF030B;
	final static int KINECT_GREEN = 0xFF03FF1D;

	int notifyBoxColor = KINECT_BLACK;

	// grasp distance variable
	float distance = 0;

	// when losing hands we need to re-calibrate
	boolean bCalibrated = false;

	// NITE and circle variables
	XnVSessionManager sessionManager;
	XnVCircleDetector circleDetector;
	XnVFlowRouter flowRouter;
	CircleCtrlElement circleCtrl;
	XnVPushDetector pushDetector;
	XnVSwipeDetector swipeDetector;

	float ctrlRadius = 200;

	public KinectWrapper(DynViz parent) {
		this.parent = parent;
		this.kinect = new Kinect(parent);
	}

	// //////////////////////////////////////////////////////////////////////////////////////////
	// hand events
	void onCreateHands(int handId, PVector pos, float time) {
		// println("onCreateHands - handId: " + handId + ", pos: " + pos + ", time:" + time);
		handsTrackFlag = true;
		handVec = pos;

		handVecList.clear();
		handVecList.add(pos);
	}

	void onUpdateHands(int handId, PVector pos, float time) {
		handVec = pos;

		// keep our vector tracking up to date
		handVecList.add(0, pos);
		if (handVecList.size() >= handVecListSize) { // remove the last point
			handVecList.remove(handVecList.size() - 1);
		}

		if (calibrateDelay)
			return;

		CheckGrasp();

		UpdatePosition(pos);

		CheckSwipe();

		CheckStopReset(pos);
	}

	void onDestroyHands(int handId, float time) {
		System.out.println("onDestroyHandsCb - handId: " + handId + ", time:" + time);

		handsTrackFlag = false;
		context.addGesture(lastGesture);
		kinect.setClicked(false);
		notifyBoxColor = KINECT_BLUE;
		resetting = false;
		resetWarning = false;
		stopDelay = false;
		bCalibrated = false;
		calibrateDelay = true;
		kinect.setGrasp(false);

	}

	// //////////////////////////////////////////////////////////////////////////////////////////
	// push event

	void onPush(float fVelocity, float fAngle) {
		System.out.println("pushed");
		kinect.setGrasp(false);
		kinect.setStopped(true);
	}

	// //////////////////////////////////////////////////////////////////////////////////////////
	// swipe events

	void onSwipeUp(float fVelocity, float fAngle) {
		// println("SwipeUp");
	}

	void onSwipeDown(float fVelocity, float fAngle) {
		// println("SwipeDown");
	}

	void onSwipeLeft(float fVelocity, float fAngle) {
		// println("SwipeLeft");
	}

	void onSwipeRight(float fVelocity, float fAngle) {
		// println("SwipeRight");
	}

	void onSwipe(int dir, float fVelocity, float fAngle) {
		// println("Swipe");
	}

	// //////////////////////////////////////////////////////////////////////////////////////////
	// gesture events

	void onRecognizeGesture(String strGesture, PVector idPosition, PVector endPosition) {
		// println("onRecognizeGesture - strGesture: " + strGesture + ", idPosition: " + idPosition + ", endPosition:" + endPosition);

		lastGesture = strGesture;
		if (strGesture.equals("RaiseHand")) {
			context.removeGesture(strGesture);
			kinect.setClicked(true);
			context.startTrackingHands(endPosition);
			// notifyBoxColor = KINECT_GREEN; //KINECT_WHITE;
		}

		if (strGesture.equals("Wave"))
			kinect.setWaved(true);
	}

	void onProgressGesture(String strGesture, PVector position, float progress) {
		// println("onProgressGesture - strGesture: " + strGesture + ", position: " + position + ", progress:" + progress);
	}

	// //////////////////////////////////////////////////////////////////////////////////////////
	// circle events
	void onCircle(float fTimes, boolean bConfident, XnVCircle circle) {
		// println("onCircle: " + fTimes + " , bConfident=" + bConfident);

		if (bZoom && bConfident) {
			if (oldX < circle.getPtCenter().getX()) {
				if (oldY < newY) {
					kinect.setZoom(40);
					bZoom = false;
					zoomTimer = new java.util.Timer();
					zoomTimer.schedule(new ZoomTask(), 2000);
				} else {
					kinect.setZoom(-40);
					bZoom = false;
					zoomTimer = new java.util.Timer();
					zoomTimer.schedule(new ZoomTask(), 2000);
				}
			} else if (oldX > circle.getPtCenter().getX()) {
				if (oldY < newY) {
					kinect.setZoom(-40);
					bZoom = false;
					zoomTimer = new java.util.Timer();
					zoomTimer.schedule(new ZoomTask(), 2000);
				} else {
					kinect.setZoom(40);
					bZoom = false;
					zoomTimer = new java.util.Timer();
					zoomTimer.schedule(new ZoomTask(), 2000);
				}
			}
		}

		circleCtrl.setState(CircleCtrlElement.CTRL_ACTIVE);
		circleCtrl.setCtrl(fTimes, circle);
	}

	void onNoCircle(float fTimes, int reason) {
		System.out.println("onNoCircle: " + fTimes + " , reason= " + reason);

		circleCtrl.setState(CircleCtrlElement.CTRL_FOCUS);
		bZoom = true;
	}

	void onPrimaryPointCreate(XnVHandPointContext pContext, XnPoint3D ptFocus) {
		// println("onPrimaryPointCreate:");
	}

	void onPrimaryPointDestroy(int nID) {
		// println("onPrimaryPointDestroy: " + nID);
	}

	void onPointUpdate(XnVHandPointContext pContext) {
		circleCtrl.setHandPos(pContext.getPtPosition().getX(), pContext.getPtPosition().getY(), pContext.getPtPosition().getZ());
		oldX = newX;
		oldY = newY;

		newX = pContext.getPtPosition().getX();
		newY = pContext.getPtPosition().getY();
	}

	// //////////////////////////////////////////////////////////////////////////////////////////
	// session events

	void onStartSession(PVector pos) {
		System.out.println("onStartSession: " + pos);
		circleCtrl.setState(CircleCtrlElement.CTRL_FOCUS);
		kinect.setReset(false);
		resetting = false;

		// set the initial Z position
		startZ = pos.z;

		// set the calibrating color
		notifyBoxColor = KINECT_GREEN;// KINECT_WHITE;

		// reset the grasp state
		kinect.setGrasp(false);

		// set up the calibration for grasping (at straws??)
		bCalibrated = false;
		calibrateDelay = true;
		calibrateTimer = new java.util.Timer();
		calibrateTimer.schedule(new CalibrateTask(), 2000);
	}

	void onEndSession() {
		System.out.println("onEndSession: ");
		circleCtrl.setState(CircleCtrlElement.CTRL_DEF);

		kinect.setReset(true);
		notifyBoxColor = KINECT_BLACK;
		context.addGesture("RaiseHand");
		kinect.setGrasp(false);
	}

	void onFocusSession(String strFocus, PVector pos, float progress) {
		// println("onFocusSession: focus=" + strFocus + ",pos=" + pos + ",progress=" + progress);
	}

	public class Kinect {

		// lovely private kinect variables
		private float xPos = 0.0f;
		private float yPos = 0.0f;
		private float zPos = 0.0f;
		private float pxPos = 0.0f;
		private float pyPos = 0.0f;
		private float pzPos = 0.0f;
		private boolean clicked = false;
		private boolean stopped = true;
		private int zoom = 0;
		private boolean waved = false;
		private boolean reset = false;
		private int swipe = SWIPE_NONE;
		private boolean grasp = false;

		// constructor, setup all the interaction here
		public Kinect(PApplet inApp) {
			context = new SimpleOpenNI(inApp);

			// disable mirror
			context.setMirror(false);

			// scene window
			context.enableScene();

			// enable depthMap generation
			context.enableDepth();

			// enable hands + gesture generation
			context.enableGesture();
			context.enableHands();

			// setup NITE and the circles
			sessionManager = context.createSessionManager("Click,Wave", "RaiseHand");

			circleDetector = new XnVCircleDetector();

			circleDetector.RegisterCircle(inApp);
			circleDetector.RegisterNoCircle(inApp);
			circleDetector.RegisterPrimaryPointCreate(inApp);
			circleDetector.RegisterPrimaryPointDestroy(inApp);

			circleDetector.RegisterPointUpdate(inApp);

			pushDetector = new XnVPushDetector();
			pushDetector.RegisterPush(inApp);

			swipeDetector = new XnVSwipeDetector();
			swipeDetector.RegisterSwipeUp(inApp);
			swipeDetector.RegisterSwipeDown(inApp);
			swipeDetector.RegisterSwipeLeft(inApp);
			swipeDetector.RegisterSwipeRight(inApp);
			swipeDetector.RegisterSwipe(inApp);

			sessionManager.AddListener(swipeDetector);
			sessionManager.AddListener(pushDetector);
			sessionManager.AddListener(circleDetector);

			// start point for the scene box
			kX = parent.width - context.sceneWidth() / 2;
			kY = (parent.height - context.sceneHeight() / 2) - 118;

			// gestures are slightly sketchy...
			context.addGesture("Wave");
			context.addGesture("Click");
			context.addGesture("RaiseHand");

			// init gui element
			circleCtrl = new CircleCtrlElement(parent, context, parent.width / 2, parent.height / 2, ctrlRadius);

			// like jazz...
			parent.smooth();
		}

		public void drawScene() {
			// update the overall context
			context.update();

			// update nite session context
			context.update(sessionManager);

			// draw the scene
			tmpImage = context.sceneImage().get();
			tmpImage.resize(context.sceneWidth() / 2, context.sceneHeight() / 2);
			parent.image(tmpImage, kX, kY);

			// draw notify box
			parent.stroke(notifyBoxColor);
			parent.strokeWeight(5);

			// top and bottom
			parent.line(kX - 3, kY, parent.width, kY);
			parent.line(kX - 3, parent.height - 118, parent.width, parent.height - 118);

			// left and right
			parent.line(kX, kY, kX, parent.height - 118);
			parent.line(parent.width, kY, parent.width, parent.height - 118);
		}

		// bunch of accessor functions....
		public void toggleClicked() {
			if (clicked)
				clicked = false;
			else
				clicked = true;
		}

		public void setClicked(boolean c) {
			clicked = c;
		}

		public boolean getClicked() {
			return clicked;
		}

		public void setXPos(float x) {
			pxPos = xPos;
			xPos = x;
		}

		public float getXPos() {
			return xPos;
		}

		public void setYPos(float y) {
			pyPos = yPos;
			yPos = y;
		}

		public float getYPos() {
			return yPos;
		}

		public void setZPos(float z) {
			pzPos = zPos;
			zPos = z;
		}

		public float getZPos() {
			return zPos;
		}

		public float getPreviousXPos() {
			return pxPos;
		}

		public float getPreviousYPos() {
			return pyPos;
		}

		public float getPreviousZPos() {
			return pzPos;
		}

		public void setStopped(boolean s) {
			stopped = s;
		}

		public boolean getStopped() {
			return stopped;
		}

		public void setReset(boolean r) {
			reset = r;
		}

		public boolean getReset() {
			return reset;
		}

		public int getZoom() {
			return zoom;
		}

		public void setZoom(int z) {
			zoom = z;
		}

		public boolean getWaved() {
			return waved;
		}

		public void setWaved(boolean w) {
			waved = w;
		}

		public void setSwiped(int s) {
			swipe = s;
		}

		public int getSwiped() {
			return swipe;
		}

		public void setGrasp(boolean g) {
			grasp = g;
		}

		public boolean getGrasp() {
			return grasp;
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////
	// Timer events
	class ResetWarningTask extends TimerTask {
		public void run() {
			System.out.println("Reset Warning...");
			resetWarningTimer.cancel();
			resetWarning = false;
			notifyBoxColor = KINECT_RED;
		}
	}

	class ResetTask extends TimerTask {
		public void run() {
			System.out.println("Resetting!");
			sessionManager.EndSession();
			resetTimer.cancel();
			resetting = false;
			notifyBoxColor = KINECT_BLACK;
			coolDownTimer = new java.util.Timer();
			coolDownTimer.schedule(new CoolDownTask(), 1500);
			coolDown = true;
		}
	}

	class CoolDownTask extends TimerTask {
		public void run() {
			System.out.println("Cooling Down!");
			coolDownTimer.cancel();
			coolDown = false;
		}
	}

	class DelayTask extends TimerTask {
		public void run() {
			System.out.println("Delaying!");
			delayTimer.cancel();
			stopDelay = false;
		}
	}

	class SwipeTask extends TimerTask {
		public void run() {
			System.out.println("Delaying Swipe!");
			swipeTimer.cancel();
			swipeDelay = false;
		}
	}

	class CalibrateTask extends TimerTask {
		public void run() {
			System.out.println("Calibrated!");
			calibrateTimer.cancel();
			calibrateDelay = false;
		}
	}

	class ZoomTask extends TimerTask {
		public void run() {
			System.out.println("Zoom Enabled!");
			zoomTimer.cancel();
			bZoom = true;
		}
	}

	// ////////////////////////////////////////////////////////////////
	// Gestures

	void CheckStopReset(PVector pos) {
		// after a reset, give the individual a second to put his hand down
		if (!coolDown && !swipeDelay) {
			// if the hand is extended compare to the start, beging timing, if it is back, stop the timer, if it is gone call 911 for there
			// has been a horrible accident...
			if (startZ - pos.z > 150 && !resetting) {
				resetting = true;
				resetTimer = new java.util.Timer();
				resetTimer.schedule(new ResetTask(), 8000);
			} else if (startZ - pos.z < 200 && resetting) {
				resetting = false;
				resetTimer.cancel();
				notifyBoxColor = KINECT_WHITE;
			}

			if (startZ - pos.z > 200 && !resetWarning) {
				resetWarning = true;
				resetWarningTimer = new java.util.Timer();
				resetWarningTimer.schedule(new ResetWarningTask(), 3000);
			} else if (startZ - pos.z < 150 && resetWarning) {
				resetWarning = false;
				resetWarningTimer.cancel();
			}
		}
	}

	void CheckSwipe() {
		// if we have a closed hand, don't worry about da swipe
		if (kinect.getGrasp()) {
			totalSwipeX = 0;
			totalSwipeY = 0;
			return;
		}

		System.out.println("oldX - newX: " + (oldX - newX));

		// check if we are a) still going the same direction and b) doing it at a pretty quick rate of change (SWIPE_SPEED)
		if (oldX > newX && oldX - newX > SWIPE_SPEED) {
			if (swipeLeft) {
				totalSwipeX = 0;
				swipeLeft = false;
			}
			totalSwipeX += (oldX - newX);
		} else if (oldX < newX && newX - oldX > SWIPE_SPEED) {
			if (!swipeLeft) {
				totalSwipeX = 0;
				swipeLeft = true;
			}
			totalSwipeX += (newX - oldX);
		} else {
			totalSwipeX = 0;
		}

		// if we got the point where we have swiped, heck, let's set the swipe!
		if (totalSwipeX > SWIPE_TOLERANCE) {
			System.out.println("SWIPED");
			swipeDelay = true;

			totalSwipeX = 0;

			resetting = false;
			resetTimer.cancel();

			resetWarning = false;
			resetWarningTimer.cancel();

			swipeTimer = new java.util.Timer();
			swipeTimer.schedule(new SwipeTask(), 1500);

			if (swipeLeft)
				kinect.setSwiped(SWIPE_LEFT);
			else
				kinect.setSwiped(SWIPE_RIGHT);
		}

		// anything X can do Y can do better, anything X can do Y can do better than X...
		if (oldY > newY && oldY - newY > SWIPE_SPEED) {
			if (swipeUp) {
				totalSwipeY = 0;
				swipeUp = false;
			}
			totalSwipeY += (oldY - newY);
			// println("swipe right: " + totalSwipeX);
		} else if (oldY < newY && newY - oldY > SWIPE_SPEED) {
			if (!swipeUp) {
				totalSwipeY = 0;
				swipeUp = true;
			}
			totalSwipeY += (newY - oldY);
			// println("swipe left: " + totalSwipeX);
		} else {
			totalSwipeY = 0;
		}

		if (totalSwipeY > SWIPE_TOLERANCE) {
			System.out.println("SWIPED");
			swipeDelay = true;

			totalSwipeY = 0;

			resetting = false;
			resetTimer.cancel();

			resetWarning = false;
			resetWarningTimer.cancel();

			swipeTimer = new java.util.Timer();
			swipeTimer.schedule(new SwipeTask(), 1500);

			if (swipeUp)
				kinect.setSwiped(SWIPE_UP);
			else
				kinect.setSwiped(SWIPE_DOWN);
		}
	}

	void UpdatePosition(PVector pos) {
		// don't move the globe for a second if we just stopped it to allow people to drop their hand
		if (!stopDelay && !swipeDelay) {
			kinect.setXPos(pos.x);
			kinect.setYPos(pos.y);
		}

		// always allow the Z movement so reset can be cancelled
		kinect.setZPos(pos.z);
	}

	void CheckGrasp() {
		int[] depthMap = context.depthMap();
		int steps = 3; // to speed up the calculations step by 3
		int index;
		PVector realWorldPoint;
		float current_distance = 0;

		// whole lot of grap to just see if the darn fist is closed... essentially, in this loop in a loop look for the farthest point from
		// the center of the hand in the real world that is within
		// a reasonable distance out (so as not to find random objects at the same depth) and at roughly the same depth of the hand then
		// calculate the distance
		for (int y = 0; y < context.depthHeight(); y += steps) {
			for (int x = 0; x < context.depthWidth(); x += steps) {
				index = x + y * context.depthWidth();
				if (depthMap[index] > 0) {
					realWorldPoint = context.depthMapRealWorld()[index];
					if (handVec.z - realWorldPoint.z < HAND_PLAY_Z && handVec.z - realWorldPoint.z > -HAND_PLAY_Z) {
						if ((handVec.x - realWorldPoint.x < HAND_PLAY && handVec.x - realWorldPoint.x > -HAND_PLAY)
								&& (handVec.y - realWorldPoint.y < HAND_PLAY && handVec.y - realWorldPoint.y > -HAND_PLAY)) {
							float temp_distance = PApplet.sqrt(PApplet.sq(handVec.x - realWorldPoint.x)
									+ PApplet.sq(handVec.y - realWorldPoint.y));
							if (current_distance < temp_distance) {
								current_distance = temp_distance;
							}
						}
					}
				}
			}
		}

		// if this is the first time we are calling this, set the hand distance, we are assumiung an open hand to start
		if (!bCalibrated) {
			distance = current_distance;
			bCalibrated = true;
			notifyBoxColor = KINECT_WHITE;
		}

		// println("current_distance is: " + current_distance + "   distance is: " + distance);

		// check to see if the hand length has shrunk significantally(sp?) if it has, assume a fist close or a point or some other pointing
		// mechanism
		if (current_distance < distance - GRASP_THRESHOLD)
			kinect.setGrasp(true);
		else
			kinect.setGrasp(false);

		// we just figure this junk out, let's print it to show how S-M-R-T we are... ummmmmm... D'OH!
		if (kinect.getGrasp()) {
			System.out.println("GRASPED :" + (current_distance - distance));
		}
	}

}
