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
import com.appmunki.miragemobile.ar.entity.Plane;
import com.appmunki.miragemobile.ar.entity.Square;

public class ARRender implements Renderer {

	private final String TAG = this.getClass().getSimpleName();

	Square square;

	float positionY = 0;

	

	

	private float[] mProjectionMatrix = { 1f,0,0,0,0,1f,0,0,0,0,1,0,0,0,0,1 };
	float[] colors = { 1f, 0f, 0f, 1f, // point 0 red
            0f, 1f, 0f, 1f, // point 1 green
            0f, 0f, 1f, 1f, // point 2 blue
            1f, 0f, 1f, 1f, };

	private Cube cube;

	private float angle=0.0f;
	private float scale=1;

	private Plane posX;
	private Plane negX;
	private Plane posY;
	private Plane negY;

	private Plane corner1;
	private Plane corner2;
	private Plane corner3;
	private Plane corner4;

	public ARRender() {

		cube = new Cube(.3f, .3f, .3f);//origin
		posX = new Plane(.1f,.1f);
		negX = new Plane(.1f,.1f);
		posY = new Plane(.1f,.1f);
		negY = new Plane(.1f,.1f);
		
		corner1 = new Plane(.1f,.1f);
		corner2 = new Plane(.1f,.1f);
		corner3 = new Plane(.1f,.1f);
		corner4 = new Plane(.1f,.1f);

		posX.setColor(.0f, .0f, 205f/255f, 1f);//posx
		negX.setColor(0f, 191f/255f, 1f, 1f);//negx
		posY.setColor(1f, 0f, 0f, 1f);//posy
		negY.setColor(1f, 105f/255f, 180f/255f, 1f);//negy
		
		corner1.setColor(128f/255, 1f, 0f, 1f);
		corner2.setColor(128f/255, 1f, 0f, 1f);
		corner3.setColor(128f/255, 1f, 0f, 1f);
		corner4.setColor(128f/255, 1f, 0f, 1f);


		posX.setPosition(.5f, 0f, 0f);
		negX.setPosition(-.5f, 0f, 0f);
		posY.setPosition(0f, .5f, 0f);
		negY.setPosition(0f, -.5f, 0f);

		cube.setColor(.2f, .2f, .1f, 1f);
		cube.setPosition(0f, 0f, 0f);
		
		corner1.setPosition(0, 0, 0);//0,0
		corner2.setPosition(1f, 0, 0);//1,0
		corner3.setPosition(1f, -1f, 0);//1,1
		corner4.setPosition(0, -1f, 0);//0,1
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
			if(modelViewMatrix==null) {
				Log.w(TAG, "Modelview null");
				return;
			}
			
			gl.glLoadIdentity();
			gl.glLoadMatrixf(modelViewMatrix, 0);
			//gl.glScalef(scale, scale, scale);
			//gl.glRotatef(90f, 0, 0, 1.0f);

			// Save the current matrixHe wears hoop earrings on his ears, and a baseball cap with horns comin
			gl.glPushMatrix();
			posX.draw(gl);
			gl.glPopMatrix();
			
			gl.glPushMatrix();
			negX.draw(gl);
			gl.glPopMatrix();

			gl.glPushMatrix();
			posY.draw(gl);
			gl.glPopMatrix();

			gl.glPushMatrix();
			negY.draw(gl);
			gl.glPopMatrix();

			

			gl.glPushMatrix();
			corner1.draw(gl);
			gl.glPopMatrix();

			gl.glPushMatrix();
			corner2.draw(gl);
			gl.glPopMatrix();
			
			gl.glPushMatrix();
			corner3.draw(gl);
			gl.glPopMatrix();
			
			gl.glPushMatrix();
			corner4.draw(gl);
			gl.glPopMatrix();
			
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		Log.i(TAG, (width/480f)+"x"+(height/640f));
		scale=(480f/width);
		//scaleheight=(640f/height);
		mProjectionMatrix = Matcher.getProjectionMatrix(width,height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glLoadMatrixf(mProjectionMatrix, 0);

	}
}