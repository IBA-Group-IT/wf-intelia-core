package com.ibagroup.wf.intelia.core.exceptions;

import java.util.HashMap;
import java.util.Map;

public class RobotException extends RuntimeException {

	private static final long serialVersionUID = -3347747870099784171L;

	private int errorCode = -1;

	private Map<String, String> params = new HashMap<String, String>();

	public RobotException() {
		super();
	}

	public RobotException(int errorCode) {
		super();
		this.errorCode = errorCode;
	}

	public RobotException(String message) {
		super(message);
	}

	public RobotException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public RobotException(Exception cause) {
		super(cause);
	}

	public RobotException(int errorCode, Exception cause) {
		super(cause);
		this.errorCode = errorCode;
	}

	public RobotException(String message, Exception cause) {
		super(message, cause);
	}

	public RobotException(int errorCode, String message, Exception cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public RobotException put(String key, String value) {
		params.put(key, value);
		return this;
	}

	public String get(String key) {
		return params.get(key);
	}
}
