/*
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

package com.liferay.poshi.runner.prose;

import com.liferay.poshi.runner.PoshiRunnerContext;
import com.liferay.poshi.runner.elements.CommandPoshiElement;
import com.liferay.poshi.runner.elements.DefinitionPoshiElement;
import com.liferay.poshi.runner.elements.ExecutePoshiElement;
import com.liferay.poshi.runner.elements.VarPoshiElement;
import com.liferay.poshi.runner.util.Dom4JUtil;
import com.liferay.poshi.runner.util.RegexUtil;
import com.liferay.poshi.runner.util.StringUtil;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.DefaultAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kenji Heigel
 */
public class PoshiProse {

	public PoshiProse(String proseContent) {
		_proseContent = proseContent;
	}

	public Element getPoshiXML() {
		try {

			System.out.println(_proseContent);

			List<String> proseStatements = StringUtil.splitByKeys(
			_proseContent, PROSE_KEYWORDS);

			System.out.println(proseStatements.toString());

			String[] includes = {"**/*macro"};

			PoshiRunnerContext.readFiles(
			includes,
			"src/test/resources/com/liferay/poshi/runner/dependencies/prose/");

			List<Node> executeElements = new ArrayList<>();
			List<Attribute> commandAttributes = new ArrayList<>();

			for (String proseStatement : proseStatements) {
				proseStatement = proseStatement.trim();

				if (proseStatement.startsWith("Scenario:")) {
					proseStatement = StringUtil.replaceFirst(
						proseStatement, "Scenario:", "");

					proseStatement = proseStatement.trim();

					Attribute commandAttribute = new DefaultAttribute("name", proseStatement);

					commandAttributes.add(commandAttribute);

					continue;
				}

				for (String keyword : PROSE_KEYWORDS) {
					if (proseStatement.startsWith(keyword)) {
						proseStatement = StringUtil.replaceFirst(
							proseStatement, keyword, "");

						break;
					}
				}

				proseStatement = proseStatement.trim();

				String matchingString = proseStatement.replaceAll(
					"\".*?\"", "\"\"");

				System.out.println(matchingString);

				PoshiProseMacroMatchingString ppmm =
					PoshiProseMacroMatchingString.getPoshiProseMacroMatchingString(matchingString);

				System.out.println(ppmm.getMacroNamespacedClassCommandName());

				String macroNamespacedClassCommandName =
					ppmm.getMacroNamespacedClassCommandName();

				String namespace =
					macroNamespacedClassCommandName.split("\\.")[0];
				String classCommandName =
					macroNamespacedClassCommandName.split("\\.")[1];

				Element macroElement =
					PoshiRunnerContext.getMacroCommandElement(
						classCommandName, namespace);

				System.out.println(Dom4JUtil.format(macroElement));

				String match = macroElement.attributeValue("matching-string");

				System.out.println(match);
				System.out.println(proseStatement);

				List<String> varNames = _getMatches(variableValue, match);
				List<String> varValues = _getMatches(quotedValue, proseStatement);

				List<Node> varElements = new ArrayList<>();

				for (int i = 0; i < varNames.size(); i++) {
					System.out.println(varValues.get(i));

					Attribute varNameAttribute = new DefaultAttribute("name", varNames.get(i));
					Attribute varValueAttribute = new DefaultAttribute("value", varValues.get(i));

					List<Attribute> varAttributes = new ArrayList<>();

					varAttributes.add(varNameAttribute);
					varAttributes.add(varValueAttribute);

					VarPoshiElement varPoshiElement =
						new VarPoshiElement(varAttributes, null);

					System.out.println(Dom4JUtil.format(varPoshiElement));

					varElements.add(varPoshiElement);
				}

				Attribute macroAttribute = new DefaultAttribute("macro", classCommandName);

				List<Attribute> macroAttributes = new ArrayList<>();

				macroAttributes.add(macroAttribute);

				ExecutePoshiElement executePoshiElement = new ExecutePoshiElement(macroAttributes, varElements);

				executeElements.add(executePoshiElement);
			}

			CommandPoshiElement commandPoshiElement = new CommandPoshiElement(commandAttributes, executeElements);

			DefinitionPoshiElement definitionPoshiElement = new DefinitionPoshiElement(null, new ArrayList<>(Arrays.asList(commandPoshiElement)));

//			PoshiRunnerContext.clear();

			System.out.println(Dom4JUtil.format(definitionPoshiElement));

			return definitionPoshiElement;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	protected static final String[] PROSE_KEYWORDS = {
		"And", "Given", "Then", "When"
	};

	private List<String> _getMatches(Pattern pattern, String s) {
		List<String> matches = new ArrayList<>();

		Matcher matcher = pattern.matcher(s);

		while(matcher.find()) {
			matches.add(matcher.group(1));
		}

		return matches;
	}

	private Pattern quotedValue = Pattern.compile("\"(.*?)\"");
	private Pattern variableValue = Pattern.compile("\\$\\{(.*?)\\}");

	private String _proseContent;
}
