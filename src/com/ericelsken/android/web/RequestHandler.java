package com.ericelsken.android.web;

import android.content.Context;
import android.os.AsyncTask;

/**
 * This class handles the Request life-cycle for a given, single Request object.
 * Please see the RequestCallback documentation for a description of the life-
 * cycle. The Context supplied to the constructor will likely be an Activity
 * from which the Request is being made. The id supplied should be unique across
 * the entire application and not the Context to ensure proper handling in your
 * callbacks. Otherwise, different contexts with conflicting id's could
 * possibly mis-handle the callback.
 * 
 * This class uses an AsyncTask to manage the networking and to call the 
 * appropriate callbacks on the given RequestCallback instance. In
 * AsyncTask.onPreExecute(), the RequestCallback.onBeforeRequest() is called.
 * Request.execute() is called in AsyncTask.doInBackground(). And the final
 * four callbacks are all received from AsyncTask.onPostExecute().
 * 
 * This class allows for a Request to be cancelled. Cancellation in this case
 * means that the AsyncTask being used is cancelled. But, because the actual
 * request being made to the remote service cannot be cancelled, this results in
 * a RequestCancelledException being sent to
 * RequestCallback.onRequestException() even though the request may have
 * completed successfully. So, if your Request has some sort of side effects
 * or state change in your service, it is recommended to check the result of the
 * Response in all cases or not cancel a Request.
 * 
 * You can get the fields supplied to this class when constructed for use in
 * callbacks if need be. Additionally, you can set the ExceptionHandler of this 
 * handler at any time during the callback life-cycle as long as the call is
 * made on the main-UI thread. 
 * This could be important if you want a specific type of
 * Exception handling to occur for this Request that is different from the
 * default provided by RequestManager.getExceptionHandler().
 * The ExceptionHandler provided to this class will be called if and only if an
 * Exception is caught on the Request life-cycle, an Exception is thrown from
 * RequestCallback.onRequestSuccess(), and RequestCallback.onRequestException()
 * returns false.
 * 
 * @author Eric Elsken
 *
 */
public class RequestHandler {
	
	private final Context mContext;
	private final int mId;
	private final RequestManager mManager;
	private final RequestTask mTask;
	private RequestCallbacks mCallback;
	private ExceptionHandler mExceptionHandler;
	
	private final Request mReq;
	private Response mRes;
	
	/**
	 * Create a new RequestHandler that handles a given Request in the specified
	 * Context with the given id and RequestCallback. Note the id should be
	 * unique across the entire application and not just for the Context.
	 * The ExceptionHandler used by this instance will be
	 * RequestManager.getExceptionHandler() until it is set explicitly by
	 * setExceptionHandler().
	 * @param context the Context being used to make the Request. Will usually
	 * be an Activity making a call.
	 * @param id the id of this RequestHandler. This is how the RequestHandler
	 * is mapped by RequestManager and can be retrieved from RequestManager. 
	 * Additionally, this is the id parameter that is sent to all callbacks.
	 * @param req the Request to execute.
	 * @param callback the RequestCallback implementation to call as events
	 * occur in the Request life-cycle.
	 * @throws NullPointerException if either Request or RequestCallback is
	 * null.
	 */
	public RequestHandler(Context context, int id, Request req, RequestCallbacks callback) {
		if(context == null) {
			throw new NullPointerException("Context cannot be null.");
		}
		if(callback == null) {
			throw new NullPointerException("RequestCallback cannot be null.");
		}
		if(req == null) {
			throw new NullPointerException("Request cannot be null.");
		}
		this.mContext = context;
		this.mId = id;
		this.mManager = RequestManager.getInstance();
		this.mManager.addRequest(mId, RequestHandler.this);
		this.mTask = new RequestTask();
		this.mCallback = callback;
		this.mExceptionHandler = this.mManager.getExceptionHandler();
		mReq = req;
		mRes = null;
	}
	
