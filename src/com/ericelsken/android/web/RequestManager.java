package com.ericelsken.android.web;

import android.util.SparseArray;

/**
 * This class is a Singleton implementation that manages all instances of
 * RequestHanlders that are used by an application. The default ExceptionHandler
 * used by the application can be set through this class.
 * Unless otherwise set, an instance of DefaultExceptionHandler is created and
 * used by all RequestHanlders when this class is first created.
 * 
 * All methods in this class MUST be called in the main-UI thread.
 * 
 * @author Eric Elsken
 *
 */
public class RequestManager {
	
	private static class InstanceHolder {
		private static final RequestManager sInstance = new RequestManager();
	}
	
	/**
	 * Returns the Singleton instance of this class.
	 * @return the Singleton instance of this class.
	 */
	public static RequestManager getInstance() {
		return InstanceHolder.sInstance;
	}
	
	private final SparseArray<RequestHandler> mArray;
	private ExceptionHandler mExceptionHandler;
	
	private RequestManager() {
		mArray = new SparseArray<RequestHandler>();
		mExceptionHandler = new DefaultExceptionHandler();
	}
	
	/**
	 * Sets the ExceptionHandler that will be used by all RequestHandlers
	 * created after this call returns.
	 * @param handler the new ExceptionHandler. A null value means no Exception
	 * handling aside from RequestCallback.onRequestException() will occur.
	 */
	public void setExceptionHandler(ExceptionHandler handler) {
		mExceptionHandler = handler;
	}
	
	/**
	 * Returns the current default ExceptionHandler. Note that this value could
	 * be used by custom-handled Requests or outside of the Request life-cycle.
	 * @return the current default ExceptionHandler implementation.
	 */
	public ExceptionHandler getExceptionHandler() {
		return mExceptionHandler;
	}
	
	/**
	 * Adds a RequestHandler to be managed by this object.
	 * If a RequestHandler is already mapped by id, then this is a no-op and
	 * false is returned. The same occurs if handler is null.
	 * @param id the id to be mapped to handler.
	 * @param handler the RequestHandler to be mapped by id.
	 * @return true if handler was successfully added, false otherwise.
	 */
	public boolean addRequest(int id, RequestHandler handler) {
		if(handler == null || containsRequest(id)) {
			return false;
		}
		mArray.append(id, handler);
		return true;
	}
	
	/**
	 * Returns whether or not the RequestHandler identified by id is currently
	 * being managed by this class.
	 * @param id the id of the RequestHandler to query for existence.
	 * @return true if the RequestHandler is currently being managed by this
	 * class, false otherwise.
	 */
	public boolean containsRequest(int id) {
		return mArray.indexOfKey(id) >= 0;
	}
	
	/**
	 * Returns the RequestHandler mapped by id if it exists.
	 * @param id the id of the RequestHandler to return.
	 * @return the RequestHandler mapped to by id if it exists, null otherwise.
	 */
	public RequestHandler getHandler(int id) {
		return mArray.get(id);
	}
	
	/**
	 * Utility function that attempts to cancel the RequestHandler identified by
	 * the given id. If the RequestHandler is not being managed by this class,
	 * then this is a no-op and false is returned.
	 * @param id the id used to identify the RequestHandler.
	 * @return RequestHandler.cancel() if the RequestHandler exists, false
	 * otherwise.
	 */
	public boolean cancelRequest(int id) {
		RequestHandler rh = mArray.get(id);
		if(rh != null) {
			return rh.cancel();
		}
		return false;
	}
	
	/**
	 * Removes the RequestHandler identified by id if it exists.
	 * @param id the id used to identify the RequestHandler.
	 */
	public void removeRequest(int id) {
		mArray.delete(id);
	}
	
	/**
	 * Sets the RequestCallback of a RequestHandler if it exists.
	 * This is a no-op is the RequestHandler does not exist.
	 * @param id the id used to identify the RequestHandler.
	 * @param callback the new RequestCallback to set on the RequestHandler.
	 */
	public void setCallback(int id, RequestCallbacks callback) {
		RequestHandler rh = mArray.get(id);
		if(rh != null) {
			rh.setRequestCallback(callback);
		}
	}
}
