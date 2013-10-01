package com.dyndns.dynviz.ui;

import javax.media.opengl.GL;

import com.dyndns.dynviz.DynViz;

import processing.core.PApplet;
import processing.core.PImage;

public class Globe {

	float SINCOS_PRECISION = 0.01f;
	int SINCOS_LENGTH = (int) (360.0 / SINCOS_PRECISION);

	float TWOPI = 2 * PApplet.PI;

	DynViz parent;

	// Sphere stuff
	PImage texmap;

	// Sphere stuff
	int sDetail = 200; // Sphere detail setting

	int imgPadding = 00;

	int tw = 0, th = 0;

	float texCoords[] = new float[sDetail * sDetail * 16 + sDetail * 4];

	float[] cx, cz, sphereX, sphereY, sphereZ;
	int vertCount;
	float sinLUT[];
	float cosLUT[];

	public Globe(DynViz parent) {
		this.parent = parent;
	}

	public void initializeSphere(/* String mapfile */) {
		sinLUT = new float[SINCOS_LENGTH];
		cosLUT = new float[SINCOS_LENGTH];

		for (int i = 0; i < SINCOS_LENGTH; i++) {
			sinLUT[i] = (float) Math.sin(i * PApplet.DEG_TO_RAD * SINCOS_PRECISION);
			cosLUT[i] = (float) Math.cos(i * PApplet.DEG_TO_RAD * SINCOS_PRECISION);
		}

		float delta = (float) SINCOS_LENGTH / sDetail;
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

		// texmap = loadImage(mapfile);
		// tw = texmap.width ;
		// th = texmap.height ;

		// println("Actual image width: " + tw);
		// println("Actual image height: " + th);
		calcTexCoords();
	}

