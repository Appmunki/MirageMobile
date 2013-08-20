package com.appmunki.miragemobile.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.appmunki.miragemobile.ar.ARActivity;
import com.appmunki.miragemobile.util.Util;

public class DataClient extends AsyncTask<ArrayList, Void, Boolean> {
	private static final String TAG = "DataClient";
	/** Server Host */
	private String HOST = "192.168.0.13";
	// private String HOST = "184.106.134.110";
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

	private JSONObject jsonResponse;

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
			/*
			 * _socket = new Socket(HOST, PORT); if (_socket.isConnected()) {
			 * long start = System.currentTimeMillis();
			 * 
			 * Log.i(TAG, "Client connected"); // Open streams _out = new
			 * DataOutputStream(_socket.getOutputStream()); _in = new
			 * DataInputStream(_socket.getInputStream()); Log.i(TAG,
			 * "Streams created");
			 * 
			 * JSONObject newJob = new JSONObject();
			 * 
			 * newJob.put("filename", pathImage);
			 * 
			 * newJob.put("user", "demo@appmunki.com");
			 * 
			 * newJob.put("type", "MATCH");
			 * 
			 * BufferedWriter bw = new BufferedWriter(new
			 * OutputStreamWriter(_out)); bw.write(newJob.toString() + "\n");
			 * bw.flush(); _socket.shutdownOutput(); Log.i(TAG, "Done send");
			 * 
			 * response = _in.readLine();
			 * 
			 * JSONObject object; try { object = new JSONObject(response);
			 * JSONObject responseObject = object.getJSONObject("response"); int
			 * code = responseObject.getInt("code"); if (code == 0) {
			 * fileToDownload = object.getString("URL"); Log.e(TAG,
			 * object.toString()); downloadFile(fileToDownload); } else {
			 * fileToDownload = responseObject.getString("message"); } } catch
			 * (JSONException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 * 
			 * Log.i(TAG, "Response time: " + (System.currentTimeMillis() -
			 * start) + "ms"); _in.close(); _out.close(); } else { Log.e(TAG,
			 * "Socket not open"); }
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		try {
			JSONArray arrayFiles = jsonResponse.getJSONArray("files");
			arActivity.setMessage(arrayFiles.getJSONObject(0).getString("match"));

		} catch (Exception e) {
			arActivity.setMessage("NO SE ENCONTRO UNA COINCIDENCIA");
		}

		super.onPostExecute(result);
	}

	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		super.onCancelled();
	}

	public void uploadFile(String path, String filename) {
		try {

			Bitmap bm = BitmapFactory.decodeFile("/sdcard/ironman.jpg");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bm.compress(CompressFormat.JPEG, 75, bos);
			byte[] data = bos.toByteArray();
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost("http://192.168.0.13:3000/uploads");
			postRequest.addHeader("accept", "application/json");
			ByteArrayBody bab = new ByteArrayBody(data, "test.jpg");
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart("upload", bab);
			postRequest.setEntity(reqEntity);

			HttpResponse response = httpClient.execute(postRequest);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String sResponse;
			StringBuilder s = new StringBuilder();

			while ((sResponse = reader.readLine()) != null) {
				s = s.append(sResponse);
			}
			Log.v("MIRAGE", "Response: " + s);

			jsonResponse = new JSONObject(s.toString());
			
			JSONArray arrayFiles = jsonResponse.getJSONArray("files");
			downloadFile(arrayFiles.getJSONObject(0).getString("url"));
			

			// String fullPath = path + "/" + filename;
			// Log.v(TAG, "FULLPATH " + fullPath);
			// FileInputStream fstrm = new FileInputStream(fullPath);
			//
			// // Set your server page url (and the file title/description)
			// // HttpFileUpload hfu = new
			// //
			// HttpFileUpload("http://192.168.0.109:8080/TestJSP/ClientUploader",
			// // "TEST MIRAGE", "UPLOADING IMAGE");
			// HttpFileUpload hfu = new HttpFileUpload("http://" + HOST +
			// ":8080/TestJSP/ClientUploader", "TEST MIRAGE",
			// "UPLOADING IMAGE");
			//
			// hfu.send_now(fstrm, filename);

		} catch (Exception e) {
			Log.e(TAG, "FILE DOESN'T EXIST");
		}
	}

	public void downloadFile(String urlFile) {

		try {
			urlFile = urlFile.replace(" ", "%20");

			Log.v(TAG, "Start downloading file");
			URL url = new URL("http://" + HOST + ":3000" + urlFile);
			URLConnection connection = url.openConnection();
			connection.connect();
			// this will be useful so that you can show a typical 0-100%
			// progress bar
			int fileLength = connection.getContentLength();

			// download the file
			InputStream input = new BufferedInputStream(url.openStream());
			String path = Util.getPathPictures() + "/Mirage" + "/" + "image.jpg";
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
