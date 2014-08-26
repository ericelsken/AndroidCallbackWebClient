package com.ericelsken.android.jsonclient;

import org.json.JSONObject;

public interface RequestCallback {
	
	public void onBeforeRequest(int id);
	
	public void onRequestDone(int id, boolean cancelled);
	
	public void onRequestSuccess(int id, JSONObject json);
	
	public boolean onRequestException(int id, Exception ex, boolean handled);
	
	public void onRequestFinally(int id, boolean cancelled);
}
