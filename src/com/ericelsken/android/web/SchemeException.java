package com.ericelsken.android.web;

public class SchemeException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public SchemeException(String scheme) {
		super("Scheme must be either \"http\" or \"https\". given: " + scheme);
	}
}
