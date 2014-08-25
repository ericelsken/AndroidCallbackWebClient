package com.ericelsken.android.jsonclient;

import org.json.JSONObject;

public class HttpException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private final int mStatus;
	private final String mReason;
	private final JSONObject mJson;

	public HttpException(int status, String reason, JSONObject json) {
		super("Reason: " + reason + " Status: " + status + " Response: " + json.toString());
		mStatus = status;
		mReason = reason;
		mJson = json;
	}
	
	public int getStatus() {
		return mStatus;
	}
	
	public String getReason() {
		return mReason;
	}
	
	public JSONObject getJsonResponse() {
		return mJson;
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
