package com.ibagroup.wf.intelia.core.datastore;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.AbstractDataStorePlugin;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.audit.DataStoreAuditContext;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.dto.DataStoreTransaction;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.service.DatabaseDataStoreServiceFactory;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.service.IRemoteDataStoreService;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.service.RemoteDataStoreServiceFactory;
import com.freedomoss.crowdcontrol.webharvest.web.WebServiceConnectionProperties;
import com.ibagroup.wf.intelia.core.BindingUtils;
import com.workfusion.utils.security.DatabaseProperties;
import groovy.lang.Binding;

public class DataStoreAccess {

    private static final RemoteDataStoreServiceFactory REMOTE_DATA_STORE_SERVICE_FACTORY = getRemoteDataStoreServiceFactory();
    private static final DatabaseDataStoreServiceFactory DATABASE_SERVICE_FACTORY = DatabaseDataStoreServiceFactory.getInstance();
    private DatabaseProperties dsProperties = null;
    private Binding binding = null;

    public DataStoreAccess(Binding binding) {
        super();
        this.binding = binding;
        this.dsProperties = BindingUtils.getTypedPropertyValue(binding, "dataStoreProperties");
    }

    public DataStoreAccess(Binding binding, String username, String password, String url) {
        super();
        this.binding = binding;
        this.dsProperties = new DatabaseProperties(username, password, url);
    }

    protected DataSource getDataSource() {
        return DATABASE_SERVICE_FACTORY.getDataSource(getDsProperties());
    }

    /**
     * This workaround/hacking solution allows to use plugin related RemoteDataStoreServiceFactory
     * that is replaced by `com\workfusion\studio\datastore\StudioDataStoreServiceFactory` when
     * running this code from WF Studio. This leads to using
     * `com\workfusion\studio\datastore\InMemoryDataStoreService` and ability to update data stories
     * locally.
     *
     * @see com.workfusion.studio.datastore.StudioDataStoreServiceFactory
     * @see com.workfusion.studio.datastore.InMemoryDataStoreService
     * @return RemoteDataStoreServiceFactory instance
     */
    private static RemoteDataStoreServiceFactory getRemoteDataStoreServiceFactory() {
        try {
            Field field = AbstractDataStorePlugin.class.getDeclaredField("REMOTE_DATA_STORE_SERVICE_FACTORY");
            Class<?> type = field.getType();
            field.setAccessible(true);
            return (RemoteDataStoreServiceFactory) field.get(type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new RemoteDataStoreServiceFactory();
    }

    protected IRemoteDataStoreService getRemoteDataStoreService(DatabaseProperties databaseProperties) {
        WebServiceConnectionProperties connectionProperties = BindingUtils.createBaseConnectionProperties(getBinding());
        return REMOTE_DATA_STORE_SERVICE_FACTORY.createRemoteDataStoreService(databaseProperties, connectionProperties);
    }

    public DatabaseProperties getDsProperties() {
        return dsProperties;
    }

    public Binding getBinding() {
        return binding;
    }

    protected DataStoreAuditContext createAuditContext() {
        return new DataStoreAuditContext("MACHINE", "Run : " + BindingUtils.getWebHarvestTaskItem(binding).getRun().getUuid());
    }

    @SuppressWarnings("unchecked")
    protected Map<String, DataStoreTransaction> getDataStoreTransactions() {
        Object dsTransObj = BindingUtils.getTypedPropertyValue(binding, "databaseTransactions");

        Map<String, DataStoreTransaction> result = null;
        if (dsTransObj != null) {
            result = (Map<String, DataStoreTransaction>) dsTransObj;
        } else {
            result = new HashMap<>();
            binding.setVariable("databaseTransactions", result);
        }
        return result;
    }

}
