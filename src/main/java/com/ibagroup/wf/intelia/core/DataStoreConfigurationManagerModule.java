package com.ibagroup.wf.intelia.core;

import static com.ibagroup.wf.intelia.core.CoreModule.BOT_CONFIG_PARAMS_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.config.DataStoreConfiguration.RPA_CONFIG_DS;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;
import org.codejargon.feather.Provides;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.config.DataStoreConfiguration;

public class DataStoreConfigurationManagerModule implements Module {
    @Provides
    @Named(RPA_CONFIG_DS)
    public String configDSName(@Named(BOT_CONFIG_PARAMS_PARAM_NAME) Map<String, String> params) {
        return params.get(RPA_CONFIG_DS);
    }

    @Provides
    @Singleton
    public ConfigurationManager configurationManager(DataStoreConfiguration dataStoreConfigManager) {
        return dataStoreConfigManager;
    }
}
