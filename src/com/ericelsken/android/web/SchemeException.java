package com.ericelsken.android.web;

/**
 * Instances of this class indicate that the URI of an attempted Request have a
 * scheme other that http or https. 
 * Note that only http and https calls are currently allowed by this library.
 * 
 * @author Eric Elsken
 *
 */
public class SchemeException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Create a new instance with the given invalid scheme.
	 * @param scheme the invalid scheme that was attempted to be used by
	 * a Request.
	 */
	public SchemeException(String scheme) {
		super("Scheme must be either \"http\" or \"https\". given: " + scheme);
	}
}
