package com.appmunki.miragemobile;

import org.opencv.android.OpenCVLoader;

import android.os.Bundle;
import android.view.Menu;

import com.appmunki.miragemobile.ar.ARActivity;

public class MainActivity extends ARActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	static {
		if (!OpenCVLoader.initDebug()) {
			System.loadLibrary("MirageMobile");
		} else {
			// Toast.makeText(this, "Error Opencv Broken", Toast.LENGTH_SHORT)
			// .show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onPause() {

		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

}
