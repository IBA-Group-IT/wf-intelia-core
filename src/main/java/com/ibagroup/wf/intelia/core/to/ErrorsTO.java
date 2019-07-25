package com.ibagroup.wf.intelia.core.to;

import java.util.ArrayList;
import java.util.List;

public class ErrorsTO {
	public static final String ERRORS_LABEL = "errors";

	private List<ErrorTO> errors = new ArrayList<>();

	public void addError(ErrorTO error) {
		this.errors.add(error);
	}

	public List<ErrorTO> getErrors() {
		return new ArrayList<>(errors);
	}

}
