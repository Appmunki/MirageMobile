package com.appmunki.miragemobile.ar;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLSurfaceView.Renderer;
import android.os.SystemClock;
import android.util.Log;

public class Renderer2 implements Renderer {

	Square square;

	float positionY = 0;

	boolean mostrado = false;

	// private float[] modelViewMatrix = { 0.960261f, 0.144090f, -0.239032f,
	// 0.000000f, -0.262574f, 0.756735f, -0.598671f, 0.000000f, 0.094621f,
	// 0.637644f, 0.764497f, 0.000000f, -0.382671f, 0.451842f, -3.304434f,
	// 1.000000f };

	private float[] modelViewMatrix = { 1.000000f, -0.000000f, 0.000000f,
			0.000000f, 0.000000f, 1.000000f, 0.000000f, 0.000000f, -0.000000f,
			-0.000000f, 1.000000f, 0.000000f, -0.276621f, 0.087513f,
			-2.621431f, 1.000000f };

	private float[] mProjectionMatrix = {
			1.000200f, 0.000000f, 0.000000f, 0.000000f, 0.000000f, 1.000000f,
			0.000000f, 0.000000f, 0.0f, 0.0f, 0.000000f, -1.000000f, 0.000000f,
			0.0000000f, -0.020002f, 0.000000f };

	public Renderer2() {

		square = new Square();
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);

	}

	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -8.0f);

		if (!mostrado) {
			mostrado = true;
		}
		long time = SystemClock.uptimeMillis() % 10000L;
		float angleInDegrees = (1.0f / 10000.0f) * ((int) time);

		positionY = (1 / 10000.0f) * ((int) time);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -10.0f);
		gl.glRotatef(angleInDegrees, 0.0f, 1.0f, 0.0f);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadMatrixf(mProjectionMatrix, 0);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		modelViewMatrix = Matcher.getMatrix();
		
		gl.glLoadMatrixf(modelViewMatrix, 0);

		gl.glTranslatef(0, 0, -2);
		gl.glColor4f(0.2f, 0.35f, 0.3f, 0.75f);
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_BLEND);
		square.draw(gl);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		float ratio = (float) width / height;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);

	}
}
