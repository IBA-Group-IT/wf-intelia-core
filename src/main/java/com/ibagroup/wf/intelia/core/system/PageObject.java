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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibagroup.wf.intelia.core.clients.RobotDriverWrapper;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.pagefactory.Wait;
import com.workfusion.rpa.helpers.RPA;

import groovy.lang.Binding;

public abstract class PageObject extends RobotDriverWrapper {

	protected static final int DEFAULT_WAIT_TIMEOUT_SECONDS = 30;
	protected static final int DEFAULT_WAIT_LOADING_TIMEOUT_SECONDS = 30 * 60;
	protected static final int DEFAULT_WAIT_SWITCH_WINDOW_SECONDS = 5;
	protected static final int DEFAULT_IMPLICITLY_WAIT_TIMEOUT = 5;

	private static final Logger logger = LoggerFactory.getLogger(PageObject.class);

	private static final String SAVE_AS_WINDOW_CSS = "[CLASS:#32770; TITLE:Save As]";
	private static final String OPEN_FILE_WINDOW_CSS = "[CLASS:#32770; TITLE:Open]";

	protected final WebDriverWait wait;
	protected final WebDriverWait waitLoading;

	@Wait(waitFunc = Wait.WaitFunc.CLICKABLE, value = DEFAULT_WAIT_TIMEOUT_SECONDS)
	@FindBy(css = "[CLASS:Edit;INSTANCE:1]")
	private WebElement filePathField;

	@Wait(waitFunc = Wait.WaitFunc.CLICKABLE, value = DEFAULT_WAIT_TIMEOUT_SECONDS)
	@FindBy(css = "[CLASS:Button;INSTANCE:2]")
	private WebElement saveBtn;

	@Wait(waitFunc = Wait.WaitFunc.CLICKABLE, value = DEFAULT_WAIT_TIMEOUT_SECONDS)
	@FindBy(css = "[CLASS:Button;INSTANCE:1]")
	private WebElement openBtn;

	private Binding binding;

	public PageObject(Binding binding, ConfigurationManager cmn) {
		super(cmn);
		this.binding = binding;
		wait = new WebDriverWait(getDriver(), DEFAULT_WAIT_TIMEOUT_SECONDS);
		waitLoading = new WebDriverWait(getDriver(), DEFAULT_WAIT_LOADING_TIMEOUT_SECONDS);
	}

	public Binding getBinding() {
		return binding;
	}

	public void chooseFile(String filePath) {
		waitAndSwitchToWindow(OPEN_FILE_WINDOW_CSS, DEFAULT_WAIT_SWITCH_WINDOW_SECONDS);

		RPA.setClipboardText(filePath);
		filePathField.click();
		RPA.pressCtrlA();
		RPA.pressCtrlV();

		openBtn.click();
	}

	public void saveAsFile(String filePath) {
		waitAndSwitchToWindow(SAVE_AS_WINDOW_CSS, DEFAULT_WAIT_SWITCH_WINDOW_SECONDS);

		RPA.setClipboardText(filePath);
		filePathField.click();
		RPA.pressCtrlA();
		RPA.pressCtrlV();

		saveBtn.click();
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
			getDriver().manage().timeouts().implicitlyWait(DEFAULT_IMPLICITLY_WAIT_TIMEOUT, TimeUnit.SECONDS);
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