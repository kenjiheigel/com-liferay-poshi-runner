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
public class ForPoshiElement extends PoshiElement {

	@Override
	public PoshiElement clone(Element element) {
		if (isElementType(_ELEMENT_NAME, element)) {
			return new ForPoshiElement(element);
		}

		return null;
	}

	@Override
	public PoshiElement clone(
		PoshiElement parentPoshiElement, String readableSyntax) {

		if (_isElementType(readableSyntax)) {
			return new ForPoshiElement(parentPoshiElement, readableSyntax);
		}

		return null;
	}

	@Override
	public void parseReadableSyntax(String readableSyntax) {
		for (String readableBlock : getReadableBlocks(readableSyntax)) {
			if (readableBlock.startsWith("for (") &&
				!readableBlock.endsWith("}")) {

				String parentheticalContent = getParentheticalContent(
					readableBlock);

				int index = parentheticalContent.indexOf(":");

				String param = parentheticalContent.substring(0, index);

				addAttribute("param", param.trim());

				String list = getQuotedContent(
					parentheticalContent.substring(index + 1));

				addAttribute("list", list.trim());

				continue;
			}

			if (isReadableSyntaxComment(readableBlock)) {
				add(PoshiNodeFactory.newPoshiNode(null, readableBlock));

				continue;
			}

			add(PoshiNodeFactory.newPoshiNode(this, readableBlock));
		}
	}

	@Override
	public String toReadableSyntax() {
		String readableSyntax = super.toReadableSyntax();

		return "\n" + createReadableBlock(readableSyntax);
	}

	protected ForPoshiElement() {
	}

	protected ForPoshiElement(Element element) {
		super(_ELEMENT_NAME, element);
	}

	protected ForPoshiElement(List<Attribute> attributes, List<Node> nodes) {
		super(_ELEMENT_NAME, attributes, nodes);
	}

	protected ForPoshiElement(
		PoshiElement parentPoshiElement, String readableSyntax) {

		super(_ELEMENT_NAME, parentPoshiElement, readableSyntax);
	}

	@Override
	protected String getBlockName() {
		StringBuilder sb = new StringBuilder();

		sb.append("for (");
		sb.append(attributeValue("param"));
		sb.append(" : \"");
		sb.append(attributeValue("list"));
		sb.append("\")");

		return sb.toString();
	}

	protected List<String> getReadableBlocks(String readableSyntax) {
		StringBuilder sb = new StringBuilder();

		List<String> readableBlocks = new ArrayList<>();

		for (String line : readableSyntax.split("\n")) {
			String trimmedLine = line.trim();

			if (readableSyntax.startsWith(line) &&
				trimmedLine.startsWith("for (")) {

				readableBlocks.add(line);

				continue;
			}

			if (!trimmedLine.startsWith("else if (") &&
				!trimmedLine.startsWith("else {")) {

				String readableBlock = sb.toString();

				readableBlock = readableBlock.trim();

				if (isValidReadableBlock(readableBlock)) {
					readableBlocks.add(readableBlock);

					sb.setLength(0);
				}
			}

			sb.append(line);
			sb.append("\n");
		}

		return readableBlocks;
	}

	private boolean _isElementType(String readableSyntax) {
		readableSyntax = readableSyntax.trim();

		if (!isBalancedReadableSyntax(readableSyntax)) {
			return false;
		}

		if (!readableSyntax.startsWith("for (")) {
			return false;
		}

		if (!readableSyntax.endsWith("}")) {
			return false;
		}

		return true;
	}

	private static final String _ELEMENT_NAME = "for";

}