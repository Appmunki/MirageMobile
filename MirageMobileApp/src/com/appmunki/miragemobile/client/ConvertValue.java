package com.appmunki.miragemobile.client;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;

public class ConvertValue {

	public static String bitmapToBase64String(Bitmap image) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();

			return Base64.encodeToString(byteArray, false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}
}