package com.ericelsken.android.web;

import android.util.SparseArray;

public class RequestManager {
	
	private static class InstanceHolder {
		private static final RequestManager sInstance = new RequestManager();
	}
	
	public static RequestManager getInstance() {
		return InstanceHolder.sInstance;
	}
	
	private final SparseArray<RequestHandler> mArray;
	private ExceptionHandler mExceptionHandler;
	
	private RequestManager() {
		mArray = new SparseArray<RequestHandler>();
		mExceptionHandler = new DefaultExceptionHandler();
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
	
	public void setCallback(int id, RequestCallback callback) {
		RequestHandler rh = mArray.get(id);
		if(rh != null) {
			rh.setRequestCallback(callback);
		}
	}
}
