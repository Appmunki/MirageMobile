package com.appmunki.miragemobile.test;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.test.SingleLaunchActivityTestCase;
import android.util.Log;
import android.widget.ImageView.ScaleType;

import com.appmunki.miragemobile.MainARActivity;
import com.appmunki.miragemobile.utils.Util;

public class ARActivityTest extends SingleLaunchActivityTestCase<MainARActivity> {

	private final String TAG = "ARActivityTest";
	public ARActivityTest(Class<MainARActivity> activityClass) {
		super("com.appmunki.miragemobile", activityClass);
	}

	public ARActivityTest() {
		super("com.appmunki.miragemobile", MainARActivity.class);
	}

	public void test1AddPattern() throws IOException {
		MainARActivity activity = getActivity();
		// Test that the images were loaded
		for (int i = 1; i < 8; i++) {
			Log.i(getName(), "posters/Movie Poster " + i + ".jpg");
			String name = "Movie Poster " + i+ ".jpg";
			InputStream stream = Util.getStreamFromAsset(getInstrumentation().getTargetContext(), "posters/Movie Poster " + i
					+ ".jpg");

			activity.addPattern(name,stream);
		}
	}

	/**
	 * Tests the matching code Loads in a list of bitmaps Then, checks the
	 * matching versus a testimage
	 * @throws Exception 
	 */
	public void test3RightMatching() throws Exception {
		final MainARActivity activity = getActivity();
		final Bitmap bmp=Util.getBitmapFromAsset(getInstrumentation().getTargetContext(), "query1.jpg");
		final Bitmap previewbmp=Util.resizedBitmap(bmp, 640, 480);
		final Bitmap picturebmp=Util.resizedBitmap(bmp, 1600, 1200);
		Log.i(TAG, "bmp's "+bmp.getWidth()+"x"+bmp.getHeight());
		Log.i(TAG, "preview's "+previewbmp.getWidth()+"x"+previewbmp.getHeight());
		Log.i(TAG, "picture's "+picturebmp.getWidth()+"x"+picturebmp.getHeight());

		activity.runOnUiThread(new Runnable() {
		  @Override
		  public void run() {
				  Matrix matrix = new Matrix();
	
				  matrix.postRotate(90); 
	
	
				  Bitmap rotated =  Bitmap.createBitmap(picturebmp,0,0,picturebmp.getWidth(),picturebmp.getHeight(),matrix,true);
				  Log.i(TAG, "rotated: "+rotated.getWidth()+"x"+rotated.getHeight());
	
				  activity.mImageView.setImageBitmap(rotated);	
				  activity.mImageView.setScaleType(ScaleType.FIT_XY);
			  }
		});
		bmp.recycle();
		assertTrue(activity.matchDebug(previewbmp) > 0);
	}

	/**
	 * Tests the matching code Loads in a list of bitmaps Then tries and check
	 * for match, but the matche is not in the db
	 * @throws Exception 
	 */
	public void test2WrongMatching() throws Exception {
		MainARActivity activity = getActivity();
		Bitmap bmp=Util.getBitmapFromAsset(getInstrumentation().getTargetContext(), "query2.jpg");

		assertEquals(0, activity.matchDebug(bmp));
	}

	public void test4Performance() throws Exception{
		final MainARActivity activity = getActivity();
		final Bitmap bmp=Util.getBitmapFromAsset(getInstrumentation().getTargetContext(), "query1.jpg");
		final Bitmap previewbmp=Util.resizedBitmap(bmp, 640, 480);
		final Bitmap picturebmp=Util.resizedBitmap(bmp, 1600, 1200);
		Log.i(TAG, "bmp's "+bmp.getWidth()+"x"+bmp.getHeight());
		Log.i(TAG, "preview's "+previewbmp.getWidth()+"x"+previewbmp.getHeight());
		Log.i(TAG, "picture's "+picturebmp.getWidth()+"x"+picturebmp.getHeight());

		activity.runOnUiThread(new Runnable() {
		  @Override
		  public void run() {
			  Matrix matrix = new Matrix();

			  matrix.postRotate(90); 


			  Bitmap rotated =  Bitmap.createBitmap(picturebmp,0,0,picturebmp.getWidth(),picturebmp.getHeight(),matrix,true);
			  Log.i(TAG, "rotated: "+rotated.getWidth()+"x"+rotated.getHeight());
			  activity.mImageView.setImageBitmap(rotated);	
			  activity.mImageView.setScaleType(ScaleType.FIT_XY);
		  }
		});
		bmp.recycle();
		for(int i=0;i<200;i++){
			activity.matchDebug(previewbmp);
			assertTrue(activity.getNumPatternResults() == 1);
		}
	}
	
	

	@SuppressWarnings("static-access")
	@Override
	protected void setUp() throws Exception {
		MainARActivity activity = getActivity();
		activity.debug=true;
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		//super.tearDown();
	}
}
