package com.ericelsken.android.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * This class represents the response or result of executing a Request.
 * Very simply, you can retrieve the body of the response, the status code, 
 * the status message, and the headers of the response.
 * More capability should be added in the future.
 * 
 * Aside from the body and Exception, all methods of this class are backed by
 * the HttpURLConnection given to the constructor. Please see the
 * documentation for java.net.HttpURLConnection as to how those methods
 * operate.
 * 
 * @author Eric Elsken
 *
 */
public class Response {

	private final HttpURLConnection conn;
	private String body;
	private boolean isBodyReleased;
	private Exception ex;
	
	/**
	 * Create a Response from an HttpURLConnection that should have been used to
	 * execute a Request, the body of the response, and a possible Exception
	 * that occurred while executing the Request.
	 * @param conn an HttpURLConnection that was used to make a request.
	 * @param body the response body.
	 * @param ex an Exception that was possibly caught while making a request.
	 */
	public Response(HttpURLConnection conn, String body, Exception ex) {
		this.conn = conn;
		this.body = body;
		this.isBodyReleased = false;
		this.ex = ex;
	}
	
	/**
	 * Returns the response body.
	 * @return the response body.
	 */
	public String getBody() {
		return body;
	}
	
	/**
	 * Releases the internal reference to the body held by this class.
	 */
	public void releaseBody() {
		isBodyReleased = true;
		body = null;
	}
	
	/**
	 * Returns whether or not releaseBody() has been called on this object.
	 * @return true if releaseBody() has been called, false otherwise.
	 */
	public boolean isBodyReleased() {
		return isBodyReleased;
	}
	
	/**
	 * Returns whether or not this Response has an Exception associated with it.
	 * @return true if an Exception is associated with this object, false
	 * otherwise.
	 */
	public boolean hasException() {
		return ex != null;
	}

	/**
	 * Returns the Exception associated with this Response.
	 * @return the Exception associated with this Response, or null if it does
	 * not exist.
	 */
	public Exception getException() {
		return ex;
	}
	
	/**
	 * Allows a caller to set an Exception on this Response.
	 * @param ex the new Exception.
	 * @return the previously held Exception, or null if it does not exist.
	 */
	public Exception setException(Exception ex) {
		Exception old = this.ex;
		this.ex = ex;
		return old;
	}
	
	/**
	 * Returns the response code of the Response.
	 * @return the response code, or -1 if it not possible to retrieve.
	 */
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
	
	/**
	 * Returns an immutable map of headers of this Response.
	 * @return an immutable map of headers of this Response, or null if it is
	 * not possible to retrieve the headers.
	 */
	public Map<String, List<String>> getHeaderFields() {
		if(conn == null) {
			return null;
		}
		return conn.getHeaderFields();
	}
	
	/**
	 * Returns the header value mapped by key.
	 * @param key the header field name for which to return its value.
	 * @return the header mapped by key, or null if there is no field with this
	 * name or it is not possible to retrieve the header value.
	 */
	public String getHeaderField(String key) {
		if(conn == null) {
			return null;
		}
		return conn.getHeaderField(key);
	}
	
	@Override
	public String toString() {
		return body + " " + ex;
	}
}
