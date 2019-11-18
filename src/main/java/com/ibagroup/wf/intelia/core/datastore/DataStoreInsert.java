package com.ibagroup.wf.intelia.core.datastore;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.dto.DataStoreRow;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.dto.DataStoreValue;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.enums.DataStoreColumnType;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.exception.CreateDataStorePluginException;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.exception.DataStoreProcessingException;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.service.IRemoteDataStoreService;
import com.freedomoss.workfusion.utils.gson.GsonUtils;
import com.google.gson.reflect.TypeToken;
import groovy.lang.Binding;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class DataStoreInsert extends DataStoreAccess {

    public DataStoreInsert(Binding binding, String username, String password, String url) {
        super(binding, username, password, url);
    }

    private static final Type TYPE_DATASTORE_ROW = new TypeToken<DataStoreRow>() {
    }.getType();

    public DataStoreInsert(Binding binding, Connection connection) {
        super(binding, connection);
    }

    public DataStoreInsert(Binding binding) {
        super(binding, null);
    }

    private boolean create = false;

    public void setCreate(boolean create) {
        this.create = create;
    }

    public long insertRow(String dsName, Map<String, String> row) {
        return insertRow(dsName, generateDataStoreRow(row));
    }

    public long insertRow(String dsName, String json) {
        return insertRow(dsName, generateDataStoreRow(json));
    }

    public long insertRow(String dsName, DataStoreRow dsRow) {
        DataStoreRow row = processDataStoreRowData(dsRow);
        IRemoteDataStoreService remoteDataStoreService = getRemoteDataStoreService(getDsProperties());
        if (create && !remoteDataStoreService.createOrUpdate(dsName, row.getColumnsDescription())) {
            throw new CreateDataStorePluginException("Failed to create Data Store " + dsName);
        }

        Long recordId;
        try {
            if (connection != null) {
                recordId = remoteDataStoreService.insertRow(dsName, row, connection, createAuditContext());
            } else {
                recordId = remoteDataStoreService.insertRow(dsName, row, createAuditContext());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (recordId == null) {
            throw new DataStoreProcessingException("Failed to insert data into Data Store");
        } else {
            return recordId;
        }
    }

    private DataStoreRow generateDataStoreRow(String jsonValue) {
        if (jsonValue.contains("columnType") && jsonValue.contains("columnName")) {
            return this.processDataStoreRowData(GsonUtils.GSON.<DataStoreRow>fromJson(jsonValue, TYPE_DATASTORE_ROW));
        } else {
            Map<String, String> valueMap2 = GsonUtils.GSON.fromJson(jsonValue, GsonUtils.TYPE_MAP_STRING_STRING);
            return this.generateDataStoreRow(valueMap2);
        }
    }

    private DataStoreRow generateDataStoreRow(Map<String, String> valueMap) {
        DataStoreRow result = null;
        if (valueMap != null) {
            result = new DataStoreRow();
            Iterator<Entry<String, String>> arg2 = valueMap.entrySet().iterator();

            while (arg2.hasNext()) {
                Entry<String, String> entry = arg2.next();
                result.addValue(entry.getKey().trim().toLowerCase(), DataStoreColumnType.TEXT, entry.getValue());
            }
        }

        return result;
    }

    private DataStoreRow processDataStoreRowData(DataStoreRow dataStoreRow) {
        if (dataStoreRow != null) {
            Iterator<DataStoreValue> arg2 = dataStoreRow.getValues().iterator();

            while (arg2.hasNext()) {
                DataStoreValue value = arg2.next();
                value.setColumnName(value.getColumnName().trim().toLowerCase());
                if (value.getColumnType() == null) {
                    value.setColumnType(DataStoreColumnType.TEXT);
                }
            }
        }

        return dataStoreRow;
    }
}
