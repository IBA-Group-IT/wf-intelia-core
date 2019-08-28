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
		//In case of defining parameters in Webharvest XML via Groovy (where not checking of types) 'props.get' can return instance of class different from String.
		//That's why it's necessary to convert result of calling get method to String.
		Object value = props.get(keyParam);
		return value != null ? value.toString() : null;
	}

	@Override
	public boolean isLocal() {
		return true;
	}

}
