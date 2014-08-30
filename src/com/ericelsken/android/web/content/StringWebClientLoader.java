package com.ericelsken.android.web.content;

import com.ericelsken.android.webclient.WebClient;

import android.content.Context;

public class StringWebClientLoader extends WebClientLoader<String> {
	
	public StringWebClientLoader(Context context, String uri) {
		super(context, uri);
	}
	
	public StringWebClientLoader(Context context, WebClient wc, String uri) {
		super(context, wc, uri);
	}

	@Override
	protected String onUnmarshal(String value) throws Exception {
		return value;
	}

	@Override
	protected void onReleaseResources(String value) {}
}
