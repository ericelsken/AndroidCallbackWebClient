package com.ericelsken.android.web;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class Response {

	private final HttpURLConnection conn;
	private final String body;
	private final Exception ex;
	
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
	
	public Map<String, List<String>> getHeaderFields() {
		if(conn == null) {
			return null;
		}
		return conn.getHeaderFields();
	}
}
