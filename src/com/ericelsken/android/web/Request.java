package com.ericelsken.android.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.os.Build;

import com.ericelsken.android.web.content.ResponseLoader;

/**
 * This class represents some request to be made to a remote http or https server. Most all of your interaction with
 * this library will be through this class, or at the very least, involve this class.
 * Instance of this class cannot be instantiated directly, and are instead created by using the request.Builder class.
 * Currently, Requests have the capability of setting a method (DELETE, GET, POST, PUT), the destination URI,
 * PUT and POST request bodies, and set a buffer size to use when writing and reading the request and response bodies,
 * and setting headers for the request and retrieving them from Response objects.
 * More capability is excepted in the future.
 * 
 * Please see the Request.Builder documentation for how to create instances of this class.
 * 
 * All Requests manage cookies using the CookieHandler class and its subclasses. Please see the CookieHandler,
 * CookieManager, and CookieStore class documentation in the java.net package for details on how to use cookies with
 * Requests.
 * It is recommended to call CookkieHandler.setDefault(new CookieManager()) in some app one-time initialization
 * code to enable simple handling of cookies.
 * 
 * The two main interactions of this class will be through newLoader() and handle().
 * newLoader() creates and returns a new Loader<Response> that can be used in the typical fashion with a LoaderManager.
 * handle() creates, starts, and returns a new RequestHandler for use with a RequestCallback instance.
 * 
 * Note that the execute() method is where the networking occurs, and therefore CANNOT be called on the main-UI thread.
 * Both ResponseLoader and RequestHandler will manage this call for you. If you wish to use this class outside of 
 * ResponseLoader or RequestHandler, then you must handle calling execute() not on the main-UI thread.
 * Multiple successive calls to execute() will return the same Response object that was returned upon the first call.
 * In essence, this class is meant to used once to obtain on Response object.
 * 
 * @author Eric Elsken
 *
 */
public class Request {
	
	static {
		//Work around pre-Froyo bugs in HTTP connection reuse.
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}
	
	/**
	 * The default buffer size to use when reading/writing requests.
	 */
	public static final int DEFAULT_BUFFER_SIZE = 1 << 10;

	private static final int DELETE = 0;
	private static final int GET = 1;
	private static final int POST = 2;
	private static final int PUT = 3;
	
	/**
	 * All object fields of this class must be immutable or deep-copied into Request objects.
	 */
	private static class Params {
		private final URI uri;
		private String data;
		private int method;
		private int bufferSize;
		private List<String[]> headers;
		
		private Params(URI uri) {
			this.uri = uri;
			data = null;
			method = GET;
			bufferSize = DEFAULT_BUFFER_SIZE;
			headers = new LinkedList<String[]>();
		}
	}
	
	/**
	 * The Response obtained from this Request in execute().
	 */
	private Response response;
	
	//The following should be an exact copy of Params fields, all final, and all deep copied from a Params object.
	private final URI uri;
	private final String data;
	private final int method;
	private final int bufferSize;
	private final List<String[]> headers;
	
	/**
	 * Deep-copy all fields from p into this class' fields.
	 * @param p the Params object from which to copy values into this instance.
	 */
	private Request(Params p) {
		uri = p.uri;
		data = p.data;
		method = p.method;
		bufferSize = p.bufferSize;
		//deep-copy the headers.
		headers = new LinkedList<String[]>();
		for(String[] pair : p.headers) {
			headers.add(new String[] {pair[0], pair[1]});
		}
	}
	
	/**
	 * Creates, starts, and returns a new RequestHandler with the given id, Context, and RequestCallback.
	 * Creates the RequestHandler via new RequestHandler(context, id, this, callback), starts the handler, and returns it.
	 * @param context the Context of the RequestHandler.
	 * @param id the id of the RequestHandler.
	 * @param callback the RequestCallback object that receives callbacks from the RequestHandler.
	 * @return the new, started RequestHandler now handling this Request.
	 */
	public RequestHandler handle(Context context, int id, RequestCallbacks callback) {
		RequestHandler handler = new RequestHandler(context, id, this, callback);
		handler.start();
		return handler;
	}
	
	/**
	 * Creates and returns a new ResponseLoader that loads its Response from this Request.
	 * @param context the Context of the ResponseLoader.
	 * @return the newly created ResponseLoader.
	 */
	public ResponseLoader newLoader(Context context) {
		ResponseLoader loader = new ResponseLoader(context, this);
		return loader;
	}
	
	/**
	 * Executes this Request and returns the Response object obtained from the Request.
	 * This method makes networking calls and thus CANNOT be called on the main-UI thread.
	 * Multiple, successive calls to this method on the same instance will return the Response from the first call.
	 * @return the Response object obtained from executing this Request.
	 */
	public Response execute() {
		if(response != null) {
			return response;
		}
		HttpURLConnection conn = null;
		String body = null;
		Exception caught = null;
		try {
			if(uri.getScheme() == null || uri.getScheme().indexOf("http") != 0) {
				throw new SchemeException(uri.getScheme());
			}
			URL url = uri.toURL();
			conn = (HttpURLConnection) url.openConnection();
			body = executeForBody(conn);
		} catch (Exception ex) {
			caught = ex;
		}
		response = new Response(conn, body, caught);
		return response;
	}
	
