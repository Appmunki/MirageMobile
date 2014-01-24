package com.appmunki.miragemobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.appmunki.miragemobile.ar.ARActivity;

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
		//initLayout();
		setupGLSurfaceViewLayout();
	}

	private void initLayout() {
		mPhotoBeingChecked = BitmapFactory.decodeResource(getResources(),
				R.drawable.testimage);
		//ImageView im = (ImageView) findViewById(R.id.testImageView);
		//im.setImageBitmap(mPhotoBeingChecked);
	}


	

	

	

	
}
