package com.appmunki.miragemobile;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.appmunki.miragemobile.ar.ARActivity;
import com.appmunki.miragemobile.ar.ARLib;
import com.appmunki.miragemobile.ar.Matcher;
import com.appmunki.miragemobile.utils.Util;

public class TestARActivity extends ARActivity {
	

	private enum CVFunction {
		Features, Match, DebugMatch
	}

	final static int PATTERN = 0;
	final static int GET_IMAGE = 1;
	final static int GET_IMAGE_PATTERN = 2;
	final static int GET_PATTERN = 6384; // onActivityResult request code

	private Bitmap mPhotoBeingChecked;
	private CVFunction mFunc = CVFunction.DebugMatch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_ar);
		initLayout();
	}

	private void initLayout() {
		mPhotoBeingChecked = BitmapFactory.decodeResource(getResources(),
				R.drawable.testimage);
		ImageView im = (ImageView) findViewById(R.id.testImageView);
		im.setImageBitmap(mPhotoBeingChecked);
	}

	public void loadImageOnClick(View v) {
		Toast.makeText(getApplicationContext(), "Load Image",
				Toast.LENGTH_SHORT).show();
		loadPhoto(GET_IMAGE);
	}

	public void loadPatternOnClick(View v) {
		Toast.makeText(getApplicationContext(), "Load Pattern",
				Toast.LENGTH_SHORT).show();
		loadPhoto(GET_IMAGE_PATTERN);
		

	}

	public void runMatchOnClick(View v) {
		Toast.makeText(getApplicationContext(), "Run Match", Toast.LENGTH_SHORT)
				.show();
		runMatch();
	}

	private void loadPhoto(int request) {
		Intent intentPhotoPicker = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intentPhotoPicker.setType("image/*");
		startActivityForResult(intentPhotoPicker, request);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == GET_PATTERN
				&& null != data) {
			String FilePath = data.getData().getPath();
			Toast.makeText(getApplicationContext(), FilePath, Toast.LENGTH_LONG)
					.show();

		}
		if (resultCode == RESULT_OK
				&& (requestCode == GET_IMAGE || requestCode == GET_IMAGE_PATTERN)
				&& null != data) {

			Uri _uri = data.getData();
			Cursor cursor = getContentResolver()
					.query(_uri,
							new String[] { android.provider.MediaStore.Images.ImageColumns.DATA },
							null, null, null);
			cursor.moveToFirst();
			String imageFilePath = cursor.getString(0);
			cursor.close();

			String imageName = imageFilePath.substring(imageFilePath
					.lastIndexOf("/") + 1);
			Toast.makeText(getApplicationContext(), imageFilePath,
					Toast.LENGTH_LONG).show();
			if (requestCode == GET_IMAGE)
				updatePhotoUI(_uri, imageName);
			if (requestCode == GET_IMAGE_PATTERN) {
				try {
					// Getting image from uri
					Bitmap bmp = BitmapFactory
							.decodeStream(getContentResolver().openInputStream(
									_uri));
					int width = bmp.getWidth();
					int height = bmp.getHeight();
					// Converting to yuv
					byte[] yuv = Util.getNV21(width, height, bmp);

					// Adding pattern to marker
					Matcher.addPattern(width, height, yuv);

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
	}

	private void updatePhotoUI(Uri _uri, String imageFilePath) {
		ImageView im = (ImageView) findViewById(R.id.testImageView);
		im.setImageURI(_uri);
	}

	private void runMatch() {

		// Get bitmap from imageview
		ImageView im = (ImageView) findViewById(R.id.testImageView);
		ImageView imResult = (ImageView) findViewById(R.id.testResultImageView);

		Bitmap bitmap = ((BitmapDrawable) im.getDrawable()).getBitmap();
		
		ARLib.testMatConvert(bitmap);
		// Set variables
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		byte[] yuv = Util.getNV21(width, height, bitmap);
		int[] rgba = new int[width * height];
		int[] gray = new int[width * height];

		// Run match code
		// Matcher.matchDebug(width, height, yuv, rgba);
		if (mFunc == CVFunction.Features) {
			Toast.makeText(getApplicationContext(), "FindFeatues",
					Toast.LENGTH_SHORT).show();
			Matcher.FindFeatures(width, height, yuv, rgba, gray);
		} else if (mFunc == CVFunction.DebugMatch) {
			Toast.makeText(getApplicationContext(), "DebugMatch",
					Toast.LENGTH_SHORT).show();
			int[] result = Matcher.matchDebug(width, height, yuv);
			Log.v("DEBUG",result.length+"");
		}
		// Set result bitmap to imageview
//		bitmap.setPixels(rgba, 0/* offset */, width/* stride */, 0, 0, width,
//				height);

		Bitmap bm = Bitmap.createBitmap(height + height / 2, width,
				Bitmap.Config.ARGB_8888);
//		bm.copyPixelsFromBuffer(ByteBuffer.wrap(yuv));

//		imResult.setImageBitmap(bm);
	}
}
