package com.appmunki.miragemobile.ar;

import com.appmunki.miragemobile.utils.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class CameraOverlayView extends View{

	private static final String TAG = "CameraOverlayView";
	private Paint paint;
	private Bitmap mbitmap;

	public CameraOverlayView(Context context) {
		super(context);
		this.paint = new Paint();
		this.paint.setAntiAlias(true);
		this.paint.setColor(Color.RED);
		this.paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeWidth(5f);
	}

	

	private Canvas canvas;
	private Bitmap bitmap;
	private int canvasHeight;
	private int canvasWidth;

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.i(TAG, "onSizeChanged");
		if (bitmap != null) {
			bitmap.recycle();
		}
		this.canvasWidth = w;
		this.canvasHeight = h;
		Log.i(TAG, "canvas " + w + "x" + h);
		canvas = new Canvas();
		bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		canvas.setBitmap(bitmap);
	}

	public void destroy() {
		if (bitmap != null) {
			bitmap.recycle();
		}
	}

	@Override
	public void onDraw(Canvas c) {
		super.onDraw(c);
		if (mbitmap != null) {
			// Log.i(TAG, "Drawing mbitmap");

			Log.i("Bitmap Size", mbitmap.getWidth() + "x" + mbitmap.getHeight());
			// Scale bitmap if larger than screen

			// mbitmap = Util.resizedBitmap(mbitmap, newHeight, 720);
			mbitmap = Util.rotateandresize(mbitmap, 270, canvasWidth,
					canvasHeight);
			Log.i("New Bitmap Size",
					mbitmap.getWidth() + "x" + mbitmap.getHeight());

			c.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
			c.drawBitmap(mbitmap,
					(this.canvasHeight - mbitmap.getHeight()) / 2,
					(this.canvasWidth - mbitmap.getWidth()) / 2, null);
			c.drawPaint(paint);
		}
	}
}
