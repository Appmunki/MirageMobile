package com.appmunki.miragemobile.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.appmunki.miragemobile.Util;
import com.appmunki.miragemobile.ar.entity.TargetImage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class DataClient extends AsyncTask<ArrayList, Void, Boolean> {
	private static final String TAG = "DataClient";
	/** Server Host */
	private String HOST = "192.168.1.3";
	/** Server port */
	private int PORT = 3302;
	/** Socket object */
	private Socket _socket = null;
	/** Output stream to socket communication. */
	private DataOutputStream _out = null;
	/** Input stream to socket communication. */
	private InputStream _in = null;
	private Context _context;

	/**
	 * Constructor
	 * 
	 * @param host
	 *            Server host
	 * @param port
	 *            Server port
	 */
	public DataClient(String host, int port) {
		HOST = host;
		PORT = port;
	}

	/**
	 * Constructor
	 * 
	 * @param host
	 *            Server host
	 * @param port
	 *            Server port
	 */
	public DataClient(Context context) {
		_context = context;
	}

	@Override
	protected Boolean doInBackground(ArrayList... arg0) {
		try {
			_socket = new Socket(HOST, PORT);
			if (_socket.isConnected()) {
				long start = System.currentTimeMillis();

				Log.i(TAG, "Client connected");
				// Open streams
				_out = new DataOutputStream(_socket.getOutputStream());
				Log.v(TAG, "OUT");
				_in = _socket.getInputStream();
				;

				Log.i(TAG, "Streams created");

				// Get bitmap from assets
				InputStream bitmap = _context.getAssets().open("query.jpg");
				Bitmap bit = BitmapFactory.decodeStream(bitmap);
				// Send bitmap
				try {
					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(_out));
					bw.write("MATCH " + ConvertValue.bitmapToBase64String(bit)
							+ "\n");
					bw.flush();
				} catch (Exception exc) {
					exc.printStackTrace();
				}
				_socket.shutdownOutput();
				Log.i(TAG, "Done send");

				// read it with BufferedReader
				BufferedReader br = new BufferedReader(new InputStreamReader(
						_in));

				StringBuilder sb = new StringBuilder();

				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

				try {
					JSONArray response = new JSONArray(sb.toString());
					for (int i = 0; i < response.length(); i++) {
						JSONObject object = response.getJSONObject(i);
						TargetImage target = new TargetImage();
						target._ID.set(object.getInt("ID"));
						target._author.set(object.getString("author"));
						target._height.set(object.getInt("height"));
						target._width.set(object.getInt("width"));
						target._description
								.set(object.getString("description"));
						target._name.set(object.getString("name"));
						target._image.set(object.getString("image"));
						target.save(_context, target._ID.get());
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Util.getInstance(_context).getAllTargets();

				br.close();

				Log.i(TAG, "Response time: "
						+ (System.currentTimeMillis() - start) + "ms");
				_in.close();
				_out.close();
			} else {
				Log.e(TAG, "Socket not open");
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		super.onCancelled();
	}

}
