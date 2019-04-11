package com.ibagroup.wf.intelia.core.mis;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ibagroup.wf.intelia.core.robots.factory.RunnerContext;

public class TaskAction {

	private final String campaignUuid;
	private final String runUuid;
	private final String recordUuid;
	private final String module;
	private final String operation;
	private final Result result;
	private final Long startTime;
	private final Long endTime;
	private final String description1;
	private final String description2;
	private final String description3;
	private final boolean transactional;

	public TaskAction(String module, String operation, Result result, Long startTime) {
		this(module, operation, result, startTime, null, null, null, false);
	}
	
	public TaskAction(String module, String operation, Result result, Long startTime,  boolean transactional) {
		this(module, operation, result, startTime, null, null, null, transactional);
	}
	
	public TaskAction(String module, String operation, Result result, Long startTime, String description1,
			String description2, String description3) {
		this(module, operation, result, startTime, description1, description2, description3, false);
	}

	public TaskAction(String module, String operation, Result result, Long startTime, String description1,
			String description2, String description3, boolean transactional) {
		this.runUuid = RunnerContext.getRunUuid();
		this.recordUuid = RunnerContext.getRecordUuid();
		this.module = module;
		this.operation = operation;
		this.result = result;
		this.startTime = startTime;
		this.endTime = Long.valueOf(new Date().getTime());
		this.description1 = description1;
		this.description2 = description2;
		this.description3 = description3;
		this.campaignUuid = RunnerContext.getCampaignUuid();
		this.transactional = transactional;
	}
	
	public TaskAction(TaskAction task1, TaskAction task2){
		this.runUuid = task1.getRunUuid();
		this.recordUuid = task1.getRecordUuid();
		this.campaignUuid = task1.getCampaignUuid();
		this.module = task1.getModule();
		this.operation = task1.getOperation();
		this.result = task2.getResult();
		this.startTime = task1.getStartTime();
		this.endTime = task2.getEndTime();
		this.description1 = task2.getDescription1();
		this.description2 = task2.getDescription2();
		this.description3 = task2.getDescription3();
		this.transactional = task1.getTransactional();
	}

	public Map<String, String> getMap() {
		Map<String, String> dsMap = new HashMap<>();
		dsMap.put("runuuid", runUuid);
		dsMap.put("recorduuid", recordUuid);
		dsMap.put("module", module);
		dsMap.put("operation", operation);
		dsMap.put("result", result.getResult());
		dsMap.put("startTime", startTime.toString());
		dsMap.put("endTime", endTime.toString());
		dsMap.put("startTimeStamp", new Date(startTime.longValue()).toString());
		dsMap.put("endTimeStamp", new Date(endTime.longValue()).toString());
		dsMap.put("description1", description1);
		dsMap.put("description2", description2);
		dsMap.put("description3", description3);
		dsMap.put("campaignuuid", campaignUuid);

		return dsMap;
	}

	public boolean getTransactional() {
		return transactional;
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

	public String getOperation() {
		return operation;
	}

	public Result getResult() {
		return result;
	}

	public Long getStartTime() {
		return startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public String getDescription1() {
		return description1;
	}

	public String getDescription2() {
		return description2;
	}

	public String getDescription3() {
		return description3;
	}

	public String getCampaignUuid() {
		return campaignUuid;
	}

	public enum Result {
		SUCCESS("success"), FAILED("failed"), EXCEPTION("exception"), EMPTYRESULT("");
		private String result;

		Result(String result) {
			this.result = result;
		}

		public String getResult() {
			return result;
		}
	}

}
