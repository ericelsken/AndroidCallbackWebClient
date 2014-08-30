package com.ericelsken.android.web.content;

import org.json.JSONObject;

import android.content.Context;

import com.ericelsken.android.webclient.WebClient;

public class JsonWebClientLoader extends WebClientLoader<JSONObject> {
	
	public JsonWebClientLoader(Context context, String uri) {
		super(context, uri);
	}

	public JsonWebClientLoader(Context context, WebClient wc, String uri) {
		super(context, wc, uri);
	}

	@Override
	protected JSONObject onUnmarshal(String value) throws Exception {
		return new JSONObject(value);
	}

	@Override
	protected void onReleaseResources(JSONObject value) {}

}
