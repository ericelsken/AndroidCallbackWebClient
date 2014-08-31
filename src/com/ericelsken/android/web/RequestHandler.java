package com.ericelsken.android.web;

import java.net.URI;

import android.content.Context;
import android.os.AsyncTask;

public class RequestHandler {
	
	public static final int DELETE = 0;
	public static final int GET = 1;
	public static final int POST = 2;
	public static final int PUT = 3;
	
	private final Context mContext;
	private final int mId;
	private final RequestManager mManager;
	private final RequestTask mTask;
	private final URI mURI;
	private RequestCallback mCallback;

	//Now begins the fields that can be set after object creation.
	private ExceptionHandler mExceptionHandler;
	private int mMethod;
	private String mData;
	
	private String mResponseBody;
	
	public RequestHandler(Context context, int id, URI uri, RequestCallback callback) {
		if(callback == null) {
			throw new NullPointerException("RequestCallback cannot be null.");
		}
		if(uri == null) {
			throw new NullPointerException("URI cannot be null.");
		}
		this.mContext = context;
		this.mId = id;
		this.mManager = RequestManager.getInstance();
		this.mTask = new RequestTask();
		this.mCallback = callback;
		
		this.mExceptionHandler = this.mManager.getExceptionHandler();
		this.mURI = uri;
		this.mMethod = GET;
		this.mData = null;
		this.mResponseBody = null;
	}
	
	public void setRequestCallback(RequestCallback rc) {
		if(rc == null) {
			throw new NullPointerException("RequestCallback parameter cannot be null.");
		}
		this.mCallback = rc;
	}
	
	public void start() {
		if(mTask.getStatus() != AsyncTask.Status.RUNNING && mTask.getStatus() != AsyncTask.Status.FINISHED) {
			mTask.execute();
		}
	}
	
	public boolean cancel() {
		return mTask.cancel(true);
	}
	
	private void handleException(Exception ex) {
		boolean handled = false;
		if(mExceptionHandler != null) {
			handled = mExceptionHandler.handleException(mContext, mId, ex);
		}
		mCallback.onRequestException(mId, ex, handled);
	}

	private class RequestTask extends AsyncTask<Void, Void, Object> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mCallback.onBeforeRequest(mId);
		}

		@Override
		protected Object doInBackground(Void... params) {
			Object result = null;
			final WebClient webClient = WebClient.getInstance();
			try {
				switch (mMethod) {
				case DELETE : result = webClient.executeDelete(mURI); break;
				case GET : result = webClient.executeGet(mURI); break;
				case POST : result = webClient.executePost(mURI, mData); break;
				case PUT : result = webClient.executePut(mURI, mData); break;
				}
			} catch (Exception ex) {
				result = ex;
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			mCallback.onRequestDone(mId, false);
			if(result == null || result instanceof String) {
				mResponseBody = (String) result;
				try {
					mCallback.onRequestSuccess(mId, mResponseBody);
				} catch (Exception ex) {
					handleException(ex);
				}
			} else if(result instanceof Exception) {
				handleException((Exception) result);
			} else {
				handleException(new IllegalArgumentException("Response must be an instance of either " + String.class.getName() + " or Exception."));
			}
			mCallback.onRequestFinally(mId, false);
			mManager.removeRequest(mId);
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			mCallback.onRequestDone(mId, true);
			handleException(new RequestCancelledException(mId));
			mCallback.onRequestFinally(mId, true);
			mManager.removeRequest(mId);
		}
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return mId;
	}

	/**
	 * @return the URI of the request
	 */
	public URI getUri() {
		return mURI;
	}

	/**
	 * @return the method
	 */
	public int getMethod() {
		return mMethod;
	}

	/**
	 * @return the RequestCallback
	 */
	public RequestCallback getCallback() {
		return mCallback;
	}

	/**
	 * @param callback the callback to set
	 */
	public void setCallback(RequestCallback callback) {
		this.mCallback = callback;
	}

	/**
	 * @return the context
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * @return the exceptionHandler
	 */
	public ExceptionHandler getExceptionHandler() {
		return mExceptionHandler;
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return mData;
	}
	
	public String getResponseBody() {
		return mResponseBody;
	}
	
	public static class Builder {
		private final RequestHandler result;
		
		public Builder(Context ctx, int id, URI uri, RequestCallback rc) {
			result = new RequestHandler(ctx, id, uri, rc);
		}
		
		public Builder delete() {
			result.mMethod = DELETE;
			return this;
		}
		
		public Builder get() {
			result.mMethod = GET;
			return this;
		}
		
		public Builder post() {
			result.mMethod = POST;
			return this;
		}
		
		public Builder put() {
			result.mMethod = PUT;
			return this;
		}
		
		public Builder setData(String data) {
			result.mData = data;
			return this;
		}
		
		public Builder setExceptionHandler(ExceptionHandler handler) {
			result.mExceptionHandler = handler;
			return this;
		}
		
		public RequestHandler create() {
			return result;
		}
	}
}
