package com.dyndns.dynviz.ui;

import processing.core.PApplet;

public class Vector3D {

	public float x, y, z;

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

	public final void setX(float X) {
		x = X;
	}

	public final void setY(float Y) {
		y = Y;
	}

	public final void setZ(float Z) {
		z = Z;
	}

	public final Vector3D set(float X, float Y, float Z) {
		x = X;
		y = Y;
		z = Z;
		return this;
	}

	public final Vector3D set(Vector3D p) {
		x = p.x;
		y = p.y;
		z = p.z;
		return this;
	}

	public final Vector3D add(Vector3D p) {
		x += p.x;
		y += p.y;
		z += p.z;
		return this;
	}

	public final Vector3D add(float a, float b, float c) {
		x += a;
		y += b;
		z += c;
		return this;
	}

	public final Vector3D subtract(Vector3D p) {
		x -= p.x;
		y -= p.y;
		z -= p.z;
		return this;
	}

	public final Vector3D divide(float a) {
		x /= a;
		y /= a;
		z /= a;
		return this;
	}

	public final Vector3D multiply(float f) {
		x *= f;
		y *= f;
		z *= f;
		return this;
	}

	public final float dot(Vector3D p) {
		return x * p.x + y * p.y + z * p.z;
	}

	public final float length() {
		return PApplet.sqrt(x * x + y * y + z * z);
	}

	public final Vector3D normalize() {
		float l = length();
		if (l == 0) {
			set(0, 0, 0);
		} else {
			divide(l);
		}
		return this;
	}

	public final void clear() {
		x = 0.0F;
		y = 0.0F;
		z = 0.0F;
	}

	public final String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

	public final Vector3D crossWith(Vector3D p) {
		return set(y * p.z - z * p.y, x * p.z - z * p.x, x * p.y - y * p.x);
	}

	public final float angleWith(Vector3D b) {
		float aDotb = this.dot(b);
		float ab = this.length() * b.length();
		if (ab == 0)
			return 0.0f;
		return PApplet.acos(aDotb / ab);
	}

}
