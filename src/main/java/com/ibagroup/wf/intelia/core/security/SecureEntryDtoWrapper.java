package com.ibagroup.wf.intelia.core.security;

import java.util.Date;
import com.drew.lang.annotations.SuppressWarnings;
import com.ibagroup.wf.intelia.compatibility.ISecureEntryDtoWrapper;

public class SecureEntryDtoWrapper {

	public static ISecureEntryDtoWrapper<SecureEntryDtoWrapper> WRAPPER = (a, k, v, d) -> new SecureEntryDtoWrapper(a, k, v, d);

	public String alias;
	public String key;
	public String value;
	public Date lastUpdateDate;

	public SecureEntryDtoWrapper() {
	}

	public SecureEntryDtoWrapper(String alias, String key, String value, Date lastUpdateDate) {
		this.alias = alias;
		this.key = key;
		this.value = value;
		this.lastUpdateDate = lastUpdateDate;
	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getLastUpdateDate() {
		return this.lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getAlias() == null) ? 0 : getAlias().hashCode());
		result = prime * result + ((getKey() == null) ? 0 : getKey().hashCode());
		result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
		return result;
	}

	@SuppressWarnings(value = "BC_EQUALS_METHOD_SHOULD_WORK_FOR_ALL_OBJECTS", justification = "This class can be used in groove webharvest config. And as webharvest can use different classloaders for each groovy scripts inside one config, instance of can return false for vene same object")

	@Override
	public boolean equals(Object second) {
		if (second == null) {
			return false;
		}

		SecureEntryDtoWrapper obj = (SecureEntryDtoWrapper) second;

		if (getAlias() == null) {
			if (obj.getAlias() != null) {
				return false;
			}
		} else if (!getAlias().equals(obj.getAlias())) {
			return false;
		}

		if (getKey() == null) {
			if (obj.getKey() != null) {
				return false;
			}
		} else if (!getKey().equals(obj.getKey())) {
			return false;
		}

		if (getValue() == null) {
			if (obj.getValue() != null) {
				return false;
			}
		} else if (!getValue().equals(obj.getValue())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SecureEntryDtoWrapper [alias=" + this.alias + ", lastUpdateDate=" + this.lastUpdateDate + "]";
	}

}
