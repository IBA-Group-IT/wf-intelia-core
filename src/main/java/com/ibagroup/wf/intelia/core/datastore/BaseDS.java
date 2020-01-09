package com.ibagroup.wf.intelia.core.datastore;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
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
 * <p>
 * Could be used on its own provided the DEFAULT_DATASTORE_PARAM_NAME exists in the dependency
 * context
 * </p>
 * 
 * @see DataStoreAccessor
 * @see TODO Samples
 * @author dmitriev
 *
 */
public class BaseDS {
    public final static String DEFAULT_DATASTORE_PARAM_NAME = "ds_name";

    protected FlowContext flowContext;
    private List<Map<String, String>> all;
    private String dataStoreName;
    private DataStoreAccessor dsAccessor;
    private boolean create = false;

    @Inject
    public BaseDS(FlowContext flowContext, @Named(DEFAULT_DATASTORE_PARAM_NAME) String dataStoreName) {
        this.flowContext = flowContext;
        this.dataStoreName = dataStoreName;
    }

    public BaseDS(FlowContext flowContext, String dataStoreName, boolean create) {
        this(flowContext, dataStoreName);
        this.create = create;
    }

    /**
     * Usually used as a Junit mock entry point
     * 
     * @param flowContext
     * @param dsAccessor
     */
    public BaseDS(FlowContext flowContext, DataStoreAccessor dsAccessor) {
        this.flowContext = flowContext;
        this.dsAccessor = dsAccessor;
    }

    public String getDSName() {
        return dataStoreName;
    }

    public DataStoreAccessor getDsAccessor() {
        if (dsAccessor == null) {
            dsAccessor = new DataStoreAccessor(flowContext, getDSName(), create);
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
