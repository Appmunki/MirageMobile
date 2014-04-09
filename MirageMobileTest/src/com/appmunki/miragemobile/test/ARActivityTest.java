package com.appmunki.miragemobile.test;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.test.SingleLaunchActivityTestCase;
import android.test.UiThreadTest;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.appmunki.miragemobile.MainARActivity;
import com.appmunki.miragemobile.utils.Util;

public class ARActivityTest extends
		SingleLaunchActivityTestCase<MainARActivity> {

	private final String TAG = "ARActivityTest";
	/*private Bitmap bmp;
	private Bitmap previewbmp;
	private Bitmap rotated;*/

	public ARActivityTest(Class<MainARActivity> activityClass) {
		super("com.appmunki.miragemobile", activityClass);
	}

	public ARActivityTest() {
		super("com.appmunki.miragemobile", MainARActivity.class);
	}
	
	public void testCamera(){
//		mImageView = new ImageView(this);

		//open camera
		
		//close camera
	}

	public void test1AddPattern() throws IOException {
		MainARActivity activity = getActivity();
		// Test that the images were loaded
		for (int i = 1; i < 8; i++) {
			Log.i(getName(), "posters/Movie Poster " + i + ".jpg");
			String name = "Movie Poster " + i + ".jpg";
			InputStream stream = Util.getStreamFromAsset(getInstrumentation()
					.getTargetContext(), "posters/Movie Poster " + i + ".jpg");

			activity.addPattern(name, stream);
			stream.close();
		}
	}
	/**
	 * Tests the matching code loads in a list of bitmaps Then tries and check
	 * for match, but the match is not in the db
	 * 
	 * @throws Exception
	 */
	@UiThreadTest
	public void test2WrongMatching() throws Exception {
		MainARActivity activity = getActivity();
		Bitmap bmp = Util.getBitmapFromAsset(getInstrumentation()
				.getTargetContext(), "query2.jpg");

		assertEquals(0, activity.matchDebug(bmp));
		bmp.recycle();
	}
	/**
	 * Tests the matching code Loads in a list of bitmaps Then, checks the
	 * matching versus a testimage
	 * 
	 * @throws Exception
	 */
	@UiThreadTest
	public void test3RightMatching() throws Exception {
		final MainARActivity activity = getActivity();
		//initBitmaps();
		
		
		//activity.mImageView.setImageBitmap(rotated);
		//activity.mImageView.setScaleType(ScaleType.FIT_XY);
		InputStream stream = Util.getStreamFromAsset(getInstrumentation()
				.getTargetContext(), "query1.jpg");
		Bitmap bmp = Util.decodeSampledBitmapFromStream(stream, 1280, 960);
		Bitmap matchbmp = Util.resizedBitmap(bmp, 640, 480);
		int size = activity.matchDebug(matchbmp);
		Log.i(TAG,"Patterns Found "+size);
		assertTrue(size > 0);
		bmp.recycle();
		stream.close();

	}

	

	/**
	 * Tests the matching code loads a bitmap. Then match it against a database
	 * multiple time to check the performance.
	 * 
	 * @throws Exception
	 */
	@UiThreadTest
	public void test4Performance() throws Exception {
		final MainARActivity activity = getActivity();
		
		
		
		
		//initBitmaps();
		//activity.mImageView.setImageBitmap(rotated);
		//activity.mImageView.setScaleType(ScaleType.FIT_XY);
		InputStream stream = Util.getStreamFromAsset(getInstrumentation()
				.getTargetContext(), "query1.jpg");
		Bitmap bmp = Util.decodeSampledBitmapFromStream(stream, 1280, 960);
		Bitmap matchbmp = Util.resizedBitmap(bmp, 640, 480);
		
		for (int i = 0; i < 100; i++) {
			int size = activity.matchDebug(matchbmp);
			Log.v(TAG, "Match: " + i+","+size);
			assertTrue(size > 0);

		}	
		bmp.recycle();
		stream.close();
	}

	/*private void initBitmaps() {
		if (previewbmp == null) {
			Matrix matrix = new Matrix();
			matrix.postRotate(90);
			InputStream stream = Util.getStreamFromAsset(getInstrumentation()
					.getTargetContext(), "query1.jpg");
			bmp = Util.decodeSampledBitmapFromStream(stream, 1280, 960);
			previewbmp = Util.resizedBitmap(bmp, 640, 480);
			rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
					bmp.getHeight(), matrix, true);
			bmp.recycle();
		}
	}*/

	@SuppressWarnings("static-access")
	@Override
	protected void setUp() throws Exception {
		Log.i(TAG, "setUp");
		MainARActivity activity = getActivity();
		activity.debug = true;
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		Log.e(TAG, "tearDown");

		// TODO Auto-generated method stub
		super.tearDown();
	}
}