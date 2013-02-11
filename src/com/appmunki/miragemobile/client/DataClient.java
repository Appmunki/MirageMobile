package com.appmunki.miragemobile.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.appmunki.miragemobile.Util;
import com.appmunki.miragemobile.ar.entity.TargetImage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class DataClient extends AsyncTask<ArrayList, Void, Boolean> {
	private static final String TAG = "DataClient";
	/** Server Host */
	//private String HOST = "184.106.134.110";
	 private String HOST = "192.168.0.100";
	/** Server port */
	private int PORT = 3302;
	/** Socket object */
	private Socket _socket = null;
	/** Output stream to socket communication. */
	private DataOutputStream _out = null;
	/** Input stream to socket communication. */
	private InputStream _in = null;
	private Context _context;

	private JSONObject json;

	ProgressDialog mProgressDialog;

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
		mProgressDialog = new ProgressDialog(_context);
		mProgressDialog.setMessage("data proto");
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

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

				Log.i(TAG,
						"Response time: Uploading image "
								+ (System.currentTimeMillis() - start) + "ms");

				// read it with BufferedReader
				BufferedReader br = new BufferedReader(new InputStreamReader(
						_in));

				Log.v(TAG, "Receive Response");

				StringBuilder sb = new StringBuilder();

				long startResponse = System.currentTimeMillis();
				
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				
				Log.i(TAG,"Response time: Response "+ (System.currentTimeMillis() - startResponse)+ "ms");

				String response = sb.toString();
				int index = response.lastIndexOf("||");

				String fileName = response.substring(index + 2);

				long startDownload = System.currentTimeMillis();

				dialogTest(fileName);

				Log.i(TAG,"Response time: Download proto file "+ (System.currentTimeMillis() - startDownload)
								+ "ms");
				
				

				try{

					JSONArray responseArray = new JSONArray(response.substring(0, index));
					for (int i = 0; i < responseArray.length(); i++) {
						JSONObject object = responseArray.getJSONObject(i);
						TargetImage target = new TargetImage();
						target._ID.set(object.getInt("ID"));
						target._author.set(object.getString("author"));
						target._height.set(object.getInt("height"));
						target._width.set(object.getInt("width"));
						target._description
								.set(object.getString("description"));
						target._name.set(object.getString("name"));
						target._image.set(object.getString("image"));

						JSONArray dess = object.getJSONArray("dessbt");
						byte[] dessbt = Util.bytefromJSONArray(dess);
						target._dess.set(dessbt);

						JSONArray keys = object.getJSONArray("keysbt");
						byte[] keysbt = Util.bytefromJSONArray(keys);
						target._keys.set(keysbt);
						target.save(_context, target._ID.get());

						json = object;
						
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				br.close();

				Log.i(TAG,
						"Response time downloading file: "
								+ (System.currentTimeMillis() - start) + "ms");

				_in.close();
				_out.close();
			} else {
				Log.e(TAG, "Socket not open");
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	public String getAppFolder() {
		PackageManager m = _context.getPackageManager();
		String s = _context.getPackageName();
		try {
			PackageInfo p = m.getPackageInfo(s, 0);
			s = p.applicationInfo.dataDir;
		} catch (NameNotFoundException e) {
			Log.w("DataClient", "Error Package name not found ", e);
			return null;
		}

		return s;
	}

	

	public void showAlertDialog(JSONObject object) {
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(_context);
			builder.setTitle("Information");
			builder.setMessage("ID:" + object.getInt("ID") + " name:"
					+ object.getString("name"));
			builder.setPositiveButton("OK", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.create().show();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void dialogTest(String fileName) {

		try {
			Log.v(TAG, "Start downloading file");
			URL url = new URL("http://192.168.0.100/" + fileName);
			URLConnection connection = url.openConnection();
			connection.connect();
			// this will be useful so that you can show a typical 0-100%
			// progress bar
			int fileLength = connection.getContentLength();

			// download the file
			InputStream input = new BufferedInputStream(url.openStream());
			String path = getAppFolder() + "/" + fileName + ".mirage";
			OutputStream output = new FileOutputStream(path);

			Log.v(TAG, "PATH " + path);

			byte data[] = new byte[1024];
			long total = 0;
			int count;
			while ((count = input.read(data)) != -1) {
				total += count;
				// publishing the progress....
				int progress = (int) (total * 100 / fileLength);
				if ((progress % 10) == 0) {
					//Log.v(TAG, "" + progress);
				}
				// publishProgress((int) (total * 100 / fileLength));
				output.write(data, 0, count);
			}

			output.flush();
			output.close();
			input.close();
		} catch (Exception e) {
			Log.v(TAG, "FAIL " + e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		Log.v(TAG,"TERMINA LA EJECUCION");
		super.onPostExecute(result);
	}

}
