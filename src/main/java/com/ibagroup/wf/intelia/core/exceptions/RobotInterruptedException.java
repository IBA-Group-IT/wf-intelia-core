package com.ibagroup.wf.intelia.core.exceptions;

public class RobotInterruptedException extends RobotException {

	private static final long serialVersionUID = 5000344353862790515L;

	public RobotInterruptedException() {
		super();
	}

	public RobotInterruptedException(ErrorDescription errorDescription, Object... params) {
		super(errorDescription, params);
	}

	public RobotInterruptedException(String message) {
		super(message);
	}

	public RobotInterruptedException(Exception cause) {
		super(cause);
	}

	public RobotInterruptedException(Exception cause, ErrorDescription errorDescription, Object... params) {
		super(cause, errorDescription, params);
	}

	public RobotInterruptedException(String message, Exception cause) {
		super(message, cause);
	}
}
