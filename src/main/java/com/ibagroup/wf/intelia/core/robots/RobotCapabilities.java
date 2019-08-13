package com.ibagroup.wf.intelia.core.robots;

import javax.inject.Inject;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ibagroup.wf.intelia.core.FlowContext;
import com.ibagroup.wf.intelia.core.Injector;
import com.ibagroup.wf.intelia.core.annotations.Wire;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.exceptions.ExceptionHandler;
import com.ibagroup.wf.intelia.core.metadata.MetadataManager;
import com.ibagroup.wf.intelia.core.metadata.types.LoggingMetadata;
import com.ibagroup.wf.intelia.core.mis.IRobotLogger;
import com.ibagroup.wf.intelia.core.mis.TaskAction.Result;
import com.ibagroup.wf.intelia.core.robots.factory.RunnerContext;
import groovy.lang.Binding;

public abstract class RobotCapabilities extends Injector implements RobotProtocol {

    @Inject
	@Wire
	private Binding binding;

    @Inject
	@Wire
	private FlowContext flowContext;

    @Inject
	@Wire
	private ExceptionHandler exceptionHandler;

    @Inject
    @Wire
	private ConfigurationManager cfg;

    @Inject
    @Wire
	private MetadataManager metadataManager;

    @Inject
	@Wire
	private IRobotLogger robotLogger;

	@Override
	public void storeCurrentActionResult(Result result, String... description) {
		RunnerContext.setLastResult(result, description);
	}

	public RobotCapabilities() {
		super();
	}

	public Binding getBinding() {
		return binding;
	}

	public MetadataManager getMetadataManager() {
		return metadataManager;
	}

	public ConfigurationManager getCfg() {
		return cfg;
	}

	public ExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	public IRobotLogger getRobotLogger() {
		return robotLogger;
	}
	
	public FlowContext getFlowContext(){
	    return flowContext;
	}

	@Override
	public boolean storeCurrentMetadata() {
		String toString = ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
		getMetadataManager().addMetadata(new LoggingMetadata("toString", toString));
		return true;
	}

}
