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

package com.liferay.poshi.runner.script;

import com.liferay.poshi.runner.elements.PoshiElement;
import com.liferay.poshi.runner.elements.PoshiNode;
import com.liferay.poshi.runner.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kenji Heigel
 */
public class PoshiScriptParserException extends Exception {

	public static List<String> getFailingFilePaths() {
		return failingFilePaths;
	}

	public PoshiScriptParserException(String msg) {
		super(msg);
	}

	public PoshiScriptParserException(String msg, PoshiNode poshiNode) {
		super(msg);

		setErrorLineNumber(poshiNode.getPoshiScriptLineNumber());
		setFilePath(poshiNode.getFilePath());
		setPoshiScriptSnippet(poshiNode.getPoshiScript());

		if (poshiNode instanceof PoshiElement) {
			PoshiElement poshiElement = (PoshiElement)poshiNode;

			setStartingLineNumber(poshiElement.getPoshiScriptLineNumber(true));
		}
		else {
			setStartingLineNumber(poshiNode.getPoshiScriptLineNumber());
		}
	}

	public PoshiScriptParserException(
		String msg, String poshiScript, PoshiNode parentPoshiNode) {

		super(msg);

		setFilePath(parentPoshiNode.getFilePath());

		String parentPoshiScript = parentPoshiNode.getPoshiScript();

		parentPoshiScript = parentPoshiScript.replaceFirst("^[\\n\\r]*", "");

		setPoshiScriptSnippet(parentPoshiScript);

		int startingLineNumber = parentPoshiNode.getPoshiScriptLineNumber();

		if (parentPoshiNode instanceof PoshiElement) {
			PoshiElement parentPoshiElement = (PoshiElement)parentPoshiNode;

			startingLineNumber = parentPoshiElement.getPoshiScriptLineNumber(
				true);
		}

		setStartingLineNumber(startingLineNumber);

		int index = parentPoshiScript.indexOf(poshiScript.trim());

		setErrorLineNumber(
			startingLineNumber +
				StringUtil.count(parentPoshiScript, "\n", index));
	}

	public int getErrorLineNumber() {
		return _errorLineNumber;
	}

	public String getErrorSnippet() {
		StringBuilder sb = new StringBuilder();

		String poshiScript = getPoshiScriptSnippet();

		int startingLineNumber = getStartingLineNumber();

		String lineNumberString = String.valueOf(
			startingLineNumber + StringUtil.count(poshiScript, "\n"));

		int pad = lineNumberString.length() + 2;

		for (String line : poshiScript.split("\n")) {
			StringBuilder prefix = new StringBuilder();

			if (startingLineNumber == getErrorLineNumber()) {
				prefix.append(">");
			}
			else {
				prefix.append(" ");
			}

			prefix.append(" ");

			prefix.append(startingLineNumber);

			sb.append(String.format("%" + pad + "s", prefix.toString()));
			sb.append(" |");
			sb.append(line.replace("\t", "    "));
			sb.append("\n");

			startingLineNumber++;
		}

		return sb.toString();
	}

	public String getFilePath() {
		return _filePath;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();

		sb.append(super.getMessage());
		sb.append(" at:\n");
		sb.append(getFilePath());
		sb.append(":");
		sb.append(getErrorLineNumber());
		sb.append("\n");
		sb.append(getErrorSnippet());

		return sb.toString();
	}

	public String getPoshiScriptSnippet() {
		return _poshiScriptSnippet;
	}

	public int getStartingLineNumber() {
		return _startingLineNumber;
	}

	public void setErrorLineNumber(int errorLineNumber) {
		_errorLineNumber = errorLineNumber;
	}

	public void setFilePath(String filePath) {
		_filePath = filePath;

		failingFilePaths.add(filePath);
	}

	public void setPoshiScriptSnippet(String poshiScriptSnippet) {
		_poshiScriptSnippet = poshiScriptSnippet;
	}

	public void setStartingLineNumber(int startingLineNumber) {
		_startingLineNumber = startingLineNumber;
	}

	protected static List<String> failingFilePaths = new ArrayList<>();

	private int _errorLineNumber;
	private String _filePath = "Unknown file";
	private String _poshiScriptSnippet = "";
	private int _startingLineNumber;

}