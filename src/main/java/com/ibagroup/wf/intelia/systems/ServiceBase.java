package com.ibagroup.wf.intelia.systems;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassPathUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.metadata.MetadataManager;
import com.ibagroup.wf.intelia.core.metadata.types.LoggingMetadata;
import com.ibagroup.wf.intelia.utils.soap.SoapServiceUtil;

public abstract class ServiceBase<REQ, REQ_SRV, RESP, RESP_SRV>
		implements RequestPreparer<REQ, REQ_SRV>, ResponseHandler<RESP, RESP_SRV> {

	private final List<String> requestResponseList = new ArrayList<>();

	public final String REQ_STR = "REQUEST:";
	public final String REQ_PS_STR = "REQUEST_PARSED:";
	public final String RESP_STR = "RESPONSE:";
	public final String RESP_PS_STR = "RESPONSE_PARSED:";
	public final String ENDPOINT_STR = "ENDPOINT:";
	private final ConfigurationManager configurationManager;
	private final MetadataManager metadataManager;

	public ServiceBase(ConfigurationManager configurationManager, MetadataManager metadataManager) {
		this.configurationManager = configurationManager;
		this.metadataManager = metadataManager;
	}

	public RESP call(REQ requestStr) {
		addRequestResponse(REQ_STR, requestStr != null ? messageToString(requestStr) : "null");

		try {
			REQ_SRV prepareRequest = prepareRequest(requestStr);
			addRequestResponse(REQ_PS_STR, prepareRequest != null ? messageToString(prepareRequest) : "null");

			RESP_SRV resp = callServiceInner(prepareRequest);
			addRequestResponse(RESP_STR, resp != null ? messageToString(resp) : "null");

			RESP handleResponse = handleResponse(resp);
			addRequestResponse(RESP_PS_STR, handleResponse != null ? messageToString(handleResponse) : "null");
			return handleResponse;
		} finally {
			if (null != metadataManager) {
				metadataManager.addMetadata(new LoggingMetadata("req_resp", getRequestResponseList().toString()));
				getRequestResponseList().clear();
			}
		}
	}

	protected String messageToString(Object object) {
		try {
			
			if (object instanceof SOAPMessage) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				((SOAPMessage) object).writeTo(out);
				return new String(out.toByteArray(), StandardCharsets.UTF_8);
			} else {
				return object.toString();
			}
		} catch (SOAPException | IOException e) {
			String err_message = ExceptionUtils.getMessage(e) + ": " + ExceptionUtils.getStackTrace(e);
			metadataManager.addMetadata(new LoggingMetadata("stacktrace", err_message));
		}
		return "";
	}

	protected abstract RESP_SRV callServiceInner(REQ_SRV requestStr);

	public List<String> getRequestResponseList() {
		return requestResponseList;
	}

	protected ConfigurationManager getCfg() {
		return configurationManager;
	}

	protected MetadataManager getActivityMgr() {
		return metadataManager;
	}

	protected String replaceValuesInTemplate(Map<String, String> valuesMap, String requestTemplateName) {
		String getTransactionDetailsrequest;
		try {
			getTransactionDetailsrequest = IOUtils
					.toString(ServiceBase.class.getResourceAsStream(prepareFilePath(requestTemplateName)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		StrSubstitutor sub = new StrSubstitutor(valuesMap);
		sub.setEnableSubstitutionInVariables(true);
		String resolvedRequest = sub.replace(getTransactionDetailsrequest);
		return resolvedRequest;
	}

	protected String prepareFilePath(String filePath) {
		String fullyQualifiedName = ClassPathUtils.toFullyQualifiedPath(this.getClass(), filePath);

		String result = fullyQualifiedName
				.replaceFirst(ServiceBase.class.getPackage().getName().replace(".", "/") + "/", "");
		return result;
	}

	protected Map<String, String> initValuesMap() {
		Map<String, String> valuesMap = new HashMap<>();
		valuesMap.put("timestamp", SoapServiceUtil.getNowInServiceString());
		return valuesMap;
	}

	protected void addRequestResponse(String type, String msg) {
		if (msg == null) {
			msg = "null";
		}
		// replace "\n", "\r" symbols with whitespace for correct CSV file
		// preview
		msg = msg.replaceAll("\n|\r|\u00A0", " ");
		requestResponseList.add(type + " " + msg);

	}

}
