package com.ibagroup.wf.intelia.core.config;

public interface ConfigurationManager {

	String getConfigItem(String keyParam);

	default String getRequiredConfigItem(String keyParam) {
		String result = getConfigItem(keyParam);
		if (result == null) {
			throw new MissingConfigItemException(keyParam);
		}
		return result;
	}

	default <T> T getConfigItem(String keyParam, T defValue, Formatter<T> formatter) {
		String result = getConfigItem(keyParam);
		return result != null ? formatter.format(result) : defValue;
	}

	default String getConfigItem(String keyParam, String defValue) {
		String result = getConfigItem(keyParam);
		return result != null ? result : defValue;
	}

	@FunctionalInterface
	interface Formatter<T> {
		T format(String input);

		Formatter<Integer> INT = (input) -> {
			return Integer.parseInt(input);
		};

		Formatter<Long> LONG = (input) -> {
			return Long.parseLong(input);
		};

		Formatter<Boolean> BOOLEAN = (input) -> {
			return Boolean.parseBoolean(input);
		};
	}

	boolean isLocal();

	public class MissingConfigItemException extends RuntimeException {

		private static final long serialVersionUID = 4496987636357758243L;

		public MissingConfigItemException(String itemName) {
			super(String.format("Required config item '%s' is not specified.", itemName));
		}
	}
}