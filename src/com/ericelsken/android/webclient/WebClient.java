package com.ericelsken.android.webclient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;

import android.net.Uri;

public final class WebClient {
	
	/**
	 * Default size of character buffer to use when reading responses.
	 */
	private static final int DEFAULT_BUFFER_SIZE = 1 << 10;
	
	/**
	 * Default character set to use when encoding/decoding the HTTP body. 
	 */
	public static final String DEFAULT_CHAR_SET = "UTF-8";
	
	/**
	 * The base URL to which all URI's are appended before making requests.
	 */
	private String mBaseUrl;
	
	/**
	 * The name of the character set to use when reading/writing to server.
	 */
	private String mCharSet;
	
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
		mBaseUrl = "";
		mCharSet = DEFAULT_CHAR_SET;
		mBufferSize = DEFAULT_BUFFER_SIZE;
	}
	
	public CookieStore getCookieStore() {
		return mCookieStore;
	}
	
	public void setCookieStore(CookieStore store) {
		mCookieStore = store;
	}
	
	public String getCharacterSet() {
		return mCharSet;
	}
	
	public void setCharacterSet(String charSet) {
		mCharSet = charSet;
	}
	
	public String getBaseUrl() {
		return mBaseUrl;
	}
	
	public void setBaseUrl(String baseUrl) {
		mBaseUrl = baseUrl;
	}
	
	public int getBufferSize() {
		return mBufferSize;
	}
	
	public void setBufferSize(int bufferSize) {
		mBufferSize = bufferSize;
	}
	
	public String executeDelete(String uri) throws ClientProtocolException, IOException, HttpException {
		HttpDelete request = new HttpDelete(mBaseUrl + uri);
		return makeRequest(request);
	}
	
	public String executeGet(String uri) throws ClientProtocolException, IOException, HttpException {
		HttpGet request = new HttpGet(mBaseUrl + uri);
		return makeRequest(request);
	}
	
	public String executePost(String uri, String data) throws ClientProtocolException, IOException, HttpException {
		HttpPost request = new HttpPost(mBaseUrl + uri);
		request.setEntity(new StringEntity(data, mCharSet));
		return makeRequest(request);
	}
	
	public String executePut(String uri, String data) throws ClientProtocolException, IOException, HttpException {
		HttpPut request = new HttpPut(mBaseUrl + uri);
		request.setEntity(new StringEntity(data, mCharSet));
		return makeRequest(request);
	}
	
	private synchronized String makeRequest(HttpRequestBase request) throws ClientProtocolException, IOException, HttpException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		mCookieStore.clearExpired(new Date());
		httpClient.setCookieStore(mCookieStore);
		try {
			HttpResponse response = httpClient.execute(request);
			StatusLine status = response.getStatusLine();
			final String body = entityToString(response.getEntity(), mBufferSize, mCharSet);
			final int code = status.getStatusCode() / 100;
			if(code >= 4 && code <= 5) {
				throw new HttpException(status.getStatusCode(), body);
			}
			return body;
		} catch (ClientProtocolException ex) {
			return null;
		}
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
			tempRead = reader.read(buffer, 0, buffer.length); //attempts to fill the buffer. returns how many chars were actually read.
			if(tempRead < 0) {
				break;
			}
			builder.append(buffer, 0, tempRead);
		}
		reader.close();
		return builder.toString();
	}
	
	public static String makeUri(String baseUrl, String appendedUri) {
		return Uri.withAppendedPath(Uri.parse(baseUrl), appendedUri).toString();
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
