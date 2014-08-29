package com.ericelsken.android.jsonclient;


public interface RequestCallback {
	
	public void onBeforeRequest(int id);
	
	public void onRequestDone(int id, boolean cancelled);
	
	public void onRequestSuccess(int id, String body) throws Exception;
	
	public boolean onRequestException(int id, Exception ex, boolean handled);
	
	public void onRequestFinally(int id, boolean cancelled);
}
