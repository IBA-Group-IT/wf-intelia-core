package com.ibagroup.wf.intelia.core.pagefactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.FIELD})
public @interface Wait {

    int value() default 5;

    WaitFunc waitFunc() default WaitFunc.DEFAULT;

    enum WaitFunc {
        CLICKABLE, VISIBLE, DEFAULT;

        WebElement waitUntil(WebDriver driver, int sec, By by) {
            WebDriverWait wait = new WebDriverWait(driver, sec);
            wait.ignoring(WebDriverException.class);

            switch (this) {
                case CLICKABLE:
                    return wait.until(ExpectedConditions.elementToBeClickable(by));
                case VISIBLE:
                    return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
                case DEFAULT:
                    return wait.until(new ExpectedCondition<WebElement>() {
                        @Override
                        public WebElement apply(final WebDriver paramF) {
                            return paramF.findElement(by);
                        }
                    });
            }

            throw new IllegalArgumentException("Unknow WaitFunc: " + this);
        }
    }
}
