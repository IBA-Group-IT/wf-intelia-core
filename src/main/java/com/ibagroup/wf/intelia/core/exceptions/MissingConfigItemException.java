package com.ibagroup.wf.intelia.core.exceptions;

public class MissingConfigItemException extends RuntimeException {

	private static final long serialVersionUID = 4496987636357758243L;

	public MissingConfigItemException(String itemName) {
		super(String.format("Required config item '%s' is not specified.", itemName));
	}
}
