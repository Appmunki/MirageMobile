package com.appmunki.miragemobile.ar;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;

public class Square {

	private FloatBuffer vertexBuffer;
	private FloatBuffer colorBuffer;
	
	final float[] vertices = {
			// X, Y, Z,
			// R, G, B, A
			-0.3f, -0.3f, 0.3f, 0.3f, -0.3f, 0.3f, -0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f,

			0.3f, -0.3f, 0.3f, 0.3f, -0.3f, -0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, -0.3f,

			0.3f, -0.3f, -0.3f, -0.3f, -0.3f, -0.3f, 0.3f, 0.3f, -0.3f, -0.3f, 0.3f, -0.3f,

			-0.3f, -0.3f, -0.3f, -0.3f, -0.3f, 0.3f, -0.3f, 0.3f, -0.3f, -0.3f, 0.3f, 0.3f,

			-0.3f, -0.3f, -0.3f, 0.3f, -0.3f, -0.3f, -0.3f, -0.3f, 0.3f, 0.3f, -0.3f, 0.3f,

			-0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, -0.3f, 0.3f, -0.3f, 0.3f, 0.3f, -0.3f, };
	//private float colors[] = { 1.0f, 0.0f, 0.0f, 0.5f, 0.0f, 1.0f, 0.0f, 0.5f, 0.0f, 0.0f, 1.0f, 0.5f };

	public Square() {
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		
	}

	public void draw(GL10 gl) {
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		
	}
}
