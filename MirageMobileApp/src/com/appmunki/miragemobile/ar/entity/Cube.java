package com.appmunki.miragemobile.ar.entity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Cube  {
	private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;
    private ByteBuffer  mIndexBuffer;
        
    private float vertices[] = {
                                -0.1f, -0.1f, -0.1f,//0
                                0.1f, -0.1f, -0.1f,//1
                                0.1f,  0.1f, -0.1f,//2
                                -0.1f, 0.1f, -0.1f,//3
                                -0.1f, -0.1f,  0.1f,//4
                                0.1f, -0.1f,  0.1f,//5
                                0.1f,  0.1f,  0.1f,//6
                                -0.1f,  0.1f,  0.1f//7
                                };
    private float colors[] = {
    						   //front
                               0.0f,  1.0f,  0.0f,  1.0f,
                               0.0f,  1.0f,  0.0f,  1.0f,
                               1.0f,  0.5f,  0.0f,  1.0f,
                               1.0f,  0.5f,  0.0f,  1.0f,
                               //back
                               1.0f,  0.0f,  0.0f,  1.0f,
                               1.0f,  0.0f,  0.0f,  1.0f,
                               0.0f,  0.0f,  1.0f,  1.0f,
                               0.0f,  0.0f,  1.0f,  1.0f
                            };
   
    private byte indices[] = {
    		// front
    	    0, 1, 2,
    	    2, 3, 0,
    	    // top
    	    3, 2, 6,
    	    6, 7, 3,
    	    // back
    	    7, 6, 5,
    	    5, 4, 7,
    	    // bottom
    	    4, 5, 1,
    	    1, 0, 4,
    	    // left
    	    4, 0, 3,
    	    3, 7, 4,
    	    // right
    	    1, 5, 6,
    	    6, 2, 1
                              };
    public Cube() {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuf.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
            
        byteBuf = ByteBuffer.allocateDirect(colors.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mColorBuffer = byteBuf.asFloatBuffer();
        mColorBuffer.put(colors);
        mColorBuffer.position(0);
            
        mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);
    }
    public void draw(GL10 gl) {             
        gl.glFrontFace(GL10.GL_CW);
        
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
        
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
         
        gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, 
                        mIndexBuffer);
            
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }
}
