package com.ericelsken.android.web.content;

import java.net.URI;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.ericelsken.android.webclient.AndroidWebClient;
import com.ericelsken.android.webclient.RequestManager;
import com.ericelsken.android.webclient.WebClient;

public abstract class WebClientLoader<E> extends AsyncTaskLoader<E> {
	
	private final WebClient mWebClient;
	private final String mUri;
	private E mData;
	private Exception mException;

	public WebClientLoader(Context context, WebClient wc, String uri) {
		super(context);
		mWebClient = wc;
		mUri = uri;
	}
	
	public WebClientLoader(Context context, String uri) {
		this(context, RequestManager.getInstance().getWebClient(), uri);
	}
	
	public boolean hasException() {
		return mException != null;
	}
	
	public Exception getException() {
		return mException;
	}

	@Override
	/**
	 * Main method for the loading the data we want.
	 */
	public E loadInBackground() {
		try {
//			final String webResult = mWebClient.executeGet(mUri);
			final String webResult = AndroidWebClient.getInstance().executePut(URI.create("http://example.com"), "some data");
			E result = onUnmarshal(webResult);
			mException = null; //no errors. delivering the result.
			return result;
		} catch (Exception ex) {
			mException = ex;
			return null;
		}
	}

	@Override
	/**
	 * Called with data that was successfully loaded.
	 * Adds some logic to process the data over default implementation.
	 */
	public void deliverResult(E data) {
		if(isReset()) {
			//An async request came in while the loader was stopped, so we don't need the result.
			if(data != null) {
				onReleaseResources(data);
			}
		}
		E oldData = mData;
		mData = data;
		if(isStarted()) {
			//If the loader is currently started, we can immediately deliver the result.
			super.deliverResult(data);
		}
		//At this point we can release the old data since we have delivered the new data.
		if(oldData != null) {
			onReleaseResources(oldData);
		}
	}
	
	@Override
	/**
	 * Called when startLoading() is called on the Loader.
	 */
	protected void onStartLoading() {
		if(mData != null) {
			deliverResult(mData);
		}
		if(takeContentChanged() || mData == null) {
			//If the data has changed since last time or there is no data to provide, then force a load of data.
			forceLoad();
		}
	}
	
	@Override
	/**
	 * Called when stopLoading() is called on the Loader.
	 */
	protected void onStopLoading() {
		//Attempt to cancel the current load task, if possible.
		cancelLoad();
	}
	
	@Override
	/**
	 * Called with data that was obtained from a cancelled task.
	 */
	public void onCanceled(E data) {
		super.onCanceled(data);
		//The task that loaded this data was cancelled, we don't need it.
		if(data != null) {
			onReleaseResources(data);
		}
		mException = null;
	}
	
	@Override
	/**
	 * Handles a request to completely reset the loader.
	 */
	protected void onReset() {
		super.onReset();
		//Ensure that the loader is stopped.
		onStopLoading();
		//We can now release anything associated with data.
		if(mData != null) {
			onReleaseResources(mData);
		}
		mData = null;
		mException = null;
	}
	
	protected abstract E onUnmarshal(String value) throws Exception;
	protected abstract void onReleaseResources(E value);
}
