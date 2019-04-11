package com.ibagroup.wf.intelia.core.security;

import com.freedomoss.crowdcontrol.webharvest.WebHarvestConstants;
import com.freedomoss.crowdcontrol.webharvest.plugin.security.provider.ISecureEntryProvider;
import com.freedomoss.crowdcontrol.webharvest.plugin.security.provider.SecureStoreProvider;
import com.freedomoss.crowdcontrol.webharvest.plugin.security.service.ISecureStoreService;
import com.freedomoss.crowdcontrol.webharvest.web.WebServiceConnectionProperties;
import com.freedomoss.crowdcontrol.webharvest.web.dto.SecureEntryDTO;
import com.ibagroup.wf.intelia.core.BindingUtils;
import com.ibagroup.wf.intelia.core.CommonConstants;
import com.ibagroup.wf.intelia.core.datastore.DataStoreQuery;
import com.ibagroup.wf.intelia.core.datastore.DataStoreQuery.RowItem;
import com.workfusion.service.CachedWebServiceFactory;
import com.workfusion.utils.security.Credentials;

import groovy.lang.Binding;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;

public class SecurityUtils {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    private static class AliasDs {
        private String appName;
        private String aliasName;

        private AliasDs() {
            super();
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getAppName() {
            return appName;
        }

        public void setAliasName(String aliasName) {
            this.aliasName = aliasName;
        }

        public String getAliasName() {
            return aliasName;
        }

        @Override
        public String toString() {
            return "AliasDs [appName=" + appName + ", aliasName=" + aliasName + "]";
        }
    }

    private List<SecureEntryDTO> secDs = new ArrayList<>();
    private List<AliasDs> aliasDs = null;
    private Binding binding = null;

    private DataStoreQuery dataStoreAccess;

    /**
     * @param binding - Binding implementation, usually from webharvest config
     */
    public SecurityUtils(Binding binding) {
        dataStoreAccess = new DataStoreQuery(binding);
        try {
            secDs = dataStoreAccess.executeQuery("SecureDataStore", "select * from @this;").getSelectResultAsListRows().get().stream().map(row -> {
                return parseSec(row);
            }).collect(Collectors.toList());

        } catch (Exception exp) {
            logger.info("SecureDataStore data store exists only localy");
        }

        this.aliasDs = dataStoreAccess.executeQuery("UserAliasesPerApplication", "select * from @this where status = '" + CommonConstants.INACTIVE + "';")
                .getSelectResultAsListRows().get().stream().map(row -> {
                    AliasDs aDs = new AliasDs();
                    for (Iterator<RowItem> iterator = row.iterator(); iterator.hasNext();) {
                        RowItem item = iterator.next();
                        switch (item.getColumn()) {
                            case "app_alias":
                                aDs.setAppName(item.getValue());
                                break;
                            case "security_alias":
                                aDs.setAliasName(item.getValue());
                                break;
                            default:
                                break;
                        }
                    }

                    return aDs;
                }).collect(Collectors.toList());
        this.binding = binding;
    }

    /**
     * @param appName - alias name to look up on datastore aliasDs
     * @return return List of SecureEntryDTO for security aliases stored in aliasDs for given
     *         application alias aliasName
     */
    public List<SecureEntryDTO> getListOfEntriesByAppName(String appName) {
        if (StringUtils.isBlank(appName)) {
            throw new IllegalArgumentException("Application alias name can't be empty");
        }

        List<SecureEntryDTO> result = aliasDs.stream().filter((AliasDs aDs) -> appName.equalsIgnoreCase(aDs.getAppName()) ? true : false)
                .map((AliasDs aDs) -> getSecureEntry(aDs.getAliasName())).collect(Collectors.toList());
        logger.info("Users list: " + result.toString());
        return result;
    }

    /**
     * @param appName - appName for creds
     * @return random SecureEntryDTO for given alias
     */
    public SecureEntryDTO getAnyCred(String appName) {
        return getListOfEntriesByAppName(appName).stream().findAny().orElseGet(null);
    }

