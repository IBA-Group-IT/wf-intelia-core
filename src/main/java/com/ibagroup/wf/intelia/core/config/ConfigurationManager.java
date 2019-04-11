package com.ibagroup.wf.intelia.core.config;

public interface ConfigurationManager {

    String getConfigItem(String keyParam);

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

}