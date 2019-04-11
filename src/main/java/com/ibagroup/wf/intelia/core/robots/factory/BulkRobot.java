package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibagroup.wf.intelia.core.CommonConstants;
import com.ibagroup.wf.intelia.core.annotations.OnError;
import com.ibagroup.wf.intelia.core.metadata.MetadataManager;
import com.ibagroup.wf.intelia.core.to.BaseTO;

public interface BulkRobot {
    String BULK_COLUMN = "bulkrows";

    List getItems();

    void performRecord(Object row);

    MetadataManager getMetadataManager();

    default void performBulk() {
        getFilteredItems().forEach((row) -> {
        	RunnerContext.setRecordUuid(((BaseTO)row).getRecordUuid());
        	performRecord(row);
    	});
    }

	default String getOutputItems() {
		Gson gson = new GsonBuilder().create();
		if (getItems().get(0).getClass().isAssignableFrom(BaseTO.class)) {
			Type type = new ListParameterizedType(getItems().get(0).getClass());
			return gson.toJson(getItems(), type);
		} else {
			return gson.toJson(getItems(), List.class);
		}
	}

    @SuppressWarnings("unchecked")
	default List getFilteredItems(String... acceptStatuses) {
        List<String> matchedStatuses = new ArrayList<>();
        if (null == acceptStatuses || acceptStatuses.length == 0) {
            matchedStatuses.add(CommonConstants.SUCCESS_CLMN_PROCEED);
            matchedStatuses.add(null);
            matchedStatuses.add("^\\s*$"); // string contains from white spaces only, e.g. " "
        } else {
            matchedStatuses.addAll(Arrays.asList(acceptStatuses));
        }

        return (List) getItems().stream()
        		.filter(row -> isSuccessStatusMatched(matchedStatuses, row))
        		.collect(Collectors.toList());
    }

    default boolean isSuccessStatusMatched(List<String> matchedStatuses, Object row) {
        return matchedStatuses.stream().anyMatch(status -> {
            return status != null && CommonConstants.SUCCESS_CLMN_PROCEED.matches(status);
        });
    }

    @OnError
    default boolean handleError(Object self, Method thisMethod, Method proceed, Throwable throwable, Object[] args) {
//        Map<String, String> row = extractRow(args);
//        row.put(CommonConstants.SUCCESS_CLMN, CommonConstants.SUCCESS_CLMN_FAILED);
//        row.put(CommonConstants.LAST_ERROR, this.getLastError());
//        row.put(CommonConstants.ERR_MESSAGE_FIELD, ExceptionUtils.getMessage(throwable) + ": " + ExceptionUtils.getStackTrace(throwable));
        return true;
    }

    /**
     * @deprecated since framework version 1.0.13
     */
    default String getLastError() {
        return "Exception occured.";
    }
    
    static class ListParameterizedType implements ParameterizedType {

		private Type type;

	    private ListParameterizedType(Type type) {
	        this.type = type;
	    }

	    @Override
	    public Type[] getActualTypeArguments() {
	        return new Type[] {type};
	    }

	    @Override
	    public Type getRawType() {
	        return ArrayList.class;
	    }

	    @Override
	    public Type getOwnerType() {
	        return null;
	    }

	}
    
}