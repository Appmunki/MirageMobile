package com.appmunki.miragemobile;

import java.io.InputStream;

import android.os.Bundle;

import com.appmunki.miragemobile.ar.ARActivity;
import com.appmunki.miragemobile.utils.Util;

public class MainARActivity extends ARActivity {
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_ar);
		
		setupARSurfaceViewLayout();
	}
	
	

	
}
