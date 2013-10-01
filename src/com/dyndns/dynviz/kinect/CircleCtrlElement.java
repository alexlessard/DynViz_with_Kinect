package com.dyndns.dynviz.kinect;
import processing.core.PApplet;
import processing.core.PVector;
import SimpleOpenNI.SimpleOpenNI;
import SimpleOpenNI.XnVCircle;

public class CircleCtrlElement {

	final static int CTRL_DEF = 0;
	final static int CTRL_FOCUS = 1;
	final static int CTRL_ACTIVE = 2;

	PApplet _parent;

	PVector _pos;
	PVector _handPos;
	PVector _centerPos;
	PVector _screenHandPos;
	PVector _screenCenterPos;

	float _radius;
	float _handRadius;
	float _angle;
	int _status;
	SimpleOpenNI _context;

	public CircleCtrlElement(PApplet parent, SimpleOpenNI context, float x, float y, float radius) {
		_parent = parent;
		_context = context;

		_pos = new PVector(x, y, 0);
		_handPos = new PVector();
		_centerPos = new PVector();

		_screenHandPos = new PVector();
		_screenCenterPos = new PVector();

		_radius = radius;
		_angle = 0;
		_status = CTRL_DEF;
	}

	public void setState(int active) {
		_status = active;
	}

	public void setCtrl(float ftime, XnVCircle circle) {
		_angle = (ftime % 1.0f) * 2 * PApplet.PI;
		_centerPos.set(circle.getPtCenter().getX(), circle.getPtCenter().getY(), circle.getPtCenter().getZ());
		_handRadius = circle.getFRadius();
		recalc();
	}

	public void setHandPos(float x, float y, float z) {
		_handPos.set(x, y, z);
		recalc();
	}

	public float getRelativeAngle() {
		// add 180deg
		return _angle + PApplet.PI;
	}

	void recalc() {
		// the center point has no depths, set it to the depth of the current
		// hand
		_centerPos.z = _handPos.z;

		// calc screenpos of the hand + center
		_context.convertRealWorldToProjective(_handPos, _screenHandPos);
		_context.convertRealWorldToProjective(_centerPos, _screenCenterPos);
	}

	public void draw() {
		_parent.pushStyle();

		switch (_status) {
		case CTRL_FOCUS:
			_parent.stroke(0, 255, 0, 90);
			_parent.strokeWeight(5);
			break;
		case CTRL_ACTIVE:
			_parent.stroke(0, 0, 255, 110);
			_parent.strokeWeight(5);
			break;
		case CTRL_DEF:
		default:
			_parent.stroke(255, 0, 0, 50);
			_parent.strokeWeight(2);
			break;
		}
		_parent.noFill();

		_parent.ellipse(_pos.x, _pos.y, _radius * 2, _radius * 2);

		if (_status >= CTRL_ACTIVE) {
			float heightTri = 30;

			_parent.pushMatrix();
			_parent.translate(_pos.x, _pos.y);
			_parent.rotate(getRelativeAngle());
			_parent.line(0, 0, 0, _radius - heightTri);

			_parent.noStroke();
			_parent.fill(0, 0, 255, 110);
			_parent.translate(0, _radius);
			_parent.triangle(0, 0, -20, -heightTri, 20, -heightTri);
			_parent.popMatrix();
		}

		_parent.popStyle();
	}

	public void drawHandsCtrl() {
		if (_status < CTRL_ACTIVE)
			return;

		_parent.pushStyle();

		_parent.stroke(255, 255, 0, 110);
		_parent.strokeWeight(2);
		_parent.noFill();

		_parent.ellipse(_screenCenterPos.x, _screenCenterPos.y, _handRadius * 2, _handRadius * 2);

		_parent.line(_screenHandPos.x, _screenHandPos.y, _screenCenterPos.x, _screenCenterPos.y);

		_parent.popStyle();
	}

}
