package com.elskene.android.web;

import android.content.Context;

public interface ExceptionHandler {
	
	public boolean handleException(int id, Exception ex, Context context);
}
