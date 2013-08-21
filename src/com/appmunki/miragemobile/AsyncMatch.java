package com.appmunki.miragemobile;

import com.appmunki.miragemobile.ar.Matcher;
import com.appmunki.miragemobile.util.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class AsyncMatch extends AsyncTask {

	DebugActivity debugActivity;
	String pathImageToMatch;

	public AsyncMatch(DebugActivity debugActivity, String pathImageToMatch) {
		this.debugActivity = debugActivity;
		this.pathImageToMatch = pathImageToMatch;
	}

	@Override
	protected Object doInBackground(Object... params) {
		String pathPattern = "";
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MirageDebug/";
		String[] listPatterns = Util.listPatternFiles(debugActivity);
		for (String pattern : listPatterns){
			pathPattern = path + pattern;

			if (pathPattern != null && !pathPattern.equals("") && pathImageToMatch != null && !pathImageToMatch.equals("")) {
				boolean isPatternPresent = Matcher.runDebug(pathPattern, pathImageToMatch);
				if (isPatternPresent) {
					Log.v("PATTERN", "PATTERN PRESENT " + isPatternPresent);

					Bitmap bmp = BitmapFactory.decodeFile("/mnt/sdcard/outputDebug.jpg");

					if (bmp.getHeight() > 2048 || bmp.getWidth() > 2048) {
						float scale = 1;
						if (bmp.getHeight() >= bmp.getWidth()) {
							scale = 2000 / (float) bmp.getHeight();
						} else if (bmp.getWidth() > bmp.getHeight()) {
							scale = 2000 / (float) bmp.getWidth();
						}

						float newWidth = bmp.getWidth() * scale;
						float newHeight = bmp.getHeight() * scale;

						bmp = getResizedBitmap(bmp, (int) newHeight, (int) newWidth);

					}
					return bmp;
				}
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(Object result) {
		debugActivity.callback((Bitmap) result);
	}

	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		return resizedBitmap;
	}

}
