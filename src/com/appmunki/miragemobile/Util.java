package com.appmunki.miragemobile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.util.Log;
import com.appmunki.miragemobile.ar.entity.TargetImage;
import com.orm.androrm.QuerySet;

public class Util {

	private static Context context;
	private static Util instance;

	private Util(Context context) {
		this.context = context;
	}

	public static void getAllTargets() {

		QuerySet<TargetImage> books = TargetImage.objects(context,
				TargetImage.class).all();
		int i = 0;
		for (TargetImage tuple : books) {
			Log.v("DataClient", "Tupla " + i + " ------ " + tuple.getId());
			Log.v("DataClient", "_ID: " + tuple._ID);
			Log.v("DataClient", "_Name: " + tuple._name);
			Log.v("DataClient", "_Author: " + tuple._author);
			Log.v("DataClient", "_Description: " + tuple._description);
			Log.v("DataClient", "_Heigth: " + tuple._height);
			Log.v("DataClient", "_Width: " + tuple._width);
			Log.v("DataClient", "_Dess length: " + tuple._dess.get().length);
			Log.v("DataClient", "_Keys length: " + tuple._keys.get().length);
			Log.v("DataClient", "_Image: " + tuple._image);
			i++;
		}
	}

	public static byte[] bytefromJSONArray(JSONArray array) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(baos);
			for (int i = 0; i < array.length(); i++) {
				Integer element = array.getInt(i);
				out.write(element);
			}
			return baos.toByteArray();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Util getInstance(Context context) {
		if (instance == null) {
			instance = new Util(context);
		}
		return instance;
	}

}
