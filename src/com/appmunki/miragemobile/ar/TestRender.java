package com.appmunki.miragemobile.ar;

/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

public class TestRender implements Renderer {

	Square square;

	float positionY = 0;

	boolean mostrado = false;

	// private float[] modelViewMatrix = { 0.960261f, 0.144090f, -0.239032f,
	// 0.000000f, -0.262574f, 0.756735f, -0.598671f, 0.000000f, 0.094621f,
	// 0.637644f, 0.764497f, 0.000000f, -0.382671f, 0.451842f, -3.304434f,
	// 1.000000f };

	private float[] modelViewMatrix = { 1.000000f, 0.000000f, 0.000000f,
			0.000000f, 0.000000f, 1.000000f, 0.000000f, 0.000000f, -0.000000f,
			-0.000000f, 1.000000f, 0.000000f, -0.276621f, 0.087513f,
			-2.621431f, 1.000000f };

	private float[] mProjectionMatrix = { 2.000000f, 0.000000f, 0.000000f,
			0.000000f, 0.000000f, 2.000000f, 0.000000f, 0.000000f, 0.000000f,
			0.000000f, -1.220000f, -2.220000f, 0.000000f, 0.0000000f,
			-1.000000f, 0.000000f };

	public TestRender() {

		square = new Square();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);

		// mProjectionMatrix = Matcher.getProjectionMatrix();

	}

	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -8.0f);

		if (!mostrado) {
			mostrado = true;
		}
		gl.glLoadIdentity();
		// gl.glTranslatef(0.0f, 0.0f, -10.0f);

		/*
		 * gl.glMatrixMode(GL10.GL_PROJECTION);
		 * gl.glLoadMatrixf(mProjectionMatrix, 0);
		 * 
		 * gl.glMatrixMode(GL10.GL_MODELVIEW); gl.glLoadIdentity();
		 * 
		 * modelViewMatrix = Matcher.getMatrix();
		 * gl.glLoadMatrixf(modelViewMatrix, 0);
		 */

		// gl.glTranslatef(0, 0, -2);
		gl.glColor4f(0.2f, 0.35f, 0.3f, 0.75f);
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_BLEND);
		if (Matcher.isPatternPresent()) {
			square.draw(gl);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		float ratio = (float) width / height;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);
	}
}