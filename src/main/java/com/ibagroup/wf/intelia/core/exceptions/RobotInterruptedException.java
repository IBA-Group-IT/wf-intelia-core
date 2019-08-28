package com.ibagroup.wf.intelia.core.exceptions;

public class RobotInterruptedException extends RobotException {

	private static final long serialVersionUID = 5000344353862790515L;

	public RobotInterruptedException() {
		super();
	}

	public RobotInterruptedException(int errorCode) {
		super(errorCode);
	}

	public RobotInterruptedException(String message) {
		super(message);
	}

	public RobotInterruptedException(int errorCode, String message) {
		super(errorCode, message);
	}

	public RobotInterruptedException(Exception cause) {
		super(cause);
	}

	public RobotInterruptedException(int errorCode, Exception cause) {
		super(errorCode, cause);
	}

	public RobotInterruptedException(String message, Exception cause) {
		super(message, cause);
	}

	public RobotInterruptedException(int errorCode, String message, Exception cause) {
		super(errorCode, message, cause);
	}
}
