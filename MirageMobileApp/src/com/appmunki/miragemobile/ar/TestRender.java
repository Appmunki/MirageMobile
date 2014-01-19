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


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class TestRender implements Renderer {

	Square square;

	float positionY = 0;

	boolean mostrado = false;


	 private float[] modelViewMatrixRender = {
			0.9998104f,		-0.016008556f,	-0.01108686f,	0.0f,
			0.017046511f,	0.9947495f,		0.10091009f,	0.0f,
			0.009413224f,	-0.10107995f,	0.99483377f,	0.0f,
			-0.11143964f,	-0.06826526f,	-3.93519f,		1.0f};
	 
	 private float[] modelViewMatrix = {
			 0.998621f,		-0.013804f,		-0.050645f,		0.000000f,
			 0.015006f,		0.999613f,		0.023417f,		0.000000f,
			 0.050302f,		-0.024144f,		0.998442f,		0.000000f,
			 0.079442f,		-0.180562f,		-3.755618f,		1.000000f};
	 

//	private float[] modelViewMatrix = { 1.000000f, 0.000000f, 0.000000f,
//			0.000000f, 0.000000f, 1.000000f, 0.000000f, 0.000000f, -0.000000f,
//			-0.000000f, 1.000000f, 0.000000f, -0.276621f, 0.087513f,
//			-2.621431f, 1.000000f };

	private float[] mProjectionMatrix = {-3.276789f,0.000000f,0.000000f,0.000000f,0.000000f,2.457592f,0.000000f,0.000000f,-0.095777f,-0.027332f,-1.000200f,-1.000000f,0.000000f,0.000000f,-0.020002f,0.000000f};

	public TestRender() {

		square = new Square();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);

//		mProjectionMatrix = Matcher.getProjectionMatrix();
//		
//		for (int i = 0; i < mProjectionMatrix.length; i++) {
//			Log.v("mProjectionMatrix",mProjectionMatrix[i]+"");
//		}

	}

	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -20.0f);

		gl.glLoadIdentity();

		
		 gl.glMatrixMode(GL10.GL_PROJECTION);
		 gl.glLoadMatrixf(mProjectionMatrix, 0);
		 
		 gl.glMatrixMode(GL10.GL_MODELVIEW); 
		 gl.glLoadIdentity();
//		 modelViewMatrix = Matcher.getMatrix();
		 gl.glLoadMatrixf(modelViewMatrix, 0);
		
		 
		 
		gl.glColor4f(0.2f, 0.35f, 0.3f, 0.75f);
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_BLEND);
		square.draw(gl);
		
		
		
		
		gl.glColor4f(0.4f, 0.2f, 0.3f, 0.75f);
		 gl.glMatrixMode(GL10.GL_MODELVIEW); 
		 gl.glLoadIdentity();
//		 modelViewMatrix = Matcher.getMatrix();
		 gl.glLoadMatrixf(modelViewMatrixRender, 0);
		
		square.draw(gl);
		

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