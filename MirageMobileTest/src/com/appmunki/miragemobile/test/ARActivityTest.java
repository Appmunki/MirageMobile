package com.appmunki.miragemobile.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.test.SingleLaunchActivityTestCase;
import android.util.Log;

import com.appmunki.miragemobile.TestARActivity;

public class ARActivityTest extends
		SingleLaunchActivityTestCase<TestARActivity> {

	public ARActivityTest(Class<TestARActivity> activityClass) {
		super("com.appmunki.miragemobile", activityClass);
	}

	public ARActivityTest() {
		super("com.appmunki.miragemobile", TestARActivity.class);
	}

	public void test1AddPattern() {
		TestARActivity activity = getActivity();
		// Test that the images were loaded
		List<Bitmap> bitmapList = new ArrayList<Bitmap>();
		for (int i = 1; i < 8; i++) {
			Log.i(getName(), "posters/Movie Poster " + i + ".jpg");
			Bitmap bitmap = getBitmapFromAsset("posters/Movie Poster " + i
					+ ".jpg");
			bitmapList.add(bitmap);
			activity.addPattern(bitmap);
		}
	}

	/**
	 * Tests the matching code Loads in a list of bitmaps Then, checks the
	 * matching versus a testimage
	 */
	public void test3RightMatching() {
		TestARActivity activity = getActivity();

		assertTrue(activity.match(getBitmapFromAsset("query1.jpg")) > 0);
	}

	/**
	 * Tests the matching code Loads in a list of bitmaps Then tries and check
	 * for match, but the matche is not in the db
	 */
	public void test2WrongMatching() {
		TestARActivity activity = getActivity();

		assertEquals(0, activity.match(getBitmapFromAsset("query2.jpg")));
	}

	public void test4MatricesProjections() {
		TestARActivity activity = getActivity();
		activity.drawSquare();
	}

	/**
	 * 
	 * @param strName
	 * @return
	 */
	private Bitmap getBitmapFromAsset(String strName) {
		AssetManager assetManager = getInstrumentation().getTargetContext()
				.getResources().getAssets();

		InputStream istr = null;
		try {
			istr = assetManager.open(strName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap bitmap = BitmapFactory.decodeStream(istr);

		return bitmap;
	}

	/**
	 * Saves the nitmap in a file for testing
	 * 
	 * @param finalBitmap
	 */
	private void saveImage(Bitmap finalBitmap) {

		String root = Environment.getExternalStorageDirectory().toString();
		Log.e("Testing", root);
		File myDir = new File(root + "/miragetest_images");
		myDir.mkdirs();
		Random generator = new Random();
		int n = 10000;
		n = generator.nextInt(n);
		String fname = "Image-" + n + ".jpg";
		File file = new File(myDir, fname);
		if (file.exists())
			file.delete();
		try {
			FileOutputStream out = new FileOutputStream(file);
			finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();

		} catch (Exception e) {

			e.printStackTrace();
		}
	}
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		//super.tearDown();
	}
}
