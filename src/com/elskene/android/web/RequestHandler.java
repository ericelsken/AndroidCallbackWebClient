package com.elskene.android.web;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

public class RequestHandler {
	
	public static final int DELETE = 0;
	public static final int GET = 1;
	public static final int POST = 2;
	public static final int PUT = 3;
	
	private final int id;
	private final Context context;
	private final RequestManager manager;
	private final WebClient webClient;
	private final RequestTask task;
	private final ExceptionHandler exceptionHandler;
	private final String uri;
	private final int method;
	private final JSONObject data;
	private RequestCallback callback;
	
	public RequestHandler(int id, Context context, RequestManager manager, WebClient webClient, ExceptionHandler exceptionHandler,
			String uri, int method, JSONObject data, RequestCallback callback) {
		this.id = id;
		this.context = context;
		this.manager = manager;
		this.webClient = webClient;
		this.task = new RequestTask();
		this.exceptionHandler = exceptionHandler;
		this.uri = uri;
		this.method = method;
		this.data = data;
		this.callback = callback;
	}
	
	public void setRequestCallback(RequestCallback callback) {
		this.callback = callback;
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
		boolean handled = callback.onRequestException(id, ex);
		if(!handled) {
			if(exceptionHandler != null) {
				exceptionHandler.handleException(id, ex, context);
			}
		}
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
			if(result == null || result instanceof JSONObject) {
				callback.onRequestSuccess(id, (JSONObject) result);
			} else if(result instanceof Exception) {
				handleException((Exception) result);
			} else {
				handleException(new IllegalArgumentException("Response must be an instance of either " + JSONObject.class.getName() + " or Exception."));
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
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @return the method
	 */
	public int getMethod() {
		return method;
	}

	/**
	 * @return the callback
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
	 * @return the webClient
	 */
	public WebClient getWebClient() {
		return webClient;
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
	public JSONObject getData() {
		return data;
	}
	
	
}
