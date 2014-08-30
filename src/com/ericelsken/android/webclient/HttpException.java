package com.ericelsken.android.webclient;


public class HttpException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private final int mStatus;
	private final String mBody;

	public HttpException(int status, String body) {
		super("Status: " + status + ", Response: " + body);
		mStatus = status;
		mBody = body;
	}
	
	public int getStatusCode() {
		return mStatus;
	}
	
	public String getBody() {
		return mBody;
	}
}
