package com.appmunki.miragemobile;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.appmunki.miragemobile.ar.Matcher;

public class LoadingScreenActivity extends Activity {

	protected static final String TAG = "LoadingScreen";
	static {
		System.loadLibrary("opencv_java");
		System.loadLibrary("MirageMobile");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		loadView();
		loadMatcher();

	}

	private void loadMatcher() {

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				if (!(new File(getFilesDir().toString() + "/Data.txt").exists())) {
					Log.i(TAG, "Data.txt doesn't exist");
				} else {
					Log.i(TAG, "Data.txt exist");
				}
				super.onPreExecute();
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				Matcher.load(true);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				startActivity(new Intent(getApplicationContext(),
						MainActivity.class));
				finish();
				super.onPostExecute(result);
			}
		}.execute();
	}

	private void loadView() {
		RelativeLayout main = new RelativeLayout(this);
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
		main.addView(content, lp);

		setContentView(main);
	}
}
