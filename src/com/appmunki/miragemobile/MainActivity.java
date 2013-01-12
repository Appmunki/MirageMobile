package com.appmunki.miragemobile;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.Menu;

import com.appmunki.miragemobile.ar.ARActivity;
import com.appmunki.miragemobile.ar.entity.TargetImage;
import com.appmunki.miragemobile.client.DataClient;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Model;


public class MainActivity extends ARActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DataClient dc = new DataClient(this);
		dc.execute(new ArrayList<String>());
		
		createDatabase();
		
		
	}
	
	
	private void createDatabase(){
		DatabaseAdapter.setDatabaseName("miragedb");
	    List<Class<? extends Model>> models = new ArrayList<Class<? extends Model>>();
	    models.add(TargetImage.class);
	     
	    DatabaseAdapter adapter = DatabaseAdapter.getInstance(getApplicationContext());
	    adapter.setModels(models);
	   
		
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
