package com.ibagroup.wf.intelia.core.robots;

import java.util.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibagroup.wf.intelia.core.metadata.types.LoggingMetadata;
import com.ibagroup.wf.intelia.core.metadata.types.ScreenshotMetadata;
import com.workfusion.rpa.driver.Driver;
import com.workfusion.rpa.helpers.utils.ApiUtils;

public abstract class UiRobotCapabilities extends RobotCapabilities implements RobotProtocol {

    private static final Logger logger = LoggerFactory.getLogger(UiRobotCapabilities.class);

//    private RemoteDriverWrapper drvWrapper = null;
    private Driver driver = null;

    @Override
    public boolean storeCurrentMetadata() {
        logger.info("Trying to store screenshot");
        try {

            logger.info("Using: " + getDriver().getDriverInfo().getType());

            if (getDriver().getSessionId() != null) {
                String driverType = getDriver().getDriverInfo().getType();
                if ("universal".equals(driverType) || "desktop".equals(driverType) || "autoit".equals(driverType)) {
                    logger.info("It is autoit driver, due to RPA-184 (function not implemented on autoit), we would need to execute remote script");

                    Object res = getDriver().executeScript(
                            "java.awt.Robot robot = new java.awt.Robot();\n"
                                + "java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();\n"
                                + "Integer iWidth = Math.ceil(screenSize.width);\n"
                                + "Integer iHeight = Math.ceil(screenSize.height);\n"
                                + "java.awt.Rectangle captureRect = new java.awt.Rectangle(0, 0, iWidth, iHeight);\n"
                                + "java.awt.image.BufferedImage screenFullImage = robot.createScreenCapture(captureRect);\n"
                                + "java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();"
                                + "javax.imageio.ImageIO.write(screenFullImage, \"PNG\", baos);"
                                + "return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());",
                            "GROOVY");
                    logger.info("Screenshot from node stored");
                    byte[] decode = Base64.getDecoder().decode(res.toString());
                    getMetadataManager().addMetadata(new ScreenshotMetadata("screenshot", decode));
                } else {

                    byte[] screenshotAs = getDriver().getScreenshotAs(OutputType.BYTES);
                    getMetadataManager().addMetadata(new ScreenshotMetadata("screenshot", screenshotAs));
                    getMetadataManager().addMetadata(new LoggingMetadata("pagesource", getDriver().getPageSource()));
                    logger.info("Screenshot stored");
                }

                return true;
            } else {
                logger.info(getDriver().toString() + " is closed. Can't grab a screenshot");
            }

        } catch (Exception ex) {
            logger.error(ExceptionUtils.getMessage(ex) + ": " + ExceptionUtils.getStackTrace(ex));
        }
        return false;
    }

    public Driver getDriver() {
        if (null == driver) {
            driver = ApiUtils.driver();
        }
        return driver;
    }

//    public RemoteDriverWrapper getDrvWrapper() {
//        if (null == drvWrapper) {
//            drvWrapper = ApiUtils.driverWrapper();
//        }
//        return drvWrapper;
//    }
}
