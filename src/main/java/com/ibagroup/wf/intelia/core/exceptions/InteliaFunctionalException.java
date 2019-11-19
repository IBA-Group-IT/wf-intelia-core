package com.ibagroup.wf.intelia.core.exceptions;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class InteliaFunctionalException extends Exception {

    protected InteliaErrorDescription error;
    protected String hcErrorMessage;

    public InteliaFunctionalException(Throwable e, InteliaErrorDescription error, Object... details) {
        this(error, details);
        initCause(e);
    }

    public InteliaFunctionalException(InteliaErrorDescription error, Object... details) {
        super(error.getMessage(details));
        this.hcErrorMessage = error.getMessage(details) + (getCause() != null ? ("\n" + ExceptionUtils.getStackTrace(getCause())) : "");
        this.error = error;
    }

    public String getMessage() {
        return this.hcErrorMessage;
    }

    public InteliaErrorDescription getError() {
        return error;
    }
}

