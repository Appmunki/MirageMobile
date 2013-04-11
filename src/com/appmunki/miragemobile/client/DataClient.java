package com.appmunki.miragemobile.client;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.appmunki.miragemobile.ar.ARActivity;
import com.appmunki.miragemobile.util.Util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class DataClient extends AsyncTask<ArrayList, Void, Boolean> {
	private static final String TAG = "DataClient";
	/** Server Host */
	// private String HOST = "192.168.0.100";
	private String HOST = "184.106.134.110";
	/** Server port */
	private int PORT = 3302;
	/** Socket object */
	private Socket _socket = null;
	/** Output stream to socket communication. */
	private DataOutputStream _out = null;
	/** Input stream to socket communication. */
	private DataInputStream _in = null;
	private Context _context;

	private String path;
	private String filename;

	private ARActivity arActivity;

	private String response;

	private String fileToDownload;

	String URL;

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

	}

	public DataClient(String path, String filename, ARActivity arActivity) {
		this.path = path;
		this.filename = filename;
		this.arActivity = arActivity;
	}

	@Override
	protected Boolean doInBackground(ArrayList... arg0) {

		// DownloadFromUrl("http://192.168.0.100/posters/",
		// "Movie Poster 1.jpg");
		// dialogTest("Movie Poster 1.jpg");

		try {
			uploadFile(path, filename);
			_socket = new Socket(HOST, PORT);
			if (_socket.isConnected()) {
				long start = System.currentTimeMillis();

				Log.i(TAG, "Client connected");
				// Open streams
				_out = new DataOutputStream(_socket.getOutputStream());
				_in = new DataInputStream(_socket.getInputStream());
				Log.i(TAG, "Streams created");

				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(_out));
				bw.write("MATCH " + filename + "\n");
				bw.flush();
				_socket.shutdownOutput();
				Log.i(TAG, "Done send");

				response = _in.readLine();

				JSONObject object;
				try {
					object = new JSONObject(response);
					JSONObject responseObject = object.getJSONObject("response");
					int code = responseObject.getInt("code");
					if (code == 0) {
						fileToDownload = object.getString("URL");
						Log.e(TAG, object.toString());
						downloadFile(fileToDownload);
					}else{
						fileToDownload = responseObject.getString("message");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Log.i(TAG, "Response time: " + (System.currentTimeMillis() - start) + "ms");
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

		return null;
	}

	@Override
	protected void onPostExecute(Boolean result) {

		arActivity.setMessage(fileToDownload);
		super.onPostExecute(result);
	}

	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		super.onCancelled();
	}

	public void uploadFile(String path, String filename) {
		try {
			String fullPath = path + "/" + filename;
			Log.v(TAG, "FULLPATH " + fullPath);
			FileInputStream fstrm = new FileInputStream(fullPath);

			// Set your server page url (and the file title/description)
			HttpFileUpload hfu = new HttpFileUpload("http://184.106.134.110/index.php", "TEST MIRAGE", "UPLOADING IMAGE");

			hfu.send_now(fstrm, filename);

		} catch (FileNotFoundException e) {
			Log.e(TAG, "FILE DOESN'T EXIST");
		}
	}

	public void downloadFile(String fileName) {

		try {
			fileName = fileName.replace(" ", "%20");

			Log.v(TAG, "Start downloading file");
			URL url = new URL("http://184.106.134.110/posters/" + fileName);
			URLConnection connection = url.openConnection();
			connection.connect();
			// this will be useful so that you can show a typical 0-100%
			// progress bar
			int fileLength = connection.getContentLength();

			// download the file
			InputStream input = new BufferedInputStream(url.openStream());
			String path = Util.getPathPictures()+"/Mirage" + "/" + fileName;
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
					Log.v(TAG, "" + progress);
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
}
