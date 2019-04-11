package com.ibagroup.wf.intelia.core.pagefactory;

import java.lang.reflect.Field;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.DefaultElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;

public class ElementLocatorFactoryExt implements ElementLocatorFactory {

  private final WebDriver driver;

  public ElementLocatorFactoryExt(WebDriver driver) {
    this.driver = driver;
  }

  @Override
  public ElementLocator createLocator(Field field) {
    return new DefaultElementLocator(this.driver, new AnnotationsEx(field, driver));
  }

}
