package com.ericelsken.android.web;


public class HttpException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private final int mStatus;
	private final String mMessage;
	private final String mBody;

	public HttpException(int status, String message, String body) {
		super("Status: " + status + ", Response: " + body);
		mStatus = status;
		mMessage = message;
		mBody = body;
	}
	
	public int getStatusCode() {
		return mStatus;
	}
	
	public String getMessage() {
		return mMessage;
	}
	
	public String getBody() {
		return mBody;
	}
}
