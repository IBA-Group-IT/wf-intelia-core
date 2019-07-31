package com.ibagroup.wf.intelia.core.exceptions;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibagroup.wf.intelia.core.CommonConstants;
import com.ibagroup.wf.intelia.core.FlowContext;

/**
 * Default implementation of ExceptionHandler
 * Out of the box:
 * <ul>
 * <li>logs to slf4j Logger</li>
 * <li>logs to WF events</li>
 * <li>adds last_error_messgae to SYS vars</li>
 * </ul>
 * @author dmitriev
 *
 */
public class DefaultExceptionHandler implements ExceptionHandler{
    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);
    private String lastErrorMessage = "";
    private FlowContext flowContext;
    
    public DefaultExceptionHandler(FlowContext flowContext) {
        this.flowContext = flowContext;
    }

    @Override
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    @Override
    public void setLastErrorMessage(String lastErrorMessage) {
        setLastErrorMessage(lastErrorMessage, null);
    }

    @Override
    public void setLastErrorMessage(String shortMsg, Throwable exception) {
        logger.error(shortMsg, exception);
        flowContext.error(shortMsg, exception);
        this.lastErrorMessage = shortMsg;
        if (null != exception) {
            this.lastErrorMessage += " : " + ExceptionUtils.getStackTrace(exception);
        }
        flowContext.addSysVar(CommonConstants.ERR_MESSAGE_FIELD, lastErrorMessage);
    }

    @Override
    public void logCurrentException() {
        setLastErrorMessage(flowContext.getFlowVariable("_exception_message"), flowContext.getFlowVariable( "_exception"));
    }

    @Override
    public void logCurrentException(Throwable throwable) {
        setLastErrorMessage(ExceptionUtils.getMessage(throwable), throwable);
    }
}
