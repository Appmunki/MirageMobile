package com.appmunki.miragemobile.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

/**
 * Contains utility functions
 * 
 * @author hoangtung
 * 
 */
public class Util {
	public enum CVFunction {
		Features, Match, DebugMatch, Convert
	}

	public static boolean DEBUG = true;
	public static final int mask[] = { 0x000000ff, 0x0000ff00, 0x00ff0000,
			0xff000000 };

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
	 * Converts byte to Int
	 * 
	 * @param data
	 * @return
	 */
	public static int[] byteToInt(byte[] data) {
		int[] ints = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			ints[i] = (int) data[i] & 0xff;
		}
		return ints;
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
		FloatBuffer buffer = ByteBuffer.wrap(barr).order(ByteOrder.BIG_ENDIAN)
				.asFloatBuffer();
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
		IntBuffer buffer = ByteBuffer.wrap(barr).order(ByteOrder.BIG_ENDIAN)
				.asIntBuffer();
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

	/**
	 * Converts a bitmap into a yuv data array
	 * 
	 * @param inputWidth
	 * @param inputHeight
	 * @param scaled
	 * @return yuv byte array
	 */
	public static byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

		int[] argb = new int[inputWidth * inputHeight];

		scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

		byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
		encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

		// scaled.recycle();

		return yuv;
	}

	/**
	 * Convert the byte array to an int starting from the given offset.
	 * 
	 * @param b
	 *            The byte array
	 * @param offset
	 *            The array offset
	 * @return The integer
	 */
	public static int byteArrayToInt(byte[] b, int offset) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i + offset] & 0x000000FF) << shift;
		}
		return value;
	}

	/**
	 * Scales bitmap
	 * 
	 * @param bm
	 *            orginal bitmap being scaled
	 * @param newHeight
	 *            scaled height of the bitmap
	 * @param newWidth
	 *            scaled width of the bitmap
	 * @return new bitmap
	 */
	public static Bitmap resizedBitmap(Bitmap bm, int newWidth, int newHeight) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);
		return resizedBitmap;
	}

	/**
	 * Scales bitmap
	 * 
	 * @param bm
	 *            orginal bitmap being scaled
	 * @param newHeight
	 *            scaled height of the bitmap
	 * @param newWidth
	 *            scaled width of the bitmap
	 * @return new bitmap
	 */
	public static Bitmap resizedToScreenBitmap(Bitmap bm, int screenWidth,
			int screenHeight) {
		if (bm.getWidth() > screenWidth) {
			int newHeight = (screenHeight * bm.getHeight()) / bm.getWidth();
			bm = resizedBitmap(bm, screenWidth, newHeight);
		} else if (bm.getHeight() > screenHeight) {
			int newWidth = (screenHeight * bm.getWidth()) / bm.getHeight();
			bm = resizedBitmap(bm, newWidth, screenHeight);
		}
		return bm;
	}

	public static Bitmap rotateBitmap(Bitmap bitmap, float rotate) {
		// bitmap = resizedBitmap(bitmap, newHeight, newWidth);
		if (rotate != 0) {
			Matrix matrix = new Matrix();
			matrix.setRotate(rotate);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), matrix, false);
		}
		return bitmap;
	}

	public static Bitmap rotateandresize(Bitmap bitmap, float rotate,
			int canvasWidth, int canvasHeight) {
		bitmap = Util.rotateBitmap(bitmap, 270);

		if (bitmap.getWidth() > canvasWidth) {
			Log.i("Changing Width",
					bitmap.getWidth() + "x" + bitmap.getHeight());
			Log.i("Canvas Size", canvasWidth + "x" + canvasHeight);
			int newHeight = (bitmap.getHeight() * canvasWidth)
					/ bitmap.getWidth();
			bitmap = resizedBitmap(bitmap, canvasWidth, newHeight);
			Log.i("New Width", bitmap.getWidth() + "x" + bitmap.getHeight());

		} else if (bitmap.getHeight() > canvasHeight) {
			// TODO fix like the width if stateent
			int newHeight = (bitmap.getHeight() * canvasWidth)
					/ bitmap.getWidth();
			bitmap = resizedBitmap(bitmap, canvasWidth, newHeight);
			Log.i("Changing Height", canvasWidth + "x" + newHeight);

		}
		return bitmap;
	}

	/**
	 * Used on encode argb to yuv
	 * 
	 * @param yuv420sp
	 * @param argb
	 * @param width
	 * @param height
	 */
	static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width,
			int height) {
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
	}
}
