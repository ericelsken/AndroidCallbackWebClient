package com.ericelsken.android.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;

public class Request {
	
	public static final int DEFAULT_BUFFER_SIZE = 1 << 10;
	public static final int DELETE = 0;
	public static final int GET = 1;
	public static final int POST = 2;
	public static final int PUT = 3;
	
	/**
	 * All object fields of this class must be immutable or copied into instances of this class.
	 */
	private static class Params {
		private final URI uri;
		private String data;
		private int method;
		private int bufferSize;
		private List<String[]> headers;
		
		private Params(URI uri) {
			this.uri = uri;
			data = null;
			method = GET;
			bufferSize = DEFAULT_BUFFER_SIZE;
			headers = new LinkedList<String[]>();
		}
	}
	
	private Response response;
	
	//following should be an exact copy of Params fields, all final.
	private final URI uri;
	private final String data;
	private final int method;
	private final int bufferSize;
	private final List<String[]> headers;
	
	private Request(Params p) {
		uri = p.uri;
		data = p.data;
		method = p.method;
		bufferSize = p.bufferSize;
		headers = new LinkedList<String[]>();
		for(String[] pair : p.headers) {
			headers.add(new String[] {pair[0], pair[1]});
		}
	}
	
	public RequestHandler handle(Context context, int id, RequestCallback callback) {
		return new RequestHandler(context, id, this, callback);
	}
	
	public Response execute() {
		if(response != null) {
			return response;
		}
		HttpURLConnection conn = null;
		String body = null;
		Exception caught = null;
		try {
			if(uri.getScheme() == null || uri.getScheme().indexOf("http") != 0) {
				throw new SchemeException(uri.getScheme());
			}
			URL url = uri.toURL();
			conn = (HttpURLConnection) url.openConnection();
			body = executeRequest(conn);
		} catch (Exception ex) {
			caught = ex;
		}
		response = new Response(conn, body, caught);
		return response;
	}
	
	private String executeRequest(HttpURLConnection conn) throws IOException, HttpException {
		String body = null;
		try {
			apply(conn);
			if(data != null) {
				writeData(conn);
			}
			body = inputToString(conn);
		}
		finally {
			conn.disconnect();
		}
		int statusCode = conn.getResponseCode();
		if(statusCode / 100 != 2) {
			throw new HttpException(statusCode, conn.getResponseMessage(), body);
		}
		return body;
	}
	
	private void apply(HttpURLConnection conn) throws ProtocolException {
		switch(method) {
		case DELETE: conn.setRequestMethod("DELETE"); break;
		case GET: conn.setRequestMethod("GET"); break;
		case POST: conn.setRequestMethod("POST"); break;
		case PUT: conn.setRequestMethod("PUT"); break;
		}
		for(String[] pair : headers) {
			conn.setRequestProperty(pair[0], pair[1]);
		}
	}
	
	private String inputToString(HttpURLConnection conn) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(bufferSize);
		InputStream in = new BufferedInputStream(conn.getInputStream(), bufferSize);
		copyStreams(in, out);
		return out.toString();
	}

	private void writeData(HttpURLConnection conn) throws IOException {
		conn.setDoOutput(true);
		byte[] bytes = data.getBytes();
		conn.setFixedLengthStreamingMode(bytes.length);
		OutputStream out = new BufferedOutputStream(conn.getOutputStream(), bufferSize);
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(bytes), bufferSize);
		copyStreams(in, out);
	}

	private void copyStreams(InputStream in, OutputStream out) throws IOException {
		int tempRead = 0;
		byte[] buffer = new byte[bufferSize];
		while(tempRead != -1) {
			tempRead = in.read(buffer, 0, buffer.length);
			if(tempRead != -1) {
				out.write(buffer, 0, tempRead);
			}
		}
		in.close();
		out.close();
	}

	public static class Builder {
		
		private final Params p;
		
		public Builder(URI uri) {
			p = new Params(uri);
		}
		
		public Builder delete() {
			p.method = DELETE;
			return this;
		}
		
		public Builder get() {
			p.method = GET;
			return this;
		}
		
		public Builder post() {
			p.method = POST;
			return this;
		}
		
		public Builder put() {
			p.method = PUT;
			return this;
		}
		
		public Builder setData(String data) {
			p.data = data;
			return this;
		}
		
		public Builder setBufferSize(int bufferSize) {
			bufferSize = bufferSize < 0 ? DEFAULT_BUFFER_SIZE : bufferSize;
			p.bufferSize = bufferSize;
			return this;
		}
		
		public Builder setHeader(String field, String newValue) {
			if(field == null || newValue == null) {
				throw new NullPointerException("field and newValue cannot be null.");
			}
			p.headers.add(new String[] {field, newValue});
			return this;
		}
		
		public Request create() {
			return new Request(p);
		}
	}
}
