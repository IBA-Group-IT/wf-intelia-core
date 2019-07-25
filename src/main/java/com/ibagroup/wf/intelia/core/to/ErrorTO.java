package com.ibagroup.wf.intelia.core.to;

public class ErrorTO {
	public static final String ERROR_LABEL = "error";

	private String task;
	private String message;

	public ErrorTO() {
	}

	public ErrorTO(String task, String message) {
		this.task = task;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

}
