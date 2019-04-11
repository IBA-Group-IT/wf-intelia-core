package com.ibagroup.wf.intelia.core.robots.factory;

import com.ibagroup.wf.intelia.core.MachineTask;

import groovy.lang.Binding;

public class MachineTaskRobotFactory extends RobotsFactoryBuilder {

	public MachineTaskRobotFactory(Binding binding) {
		this(binding, false, null);
	}

	public MachineTaskRobotFactory(Binding binding, String dsName) {
		this(binding, false, dsName);
	}

	public MachineTaskRobotFactory(Binding binding, boolean throwException, String dsName) {
		MachineTask baseRpa = new MachineTask(binding, dsName);
		this.setActivityMgr(baseRpa);
		this.setBinding(binding);
		this.setExHandler(baseRpa);
		this.setActivitiesStorage(baseRpa);
		this.setRobotLogger(baseRpa);
		this.setCfg(baseRpa);

		if (!throwException) {
			doNotReThrowException();
		}
	}

	public MachineTaskRobotFactory(Binding binding, boolean throwException) {
		this(binding, throwException, null);
	}

}
