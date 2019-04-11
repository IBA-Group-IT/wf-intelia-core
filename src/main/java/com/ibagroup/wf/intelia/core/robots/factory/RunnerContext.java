package com.ibagroup.wf.intelia.core.robots.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ibagroup.wf.intelia.core.CommonConstants;
import com.ibagroup.wf.intelia.core.mis.TaskAction.Result;

public class RunnerContext {

	private static String recordUuid = CommonConstants.DUMMY_UUID;
	private static String campaignUuid = CommonConstants.DUMMY_UUID;
	private static String runUuid = CommonConstants.DUMMY_UUID;
	private static int i = 0;
	private static int j = 0;
	private static Result result = Result.EMPTYRESULT;
	private static List<String> descriptions = new ArrayList<>();

	public static String getRecordUuid() {
		return recordUuid;
	}

	public static void setRecordUuid(String recordUuid) {
		RunnerContext.recordUuid = recordUuid;
	}

	public static String getCampaignUuid() {
		return campaignUuid;
	}

	public static void setCampaignUuid(String campaignUuid) {
		if (i == 0) {
			RunnerContext.campaignUuid = campaignUuid;
			i++;
		}
	}

	public static String getRunUuid() {
		return runUuid;
	}

	public static void setRunUuid(String runUuid) {
		if (j == 0) {
			RunnerContext.runUuid = runUuid;
			j++;
		}
	}

	public static void setLastResult(Result result, String... descriptions) {
		RunnerContext.result = result;
		RunnerContext.descriptions.clear();
		if (descriptions != null) {
			RunnerContext.descriptions.addAll(Arrays.asList(descriptions));
		}

	}

	public static Result getLastResult() {
		return result;
	}

	public static List<String> getLastDescriptions() {
		return descriptions;
	}

	public static void cleanActionData() {
		result = Result.EMPTYRESULT;
		descriptions.clear();
	}
}
