package com.appmunki.miragemobile.ar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.appmunki.miragemobile.utils.Util;

/**
 * Purpose of this activity is to setup AR for a inheriting activity. Loading in
 * Markers Initializing Camera and overlay
 * 
 * @author radzell
 * 
 */
public abstract class ARActivity extends Activity {
	static {
		System.loadLibrary("opencv_java");
		System.loadLibrary("MirageMobile");
	}

	private final String TAG = this.getClass().getSimpleName();
	// private CameraViewBase mCameraViewBase;
	@SuppressWarnings("unused")
	private RelativeLayout main;

	private int mFrameWidth;
	private int mFrameHeight;

	private int mPictureWidth;
	private int mPictureHeight;

	public ReentrantLock mPreviewBufferLock = new ReentrantLock();
	public ImageView mImageView;
	private CameraPreview mPreview;

	public static boolean debug = false;
	public static boolean debugcamera = true;
	private Camera mCamera;
	private int mCameraId;
	private int mRotation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "ARActivity Started");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		super.onCreate(savedInstanceState);
		setupCameraViewLayout();
		Log.e(TAG, "Thread Id: "+ android.os.Process.myTid());

	}

	/**
	 * Debugging version of the add Pattern code
	 * 
	 * @param name
	 *            the name of the pattern that will be saved
	 * @param in
	 *            the inputstream containing the image to be saved
	 */
	public void addPattern(String name, InputStream in) {
		Bitmap bitmap = Util.decodeSampledBitmapFromStream(in, 640, 480);
		addPattern(name, bitmap);
		bitmap.recycle();
	}

	/**
	 * Debugging version of the add Pattern code
	 * 
	 * @param name
	 *            the name of the pattern that will be saved
	 * @param bitmap
	 *            the bitmap containing the image to be saved
	 */
	private void addPattern(String name, Bitmap bitmap) {
		Log.i(TAG, bitmap.getWidth() + "x" + bitmap.getHeight());
		Mat color = new Mat(bitmap.getWidth(), bitmap.getHeight(),
				CvType.CV_8UC4);
		Mat grey = new Mat(bitmap.getWidth(), bitmap.getHeight(),
				CvType.CV_8UC4);

		Utils.bitmapToMat(bitmap, color);
		Imgproc.cvtColor(color, grey, Imgproc.COLOR_RGB2GRAY);

		Matcher.addPattern(grey.getNativeObjAddr());
		if (debug) {
			File dir = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + File.separator + "miragemobile/");
			dir.mkdirs();
			Highgui.imwrite(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ File.separator
					+ "miragemobile/"
					+ name, grey);
		}
		color.release();
		grey.release();

	}

	/**
	 * Match debug call used to help debug the homographies in a image
	 * 
	 * @param mat
	 *            of the image being matched
	 * @return number of patterns found in the scene
	 */
	public int matchDebug(Mat color, Mat grey) {
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

	/**
	 * Match debug call used to help debug the homographies in a image
	 * 
	 * @param bitmap
	 *            a bitmap of a image being matched against
	 * @return the number of matched images
	 */
	public int matchDebug(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Log.i("Match", "Scene size " + width + "x" + height);

		// Result of the amount of found markers
		Mat color = new Mat(bitmap.getWidth(), bitmap.getHeight(),
				CvType.CV_8UC4);
		Mat grey = new Mat(bitmap.getWidth(), bitmap.getHeight(),
				CvType.CV_8UC4);
		Utils.bitmapToMat(bitmap, color);
		Imgproc.cvtColor(color, grey, Imgproc.COLOR_RGB2GRAY);
		return matchDebug(color, grey);
	}

	/**
	 * Sets up cameraview in layout
	 */
	public void setupCameraViewLayout() {
		
		// acquire camera and initialize params
		if (!safeCameraOpen()) {
			Log.e(TAG, "Failed to acquire camera in setupCameraViewLayout");
			System.exit(0);
		} else {
			Log.e(TAG, "Acquired camera in setupCameraViewLayout");
		}

		mPreview = new CameraPreview(this);
		mPreview.setCamera(mCameraId, mCamera,mRotation);
		// final Preview preview = new Preview(this);
		setContentView(mPreview, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	/**
	 * Sets up the ARSurfaceView which does the matching and displaying
	 */
	public void setupARSurfaceViewLayout() {

		GLSurfaceView mGLView = new GLSurfaceView(this);
		mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		mGLView.setZOrderOnTop(true);
		mGLView.setRenderer(new ARRender());
		mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		addContentView(mGLView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	/**
	 * Method for junit to get the number of pattern results
	 * 
	 * @return number of pattern results
	 */
	public int getNumPatternResults() {
		return Matcher.getNumpatternResults();
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "called onResume");
		super.onResume();
		if (safeCameraOpen()) {
			mPreview.setCamera(mCameraId,mCamera,mRotation);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "called onPause");
		releaseCameraAndPreview();
	}

	public void onStop() {
		super.onStop();
		Log.i(TAG, "Stopping");
		releaseCameraAndPreview();
	}

	private void releaseCameraAndPreview() {
		//if(!Matcher.debug) return;
		Log.i(TAG, "releaseCameraAndPreview");
		
		mPreview.stopPreviewAndFreeCamera();
		
	}

	/** safely access an instance of the camera */
	private boolean safeCameraOpen() {
		//if(Matcher.debug) return false;
		
		boolean qOpened = false;

		try {
			if (mCamera != null)
				return true;
			if(mCameraId==-1){
				for(int i=0;i<Camera.getNumberOfCameras();i++){
					android.hardware.Camera.CameraInfo info =
				             new android.hardware.Camera.CameraInfo();
				     android.hardware.Camera.getCameraInfo(i, info);
				     if(info.facing==Camera.CameraInfo.CAMERA_FACING_BACK){
				    	 mCameraId= i;
				    	 break;
				     }
				}
				System.exit(0);
			}
			mRotation= this.getWindowManager().getDefaultDisplay()
            .getRotation();
			mCamera = Camera.open(mCameraId);
			
			qOpened = (mCamera != null);
		} catch (Exception e) {
			Log.e(TAG, "failed to open Camera");
			e.printStackTrace();
		}

		return qOpened;
	}
	

	

}
