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
import android.graphics.BitmapFactory.Options;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.appmunki.miragemobile.MainActivity;
import com.appmunki.miragemobile.TestARActivity;

public class ARActivityTest extends ActivityInstrumentationTestCase2<TestARActivity>{

	
	
	

	public ARActivityTest(Class<TestARActivity> activityClass) {
		super(activityClass);
	}
	public ARActivityTest() {
	    super(TestARActivity.class);
	}
	/**
	 * Tests the matching code
	 * Loads in a list of bitmaps 
	 * Then, checks the matching versus a testimage
	 */
	public void testMatching(){
		  TestARActivity activity = getActivity();
		  
		  
		  //Test that the images were loaded
		  List<Bitmap> bitmapList = new ArrayList<Bitmap>();
		  for(int i=1;i<8;i++){
			  Log.i(getName(), "posters/Movie Poster "+i+".jpg");
			  Bitmap bitmap = getBitmapFromAsset("posters/Movie Poster "+i+".jpg");
			  bitmapList.add(bitmap);
			  activity.addPattern(bitmap);
		  }
		  activity.matchDebug(getBitmapFromAsset("query1.jpg"));
	}
	/**
	 * Retrieve a bitmap from a assestatic void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width,
			int height) {
		Log.i("Util","yuv size "+yuv420sp.length);

		final int frameSize = width * height;

		int yIndex = 0;
		int uvIndex = frameSize;

		int a, R, G, B, Y, U, V;
		int index = 0;
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {

				a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
				R = (argb[index] & 0xff0000) >> 16;
				G = (argb[index] & 0xff00) >> 8;
				B = (argb[index] & 0xff) >> 0;

				// well known RGB to YUV algorithm
				Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
				U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
				V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

				// NV21 has a plane of Y and interleaved planes of VU each
				// sampled by a factor of 2
				// meaning for every 4 Y pixels there are 1 V and 1 U. Note the
				// sampling is every other
				// pixel AND every other scanline.
				yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0
						: ((Y > 255) ? 255 : Y));
				if (j % 2 == 0 && index % 2 == 0) {
					yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0
							: ((V > 255) ? 255 : V));
					yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0
							: ((U > 255) ? 255 : U));
				}

				index++;
			}
		}
	}ts folder
	 * @param strName
	 * @return
	 */
	private Bitmap getBitmapFromAsset(String strName)
    {
        AssetManager assetManager = getInstrumentation().getTargetContext().getResources().getAssets();
        
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
	    String fname = "Image-"+ n +".jpg";
	    File file = new File (myDir, fname);
	    if (file.exists ()) file.delete (); 
	    try {
	           FileOutputStream out = new FileOutputStream(file);
	           finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
	           out.flush();
	           out.close();

	    } catch (Exception e) {
	    	   
	           e.printStackTrace();
	    }
	}

}
