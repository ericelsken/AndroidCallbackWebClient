package com.ericelsken.android.web;

/**
 * Defines a callback contract used by RequestHandlers to signal the caller when events on the Request life-cycle occur.
 * All callbacks received by implementations of this interface will be received on the thread that RequestHander.start()
 * was called on. This thread MUST always be the main-UI thread of the app.
 * Note that all callbacks receive the id of the Request given to the RequestHandler handling the Request.
 * 
 * The life-cycle for Requests is as follows:
 * 1. onBeforeRequest() is called before any background or networking actions are taken. Examples of this callback are
 * 	to show a dialog indicating the Request is taking place, to be cancelled in onRequestDone() or onRequestFinally().
 * 2. onRequestDone() is called when the Request completes, whether or not an Exception or a successful Response
 * 	was obtained. The cancelled parameter indicates if the Request was cancelled by a call to RequestHandler.cancel()
 * 	between callbacks onBeforeRequest() and onRequestDone().
 * 3. onRequestSuccess() is called if the Request completes without an Exception being caught.
 * 	A success is considered to be any Response with status code 2xx. Any other status code will result in an HttpException
 * 	being caught and sent to onRequestException() (See callback 4.)
 * 	Note that this callback WILL NOT be called if Response.hasException() returns true. Thus res.hasException() is
 * 	guaranteed to return false in this method.
 * 	Note also that this method throws an Exception. A Response may not have an Exception, but an Exception could be thrown
 * 	somewhere inside this method while processing the Response. In this case, you may throw that Exception. This results
 * 	in Response.hasException() to return true, Response.getException() to return the Exception thrown, and
 * 	onRequestException() to be called with the thrown Exception.
 * 4. onRequestException() is called if an Exception is caught while e2xecuteing the Request or if one is thrown from
 * 	onRequestSuccess(). Note that only one of onRequestSuccess() or onRequestException() will be called unless an Exception
 * 	is thrown from onRequestSuccess(). 
 * 	The return value of this method indicates whether or not this method properly and completely handled this Exception.
 * 	A return value of false will stop processing of the Exception. A return value of true will notify the RequestHandler
 * 	to allow the ExceptionHandler of the RequestHandler to process the Exception. After the ExceptionHandler returns from
 * 	its processing, no further action is taken with the Exception.
 * 	If the Request was cancelled, onRequestSuccess() will not be called, and the Exception received by this callback
 * 	will be an instance of RequestCancelledException.
 * 	As long as you properly create the URI of a request and have access to make network calls, the exceptions
 * 	received in the method will mostly be instances of HttpException or ones thrown by onRequestSuccess().
 * 5. onRequestFinally() is called in all cases after the call(s) to onRequestSuccess() and onRequestException() return.
 * 
 * @author Eric Elsken
 *
 */
public interface RequestCallback {
	
	/**
	 * Called before background or network processing of the Request occurs.
	 * @param id the id given to the RequestHandler handling the Request.
	 */
	public void onBeforeRequest(int id);
	
	/**
	 * Called regardless of success or failure (Exception caught) when the Request completes.
	 * @param id the id given to the RequestHandler handling the Request.
	 * @param cancelled indicates whether or not the Request was cancelled.
	 */
	public void onRequestDone(int id, boolean cancelled);
	
	/**
	 * Called when the Request finishes with a 2xx status code.
	 * @param id the id given to the RequestHandler handling the Request.
	 * @param res the Response the completed successfully.
	 * @throws Exception an Exception that will be sent to onReuestException().
	 */
	public void onRequestSuccess(int id, Response res) throws Exception;
	
	/**
	 * Called when the Request finished with a non 2xx status code (thus ex will be an instance of HttpException)
	 * or when another Exception was caught while executing the Request.
	 * @param id the id given to the RequestHandler handling the Request.
	 * @param ex the Exception caught as result of executing the Request.
	 * @return true to indicate ex requires further processing by the RequestHandler's ExceptionHandler, false if not.
	 */
	public boolean onRequestException(int id, Exception ex);
	
	/**
	 * Called regardless of success or failure (Exception caught) when the Request completes and after call(s) to
	 * onRequestSuccess() and onRequestException() return.
	 * @param id the id given to the RequestHandler handling the Request.
	 * @param cancelled indicates whether or not the Request was cancelled.
	 */
	public void onRequestFinally(int id, boolean cancelled);
}
