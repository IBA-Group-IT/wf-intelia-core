package com.ibagroup.wf.intelia.core.robots;

import javax.inject.Inject;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ibagroup.wf.intelia.core.FlowContext;
import com.ibagroup.wf.intelia.core.Injector;
import com.ibagroup.wf.intelia.core.annotations.Wire;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.metadata.MetadataManager;
import com.ibagroup.wf.intelia.core.metadata.types.LoggingMetadata;
import com.ibagroup.wf.intelia.core.mis.TaskAction.Result;
import com.ibagroup.wf.intelia.core.robots.factory.RunnerContext;
import com.ibagroup.wf.intelia.core.security.SecurityUtils;
import groovy.lang.Binding;

public abstract class RobotCapabilities implements RobotProtocol {

    @Inject
	@Wire
	private Binding binding;

    @Inject
	@Wire
	private FlowContext flowContext;

    @Inject
    @Wire
	private ConfigurationManager cfg;

    @Inject
    @Wire
	private MetadataManager metadataManager;

    @Inject
    @Wire
    private SecurityUtils securityUtils;

    @Inject
    private Injector injector;

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
	
	public FlowContext getFlowContext(){
	    return flowContext;
	}

	@Override
	public boolean storeCurrentMetadata() {
		String toString = ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
		getMetadataManager().addMetadata(new LoggingMetadata("toString", toString));
		return true;
	}

    public Injector getInjector() {
        return injector;
    }

    public SecurityUtils getSecurityUtils() {
        return securityUtils;
    }
}
