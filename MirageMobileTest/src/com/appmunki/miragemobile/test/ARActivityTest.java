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

public class ARActivityTest extends ActivityInstrumentationTestCase2<MainActivity>{

	
	
	

	public ARActivityTest(Class<MainActivity> activityClass) {
		super(activityClass);
	}
	public ARActivityTest() {
	    super(MainActivity.class);
	}

	public void testMatching(){
		  MainActivity activity = getActivity();
		  
		  //Test that the images were loaded
		  List<Bitmap> bitmapList = new ArrayList<Bitmap>();
		  for(int i=1;i<10;i++){
			  Bitmap bitmap = getBitmapFromAsset("posters/Movie Poster "+i+".jpg");
			  bitmapList.add(bitmap);
			  activity.addPattern(bitmap);
		  }
		  activity.matchDebug(getBitmapFromAsset("query.jpg"));
	}
	
	private Bitmap getBitmapFromAsset(String strName)
    {
        AssetManager assetManager = getInstrumentation().getTargetContext().getResources().getAssets();
        try {
			for(String n : assetManager.list("posters")){
				//Log.i("Test","Assets "+n);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        InputStream istr = null;
        try {
            istr = assetManager.open(strName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Options o = new BitmapFactory.Options();
        o.inSampleSize =  666666;
        Bitmap bitmap = BitmapFactory.decodeStream(istr,null,o);
        		
        return bitmap;
    }
	
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
