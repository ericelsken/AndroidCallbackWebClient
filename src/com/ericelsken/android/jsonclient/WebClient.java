package com.ericelsken.android.jsonclient;

import java.io.InputStreamReader;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.net.Uri;

public final class WebClient {
	
	public static final String sCharSet = "UTF-8"; //the character set used for the entities and responses.
	public static final int sBufferSize = 1 << 10; //buffer size of the InputStreamReader used for reading the response entity.
	
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
	
	public synchronized JSONObject executeDelete(String uri) throws Exception {
		HttpDelete request = new HttpDelete(uri);
		return makeJsonRequest(request);
	}
	
	public synchronized JSONObject executeGet(String uri) throws Exception {
		HttpGet request = new HttpGet(uri);
		return makeJsonRequest(request);
	}
	
	public synchronized JSONObject executePost(String uri, JSONObject json) throws Exception {
		HttpPost request = new HttpPost(uri);
		String jsonStr = json.toString();
		StringEntity entity = new StringEntity(jsonStr, sCharSet);
		request.setEntity(entity);
		return makeJsonRequest(request);
	}
	
	public synchronized JSONObject executePut(String uri, JSONObject json) throws Exception {
		HttpPut request = new HttpPut(uri);
		String jsonStr = json.toString();
		StringEntity entity = new StringEntity(jsonStr, sCharSet);
		request.setEntity(entity);
		return makeJsonRequest(request);
	}
	
	private synchronized JSONObject makeJsonRequest(HttpRequestBase request) throws Exception {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		mCookieStore.clearExpired(new Date());
		httpClient.setCookieStore(mCookieStore);
		HttpResponse response = httpClient.execute(request);
		JSONObject responseObj = entityToJson(response.getEntity());
		StatusLine status = response.getStatusLine();
		int code = status.getStatusCode() / 100;
		if(code >= 4 && code <= 5) {
			throw new HttpException(status.getStatusCode(), status.getReasonPhrase(), responseObj);
		}
		return responseObj;
	}
	
	private static JSONObject entityToJson(HttpEntity entity) throws Exception {
		int tempRead = 0;
		char[] buffer = new char[sBufferSize];
		StringBuilder jsonBuilder = new StringBuilder();
		InputStreamReader reader = new InputStreamReader(entity.getContent(), sCharSet);
		while(tempRead != -1) {
			tempRead = reader.read(buffer, 0, buffer.length); //attempts to fill the buffer. returns how many chars are actually read.
			if(tempRead < 0) {
				break;
			}
			jsonBuilder.append(buffer, 0, tempRead);
		}
		reader.close();
		if(jsonBuilder.length() == 0) {
			return null; //if the response is empty, then return null to denote nothing was sent back from the service.
		}
		return new JSONObject(jsonBuilder.toString());
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
