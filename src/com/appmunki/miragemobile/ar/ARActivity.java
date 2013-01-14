package com.appmunki.miragemobile.ar;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.entity.KeyPoint;
import com.entity.Mat;
import com.entity.TargetImage;
import com.entity.TargetImageResponse;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public abstract class ARActivity extends Activity {

	protected static final String TAG = "Aractivity";
	private static int runs = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFullscreen();
		disableScreenTurnOff();
		setOrientation();
		setupLayout();
		// Testing JSON Parse

		// Testing DataClient
		// DataClient dc = new DataClient(this);
		// dc.execute(new ArrayList<String>());

		// Testing Matcher
		loadMatcher();

	}

	static {
		System.loadLibrary("opencv_java");
		System.loadLibrary("MirageMobile");

	}

	private void loadMatcher() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				if (runs == 0) {
					runs++;
					Log.e(TAG, "Error redo");
					syncDB();
					// testJson();
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
					Matcher.fetch();
				}
			}
		}).start();
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

	private void setupLayout() {
		// Add a Parent
		RelativeLayout main = new RelativeLayout(this);

		// Defining the RelativeLayout layout parameters.
		// In this case I want to fill its parent
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);

		// Add preview
		Preview preview = new Preview(this);

		main.addView(preview);

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
		JSONSerializer serializer = new JSONSerializer().prettyPrint(true);

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
}
