package com.ibagroup.wf.intelia.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.FileUtils;

public class PropertyConfiguration implements ConfigurationManager {
    private Properties properties = new Properties();

    public PropertyConfiguration(String fileName) {
        this(initStream(fileName));
    }

    private static FileInputStream initStream(String fileName) {
        try {
            return FileUtils.openInputStream(new File(PropertyConfiguration.class.getClassLoader().getResource("").getPath() + fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PropertyConfiguration(InputStream inputStream) {
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public String getConfigItem(String keyParam) {
        return properties.getProperty(keyParam);
    }
};
