package com.ericelsken.android.web.content;

import java.net.URI;

import org.json.JSONObject;

import android.content.Context;

public class JsonWebClientLoader extends WebClientLoader<JSONObject> {
	
	public JsonWebClientLoader(Context context, URI uri) {
		super(context, uri);
	}

	@Override
	protected JSONObject onUnmarshal(String value) throws Exception {
		return new JSONObject(value);
	}

	@Override
	protected void onReleaseResources(JSONObject value) {}

}
