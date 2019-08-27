package com.ibagroup.wf.intelia.core;

import static com.ibagroup.wf.intelia.core.CoreModule.BOT_CONFIG_PARAMS_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.mis.RobotLogger.BP_ACTIONS_DS_NAME_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.mis.RobotLogger.BP_DETAILS_DS_NAME_PARAM_NAME;
import java.util.Map;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.codejargon.feather.Provides;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.mis.IRobotLogger;
import com.ibagroup.wf.intelia.core.mis.RobotLogger;
import com.workfusion.intake.core.Module;

public class RobotLoggerModule implements Module {

    @Provides
    @Named(BP_ACTIONS_DS_NAME_PARAM_NAME)
    public String bpActionsDSName(ConfigurationManager cfg, @Named(BOT_CONFIG_PARAMS_PARAM_NAME) Map<String, String> params) {
        return params.containsKey(BP_ACTIONS_DS_NAME_PARAM_NAME) ? params.get(BP_ACTIONS_DS_NAME_PARAM_NAME) : cfg.getConfigItem(BP_ACTIONS_DS_NAME_PARAM_NAME);
    }

    @Provides
    @Named(BP_DETAILS_DS_NAME_PARAM_NAME)
    public String bpDetailsDSName(ConfigurationManager cfg, @Named(BOT_CONFIG_PARAMS_PARAM_NAME) Map<String, String> params) {
        return params.containsKey(BP_DETAILS_DS_NAME_PARAM_NAME) ? params.get(BP_DETAILS_DS_NAME_PARAM_NAME) : cfg.getConfigItem(BP_DETAILS_DS_NAME_PARAM_NAME);
    }

    @Provides
    @Singleton
    public IRobotLogger robotLogger(RobotLogger robotLogger) {
        return robotLogger;
    }

    @Provides
    @Singleton
    @Named("IRobotLogger")
    public Optional<IRobotLogger> optionalRobotLogger(RobotLogger robotLogger) {
        return Optional.of(robotLogger);
    }
}
