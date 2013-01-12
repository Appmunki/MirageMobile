package com.appmunki.miragemobile;

import android.content.Context;
import android.util.Log;
import com.appmunki.miragemobile.ar.entity.TargetImage;
import com.orm.androrm.QuerySet;
import flexjson.JSONSerializer;

public class Util {

	private Context context;
	private static Util instance;

	private Util(Context context) {
		this.context = context;
	}

	public void getAllTargets() {

		QuerySet<TargetImage> books = TargetImage.objects(context,
				TargetImage.class).all();
		int i = 0;
		for (TargetImage tuple : books) {
			Log.v("DataClient", "Tupla "+i+" ------ "+tuple.getId());
			Log.v("DataClient", "_ID: "+tuple._ID);
			Log.v("DataClient", "_Name: "+tuple._name);
			Log.v("DataClient", "_Author: "+tuple._author);
			Log.v("DataClient", "_Description: "+tuple._description);
			Log.v("DataClient", "_Heigth: "+tuple._height);
			Log.v("DataClient", "_Width: "+tuple._width);
			Log.v("DataClient", "_Image: "+tuple._image);
			i++;
		}
	}

	public static Util getInstance(Context context) {
		if (instance == null) {
			instance = new Util(context);
		}
		return instance;
	}

}

