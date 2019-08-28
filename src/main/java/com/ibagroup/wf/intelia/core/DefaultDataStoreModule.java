package com.ibagroup.wf.intelia.core;

import static com.ibagroup.wf.intelia.core.CoreModule.BOT_CONFIG_PARAMS_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.datastore.BaseDS.DEFAULT_DATASTORE_PARAM_NAME;
import java.util.Map;
import javax.inject.Named;
import org.codejargon.feather.Provides;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.workfusion.intake.core.Module;

public class DefaultDataStoreModule implements Module {
    @Provides
    @Named(DEFAULT_DATASTORE_PARAM_NAME)
    public String defaultDatastoreName(ConfigurationManager cfg, @Named(BOT_CONFIG_PARAMS_PARAM_NAME) Map<String, String> params) {
        return params.containsKey(DEFAULT_DATASTORE_PARAM_NAME) ? params.get(DEFAULT_DATASTORE_PARAM_NAME) : cfg.getConfigItem(DEFAULT_DATASTORE_PARAM_NAME);
    }

}
