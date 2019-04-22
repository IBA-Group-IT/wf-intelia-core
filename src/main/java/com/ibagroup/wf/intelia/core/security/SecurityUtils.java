package com.ibagroup.wf.intelia.core.security;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibagroup.wf.intelia.core.CommonConstants;
import com.ibagroup.wf.intelia.core.datastore.DataStoreQuery;
import com.ibagroup.wf.intelia.core.datastore.DataStoreQuery.RowItem;
import groovy.lang.Binding;

/**
 * Machine-Agnostic Security Utils.
 * <p>
 * <b> No machine classes use is allowed </b>
 * </p>
 * 
 * @author dmitriev
 *
 */
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

    private List<SecureEntryDtoWrapper> secDs = new ArrayList<>();
    private List<AliasDs> aliasDs = null;

    private DataStoreQuery dataStoreAccess;

    private WfSecurityBridge wfSecurityBridge;

    private Binding binding;

    /**
     * @param binding - Binding implementation, usually from webharvest config
     */
    public SecurityUtils(Binding binding) {
        this.binding = binding;
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
    }

    /**
     * @param appName - alias name to look up on datastore aliasDs
     * @return return List of SecureEntryDtoWrapper for security aliases stored in aliasDs for given
     *         application alias aliasName
     */
    public List<SecureEntryDtoWrapper> getListOfEntriesByAppName(String appName) {
        if (StringUtils.isBlank(appName)) {
            throw new IllegalArgumentException("Application alias name can't be empty");
        }

        List<SecureEntryDtoWrapper> result = aliasDs.stream().filter((AliasDs aDs) -> appName.equalsIgnoreCase(aDs.getAppName()) ? true : false)
                .map((AliasDs aDs) -> getSecureEntry(aDs.getAliasName())).collect(Collectors.toList());
        logger.info("Users list: " + result.toString());
        return result;
    }

    /**
     * @param appName - appName for creds
     * @return random SecureEntryDtoWrapper for given alias
     */
    public SecureEntryDtoWrapper getAnyCred(String appName) {
        return getListOfEntriesByAppName(appName).stream().findAny().orElseGet(null);
    }

    /**
     * @param aliasString - alias name in secure data store
     * @return return Item from Security Storage for specific <code>aliasString</code>
     */
    public SecureEntryDtoWrapper getSecureEntry(String aliasString) {
        logger.info("Trying to get securityDTO for: " + aliasString);

        return secDs.stream().filter((sec) -> sec.getAlias().equalsIgnoreCase(aliasString)).findAny().orElseGet(() -> {
            return getWfSecurityBridge().getUserSecureEntry(null, aliasString);
        });

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

    private SecureEntryDtoWrapper parseSec(List<RowItem> rowList) {
        SecureEntryDtoWrapper secResult = new SecureEntryDtoWrapper();
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

    public WfSecurityBridge getWfSecurityBridge() {
        if (wfSecurityBridge == null) {
            wfSecurityBridge = new WfSecurityBridge(binding);
        }
        return wfSecurityBridge;
    }

    public void setWfSecurityBridge(WfSecurityBridge wfSecurityBridge) {
        this.wfSecurityBridge = wfSecurityBridge;
    }

}
