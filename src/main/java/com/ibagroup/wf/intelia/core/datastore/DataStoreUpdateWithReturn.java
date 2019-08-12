package com.ibagroup.wf.intelia.core.datastore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.service.IRemoteDataStoreService;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.util.DataStoreUtils;
import com.ibagroup.wf.intelia.core.datastore.DataStoreQuery.RowItem;
import groovy.lang.Binding;

/**
 * <p>
 * 2 in 1 - updates the table and returns what was updated.<br>
 * Basically its a very handy replacement for Select For Update + Update.
 * </p>
 * <b>Note: </b>Works with PostGre ONLY !!!
 * 
 * @author dmitriev
 *
 */
public class DataStoreUpdateWithReturn extends DataStoreAccess {

    public DataStoreUpdateWithReturn(Binding binding) {
        super(binding);
    }

    public List<Map<String, String>> executeUpdateWithReturn(String dsName, String query) {
        try {
            String preparedQuery = DataStoreUtils.replaceQueryPlaceholders(query, dsName);
            IRemoteDataStoreService remoteDataStoreService = getRemoteDataStoreService(getDsProperties());
            return remoteDataStoreService.executeSelectQuery(-1, preparedQuery).stream().map(DataStoreQuery::convertRowToStringRowList).map(this::listAsMap).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> listAsMap(List<RowItem> rowItems) {
        Map<String, String> rowMap = new HashMap<>();
        // BEWARE: rowItem can have null as value - this will break Collector.toMap
        rowItems.stream().forEach(rowItem -> rowMap.put(rowItem.getColumn(), rowItem.getValue()));
        return rowMap;
    }
}