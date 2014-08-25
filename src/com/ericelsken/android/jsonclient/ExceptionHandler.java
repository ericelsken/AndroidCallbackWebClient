package com.ericelsken.android.jsonclient;

import android.content.Context;

public interface ExceptionHandler {
	
	public boolean handleException(int id, Exception ex, Context context);
}
