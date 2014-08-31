package com.ericelsken.android.web;

import java.net.URI;

import android.content.Context;
import android.os.AsyncTask;

public class RequestHandler {
	
	public static final int DELETE = 0;
	public static final int GET = 1;
	public static final int POST = 2;
	public static final int PUT = 3;
	
	private final Context context;
	private final int id;
	private final RequestManager manager;
	private final RequestTask task;
	private final URI uri;
	private RequestCallback callback;

	//Now begins the fields that can be set after object creation.
	private ExceptionHandler exceptionHandler;
	private int method;
	private String data;
	
	private String responseBody;
	
	public RequestHandler(Context context, int id, URI uri, RequestCallback callback) {
		if(callback == null) {
			throw new NullPointerException("RequestCallback cannot be null.");
		}
		if(uri == null) {
			throw new NullPointerException("URI cannot be null.");
		}
		this.context = context;
		this.id = id;
		this.manager = RequestManager.getInstance();
		this.task = new RequestTask();
		this.callback = callback;
		
		this.exceptionHandler = this.manager.getExceptionHandler();
		this.uri = uri;
		this.method = GET;
		this.data = null;
		this.responseBody = null;
	}
	
	public void setRequestCallback(RequestCallback rc) {
		if(rc == null) {
			throw new NullPointerException("RequestCallback parameter cannot be null.");
		}
		this.callback = rc;
	}
	
	public void start() {
		if(task.getStatus() != AsyncTask.Status.RUNNING && task.getStatus() != AsyncTask.Status.FINISHED) {
			task.execute();
		}
	}
	
	public boolean cancel() {
		return task.cancel(true);
	}
	
	private void handleException(Exception ex) {
		boolean handled = false;
		if(exceptionHandler != null) {
			handled = exceptionHandler.handleException(context, id, ex);
		}
		callback.onRequestException(id, ex, handled);
	}

	private class RequestTask extends AsyncTask<Void, Void, Object> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			callback.onBeforeRequest(id);
		}

		@Override
		protected Object doInBackground(Void... params) {
			Object result = null;
			final WebClient webClient = WebClient.getInstance();
			try {
				switch (method) {
				case DELETE : result = webClient.executeDelete(uri); break;
				case GET : result = webClient.executeGet(uri); break;
				case POST : result = webClient.executePost(uri, data); break;
				case PUT : result = webClient.executePut(uri, data); break;
				}
			} catch (Exception ex) {
				result = ex;
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			callback.onRequestDone(id, false);
			if(result == null || result instanceof String) {
				responseBody = (String) result;
				try {
					callback.onRequestSuccess(id, responseBody);
				} catch (Exception ex) {
					handleException(ex);
				}
			} else if(result instanceof Exception) {
				handleException((Exception) result);
			} else {
				handleException(new IllegalArgumentException("Response must be an instance of either " + String.class.getName() + " or Exception."));
			}
			callback.onRequestFinally(id, false);
			manager.removeRequest(id);
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			callback.onRequestDone(id, true);
			handleException(new RequestCancelledException(id));
			callback.onRequestFinally(id, true);
			manager.removeRequest(id);
		}
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the URI of the request
	 */
	public URI getUri() {
		return uri;
	}

	/**
	 * @return the method
	 */
	public int getMethod() {
		return method;
	}

	/**
	 * @return the RequestCallback
	 */
	public RequestCallback getCallback() {
		return callback;
	}

	/**
	 * @param callback the callback to set
	 */
	public void setCallback(RequestCallback callback) {
		this.callback = callback;
	}

	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @return the exceptionHandler
	 */
	public ExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}
	
	public String getResponseBody() {
		return responseBody;
	}
	
	public static class Builder {
		private final RequestHandler result;
		
		public Builder(Context ctx, int id, URI uri, RequestCallback rc) {
			result = new RequestHandler(ctx, id, uri, rc);
		}
		
		public Builder delete() {
			result.method = DELETE;
			return this;
		}
		
		public Builder get() {
			result.method = GET;
			return this;
		}
		
		public Builder post() {
			result.method = POST;
			return this;
		}
		
		public Builder put() {
			result.method = PUT;
			return this;
		}
		
		public Builder setData(String data) {
			result.data = data;
			return this;
		}
		
		public Builder setExceptionHandler(ExceptionHandler handler) {
			result.exceptionHandler = handler;
			return this;
		}
		
		public RequestHandler create() {
			return result;
		}
	}
}
