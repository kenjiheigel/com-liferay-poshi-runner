# Poshi Script

Poshi Script is built off of the original Poshi XML syntax. It has been simplified and streamlined to provide a more readable and writable scripting syntax. For syntax highlighting in a text editor, it works best using syntax highlighting for javascript or groovy files.

The pieces of the syntax itself are divided into two main categories: blocks and statements. Blocks are snippets of code that can contain other blocks or statements, and statements are any kind of variable assignment, or invocation.

## Code Blocks

Code blocks begin with a block name or header followed by a `{` character, and end with a `}` character. Between the braces are the content of the code block.

Examples:
```javascript
if (isSet(variable)) { // block header
	AssertVisible.assertVisible(); // Block content
}

```
### `definition` block
All .macro and .testcase files must begin with a `definition` block.
```javascript
definition {
	...
}
```

### `setUp` and `tearDown` blocks
```javascript
setUp {
	...
}
```

```javascript
tearDown {
	...
}
```
### `macro`/`test` blocks
```javascript
macro macroName {
	...
}
```

```javascript
test macroName {
	...
}
```
### `for` blocks
### `if`, `else if`, and `else` blocks
### `while` blocks
```javascript
while (IsElementPresent(locator1 = "AssetCategorization#TAGS_REMOVE_ICON_GENERIC")) {
	Click.click(locator1 = "AssetCategorization#TAGS_REMOVE_ICON_GENERIC");
}
```
### `task` blocks
## Code Statements
### Invocations
#### Macro Invocations
#### Function Invocations
#### Class/Method Invocations
#### Utility Invocations
echo, fail, takeScreenshot
### Variable Assignments
There are two possible types of variable assignments, vars and properties.
#### `var` assignments
##### Basic Strings
```javascript
var userEmailAddress = "userea@liferay.com";
```

##### Multiline Strings
```groovy
var wikiPageContent = '''<p id='demo'>PASS</p>

<script type='text/javascript'>
	document.getElementById('demo').innerHTML = 'FAIL';
</script>''';
```

##### Assigning `var`'s to macro invocations
```javascript
var siteName = TestCase.getSiteName();
```

##### Assigning `var`'s to class/method invocations
```javascript
var breadcrumbNameUppercase = StringUtil.upperCase('${breadcrumbName}');
```
#### `property` assignments

## Conditionals

## Other
### Comments
### Annotations
