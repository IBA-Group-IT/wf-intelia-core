package com.ibagroup.wf.intelia.core;

import java.util.Properties;

import org.junit.Before;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

@PowerMockIgnore({ "org.apache.commons.*", "javax.net.ssl.*", "javax.crypto.*", "org.apache.log4j.*" })
public class BaseIntegrationTest {

	@Before
	public void initSystemProperties() {

		Properties properties = System.getProperties();
		properties.setProperty("javax.net.ssl.keyStore", "");
		properties.setProperty("javax.net.ssl.keyStorePassword", "");
		properties.setProperty("javax.net.ssl.trustStore", "");
		properties.setProperty("javax.net.ssl.trustStorePassword", "");
		System.setProperties(properties);
	}

}
