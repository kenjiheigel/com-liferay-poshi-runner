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

package com.liferay.poshi.runner;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.liferay.poshi.runner.pql.PQLEntity;
import com.liferay.poshi.runner.pql.PQLEntityFactory;
import com.liferay.poshi.runner.prose.PoshiProseMatcher;
import com.liferay.poshi.runner.selenium.LiferaySelenium;
import com.liferay.poshi.runner.util.FileUtil;
import com.liferay.poshi.runner.util.MathUtil;
import com.liferay.poshi.runner.util.OSDetector;
import com.liferay.poshi.runner.util.PropsValues;
import com.liferay.poshi.runner.util.StringUtil;
import com.liferay.poshi.runner.util.Validator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.lang.reflect.Method;

import java.net.URI;
import java.net.URL;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import org.dom4j.Element;

/**
 * @author Karen Dang
 * @author Michael Hashimoto
 */
public class PoshiRunnerContext {

	public static final String[] POSHI_SUPPORT_FILE_INCLUDES =
		{"**/*.action", "**/*.function", "**/*.macro", "**/*.path"};

	public static final String[] POSHI_TEST_FILE_INCLUDES =
		{"**/*.prose", "**/*.testcase"};

	public static void clear() {
		_commandElements.clear();
		_commandSummaries.clear();
		_filePaths.clear();
		_functionLocatorCounts.clear();
		_pathLocators.clear();
		_rootElements.clear();
		_seleniumParameterCounts.clear();
	}

	public static String getDefaultNamespace() {
		return _DEFAULT_NAMESPACE;
	}

	public static String getFilePathFromFileName(
		String fileName, String namespace) {

		return _filePaths.get(namespace + "." + fileName);
	}

	public static List<String> getFilePaths() {
		return new ArrayList<>(_filePaths.values());
	}

	public static Element getFunctionCommandElement(
		String classCommandName, String namespace) {

		return _commandElements.get(
			"function#" + namespace + "." + classCommandName);
	}

	public static String getFunctionCommandSummary(
		String classCommandName, String namespace) {

		return _commandSummaries.get(
			"function#" + namespace + "." + classCommandName);
	}

	public static Set<String> getFunctionFileNames() {
		return _functionFileNames;
	}

	public static int getFunctionLocatorCount(
		String className, String namespace) {

		String functionLocatorCountKey = namespace + "." + className;

		if (_functionLocatorCounts.containsKey(functionLocatorCountKey)) {
			return _functionLocatorCounts.get(functionLocatorCountKey);
		}

		return 0;
	}

	public static Element getFunctionRootElement(
		String className, String namespace) {

		return _rootElements.get("function#" + namespace + "." + className);
	}

	public static Element getMacroCommandElement(
		String classCommandName, String namespace) {

		return _commandElements.get(
			"macro#" + namespace + "." + classCommandName);
	}

	public static String getMacroCommandSummary(
		String classCommandName, String namespace) {

		return _commandSummaries.get(
			"macro#" + namespace + "." + classCommandName);
	}

	public static Element getMacroRootElement(
		String className, String namespace) {

		return _rootElements.get("macro#" + namespace + "." + className);
	}

	public static String getNamespaceFromFilePath(String filePath) {
		if (Validator.isNull(filePath)) {
			return getDefaultNamespace();
		}

		for (Map.Entry<String, String> entry : _filePaths.entrySet()) {
			String value = entry.getValue();

			if (value.equals(filePath)) {
				String key = entry.getKey();
				String fileName = PoshiRunnerGetterUtil.getFileNameFromFilePath(
					filePath);

				return key.substring(0, key.indexOf("." + fileName));
			}
		}

		return getDefaultNamespace();
	}

	public static String getOverrideClassName(String namespacedClassName) {
		return _overrideClassNames.get(namespacedClassName);
	}

	public static String getPathLocator(
		String pathLocatorKey, String namespace) {

		String pathLocator = _pathLocators.get(
			namespace + "." + pathLocatorKey);

		String className =
			PoshiRunnerGetterUtil.getClassNameFromNamespacedClassCommandName(
				pathLocatorKey);

		if ((pathLocator == null) &&
			_pathExtensions.containsKey(namespace + "." + className)) {

			String pathExtension = _pathExtensions.get(
				namespace + "." + className);
			String commandName =
				PoshiRunnerGetterUtil.
					getCommandNameFromNamespacedClassCommandName(
						pathLocatorKey);

			return getPathLocator(pathExtension + "#" + commandName, namespace);
		}

		return pathLocator;
	}

	public static Element getPathRootElement(
		String className, String namespace) {

		return _rootElements.get("path#" + namespace + "." + className);
	}

	public static List<Element> getRootVarElements(
		String classType, String className, String namespace) {

		return _rootVarElements.get(
			classType + "#" + namespace + "." + className);
	}

	public static int getSeleniumParameterCount(String commandName) {
		return _seleniumParameterCounts.get(commandName);
	}

	public static List<String> getTestCaseAvailablePropertyNames() {
		return _testCaseAvailablePropertyNames;
	}

	public static Element getTestCaseCommandElement(
		String classCommandName, String namespace) {

		return _commandElements.get(
			"test-case#" + namespace + "." + classCommandName);
	}

	public static String getTestCaseDescription(String classCommandName) {
		return _testCaseDescriptions.get(classCommandName);
	}

	public static String getTestCaseNamespacedClassCommandName() {
		return _testCaseNamespacedClassCommandName;
	}

	public static List<String> getTestCaseRequiredPropertyNames() {
		return _testCaseRequiredPropertyNames;
	}

	public static Element getTestCaseRootElement(
		String className, String namespace) {

		return _rootElements.get("test-case#" + namespace + "." + className);
	}

	public static boolean isCommandElement(
		String classType, String classCommandName, String namespace) {

		return _commandElements.containsKey(
			classType + "#" + namespace + "." + classCommandName);
	}

