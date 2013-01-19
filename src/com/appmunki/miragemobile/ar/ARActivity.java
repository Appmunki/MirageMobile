package com.appmunki.miragemobile.ar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.entity.KeyPoint;
import com.entity.Mat;
import com.entity.TargetImage;
import com.entity.TargetImageResponse;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Model;

import flexjson.JSONDeserializer;

public abstract class ARActivity extends Activity {

	protected static final String TAG = "Aractivity";
	private static int runs = 0;
	private int mFrameWidth;
	private int mFrameHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFullscreen();
		disableScreenTurnOff();
		setOrientation();
		setupLayout();
		// Testing JSON Parse

		// Testing DataClient
		// DataClient dc = new DataClient(this);
		// dc.execute(new ArrayList<String>());

		// Testing Matcher
		loadMatcher();

	}

	static {
		System.loadLibrary("opencv_java");
		System.loadLibrary("MirageMobile");

	}

	private void loadMatcher() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				if (runs == 0) {
					runs++;
					Log.e(TAG, "Error redo");
					syncDB();
					if (TargetImage.objects(getBaseContext()).isEmpty()) {
						Log.i(TAG, "Loading Test Data");
						testJson();
					}
					Log.i(TAG, "Displaying Data");
					List<TargetImage> bs = TargetImage
							.objects(getApplicationContext()).all().toList();
					for (TargetImage b : bs) {
						Log.i(TAG, "name:" + b.getname());
						Log.i(TAG, "keys: " + b.getkeys().size());
						Log.i(TAG,
								"dess: " + b.getdess().rows + ":"
										+ b.getdess().cols + ":"
										+ b.getdess().rows * b.getdess().cols);
					}
					Matcher.fetch();
				}
			}
		}).start();
	}

	/**
	 * Avoid that the screen get's turned off by the system.
	 */
	public void disableScreenTurnOff() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	/**
	 * Maximize the application.
	 */
	public void setFullscreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	/**
	 * Set's the orientation to landscape, as this is needed by AndAR.
	 */
	public void setOrientation() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	private void setupLayout() {
		// Add a Parent
		RelativeLayout main = new RelativeLayout(this);

		// Defining the RelativeLayout layout parameters.
		// In this case I want to fill its parent
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);

		// Add preview
		Preview preview = new Preview(this);

		main.addView(preview);

		// Add relative layout as main
		setContentView(main, rlp);
	}

	private void testJson() {
		try {
			InputStream in = getAssets().open("json.txt");
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				total.append(line);
			}
			String json = total.toString().split("Response")[1].trim();
			parseJson(json);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parseJson(String json) {
		System.out.print(json);
		List<TargetImageResponse> items = new JSONDeserializer<List<TargetImageResponse>>()
				.use("values", TargetImageResponse.class)
				.use("values.dess", Mat.class).use("values.keys", Vector.class)
				.use("values.keys.values", KeyPoint.class).deserialize(json);
		for (TargetImageResponse item : items) {

			new TargetImage(item).save(this);
		}
	}

	/** Will set up the database definition **/
	private void syncDB() {
		DatabaseAdapter.setDatabaseName("miragedb");

		List<Class<? extends Model>> models = new ArrayList<Class<? extends Model>>();
		models.add(TargetImage.class);

		DatabaseAdapter adapter = DatabaseAdapter.getInstance(this);
		adapter.setModels(models);
	}

	protected synchronized void processFrame(byte[] org) {
		// Log.i(TAG, "data: " + org.length);
		byte[] data = org.clone();
		org.opencv.core.Mat mYuv = new org.opencv.core.Mat(mFrameHeight,
				mFrameWidth, CvType.CV_8UC1);
		mYuv.put(0, 0, data);

		org.opencv.core.Mat mRGB = new org.opencv.core.Mat();
		org.opencv.core.Mat mGray = new org.opencv.core.Mat();
		Imgproc.cvtColor(mYuv, mRGB, Imgproc.COLOR_YUV2RGB_NV21, 3);
		Imgproc.cvtColor(mRGB, mGray, Imgproc.COLOR_RGB2GRAY, 0);

		Matcher.match(mGray.getNativeObjAddr());

	}

	class Preview extends SurfaceView implements SurfaceHolder.Callback {
		private static final String TAG = "Preview";
		private SurfaceHolder mHolder;
		public Camera mCamera;

		private int mFrameCount = 0;

		/**
		 * Class constructor
		 */
		public Preview(Context context) {
			super(context);
			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		/**
		 * Called once the holder is ready
		 */
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// The Surface has been created, acquire the camera and tell it
			// where to
			// draw.
			mCamera = Camera.open();
			Camera.Parameters p = mCamera.getParameters();
			List<String> focusModes = p.getSupportedFocusModes();
			if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
				p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			}

			// p.setPictureFormat(PixelFormat.JPEG);
			// best quality
			// p.setJpegQuality(100);

			// max resolution
			mFrameWidth = 640;
			mFrameHeight = 480;
			p.setPreviewSize(mFrameWidth, mFrameHeight);
			// p.setPreviewSize(mFrameWidth, mFrameHeight);
			// Log.d(TAG, "Focus mode: " + p.getFocusMode());
			for (Size size : p.getSupportedPreviewSizes())
				Log.e(TAG, "Preview size choosen: " + size.width + "x"
						+ size.height);

			Log.e(TAG, "Fps choosen: "
					+ p.getSupportedPreviewFpsRange().get(0).length);
			mCamera.setParameters(p);
			mCamera.setPreviewCallback(previewCallback);

			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			} catch (IOException exception) {
				mCamera.release();
				mCamera = null;
				Log.d(TAG, "Error setting preview display: ");
				exception.printStackTrace();
			}

		}

		/**
		 * Called when the holder is destroyed
		 */
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		}

		Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				if (mFrameCount == 10) {
					processFrame(data);
					// new ImageProcess().execute(data);
					mFrameCount = 0;
				}
				mFrameCount++;
			}

		};

		/**
		 * For checking the conversion of the bitmap
		 * 
		 * @param bitmap
		 */
		void imageToFile(Bitmap finalBitmap) {
			String root = Environment.getExternalStorageDirectory().toString();
			File myDir = new File(root + "/saved_images");
			myDir.mkdirs();
			Random generator = new Random();
			int n = 10000;
			n = generator.nextInt(n);
			String fname = "Image-" + n + ".jpg";
			File file = new File(myDir, fname);
			if (file.exists())
				file.delete();
			try {
				FileOutputStream out = new FileOutputStream(file);
				finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.flush();
				out.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		/**
		 * Called when holder has changed Unused for problems with autofocus
		 */
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			/*
			 * try { mCamera.stopPreview(); } catch (Exception e) { Log.d(TAG,
			 * "Error stopping camera preview: " + e.getMessage()); }
			 */

			Camera.Parameters parameters = mCamera.getParameters(); // set

			parameters.setPreviewSize(mFrameWidth, mFrameHeight);

			// start preview with new settings
			mCamera.setParameters(parameters);
			mCamera.startPreview();

		}

		private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
			final double ASPECT_TOLERANCE = 0.05;
			double targetRatio = (double) w / h;
			if (sizes == null)
				return null;

			Size optimalSize = null;
			double minDiff = Double.MAX_VALUE;

			int targetHeight = h;

			// Try to find an size match aspect ratio and size
			for (Size size : sizes) {
				double ratio = (double) size.width / size.height;
				if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
					continue;
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}

			// Cannot find the one match the aspect ratio, ignore the
			// requirement
			if (optimalSize == null) {
				minDiff = Double.MAX_VALUE;
				for (Size size : sizes) {
					if (Math.abs(size.height - targetHeight) < minDiff) {
						optimalSize = size;
						minDiff = Math.abs(size.height - targetHeight);
					}
				}
			}
			return optimalSize;
		}

		public void release() {
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		}
	}

	class ImageProcess extends AsyncTask<byte[], Void, Void> {

		@Override
		protected Void doInBackground(byte[]... arg0) {
			// TODO Auto-generated method stub
			processFrame(arg0[0]);
			return null;
		}

	}
}
