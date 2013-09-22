package com.appmunki.miragemobile.ar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.appmunki.miragemobile.utils.Util;
import com.appmunki.miragemobile.utils.Util.CVFunction;

public class CameraViewBase extends SurfaceView implements
		SurfaceHolder.Callback, Runnable {
	private static final String TAG = "Mirage::CameraViewBase";

	private Camera mCamera;
	private SurfaceHolder mHolder;
	private int mFrameWidth;
	private int mFrameHeight;
	private byte[] mFrame;
	private volatile boolean mThreadRun;
	private byte[] mBuffer;
	private SurfaceTexture mSf;
	public static final int VIEW_MODE_RGBA = 0;
	public static final int VIEW_MODE_GRAY = 1;

	int mSize;
	int[] mRGBA;
	int[] mGRAY;

	private Bitmap mBitmap;
	private byte[] mTestData;

	private int mViewMode;

	private boolean isDebugging;

	private MarkerFoundListener m_markerfoundListener;
	private CVFunction mFunc = CVFunction.Features;

	private Bitmap testbmp;

	public CameraViewBase(Context context, boolean debugging) {
		super(context);
		loadTestData(context);
		this.isDebugging = debugging;
		mHolder = getHolder();
		mHolder.addCallback(this);
		Log.i(TAG, "Instantiated new " + this.getClass());
		mSize = 0;
		mViewMode = VIEW_MODE_RGBA;
	}

	public void loadTestData(Context context) {

		// load image
		try {
			// get input stream
			InputStream ims = context.getAssets().open("testimage.png");
			// Getting image from uri
			testbmp = BitmapFactory.decodeStream(ims);
			int width = testbmp.getWidth();
			int height = testbmp.getHeight();
			// Converting to yuv
			byte[] yuv = Util.getNV21(width, height, testbmp);

			mTestData = yuv;
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	public int getFrameWidth() {
		return mFrameWidth;
	}

	public int getFrameHeight() {
		return mFrameHeight;
	}

	@TargetApi(11)
	public void setPreview() throws IOException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mSf = new SurfaceTexture(10);
			mCamera.setPreviewTexture(mSf);
			// mCamera.setPreviewDisplay(null);
			mCamera.setPreviewDisplay(getHolder());
		} else {
			mCamera.setPreviewDisplay(getHolder());
		}
	}

	public boolean openCamera() {
		Log.i(TAG, "Opening Camera");
		mCamera = null;

		try {
			mCamera = Camera.open();
		} catch (Exception e) {
			Log.e(TAG, "Camera is not available (in use or does not exist): "
					+ e.getLocalizedMessage());
		}

		if (mCamera == null
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
				try {
					mCamera = Camera.open(camIdx);
				} catch (RuntimeException e) {
					Log.e(TAG,
							"Camera #" + camIdx + "failed to open: "
									+ e.getLocalizedMessage());
				}
			}
		}

		if (mCamera == null) {
			Log.e(TAG, "Can't open any camera");
			return false;
		}

		mCamera.setPreviewCallbackWithBuffer(new PreviewCallback() {
			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				synchronized (CameraViewBase.this) {

					// Add testing framework here
					if (isDebugging) {
						// Getting image from uri
						Log.i(TAG, "Is in Debug Mode");
						data = mTestData;
					} else {
						System.arraycopy(data, 0, mFrame, 0, data.length);
					}
					CameraViewBase.this.notify();
				}
				camera.addCallbackBuffer(mBuffer);
			}
		});

		return true;
	}

	public void releaseCamera() {
		Log.i(TAG, "Releasing Camera");
		mThreadRun = false;
		synchronized (this) {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		}
		onPreviewStopped();
	}

	public synchronized void setupCamera(int width, int height) {
		if (mCamera != null) {
			Log.i(TAG, "Setup Camera - " + width + "x" + height);
			Camera.Parameters params = mCamera.getParameters();
			List<Camera.Size> sizes = params.getSupportedPreviewSizes();
			mFrameWidth = width;
			mFrameHeight = height;

			// selecting optimal camera preview size
			{
				int minDiff = Integer.MAX_VALUE;
				for (Camera.Size size : sizes) {
					if (Math.abs(size.height - height) < minDiff) {
						mFrameWidth = size.width;
						mFrameHeight = size.height;
						minDiff = Math.abs(size.height - height);
					}
				}
			}

			params.setPreviewSize(getFrameWidth(), getFrameHeight());

			List<String> FocusModes = params.getSupportedFocusModes();
			if (FocusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
			}

			mCamera.setParameters(params);

			/* Now allocate the buffer */
			params = mCamera.getParameters();
			int size = params.getPreviewSize().width
					* params.getPreviewSize().height;
			size = size
					* ImageFormat.getBitsPerPixel(params.getPreviewFormat())
					/ 8;
			mBuffer = new byte[size];
			/* The buffer where the current frame will be copied */
			mFrame = new byte[size];
			mCamera.addCallbackBuffer(mBuffer);

			/*
			 * Notify that the preview is about to be started and deliver
			 * preview size
			 */
			onPreviewStarted(params.getPreviewSize().width,
					params.getPreviewSize().height);

			try {
				setPreview();
			} catch (IOException e) {
				Log.e(TAG,
						"mCamera.setPreviewDisplay/setPreviewTexture fails: "
								+ e);
			}

			/* Now we can start a preview */
			mCamera.startPreview();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder _holder, int format, int width,
			int height) {
		Log.i(TAG, "called surfaceChanged");
		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// start preview with new settings
		setupCamera(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "called surfaceCreated");
		(new Thread(this)).start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "called surfaceDestroyed");
	}

	/*
	 * The bitmap returned by this method shall be owned by the child and
	 * released in onPreviewStopped()
	 */
	protected Bitmap processFrame(byte[] data) {
		Log.i(TAG, "processFrame " + mViewMode);

		Arrays.fill(mRGBA, 0);
		int[] rgba = mRGBA;
		int[] gray = mGRAY;

		if (mFunc == CVFunction.Match) {
			// Testing of the matching
			Matcher.match(getFrameWidth());
		}
		if (mFunc == CVFunction.DebugMatch) {
			// Testing of the matching
			Matcher.matchDebug(getFrameWidth(), getFrameHeight(), data, rgba);
		}
		if (mFunc == CVFunction.Features) {
			// Testing of the keypoint features. Also works as the test of
			// finding the rgba and gray
			Matcher.FindFeatures(getFrameWidth(), getFrameHeight(), data, rgba,
					gray);
		}

		if (mFunc == CVFunction.Convert) {
			rgba = Util.byteToInt(data);
		}

		Log.i(TAG, "Converted");

		mBitmap.setPixels(rgba, 0/* offset */, getFrameWidth() /* stride */, 0, 0,
				getFrameWidth(), getFrameHeight());
		Log.i(TAG, "Pixel Sets");

		return mBitmap;
	}

	/**
	 * Adding Listener
	 */
	public void addMarkerFoundListener(MarkerFoundListener markerfoundListener) {
		this.m_markerfoundListener = markerfoundListener;
	}

	/**
	 * This method is called when the preview process is being started. It is
	 * called before the first frame delivered and processFrame is called It is
	 * called with the width and height parameters of the preview process. It
	 * can be used to prepare the data needed during the frame processing.
	 * 
	 * @param previewWidth
	 *            - the width of the preview frames that will be delivered via
	 *            processFrame
	 * @param previewHeight
	 *            - the height of the preview frames that will be delivered via
	 *            processFrame
	 */
	protected void onPreviewStarted(int previewWidth, int previewHeight) {
		Log.i(TAG, "called onPreviewStarted(" + previewWidth + ", "
				+ previewHeight + ")");
		/* Create a bitmap that will be used through to calculate the image to */
		mBitmap = Bitmap.createBitmap(previewWidth, previewHeight,
				Bitmap.Config.ARGB_8888);
		mRGBA = new int[previewWidth * previewHeight];
		mGRAY = new int[previewWidth * previewHeight];

	}

	/**
	 * This method is called when preview is stopped. When this method is called
	 * the preview stopped and all the processing of frames already completed.
	 * If the Bitmap object returned via processFrame is cached - it is a good
	 * time to recycle it. Any other resources used during the preview can be
	 * released.
	 */
	protected void onPreviewStopped() {
		Log.i(TAG, "called onPreviewStopped");
		if (mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}

		if (mRGBA != null) {
			mRGBA = null;
		}
	}

	public void setViewMode(int viewMode) {
		Log.i(TAG, "called setViewMode(" + viewMode + ")");
		mViewMode = viewMode;
	}

	@Override
	public void run() {
		mThreadRun = true;
		Log.i(TAG, "Started processing thread");
		while (mThreadRun) {
			Bitmap bmp = null;

			synchronized (this) {
				try {
					this.wait();
					if (!mThreadRun)
						break;

					// Take the frame and return just the overlay
					bmp = processFrame(mFrame);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// TODO change back to bmp
			if (bmp != null) {
				Log.i(TAG, "testbmp Not Null");
				this.m_markerfoundListener.found(bmp);
			} else {
				Log.i(TAG, "testbmp Null");
			}
		}
		Log.i(TAG, "Finished processing thread");
	}
}