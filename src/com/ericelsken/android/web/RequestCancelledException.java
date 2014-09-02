package com.ericelsken.android.web;

/**
 * Instances of this class indicate that a Request was cancelled before the
 * execution completed. 
 * Instances of this class will be sent to RequestCallback.onRequestException()
 * if the Request was cancelled. Note that RequestCallback.onRequestSuccess()
 * will not be called in this case.
 * 
 * @author Eric Elsken
 *
 */
public class RequestCancelledException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private final int mId;
	
	/**
	 * Create an instance of this class that references a RequestHandler by its
	 * id.
	 * @param id the id of the RequestHandler that was cancelled.
	 */
	public RequestCancelledException(int id) {
		super("Request with id " + id + " was cancelled");
		mId = id;
	}
	
	/**
	 * Returns the id of the RequestHandler that was cancelled.
	 * @return the id of the RequestHandler that was cancelled.
	 */
	public int getId() {
		return mId;
	}
}
