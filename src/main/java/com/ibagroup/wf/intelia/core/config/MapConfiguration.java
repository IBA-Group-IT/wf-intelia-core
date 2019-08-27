package com.ibagroup.wf.intelia.core.config;

import static com.ibagroup.wf.intelia.core.CoreModule.BOT_CONFIG_PARAMS_PARAM_NAME;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

public class MapConfiguration implements ConfigurationManager {

	private final Map<String, String> props;

	@Inject
	public MapConfiguration(@Named(BOT_CONFIG_PARAMS_PARAM_NAME) Map<String, String> props) {
		super();
		this.props = props;
	}

	@Override
	public String getConfigItem(String keyParam) {
		Object value = props.get(keyParam);
		return value != null ? value.toString() : null;
	}

	@Override
	public boolean isLocal() {
		return true;
	}

}