    private ISecureStoreService getService() {

       // SecureStoreServiceFactory secureStoreServiceFactory = SecureStoreServiceFactory.getInstance();
    	CachedWebServiceFactory secureStoreServiceFactory = CachedWebServiceFactory.getInstance();
        WebServiceConnectionProperties connectionProperties = new WebServiceConnectionProperties();
        Credentials credentials = BindingUtils.getUserCredentials(binding);
        connectionProperties.setCredentials(credentials);

        String applicationHostString = BindingUtils.getApplicationHost(binding);
        connectionProperties.setWorkfusionHost(applicationHostString);
        String contextPathString = BindingUtils.getApplicationContextPath(binding);
        connectionProperties.setWorkfusionUrl(contextPathString);

        ISecureStoreService secureStoreService = secureStoreServiceFactory.getOrCreateSecureStoreService(connectionProperties);

        return secureStoreService;
    }

    private ISecureEntryProvider getPasswordProvider(String providerId) {
        Map<?, ?> securityProviderMap = (Map<?, ?>) BindingUtils.getWrappedObjFromContext(binding, WebHarvestConstants.SECURITY_PROVIDER_MAP);

        ISecureEntryProvider provider = securityProviderMap == null ? null : (ISecureEntryProvider) securityProviderMap.get(providerId);

        if (provider == null) {
            //provider = new SecureStoreProvider(getService(), BindingUtils.getPropertyValue(binding, WebHarvestConstants.SECURE_STORE_PASSWORD));
            provider = new SecureStoreProvider(getService());

        }
        return provider;
    }

    private SecureEntryDTO getSecureEntryInternal(String provider, String aliasString) {
        logger.info("Trying to get securityDTO for: " + aliasString);

        return secDs.stream().filter((sec) -> sec.getAlias().equalsIgnoreCase(aliasString)).findAny().orElseGet(() -> {

            ISecureEntryProvider entryProvider = getPasswordProvider(provider);
            logger.info("entryProvider: " + entryProvider.toString());
            Map<String, Object> params = new HashMap<>();
            params.put(ISecureEntryProvider.PARAM_ALIAS, aliasString);

            return new SecureEntryDtoWrapper(entryProvider.getUserSecureEntry(params));
        });

    }

    /**
     * @param aliasString - alias name in secure data store
     * @return return Item from Security Storage for specific <code>aliasString</code>
     */
    public SecureEntryDTO getSecureEntry(String aliasString) {
        SecureEntryDTO entryInternalResult = getSecureEntryInternal(null, aliasString);
        logger.info("entryInternalResult: " + entryInternalResult);
        try {
            if (entryInternalResult == null) {
                ISecureStoreService service = ContextLoader.getCurrentWebApplicationContext().getBean(ISecureStoreService.class);
                // SecureEntry
                logger.info("service:" + service.toString());
                //entryInternalResult = service.getEntry(aliasString, BindingUtils.getPropertyValue(binding, WebHarvestConstants.SECURE_STORE_PASSWORD));
                entryInternalResult = service.getEntry(aliasString);
            }
        } catch (Throwable e) {
            logger.error(e.toString());
        }

        return entryInternalResult;
    }

    /**
     * Update status column in UserAliasesPerApplication data store.
     *
     * @param appAlias
     * @param securityAlias
     * @param status
     */
    public void updateUserAliasesPerApplication(String appAlias, String securityAlias, String status) {
        String updateQuery = "UPDATE @this SET status =\'" + status + "\' WHERE app_alias =\'" + appAlias + "\' and security_alias=\'" + securityAlias + "\'";
        dataStoreAccess.executeQuery("UserAliasesPerApplication", updateQuery);
    }

    private SecureEntryDTO parseSec(List<RowItem> rowList) {
        SecureEntryDTO secResult = new SecureEntryDtoWrapper();
        for (Iterator<RowItem> iterator = rowList.iterator(); iterator.hasNext();) {
            RowItem item = iterator.next();
            switch (item.getColumn()) {
                case "Alias":
                    secResult.setAlias(item.getValue());
                    break;
                case "Key":
                    secResult.setKey(item.getValue());
                    break;
                case "Value":
                    secResult.setValue(item.getValue());
                    break;
                case "Last_Update_Date":
                    secResult.setLastUpdateDate(new SimpleDateFormat("MM.dd.yyyy HH:mm").parse(item.getValue(), new ParsePosition(0)));
                    break;
                default:
                    break;
            }
        }

        return secResult;
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
		ISecureStoreService service = getService();

		isSuccessful = service.updateEntry(alias, key, value);
		logger.info("- SecureStorageUpdate - isSaveSuccessful = {} ", isSuccessful);

		return isSuccessful;
	}
}
