package com.ericelsken.android.web.content;

import android.content.Context;

import com.ericelsken.android.web.Request;
import com.ericelsken.android.web.Response;

/**
 * A utility class that allows for some processing to automatically take place
 * on a Response once it is loaded by the superclass.
 * The unmarshal() method provided by this class should be implemented such that
 * it converts the Response to some other form for use elsewhere in the
 * application. Note that the Response body will still be a string in memory
 * after unmarshal() returns. If this is a lot of data or it no longer needed,
 * it may be helpful to call Response.releaseBody() in order to release that
 * reference to free up that memory.
 * 
 * Subclasses of this class should implement onReleaseResources() in order to
 * release anything created in unmarshal(). For instance, this could be closing
 * a Cursor or something of that nature.
 * 
 * An example of this would be to create a JSONObject from the Response body if
 * you know the result of the Request is going to be some JSON content.
 * Then by calling getData(), that JSONObject is ready for use without further
 * processing needed in the onLoadFinished() callback.
 * 
 * Calling getUnmarshalingException() returns the Exception thrown from
 * unmarshal(), if it exists. This is to help differentiate the unmarshaling
 * Exception from the Exception that could be held by the Response.
 * 
 * @author Eric Elsken
 *
 * @param <E> the type of data that is to be unmarshaled from a Response once
 * the Response is loaded.
 */
public abstract class UnmarshalingResponseLoader<E> extends ResponseLoader {
	
	private E mData;
	private Exception mUnmarshalingException;
	
	/**
	 * Creates a new Loader that loads a Response from the given Request in
	 * the given Context.
	 * @param context the Context in which to load.
	 * @param req the Request to execute.
	 */
	public UnmarshalingResponseLoader(Context context, Request req) {
		super(context, req);
		mData = null;
		mUnmarshalingException = null;
	}
	
	/**
	 * Worker method for the loading the data we want.
	 */
	@Override
	public Response loadInBackground() {
		super.loadInBackground();
		try {
			mData = unmarshal(mRes);
		} catch (Exception ex) {
			mUnmarshalingException = ex;
		}
		return mRes;
	}
	
	/**
	 * Returns the data that was unmarshaled by the call to unmarshal().
	 * @return the data that was unmarshaled.
	 */
	public final E getData() {
		return mData;
	}
	
	/**
	 * Returns the Exception thrown by unmarshal() or null if no Exception was
	 * thrown.
	 * @return the Exception thrown by unmarshal() or null if no Exception was
	 * thrown.
	 */
	public final boolean hasUnmarshalingException() {
		return mUnmarshalingException != null;
	}
	
	/**
	 * Returns whether or not an Exception was thrown by unmarshal().
	 * @return true if an Exception was thrown by unmarshal(), false otherwise.
	 */
	public final Exception getUnmarshalingException() {
		return mUnmarshalingException;
	}
	
	/**
	 * Nullifies mData and mUnmarshalingException.
	 */
	@Override
	protected void onReset() {
		super.onReset();
		mData = null;
		mUnmarshalingException = null;
	}

	/**
	 * Called when the Response needs to be unmarshaled.
	 * IMPORTANT: This method is called on the background thread used by the
	 * superclass.
	 * @param res the Response that was loaded that needs to be unmarshaled.
	 * @return the unmarshaled data.
	 * @throws Exception if an Exception occurs while unmarshaling the Response.
	 */
	protected abstract E unmarshal(Response res) throws Exception;
}
