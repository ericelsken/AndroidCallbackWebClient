package com.ericelsken.android.web.content;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.ericelsken.android.web.Request;
import com.ericelsken.android.web.Response;

/**
 * An implementation of AsyncTaskLoader that loads a Response by executing
 * a given Request. 
 * 
 * @author Eric Elsken
 *
 */
public class ResponseLoader extends AsyncTaskLoader<Response> {
	
	/**
	 * The Request to execute.
	 */
	private final Request mReq;
	
	/**
	 * The Response received from mReq.execute().
	 */
	private Response mRes;
	
	/**
	 * Creates a new Loader that loads a Response from the given Request in
	 * the given Context.
	 * @param context the Context in which to load.
	 * @param req the Request to execute.
	 */
	public ResponseLoader(Context context, Request req) {
		super(context);
		if(req == null) {
			throw new NullPointerException("Request cannot be null.");
		}
		mReq = req;
	}
	
	protected Response getResponse() {
		return mRes;
	}

	/**
	 * Worker method for executing the Request.
	 */
	@Override
	public Response loadInBackground() {
		Response res = mReq.execute();
		return res;
	}

	/**
	 * Called with data that was successfully loaded.
	 * Adds some logic to process the data over default implementation.
	 */
	@Override
	public void deliverResult(Response data) {
		if(isReset()) {
			//An async request came in while the loader was stopped, so we
			//don't need the result.
			if(data != null) {
				onReleaseResources(data);
			}
		}
		Response oldData = mRes;
		mRes = data;
		if(isStarted()) {
			//If the loader is currently started, we can immediately deliver
			//the result.
			super.deliverResult(data);
		}
		//At this point we can release the old data since we have delivered
		//the new data.
		if(oldData != null) {
			onReleaseResources(oldData);
		}
	}
	
	/**
	 * Called when startLoading() is called on the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if(mRes != null) {
			deliverResult(mRes);
		}
		if(takeContentChanged() || mRes == null) {
			//If the data has changed since last time or there is no data to
			//provide, then force a load of data.
			forceLoad();
		}
	}
	
	/**
	 * Called when stopLoading() is called on the Loader.
	 */
	@Override
	protected void onStopLoading() {
		//Attempt to cancel the current load task, if possible.
		cancelLoad();
	}
	
	/**
	 * Called with data that was obtained from a cancelled task.
	 */
	@Override
	public void onCanceled(Response data) {
		super.onCanceled(data);
		//The task that loaded this data was cancelled, we don't need it.
		if(data != null) {
			onReleaseResources(data);
		}
	}
	
	/**
	 * Handles a request to completely reset the loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();
		//Ensure that the loader is stopped.
		onStopLoading();
		//We can now release anything associated with data.
		if(mRes != null) {
			onReleaseResources(mRes);
		}
		mRes = null;
	}
	
	protected void onReleaseResources(Response res) {
		//Do nothing. Leave here for future development.
	}
}
