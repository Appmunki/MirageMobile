package com.appmunki.miragemobile;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.Menu;

import com.appmunki.miragemobile.ar.ARActivity;
import com.appmunki.miragemobile.client.DataClient;

public class MainActivity extends ARActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DataClient dc = new DataClient(this);
		dc.execute(new ArrayList<String>());
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
