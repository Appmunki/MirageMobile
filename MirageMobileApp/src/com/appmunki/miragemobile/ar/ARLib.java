package com.appmunki.miragemobile.ar;

import android.graphics.Bitmap;

public class ARLib {
	private static final String TAG = "Matcher";
	static {
		System.loadLibrary("opencv_java");
		System.loadLibrary("MirageMobile");
	}

	public static native void testMatConvert(Bitmap bitmap);

}
