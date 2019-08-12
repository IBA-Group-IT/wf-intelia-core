package com.ibagroup.wf.intelia.core.clients;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Clock;
import org.openqa.selenium.support.ui.Duration;
import org.openqa.selenium.support.ui.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.pagefactory.PageFactory;
import com.workfusion.rpa.driver.Driver;
import com.workfusion.rpa.helpers.RPA;
import com.workfusion.rpa.helpers.utils.ApiUtils;

public class RobotDriverWrapper {

    private static final Logger logger = LoggerFactory.getLogger(RobotDriverWrapper.class);

    private Driver driver = null;
    private ConfigurationManager cfg = null;

    public RobotDriverWrapper(ConfigurationManager cmn) {
        this.cfg = cmn;
        this.driver = ApiUtils.driver();

        PageFactory.initElements(getDriver(), this);
    }

    public Driver getDriver() {
        return driver;
    }

    public ConfigurationManager getCfg() {
        return cfg;
    }

    public <T> T waitForElement(Function<WebDriver, T> function, int secondsToPoll) {
        return waitForElement(function, secondsToPoll, false);
    }

    public <T> T waitForElementNot(Function<WebDriver, T> function, int secondsToPoll) {
        return waitForElement(function, secondsToPoll, true);
    }

    public <T> T waitForElement(Function<WebDriver, T> function, int secondsToPoll, boolean notMatch) {
        Clock clock = new SystemClock();
        // no way how to check differently without set implicit wait.. that is
        // not suitable.. as applied globally
        Duration timeout = new Duration(secondsToPoll, TimeUnit.SECONDS);

        long end = clock.laterBy(timeout.in(TimeUnit.MILLISECONDS));

        Exception lastException = null;
        logger.info("Waiting for: {} {} seconds, with not_={}", function, secondsToPoll, notMatch);

        while (true) {
            try {
                T e = function.apply(driver);
                logger.info("Found: {}", e);
                if (!notMatch) {
                    if (e != null && Boolean.class.equals(e.getClass())) {
                        if (Boolean.TRUE.equals(e)) {
                            return e;
                        }
                    } else if (e != null) {
                        return e;
                    }
                }
            } catch (Exception arg8) {
                lastException = arg8;
                logger.info("waitForElement: {}: {} occured", function, arg8.getMessage());
                if (notMatch) {
                    logger.info("return null not_ = {}", notMatch);
                    return null;
                }
            }

            if (!clock.isNowBefore(end)) {
                String toAppend = " waiting for " + function;
                String timeoutMessage = String.format("Timed out after %d seconds%s", Long.valueOf(timeout.in(TimeUnit.SECONDS)), toAppend);
                throw new TimeoutException(timeoutMessage, lastException);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException exep) {
                Thread.currentThread().interrupt();
                throw new WebDriverException(exep);
            }
        }

    }

    public void waitAndSwitchToWindow(String windowSearch, int secondsToPoll) {
        waitAndSwitchToWindow(windowSearch, null, secondsToPoll);
    }

    public void waitAndSwitchToWindow(String windowSearch, By by, int secondsToPoll) {
        Duration timeout = new Duration(secondsToPoll, TimeUnit.SECONDS);
        Clock clock = new SystemClock();
        long end = clock.laterBy(timeout.in(TimeUnit.MILLISECONDS));
        Throwable lastException = null;

        while (true) {
            try {
                driver.switchTo().window(windowSearch);
                if (by != null) {
                    try {
                        driver.findElement(by);
                        break; // correct window found
                    } catch (Exception arg8) {
                        logger.info("no element found, wrong window");
                    }
                } else {
                    break;
                }

            } catch (NoSuchWindowException e) {
                logger.info("Not found: {}", windowSearch);
                logger.info("Current windows: {}", driver.getWindowHandles());
            }

            if (!clock.isNowBefore(end)) {
                String toAppend = " waiting for " + windowSearch + " to be opened";
                String timeoutMessage = String.format("Timed out after %d seconds%s", Long.valueOf(timeout.in(TimeUnit.SECONDS)), toAppend);
                throw new TimeoutException(timeoutMessage, lastException);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException arg7) {
                Thread.currentThread().interrupt();
                throw new WebDriverException(arg7);
            }
        }
        logger.info("Switched to window: {}", windowSearch);
        logger.info("Current windows: {}", driver.getWindowHandles());
    }

