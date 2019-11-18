package com.ibagroup.wf.intelia.core.exceptions;

import com.ibagroup.wf.intelia.core.mis.TaskAction;

import java.text.MessageFormat;

public interface InteliaErrorDescription {
    String name();

    String getMessageTemplate();

    TaskAction.Result getStatus();

    default String getMessage(Object... params) {
        return name() + ": " + MessageFormat.format(getMessageTemplate(), params);
    }
}
