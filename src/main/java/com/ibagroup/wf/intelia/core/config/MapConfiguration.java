package com.ibagroup.wf.intelia.core.config;

import java.util.Map;

public class MapConfiguration implements ConfigurationManager {

    private final Map<String, String> props;

    public MapConfiguration(Map<String, String> props) {
        super();
        this.props = props;
    }

    @Override
    public String getConfigItem(String keyParam) {
        return props.get(keyParam);
    }

    @Override
    public boolean isLocal() {
        return true;
    }

}
