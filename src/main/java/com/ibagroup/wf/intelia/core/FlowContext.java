package com.ibagroup.wf.intelia.core;

import static com.ibagroup.wf.intelia.core.utils.JsonStringUtils.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.impl.Log4jLoggerAdapter;
import org.webharvest.utils.SystemUtilities;
import com.freedomoss.crowdcontrol.webharvest.CampaignDto;
import com.freedomoss.crowdcontrol.webharvest.RunDto;
import com.freedomoss.crowdcontrol.webharvest.WebHarvestTaskItem;
import com.ibagroup.wf.intelia.core.to.ErrorTO;
import com.ibagroup.wf.intelia.core.to.ErrorsTO;
import com.ibagroup.wf.intelia.core.utils.BindingUtils;
import groovy.lang.Binding;

/**
 * Utility class that provides access to the flow context's:
 * <ul>
 * <li>binding</li>
 * <li>event logging</li>
 * <li>errors collection</li>
 * <li>export collection</li>
 * </ul>
 * <pre>
 *  
 * @see TODO Samples
 * 
 * @author dmitriev
 *
 */

public class FlowContext {
	// name for Item UUID
	public static final String PROCESS_UUID = "processguid"; // column
	
	private Binding binding;

	private final SystemUtilities sys;

	private final WebHarvestTaskItem item;
	private final RunDto runDto;
	private final CampaignDto campaignDto;
	private final String runname;
	private final String taskName;

	private final String submissionId;
	private final String processGuid;

	private final Log4jLoggerAdapter wfLog;

	private final LinkedList<Map<String, String>> toExport;

	private final boolean logAsError;

	private final ErrorsTO errors;

	public FlowContext(Binding binding) {
		this.binding = binding;
		this.sys = BindingUtils.getSys(binding);

		this.item = BindingUtils.getWebHarvestTaskItem(binding);
		this.runDto = item.getRun();
		this.campaignDto = item.getCampaignDto();
		this.runname = runDto.getCampaignName();
		this.taskName = getCampaignDto().getTitle();

		this.submissionId = BindingUtils.getPropertyValue(binding, CommonConstants.ITEM_UUID_CLM);
		this.processGuid = runDto.getRootRunUuid();
		this.wfLog = BindingUtils.getTypedPropertyValue(binding, "log");
		this.toExport = new LinkedList<>();

		this.logAsError = Boolean.parseBoolean(BindingUtils.getPropertyValue(binding, "log_as_error", "false"));

		ErrorsTO errorsFromVar = getJsonFlowVariableAsObject(ErrorsTO.ERRORS_LABEL, ErrorsTO.class);
		this.errors = errorsFromVar == null ? new ErrorsTO() : errorsFromVar;
	}
	
    public Binding getBinding() {
        return binding;
    }

	public SystemUtilities getSys() {
		return sys;
	}
	
	public void addSysVar(String varName, String varValue){
	    getSys().defineVariable(varName, varValue, true);
	}
	

	public WebHarvestTaskItem getItem() {
		return item;
	}

	public RunDto getRunDto() {
		return runDto;
	}

	public CampaignDto getCampaignDto() {
		return campaignDto;
	}

	public String getRunname() {
		return runname;
	}

	public String getTaskName() {
		return taskName;
	}

	public String getSubmissionId() {
		return submissionId;
	}

	public String getProcessGuid() {
		return processGuid;
	}

	public Log4jLoggerAdapter getWfLog() {
		return wfLog;
	}

	public List<Map<String, String>> getToExport() {
		return toExport;
	}

	public Set<String> getExportColumns() {
		return getToExport().stream().flatMap(r -> r.keySet().stream()).collect(Collectors.toSet());
	}

	public Throwable logExceptionToExport(Throwable throwable) {
		if (throwable instanceof InvocationTargetException) {
			throwable = ((InvocationTargetException) throwable).getTargetException();
		}

		error(throwable.getMessage(), throwable.getCause());

		errors.addError(new ErrorTO(taskName, throwable.getMessage()));
		putJsonObjectToExport(ErrorsTO.ERRORS_LABEL, errors);
		return throwable;
	}

	public Map<String, String> getLastRow() {
		if (CollectionUtils.isEmpty(toExport)) {
			addRowToExport();
		}
		return toExport.getLast();
	}

	public void putToExport(String name, String value) {
		getLastRow().put(name, value);
	}

	public void putJsonObjectToExport(String name, Object value) {
		// put it as a json
		getLastRow().put(name, asJson(value));
	}

	public HashMap<String, String> addRowToExport() {
		HashMap<String, String> newRow = new HashMap<>();
		toExport.add(newRow);
		return newRow;
	}

	public <T> T getFlowVariable(String varName) {
		return BindingUtils.getTypedPropertyValue(getBinding(), varName);
	}

	public <T> T getJsonFlowVariableAsObject(String varName, Class<T> objClass) {
		String asJsonString = BindingUtils.getPropertyValue(getBinding(), varName);
		return StringUtils.isEmpty(asJsonString) ? null : jsonAsObject(asJsonString, objClass);
	}

	public void error(String msg, Object... argArray) {
		getWfLog().error(msg, argArray);
	}

	public void error(String msg, Throwable t) {
		getWfLog().error(msg, t);
	}

	public void warn(String msg, Object... argArray) {
		if (logAsError) {
			getWfLog().error(msg, argArray);
		} else {
			getWfLog().warn(msg, argArray);
		}
	}

	public void info(String msg, Object... argArray) {
		if (logAsError) {
			getWfLog().error(msg, argArray);
		} else {
			getWfLog().info(msg, argArray);
		}
	}

	public void debug(String msg, Object... argArray) {
		if (logAsError) {
			getWfLog().error(msg, argArray);
		} else {
			getWfLog().debug(msg, argArray);
		}
	}

}
