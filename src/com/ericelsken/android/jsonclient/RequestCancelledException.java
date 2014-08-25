package com.ericelsken.android.jsonclient;

public class RequestCancelledException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private final int mId;
	
	public RequestCancelledException(int id) {
		super("Request with id " + id + " was cancelled");
		mId = id;
	}
	
	public int getId() {
		return mId;
	}
}
