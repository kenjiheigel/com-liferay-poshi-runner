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

package com.liferay.poshi.runner.elements;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * @author Kenji Heigel
 */
public class TaskPoshiElement extends PoshiElement {

	@Override
	public PoshiElement clone(Element element) {
		if (isElementType(_ELEMENT_NAME, element)) {
			return new TaskPoshiElement(element);
		}

		return null;
	}

	@Override
	public PoshiElement clone(
		PoshiElement parentPoshiElement, String poshiScript) {

		if (_isElementType(poshiScript)) {
			return new TaskPoshiElement(parentPoshiElement, poshiScript);
		}

		return null;
	}

	@Override
	public void parseReadableSyntax(String poshiScript) {
		for (String readableBlock : getReadableBlocks(poshiScript)) {
			if (readableBlock.startsWith("task (")) {
				String parentheticalContent = getParentheticalContent(
					readableBlock);

				String summary = getQuotedContent(parentheticalContent);

				addAttribute("summary", summary);

				continue;
			}

			add(PoshiNodeFactory.newPoshiNode(this, readableBlock));
		}
	}

	@Override
	public String toReadableSyntax() {
		StringBuilder sb = new StringBuilder();

		sb.append("\n");

		StringBuilder content = new StringBuilder();

		for (PoshiElement poshiElement : toPoshiElements(elements())) {
			content.append(poshiElement.toReadableSyntax());
		}

		String readableBlock = createReadableBlock(content.toString());

		sb.append(readableBlock);

		return sb.toString();
	}

	protected TaskPoshiElement() {
	}

	protected TaskPoshiElement(Element element) {
		super(_ELEMENT_NAME, element);
	}

	protected TaskPoshiElement(List<Attribute> attributes, List<Node> nodes) {
		super(_ELEMENT_NAME, attributes, nodes);
	}

	protected TaskPoshiElement(
		PoshiElement parentPoshiElement, String poshiScript) {

		super(_ELEMENT_NAME, parentPoshiElement, poshiScript);
	}

	@Override
	protected String getBlockName() {
		StringBuilder sb = new StringBuilder();

		sb.append("task (\"");
		sb.append(attributeValue("summary"));
		sb.append("\")");

		return sb.toString();
	}

	protected List<String> getReadableBlocks(String poshiScript) {
		StringBuilder sb = new StringBuilder();

		List<String> readableBlocks = new ArrayList<>();

		for (String line : poshiScript.split("\n")) {
			String trimmedLine = line.trim();

			String readableBlock = sb.toString();

			readableBlock = readableBlock.trim();

			if (trimmedLine.startsWith(getReadableName() + " (") &&
				trimmedLine.endsWith("{") && (readableBlock.length() == 0)) {

				readableBlocks.add(line);

				continue;
			}

			if (trimmedLine.endsWith("{") && readableBlocks.isEmpty()) {
				continue;
			}

			if (isValidReadableBlock(readableBlock)) {
				readableBlocks.add(readableBlock);

				sb.setLength(0);
			}

			sb.append(line);
			sb.append("\n");
		}

		return readableBlocks;
	}

	protected String getReadableName() {
		return getName();
	}

	private boolean _isElementType(String poshiScript) {
		poshiScript = poshiScript.trim();

		if (!isBalancedReadableSyntax(poshiScript)) {
			return false;
		}

		if (!poshiScript.startsWith("task (")) {
			return false;
		}

		if (!poshiScript.endsWith("}")) {
			return false;
		}

		return true;
	}

	private static final String _ELEMENT_NAME = "task";

}