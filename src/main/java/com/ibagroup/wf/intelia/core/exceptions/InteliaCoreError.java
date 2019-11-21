package com.ibagroup.wf.intelia.core.exceptions;


import com.ibagroup.wf.intelia.core.mis.TaskAction;

import java.util.ResourceBundle;

public enum InteliaCoreError implements InteliaErrorDescription {

    UNEXPECTED_FATAL(TaskAction.Result.FAILED),
    UNEXPECTED_ERROR(TaskAction.Result.FAILED),
    NO_SECURE_STORE_ERROR(TaskAction.Result.FAILED),
    CFG_NO_CONFIGURATION_VALUE(TaskAction.Result.FAILED);

    private InteliaCoreError(TaskAction.Result status) {
        this.status = status;
    }

    private TaskAction.Result status;

    private static ResourceBundle rb = ResourceBundle.getBundle("intelia_error");

    public String getMessageTemplate() {
        return rb.getString(this.name());
    }

    public TaskAction.Result getStatus() {
        return status;
    }
}