	/**
	 * Actually makes the networking request and returns the response body returned from the request.
	 * @param conn connection used in making the request.
	 * @return the response body returned from the request.
	 * @throws IOException if an IO problem occurs while making the request.
	 * @throws HttpException if the response status code is not 2xx.
	 */
	private String executeForBody(HttpURLConnection conn) throws IOException, HttpException {
		String body = null;
		try {
			apply(conn);
			if(data != null) {
				writeData(conn);
			}
			body = inputToString(conn);
		}
		finally {
			conn.disconnect();
		}
		int statusCode = conn.getResponseCode();
		if(statusCode / 100 != 2) {
			throw new HttpException(statusCode, conn.getResponseMessage(), body);
		}
		return body;
	}
	
	/**
	 * Applies the settings of this request to the connection being used to make the request.
	 * @param conn connection used in making the request.
	 * @throws ProtocolException see ProtocolException documentation.
	 */
	private void apply(HttpURLConnection conn) throws ProtocolException {
		switch(method) {
		case DELETE: conn.setRequestMethod("DELETE"); break;
		case GET: conn.setRequestMethod("GET"); break;
		case POST: conn.setRequestMethod("POST"); break;
		case PUT: conn.setRequestMethod("PUT"); break;
		}
		for(String[] pair : headers) {
			conn.setRequestProperty(pair[0], pair[1]);
		}
	}
	
	private String inputToString(HttpURLConnection conn) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(bufferSize);
		InputStream in = new BufferedInputStream(conn.getInputStream(), bufferSize);
		copyStreams(in, out);
		return out.toString();
	}

	private void writeData(HttpURLConnection conn) throws IOException {
		conn.setDoOutput(true);
		byte[] bytes = data.getBytes();
		conn.setFixedLengthStreamingMode(bytes.length);
		OutputStream out = new BufferedOutputStream(conn.getOutputStream(), bufferSize);
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(bytes), bufferSize);
		copyStreams(in, out);
	}

	private void copyStreams(InputStream in, OutputStream out) throws IOException {
		int tempRead = 0;
		byte[] buffer = new byte[bufferSize];
		while(tempRead != -1) {
			tempRead = in.read(buffer, 0, buffer.length);
			if(tempRead != -1) {
				out.write(buffer, 0, tempRead);
			}
		}
		in.close();
		out.close();
	}
	
	/**
	 * Utility class for building Request objects.
	 * All Requests must be created through this class.
	 * All calls on this class will reflect the settings of the Request object
	 * returned by create().
	 * All methods of this class return a reference to this so that method call
	 * chaining is easy.
	 * 
	 * @author Eric Elsken
	 *
	 */
	public static class Builder {
		
		private final Params p;
		
		/**
		 * Creates a new Builder with the provided URI.
		 * Without any further calls, the built Request will be a GET request
		 * to the given URI with the default buffer size and no headers.
		 * @param uri the URI of the Request.
		 */
		public Builder(URI uri) {
			p = new Params(uri);
		}
		
		/**
		 * Sets the built Request to be a DELETE Request.
		 * @return this
		 */
		public Builder delete() {
			p.method = DELETE;
			return this;
		}
		
		/**
		 * Sets the built Request to be a GET Request.
		 * If there is any data already associated with the Request, then it is
		 * removed.
		 * @return this
		 */
		public Builder get() {
			p.method = GET;
			p.data = null;
			return this;
		}
		
		/**
		 * Sets the built Request to be a POST Request.
		 * @return this
		 */
		public Builder post() {
			p.method = POST;
			return this;
		}
		
		/**
		 * Sets the built Request to be a PUT Request.
		 * @return this
		 */
		public Builder put() {
			p.method = PUT;
			return this;
		}
		
		/**
		 * Sets the data to be sent in the Request for DELETE, POST, and PUT
		 * requests.
		 * @param data the data to send as part of the Request.
		 * @return this
		 */
		public Builder setData(String data) {
			p.data = data;
			return this;
		}
		
		/**
		 * Sets the size of the buffer to use when writing/reading the Request/
		 * Response.
		 * @param bufferSize the new buffer size. If negative or zero, this is
		 * a no-op.
		 * @return this
		 */
		public Builder setBufferSize(int bufferSize) {
			bufferSize = bufferSize <= 0 ? p.bufferSize : bufferSize;
			p.bufferSize = bufferSize;
			return this;
		}
		
		/**
		 * Sets the header value to be associated with the field name.
		 * @param field the name of the header.
		 * @param newValue the new value of the header to be associated with field.
		 * @return this.
		 */
		public Builder setHeader(String field, String newValue) {
			if(field == null || newValue == null) {
				throw new NullPointerException("field and newValue cannot be null.");
			}
			p.headers.add(new String[] {field, newValue});
			return this;
		}
		
		/**
		 * Creates and returns a new Request with the settings provided by all
		 * the calls on this object.
		 * @return the newly created Request.
		 */
		public Request create() {
			return new Request(p);
		}
	}
}
