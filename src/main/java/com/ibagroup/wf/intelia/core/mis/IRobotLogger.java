package com.ibagroup.wf.intelia.core.mis;

import java.util.List;

public interface IRobotLogger {

	void addAction(TaskAction... actions);

	List<TaskAction> getActions();

	void clearActions();

	void addDetails(TaskDetail... details);

	List<TaskDetail> getDetails();

	void clearDetails();

	boolean storeLogs();

}
