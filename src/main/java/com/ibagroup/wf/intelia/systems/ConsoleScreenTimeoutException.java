package com.ibagroup.wf.intelia.systems;

import org.openqa.selenium.TimeoutException;

public class ConsoleScreenTimeoutException extends TimeoutException {

	private static final long serialVersionUID = 1L;
	private String screenText;

	public ConsoleScreenTimeoutException() {

	}

	public ConsoleScreenTimeoutException(String message) {
		super(message);
	}

	public ConsoleScreenTimeoutException(String message, String screenText) {
		super(message);
		this.screenText = screenText;
	}

	public String getScreenText() {
		return screenText;
	}
}