	public static boolean isPathLocator(
		String pathLocatorKey, String namespace) {

		String pathLocator = getPathLocator(pathLocatorKey, namespace);

		if (pathLocator != null) {
			return true;
		}

		return false;
	}

	public static boolean isRootElement(
		String classType, String rootElementKey, String namespace) {

		return _rootElements.containsKey(
			classType + "#" + namespace + "." + rootElementKey);
	}

	public static void main(String[] args) throws Exception {
		readFiles();

		_writeTestCaseMethodNamesProperties();
		_writeTestCSVReportFile();
		_writeTestGeneratedProperties();
	}

	public static void readFiles() throws Exception {
		_readPoshiFiles();
		_readSeleniumFiles();
	}

	public static void readFiles(String[] includes, String... baseDirNames)
		throws Exception {

		_readPoshiFilesFromClassPath(includes, "testFunctional");
		_readPoshiFiles(includes, baseDirNames);
		_readSeleniumFiles();
	}

	public static void setTestCaseNamespacedClassCommandName(
		String testCaseNamespacedClassCommandName) {

		_testCaseNamespacedClassCommandName =
			testCaseNamespacedClassCommandName;
	}

	private static int _getAllocatedTestGroupSize(int testCount) {
		int groupCount = MathUtil.quotient(
			testCount, PropsValues.TEST_BATCH_MAX_GROUP_SIZE, true);

		return MathUtil.quotient(testCount, groupCount, true);
	}

	private static Properties _getClassCommandNameProperties(
			Element rootElement, Element commandElement)
		throws Exception {

		Properties properties = new Properties();

		List<Element> rootPropertyElements = rootElement.elements("property");

		for (Element propertyElement : rootPropertyElements) {
			String propertyName = propertyElement.attributeValue("name");
			String propertyValue = propertyElement.attributeValue("value");

			properties.setProperty(propertyName, propertyValue);
		}

		List<Element> commandPropertyElements = commandElement.elements(
			"property");

		for (Element propertyElement : commandPropertyElements) {
			String propertyName = propertyElement.attributeValue("name");
			String propertyValue = propertyElement.attributeValue("value");

			properties.setProperty(propertyName, propertyValue);
		}

		if (Validator.isNotNull(
				commandElement.attributeValue("known-issues"))) {

			String knownIssues = commandElement.attributeValue("known-issues");

			properties.setProperty("known-issues", knownIssues);
		}

		if (Validator.isNotNull(commandElement.attributeValue("priority"))) {
			String priority = commandElement.attributeValue("priority");

			properties.setProperty("priority", priority);
		}

		return properties;
	}

	private static String _getCommandSummary(
		String classCommandName, String classType, Element commandElement,
		Element rootElement) {

		String summaryIgnore = commandElement.attributeValue("summary-ignore");

		if (Validator.isNotNull(summaryIgnore) &&
			summaryIgnore.equals("true")) {

			return null;
		}

		if (Validator.isNotNull(commandElement.attributeValue("summary"))) {
			return commandElement.attributeValue("summary");
		}

		if (classType.equals("function")) {
			if (Validator.isNotNull(rootElement.attributeValue("summary"))) {
				return rootElement.attributeValue("summary");
			}
		}

		return classCommandName;
	}

	private static Exception _getDuplicateLocatorsException() {
		StringBuilder sb = new StringBuilder();

		sb.append(
			"Duplicate locator(s) found while loading Poshi files into " +
				"context:\n");

		for (String exception : _duplicateLocatorMessages) {
			sb.append(exception);
			sb.append("\n\n");
		}

		return new Exception(sb.toString());
	}

	private static List<URL> _getPoshiURLs(
			FileSystem fileSystem, String[] includes, String baseDirName)
		throws IOException {

		List<URL> urls = null;

		if (fileSystem == null) {
			urls = FileUtil.getIncludedResourceURLs(includes, baseDirName);
		}
		else {
			urls = FileUtil.getIncludedResourceURLs(
				fileSystem, includes, baseDirName);
		}

		return urls;
	}

	private static List<URL> _getPoshiURLs(
			String[] includes, String baseDirName)
		throws Exception {

		return _getPoshiURLs(null, includes, baseDirName);
	}

	private static String _getTestBatchGroups() throws Exception {
		String propertyQuery = PropsValues.TEST_BATCH_PROPERTY_QUERY;

		if (propertyQuery == null) {
			String[] propertyNames = PropsValues.TEST_BATCH_PROPERTY_NAMES;
			String[] propertyValues = PropsValues.TEST_BATCH_PROPERTY_VALUES;

			if (propertyNames.length != propertyValues.length) {
				throw new Exception(
					"'test.batch.property.names'" +
						"/'test.batch.property.values' must have matching " +
							"amounts of entries!");
			}

			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < propertyNames.length; i++) {
				sb.append(propertyNames[i]);
				sb.append(" == \"");
				sb.append(propertyValues[i]);
				sb.append("\"");

				if (i < (propertyNames.length - 1)) {
					sb.append(" OR ");
				}
			}

			propertyQuery = sb.toString();
		}

		if (Validator.isNotNull(PropsValues.TEST_RUN_ENVIRONMENT)) {
			StringBuilder sb = new StringBuilder();

			sb.append("(");
			sb.append(propertyQuery);
			sb.append(") AND (ignored != true) AND ");
			sb.append("(test.run.environment == \"");
			sb.append(PropsValues.TEST_RUN_ENVIRONMENT);
			sb.append("\" OR test.run.environment == null)");

			propertyQuery = sb.toString();
		}

		List<String> namespacedClassCommandNames = new ArrayList<>();

		PQLEntity pqlEntity = PQLEntityFactory.newPQLEntity(propertyQuery);

