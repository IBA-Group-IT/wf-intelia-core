package com.ibagroup.wf.intelia.core.exceptions;

public class RobotInterruptedException extends RuntimeException {

	private static final long serialVersionUID = -3347747870099784171L;

	public RobotInterruptedException() {
		super();
	}

	public RobotInterruptedException(String message) {
		super(message);
	}

	public RobotInterruptedException(String message, Exception cause) {
		super(message, cause);
	}
}
