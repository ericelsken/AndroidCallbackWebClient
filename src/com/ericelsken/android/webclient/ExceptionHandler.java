package com.ericelsken.android.webclient;

import android.content.Context;

public interface ExceptionHandler {
	
	public boolean handleException(Context context, int id, Exception ex);
}
