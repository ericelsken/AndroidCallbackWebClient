package com.ericelsken.android.web;

import android.content.Context;

public interface ExceptionHandler {
	
	public boolean handleException(Context context, int id, Exception ex);
}
