package com.ericelsken.android.jsonclient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

public final class WebClient {
	
	/**
	 * Default size of character buffer to use when reading responses.
	 */
	private static final int DEFAULT_BUFFER_SIZE = 1 << 10;
	
	/**
	 * The name of the character set to use when reading/writing to server.
	 */
	private String mCharSet = "UTF-8";
	
	/**
	 * The size of the buffer to use when reading from the server.
	 */
	private int mBufferSize;
	
	/**
	 * CookieStore for this instance of a WebClient. All in memory and no persistent storage yet.
	 */
	private CookieStore mCookieStore;
	
	public WebClient() {
		mCookieStore = new BasicCookieStore();
	}
	
	public synchronized CookieStore getCookieStore() {
		return mCookieStore;
	}
	
	public synchronized void setCookieStore(CookieStore store) {
		mCookieStore = store;
	}
	
	public synchronized JSONObject executeDelete(String uri) throws ClientProtocolException, IOException, HttpException, JSONException {
		HttpDelete request = new HttpDelete(uri);
		return makeJsonRequest(request);
	}
	
	public synchronized JSONObject executeGet(String uri) throws ClientProtocolException, IOException, HttpException, JSONException {
		HttpGet request = new HttpGet(uri);
		return makeJsonRequest(request);
	}
	
	public synchronized JSONObject executePost(String uri, JSONObject json) throws ClientProtocolException, IOException, HttpException, JSONException {
		HttpPost request = new HttpPost(uri);
		String jsonStr = json.toString();
		StringEntity entity = new StringEntity(jsonStr, mCharSet);
		request.setEntity(entity);
		return makeJsonRequest(request);
	}
	
	public synchronized JSONObject executePut(String uri, JSONObject json) throws ClientProtocolException, IOException, HttpException, JSONException {
		HttpPut request = new HttpPut(uri);
		String jsonStr = json.toString();
		StringEntity entity = new StringEntity(jsonStr, mCharSet);
		request.setEntity(entity);
		return makeJsonRequest(request);
	}
	
	private synchronized JSONObject makeJsonRequest(HttpRequestBase request) throws ClientProtocolException, IOException, HttpException, JSONException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		mCookieStore.clearExpired(new Date());
		httpClient.setCookieStore(mCookieStore);
		HttpResponse response = httpClient.execute(request);
		final String body = entityToString(response.getEntity(), mBufferSize, mCharSet);
		StatusLine status = response.getStatusLine();
		final int code = status.getStatusCode() / 100;
		if(code >= 4 && code <= 5) {
			throw new HttpException(status.getStatusCode(), status.getReasonPhrase(), body);
		}		
		return new JSONObject(body);
	}
	
	private static String entityToString(HttpEntity entity, int bufferSize, String charSet) throws UnsupportedEncodingException, IllegalStateException, IOException {
		int tempRead = 0;
		if(bufferSize <= 0) {
			bufferSize = DEFAULT_BUFFER_SIZE;
		}
		char[] buffer = new char[bufferSize];
		StringBuilder builder = new StringBuilder();
		InputStreamReader reader = new InputStreamReader(entity.getContent(), charSet);
		while(tempRead != -1) {
			tempRead = reader.read(buffer, 0, buffer.length); //attempts to fill the buffer. returns how many chars are actually read.
			if(tempRead < 0) {
				break;
			}
			builder.append(buffer, 0, tempRead);
		}
		reader.close();
		return builder.toString();
	}
	
	public static String makeUri(String serverUrl, String appendedUri) {
		return Uri.withAppendedPath(Uri.parse(serverUrl), appendedUri).toString();
	}
	
	private static String appendQuery(String base, String query) {
		if(query == null || query.length() == 0) {
			return base;
		}
		if(base.indexOf('?') < 0) { //the base string does not already contain a query
			return base + "?" + query;
		}
		return base + "&" + query;
	}
	
	public static String appendQuery(String base, String key, String value) {
		return appendQuery(base, key + "=" + Uri.encode(value));
	}
}
