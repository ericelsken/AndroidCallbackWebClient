package com.ericelsken.android.web.content;

import java.net.URI;

import android.content.Context;

public class StringWebClientLoader extends WebClientLoader<String> {
	
	public StringWebClientLoader(Context context, URI uri) {
		super(context, uri);
	}
	
	@Override
	protected String onUnmarshal(String value) throws Exception {
		return value;
	}

	@Override
	protected void onReleaseResources(String value) {}
}
