package com.ibagroup.wf.intelia.core.datastore;

import java.util.List;
import java.util.Map;
import com.ibagroup.wf.intelia.core.FlowContext;

/**
 * Lightweight base class for any DataStore object.
 * <ul>
 * Out of the box:
 * <li>{@link #getAll()} retrieves everything</li>
 * <li>{@link #getCachedAll()} retrieves everything (cached version checked first)</li>
 * <li>{@link #deleteAll()} deletes everything</li>
 * <li>{@link #insertRecord(Map)} inserts a record into data store</li>
 * </ul>
 * 
 * @see DataStoreAccessor
 * @see TODO Samples
 * @author dmitriev
 *
 */
public class BaseDS {

    private DataStoreAccessor dsAccessor;
    protected FlowContext flowContext;
    private List<Map<String, String>> all;
    private String dataStoreName;

    public BaseDS(FlowContext flowContext) {
        this.flowContext = flowContext;
    }

    public BaseDS(FlowContext flowContext, String dataStoreName) {
        this.flowContext = flowContext;
        this.dataStoreName = dataStoreName;
    }

    public BaseDS(FlowContext flowContext, DataStoreAccessor dsAccessor) {
        this.flowContext = flowContext;
        this.dsAccessor = dsAccessor;
    }

    public String getDSName() {
        return dataStoreName;
    }

    public DataStoreAccessor getDsAccessor() {
        if (dsAccessor == null) {
            dsAccessor = new DataStoreAccessor(flowContext, getDSName());
        }
        return dsAccessor;
    }

    protected List<Map<String, String>> getCachedAll() {
        if (all == null) {
            return getAll();
        }
        return all;
    }

    public List<Map<String, String>> getAll() {
        return all = getDsAccessor().selectAll();
    }

    public void deleteAll() {
        String query = "DELETE FROM @this";
        getDsAccessor().delete(query);
    }

    public void insertRecord(Map<String, String> recordForInput) {
        getDsAccessor().insertRow(recordForInput);
    }

}