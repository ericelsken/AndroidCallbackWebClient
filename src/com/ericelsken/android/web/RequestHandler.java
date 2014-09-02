package com.ericelsken.android.web;

import android.content.Context;
import android.os.AsyncTask;

public class RequestHandler {
	
	private final Context mContext;
	private final int mId;
	private final RequestManager mManager;
	private final RequestTask mTask;
	private RequestCallback mCallback;
	private ExceptionHandler mExceptionHandler;
	
	private final Request mReq;
	private Response mRes;
	
	public RequestHandler(Context context, int id, Request req, RequestCallback callback) {
		if(callback == null) {
			throw new NullPointerException("RequestCallback cannot be null.");
		}
		if(req == null) {
			throw new NullPointerException("Request cannot be null.");
		}
		this.mContext = context;
		this.mId = id;
		this.mManager = RequestManager.getInstance();
		this.mTask = new RequestTask();
		this.mCallback = callback;
		this.mExceptionHandler = this.mManager.getExceptionHandler();
		mReq = req;
		mRes = null;
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
		boolean handled = mCallback.onRequestException(mId, ex);
		if(!handled && mExceptionHandler != null) {
			handled = mExceptionHandler.handleException(mContext, mId, ex);
		}
	}

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
				handleException(mRes.getException());
			} else {
				try {
					mCallback.onRequestSuccess(mId, mRes);
				} catch (Exception ex) {
					mRes.setException(ex);
					handleException(ex);
				}
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

	public int getId() {
		return mId;
	}

	public RequestCallback getCallback() {
		return mCallback;
	}

	public void setCallback(RequestCallback callback) {
		this.mCallback = callback;
	}

	public Context getContext() {
		return mContext;
	}
	
	public ExceptionHandler getExceptionHandler() {
		return mExceptionHandler;
	}
	
	public void setExceptionHandler(ExceptionHandler handler) {
		mExceptionHandler = handler;
	}
}
