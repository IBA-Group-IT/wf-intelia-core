package com.ibagroup.wf.intelia.core.pagefactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.Annotations;

import com.ibagroup.wf.intelia.core.pagefactory.Wait.WaitFunc;
import com.workfusion.rpa.helpers.RPA;

public class AnnotationsEx extends Annotations {

	private final WebDriver driver;

	public AnnotationsEx(Field field, WebDriver driver) {
		super(field);
		this.driver = driver;
	}

	private boolean isWaitWrap() {
		return this.getField().getAnnotation(Wait.class) != null;
	}

	private boolean isFindByWrap() {
		return this.getField().getAnnotation(FindBy.class) != null;
	}

	@Override
	public By buildBy() {
		By buildBy = super.buildBy();
		if (isFindByWrap()) {
			buildBy = wrapFindBy(buildBy);
		}
		if (isWaitWrap()) {
			buildBy = wrapWait(buildBy);
		}
		return buildBy;
	}

	private By wrapFindBy(final By by) {
		return new By() {
			@Override
			public List<WebElement> findElements(SearchContext paramSearchContext) {
				return by.findElements(paramSearchContext).stream()
						.map(item -> {
							return RPA.$(item);
						}).collect(Collectors.toList());
			}
			@Override
			public WebElement findElement(SearchContext context) {
				return RPA.$(by.findElement(context));
			}
		};
	}

	private By wrapWait(final By by) {
		Wait waitAnno = this.getField().getAnnotation(Wait.class);
		int sec = waitAnno.value();
		WaitFunc func = waitAnno.waitFunc();
		return new By() {

			@Override
			public List<WebElement> findElements(SearchContext paramSearchContext) {
				return by.findElements(paramSearchContext);
			}

			@Override
			public WebElement findElement(SearchContext context) {
				return func.waitUntil(driver, sec, by);
			}
		};
	}
}
