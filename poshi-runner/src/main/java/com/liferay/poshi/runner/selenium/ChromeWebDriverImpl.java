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

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

/**
 * @author Brian Wing Shun Chan
 */
public class ChromeWebDriverImpl extends BaseWebDriverImpl {

	public ChromeWebDriverImpl(String browserURL, WebDriver webDriver) {
		super(browserURL, webDriver);
	}

	@Override
	public void click(String locator) {
		try {
			super.click(locator);
		}
		catch (WebDriverException wde) {
			String message = wde.getMessage();

			Matcher matcher = _elementNotClickableErrorPattern.matcher(message);

			if (matcher.find()) {
				javaScriptClick(locator);

				return;
			}

			throw new ElementNotInteractableException(message, wde);
		}
	}

	@Override
	public void clickAt(
		String locator, String coordString, boolean scrollIntoView) {

		try {
			super.clickAt(locator, coordString, scrollIntoView);
		}
		catch (WebDriverException wde) {
			String message = wde.getMessage();

			Matcher matcher = _elementNotClickableErrorPattern.matcher(message);

			if (matcher.find()) {
				javaScriptClick(locator);

				return;
			}

			throw new ElementNotInteractableException(message, wde);
		}
	}

	@Override
	public String getSelectedLabel(String selectLocator, String timeout) {
		String text = super.getSelectedLabel(selectLocator, timeout);

		return text.trim();
	}

	@Override
	public String[] getSelectedLabels(String selectLocator) {
		String[] selectedLabels = super.getSelectedLabels(selectLocator);

		for (int i = 0; i < selectedLabels.length; i++) {
			selectedLabels[i] = selectedLabels[i].trim();
		}

		return selectedLabels;
	}

	@Override
	public String getText(String locator, String timeout) throws Exception {
		String text = super.getText(locator, timeout);

		return text.trim();
	}

	@Override
	public void typeKeys(String locator, String value) {
		try {
			super.typeKeys(locator, value);
		}
		catch (WebDriverException wde) {
			String message = wde.getMessage();

			Matcher matcher = _cannotFocusElementErrorPattern.matcher(message);

			if (matcher.find()) {
				WebElement webElement = getWebElement(locator);

				JavascriptExecutor javascriptExecutor =
					createJavascriptExecutor(webElement);

				javascriptExecutor.executeScript(
					"arguments[0].focus();", webElement);

				super.typeKeys(locator, value);

				return;
			}

			throw new ElementNotInteractableException(message, wde);
		}
	}

	protected WebElement getWebElement(String locator, String timeout) {
		try {
			return super.getWebElement(locator, timeout);
		}
		catch (RuntimeException re) {
			Stack<WebElement> frameWebElements = getFrameWebElements();

			if (!frameWebElements.isEmpty()) {
				if (frameWebElements.peek() instanceof RetryWebElementImpl) {
					RetryWebElementImpl frameWebElement =
						(RetryWebElementImpl)frameWebElements.peek();

					String frameWebElementLocator =
						frameWebElement.getLocator();

					frameWebElements.pop();

					frameWebElements.push(
						getWebElement(frameWebElementLocator));

					WebDriver.TargetLocator targetLocator = switchTo();

					targetLocator.frame(frameWebElements.peek());
				}
			}

			throw re;
		}
	}

	private static final Pattern _cannotFocusElementErrorPattern =
		Pattern.compile("cannot focus element");
	private static final Pattern _elementNotClickableErrorPattern =
		Pattern.compile(
			"Element[\\s\\S]*is not clickable at point[\\s\\S]*" +
				"Other element would receive the click");

}