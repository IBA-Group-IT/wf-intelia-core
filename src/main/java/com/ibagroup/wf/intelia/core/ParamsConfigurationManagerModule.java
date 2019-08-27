package com.ibagroup.wf.intelia.core;

import javax.inject.Singleton;
import org.codejargon.feather.Provides;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.config.MapConfiguration;
import com.workfusion.intake.core.Module;

public class ParamsConfigurationManagerModule implements Module {

    @Provides
    @Singleton
    public ConfigurationManager configurationManager(MapConfiguration mapConfigManager) {
        return mapConfigManager;
    }
}
