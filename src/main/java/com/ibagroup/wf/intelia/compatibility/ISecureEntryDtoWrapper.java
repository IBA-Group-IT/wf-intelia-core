package com.ibagroup.wf.intelia.compatibility;

import java.util.Date;

@FunctionalInterface
public interface ISecureEntryDtoWrapper<T> {
	T wrap(String alias, String key, String value, Date lastUpdateDate);
}
