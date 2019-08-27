package com.ibagroup.wf.intelia.core.robots.factory;

import com.ibagroup.wf.intelia.core.InteliaBuilder;
import groovy.lang.Binding;

/**
 * @deprecated {@link InteliaBuilder#defaultSetup()} instead.
 *
 */
@Deprecated
public class MachineTaskRobotFactory extends RobotsFactoryBuilder {

    @Deprecated
    public MachineTaskRobotFactory(Binding binding) {
        super(binding);
        defaultSetup();
    }

    @Deprecated
    public MachineTaskRobotFactory(Binding binding, String dsName) {
        super(binding);
        defaultSetup(dsName);
    }

    @Deprecated
    public MachineTaskRobotFactory(Binding binding, boolean throwException, String dsName) {
        super(binding);
        defaultSetup(throwException, dsName);
    }

    @Deprecated
    public MachineTaskRobotFactory(Binding binding, boolean throwException) {
        super(binding);
        defaultSetup(throwException);
    }

}
