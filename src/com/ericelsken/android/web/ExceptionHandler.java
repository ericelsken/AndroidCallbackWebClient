package com.ericelsken.android.web;

import android.content.Context;

/**
 * An interface that defines Exception handling throughout different parts of this library.
 * The Context given when handling (using a RequestHandler), the id of the request, and the Exception caught
 * while making the request are all available to do some processing of the Exception before any other classes
 * have a chance to interact with the Exception.
 * 
 * An example extension of this class would be to check for known app Exceptions that would all be handled the same
 * way, i.e. a credential error that requires the user to give a certain permission.
 * 
 * @author Eric Elsken
 *
 */
public interface ExceptionHandler {
	
	public boolean handleException(Context context, int id, Exception ex);
}
