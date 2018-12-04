/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.poshi.runner.selenium;

import com.liferay.poshi.runner.util.GetterUtil;
import com.liferay.poshi.runner.util.PropsValues;

import java.util.Stack;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author Brian Wing Shun Chan
 */
public class FirefoxWebDriverImpl extends BaseWebDriverImpl {

	public FirefoxWebDriverImpl(String browserURL, WebDriver webDriver) {
		super(browserURL, webDriver);
	}

	@Override
	public void assertJavaScriptErrors(String ignoreJavaScriptError)
		throws Exception {

		if (GetterUtil.getDouble(PropsValues.BROWSER_VERSION) >= 57) {
			return;
		}
	}

	@Override
	public void selectFrame(String locator) {
		if (GetterUtil.getDouble(PropsValues.BROWSER_VERSION) >= 57) {
			if (locator.equals("relative=top")) {
				WebDriver.TargetLocator targetLocator = switchTo();

				targetLocator.defaultContent();

				switchTo().defaultContent();

				Stack<WebElement> frameWebElements = getFrameWebElements();

				frameWebElements = new Stack<>();
			}
			else {
				super.selectFrame(locator);
			}
		}
	}

}