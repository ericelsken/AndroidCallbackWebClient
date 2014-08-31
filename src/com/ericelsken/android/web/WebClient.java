package com.ericelsken.android.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URI;

import android.os.Build;

public class WebClient {
	
	/**
	 * Size of byte buffer to use when reading responses.
	 */
	public static final int DEFAULT_BUFFER_SIZE = 1 << 10;
	
	/**
	 * Character set to use when encoding/decoding the HTTP body. 
	 */
	public static final String DEFAULT_CHAR_SET_NAME = "UTF-8";
	
	private static class InstanceHolder {
		private static final WebClient sInstance = new WebClient();
	}
	
	public static WebClient getInstance() {
		return InstanceHolder.sInstance;
	}
	
	private String mCharSetName;
	
	private int mBbufferSize;
	
	private WebClient() {
		CookieHandler.setDefault(new CookieManager());
		mCharSetName = DEFAULT_CHAR_SET_NAME;
		mBbufferSize = DEFAULT_BUFFER_SIZE;
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) { //Work around pre-Froyo bugs in HTTP connection reuse.
			System.setProperty("http.keepAlive", "false");
		}
	}
	
	public String getCharSetName() {
		return mCharSetName;
	}

	public void setCharSetName(String charSetName) {
		if(charSetName == null) {
			throw new NullPointerException("charSetName cannot be null.");
		}
		this.mCharSetName = charSetName;
	}

	public int getBufferSize() {
		return mBbufferSize;
	}

	public void setBufferSize(int bufferSize) {
		if(bufferSize < 0) {
			bufferSize = DEFAULT_BUFFER_SIZE;
		}
		this.mBbufferSize = bufferSize;
	}

	public String executeDelete(URI uri) throws IOException, HttpException {
		HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
		conn.setRequestMethod("DELETE");
		return makeRequest(conn, null);
	}
	
	public String executeGet(URI uri) throws IOException, HttpException {
		HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
		conn.setRequestMethod("GET");
		return makeRequest(conn, null);
	}
	
	public String executePost(URI uri, String data) throws IOException, HttpException {
		HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
		conn.setRequestMethod("POST");
		return makeRequest(conn, data);
	}
	
	public String executePut(URI uri, String data) throws IOException, HttpException {
		HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
		conn.setRequestMethod("PUT");
		return makeRequest(conn, data);
	}
	
	private String makeRequest(HttpURLConnection conn, String data) throws IOException, HttpException {
		String body = null;
		try {
			if(data != null) {
				writeData(conn, data);
			}
			body = inputToString(conn);
		}
		finally {
			conn.disconnect();
		}
		int statusCode = conn.getResponseCode();
		if(statusCode / 100 != 2) {
			throw new HttpException(statusCode, body);
		}
		return body;
	}

	private String inputToString(HttpURLConnection conn) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(mBbufferSize);
		InputStream in = new BufferedInputStream(conn.getInputStream(), mBbufferSize);
		copyStreams(in, out);
		return out.toString(mCharSetName);
	}
	
	private void writeData(HttpURLConnection conn, String data) throws IOException {
		conn.setDoOutput(true);
		OutputStream out = new BufferedOutputStream(conn.getOutputStream(), mBbufferSize);
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(data.getBytes(mCharSetName)), mBbufferSize);
		copyStreams(in, out);
	}
	
	private void copyStreams(InputStream in, OutputStream out) throws IOException {
		int tempRead = 0;
		byte[] buffer = new byte[mBbufferSize];
		while(tempRead != -1) {
			tempRead = in.read(buffer, 0, buffer.length);
			if(tempRead == -1) {
				break;
			}
			out.write(buffer, 0, tempRead);
		}
		in.close();
		out.close();
	}
}
