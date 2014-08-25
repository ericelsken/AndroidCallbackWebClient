package com.ericelsken.android.jsonclient;

import org.json.JSONObject;

public interface IRequestDelegate {

	public void onBeforeRequest();
	
	public JSONObject executeRequest() throws Exception;
	
	public void onRequestDone(boolean cancelled);
	
	public void onRequestSuccess(JSONObject json);
	
	public boolean onRequestException(Exception ex);
	
	public void onRequestFinally(boolean cancelled);
}
