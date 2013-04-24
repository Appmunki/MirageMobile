package com.appmunki.miragemobile.ar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class CameraOverlayView extends View implements MarkerFoundListener {

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

	@Override
	public void found(Bitmap mbitmap) {
		Log.i(TAG, "Match finished");
		this.mbitmap = mbitmap;
		postInvalidate();

	}

	private Canvas canvas;
	private Bitmap bitmap;

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.i(TAG, "onSizeChanged");
		if (bitmap != null) {
			bitmap.recycle();
		}
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
			// c.drawColor(0xFFAAAAAA);
			c.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
			c.drawBitmap(mbitmap, (1196 - 1280) / 2, (720 - 720) / 2, null);
		}
	}
}