		for (String testCaseNamespacedClassCommandName :
				_testCaseNamespacedClassCommandNames) {

			Properties properties =
				_namespacedClassCommandNamePropertiesMap.get(
					testCaseNamespacedClassCommandName);

			Boolean pqlResultBoolean = (Boolean)pqlEntity.getPQLResult(
				properties);

			if (pqlResultBoolean) {
				namespacedClassCommandNames.add(
					testCaseNamespacedClassCommandName);
			}
		}

		System.out.println(
			"The following query returned " +
				namespacedClassCommandNames.size() +
					" test class command names:");
		System.out.println(propertyQuery);

		try {
			PoshiRunnerValidation.validate();
		}
		catch (Exception e) {
			System.out.println(
				"Poshi validation issues occurred. Functional batch " +
					"properties will not be created, resulting in functional " +
						"tests being skipped.");

			StringBuilder sb = new StringBuilder();

			sb.append("SKIPPED_TEST_CASE_METHOD_GROUP_0=");

			for (String namespacedClassCommandName :
					namespacedClassCommandNames) {

				sb.append(namespacedClassCommandName);
				sb.append(",");
			}

			sb.setLength(sb.length() - 1);

			return sb.toString();
		}

		if (PropsValues.TEST_BATCH_RUN_TYPE.equals("sequential")) {
			return _getTestBatchSequentialGroups(namespacedClassCommandNames);
		}

		if (PropsValues.TEST_BATCH_RUN_TYPE.equals("single")) {
			return _getTestBatchSingleGroups(namespacedClassCommandNames);
		}

