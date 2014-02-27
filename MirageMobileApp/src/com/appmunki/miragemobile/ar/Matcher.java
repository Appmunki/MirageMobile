package com.appmunki.miragemobile.ar;

import android.graphics.Bitmap;

public class Matcher {
	private static final String TAG = "Matcher";
	static {
		System.loadLibrary("opencv_java");
		System.loadLibrary("MirageMobile");
	}
	

	
	public static native void load(boolean isDebug);


	public static native int[] match(int width, int height, byte yuv[],
			int[] rgba);
	public static native int[] match(Bitmap bitmap);

	public static native int matchDebug(long mGray);
	
	public static native int matchDebugDiego(long mGray);
	

	public static native void FindFeatures(int width, int height, byte yuv[],
			int[] rgba, int[] gray);

	public static native boolean isPatternPresent();

	public static native float[] getMatrix();

	public static native float[] getProjectionMatrix(int width, int height);

	public static native void convertFrame(int frameWidth, int frameHeight,
			byte[] data, int[] rgba);

	public static native void addPattern(String imageFilePath);

	public static native void addPattern(long mGray);

	public static native int getNumpatternResults();

	public static native float[] getHomography(int i);
	public static native void debugHomography(int i,long mGray);

}
