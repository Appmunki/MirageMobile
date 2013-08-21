package com.appmunki.miragemobile.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

/**
 * Contains utility functions
 * 
 * @author hoangtung
 * 
 */
public class Util {

	public static final int mask[] = { 0x000000ff, 0x0000ff00, 0x00ff0000, 0xff000000 };

	/**
	 * Compute average of an array
	 * 
	 * @param a
	 * @return
	 */
	public static float compAver(final float a[]) {
		float aver = 0;
		for (int i = 0; i < a.length; ++i) {
			aver += a[i];
		}

		return aver / a.length;
	}

	/**
	 * Compute standard deviation of an array
	 * 
	 * @param a
	 * @param aver
	 * @return
	 */
	public static float compDevi(final float a[], float aver) {
		float devi = 0;
		for (int i = 0; i < a.length; ++i) {
			devi += (a[i] - aver) * (a[i] - aver);
		}

		return (float) Math.sqrt(devi / a.length);
	}

	/**
	 * Convert float array to byte array
	 * 
	 * @param array
	 * @return
	 */
	public static byte[] floatArrayToByteArray(final float array[]) {
		ByteBuffer bb = ByteBuffer.allocate(array.length * 4);
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(array);

		return bb.array();
	}

	/**
	 * Convert a float number to byte array
	 * 
	 * @param f
	 * @return
	 */
	public static byte[] floatToByteArray(float f) {
		byte result[] = new byte[4];

		int value = Float.floatToIntBits(f);
		for (int j = 0; j < 4; ++j) {
			result[3 - j] = (byte) ((value & mask[j]) >> (j * 8));
		}

		return result;
	}

	/**
	 * Convert an int to byte array
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] intToByteArray(int value) {
		byte result[] = new byte[4];
		for (int j = 0; j < 4; ++j) {
			result[3 - j] = (byte) ((value & mask[j]) >> (j * 8));
		}

		return result;
	}

	/**
	 * Convert int array to byte array
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] intArrayToByteArray(int[] value) {
		ByteBuffer bb = ByteBuffer.allocate(value.length * 4);
		IntBuffer intb = bb.asIntBuffer();
		intb.put(value);

		return bb.array();
	}

	/**
	 * Convert byte array to float
	 * 
	 * @param b
	 * @return
	 */
	public static float byteArrayToFloat(byte b[]) {
		int temp = 0;
		for (int i = 0; i < 4; ++i) {
			temp = temp | (b[3 - i] << (i * 8));
		}

		System.out.println(temp);

		return Float.intBitsToFloat(temp);
	}

	/**
	 * Convert byte array to float array
	 * 
	 * @param barr
	 * @return
	 */
	public static float[] byteArrayToFloatArray(byte barr[]) {
		FloatBuffer buffer = ByteBuffer.wrap(barr).order(ByteOrder.BIG_ENDIAN).asFloatBuffer();
		float[] ints = new float[barr.length / 4];
		buffer.get(ints);
		return ints;
	}

	/**
	 * Convert byte array to int array
	 * 
	 * @param barr
	 * @return
	 */
	public static int[] byteArrayToIntArray(byte[] barr) {
		IntBuffer buffer = ByteBuffer.wrap(barr).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
		int[] ints = new int[barr.length / 4];
		buffer.get(ints);
		return ints;
	}

	/**
	 * Convert a serializable object to byte array
	 * 
	 * @param obj
	 * @return
	 */
	public static byte[] objectToByteArray(Object obj) {
		ObjectOutputStream oos;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return baos.toByteArray();
	}

	/**
	 * Convert byte array to object
	 * 
	 * @param b
	 * @return
	 */
	public static Object objectFromByteArray(byte b[]) {
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(bais);
			Object obj = ois.readObject();
			ois.close();
			return obj;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static String getPathPictures() {
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
	}

	public static void copyFileFromAssets(Context context, String file, String dest) throws Exception {
		InputStream in = null;
		OutputStream fout = null;
		int count = 0;
		try {
			in = context.getAssets().open(file);
			fout = new FileOutputStream(new File(dest));
			byte data[] = new byte[1024];
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static boolean createDirIfNotExists(String path) {
		boolean ret = true;

		File file = new File(Environment.getExternalStorageDirectory(), path);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				Log.e("TravellerLog :: ", "Problem creating Image folder");
				ret = false;
			}
		}
		return ret;
	}

	public static void copyAssetsPatterns(Context context) {
		try {
			createDirIfNotExists("MirageDebug");
			String path = "";
			for (String name : listPatternFiles(context)) {
				path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MirageDebug/";
				path += name;
				Util.copyFileFromAssets(context, "patterns/" + name, path);

				Log.v("PATTERN", "PATH " + path);
				Log.v("PATTERN", name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String[] listPatternFiles(Context context) {
		try {
			String[] filenames = context.getAssets().list("patterns");			
			return filenames;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
