package com.ibagroup.wf.intelia.core.datastore;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.ibagroup.wf.intelia.core.FlowContext;

/**
 * Datastore access aggregator class
 * <ul>
 * Out of the box:
 * <li>{@link #select(String)} does SQL SELECT against a datastore</li>
 * <li>{@link #insertRow(Map)} does SQL INSERT against a datastore</li>
 * <li>{@link #updateWithReturn(String)} does SQL UPDATE .. RETURNING against a datastore</li>
 * <li>{@link #update(String)} does SQL UPDATE against a datastore</li>
 * <li>{@link #deleteRow(String)} does SQL DELETE against a datastore</li>
 * </ul>
 * 
 * @see BaseDS
 * @see TODO Samples
 * 
 * @author dmitriev
 *
 */
public class DataStoreAccessor {

    private FlowContext flowContext;
    private DataStoreQuery dataStoreQuery;
    private DataStoreInsert dataStoreInsert;
    private DataStoreUpdateWithReturn dataStoreUpdateWithReturn;
    private String dsName;

    public DataStoreAccessor(FlowContext flowContext, String dsName) {
        this(flowContext, new DataStoreQuery(flowContext.getBinding()), new DataStoreInsert(flowContext.getBinding()), new DataStoreUpdateWithReturn(flowContext.getBinding()),
                dsName);
    }

    public DataStoreAccessor(FlowContext flowContext, DataStoreQuery dataStoreQuery, DataStoreInsert dataStoreInsert, DataStoreUpdateWithReturn dataStoreUpdateWithReturn,
            String dsName) {
        this.flowContext = flowContext;
        this.dataStoreQuery = dataStoreQuery;
        this.dataStoreInsert = dataStoreInsert;
        this.dataStoreUpdateWithReturn = dataStoreUpdateWithReturn;
        this.dsName = dsName;
    }

    public List<Map<String, String>> selectAll() {
        return select("SELECT * FROM @this");
    }

    public List<Map<String, String>> select(String selectQuery) {
        try {
            return dataStoreQuery.executeQuery(dsName, selectQuery).getSelectResultAsMapRows().orElse(Collections.emptyList());
        } catch (RuntimeException e) {
            flowContext.debug("Selecting from Data Store {} with query {}", dsName, selectQuery);
            flowContext.error("Error selecting data from {}:{}", dsName, e.getMessage());
            throw e;
        }
    }

    public DataStoreAccessor insertRow(Map<String, String> row) {
        try {
            dataStoreInsert.insertRow(dsName, row);
            return this;
        } catch (RuntimeException e) {
            flowContext.debug("Inserting row into data store {}: {}", dsName, row);
            flowContext.error("Error inserting row to {}:{}", dsName, e.getMessage());
            throw e;
        }
    }

    public List<Map<String, String>> updateWithReturn(String updateWithReturnQuery) {
        try {
            return dataStoreUpdateWithReturn.executeUpdateWithReturn(dsName, updateWithReturnQuery);
        } catch (RuntimeException e) {
            flowContext.debug("Updating rows in data store {}: {}", dsName, updateWithReturnQuery);
            flowContext.error("Error updating rows in data store {}:{}", dsName, e.getMessage());
            throw e;
        }
    }

    public int update(String updateQuery) {
        try {
            return dataStoreQuery.executeQuery(dsName, updateQuery).getNumberOfRowsAffected();
        } catch (RuntimeException e) {
            flowContext.debug("Updating rows in data store {}: {}", dsName, updateQuery);
            flowContext.error("Error updating rows in data store {}:{}", dsName, e.getMessage());
            throw e;
        }
    }

    public void delete(String deleteQuery) {
        try {
            dataStoreQuery.executeQuery(dsName, deleteQuery);
        } catch (RuntimeException e) {
            flowContext.debug("Deleting rows in data store {}: {}", dsName, deleteQuery);
            flowContext.error("Error deleting rows in data store {}:{}", dsName, e.getMessage());
            throw e;
        }
    }
}
