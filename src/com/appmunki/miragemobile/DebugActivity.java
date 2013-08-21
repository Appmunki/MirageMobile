package com.appmunki.miragemobile;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import com.appmunki.miragemobile.ar.Matcher;
import com.appmunki.miragemobile.util.SystemUiHider;
import com.appmunki.miragemobile.util.Util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class DebugActivity extends Activity {

	Button buttonRun, buttonImageToMatch;
	TextView textViewImageToMatch;
	ImageView image;

	final static int PATTERN = 0;
	final static int IMAGE_TO_MATCH = 1;

	final static boolean DEBUG = true;

	final static String TAG = "DEBUG";

	String pathImageToMatch;

	boolean opencvLoad = false;

	DebugActivity debugActivity;
	ProgressDialog progressDialog;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_debug);
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("MATCH");
		progressDialog.setMessage("Looking for a possible match");
		

		image = (ImageView) findViewById(R.id.imageView1);

		buttonRun = (Button) findViewById(R.id.buttonRun);
		buttonImageToMatch = (Button) findViewById(R.id.buttonImageToMatch);

		textViewImageToMatch = (TextView) findViewById(R.id.textViewImageToMatch);

		buttonImageToMatch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				loadPhoto(IMAGE_TO_MATCH);
			}
		});

		buttonRun.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (opencvLoad) {
					progressDialog.show();
					new AsyncMatch(debugActivity,pathImageToMatch).execute();

				}
			}
		});

		
		debugActivity = this;

		if (!DEBUG) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}
	}

	
	
	public void callback(Bitmap bitmap){
		progressDialog.dismiss();
		if(bitmap!=null){
		image.setImageBitmap(bitmap);
		}else{
			Toast.makeText(this, "ERROR the image is null", Toast.LENGTH_SHORT).show();
		}
	}

	public void loadPattern() {

		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
		path = path + "PyramidPattern.jpg";
		try {
			Util.copyFileFromAssets(getApplicationContext(), "PyramidPattern.jpg", path);
			Matcher.loadPattern(path);
			Log.v(TAG, "PATTERN LOADED");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void loadPhoto(int request) {
		Intent intentPhotoPicker = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intentPhotoPicker.setType("image/*");
		startActivityForResult(intentPhotoPicker, request);
	}

	@Override
	protected void onResume() {
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);

		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Uri _uri = data.getData();
			Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
			cursor.moveToFirst();
			final String imageFilePath = cursor.getString(0);
			cursor.close();

			switch (requestCode) {
			case PATTERN:
				break;
			case IMAGE_TO_MATCH:
				pathImageToMatch = imageFilePath;
				String imageToMatchName = pathImageToMatch.substring(pathImageToMatch.lastIndexOf("/") + 1);
				textViewImageToMatch.setText(imageToMatchName);
				break;
			default:
				break;
			}
		}
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				System.loadLibrary("MirageMobile");
				Log.v(TAG, "LIBRARY LOADED");
				opencvLoad = true;
				loadPattern();
			}
				break;
			case LoaderCallbackInterface.INIT_FAILED: {
				Toast.makeText(debugActivity, "INIT FAIL", Toast.LENGTH_LONG).show();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

}
