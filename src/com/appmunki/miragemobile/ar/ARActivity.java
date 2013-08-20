package com.appmunki.miragemobile.ar;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appmunki.miragemobile.ar.entity.TargetImage;
import com.appmunki.miragemobile.client.DataClient;
import com.appmunki.miragemobile.util.Util;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Model;

import flexjson.JSONDeserializer;

public abstract class ARActivity extends Activity {

	protected static final String TAG = "Aractivity";
	private static int runs = 0;
	private int mFrameWidth;
	private int mFrameHeight;

	private int mPictureWidth;
	private int mPictureHeight;

	private Button captureButton;

	private AlertDialog alert;

	private int counter = 0;

	private String fileToDownload = "";

	ARActivity arActivity;

	private GLSurfaceView mGLView;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				// Load native library after(!) OpenCV initialization
				// System.loadLibrary("opencv_java");
				System.loadLibrary("MirageMobile");
				loadPattern();
				loadOnCreate();

			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public void loadPattern() {

		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
		path = path + "PyramidPattern.jpg";

		try {
			Util.copyFileFromAssets(getApplicationContext(), "PyramidPattern.jpg", path);
			Matcher.loadPattern(path);
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	public void loadOnCreate() {
		arActivity = this;

		// setOrientation();
		 setupLayout();

//		mGLView = new MyGLSurfaceView(this);
//
//		setContentView(mGLView);
//
//		final Preview preview = new Preview(this);
//
//		// addContentView( preview, new LayoutParams( LayoutParams.WRAP_CONTENT,
//		// LayoutParams.WRAP_CONTENT ) );
//
//		mGLView = new GLSurfaceView(this);
//		// mGLView.setEGLContextClientVersion(2);
//		mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
//		mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
//		mGLView.setZOrderOnTop(true);
//		setContentView(preview, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//		mGLView.setRenderer(new Renderer2());
//		addContentView(mGLView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//
//		Log.v("MIRAGE_NATIVE", Util.getPathPictures() + "/Mirage" + "/");
//
//		// Testing Matcher
//		loadMatcher();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFullscreen();
		disableScreenTurnOff();
		

	}

	@Override
	protected void onResume() {
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		super.onResume();
	}

	class MyGLSurfaceView extends GLSurfaceView {

		public MyGLSurfaceView(Context context) {
			super(context);

			// Create an OpenGL ES 2.0 context.
			// setEGLContextClientVersion(2);
			setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			setZOrderMediaOverlay(true);
			getHolder().setFormat(PixelFormat.TRANSLUCENT);

			// Set the Renderer for drawing on the GLSurfaceView
			setRenderer(new MyGLRenderer());

			// Render the view only when there is a change in the drawing data
			// setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		}
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
					List<TargetImage> bs = TargetImage.objects(getApplicationContext()).all().toList();
					for (TargetImage b : bs) {
						Log.i(TAG, "name:" + b.getname());
						Log.i(TAG, "keys: " + b.getkeys().size());
						Log.i(TAG, "dess: " + b.getdess().rows + ":" + b.getdess().cols + ":" + b.getdess().rows * b.getdess().cols);
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
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	/**
	 * Maximize the application.
	 */
	public void setFullscreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	/**
	 * Set's the orientation to landscape, as this is needed by AndAR.
	 */
	public void setOrientation() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	protected void setDisplayOrientation(Camera camera, int angle) {
		Method downPolymorphic;
		try {
			downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
			if (downPolymorphic != null)
				downPolymorphic.invoke(camera, new Object[] { angle });
		} catch (Exception e1) {
		}
	}

	private void setupLayout() {
		// Add a Parent
		RelativeLayout main = new RelativeLayout(this);// (RelativeLayout)
														// findViewById(R.id.Prueba);//

		// Defining the RelativeLayout layout parameters.
		// In this case I want to fill its parent
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		main.setLayoutParams(rlp);

		// Add preview
		final Preview preview = new Preview(this);

		captureButton = new Button(this);
		captureButton.setText("TAKE");
		captureButton.setBottom(1);
		captureButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				preview.mCamera.takePicture(null, null, mPicture);
			}
		});

		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		captureButton.setLayoutParams(params1);
//		main.addView(captureButton);

		//
		// mGLView = new MyGLSurfaceView(this);
		// mGLView.setZOrderMediaOverlay(true);
		// // mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		// mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
//		 main.addView(preview);
		// main.addView(mGLView);
		//
		// final Preview preview = new Preview(this);
		//
//		main.addView(captureButton);
		main.addView( preview, new LayoutParams( LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT ) );
//		main.addView( captureButton, new LayoutParams( LayoutParams.MATCH_PARENT,
//				LayoutParams.WRAP_CONTENT ) );
		
		main.addView(captureButton,params1);
		
		
		//
		//
		//

		setContentView(main);

		// Add relative layout as main
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
		// List<TargetImageResponse> items = new
		// JSONDeserializer<List<TargetImageResponse>>().use("values",
		// TargetImageResponse.class)
		// .use("values.dess", Mat.class).use("values.keys",
		// Vector.class).use("values.keys.values",
		// KeyPoint.class).deserialize(json);
		// for (TargetImageResponse item : items) {
		//
		// new TargetImage(item).save(this);
		// }
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
		org.opencv.core.Mat mYuv = new org.opencv.core.Mat(mFrameWidth, mFrameHeight, CvType.CV_8U);
		mYuv.put(0, 0, data);

		// testSaveImage(org);

		org.opencv.core.Mat mRGB = new org.opencv.core.Mat();
		org.opencv.core.Mat mGray = new org.opencv.core.Mat();
		Imgproc.cvtColor(mYuv, mRGB, Imgproc.COLOR_YUV2RGB_NV21, 3);
		Imgproc.cvtColor(mRGB, mGray, Imgproc.COLOR_RGB2GRAY, 0);

		Matcher.match(mGray.getNativeObjAddr());

	}

	public void testSaveImage(byte[] org) {
		Log.v("TEST", "" + Environment.getExternalStorageDirectory());
		File photo = new File(Environment.getExternalStorageDirectory(), "photo" + counter + ".jpg");
		counter++;

		if (photo.exists()) {
			photo.delete();
		}

		try {
			FileOutputStream fos = new FileOutputStream(photo.getPath());
			fos.write(org);
			fos.close();
		} catch (java.io.IOException e) {
			Log.e("TEST", "Exception in photoCallback", e);
		}
	}

	PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			File pictureFile = getOutputMediaFile();
			if (pictureFile == null) {
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {

			} catch (IOException e) {

			}
			// camera.startPreview();
			camera.stopPreview();

			Log.e(TAG, "PATH: " + getPath(pictureFile.getAbsolutePath()) + "  Filename: " + pictureFile.getName());

			captureButton.setVisibility(View.INVISIBLE);

			showDialog();


			Log.v("TEST","PRUEBA PRINCIPAL INICIA");
			Matcher.isPatternPresent();
			Log.v("TEST","PRUEBA PRINCIPAL TERMINA");
			
			
			//DataClient dc = new DataClient(getPath(pictureFile.getAbsolutePath()), pictureFile.getName(), arActivity);
			//dc.execute(new ArrayList<String>());

		}

	};

	private String getPath(String absolutePath) {
		String path = absolutePath.substring(0, absolutePath.lastIndexOf('/'));

		return path;
	}

	private static File getOutputMediaFile() {

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Mirage");

		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("Mirage", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

		return mediaFile;
	}

	class Preview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
		private static final String TAG = "Preview";
		private SurfaceHolder mHolder;
		public Camera mCamera;

		private int mFrameCount = 0;
		private int preview_width;
		private int preview_height;

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
			this.preview_width = 640;
			this.preview_height = 480;

			// this.preview_width = 800;
			// this.preview_height = 480;
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

			setDisplayOrientation(mCamera, 90);

			// best quality
			// p.setJpegQuality(100);

			// max resolution
			mFrameWidth = 640;
			mFrameHeight = 480;

			mPictureWidth = 1600;
			mPictureHeight = 1200;

			p.setPreviewSize(mFrameWidth, mFrameHeight);
			p.setPictureSize(mPictureWidth, mPictureHeight);
			// p.setPreviewSize(mFrameWidth, mFrameHeight);
			// Log.d(TAG, "Focus mode: " + p.getFocusMode());
			for (Size size : p.getSupportedPreviewSizes())
				Log.e(TAG, "Preview size choosen: " + size.width + "x" + size.height);

			for (Size size : p.getSupportedPictureSizes())
				Log.e(TAG, "Picture size choosen: " + size.width + "x" + size.height);

			Log.e(TAG, "Fps choosen: " + p.getSupportedPreviewFpsRange().get(0).length);
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
				if (counter == 10) {
					Mat src = new Mat(camera.getParameters().getPreviewSize().height, camera.getParameters().getPreviewSize().width, CvType.CV_8U,
							new Scalar(255));
					src.put(0, 0, data);
					Mat dst = new Mat(camera.getParameters().getPreviewSize().height, camera.getParameters().getPreviewSize().width, CvType.CV_8U,
							new Scalar(255));

					Core.transpose(src, dst);
					Core.flip(dst, dst, 1);
					// Log.v("MIRAGE_NATIVE","CAMBIAR IMAGEN");
					Matcher.loadImage(dst.getNativeObjAddr());
					counter = 0;
				}
				counter++;
			}

		};

