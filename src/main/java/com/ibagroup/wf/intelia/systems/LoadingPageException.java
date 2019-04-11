package com.ibagroup.wf.intelia.systems;

/**
 * Exception occurs when robot can't loading to page (Tunley) due to different
 * reason (invalid URL etc.).
 */

public class LoadingPageException extends RuntimeException {

	// Parameterless Constructor
	public LoadingPageException() {
	}

	// Constructor that accepts a message
	public LoadingPageException(String message) {
		super(message);
	}

}
