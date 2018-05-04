package com.liferay.poshi.runner.prose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PoshiProseMacroMatchingString {

	public static PoshiProseMacroMatchingString
		getPoshiProseMacroMatchingString(String matchingString) {

		return poshiProseMacroMatchingStringMap.get(matchingString);
	}

	public static PoshiProseMacroMatchingString
		getPoshiProseMacroMatchingString(
			String matchingString, String macroNamespacedClassCommandName) {

		String key = _toString(matchingString);

		if (poshiProseMacroMatchingStringMap.containsKey(key)) {
			return poshiProseMacroMatchingStringMap.get(key);
		}

		poshiProseMacroMatchingStringMap.put(
			key,
			new PoshiProseMacroMatchingString(
				matchingString, macroNamespacedClassCommandName));

		return poshiProseMacroMatchingStringMap.get(key);
	}

	public String getMacroNamespacedClassCommandName() {
		return _macroNamespacedClassCommandName;
	}

	public String getMatchingString() {
		return _matchingString;
	}

	public List<String> getParameterNames() {
		List<String> parameterNames = new ArrayList<>();

		Matcher matcher = _matchingStringParameterPattern.matcher(
			_matchingString);

		while (matcher.find()) {
			parameterNames.add(matcher.group(1));
		}

		return parameterNames;
	}

	@Override
	public String toString() {
		return _toString(_matchingString);
	}

	protected static final Map<String, PoshiProseMacroMatchingString>
		poshiProseMacroMatchingStringMap = new HashMap<>();

	private static String _toString(String matchingString) {
		return matchingString.replaceAll("\\$\\{.+?\\}", "\"\"");
	}

	private PoshiProseMacroMatchingString(
		String matchingString, String macroNamespacedClassCommandName) {

		_macroNamespacedClassCommandName = macroNamespacedClassCommandName;
		_matchingString = matchingString;
	}

	private static final Pattern _matchingStringParameterPattern =
		Pattern.compile("\\$\\{(.+?)\\}");

	private final String _macroNamespacedClassCommandName;
	private final String _matchingString;

}