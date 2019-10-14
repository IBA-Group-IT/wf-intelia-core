package com.ibagroup.wf.intelia.core.system;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibagroup.wf.intelia.core.clients.RobotDriverWrapper;
import com.ibagroup.wf.intelia.core.pagefactory.Wait;
import com.workfusion.rpa.helpers.RPA;

public abstract class BasePage extends RobotDriverWrapper {

	private static final Logger logger = LoggerFactory.getLogger(BasePage.class);

	private static final String SAVE_AS_WINDOW_CSS = "[CLASS:#32770; TITLE:Save As]";
	private static final String OPEN_FILE_WINDOW_CSS = "[CLASS:#32770; TITLE:Open]";

	@Wait(waitFunc = Wait.WaitFunc.CLICKABLE, value = DEFAULT_WAIT_TIMEOUT_SECONDS)
	@FindBy(css = "[CLASS:Edit]")
	private WebElement filePathField;

	@Wait(waitFunc = Wait.WaitFunc.CLICKABLE, value = DEFAULT_WAIT_TIMEOUT_SECONDS)
	@FindBy(css = "[CLASS:Button; NAME:Save]")
	private WebElement saveBtn;

	@Wait(waitFunc = Wait.WaitFunc.CLICKABLE, value = DEFAULT_WAIT_TIMEOUT_SECONDS)
	@FindBy(css = "[CLASS:Button; NAME:Open]")
	private WebElement openBtn;

	public void chooseFile(String filePath) {
		RPA.switchToExistingWindow(OPEN_FILE_WINDOW_CSS, getWaitLoadingTimeoutInSeconds() * 1000L);

		RPA.setClipboardText(filePath);
		filePathField.click();
		RPA.pressCtrlA();
		RPA.pressCtrlV();

		openBtn.click();
	}

	public void saveAsFile(String filePath) {
		RPA.switchToExistingWindow(SAVE_AS_WINDOW_CSS, getWaitLoadingTimeoutInSeconds() * 1000L);
		String saveAsWinHandle = RPA.driver().getWindowHandle();

		RPA.setClipboardText(filePath);
		filePathField.click();
		RPA.pressCtrlA();
		RPA.pressCtrlV();

		saveBtn.click();

		wait.until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return !RPA.driver().getWindowHandles().contains(saveAsWinHandle);
			}
		});
	}

	public boolean isElementExist(By elementLocator) {
		return isElementExist(elementLocator, null);
	}

	public boolean isElementExist(By elementLocator, WebElement relatedTo) {
		try {
			getDriver().manage().timeouts().implicitlyWait(200, TimeUnit.MICROSECONDS);
			return (relatedTo != null ? relatedTo : getDriver()).findElements(elementLocator).size() > 0;

		} catch (UnhandledAlertException e) {
			throw e;
		} catch (Exception e) {
			logger.error(String.format("Cannot find element using '%s' because an error occured.", elementLocator), e);
			return false;
		} finally {
			getDriver().manage().timeouts().implicitlyWait(getImplicitlyWaitTimeoutInSeconds(), TimeUnit.SECONDS);
		}
	}

	public InputStream takeScreenshot() {
		try {
			BufferedImage image = RPA.screenshotAsImage();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				ImageIO.write(image, "PNG", baos);
				baos.flush();
				byte[] imageInByte = baos.toByteArray();
				return new ByteArrayInputStream(imageInByte);
			} finally {
				baos.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("Taking of screenshot has failed.", e);
		}
	}

}