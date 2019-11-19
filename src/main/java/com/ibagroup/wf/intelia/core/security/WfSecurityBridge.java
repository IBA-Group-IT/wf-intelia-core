package com.ibagroup.wf.intelia.core.security;

import static com.ibagroup.wf.intelia.core.adaptations.MachineVersionAdaptations.getISecureStoreServiceClass;
import static com.ibagroup.wf.intelia.core.adaptations.MachineVersionAdaptations.wrap;
import static com.ibagroup.wf.intelia.core.security.SecureEntryDtoWrapper.WRAPPER;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import com.freedomoss.crowdcontrol.webharvest.WebHarvestConstants;
import com.freedomoss.crowdcontrol.webharvest.plugin.security.provider.ISecureEntryProvider;
import com.freedomoss.crowdcontrol.webharvest.plugin.security.provider.SecureStoreProvider;
import com.freedomoss.crowdcontrol.webharvest.web.WebServiceConnectionProperties;
import com.ibagroup.wf.intelia.core.utils.BindingUtils;
import com.workfusion.service.CachedWebServiceFactory;
import com.workfusion.utils.security.Credentials;
import groovy.lang.Binding;

/**
 * Bridge to Workfusion Machine Security features.
 * <ul>
 * <li>Hides internal machine guts and exposes machine-agnostic entities</li>
 * <li>Should not be used outside core</li>
 * <li>Should import only 'stable' WF classes that does not change across machine versions</li>
 * </ul>
 * 
 * @author dmitriev
 *
 */
public class WfSecurityBridge {

    private static final Logger logger = LoggerFactory.getLogger(WfSecurityBridge.class);

    private Binding binding = null;

    /**
     * @param binding - Binding implementation, usually from webharvest config
     */
    public WfSecurityBridge(Binding binding) {
        super();
        this.binding = binding;
    }

    public SecureEntryDtoWrapper getUserSecureEntry(String providerId, String alias) {
        Map<?, ?> securityProviderMap = (Map<?, ?>) BindingUtils.getWrappedObjFromContext(binding, WebHarvestConstants.SECURITY_PROVIDER_MAP);

        ISecureEntryProvider entryProvider = securityProviderMap == null ? null : (ISecureEntryProvider) securityProviderMap.get(providerId);

        if (entryProvider == null) {
            entryProvider = new SecureStoreProvider(getService(getISecureStoreServiceClass()));
        }
        logger.info("entryProvider: " + entryProvider.toString());
        Map<String, Object> params = new HashMap<>();
        params.put(ISecureEntryProvider.PARAM_ALIAS, alias);

        SecureEntryDtoWrapper entry = wrap(entryProvider.getUserSecureEntry(params), WRAPPER);

        try {
            if (entry == null) {
                entry = wrap(getServiceFromWebAppContext(getISecureStoreServiceClass()).getEntry(alias), WRAPPER);
            }
        } catch (Throwable e) {
            logger.error(e.toString());
        }
        return entry;
    }

    /**
     * Update the secured storage entry
     * 
     * @param alias
     * @param key
     * @param value
     * @return
     */
    public boolean updateEntry(String alias, String key, String value) {
        boolean isSuccessful = true;
        isSuccessful = getService(getISecureStoreServiceClass()).updateEntry(alias, key, value);
        logger.info("- SecureStorageUpdate - isSaveSuccessful = {} ", isSuccessful);

        return isSuccessful;
    }

    private <T> T getServiceFromWebAppContext(Class<T> claz) {
        return ContextLoader.getCurrentWebApplicationContext().getBean(claz);
    }

    private <T> T getService(Class<T> claz) {
        WebServiceConnectionProperties connectionProperties = new WebServiceConnectionProperties();
        Credentials credentials = BindingUtils.getUserCredentials(binding);
        connectionProperties.setCredentials(credentials);

        String applicationHostString = BindingUtils.getApplicationHost(binding);
        connectionProperties.setWorkfusionHost(applicationHostString);
        String contextPathString = BindingUtils.getApplicationContextPath(binding);
        connectionProperties.setWorkfusionUrl(contextPathString);
        return (T) CachedWebServiceFactory.getInstance().getOrCreateSecureStoreService(connectionProperties);
    }

}
