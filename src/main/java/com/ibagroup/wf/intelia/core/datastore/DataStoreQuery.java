package com.ibagroup.wf.intelia.core.datastore;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.audit.DataStoreAuditContext;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.dto.DbColumnDescriptionDTO;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.dto.DbRowDTO;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.service.IRemoteDataStoreService;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.util.DataStoreUtils;
import groovy.lang.Binding;

public class DataStoreQuery extends DataStoreAccess {

    private int maxRows = -1;
    
    public static final String QUERY_SELECT_ALL = "select * from @this;";

    public DataStoreQuery(Binding binding) {
        super(binding);
    }

    public DataStoreQuery(Binding binding, String username, String password, String url) {
        super(binding, username, password, url);
    }

    private String prepareQuery(String query, String dsName) {
        return DataStoreUtils.replaceQueryPlaceholders(query, dsName);
    }

    public QueryResult executeQuery(String query) {
        return executeQuery(query, null);
    }

    public QueryResult executeQuery(String dsName, String query_) {
        String _query = prepareQuery(query_, dsName);
        List<DbRowDTO> select = null;
        int rows = -1;
        boolean executed = true;
        IRemoteDataStoreService remoteDataStoreService = getRemoteDataStoreService(getDsProperties());
        try {
            if (remoteDataStoreService.isSelectQuery(_query)) {
                select = remoteDataStoreService.executeSelectQuery(maxRows, _query);
            } else if ((remoteDataStoreService.isUpdateQuery(_query) || remoteDataStoreService.isDeleteQuery(_query))) {
                DataStoreAuditContext auditContext = this.createAuditContext();
                rows = remoteDataStoreService.executeUpdateQuery(_query, dsName, auditContext);
            } else {
                executed = remoteDataStoreService.executeQuery(_query);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return new QueryResult(select != null ? select.size() : rows, select, executed);
    }

    public static List<RowItem> convertRowToStringRowList(DbRowDTO row) {
        if (row != null) {
            List<RowItem> entries = new ArrayList<>();
            DbColumnDescriptionDTO[] desc = row.getColumnDescriptions();
            for (int i = 0; i < desc.length; i++) {
                if (!StringUtils.isBlank(desc[i].getName())) {
                    String value = row.getRowData()[i] != null ? row.getRowData()[i].toString() : null;
                    entries.add(new RowItem(desc[i].getName(), value));
                }
            }

            return entries;
        }
        return null;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public static class QueryResult {
        private final int numberOfRowsAffected;
        private final List<DbRowDTO> select;
        private final boolean executed;

        public QueryResult(int numberOfRowsAffected, List<DbRowDTO> select, boolean executed) {
            super();
            this.numberOfRowsAffected = numberOfRowsAffected;
            this.select = select;
            this.executed = executed;
        }

        public int getNumberOfRowsAffected() {
            return numberOfRowsAffected;
        }

        public Optional<List<DbRowDTO>> getSelectResult() {
            return Optional.ofNullable(select);
        }

        public Optional<List<List<RowItem>>> getSelectResultAsListRows() {
            if (null != select) {
                List<List<RowItem>> collect = select.stream().map((row) -> convertRowToStringRowList(row)).collect(Collectors.toList());
                return Optional.of(collect);
            }
            return Optional.empty();
        }

        public Optional<List<Map<String, String>>> getSelectResultAsMapRows() {
            if (null != select) {
                List<Map<String, String>> collect = new ArrayList<>();
                for (DbRowDTO row : select) {
                    Map<String, String> record = new HashMap<>();
                    List<RowItem> rec = convertRowToStringRowList(row);
                    for (RowItem item : rec) {
                        record.put(item.getColumn(), item.getValue());
                    }
                    collect.add(record);
                }
                return Optional.of(collect);
            }
            return Optional.empty();
        }

        public boolean isExecuted() {
            return executed;
        }
    }

    public static class RowItem {
        private final String column;
        private final String value;

        public RowItem(String column, String value) {
            super();
            this.column = column;
            this.value = value;
        }

        public String getColumn() {
            return column;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "RowItem [column=" + column + ", value=" + value + "]";
        }
    }

}
