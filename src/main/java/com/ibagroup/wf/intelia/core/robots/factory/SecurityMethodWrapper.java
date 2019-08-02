package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Method;
import java.util.function.Predicate;
import com.freedomoss.crowdcontrol.webharvest.web.dto.SecureEntryDTO;
import com.ibagroup.wf.intelia.core.CommonConstants;
import com.ibagroup.wf.intelia.core.security.SecurityUtils;
import groovy.lang.Binding;

public class SecurityMethodWrapper extends ChainMethodWrapper {

    public final static Predicate<Method> isPerformMethod = m -> "perform".equalsIgnoreCase(m.getName());

    private final Binding binding;

    public SecurityMethodWrapper(Binding binding) {
        this.binding = binding;
    }

    public boolean isHandled(Method m) {
        return isPerformMethod.test(m);
    }

    @Override
    Object wrap(Invocation invocation) throws Throwable {
        // TODO verify and refactor old webharvest SecureEntryDTO ref
        SecureEntryDTO secureEntryDTO = extractSecureEntry(invocation.getArgs());
        SecurityUtils securityUtils = null;
        try {
            if (secureEntryDTO != null) {
                securityUtils = new SecurityUtils(binding);
                securityUtils.updateUserAliasesPerApplication(secureEntryDTO.getAlias(), secureEntryDTO.getKey(), CommonConstants.ACTIVE);
            }
            return invokeNext(invocation);
        
        } finally {
            if (secureEntryDTO != null) {
                securityUtils.updateUserAliasesPerApplication(secureEntryDTO.getAlias(), secureEntryDTO.getKey(), CommonConstants.INACTIVE);
            }
        }
    }


    protected SecureEntryDTO extractSecureEntry(Object[] args) {
        if (null != args) {
            for (int i = 0; i < args.length; i++) {
                if (null != args[i] && SecureEntryDTO.class.isAssignableFrom(args[i].getClass())) {
                    return (SecureEntryDTO) args[i];
                    }
                }
            }

        return null;
        }
}
