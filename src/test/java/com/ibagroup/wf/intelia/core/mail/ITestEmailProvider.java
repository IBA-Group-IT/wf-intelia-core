package com.ibagroup.wf.intelia.core.mail;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.config.PropertyConfiguration;
import com.ibagroup.wf.intelia.core.mail.SendEmailSmtpNonAuth;

public class ITestEmailProvider {

  private static final Logger logger = LoggerFactory.getLogger(ITestEmailProvider.class);

  @Test
  public void testEmailProvider() {
    ConfigurationManager config =
        new PropertyConfiguration("properties/systems/email/Email.properties");

    SendEmailSmtpNonAuth auth = new SendEmailSmtpNonAuth(config.getConfigItem("smtp_host"));
    boolean result = auth.sendSimpleEmail(config.getConfigItem("test_email_from"),
        config.getConfigItem("test_email_to"), config.getConfigItem("test_email_body"),
        config.getConfigItem("test_email_subject"));
    logger.info("" + result);
    Assert.assertTrue(result);
  }
}
