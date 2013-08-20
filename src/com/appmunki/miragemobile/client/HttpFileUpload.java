package com.appmunki.miragemobile.client;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.util.Log;

public class HttpFileUpload implements Runnable {
	
	private static String TAG = "FileUpload";
	
	URL connectURL;
	String responseString;
	String Title;
	String Description;
	byte[] dataToServer;
	FileInputStream fileInputStream = null;
	String imageName;

	HttpFileUpload(String urlString, String vTitle, String vDesc) {
		try {
			connectURL = new URL(urlString);
			Title = vTitle;
			Description = vDesc;
		} catch (Exception ex) {
			Log.i("HttpFileUpload", "URL Malformatted");
		}
	}

	void send_now(FileInputStream fStream, String imageName) {
		fileInputStream = fStream;
		this.imageName = imageName;
		Sending();
	}

	void Sending() {
		String iFileName = imageName;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		String Tag = "fSnd";
		try {
			Log.e(Tag, "Starting Http File Sending to URL");
			Long start = System.currentTimeMillis();

			// Open a HTTP connection to the URL
			HttpURLConnection conn = (HttpURLConnection) connectURL.openConnection();

			// Allow Inputs
			conn.setDoInput(true);

			// Allow Outputs
			conn.setDoOutput(true);

			// Don't use a cached copy.
			conn.setUseCaches(false);

			// Use a post method.
			conn.setRequestMethod("POST");

			conn.setRequestProperty("Connection", "Keep-Alive");

			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"title\"" + lineEnd);
			dos.writeBytes(lineEnd);
			dos.writeBytes(Title);
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);

			dos.writeBytes("Content-Disposition: form-data; name=\"description\"" + lineEnd);
			dos.writeBytes(lineEnd);
			dos.writeBytes(Description);
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);

			dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + iFileName + "\"" + lineEnd);
			dos.writeBytes(lineEnd);

			Log.e(Tag, "Headers are written");

			// create a buffer of maximum size
			int bytesAvailable = fileInputStream.available();

			int maxBufferSize = 1024;
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);
			byte[] buffer = new byte[bufferSize];

			// read file and write it into form...
			int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// close streams
			fileInputStream.close();

			dos.flush();

			Log.e(Tag, "File Sent, Response: " + String.valueOf(conn.getResponseCode()));

			InputStream is = conn.getInputStream();

			// retrieve the response from server
			int ch;

			StringBuffer b = new StringBuffer();
			while ((ch = is.read()) != -1) {
				b.append((char) ch);
			}
			String s = b.toString();
			Log.i("Response", s);
			dos.close();

			if (s.contains("has been uploaded")) {
				Log.v(TAG, "IMAGE RECEIVED");
			}

			Log.v("TEST", "Time to Send " + (System.currentTimeMillis() - start) + "ms");

		} catch (MalformedURLException ex) {
			Log.e(Tag, "URL error: " + ex.getMessage(), ex);
		}

		catch (IOException ioe) {
			Log.e(Tag, "IO error: " + ioe.getMessage(), ioe);
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
	}
}
