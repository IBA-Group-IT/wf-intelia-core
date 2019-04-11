package com.ibagroup.wf.intelia.core.security;

import com.drew.lang.annotations.SuppressWarnings;
import com.freedomoss.crowdcontrol.webharvest.web.dto.SecureEntryDTO;

public class SecureEntryDtoWrapper extends SecureEntryDTO {

    public SecureEntryDtoWrapper() {
        super();
    }

    public SecureEntryDtoWrapper(String alias) {
        super(alias);
    }

    public SecureEntryDtoWrapper(SecureEntryDTO dto) {
        setAlias(dto.getAlias());
        setKey(dto.getKey());
        setLastUpdateDate(dto.getLastUpdateDate());
        setValue(dto.getValue());
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

    @SuppressWarnings(value = "BC_EQUALS_METHOD_SHOULD_WORK_FOR_ALL_OBJECTS",
            justification = "This class can be used in groove webharvest config. And as webharvest can use different classloaders for each groovy scripts inside one config, instance of can return false for vene same object")

    @Override
    public boolean equals(Object second) {
        if (second == null) {
            return false;
        }

        SecureEntryDTO obj = (SecureEntryDTO) second;

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
        return "SecureEntryDtoWrapper - " + super.toString();
    };
}
