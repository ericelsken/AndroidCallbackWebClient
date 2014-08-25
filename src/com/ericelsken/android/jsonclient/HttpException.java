package com.ericelsken.android.jsonclient;


public class HttpException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private final int mStatus;
	private final String mReason;
	private final String mBody;

	public HttpException(int status, String reason, String body) {
		super("Reason: " + reason + ", Status: " + status + ", Response: " + body);
		mStatus = status;
		mReason = reason;
		mBody = body;
	}
	
	public int getStatus() {
		return mStatus;
	}
	
	public String getReason() {
		return mReason;
	}
	
	public String getBody() {
		return mBody;
	}
	
	public boolean isServerError() {
		return mStatus / 100 == 5;
	}
	
	public boolean isClientError() {
		return mStatus / 100 == 4;
	}
	
	public boolean isUnauthorizedError() {
		return mStatus == 401;
	}
}