		throw new Exception(
			"'test.batch.run.type' must be set to 'single' or 'sequential'");
	}

	private static String _getTestBatchSequentialGroups(
			List<String> namespacedClassCommandNames)
		throws Exception {

		Multimap<Properties, String> multimap = HashMultimap.create();

		for (String namespacedClassCommandName : namespacedClassCommandNames) {
			Properties properties = new Properties();

			properties.putAll(
				_namespacedClassCommandNamePropertiesMap.get(
					namespacedClassCommandName));

			if (Validator.isNotNull(
					PropsValues.TEST_BATCH_GROUP_IGNORE_REGEX)) {

				Set<String> propertyNames = properties.stringPropertyNames();

				for (String propertyName : propertyNames) {
					if (propertyName.matches(
							PropsValues.TEST_BATCH_GROUP_IGNORE_REGEX)) {

						properties.remove(propertyName);
					}
				}
			}

			multimap.put(properties, namespacedClassCommandName);
		}

		Map<Integer, List<String>> classCommandNameGroups = new HashMap<>();
		int classCommandNameIndex = 0;
		Map<Properties, Collection<String>> map = multimap.asMap();

		for (Collection<String> value : map.values()) {
			List<String> classCommandNameGroup = new ArrayList(value);

			Collections.sort(classCommandNameGroup);

			int groupSize = _getAllocatedTestGroupSize(
				classCommandNameGroup.size());

			List<List<String>> partitions = Lists.partition(
				classCommandNameGroup, groupSize);

			for (List<String> partition : partitions) {
				classCommandNameGroups.put(classCommandNameIndex, partition);

				classCommandNameIndex++;
			}
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < classCommandNameGroups.size(); i++) {
			List<String> classCommandNameGroup = classCommandNameGroups.get(i);
			int subgroupSize = PropsValues.TEST_BATCH_MAX_SUBGROUP_SIZE;

			int subgroupCount = MathUtil.quotient(
				classCommandNameGroup.size(), subgroupSize, true);

			sb.append("RUN_TEST_CASE_METHOD_GROUP_");
			sb.append(i);
			sb.append("=");

			for (int j = 0; j < subgroupCount; j++) {
				sb.append(i);
				sb.append("_");
				sb.append(j);

				if (j < (subgroupCount - 1)) {
					sb.append(" ");
				}
			}

			sb.append("\n");

			for (int j = 0; j < classCommandNameGroup.size(); j++) {
				if ((j % subgroupSize) == 0) {
					sb.append("RUN_TEST_CASE_METHOD_GROUP_");
					sb.append(i);
					sb.append("_");
					sb.append(j / subgroupSize);
					sb.append("=");
					sb.append(classCommandNameGroup.get(j));
				}
				else if (((j + 1) % subgroupSize) == 0) {
					sb.append(",");
					sb.append(classCommandNameGroup.get(j));
					sb.append("\n");
				}
				else {
					sb.append(",");
					sb.append(classCommandNameGroup.get(j));
				}
			}

			sb.append("\n");
		}

		sb.append("RUN_TEST_CASE_METHOD_GROUPS=");

		for (int i = 0; i < classCommandNameGroups.size(); i++) {
			sb.append(i);

			if (i < (classCommandNameGroups.size() - 1)) {
				sb.append(" ");
			}
		}

		return sb.toString();
	}

	private static String _getTestBatchSingleGroups(
		List<String> namespacedClassCommandNames) {

		StringBuilder sb = new StringBuilder();

		int groupSize = 15;

		List<List<String>> partitions = Lists.partition(
			namespacedClassCommandNames, groupSize);

		for (int i = 0; i < partitions.size(); i++) {
			sb.append("RUN_TEST_CASE_METHOD_GROUP_");
			sb.append(i);
			sb.append("=");

			List<String> partition = partitions.get(i);

			for (int j = 0; j < partition.size(); j++) {
				sb.append(i);
				sb.append("_");
				sb.append(j);

				if (j < (partition.size() - 1)) {
					sb.append(" ");
				}
			}

			sb.append("\n");

			for (int j = 0; j < partition.size(); j++) {
				sb.append("RUN_TEST_CASE_METHOD_GROUP_");
				sb.append(i);
				sb.append("_");
				sb.append(j);
				sb.append("=");
				sb.append(partition.get(j));
				sb.append("\n");
			}
		}

		sb.append("RUN_TEST_CASE_METHOD_GROUPS=");

		for (int i = 0; i < partitions.size(); i++) {
			sb.append(i);

			if (i < (partitions.size() - 1)) {
				sb.append(" ");
			}
		}

		return sb.toString();
	}

	private static void _initComponentCommandNamesMap() {
		for (String testCaseNamespacedClassName :
				_testCaseNamespacedClassNames) {

			String className =
				PoshiRunnerGetterUtil.getClassNameFromNamespacedClassName(
					testCaseNamespacedClassName);
			String namespace =
				PoshiRunnerGetterUtil.getNamespaceFromNamespacedClassName(
					testCaseNamespacedClassName);

			Element rootElement = getTestCaseRootElement(className, namespace);

			if (rootElement.attributeValue("extends") != null) {
				String extendsTestCaseClassName = rootElement.attributeValue(
					"extends");

				Element extendsRootElement = getTestCaseRootElement(
					extendsTestCaseClassName, namespace);

				List<Element> extendsCommandElements =
					extendsRootElement.elements("command");

				for (Element extendsCommandElement : extendsCommandElements) {
					String extendsCommandName =
						extendsCommandElement.attributeValue("name");

					String extendsNamespacedClassCommandName =
						StringUtil.combine(
							namespace, ".", extendsTestCaseClassName, "#",
							extendsCommandName);

					Properties properties =
						_namespacedClassCommandNamePropertiesMap.get(
							extendsNamespacedClassCommandName);

					if (_isIgnorableCommandNames(
							rootElement, extendsCommandElement,
							extendsCommandName)) {

						properties.setProperty("ignored", "true");
					}

					String namespacedClassCommandName = StringUtil.combine(
						namespace, ".", className, "#", extendsCommandName);

					_namespacedClassCommandNamePropertiesMap.put(
						namespacedClassCommandName, properties);

					_commandElements.put(
						"test-case#" + namespacedClassCommandName,
						extendsCommandElement);
				}
			}

			List<Element> commandElements = rootElement.elements("command");

			for (Element commandElement : commandElements) {
				String commandName = commandElement.attributeValue("name");

				if (_isIgnorableCommandNames(
						rootElement, commandElement, commandName)) {

					String namespacedClassCommandName = StringUtil.combine(
						namespace, ".", className, "#", commandName);

					Properties properties =
						_namespacedClassCommandNamePropertiesMap.get(
							namespacedClassCommandName);

					properties.setProperty("ignored", "true");

					_namespacedClassCommandNamePropertiesMap.put(
						namespacedClassCommandName, properties);
				}

				String namespacedClassCommandName =
					testCaseNamespacedClassName + "#" + commandName;

				_testCaseNamespacedClassCommandNames.add(
					namespacedClassCommandName);
			}
		}
	}

	private static boolean _isClassOverridden(String namespacedClassName) {
		return _overrideClassNames.containsKey(namespacedClassName);
	}

	private static boolean _isIgnorableCommandNames(
		Element rootElement, Element commandElement, String commandName) {

		if (Objects.equals(commandElement.attributeValue("ignore"), "true")) {
			return true;
		}

		if (Objects.equals(commandElement.attributeValue("ignore"), "false")) {
			return false;
		}

		List<String> ignorableCommandNames = new ArrayList<>();

		if (rootElement.attributeValue("ignore-command-names") != null) {
			String ignoreCommandNamesString = rootElement.attributeValue(
				"ignore-command-names");

			ignorableCommandNames = Arrays.asList(
				ignoreCommandNamesString.split(","));
		}

		if (ignorableCommandNames.contains(commandName)) {
			return true;
		}

		if (Objects.equals(rootElement.attributeValue("ignore"), "true")) {
			return true;
		}

		return false;
	}

	private static void _overrideRootElement(
			Element rootElement, String filePath, String namespace)
		throws Exception {

		String className = PoshiRunnerGetterUtil.getClassNameFromFilePath(
			filePath);

		String baseNamespacedClassName = rootElement.attributeValue("override");

		String baseClassName =
			PoshiRunnerGetterUtil.getClassNameFromNamespacedClassName(
				baseNamespacedClassName);

		if (!className.equals(baseClassName)) {
			StringBuilder sb = new StringBuilder();

			sb.append("Override class name does not match base class name:\n");
			sb.append("Override: ");
			sb.append(className);
			sb.append("\nBase: ");
			sb.append(baseClassName);

			throw new RuntimeException(sb.toString());
		}

		String baseNamespace =
			PoshiRunnerGetterUtil.getNamespaceFromNamespacedClassName(
				baseNamespacedClassName);

		if (_isClassOverridden(baseNamespace + "." + baseClassName)) {
			StringBuilder sb = new StringBuilder();

			sb.append("Duplicate override for class '");
			sb.append(baseNamespace);
			sb.append(".");
			sb.append(baseClassName);
			sb.append("'\n at ");
			sb.append(namespace);
			sb.append(".");
			sb.append(className);
			sb.append("\npreviously overridden by ");
			sb.append(
				getOverrideClassName(baseNamespace + "." + baseClassName));

			throw new RuntimeException(sb.toString());
		}

		String classType = PoshiRunnerGetterUtil.getClassTypeFromFilePath(
			filePath);

		if (classType.equals("test-case")) {
			Element setUpElement = rootElement.element("set-up");

			if (setUpElement != null) {
				String classCommandName = className + "#set-up";

				_commandElements.put(
					classType + "#" + baseNamespace + "." + classCommandName,
					setUpElement);
			}

			Element tearDownElement = rootElement.element("tear-down");

			if (tearDownElement != null) {
				String classCommandName = className + "#tear-down";

				_commandElements.put(
					classType + "#" + baseNamespace + "." + classCommandName,
					tearDownElement);
			}
		}

		if (classType.equals("action") || classType.equals("function") ||
			classType.equals("macro") || classType.equals("test-case")) {

			List<Element> overrideVarElements = rootElement.elements("var");

			if (!overrideVarElements.isEmpty()) {
				List<Element> baseVarElements = getRootVarElements(
					classType, className, baseNamespace);

				Map<String, Element> overriddenVarElementMap = new HashMap();

				for (Element baseVarElement : baseVarElements) {
					overriddenVarElementMap.put(
						baseVarElement.attributeValue("name"), baseVarElement);
				}

				for (Element overrideVarElement : overrideVarElements) {
					overriddenVarElementMap.put(
						overrideVarElement.attributeValue("name"),
						overrideVarElement);
				}

				_rootVarElements.put(
					classType + "#" + baseNamespace + "." + className,
					new ArrayList<>(overriddenVarElementMap.values()));
			}

			List<Element> overrideCommandElements = rootElement.elements(
				"command");

			for (Element overrideCommandElement : overrideCommandElements) {
				String commandName = overrideCommandElement.attributeValue(
					"name");

				String classCommandName = className + "#" + commandName;

				String baseNamespacedClassCommandName =
					baseNamespace + "." + className + "#" + commandName;

				_commandElements.put(
					classType + "#" + baseNamespacedClassCommandName,
					overrideCommandElement);

				_commandSummaries.put(
					classType + "#" + baseNamespacedClassCommandName,
					_getCommandSummary(
						classCommandName, classType, overrideCommandElement,
						rootElement));

				String prose = overrideCommandElement.attributeValue("prose");

				if (classType.equals("macro") && (prose != null) &&
					!prose.isEmpty()) {

					PoshiProseMatcher.storePoshiProseMatcher(
						overrideCommandElement.attributeValue("prose"),
						baseNamespacedClassCommandName);
				}

				if (classType.equals("test-case")) {
					Properties baseProperties =
						_namespacedClassCommandNamePropertiesMap.get(
							baseNamespacedClassCommandName);

					Properties overrideProperties =
						_getClassCommandNameProperties(
							rootElement, overrideCommandElement);

					Properties overriddenProperties = new Properties(
						baseProperties);

					overriddenProperties.putAll(overrideProperties);

					_namespacedClassCommandNamePropertiesMap.put(
						baseNamespacedClassCommandName, overriddenProperties);

					if (Validator.isNotNull(
							overrideCommandElement.attributeValue(
								"description"))) {

						_testCaseDescriptions.put(
							baseNamespacedClassCommandName,
							overrideCommandElement.attributeValue(
								"description"));
					}
				}
			}
		}

		if (classType.equals("function")) {
			String defaultClassCommandName =
				className + "#" + rootElement.attributeValue("default");

			Element defaultCommandElement = getFunctionCommandElement(
				defaultClassCommandName, baseNamespace);

			_commandElements.put(
				classType + "#" + baseNamespace + "." + className,
				defaultCommandElement);

			_commandSummaries.put(
				classType + "#" + baseNamespace + "." + className,
				_getCommandSummary(
					defaultClassCommandName, classType, defaultCommandElement,
					rootElement));

			String xml = rootElement.asXML();

			for (int i = 1;; i++) {
				if (xml.contains("${locator" + i + "}")) {
					continue;
				}

				if (i > 1) {
					i--;
				}

				int baseLocatorCount = _functionLocatorCounts.get(
					baseNamespace + "." + className);

				if (i > baseLocatorCount) {
					StringBuilder sb = new StringBuilder();

					sb.append("Overriding function file cannot have a ");
					sb.append("locator count higher than base function file\n");
					sb.append(namespace);
					sb.append(".");
					sb.append(className);
					sb.append(" locator count: ");
					sb.append(i);
					sb.append("\n");
					sb.append(baseNamespace);
					sb.append(".");
					sb.append(className);
					sb.append(" locator count: ");
					sb.append(baseLocatorCount);

					throw new RuntimeException(sb.toString());
				}

				break;
			}
		}
		else if (classType.equals("path")) {
			_storePathElement(rootElement, className, filePath, baseNamespace);
		}
	}

	private static void _readPoshiFiles() throws Exception {
		if (Validator.isNotNull(PropsValues.TEST_INCLUDE_DIR_NAMES)) {
			_readPoshiFiles(
				POSHI_SUPPORT_FILE_INCLUDES,
				PropsValues.TEST_INCLUDE_DIR_NAMES);
		}

		String[] poshiFileIncludes = ArrayUtils.addAll(
			PoshiRunnerContext.POSHI_SUPPORT_FILE_INCLUDES,
			PoshiRunnerContext.POSHI_TEST_FILE_INCLUDES);

		_readPoshiFilesFromClassPath(poshiFileIncludes, "testFunctional");

		if (Validator.isNotNull(PropsValues.TEST_SUBREPO_DIRS)) {
			_readPoshiFiles(poshiFileIncludes, PropsValues.TEST_SUBREPO_DIRS);
		}

		_readPoshiFiles(poshiFileIncludes, _TEST_BASE_DIR_NAME);

		_initComponentCommandNamesMap();

		if (!_duplicateLocatorMessages.isEmpty()) {
			throw _getDuplicateLocatorsException();
		}
	}

	private static void _readPoshiFiles(
			String[] includes, String... baseDirNames)
		throws Exception {

		for (String baseDirName : baseDirNames) {
			List<URL> poshiURLs = _getPoshiURLs(includes, baseDirName);

			_storeRootElements(poshiURLs, _DEFAULT_NAMESPACE);
		}
	}

	private static void _readPoshiFilesFromClassPath(
			String[] includes, String... resourceNames)
		throws Exception {

		for (String resourceName : resourceNames) {
			ClassLoader classLoader = PoshiRunnerContext.class.getClassLoader();

			Enumeration<URL> enumeration = classLoader.getResources(
				resourceName);

			while (enumeration.hasMoreElements()) {
				URL resourceURL = enumeration.nextElement();

				String resourceURLString = resourceURL.toString();

				int x = resourceURLString.indexOf("!");

				try (FileSystem fileSystem = FileSystems.newFileSystem(
						URI.create(resourceURLString.substring(0, x)),
						new HashMap<String, String>(), classLoader)) {

					Matcher matcher = _poshiResourceJarNamePattern.matcher(
						resourceURLString);

					if (!matcher.find()) {
						throw new RuntimeException(
							"A namespace must be defined for resource: " +
								resourceURLString);
					}

					String namespace = StringUtils.capitalize(
						matcher.group("namespace"));

					if (namespace.equals(_DEFAULT_NAMESPACE)) {
						throw new RuntimeException(
							"Namespace: '" + _DEFAULT_NAMESPACE +
								"' is reserved");
					}
					else if (_namespaces.contains(namespace)) {
						throw new RuntimeException(
							"Duplicate namespace " + namespace);
					}

					_namespaces.add(namespace);

					List<URL> poshiURLs = _getPoshiURLs(
						fileSystem, includes,
						resourceURLString.substring(x + 1));

					_storeRootElements(poshiURLs, namespace);
				}
			}
		}
	}

	private static void _readSeleniumFiles() throws Exception {
		Method[] methods = LiferaySelenium.class.getMethods();

		for (Method method : methods) {
			Class<?>[] classes = method.getParameterTypes();

			_seleniumParameterCounts.put(method.getName(), classes.length);
		}

		_seleniumParameterCounts.put("open", 1);
	}

	private static void _storePathElement(
			Element rootElement, String className, String filePath,
			String namespace)
		throws Exception {

		if (rootElement.attributeValue("override") == null) {
			_rootElements.put(
				"path#" + namespace + "." + className, rootElement);
		}

		List<String> locatorKeys = new ArrayList<>();

		Element bodyElement = rootElement.element("body");

		Element tableElement = bodyElement.element("table");

		Element tBodyElement = tableElement.element("tbody");

		List<Element> trElements = tBodyElement.elements("tr");

		for (Element trElement : trElements) {
			List<Element> tdElements = trElement.elements("td");

			Element locatorKeyElement = tdElements.get(0);

			String locatorKey = locatorKeyElement.getText();

			Element locatorElement = tdElements.get(1);

			String locator = locatorElement.getText();

			if (locatorKey.equals("") && locator.equals("")) {
				continue;
			}

			if (locatorKeys.contains(locatorKey)) {
				StringBuilder sb = new StringBuilder();

				sb.append("Duplicate path locator key '");
				sb.append(className + "#" + locatorKey);
				sb.append("' at namespace '");
				sb.append(namespace);
				sb.append("' \n");
				sb.append(filePath);
				sb.append(": ");
				sb.append(
					PoshiRunnerGetterUtil.getLineNumber(locatorKeyElement));

				_duplicateLocatorMessages.add(sb.toString());
			}

			locatorKeys.add(locatorKey);

			if (locatorKey.equals("EXTEND_ACTION_PATH")) {
				_pathExtensions.put(namespace + "." + className, locator);
			}
			else {
				_pathLocators.put(
					namespace + "." + className + "#" + locatorKey, locator);
			}
		}
	}

	private static void _storeRootElement(
			Element rootElement, String filePath, String namespace)
		throws Exception {

		if (rootElement.attributeValue("override") != null) {
			_overrideRootElement(rootElement, filePath, namespace);

			return;
		}

		String className = PoshiRunnerGetterUtil.getClassNameFromFilePath(
			filePath);
		String classType = PoshiRunnerGetterUtil.getClassTypeFromFilePath(
			filePath);

		if (classType.equals("test-case")) {
			_testCaseNamespacedClassNames.add(namespace + "." + className);

			if (rootElement.element("set-up") != null) {
				Element setUpElement = rootElement.element("set-up");

				String classCommandName = className + "#set-up";

				_commandElements.put(
					classType + "#" + namespace + "." + classCommandName,
					setUpElement);
			}

			if (rootElement.element("tear-down") != null) {
				Element tearDownElement = rootElement.element("tear-down");

				String classCommandName = className + "#tear-down";

				_commandElements.put(
					classType + "#" + namespace + "." + classCommandName,
					tearDownElement);
			}
		}

		if (classType.equals("action") || classType.equals("function") ||
			classType.equals("macro") || classType.equals("test-case")) {

			_rootElements.put(
				classType + "#" + namespace + "." + className, rootElement);

			_rootVarElements.put(
				classType + "#" + namespace + "." + className,
				rootElement.elements("var"));

			List<Element> commandElements = rootElement.elements("command");

			for (Element commandElement : commandElements) {
				String commandName = commandElement.attributeValue("name");

				String classCommandName = className + "#" + commandName;

				if (isCommandElement(classType, classCommandName, namespace)) {
					StringBuilder sb = new StringBuilder();

					sb.append("Duplicate command name '");
					sb.append(classCommandName);
					sb.append("' at namespace '");
					sb.append(namespace);
					sb.append("'\n");
					sb.append(filePath);
					sb.append(": ");
					sb.append(
						PoshiRunnerGetterUtil.getLineNumber(commandElement));
					sb.append("\n");

					String duplicateElementFilePath = getFilePathFromFileName(
						PoshiRunnerGetterUtil.getFileNameFromFilePath(filePath),
						namespace);

					if (Validator.isNull(duplicateElementFilePath)) {
						duplicateElementFilePath = filePath;
					}

					sb.append(duplicateElementFilePath);
					sb.append(": ");

					Element duplicateElement = _commandElements.get(
						classType + "#" + namespace + "." + classCommandName);

					sb.append(
						PoshiRunnerGetterUtil.getLineNumber(duplicateElement));

					_duplicateLocatorMessages.add(sb.toString());

					continue;
				}

				String namespacedClassCommandName =
					namespace + "." + className + "#" + commandName;

				_commandElements.put(
					classType + "#" + namespacedClassCommandName,
					commandElement);

				_commandSummaries.put(
					classType + "#" + namespacedClassCommandName,
					_getCommandSummary(
						classCommandName, classType, commandElement,
						rootElement));

				String prose = commandElement.attributeValue("prose");

				if (classType.equals("macro") && (prose != null) &&
					!prose.isEmpty()) {

					PoshiProseMatcher.storePoshiProseMatcher(
						commandElement.attributeValue("prose"),
						namespacedClassCommandName);
				}

				if (classType.equals("test-case")) {
					Properties properties = _getClassCommandNameProperties(
						rootElement, commandElement);

					_namespacedClassCommandNamePropertiesMap.put(
						namespace + "." + classCommandName, properties);

					if (Validator.isNotNull(
							commandElement.attributeValue("description"))) {

						_testCaseDescriptions.put(
							namespacedClassCommandName,
							commandElement.attributeValue("description"));
					}
				}
			}

			if (classType.equals("function")) {
				String defaultClassCommandName =
					className + "#" + rootElement.attributeValue("default");

				Element defaultCommandElement = getFunctionCommandElement(
					defaultClassCommandName, namespace);

				_commandElements.put(
					classType + "#" + namespace + "." + className,
					defaultCommandElement);

				_commandSummaries.put(
					classType + "#" + namespace + "." + className,
					_getCommandSummary(
						defaultClassCommandName, classType,
						defaultCommandElement, rootElement));

				String xml = rootElement.asXML();

				for (int i = 1;; i++) {
					if (xml.contains("${locator" + i + "}")) {
						continue;
					}

					if (i > 1) {
						i--;
					}

					_functionLocatorCounts.put(namespace + "." + className, i);

					break;
				}
			}
		}
		else if (classType.equals("path")) {
			_storePathElement(rootElement, className, filePath, namespace);
		}
	}

	private static void _storeRootElements(List<URL> urls, String namespace)
		throws Exception {

		Map<String, String> filePaths = new HashMap<>();

		Collections.sort(
			urls,
			new Comparator<URL>() {

				@Override
				public int compare(URL url1, URL url2) {
					String urlPath1 = url1.getPath();
					String urlPath2 = url2.getPath();

					Matcher urlPathMatcher1 = _urlPathPattern.matcher(urlPath1);
					Matcher urlPathMatcher2 = _urlPathPattern.matcher(urlPath2);

					if (urlPathMatcher1.find() && urlPathMatcher2.find()) {
						String fileType1 = urlPathMatcher1.group(1);
						String fileType2 = urlPathMatcher2.group(1);

						List<String> fileTypeList = Arrays.asList(
							"action", "path", "function", "macro", "testcase",
							"prose");

						Integer fileTypeIndex1 = fileTypeList.indexOf(
							StringUtil.toLowerCase(fileType1));
						Integer fileTypeIndex2 = fileTypeList.indexOf(
							StringUtil.toLowerCase(fileType2));

						int indexCompareValue = fileTypeIndex1.compareTo(
							fileTypeIndex2);

						if (indexCompareValue == 0) {
							return urlPath1.compareTo(urlPath2);
						}

						return indexCompareValue;
					}

					throw new RuntimeException("Unable to sort Poshi files");
				}

			});

		for (URL url : urls) {
			String filePath = url.getFile();

			if (OSDetector.isWindows()) {
				if (filePath.startsWith("/")) {
					filePath = filePath.substring(1);
				}

				filePath = filePath.replace("/", "\\");
			}

			String fileName = PoshiRunnerGetterUtil.getFileNameFromFilePath(
				filePath);

			if (filePaths.containsKey(fileName)) {
				System.out.println(
					"WARNING: Duplicate file name '" + fileName +
						"' found within the namespace '" + namespace + "':\n" +
							filePath + "\n" + filePaths.get(fileName) + "\n");
			}

			filePaths.put(fileName, filePath);

			Element rootElement = PoshiRunnerGetterUtil.getRootElementFromURL(
				url);

			_storeRootElement(rootElement, filePath, namespace);

			if (rootElement.attributeValue("override") == null) {
				_filePaths.put(namespace + "." + fileName, filePath);

				if (fileName.endsWith(".function")) {
					_functionFileNames.add(fileName.replace(".function", ""));
				}
			}
		}
	}

	private static void _writeTestCaseMethodNamesProperties() throws Exception {
		StringBuilder sb = new StringBuilder();

		if ((PropsValues.TEST_BATCH_MAX_GROUP_SIZE > 0) &&
			(((PropsValues.TEST_BATCH_PROPERTY_NAMES != null) &&
			  (PropsValues.TEST_BATCH_PROPERTY_VALUES != null)) ||
			 (PropsValues.TEST_BATCH_PROPERTY_QUERY != null))) {

			sb.append(_getTestBatchGroups());
		}

		FileUtil.write("test.case.method.names.properties", sb.toString());
	}

	private static void _writeTestCSVReportFile() throws Exception {
		if (PropsValues.TEST_CSV_REPORT_PROPERTY_NAMES == null) {
			return;
		}

		PoshiRunnerValidation.validate();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");

		File reportCSVFile = new File(
			StringUtil.combine(
				"Report_", simpleDateFormat.format(new Date()), ".csv"));

		try (FileWriter reportCSVFileWriter = new FileWriter(reportCSVFile)) {
			List<String> reportLineItems = new ArrayList<>();

			reportLineItems.add("Namespace");
			reportLineItems.add("Class Name");
			reportLineItems.add("Command Name");

			for (String propertyName :
					PropsValues.TEST_CSV_REPORT_PROPERTY_NAMES) {

				reportLineItems.add(propertyName);
			}

			reportCSVFileWriter.write(StringUtils.join(reportLineItems, ","));

			reportLineItems.clear();

			for (String testCaseNamespacedClassCommandName :
					_testCaseNamespacedClassCommandNames) {

				Matcher matcher = _namespaceClassCommandNamePattern.matcher(
					testCaseNamespacedClassCommandName);

				if (!matcher.find()) {
					throw new RuntimeException(
						"Invalid namespaced class command name " +
							testCaseNamespacedClassCommandName);
				}

				reportLineItems.add(matcher.group("namespace"));
				reportLineItems.add(matcher.group("className"));
				reportLineItems.add(matcher.group("commandName"));

				Properties properties =
					_namespacedClassCommandNamePropertiesMap.get(
						testCaseNamespacedClassCommandName);

				for (String propertyName :
						PropsValues.TEST_CSV_REPORT_PROPERTY_NAMES) {

					if (properties.containsKey(propertyName)) {
						String propertyValue = properties.getProperty(
							propertyName);

						if (propertyValue.contains(",")) {
							reportLineItems.add(
								StringUtils.join(
									ArrayUtils.toArray(
										"\"", propertyValue, "\"")));
						}
						else {
							reportLineItems.add(propertyValue);
						}
					}
					else {
						reportLineItems.add("");
					}
				}

				reportCSVFileWriter.write(
					"\n" + StringUtils.join(reportLineItems, ","));

				reportLineItems.clear();
			}
		}
		catch (IOException ioe) {
			if (reportCSVFile.exists()) {
				reportCSVFile.deleteOnExit();
			}

			throw new RuntimeException(ioe);
		}
	}

	private static void _writeTestGeneratedProperties() throws Exception {
		PoshiRunnerValidation.validate();

		StringBuilder sb = new StringBuilder();

		for (String testCaseNamespacedClassCommandName :
				_testCaseNamespacedClassCommandNames) {

			Properties properties =
				_namespacedClassCommandNamePropertiesMap.get(
					testCaseNamespacedClassCommandName);
			String testNamespacedClassName =
				PoshiRunnerGetterUtil.
					getNamespacedClassNameFromNamespacedClassCommandName(
						testCaseNamespacedClassCommandName);
			String testCommandName =
				PoshiRunnerGetterUtil.
					getCommandNameFromNamespacedClassCommandName(
						testCaseNamespacedClassCommandName);

			for (String propertyName : properties.stringPropertyNames()) {
				sb.append(testNamespacedClassName);
				sb.append("TestCase.test");
				sb.append(testCommandName);
				sb.append(".");
				sb.append(propertyName);
				sb.append("=");
				sb.append(properties.getProperty(propertyName));
				sb.append("\n");
			}
		}

		FileUtil.write("test.generated.properties", sb.toString());
	}

	private static final String _DEFAULT_NAMESPACE = "LocalFile";

	private static final String _TEST_BASE_DIR_NAME =
		PoshiRunnerGetterUtil.getCanonicalPath(PropsValues.TEST_BASE_DIR_NAME);

	private static final Map<String, Element> _commandElements =
		new HashMap<>();
	private static final Map<String, String> _commandSummaries =
		new HashMap<>();
	private static final Set<String> _duplicateLocatorMessages =
		new HashSet<>();
	private static final Map<String, String> _filePaths = new HashMap<>();
	private static final Set<String> _functionFileNames = new HashSet<>();
	private static final Map<String, Integer> _functionLocatorCounts =
		new HashMap<>();
	private static final Pattern _namespaceClassCommandNamePattern =
		Pattern.compile(
			"(?<namespace>[^\\.]+)\\.(?<className>[^\\#]+)\\#" +
				"(?<commandName>.+)");
	private static final Map<String, Properties>
		_namespacedClassCommandNamePropertiesMap = new HashMap<>();
	private static final List<String> _namespaces = new ArrayList<>();
	private static final Map<String, String> _overrideClassNames =
		new HashMap<>();
	private static final Map<String, String> _pathExtensions = new HashMap<>();
	private static final Map<String, String> _pathLocators = new HashMap<>();
	private static final Pattern _poshiResourceJarNamePattern = Pattern.compile(
		"jar:.*\\/(?<namespace>\\w+)\\-(?<branchName>\\w+" +
			"([\\-\\.]\\w+)*)\\-(?<timestamp>\\d+)\\-(?<sha>\\w+)\\.jar.*");
	private static final Map<String, Element> _rootElements = new HashMap<>();
	private static final Map<String, List<Element>> _rootVarElements =
		new HashMap<>();
	private static final Map<String, Integer> _seleniumParameterCounts =
		new HashMap<>();
	private static final List<String> _testCaseAvailablePropertyNames =
		new ArrayList<>();
	private static final Map<String, String> _testCaseDescriptions =
		new HashMap<>();
	private static String _testCaseNamespacedClassCommandName;
	private static final List<String> _testCaseNamespacedClassCommandNames =
		new ArrayList<>();
	private static final List<String> _testCaseNamespacedClassNames =
		new ArrayList<>();
	private static final List<String> _testCaseRequiredPropertyNames =
		new ArrayList<>();
	private static final Pattern _urlPathPattern = Pattern.compile(
		".*\\.(\\w+)");

	static {
		String testCaseAvailablePropertyNames =
			PropsValues.TEST_CASE_AVAILABLE_PROPERTY_NAMES;

		if (Validator.isNotNull(testCaseAvailablePropertyNames)) {
			Collections.addAll(
				_testCaseAvailablePropertyNames,
				StringUtil.split(testCaseAvailablePropertyNames));
		}

		_testCaseAvailablePropertyNames.add("ignored");
		_testCaseAvailablePropertyNames.add("known-issues");
		_testCaseAvailablePropertyNames.add("priority");
		_testCaseAvailablePropertyNames.add("test.run.environment");

		String testCaseRequiredPropertyNames =
			PropsValues.TEST_CASE_REQUIRED_PROPERTY_NAMES;

		if (Validator.isNotNull(testCaseRequiredPropertyNames)) {
			Collections.addAll(
				_testCaseRequiredPropertyNames,
				StringUtil.split(testCaseRequiredPropertyNames));
		}
	}

}