	/**
	 * Sets the RequestCallback implementation that will receive future call on
	 * the Request life-cycle.
	 * @param rc the new ReuqetsCallback implementation.
	 */
	public void setRequestCallback(RequestCallbacks rc) {
		if(rc == null) {
			throw new NullPointerException(
					"RequestCallback parameter cannot be null.");
		}
		this.mCallback = rc;
	}
	
	/**
	 * Starts executing the AsyncTask/Request if it has not already been started
	 * or is not already completed.
	 */
	public void start() {
		if(mTask.getStatus() != AsyncTask.Status.RUNNING
				&& mTask.getStatus() != AsyncTask.Status.FINISHED) {
			mTask.execute();
		}
	}
	
	/**
	 * Attempts to cancel this Request. Note that this network call cannot
	 * actually be cancelled, so this only signals that a cancellation has
	 * occurred and results in a RequestCancelledException to be sent to
	 * RequestCallback.onRequestExeption().
	 * @return AsyncTask.cancel(true).
	 */
	public boolean cancel() {
		return mTask.cancel(true);
	}
	
	/**
	 * Attempt to handle an Exception caught somewhere in executing the Request.
	 */
	private void handleException() {
		//First attempt to handle with the callback.
		boolean handled = mCallback.onRequestException(mId, mRes);
		//If it was not handled by the callback, then attempt with the handler.
		if(!handled && mExceptionHandler != null) {
			handled = mExceptionHandler.handleException(mContext, mId, mRes.getException());
		}
	}
	
	/**
	 * Implementation of AsyncTask that executes the Request and calls the
	 * appropriate callbacks.
	 * 
	 * @author Eric Elsken
	 *
	 */
	private class RequestTask extends AsyncTask<Void, Void, Response> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mCallback.onBeforeRequest(mId);
		}

		@Override
		protected Response doInBackground(Void... params) {
			return mReq.execute();
		}
		
		@Override
		protected void onPostExecute(Response result) {
			super.onPostExecute(result);
			mRes = result;
			mCallback.onRequestDone(mId, false);
			if(mRes.hasException()) {
				handleException();
			} else {
				try {
					mCallback.onRequestSuccess(mId, mRes);
				} catch (Exception ex) {
					mRes.setException(ex);
					handleException();
				}
			}
			mCallback.onRequestFinally(mId, false);
			mManager.removeRequest(mId);
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			mCallback.onRequestDone(mId, true);
			mRes.setException(new RequestCancelledException(mId));
			handleException();
			mCallback.onRequestFinally(mId, true);
			mManager.removeRequest(mId);
		}
	}
	
	/**
	 * Returns the id of this handler.
	 * @return the id of this handler.
	 */
	public int getId() {
		return mId;
	}
	
	/**
	 * Returns the RequestCallback currently being used by this handler.
	 * @return the RequestCallback currently being used by this handler.
	 */
	public RequestCallbacks getCallback() {
		return mCallback;
	}

	/**
	 * Attempts to set the RequestCallback that will be called on future events
	 * in the Request life-cycle.
	 * @param callback the new RequestCallback instance.
	 * @throws NullPointerException if RequestCallback is null.
	 */
	public void setCallback(RequestCallbacks callback) {
		if(callback == null) {
			throw new NullPointerException("RequestCallback cannot be null.");
		}
		this.mCallback = callback;
	}

	/**
	 * Returns the Context supplied to this handler.
	 * @return the Context supplied to this handler.
	 */
	public Context getContext() {
		return mContext;
	}
	
	/**
	 * Returns the ExceptionHandler currently being used by this handler.
	 * @return the ExceptionHandler currently being used by this handler. A null
	 * value means that no Exception handling aside from
	 * RequestCallback.onRequestException() will occur.
	 */
	public ExceptionHandler getExceptionHandler() {
		return mExceptionHandler;
	}
	
	/**
	 * Sets the ExceptionHandler that will be called on future events in the
	 * Request life-cycle.
	 * @param handler the new ExceptionHandler.
	 */
	public void setExceptionHandler(ExceptionHandler handler) {
		mExceptionHandler = handler;
	}
}
