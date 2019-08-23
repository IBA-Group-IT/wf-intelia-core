package com.ibagroup.wf.intelia.core;

import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.codejargon.feather.Provides;
import com.ibagroup.wf.intelia.core.exceptions.DefaultExceptionHandler;
import com.ibagroup.wf.intelia.core.exceptions.ExceptionHandler;

public class DefaultExceptionHandlerModule implements Module {
    @Provides
    @Singleton
    public ExceptionHandler exceptionHandler(DefaultExceptionHandler defaultExceptionHandler) {
        return defaultExceptionHandler;
    }

    @Provides
    @Singleton
    @Named("ExceptionHandler")
    public Optional<ExceptionHandler> optionalExceptionHandler(DefaultExceptionHandler defaultExceptionHandler) {
        return Optional.of(defaultExceptionHandler);
    }

}
