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

import com.appmunki.miragemobile.ar.entity.Cube;
import com.appmunki.miragemobile.ar.entity.Cube2;
import com.appmunki.miragemobile.ar.entity.Grid;
import com.appmunki.miragemobile.ar.entity.Plane;
import com.appmunki.miragemobile.ar.entity.Square;
import com.appmunki.miragemobile.ar.entity.Square2;

public class ARRender implements Renderer {

	private final String TAG = this.getClass().getSimpleName();

	Square2 square;

	float positionY = 0;

	

	

	private float[] mProjectionMatrix = { 1f,0,0,0,0,1f,0,0,0,0,1,0,0,0,0,1 };
	float[] colors = { 1f, 0f, 0f, 1f, // point 0 red
            0f, 1f, 0f, 1f, // point 1 green
            0f, 0f, 1f, 1f, // point 2 blue
            1f, 0f, 1f, 1f, };

	private Cube2 cube;

	private float angle=0.0f;
	private float scalewidth=1;
	private float scaleheight=1;

	private Plane plane;
	public ARRender() {

		cube = new Cube2(.3f, .3f, .3f);
		square = new Square2();
		plane = new Plane(.3f,.3f);
		plane.setColor(.1f, .2f, .3f, 1f);
		cube.setColor(.2f, .2f, .1f, 1f);
		plane.setPosition(1f, 0f, 0f);
		cube.setPosition(0f, 0f, 0f);
		cube.setColors(colors);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);


	}

	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glLoadMatrixf(mProjectionMatrix, 0);
		if (Matcher.isPatternPresent()) {
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			float[] modelViewMatrix = Matcher.getMatrix();
			gl.glLoadIdentity();
			gl.glLoadMatrixf(modelViewMatrix, 0);
			gl.glScalef(scalewidth, scaleheight, 1f);
			gl.glRotatef(90f, 0, 0, 1.0f);

			// Save the current matrixHe wears hoop earrings on his ears, and a baseball cap with horns comin
			gl.glPushMatrix();
	    	gl.glColor4f(1f, 0.5f, 0.5f, 1.0f); // 0x8080FFFF
			//square.draw(gl);
			cube.draw(gl);
			plane.draw(gl);

			// Restore to the matrix as it was before C.
			gl.glPopMatrix();

			
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		Log.i(TAG, (width/480f)+"x"+(height/640f));
		scalewidth=(width/480f);
		scaleheight=(height/640f);
		mProjectionMatrix = Matcher.getProjectionMatrix(width,height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glLoadMatrixf(mProjectionMatrix, 0);

	}
}