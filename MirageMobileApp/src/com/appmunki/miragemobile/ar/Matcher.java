package com.appmunki.miragemobile.ar;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.appmunki.miragemobile.utils.Util;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class Matcher {
	private static final String TAG = "Matcher";
	static {
		System.loadLibrary("opencv_java");
		System.loadLibrary("MirageMobile");
	}
	public static boolean debug=false;
	public static boolean debugcamera=false;
	
	/**
	 * Debugging version of the add Pattern code
	 * @param name the name of the pattern that will be saved
	 * @param in the inputstream containing the image to be saved
	 */
	public static void addPattern(String name, InputStream in)
			 {
		Bitmap bitmap = Util. decodeSampledBitmapFromStream(in,640,480);
		addPattern(name, bitmap);
		bitmap.recycle();
	}
	/**
	 * Debugging version of the add Pattern code
	 * @param name the name of the pattern that will be saved
	 * @param bitmap the bitmap containing the image to be saved
	 */
	private static void addPattern(String name, Bitmap bitmap) {
		Log.i(TAG, bitmap.getWidth() + "x" + bitmap.getHeight());
		Mat color = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
		Mat grey = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);

		Utils.bitmapToMat(bitmap, color);
		Imgproc.cvtColor(color, grey, Imgproc.COLOR_RGB2GRAY);

		Matcher.addPattern(grey.getNativeObjAddr());
		if(debug){
			File dir = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + File.separator + "miragemobile/");
			dir.mkdirs();
			Highgui.imwrite(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + File.separator + "miragemobile/" + name,
					grey);
		}
		color.release();
		grey.release();

	}
	
	public static native void load(boolean isDebug);


	public static native int[] match(int width, int height, byte yuv[],
			int[] rgba);
	public static native int[] match(Bitmap bitmap);

	public static native int matchDebug(long mGray);
	

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
	public static native void debugScaledHomography(int i,long mGray);
	public static native void debugScaledRotatedHomography(int i,long mGray);
	
	/**
	 * Match debug call used to help debug the homographies in a image
	 * 
	 * @param mat
	 *            of the image being matched
	 * @return number of patterns found in the scene
	 */
	public static int matchDebug(Mat color, Mat grey) {
		int resultSize = Matcher.matchDebug(grey.getNativeObjAddr());
		String currentDateTimeString = DateFormat.getDateTimeInstance().format(
				new Date());

		for (int i = 0; i < Matcher.getNumpatternResults(); i++) {
			org.opencv.core.Mat test = new org.opencv.core.Mat();
			org.opencv.core.Mat testScaled = new org.opencv.core.Mat();
			org.opencv.core.Mat testScaledRotated = new org.opencv.core.Mat();

			color.copyTo(test);
			color.copyTo(testScaled);

			Imgproc.resize(testScaled, testScaled, new org.opencv.core.Size(
					1184, 720));
			testScaled.copyTo(testScaledRotated);

			Core.flip(testScaledRotated.t(), testScaledRotated, 1);

			Matcher.debugHomography(i, test.getNativeObjAddr());
			Matcher.debugScaledHomography(i, testScaled.getNativeObjAddr());
			Matcher.debugScaledRotatedHomography(i,
					testScaledRotated.getNativeObjAddr());

			if (!Highgui.imwrite(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ File.separator
					+ "miragemobile/"
					+ currentDateTimeString + ".jpg", test)
					|| !Highgui
							.imwrite(Environment.getExternalStorageDirectory()
									.getAbsolutePath()
									+ File.separator
									+ "miragemobile/"
									+ currentDateTimeString
									+ "_scaled.jpg", testScaled)
					|| !Highgui.imwrite(Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ File.separator
							+ "miragemobile/"
							+ currentDateTimeString + "_scaledrotated.jpg",
							testScaledRotated)) {

				Log.e(TAG, "Error imwrite");
			}
			test.release();
			testScaled.release();
			testScaledRotated.release();
		}
		color.release();
		grey.release();

		return resultSize;
	}


}