    public void waitForWindowToBeClosed(String windowSearch, By by, int secondsToPoll) {
        Duration timeout = new Duration(secondsToPoll, TimeUnit.SECONDS);
        Clock clock = new SystemClock();
        long end = clock.laterBy(timeout.in(TimeUnit.MILLISECONDS));
        Exception lastException = null;

        while (true) {
            try {
                driver.switchTo().window(windowSearch);
                if (by != null) {
                    driver.findElement(by); // if no element found that lookup window closed
                }
            } catch (Exception arg8) {
                lastException = arg8;
                logger.info("waitForElement: {}: {} closed", windowSearch, arg8.getMessage());
                break;
            }

            if (!clock.isNowBefore(end)) {
                String toAppend = " waiting for " + windowSearch + " to be closed";
                String timeoutMessage = String.format("Timed out after %d seconds%s", Long.valueOf(timeout.in(TimeUnit.SECONDS)), toAppend);
                throw new TimeoutException(timeoutMessage, lastException);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException arg7) {
                Thread.currentThread().interrupt();
                throw new WebDriverException(arg7);
            }
        }
    }

    /**
     * Get attribute value from element in json format please also note that this should be
     * configured for driver to be able to get JSON attribute
     * seleniumDriver.manage().set("returnType", "JSON");
     *
     * @param elem - web element on which we want to get attribute from
     * @param attribute - attribute name of element
     *
     * @return parsed json object
     */
    public JsonElement getElemAttributeAsJson(WebElement elem, String attribute) {

        try {
            Method method = driver.manage().getClass().getMethod("set", String.class, String.class);
            method.invoke(driver.manage(), "returnType", "JSON");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Not able to set JSON return value", e);
        }

        logger.info("Trying to get attribute [{}] of element ", elem);
        Gson gson = new Gson();
        String attr = elem.getAttribute(attribute);
        logger.info("Got value: {}", attr);

        if (attr != null && !attr.isEmpty()) {
            JsonElement obj = gson.fromJson(attr, JsonElement.class);
            logger.info("Parsed to Json: {}", obj);
            return obj;
        }

        return null;
    }

    /**
     * Waits until console screen will contains specified text
     *
     * @param text for verification
     * @param seconds to wait
     */
    public void waitForConsoleScreenToBeLoaded(String verificationText, int secondsToPoll) {
        this.waitForConsoleScreenToBeLoaded(verificationText, secondsToPoll, false);
    }

    /**
     * Waits until console screen stops having contain specific text.
     *
     * @param text for verification
     * @param seconds to wait
     */
    public void waitForConsoleScreenToBeLoaded(String verificationText, int secondsToPoll, boolean useNegativeCondition) {
        Duration timeout = new Duration(secondsToPoll, TimeUnit.SECONDS);
        Clock clock = new SystemClock();
        long end = clock.laterBy(timeout.in(TimeUnit.MILLISECONDS));

        String regexp = "";
        if (useNegativeCondition) {
            regexp = "^(?!(.*)" + verificationText + "(.*)).*$";
        } else {
            regexp = "^((.*)" + verificationText + "(.*)).*$";
        }
        Pattern p = Pattern.compile(regexp, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        while (true) {
            String selectedText = Strings.nullToEmpty(RPA.selectAllTextAndCopy());
            Matcher m = p.matcher(selectedText);
            if (m.find()) {
                break;
            }

            if (!clock.isNowBefore(end)) {
                String toAppend = " waiting for " + verificationText + " to be present";
                String timeoutMessage = String.format("Timed out after %d seconds%s", Long.valueOf(timeout.in(TimeUnit.SECONDS)), toAppend);
                throw new TimeoutException(timeoutMessage);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException arg7) {
                Thread.currentThread().interrupt();
                throw new WebDriverException(arg7);
            }
        }
    }

}
