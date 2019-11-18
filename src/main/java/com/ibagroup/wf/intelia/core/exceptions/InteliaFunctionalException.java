package com.ibagroup.wf.intelia.core.exceptions;

public class InteliaFunctionalException extends Exception {

    protected InteliaErrorDescription error;
    protected String hcErrorMessage;

    public InteliaFunctionalException(Throwable e, InteliaErrorDescription error, Object... details) {
        this(error, details);
        initCause(e);
    }

    public InteliaFunctionalException(InteliaErrorDescription error, Object... details) {
        super(error.getMessage(details));
        this.hcErrorMessage = error.getMessage(details);
        this.error = error;
    }

    public String getMessage() {
        return this.hcErrorMessage;
    }

    public InteliaErrorDescription getError() {
        return error;
    }
}

