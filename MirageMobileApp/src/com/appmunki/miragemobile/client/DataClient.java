package com.appmunki.miragemobile.client;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class DataClient extends AsyncTask<ArrayList, Void, Boolean> {
	private static final String TAG = "DataClient";
	/** Server Host */
	private String HOST = "http://www.trymirage.com";
	/** Server port */
	private int PORT = 44693;
	/** Socket object */
	private Socket _socket = null;
	/** Output stream to socket communication. */
	private DataOutputStream _out = null;
	/** Input stream to socket communication. */
	private ObjectInputStream _in = null;
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
				_in = new ObjectInputStream(_socket.getInputStream());
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

				Scanner input = new Scanner(_in);
				String response = null;

				while (input.hasNext()) {
					response = input.nextLine();
					Log.i(TAG, "Response " + response);
					response = "<bb>" + response + "</bb>";
				}
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