		/**
		 * For checking the conversion of the bitmap
		 * 
		 * @param bitmap
		 */
		void imageToFile(Bitmap finalBitmap) {
			String root = Environment.getExternalStorageDirectory().toString();
			File myDir = new File(root);
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
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			try {
				initCamera(mHolder);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			// Now that the size is known, set up the camera parameters and
			// begin
			// the preview.

			Camera.Parameters parameters = mCamera.getParameters();
			List<Camera.Size> pvsizes = mCamera.getParameters().getSupportedPreviewSizes();
			int best_width = 1000000;
			int best_height = 1000000;
			int bdist = 100000;
			for (Size x : pvsizes) {
				if (Math.abs(x.width - preview_width) < bdist) {
					bdist = Math.abs(x.width - preview_width);
					best_width = x.width;
					best_height = x.height;
				}
			}
			preview_width = best_width;
			preview_height = best_height;

			Log.d("NativePreviewer", "Determined compatible preview size is: (" + preview_width + "," + preview_height + ")");

			Log.d("NativePreviewer", "Supported params: " + mCamera.getParameters().flatten());

			// this is available in 8+
			// parameters.setExposureCompensation(0);
			if (parameters.getSupportedWhiteBalance().contains("auto")) {
				parameters.setWhiteBalance("auto");
			}
			// if (parameters.getSupportedAntibanding().contains(
			// Camera.Parameters.ANTIBANDING_OFF)) {
			// parameters.setAntibanding(Camera.Parameters.ANTIBANDING_OFF);
			// }

			List<String> fmodes = mCamera.getParameters().getSupportedFocusModes();
			// for(String x: fmodes){

			// }

			if (parameters.get("meter-mode") != null)
				parameters.set("meter-mode", "meter-average");
			int idx = fmodes.indexOf(Camera.Parameters.FOCUS_MODE_INFINITY);
			if (idx != -1) {
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
			} else if (fmodes.indexOf(Camera.Parameters.FOCUS_MODE_FIXED) != -1) {
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
			}

			// if (fmodes.indexOf(Camera.Parameters.FOCUS_MODE_AUTO) != -1) {
			// hasAutoFocus = true;
			// }

			List<String> scenemodes = mCamera.getParameters().getSupportedSceneModes();
			if (scenemodes != null)
				if (scenemodes.indexOf(Camera.Parameters.SCENE_MODE_ACTION) != -1) {
					parameters.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
					Log.d("NativePreviewer", "set scenemode to action");
				}

			parameters.setPreviewSize(preview_width, preview_height);

			mCamera.setParameters(parameters);

			PixelFormat pixelinfo = new PixelFormat();
			int pixelformat = mCamera.getParameters().getPreviewFormat();
			PixelFormat.getPixelFormatInfo(pixelformat, pixelinfo);

			Size preview_size = mCamera.getParameters().getPreviewSize();
			preview_width = preview_size.width;
			preview_height = preview_size.height;
			int bufSize = preview_width * preview_height * pixelinfo.bitsPerPixel;

			mCamera.startPreview();

		}

		private void initCamera(SurfaceHolder holder) throws InterruptedException {
			if (mCamera == null) {
				// The Surface has been created, acquire the camera and tell it
				// where
				// to draw.
				int i = 0;
				while (i++ < 5) {
					try {
						mCamera = Camera.open();
						break;
					} catch (RuntimeException e) {
						Thread.sleep(200);
					}
				}
				try {
					mCamera.setPreviewDisplay(holder);
				} catch (IOException exception) {
					mCamera.release();
					mCamera = null;

				} catch (RuntimeException e) {
					Log.e("camera", "stacktrace", e);
				}
			}
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

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * Show alert dialog to wait the response
	 */
	public void showDialog() {
		Builder builder = new Builder(this);

		builder.setTitle("Mirage");

		builder.setMessage("Analyzing");
		builder.setPositiveButton("Show", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!fileToDownload.equals("")) {
					File image = new File(Util.getPathPictures() + "/Mirage/" + fileToDownload);
					Uri uri = Uri.fromFile(image);
					Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
					String mime = "*/*";
					MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
					if (mimeTypeMap.hasExtension(mimeTypeMap.getFileExtensionFromUrl(uri.toString())))
						mime = mimeTypeMap.getMimeTypeFromExtension(mimeTypeMap.getFileExtensionFromUrl(uri.toString()));
					intent.setDataAndType(uri, mime);
					startActivity(intent);
				} else {
					setMessage("Please Wait");

				}
			}
		});

		alert = builder.show();

		TextView messageText = (TextView) alert.findViewById(android.R.id.message);
		messageText.setGravity(Gravity.CENTER);

	}

	/**
	 * Change the message on the alertdialog
	 * 
	 * @param message
	 */
	public void setMessage(String message) {
		
		
		fileToDownload = message.replaceAll(" ", "%20");
		if (alert != null) {
			alert.setMessage(message);
		}
		if (!alert.isShowing()) {
			alert.show();
		}

	}

}
