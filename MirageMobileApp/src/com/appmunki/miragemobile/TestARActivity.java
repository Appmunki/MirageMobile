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
