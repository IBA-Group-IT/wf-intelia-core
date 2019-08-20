package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;
import com.ibagroup.wf.intelia.core.CommonConstants;
import com.ibagroup.wf.intelia.core.adaptations.MachineVersionAdaptations;
import com.ibagroup.wf.intelia.core.security.SecureEntryDtoWrapper;
import com.ibagroup.wf.intelia.core.security.SecurityUtils;
import groovy.lang.Binding;

public class SecurityMethodWrapper extends ChainMethodWrapper {

    private final Binding binding;

    public SecurityMethodWrapper(Binding binding) {
        this.binding = binding;
    }

    public boolean isHandled(Method m) {
        return PerformMethodWrapper.isPerformMethod.test(m);
    }

    @Override
    Object wrap(Invocation invocation) throws Throwable {
        SecureEntryDtoWrapper secureEntryDTO = extractSecureEntry(invocation.getArgs());
        SecurityUtils securityUtils = null;
        try {
            if (secureEntryDTO != null) {
                securityUtils = new SecurityUtils(binding);
                securityUtils.updateUserAliasesPerApplication(secureEntryDTO.getAlias(), secureEntryDTO.getKey(), CommonConstants.ACTIVE);
            }
            return invokeInner(invocation);

        } finally {
            if (secureEntryDTO != null) {
                securityUtils.updateUserAliasesPerApplication(secureEntryDTO.getAlias(), secureEntryDTO.getKey(), CommonConstants.INACTIVE);
            }
        }
    }


    protected SecureEntryDtoWrapper extractSecureEntry(Object[] args) {
        if (null != args) {
            for (int i = 0; i < args.length; i++) {
                if (null != args[i] && "SecureEntryDTO".equals(args[i].getClass().getSimpleName())) {
                    return MachineVersionAdaptations.wrap(args[i], SecureEntryDtoWrapper.WRAPPER);
                }
            }
        }

        return null;
    }
}