	// Generic routine to draw textured sphere
	public void texturedSphere(GL gl, int globeTextureId, float rad, float rotX, float rotY, float z) {

		parent.noStroke();
		parent.fill(255);
		parent.specular(0);
		// specular(255, 255, 255);
		// shininess(6.0f);
		//
		int v1, v11, v2;

		// Add the Northern cap

		int texElem = 0;

		float vtx[] = { 0.0f, 0.0f, 0.0f };
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glColor4ub((byte) 255, (byte) 255, (byte) 255, (byte) 255);
		gl.glEnable(GL.GL_DEPTH_TEST);

		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, globeTextureId);
		gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE);
		gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_COMBINE);

		gl.glPushMatrix();
		gl.glTranslatef(0f, 0f, -z);
		gl.glRotatef(-rotX, 0.1f, 0.0f, 0.0f);
		gl.glRotatef(rotY, 0.0f, 0.1f, 0.0f);

		gl.glBegin(GL.GL_TRIANGLE_STRIP);
		for (int i = 0; i < sDetail; i++) {
			gl.glNormal3f(0f, -1f, 0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem + 1]);
			// gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem],
			// texCoords[texElem+1]);
			texElem += 2;
			gl.glVertex3f(0f, -rad, 0f);

			vtx[0] = sphereX[i] * rad;
			vtx[1] = sphereY[i] * rad;
			vtx[2] = sphereZ[i] * rad;

			gl.glNormal3f(sphereX[i], sphereY[i], sphereZ[i]);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem + 1]);
			// gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem],
			// texCoords[texElem+1]);
			texElem += 2;
			gl.glVertex3f(vtx[0], vtx[1], vtx[2]);
		}

		gl.glNormal3f(0f, -1f, 0f);
		gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem + 1]);
		// gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem],
		// texCoords[texElem+1]);
		texElem += 2;
		gl.glVertex3f(0f, -rad, 0f);

		vtx[0] = sphereX[0] * rad;
		vtx[1] = sphereY[0] * rad;
		vtx[2] = sphereZ[0] * rad;

		gl.glNormal3f(sphereX[0], sphereY[0], sphereZ[0]);
		gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem + 1]);
		// gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem],
		// texCoords[texElem+1]);
		texElem += 2;
		gl.glVertex3f(vtx[0], vtx[1], vtx[2]);
		gl.glEnd();

		// Middle rings
		int voff = 0;

		for (int i = 2; i < sDetail; i++) {
			v1 = v11 = voff;
			voff += sDetail;
			v2 = voff;

			gl.glBegin(GL.GL_TRIANGLE_STRIP);
			for (int j = 0; j < sDetail; j++) {
				vtx[0] = sphereX[v1] * rad;
				vtx[1] = sphereY[v1] * rad;
				vtx[2] = sphereZ[v1] * rad;

				gl.glNormal3f(sphereX[v1], sphereY[v1], sphereZ[v1]);
				gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem + 1]);
				// gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem],
				// texCoords[texElem+1]);
				texElem += 2;
				gl.glVertex3f(vtx[0], vtx[1], vtx[2]);

				v1++;

				vtx[0] = sphereX[v2] * rad;
				vtx[1] = sphereY[v2] * rad;
				vtx[2] = sphereZ[v2] * rad;

				gl.glNormal3f(sphereX[v2], sphereY[v2], sphereZ[v2]);
				gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem + 1]);
				// gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem],
				// texCoords[texElem+1]);
				texElem += 2;
				gl.glVertex3f(vtx[0], vtx[1], vtx[2]);

				v2++;
			}

			// Close each ring
			v1 = v11;
			v2 = voff;

			vtx[0] = sphereX[v1] * rad;
			vtx[1] = sphereY[v1] * rad;
			vtx[2] = sphereZ[v1] * rad;

			gl.glNormal3f(sphereX[v1], sphereY[v1], sphereZ[v1]);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem + 1]);
			// gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem],
			// texCoords[texElem+1]);
			texElem += 2;
			gl.glVertex3f(vtx[0], vtx[1], vtx[2]);

			vtx[0] = sphereX[v2] * rad;
			vtx[1] = sphereY[v2] * rad;
			vtx[2] = sphereZ[v2] * rad;

			gl.glNormal3f(sphereX[v2], sphereY[v2], sphereZ[v2]);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem + 1]);
			// gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem],
			// texCoords[texElem+1]);
			texElem += 2;
			gl.glVertex3f(vtx[0], vtx[1], vtx[2]);
			gl.glEnd();

		}

		// Add the Southern cap

		gl.glBegin(GL.GL_TRIANGLE_STRIP);
		for (int i = 0; i < sDetail; i++) {
			v2 = voff + i;

			vtx[0] = sphereX[v2] * rad;
			vtx[1] = sphereY[v2] * rad;
			vtx[2] = sphereZ[v2] * rad;

			gl.glNormal3f(vtx[0], vtx[1], vtx[2]);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem + 1]);
			// gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem],
			// texCoords[texElem+1]);
			texElem += 2;
			gl.glVertex3f(vtx[0], vtx[1], vtx[2]);

			gl.glNormal3f(0f, 1f, 0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem + 1]);
			// gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem],
			// texCoords[texElem+1]);
			texElem += 2;
			gl.glVertex3f(0f, rad, 0f);

		}

		vtx[0] = sphereX[voff] * rad;
		vtx[1] = sphereY[voff] * rad;
		vtx[2] = sphereZ[voff] * rad;

		gl.glNormal3f(vtx[0], vtx[1], vtx[2]);
		gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem], texCoords[texElem + 1]);
		// gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem],
		// texCoords[texElem+1]);
		texElem += 2;
		gl.glVertex3f(vtx[0], vtx[1], vtx[2]);
		//
		// vtx[0] = 0;
		// vtx[1] = r;
		// vtx[2] = 0;

		// gl.glNormal3f(vtx[0], vtx[1], vtx[2]);
		// gl.glMultiTexCoord2f(GL.GL_TEXTURE0, texCoords[texElem],
		// texCoords[texElem+1]);
		// // gl.glMultiTexCoord2f(GL.GL_TEXTURE1, texCoords[texElem],
		// texCoords[texElem+1]);
		// texElem+=2;
		// gl.glVertex3f(vtx[0], vtx[1], vtx[2]);
		gl.glEnd();

		gl.glPopMatrix();
		// gl.glActiveTexture(GL.GL_TEXTURE1);
		// gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glDisable(GL.GL_DEPTH_TEST);
		// specular(0);
		// shininess(0);
		// emissive(0);

	}

	public void calcTexCoords() {
		// Add the Northern cap

		float iu = (float) 1f / (float) (sDetail);
		float iv = (float) 1f / (float) (sDetail);

		float u = 0;
		float v = iv;

		int texElem = 0;
		for (int i = 0; i < sDetail; i++) {
			texCoords[texElem] = u;
			texCoords[texElem + 1] = 0;
			texCoords[texElem + 2] = u;
			texCoords[texElem + 3] = v;
			texElem += 4;
			u += iu;
		}
		texCoords[texElem] = u;
		texCoords[texElem + 1] = 0;
		texCoords[texElem + 2] = u;
		texCoords[texElem + 3] = v;
		texElem += 4;

		v = iv;
		for (int i = 2; i < sDetail; i++) {
			u = imgPadding;
			for (int j = 0; j < sDetail; j++) {
				if (j == sDetail - 1) {
					u -= imgPadding;
				}
				texCoords[texElem] = u;
				texCoords[texElem + 1] = v;
				texCoords[texElem + 2] = u;
				texCoords[texElem + 3] = v + iv;
				texElem += 4;
				u += iu;
			}
			texCoords[texElem] = u;
			texCoords[texElem + 1] = v;
			texCoords[texElem + 2] = u;
			texCoords[texElem + 3] = v + iv;
			texElem += 4;
			v += iv;
		}
		u = 0;
		for (int i = 0; i < sDetail; i++) {
			texCoords[texElem] = u;
			texCoords[texElem + 1] = v;
			texCoords[texElem + 2] = u;
			texCoords[texElem + 3] = v + iv;
			texElem += 4;
			u += iu;
		}

		texCoords[texElem] = u;
		texCoords[texElem + 1] = v;
		texCoords[texElem + 2] = u;
		texCoords[texElem + 3] = v + iv;
	}

}
