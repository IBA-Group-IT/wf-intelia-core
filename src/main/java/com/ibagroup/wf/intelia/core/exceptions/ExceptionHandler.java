package com.ibagroup.wf.intelia.core.exceptions;

public interface ExceptionHandler {

    void logCurrentException(Throwable throwable);

    void logCurrentException();

    void setLastErrorMessage(String last_error_message);

    void setLastErrorMessage(String shortMsg, Throwable exception);

    String getLastErrorMessage();
}
