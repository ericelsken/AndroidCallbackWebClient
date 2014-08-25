package com.elskene.android.web;

import org.json.JSONObject;

import android.content.Context;
import android.util.SparseArray;

public class RequestManager {
	
	private static class InstanceHolder {
		private static final RequestManager sInstance = new RequestManager();
	}
	
	public static RequestManager getInstance() {
		return InstanceHolder.sInstance;
	}
	
	private final WebClient mWebClient;
	private final SparseArray<RequestHandler> mArray;
	private ExceptionHandler mExceptionHandler;
	
	private RequestManager() {
		mWebClient = new WebClient();
		mArray = new SparseArray<RequestHandler>();
		mExceptionHandler = new DefaultExceptionHandler();
	}
	
	public WebClient getWebClient() {
		return mWebClient;
	}
	
	public void setExceptionHandler(ExceptionHandler handler) {
		mExceptionHandler = handler == null ? new DefaultExceptionHandler() : handler;
	}
	
	public ExceptionHandler getExceptionHandler() {
		return mExceptionHandler;
	}
	
	public boolean containsRequest(int id) {
		return mArray.indexOfKey(id) >= 0;
	}
	
	public boolean cancelRequest(int id) {
		RequestHandler rh = mArray.get(id);
		if(rh != null) {
			return rh.cancel();
		}
		return false;
	}
	
	public void removeRequest(int id) {
		mArray.delete(id);
	}
	
	public RequestHandler createDeleteRequest(int id, Context context, String uri, RequestCallback callback) {
		return createRequest(id, context, uri, RequestHandler.DELETE, null, callback);
	}
	
	public RequestHandler createGetRequest(int id, Context context, String uri, RequestCallback callback) {
		return createRequest(id, context, uri, RequestHandler.GET, null, callback);
	}
	
	public RequestHandler createPostRequest(int id, Context context, String uri, JSONObject json, RequestCallback callback) {
		return createRequest(id, context, uri, RequestHandler.POST, json, callback);
	}
	
	public RequestHandler createPutRequest(int id, Context context, String uri, JSONObject json, RequestCallback callback) {
		return createRequest(id, context, uri, RequestHandler.PUT, json, callback);
	}
	
	private RequestHandler createRequest(int id, Context context, String uri, int method, JSONObject json, RequestCallback callback) {
		RequestHandler rh = mArray.get(id);
		if(rh != null) {
			return rh;
		}
		rh = new RequestHandler(id, context, this, mWebClient, mExceptionHandler, uri, method, json, callback);
		mArray.append(id, rh);
		return rh;
	}
	
	public void setCallback(int id, RequestCallback callback) {
		RequestHandler rh = mArray.get(id);
		if(rh != null) {
			rh.setRequestCallback(callback);
		}
	}
}
