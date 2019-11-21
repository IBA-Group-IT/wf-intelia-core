package com.ibagroup.wf.intelia.core.exceptions;

import java.util.HashMap;
import java.util.Map;

public class RobotException extends RuntimeException {

	private static final long serialVersionUID = -3347747870099784171L;

	private ErrorDescription errorDescription;
	private String localizedMessage;

	private Map<String, String> params = new HashMap<String, String>();

	public RobotException() {
		super();
	}

	public RobotException(ErrorDescription errorDescription, Object... params) {
		super(errorDescription.getMessage(params));
		this.errorDescription = errorDescription;
		this.localizedMessage = errorDescription.getLocalizedMessage(params);
	}

	public RobotException(String message) {
		super(message);
	}

	public RobotException(Exception cause) {
		super(cause);
	}

	public RobotException(Exception cause, ErrorDescription errorDescription, Object... params) {
		super(errorDescription.getMessage(params), cause);
		this.errorDescription = errorDescription;
		this.localizedMessage = errorDescription.getLocalizedMessage(params);
	}

	public RobotException(String message, Exception cause) {
		super(message, cause);
	}

	public ErrorDescription getErrorDescription() {
		return errorDescription;
	}

	@Override
	public String getLocalizedMessage() {
		return localizedMessage != null ? localizedMessage : super.getLocalizedMessage();
	}

	@Override
	public String toString() {
		String s = getClass().getName();
		String message = getMessage();
		return (message != null) ? (s + ": " + message) : s;
	}

	public RobotException put(String key, String value) {
		params.put(key, value);
		return this;
	}

	public String get(String key) {
		return params.get(key);
	}

	public Map<String, String> getParams() {
		return params;
	}
}
