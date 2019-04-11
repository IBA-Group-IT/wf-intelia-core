package com.ibagroup.wf.intelia.core.mis;

import java.util.HashMap;
import java.util.Map;

import com.ibagroup.wf.intelia.core.robots.factory.RunnerContext;

public class TaskDetail {

	private final String campaignUuid;
	private final String runUuid;
	private final String recordUuid;
	private final String module;
	private final String field;
	private final String value;
	

	public TaskDetail(String taskName, String field, String value) {
		this.campaignUuid = RunnerContext.getCampaignUuid();
		this.runUuid = RunnerContext.getRunUuid();
		this.recordUuid = RunnerContext.getRecordUuid();
		this.module = taskName;
		this.field = field;
		this.value = value;	
	}
	
	public Map<String, String> getMap() {
		Map<String, String> dsMap = new HashMap<>();
		dsMap.put("runuuid", runUuid);
		dsMap.put("recorduuid", recordUuid);
		dsMap.put("module", module);
		dsMap.put("field", field);
		dsMap.put("value", value);
		dsMap.put("campaignuuid", campaignUuid);

		return dsMap;
	}

	public String getRunUuid() {
		return runUuid;
	}

	public String getRecordUuid() {
		return recordUuid;
	}

	public String getModule() {
		return module;
	}

	public String getField() {
		return field;
	}

	public String getValue() {
		return value;
	}

	public String getCampaignUuid() {
		return campaignUuid;
	}

}