package com.ericelsken.android.web;

import android.content.Context;
import android.util.Log;

/**
 * This class is a simple implementation of ExceptionHandler that simply logs an Exception and returns
 * false to indicate that the Exception was not handled, allowing for other parts of this library
 * to handle the Exception.
 * 
 * @author Eric Elsken
 *
 */
public class DefaultExceptionHandler implements ExceptionHandler {
	
	/**
	 * The log tag to use when logging from this class.
	 */
	private static final String TAG	= DefaultExceptionHandler.class.getSimpleName();
	
	/**
	 * Logs the Exception and returns false.
	 */
	@Override
	public boolean handleException(Context context, int id, Exception ex) {
		Log.e(TAG, "Exception not handled (logged by default) with request id " + id, ex);
		return false;
	}

}
