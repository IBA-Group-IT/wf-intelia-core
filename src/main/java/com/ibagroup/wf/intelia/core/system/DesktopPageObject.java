package com.ibagroup.wf.intelia.core.system;

public class DesktopPageObject extends PageObject {

	public boolean titleContains(String valueToCheck) {
		return getDriver().getTitle().trim().contains(valueToCheck);
	}
}
