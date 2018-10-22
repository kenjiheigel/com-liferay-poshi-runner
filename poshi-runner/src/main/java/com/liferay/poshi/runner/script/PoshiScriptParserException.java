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

import com.liferay.poshi.runner.elements.PoshiNode;

/**
 * @author Kenji Heigel
 */
public class PoshiScriptParserException extends Exception {

	public PoshiScriptParserException() {
	}

	public PoshiScriptParserException(String msg) {
		super(msg);
	}

	public PoshiScriptParserException(String msg, PoshiNode poshiNode) {
		super(_formatMessage(msg, poshiNode));

		_poshiNode = poshiNode;
	}

	public PoshiScriptParserException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public PoshiScriptParserException(Throwable cause) {
		super(cause);
	}

	public PoshiNode getPoshiNode() {
		return _poshiNode;
	}

	private static String _formatMessage(String msg, PoshiNode poshiNode) {
		StringBuilder sb = new StringBuilder();

		sb.append(msg);

		sb.append(" at:\n\t");
		sb.append(poshiNode.getFilePath());
		sb.append(":");
		sb.append(poshiNode.getPoshiScriptLineNumber());
		sb.append("\n\t[");

		String poshiScript = poshiNode.getPoshiScript();

		sb.append(poshiScript.trim());

		sb.append("]");

		return sb.toString();
	}

	private PoshiNode _poshiNode;

}