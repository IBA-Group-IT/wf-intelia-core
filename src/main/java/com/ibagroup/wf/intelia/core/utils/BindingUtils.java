package com.ibagroup.wf.intelia.core.utils;

import com.freedomoss.crowdcontrol.webharvest.HitSubmissionDataItemDto;
import com.freedomoss.crowdcontrol.webharvest.HitSubmissionDataItemValueDto;
import com.freedomoss.crowdcontrol.webharvest.WebHarvestTaskItem;
import com.freedomoss.crowdcontrol.webharvest.web.WebServiceConnectionProperties;
import com.workfusion.utils.security.Credentials;
import com.workfusion.utils.property.PropertyUtils;
import groovy.lang.Binding;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.SystemUtilities;

public class BindingUtils {

    private static final String SYS = "sys";

    public static Object extractVarOrNull(Binding binding, String property) {
        if (binding.hasVariable(property)) {
            return binding.getVariable(property);
        }
        return !SYS.equals(property) ? getSys(binding).getVar(property) : null;
    }

    public static String getPropertyValue(Binding binding, String property) {
        String resultValue;
        Object nodeVariable = extractVarOrNull(binding, property);
        if (nodeVariable != null && PropertyUtils.isPropertyDefined(resultValue = nodeVariable.toString())) {
            return resultValue;
        }
        return null;
    }

    public static String getPropertyValue(Binding binding, String property, String def) {
        String resultValue = getPropertyValue(binding, property);
        if (resultValue != null && !StringUtils.isEmpty(resultValue)) {
            return resultValue;
        }
        return def;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getTypedPropertyValue(Binding binding, String property) {
        Object nodeVariable = extractVarOrNull(binding, property);
        if (nodeVariable == null || nodeVariable instanceof EmptyVariable) {
            return null;
        }
        if (nodeVariable instanceof Variable) {
            return (T) ((Variable) nodeVariable).getWrappedObject();

        }
        return (T) nodeVariable;
    }

    public static String getApplicationContextPath(Binding binding) {
        return BindingUtils.getPropertyValue(binding, "applicationContextPath");
    }

    public static String getApplicationHost(Binding binding) {
        return BindingUtils.getPropertyValue(binding, "applicationHost");
    }

    public static String getRestBasePath(Binding binding) {
        String hostVariable = getApplicationHost(binding);
        if (BindingUtils.isNull(hostVariable)) {
            throw new RuntimeException(String.format("'%s' variable is not defined", "applicationHost"));
        }
        String pathVariable = getApplicationContextPath(binding);
        if (BindingUtils.isNull(pathVariable)) {
            throw new RuntimeException(String.format("'%s' variable is not defined", "applicationContextPath"));
        }
        return hostVariable + pathVariable;
    }

    public static HttpHeaders getHeaderWithAuthorization(Binding binding) {
        HttpHeaders headers = new HttpHeaders();
        Credentials credentials = BindingUtils.getUserCredentials(binding);
        String plainCreds = credentials.getUsername() + ":" + credentials.getPassword();
        byte[] plainCredsBytes = plainCreds.getBytes(StandardCharsets.UTF_8);
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes, StandardCharsets.UTF_8);
        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }

    public static Credentials getUserCredentials(Binding binding) {
        Object userCred = getWrappedObjFromContext(binding, "userInternalCredentials");
        if (BindingUtils.isNull(userCred)) {
            throw new RuntimeException(String.format("'%s' is null", "userInternalCredentials"));
        }
        return (Credentials) userCred;
    }

    public static WebServiceConnectionProperties createBaseConnectionProperties(Binding binding) {
        WebServiceConnectionProperties connectionProperties = new WebServiceConnectionProperties();
        Credentials credentials = BindingUtils.getUserCredentials(binding);
        connectionProperties.setCredentials(credentials);
        Boolean restFormBasedAuth = (Boolean) BindingUtils.getTypedPropertyValue(binding, "restFormAuthEnable");
        connectionProperties.setRestFormAuthEnable(restFormBasedAuth);
        String applicationHost = BindingUtils.getApplicationHost(binding);
        connectionProperties.setWorkfusionHost(applicationHost);
        String contextPath = BindingUtils.getApplicationContextPath(binding);
        connectionProperties.setWorkfusionUrl(contextPath);
        return connectionProperties;
    }

    public static boolean isNull(Object hostVariable) {
        if (hostVariable != null) {
            return hostVariable instanceof Variable && ((Variable) hostVariable).getWrappedObject() == null;
        }
        return true;
    }

    public static Object getWrappedObjFromContext(Binding binding, String key) {
        Object var = extractVarOrNull(binding, key);
        if (var instanceof Variable) {
            return ((Variable) var).getWrappedObject();
        }
        return var;
    }

    public static SystemUtilities getSys(Binding binding) {
        return getTypedPropertyValue(binding, SYS);
    }

    public static WebHarvestTaskItem getWebHarvestTaskItem(Binding binding) {
        return getTypedPropertyValue(binding, "item");
    }

    /**
     * Collect columns from input data to Map.
     *
     * @param binding - groovy binding reference from workfusion config
     * @param inclColumns - Columns to be included, if null - include everything
     *
     * @return Map of columns
     */
    public static Map<String, String> getInputAsMap(Binding binding, String inclColumns) {
        Map<String, String> result = new HashMap<>();
        // populate transaction info
        List<String> columns = null != inclColumns ? Arrays.asList(inclColumns.split(",")) : new ArrayList<>();

        List<HitSubmissionDataItemValueDto> list = getHitSubmissionDataItemValues(binding);

        for (int i = 0; i < list.size(); i++) {
            HitSubmissionDataItemValueDto item_ = list.get(i);
            if (columns.isEmpty() || columns.contains(item_.getName())) {
                String val = item_.getValue();
                result.put(item_.getName(), val);
            }
        }
        return result;
    }

    public static List<HitSubmissionDataItemValueDto> getHitSubmissionDataItemValues(Binding binding) {
        return ((HitSubmissionDataItemDto) BindingUtils.getWrappedObjFromContext(binding, "hit_submission_data_item")).getItemValueList();
    }
}
