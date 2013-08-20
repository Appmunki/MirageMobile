package com.appmunki.miragemobile;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import com.appmunki.miragemobile.ar.Matcher;
import com.appmunki.miragemobile.util.SystemUiHider;
import com.appmunki.miragemobile.util.Util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class DebugActivity extends Activity {

	Button buttonRun, buttonImagePattern, buttonImageToMatch;
	TextView textViewPattern, textViewImageToMatch;
	ImageView image;

	final static int PATTERN = 0;
	final static int IMAGE_TO_MATCH = 1;

	final static String TAG = "DEBUG";

	String pathPattern, pathImageToMatch;

	boolean opencvLoad = false;

	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_debug);

		image = (ImageView) findViewById(R.id.imageView1);

		buttonRun = (Button) findViewById(R.id.buttonRun);
		buttonImagePattern = (Button) findViewById(R.id.buttonPattern);
		buttonImageToMatch = (Button) findViewById(R.id.buttonImageToMatch);

		textViewImageToMatch = (TextView) findViewById(R.id.textViewImageToMatch);
		textViewPattern = (TextView) findViewById(R.id.textViewPattern);

		buttonImagePattern.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				loadPhoto(PATTERN);
			}
		});

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
					if(pathPattern!=null&&!pathPattern.equals("")&&pathImageToMatch!=null&&!pathImageToMatch.equals("")){
					Matcher.runDebug(pathPattern, pathImageToMatch);
					Bitmap bmp = BitmapFactory.decodeFile("/mnt/sdcard/outputDebug.jpg");
					
					if(bmp.getHeight()>2048||bmp.getWidth()>2048){
						float scale = 1;
						if(bmp.getHeight()>=bmp.getWidth()){
							scale = 2000/(float)bmp.getHeight();
						}else if(bmp.getWidth()>bmp.getHeight()){
							scale = 2000/(float)bmp.getWidth();
						}
						
						float newWidth = bmp.getWidth()*scale;
						float newHeight = bmp.getHeight()*scale;
						 Log.v(TAG,"NEW WIDTH "+newWidth);
						    Log.v(TAG,"NEW HEIGHT "+newHeight);
						
						
						bmp = getResizedBitmap(bmp, (int) newHeight,  (int)newWidth);
					}
					image.setImageBitmap(bmp);
					}else{
						Toast.makeText(context, "PATH TO IMAGES NULL", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		context = this;
	}
	
	
	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    float scaleWidth = ((float) newWidth) / width;
	    float scaleHeight = ((float) newHeight) / height;
	    
	    Log.v(TAG,"NEW WIDTH "+scaleWidth);
	    Log.v(TAG,"NEW HEIGHT "+scaleHeight);
	    
	    // CREATE A MATRIX FOR THE MANIPULATION
	    Matrix matrix = new Matrix();
	    // RESIZE THE BIT MAP
	    matrix.postScale(scaleWidth, scaleHeight);

	    // "RECREATE" THE NEW BITMAP
	    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	    return resizedBitmap;
	}


	
	
	
	public void loadPattern() {

		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
		path = path + "PyramidPattern.jpg";

		try {
			Util.copyFileFromAssets(getApplicationContext(), "PyramidPattern.jpg", path);
			Matcher.loadPattern(path);
			Log.v(TAG,"PATTERN LOADED");
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
				pathPattern = imageFilePath;
				String imageName = pathPattern.substring(pathPattern.lastIndexOf("/") + 1);
				textViewPattern.setText(imageName);
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
				Log.v(TAG,"LIBRARY LOADED");
				opencvLoad = true;
				loadPattern();
			}
				break;
			case LoaderCallbackInterface.INIT_FAILED: {
				Toast.makeText(context, "INIT FAIL", Toast.LENGTH_LONG).show();
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
