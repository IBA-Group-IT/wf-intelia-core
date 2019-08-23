package com.ibagroup.wf.intelia.core.system;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workfusion.rpa.helpers.RPA;

public class WebPageObject extends PageObject {

	private static final Logger logger = LoggerFactory.getLogger(WebPageObject.class);

	public boolean isElementDisplayed(By elementLocator) {
		return isElementDisplayed(elementLocator, null);
	}

	public boolean isElementDisplayed(By elementLocator, WebElement relatedTo) {
		try {
			getDriver().manage().timeouts().implicitlyWait(200, TimeUnit.MICROSECONDS);
			List<WebElement> elements = (relatedTo != null ? relatedTo : getDriver()).findElements(elementLocator);
			return elements.stream().anyMatch(e -> e.isDisplayed());
		} catch (UnhandledAlertException e) {
			throw e;
		} catch (Exception e) {
			logger.error(String.format("Cannot find element using '%s' because an error occured.", elementLocator), e);
			return false;
		} finally {
			getDriver().manage().timeouts().implicitlyWait(DEFAULT_IMPLICITLY_WAIT_TIMEOUT, TimeUnit.SECONDS);
		}
	}

	public void scrollToElement(WebElement element) {
		StringBuilder script = new StringBuilder();
		script.append("var el = arguments[0]; \n");
		script.append("el.scrollIntoView({block: 'center', inline: 'center'}); \n");
		script.append("var hScrollOffset = el.getBoundingClientRect().left; \n");
		script.append("while(el && el.scrollWidth == el.clientWidth){ \n");
		script.append("		el = el.parentElement;\n");
		script.append("} \n");
		script.append("if(el){ \n");
		script.append("		el.scrollLeft = hScrollOffset; \n");
		script.append("}");
		getDriver().executeScript(script.toString(), element);
	}

	public void zoom(double factor) {
		getDriver().executeScript("document.body.style.zoom = arguments[0];", factor);
	}

	public InputStream takeScreenshot(Double zoomFactor) {
		try {
			if (zoomFactor != null) {
				zoom(zoomFactor);
			}
			BufferedImage image = RPA.screenshotAsImage();
			if (zoomFactor != null) {
				zoom(1);
			}
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
