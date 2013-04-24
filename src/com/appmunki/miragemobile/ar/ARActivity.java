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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.appmunki.miragemobile.R;
import com.entity.KeyPoint;
import com.entity.Mat;
import com.entity.TargetImage;
import com.entity.TargetImageResponse;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Model;

import flexjson.JSONDeserializer;

public abstract class ARActivity extends Activity {

	protected static final String TAG = "Aractivity";
	static int countOnCreate = 0;

	private CameraViewBase mCameraViewBase;
	private RelativeLayout splashmain;
	private CameraOverlayView mCameraOverlayView;

	// private Preview mPreview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setFullscreen();
		disableScreenTurnOff();
		setOrientation();
		super.onCreate(savedInstanceState);
		if (countOnCreate == 1) {
			// Check if test data is loaded or not
			checkTestData();

		}
		countOnCreate++;

	}

	private void checkTestData() {
		Log.d(TAG, "checkTestData");
		// Load the splashscreen

		loadSplashScreen();
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				// Create the db
				syncDB();

				// Check the data.txt
				if (!(new File(getApplicationContext().getFilesDir().toString()
						+ "/Data.txt").exists())) {
					Log.d(TAG, "Data.txt doesn't exist");
					if (TargetImage.objects(getBaseContext()).isEmpty()) {
						Log.i(TAG, "Loading Test Data");
						testJson();
					}
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

					// Turns the list of targetimages to a data.txt file
					Matcher.fetch(getApplicationContext());
				} else {
					Log.d(TAG, "Data.txt exist");
				}
				Matcher.load();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				// Remove splash
				splashmain.setVisibility(RelativeLayout.GONE);
				// Set up Camera
				setupCameraLayout();

				// Start the camera
				mCameraViewBase.openCamera();
			};

		}.execute();

	}

	private void loadSplashScreen() {
		splashmain = new RelativeLayout(this);
		LinearLayout content = new LinearLayout(this);
		content.setOrientation(LinearLayout.VERTICAL);

		// Adding Logo
		ImageView logoLayout = new ImageView(this);
		logoLayout.setImageResource(R.drawable.miragelogo);
		android.widget.LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		content.addView(logoLayout, lp1);

		// Adding Text
		TextView loadingText = new TextView(this);

		loadingText.setText("Loading AR ...");
		loadingText.setTypeface(null, Typeface.BOLD);
		loadingText.setGravity(Gravity.CENTER);
		lp1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		content.addView(loadingText, lp1);

		// Adding progress bar
		ProgressBar progressBar = new ProgressBar(this, null,
				android.R.attr.progressBarStyleLarge);

		content.addView(progressBar);

		LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT, content.getId());
		splashmain.addView(content, lp);

		setContentView(splashmain);
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

	@Override
	protected void onPause() {
		Log.i(TAG, "called onPause");
		super.onPause();
		if (mCameraViewBase != null)
			mCameraViewBase.releaseCamera();
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "called onResume");
		super.onResume();
		if (mCameraViewBase != null && !mCameraViewBase.openCamera()) {
			AlertDialog ad = new AlertDialog.Builder(this).create();
			ad.setCancelable(false); // This blocks the 'BACK' button
			ad.setMessage("Fatal error: can't open camera!");
			ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}
					});
			ad.show();
		}
	}

	private void setupCameraLayout() {
		// Add a Parent
		RelativeLayout main = new RelativeLayout(this);

		// Defining the RelativeLayout layout parameters.
		// In this case I want to fill its parent
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);

		// Add preview
		mCameraViewBase = new CameraViewBase(this);
		main.addView(mCameraViewBase);
		mCameraViewBase.setVisibility(SurfaceView.VISIBLE);

		// Add canvas overlay
		mCameraOverlayView = new CameraOverlayView(this);
		mCameraOverlayView.setVisibility(View.VISIBLE);
		mCameraViewBase.addMarkerFoundListener(mCameraOverlayView);

		main.addView(mCameraOverlayView);
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

	static {
		System.loadLibrary("opencv_java");
		System.loadLibrary("MirageMobile");
	}

}
