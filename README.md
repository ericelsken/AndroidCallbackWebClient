AndroidCallbackWebClient
========================

An Android web client for making simple HTTP requests using callbacks or a Loader.

For a general and simple example as to how to use this library, please see [MainActivity.java](https://github.com/ericelsken/AndroidCallbackWebClient/blob/master/src/com/ericelsken/android/web/example/MainActivity.java).
This class uses a Loader to load a get request (via the standard `LoaderCallbacks` interface) and provides both a simple and more custom way of using a `RequestHandler` to handle the Request callbacks.
If you clone this repository, it is a working application where `MainActivity` allows you to see the callbacks/loading in action.

###Using this repo as an Android library
This repo is intended to be used as a library for another Android application required to make HTTP requests.
It is recommended to clone this repo outside of your workspace (if using Eclipse), then import the project into your workspace.
To make this project a library (in Eclipse) click Project > Properties > Android (in left pane) > check the Is Library checkbox > OK.
To enable another project to use this library (in Eclipse) click Project > Properties > Android (in left pane) > Add (in the Library section) > select this project > OK > OK.

###The Request class
The [Request](https://github.com/ericelsken/AndroidCallbackWebClient/blob/master/src/com/ericelsken/android/web/Request.java) class represents some request to be made to a remote http or https server.
Most all of your interaction with this library will be through this class, or at the very least, involve this class.
Instances of this class cannot be instantiated directly, and are instead created by using the request.Builder class.
Currently, Requests have the capability of setting a method (DELETE, GET, POST, PUT), the destination URI, PUT and POST request bodies, and set a buffer size to use when writing and reading the request and response bodies, and setting headers for the request and retrieving them from Response objects.
More capability is excepted in the future.

Please see the `Request.Builder` documentation for how to create instances of this class.

All Requests manage cookies using the `CookieHandler` class and its subclasses.
Please see the `CookieHandler`, `CookieManager`, and `CookieStore` class documentation in the `java.net` package for details on how to use cookies with Requests.
For example, to enable simple cookie management for the entire application, the following line would suffice:
```java
CookieManager manager = new CookieManager();
CookieHandler.setDefault(manager);
```
The `MainActivity` class described above does this in a static initialization block.

The two main interactions of this class will be through `newLoader()` and `handle()`.
`newLoader()` creates and returns a new `Loader<Response>` that can be used in the typical fashion with a `LoaderManager`.
`handle()` creates, starts, and returns a new `RequestHandler` for use with a `RequestCallbacks` instance.

Note that the `execute()` method is where the networking occurs, and therefore CANNOT be called on the main-UI thread.
Both `ResponseLoader` and `RequestHandler` will manage this call for you.
If you wish to use this class outside of `ResponseLoader` or `RequestHandler`, then you must handle calling `execute()` on a thread other than the main-UI thread.
Multiple successive calls to `execute()` will return the same `Response` object that was returned upon the first call.
In essence, this class is meant to be used once to obtain one `Response` object.

###The RequestCallbacks interface and Request life-cycle
Very simply, the `RequestCallbacks` interface provides a contract for some implementation to receive callbacks from a `RequestHandler` during the life-cycle events of a `Request`.
In short, the callback implementation will receive the following callbacks from the events described below.
Creating and handling `RequestHandler` objects MUST be done on the main-UI thread.
The following callbacks will all be on the main-UI thread as well.

1. `onBeforeRequest()` is called just before the `Request` is executed on a background thread.

2. `onRequestDone()` is called when the `Request` is done executing in the background regardless of success or failure.

3. `onRequestSuccess()` is called after `onRequestDone()` if the `Request` has a 2xx status code and no other Exceptions occurred while making the `Request`.

4. `onRequestException()` is called after `onRequestDone()` if the `Request` has a non-2xx status code, an Exception occurs while making the `Request`, or an Exception is thrown from `onRequestSuccess()`.

5. `onRequestFinally()` is called after both `onRequestSuccess()` and `onRequestException()` return regardless of success or failure.

Please see the `RequestCallbacks` documentation for the full details of the interface.

###The UnmarshalingResponseLoader class
The [UnmarshalingResponseLoader](https://github.com/ericelsken/AndroidCallbackWebClient/blob/master/src/com/ericelsken/android/web/content/UnmarshalingResponseLoader.java) class is a utility class that allows for some processing to automatically take place on a Response once it is loaded by the superclass.
The `nmarshal()` method provided by this class should be implemented such that it converts the Response to some other form for use elsewhere in the application.
Note that the Response body will still be a string in memory after `unmarshal()` returns.
If this is a lot of data or it is no longer needed, it may be helpful to call `Response.releaseBody()` in order to release that reference to free up that memory.

Subclasses of this class should implement `onReleaseResources()` in order to release anything created in `unmarshal()`.
For instance, this could be closing a Cursor or something of that nature.

An example of this class would be to create a JSONObject from the `Response` body if you know the result of the `Request` is going to be some JSON content.
Then by calling `getData()`, that JSONObject is ready for use without further processing needed in the `onLoadFinished()` callback.
```java
public class JSONUnmarshalingLoader 
		extends UnmarshalingResponseLoader<JSONObject> {
	
	//constructor and other necessary stuff...
	
	@Override
	protected JSONObject unmarshal(Response res) throws Exception {
		if(res.getBody() == null) {
			return new JSONObject();
		}
		JSONObject json = new JSONObject(res.getBody());
		res.releaseBody();
		return json;
	}
}
```

Calling `getUnmarshalingException()` returns the Exception thrown from `unmarshal()`, if it exists.
This is to help differentiate the unmarshaling Exception from the Exception that could be held by the Response.

###Documentation
All code is documented such that you can use Javadoc to generate the documenation pages.
