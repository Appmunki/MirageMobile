package com.appmunki.miragemobile.ar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.entity.KeyPoint;
import com.entity.Mat;
import com.entity.TargetImage;

public class Matcher {
	private static final String TAG = "Matcher";

	public static void fetch(Context context) {
		List<TargetImage> bs = TargetImage.objects(context).all().toList();

		if (!(new File(context.getFilesDir().toString() + "/Data.txt").exists())) {
			writeData(bs, context);
		}
	}

	/**
	 * Write fetched keypoints and descriptors to file
	 * 
	 * @param b
	 */
	private synchronized static void writeData(List<TargetImage> b,
			Context context) {
		try {
			Log.e(TAG, context.getFilesDir().toString() + "/Data.txt");

			BufferedWriter bw = new BufferedWriter(new FileWriter(context
					.getFilesDir().toString() + "/Data.txt"));
			int dataSize = 1;
			Iterator<TargetImage> it = b.iterator();
			int size = b.size();
			for (int i = 0; i < size; ++i) {
				TargetImage temp = it.next();
				dataSize += temp.getdess().rows * temp.getdess().cols + 3
						+ temp.getkeys().size() * 7 + 1;
			}
			bw.write(dataSize + " ");
			bw.write(size + " ");
			it = b.iterator();
			for (int i = 0; i < size; ++i) {
				TargetImage temp = it.next();
				bw.write(temp.getwidth() + " ");
				bw.write(temp.getheight() + " ");
				int kSize = temp.getkeys().size();
				bw.write(kSize + " ");
				Log.e(TAG, "Writing Keys:" + kSize);
				for (int j = 0; j < kSize; ++j) {
					// Log.e(TAG, "Keys:");
					writeKey(bw, temp.getkeys().get(j));
				}
				Log.e(TAG, "Writing Dess");
				writeDes(bw, temp.getdess());
			}
			bw.flush();
			bw.close();
			Log.e(TAG, "Done");
		} catch (Exception exc) {
			Log.e(TAG, exc.getMessage());
			exc.printStackTrace();
		}
	}

	/**
	 * Write a KeyPoint to file
	 * 
	 * @param bw
	 * @param k
	 * @throws IOException
	 */
	private synchronized static void writeKey(BufferedWriter bw, KeyPoint k)
			throws IOException {
		bw.write(k.angle + " ");
		bw.write(k.classId + " ");
		bw.write(k.octave + " ");
		bw.write(k.x + " ");
		bw.write(k.y + " ");
		bw.write(k.response + " ");
		bw.write(k.size + " ");
	}

	/**
	 * Write a Mat to file
	 * 
	 * @param bw
	 * @param k
	 * @throws IOException
	 */
	private synchronized static void writeDes(BufferedWriter bw, Mat k)
			throws IOException {
		bw.write(k.rows + " ");
		bw.write(k.cols + " ");
		bw.write(k.type + " ");
		int size = k.rows * k.cols;
		for (int i = 0; i < size; ++i) {
			bw.write(k.data[i] + " ");
		}
	}

	public static native void load(boolean isDebug);

	public static native int[] match(long mGray);

	public static native int[] matchDebug(int width, int height, byte yuv[],
			int[] rgba);

	public static native void FindFeatures(int width, int height, byte yuv[],
			int[] rgba, int[] gray);

	public static native boolean isPatternPresent();

	public static native float[] getMatrix();

	public static native float[] getProjectionMatrix();

	public static native void convertFrame(int frameWidth, int frameHeight,
			byte[] data, int[] rgba);

}
