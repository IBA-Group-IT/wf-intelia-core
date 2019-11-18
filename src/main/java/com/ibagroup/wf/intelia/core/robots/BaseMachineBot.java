package com.ibagroup.wf.intelia.core.robots;

import com.freedomoss.crowdcontrol.webharvest.WebHarvestTaskItem;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.ibagroup.wf.intelia.core.annotations.AfterInit;
import com.ibagroup.wf.intelia.core.annotations.OnError;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.exceptions.InteliaCoreError;
import com.ibagroup.wf.intelia.core.exceptions.InteliaFunctionalException;
import com.ibagroup.wf.intelia.core.mis.TaskAction;
import com.ibagroup.wf.intelia.core.security.SecureEntryDtoWrapper;
import com.ibagroup.wf.intelia.core.security.SecurityUtils;
import com.ibagroup.wf.intelia.core.utils.BindingUtils;
import com.workfusion.common.utils.GsonUtils;
import groovy.lang.Binding;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.webharvest.runtime.ConfigExecutionLogger;
import sun.misc.SharedSecrets;

import java.lang.reflect.Method;

public class BaseMachineBot extends UiRobotCapabilities implements RobotProtocol {

    public final static org.slf4j.Logger log = ConfigExecutionLogger.log;

    public static String toWireJson(Object o) {
        ExclusionStrategy strategy = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes field) {
                return field.getAnnotation(SerializedName.class) == null;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        };

        return new GsonBuilder().addSerializationExclusionStrategy(strategy).create().toJson(o);
    }

    public String toWireJson() {
        return toWireJson(this);
    }


    public static String toJson(Object object) {
        return GsonUtils.GSON.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> toClass) {
        return GsonUtils.GSON.fromJson(json, toClass);
    }

    public final static String CFG_MANAGER = "_cfg_manager";

    @AfterInit
    public void bb_afterInit() {
        getBinding().setVariable(CFG_MANAGER, getCfg());
    }

    public static String getConfigItem(Binding binding, String key, String defaultValue) {
        ConfigurationManager cfg = BindingUtils.getTypedPropertyValue(binding, CFG_MANAGER);
        if (cfg != null) {
            return cfg.getConfigItem(key, defaultValue);
        }
        return defaultValue;
    }

    @OnError
    public TaskAction.Result handleError(Object self, Method thisMethod, Method processes, Throwable e, Object[] args) {
        Throwable error;
        if (e.getCause() instanceof InteliaFunctionalException) {
            error = e.getCause();
        } else {
            error = new InteliaFunctionalException(e.getCause(), InteliaCoreError.UNEXPECTED_ERROR, ExceptionUtils.getStackTrace((e.getCause())));
        }
        TaskAction.Result result = ((InteliaFunctionalException) error).getError().getStatus();
        storeCurrentActionResult(result, "UNEXPECTED BOT CONFIG ERROR", ExceptionUtils.getStackTrace(error), null);
        try {
            handleErrorState((InteliaFunctionalException) error);
        } catch (Exception z) {
            log.error("UNEXPECTED BOT CONFIG RECOVERING ERROR", e);
        }
        log.error("UNEXPECTED BOT CONFIG ERROR", error);
        return result;
    }

    public void handleErrorState(InteliaFunctionalException error) throws Exception {
    }

    @Override
    public boolean storeCurrentMetadata() {
        return false;
    }

    public String getConfigItem(Enum key, String defaultValue) {
        return getCfg().getConfigItem(key.name(), defaultValue);
    }

    public String getConfigItem(Enum key) throws InteliaFunctionalException {
        String value = getCfg().getConfigItem(key.name());
        if (value == null) {
            throw new InteliaFunctionalException(InteliaCoreError.CFG_NO_CONFIGURATION_VALUE, key);
        }
        return value;
    }

    public <T> T getConfigItem(Enum key, T defValue, ConfigurationManager.Formatter<T> formatter) {
        return getCfg().getConfigItem(key.name(), defValue, formatter);
    }

    public <T> T getConfigItem(Enum key, ConfigurationManager.Formatter<T> formatter) throws InteliaFunctionalException {
        T value = getCfg().getConfigItem(key.name(), null, formatter);
        if (value == null) {
            throw new InteliaFunctionalException(InteliaCoreError.CFG_NO_CONFIGURATION_VALUE, key);
        }
        return value;
    }

    public void exportConfiguration(Enum... keys) throws InteliaFunctionalException {
        for (Enum key : keys) {
            String value = getConfigItem(key);
            getBinding().setVariable(key.name(), value);
        }
    }

    public void exportConfiguration(String defaultValue, Enum... keys) {
        for (Enum key : keys) {
            String value = getConfigItem(key, defaultValue);
            getBinding().setVariable(key.name(), value);
        }
    }

    public <E extends Enum<E>> void exportConfiguration(Class<E> all) {
        exportConfiguration("", SharedSecrets.getJavaLangAccess().getEnumConstantsShared(all));
    }


    public SecureEntryDtoWrapper getSecureEntry(String secretVaultKey) throws InteliaFunctionalException {
        try {
            SecureEntryDtoWrapper credentials = new SecurityUtils(getBinding()).getSecureEntry(secretVaultKey);
            //check
            if (credentials != null) {
                return credentials;
            }
        } catch (Exception e) {
            throw new InteliaFunctionalException(e, InteliaCoreError.NO_SECURE_STORE_ERROR, secretVaultKey);
        }
        throw new InteliaFunctionalException(InteliaCoreError.NO_SECURE_STORE_ERROR, secretVaultKey);
    }

    public String getRunId() {
        return getRunId(getBinding());
    }

    public static String getRunId(Binding binding) {
        try {
            return ((WebHarvestTaskItem) BindingUtils.getWrappedObjFromContext(binding, "item")).getRun().getRootRunUuid();
        } catch (Exception e) {
        }
        return "";
    }

}
