package com.ericelsken.android.web;

/**
 * A representation of an HTTP Response that has a non 2xx status code.
 * The status code, status message, and body of the Response are available from this class.
 * 
 * @author Eric Elsken
 *
 */
public class HttpException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private final int mStatus;
	private final String mMessage;
	private final String mBody;

	/**
	 * Create an instance of this Exception.
	 * @param status the integer status code of the Response.
	 * @param message the status message of the Response, i.e. OK or Unauthorized.
	 * @param body the Response body.
	 */
	public HttpException(int status, String message, String body) {
		super("Status: " + status + ", Response: " + body);
		mStatus = status;
		mMessage = message;
		mBody = body;
	}
	
	/**
	 * Returns the status code of the Response represented by this Exception.
	 * @return the status code.
	 */
	public int getStatusCode() {
		return mStatus;
	}
	
	/**
	 * Returns the status message of the Response represented by this Exception
	 * @return the status message.
	 */
	public String getMessage() {
		return mMessage;
	}
	
	/**
	 * Returns the body of the Response represented by this Exception.
	 * @return the Response body.
	 */
	public String getBody() {
		return mBody;
	}
}
