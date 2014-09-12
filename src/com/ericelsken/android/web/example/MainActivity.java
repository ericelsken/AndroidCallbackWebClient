package com.ericelsken.android.web.example;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Toast;

import com.ericelsken.android.web.R;
import com.ericelsken.android.web.Request;
import com.ericelsken.android.web.RequestCallbacks;
import com.ericelsken.android.web.RequestHandler;
import com.ericelsken.android.web.RequestManager;
import com.ericelsken.android.web.Response;

public class MainActivity extends Activity implements OnClickListener,
		RequestCallbacks, LoaderCallbacks<Response> {
	
	static {
		//set simple cookie management for the entire process.
		CookieManager manager = new CookieManager();
		CookieHandler.setDefault(manager);
	}
	
	//request code values for request handling and loading.
	private static final int WRC_SUCCESS = 0;
	private static final int WRC_FAILURE = 1;
	private static final int LID_GET_EXAMPLE = 0;
	
	//the view into which to show loaded data.
	private WebView mWebView;
	
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.activity_main);
		
		defineViews();
		getLoaderManager().initLoader(LID_GET_EXAMPLE, null, this);
	}
	
	private void defineViews() {
		findViewById(R.id.button_request_success).setOnClickListener(this);
		findViewById(R.id.button_request_failure).setOnClickListener(this);
		mWebView = (WebView) findViewById(R.id.webview_client_lib);
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if(id == R.id.button_request_success) {
			//simple way to create a Request and handle it.
			if(!RequestManager.getInstance().containsRequest(WRC_SUCCESS)) {
				new Request.Builder(URI.create("http://www.example.com")).create().handle(this, WRC_SUCCESS, this);
			}
			return;
		}
		if(id == R.id.button_request_failure) {
			//more custom way to handle a request.
			Request.Builder builder = new Request.Builder(URI.create("http://www.example.com"));
			//add customizations to the Request here.
			builder.put();
			Request req = builder.create(); //create the Request.
			RequestHandler handler = new RequestHandler(this, WRC_FAILURE, req, this); //create a new handler for the Request.
			handler.start(); //start the handler.
			return;
		}
	}

	@Override
	public void onBeforeRequest(int id) {
		Toast.makeText(this, "onBeforeRequest() " + id, Toast.LENGTH_SHORT).show();
		//you could start a dialog here, to be dismissed in onRequestDone() or onRequestFinally().
	}

	@Override
	public void onRequestDone(int id, boolean cancelled) {
		Toast.makeText(this, "onRequestDone() " + id + " " + cancelled, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRequestSuccess(int id, Response res) throws Exception {
		Toast.makeText(this, "onRequestSuccess() " + id + " \n" + res, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onRequestException(int id, Response res) {
		int status = res.getStatusCode();
		Toast.makeText(this, "onRequestException() " + id + " " + status + " " + res.getException(), Toast.LENGTH_SHORT).show();
		return false;
	}

	@Override
	public void onRequestFinally(int id, boolean cancelled) {
		Toast.makeText(this, "onRequestFinally() " + id + " " + cancelled, Toast.LENGTH_SHORT).show();
	}

	@Override
	public Loader<Response> onCreateLoader(int id, Bundle args) {
		return new Request.Builder(URI.create("http://www.example.com")).create().newLoader(this);
		
//		//the following would also work...
//		Request.Builder builder = new Request.Builder(URI.create("http://www.example.com"));
//		//add customizations to the Request here.
//		Request req = builder.create();
//		return new ResponseLoader(this, req);
	}

	@Override
	public void onLoadFinished(Loader<Response> loader, Response data) {
		mWebView.loadData(data.getBody(), "text/html", null);
		if(data.hasException()) {
			Toast.makeText(this, data.getException().getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
	}

	@Override
	public void onLoaderReset(Loader<Response> loader) {
		mWebView.loadData("", "text/html", null);
	}
}
