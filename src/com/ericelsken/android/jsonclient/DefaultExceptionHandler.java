package com.ericelsken.android.jsonclient;

import android.content.Context;
import android.util.Log;

public class DefaultExceptionHandler implements ExceptionHandler {
	
	private static final String TAG	= DefaultExceptionHandler.class.getSimpleName();
	
	@Override
	public boolean handleException(Context context, int id, Exception ex) {
		Log.e(TAG, "Exception not handled (logged by default) with request id " + id, ex);
		return false;
	}

}
