package com.ibagroup.wf.intelia.core.to;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ibagroup.wf.intelia.core.mis.LoggableDetail;
import com.ibagroup.wf.intelia.core.robots.factory.RunnerContext;

public class BaseTO {
	
	@SerializedName("recorduuid")
	@Expose
	@LoggableDetail(name = "recorduuid")
	private String recordUuid;
	

	public String getRecordUuid() {
		return recordUuid == null ? RunnerContext.getRecordUuid() : recordUuid;
	}

	public void setRecordUuid(String recordUuid) {
		this.recordUuid = recordUuid;
	}
	
	public String serializeRecord() {
		return new Gson().toJson(this);
	}

}
