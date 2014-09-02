package com.ericelsken.android.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class Response {

	private final HttpURLConnection conn;
	private final String body;
	private Exception ex;
	
	public Response(HttpURLConnection conn, String body, Exception ex) {
		this.conn = conn;
		this.body = body;
		this.ex = ex;
	}
	
	public String getBody() {
		return body;
	}
	
	public boolean hasException() {
		return ex != null;
	}

	public Exception getException() {
		return ex;
	}
	
	public void setException(Exception ex) {
		this.ex = ex;
	}
	
	public int getResponseCode() {
		if(conn == null) {
			return -1;
		}
		try {
			return conn.getResponseCode();
		} catch (IOException e) {
			return -1;
		}
	}
	
	public Map<String, List<String>> getHeaderFields() {
		if(conn == null) {
			return null;
		}
		return conn.getHeaderFields();
	}
	
	public String getHeaderField(String key) {
		if(conn == null) {
			return null;
		}
		return conn.getHeaderField(key);
	}
}
