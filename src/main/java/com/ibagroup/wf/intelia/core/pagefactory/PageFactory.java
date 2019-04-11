package com.ibagroup.wf.intelia.core.pagefactory;

import org.openqa.selenium.WebDriver;

public class PageFactory {

  public static void initElements(WebDriver driver, Object page) {
    WebDriver driverRef = driver;
    org.openqa.selenium.support.PageFactory.initElements(new ElementLocatorFactoryExt(driverRef),
        page);
  }

}
