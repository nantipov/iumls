package org.palettelabs.iumls.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.palettelabs.iumls.IumlsException;

/**
 * Well, this a class that manages all grammar parsing stuff regarding to
 * IUMLS-language texts. What does it mean?<br>
 * <br>
 * Here we use the 'notation' word to point for language itself. Basically,
 * notation is very simple and consist of three base <i>parser entity</i> blocks: section,
 * array and expression.
 * <br>
 *  Syntax structures.<br>
 *  <br>
 *	<pre>
 *  <b>section</b> ::= &lt;section_name&gt; <b>{</b>
 *  			&lt;entity&gt; <b>=</b> &lt;expression&gt; | &lt;section&gt; | &lt;array&gt;<b>;</b> 
 *  	<b>}</b>
 *  <b>section_name</b> ::= #a-zA-Z0-9
 *  <b>entity</b> ::= a-zA-Z0-9
 *  <b>expression</b> ::= classical expressions stuff with identifiers, numbers and functions
 *  <b>array</b> ::= <b>[</b>&lt;expression&gt; | &lt;section&gt;[, &lt;expression&gt; | &lt;section&gt;...]<b>]</b>
 *  </pre>
 * The highest level of notation is always a 'Section', so therefore each notation
 * starts with '#section' name.
 * <br>
 * Furthermore, feel free to mention comment blocks:
 * <ul>
 * 	<li><b>line comment</b> - #any_notation_text { <b>&frasl;&frasl; comment here </b><i>LINE-BREAK</i>;</li>
 * 	<li><b>block comment</b> - #any_notation_text { <b>&frasl;* comment here *&frasl;</b> a = ["notation text continues"]; }.</li>
 * </ul>
 *
 * Notation looks like.
 * 
 * <pre>
 *	#shop {
 *		fruits = [
 * 		"orange",
 *			[
 *				"apple",
 *				"lemon"
 *			] // like a box
 *		];
 *		another_goods = [
 *			"item1";
 *		];
 *	}
 * </pre>
 *  
 * @author Nikolay Antipov
 *
 */
public class Parser {

	private String notationText;

	private String[] defaultOperators = {"+", "-", "*", "/", "==", "!=", ">", "<", ">=", "<=", "&&", "||"};
	
	private final String COMMENT_LINE_START = "//";
	private final String COMMENT_BLOCK_START = "/*";
	private final String COMMENT_BLOCK_END = "*/";

	private Set<String> operators = new HashSet<String>();

	protected Section rootSection = new Section();

	protected String errorText = "";
	protected int errorColumn = 0;
	protected int errorLine = 0;

	/**
	 * Constructs parser with notation text.
	 * @param notationText - text to parse
	 */
	public Parser(String notationText) {
		this.notationText = notationText;
		buildOperatorsSet(defaultOperators);
	}

	/**
	 * Constructs parser with notation text and list of operators.
	 * @param notationText - text to parse
	 * @param operators - list of operators
	 */
	public Parser(String notationText, String ...operators) {
		this.notationText = notationText;
		buildOperatorsSet(operators);
	}

	private void buildOperatorsSet(String[] operators) {
		this.operators.addAll(Arrays.asList(operators));
	}

	/**
	 * Parsers specified notation.
	 * @throws IumlsException throws in case of any grammar error
	 */
	public void parse() throws IumlsException {

		final int STATE_START = 0;

		final int STATE_SECTION = 1;
		final int STATE_SECTION_NAME = 2;
		final int STATE_SECTION_OPEN = 3;
		final int STATE_SECTION_CLOSE = 4;
		final int STATE_ENTITY_NAME = 5;
		final int STATE_ENTITY_ASSIGMENT = 6;
		final int STATE_ARRAY_OPEN = 7;
		final int STATE_ARRAY_COMMA = 8;
		final int STATE_ARRAY_CLOSE = 9;
		final int STATE_PARENTHESIS_OPEN = 10;
		final int STATE_PARENTHESIS_CLOSE = 11;
		final int STATE_STRING = 12;
		final int STATE_STRING_ESCAPE = 13;
		final int STATE_STRING_END = 14;
		final int STATE_UNARY_MINUS = 15;
		final int STATE_NUMBER = 16;
		final int STATE_NUMBER_FLOAT = 17;
		final int STATE_OPERATOR = 18;
		final int STATE_IDENTIFIER = 19;
		final int STATE_FUNCTION_OPEN = 20;
		final int STATE_FUNCTION_ARGUMENT_COMMA = 21;
		final int STATE_STATEMENT_END = 22;

		final int STATE_ERROR = -1;
		final int STATE_FINAL = 30;

		int state = STATE_START;

		boolean isWhitespaceActivated = false;
		boolean isWhitespaceSkipNeeded = true;

		boolean isWhitespaceOccuredFlag = false;

		//int parenthesisBalance = 0;
		int arrayBracketBalance = 0;
		int sectionCurveBalance = 0;
		//int functionParenthesisBalance = 0;

		int position = 0;
		int column = 0;
		int line = 1;

		String data = this.notationText;

		data = cutOffComments(data);

		if (!data.endsWith(".")) data += ".";

		data = data.replaceAll("\r\n", " \n");
		data = data.replaceAll("\r", "\n");

		Section currentSection = this.rootSection; // root section
		Entity currentEntity = currentSection;
		Expression currentExpression = null;
		Array currentArray = null;

		ExpressionElement currentExpressionElement = null;

		String stringStack = "";

		char c = ' ';

		while ((position < data.length()) && (state != STATE_ERROR)) {

			c = data.charAt(position);
			column++;

			if (c == '\n') {
				column = 0; // in order not to take in account a '\n' character
				line++;
			}

			// whitespace skip

			if (isWhitespaceSkipNeeded && !isWhitespaceActivated && isWhitespace(c)) {
				// enter whitespace state
				isWhitespaceOccuredFlag = true;
				isWhitespaceActivated = true;
				position++;
				continue;
			} else if (isWhitespaceActivated && isWhitespace(c)) {
				// follow whitespace skip logic
				position++;
				continue;
			} else if (isWhitespaceActivated && !isWhitespace(c)) {
				// leave whitespace state
				isWhitespaceActivated = false;
			} else {
				// reset whitespace flag
				isWhitespaceOccuredFlag = false;
			}

			if (state == STATE_START) {

				if (c == '#') {
					state = STATE_SECTION;
				} else {
					state = STATE_ERROR;
				}

			} else if (state == STATE_SECTION) {

				if (isIdentifierStarted(c)) {
					stringStack = "";
					stringStack += c;
					state = STATE_SECTION_NAME;
				} else {
					state = STATE_ERROR;
				}

			} else if (state == STATE_SECTION_NAME) {

				if (!isWhitespaceOccuredFlag && isIdentifierContinued(c)) {
					stringStack += c;
				} else if (c == '{') {
					sectionCurveBalance++;

					currentSection.sectionName = stringStack;

					state = STATE_SECTION_OPEN;
				} else {
					state = STATE_ERROR;
				}

			} else if (state == STATE_SECTION_OPEN) {

				if ((c == '}') && (sectionCurveBalance > 0)) {
					sectionCurveBalance--;
					currentEntity = currentSection.parentEntity;
					if (currentEntity instanceof Expression) currentExpression = (Expression) currentEntity;
					else if (currentEntity instanceof Section) currentSection = (Section) currentEntity;
					else if (currentEntity instanceof Array) currentArray = (Array) currentEntity;
					state = STATE_SECTION_CLOSE;
				} else if (isIdentifierStarted(c)) {
					stringStack = "";
					stringStack += c;
					state = STATE_ENTITY_NAME;
				} else {
					state = STATE_ERROR;
				}

			} else if (state == STATE_ENTITY_NAME) {

				if (!isWhitespaceOccuredFlag && isIdentifierContinued(c)) {
					stringStack += c;
				} else if (c == '=') {
					state = STATE_ENTITY_ASSIGMENT;
				} else {
					state = STATE_ERROR;
				}

			} else if ((state == STATE_OPERATOR) && isOperatorContinued(c, stringStack)) {

				stringStack += c;
				state = STATE_OPERATOR;

			}
			// operator starting logic
			else if (
					isOneOf(state, STATE_IDENTIFIER, STATE_STRING_END, STATE_NUMBER, STATE_NUMBER_FLOAT, STATE_PARENTHESIS_CLOSE) &&
					isOperatorStarted(c))
			{

				// string has its own finalization routine, that's why STATE_STRING_END is considered
				// as excluding case
				if ((state != STATE_PARENTHESIS_CLOSE) && (state != STATE_STRING_END)) {
					ExpressionElement e = currentExpressionElement.extendElement(stringStack);

					switch (state) {
					case STATE_IDENTIFIER: e.type = ExpressionElement.IDENTIFIER; break;
					//case STATE_STRING_END: e.type = ExpressionElement.STRING; break;
					case STATE_NUMBER:
					case STATE_NUMBER_FLOAT: e.type = ExpressionElement.NUMBER; break;
					default: state = STATE_ERROR; break;
					}
				}

				stringStack = "";
				stringStack += c;

				state = STATE_OPERATOR;

			}
			// typical entity (after entity name)/expression (after parenthesis and operator) beginning
			else if (
					(isOneOf(state, STATE_ENTITY_ASSIGMENT, STATE_ARRAY_OPEN, STATE_ARRAY_COMMA) && isEntityStarted(c)) ||
					(isOneOf(state, STATE_PARENTHESIS_OPEN, STATE_OPERATOR, STATE_FUNCTION_ARGUMENT_COMMA, STATE_FUNCTION_OPEN) && isExpressionStarted(c))
					)
			{

				boolean isEntityBeginning = isOneOf(state, STATE_ENTITY_ASSIGMENT, STATE_ARRAY_OPEN, STATE_ARRAY_COMMA) && isEntityStarted(c);
				String entityName = stringStack;

				if ((c == '[') && isEntityBeginning) {
					// array

					arrayBracketBalance++;
					currentArray = new Array();
					currentArray.parentEntity = currentEntity;

					currentEntity = currentArray;

					state = STATE_ARRAY_OPEN;

				} else if ((c == '#') && isEntityBeginning) {
					// section

					currentSection = new Section();
					currentSection.parentEntity = currentEntity;

					currentEntity = currentSection;

					state = STATE_SECTION;

				} else {
					// expression

					if (state == STATE_OPERATOR) {
						ExpressionElement e = currentExpressionElement.extendElement(stringStack);
						e.type = ExpressionElement.OPERATOR;
					} else if (!(currentEntity instanceof Expression)) {

						currentExpression = new Expression();
						currentExpression.parentEntity = currentEntity;

						currentEntity = currentExpression;
						currentExpressionElement = currentExpression.element;

					}

					if (state == STATE_FUNCTION_ARGUMENT_COMMA) {
						ExpressionElement e = currentExpressionElement.parentExpressionElement;

						e = e.extendElement("ARGUMENT");
						e.type = ExpressionElement.FUNCTION_ARGUMENT;

						currentExpressionElement = e;
					}

					if (c == '(') {
						//parenthesisBalance++;

						currentExpressionElement.parenthesisBalance++;
						currentExpression.parenthesisBalance++;

						ExpressionElement e = currentExpressionElement.extendElement('(');
						e.type = ExpressionElement.SYMBOL;

						state = STATE_PARENTHESIS_OPEN;
					} else if (c == '"') {
						stringStack = "";
						isWhitespaceSkipNeeded = false;
						state = STATE_STRING;
					} else if (c == '-') {
						stringStack = "";
						stringStack += c;
						state = STATE_UNARY_MINUS;
					} else if (isNumber(c)) {
						stringStack = "";
						stringStack += c;
						state = STATE_NUMBER;
					} else if (isIdentifierStarted(c)) {
						stringStack = "";
						stringStack += c;
						state = STATE_IDENTIFIER;
					} else {
						state = STATE_ERROR;
					}

				}

				if (isEntityBeginning) {
					if (currentEntity.parentEntity != null) {
						if (currentEntity.parentEntity instanceof Section) {
							currentEntity.name = entityName;
							((Section) currentEntity.parentEntity).entities.add(currentEntity);
						} else if (currentEntity.parentEntity instanceof Array) {
							((Array) currentEntity.parentEntity).entities.add(currentEntity);
						}
					}
				}

			}
			else if (state == STATE_ARRAY_OPEN) {

				if ((c == ']') && (arrayBracketBalance > 0)) {

					arrayBracketBalance--;
					currentEntity = currentEntity.parentEntity;

					if (currentEntity instanceof Array) {
						currentArray = (Array) currentEntity;
					} else currentArray = null;

					state = STATE_ARRAY_CLOSE;
				} else {
					state = STATE_ERROR;
				}

			} else if (state == STATE_PARENTHESIS_OPEN) {

				if (c == '(') {
					//parenthesisBalance++;

					currentExpressionElement.parenthesisBalance++;
					currentExpression.parenthesisBalance++;

					ExpressionElement e = currentExpressionElement.extendElement('(');
					e.type = ExpressionElement.SYMBOL;

					state = STATE_PARENTHESIS_OPEN;
				} else {
					state = STATE_ERROR;
				}

			} else if (state == STATE_STRING) {

				if (c == '\\') {
					state = STATE_STRING_ESCAPE;
				} else if (c == '"') {
					ExpressionElement e = currentExpressionElement.extendElement(stringStack);
					e.type = ExpressionElement.STRING;

					currentEntity = e.parentExpression/*.parentEntity*/;

					isWhitespaceSkipNeeded = true;
					state = STATE_STRING_END;
				} else {
					stringStack += c;
				}

			} else if (state == STATE_STRING_ESCAPE) {

				switch (c) {
				case '"':
					stringStack += "\"";
					state = STATE_STRING;
					break;
				case '\\':
					stringStack += "\\";
					state = STATE_STRING;
					break;
				case 't':
					stringStack += "\t";
					state = STATE_STRING;
					break;
				default:
					state = STATE_ERROR;
					break;
				}

			} else if (state == STATE_STATEMENT_END) {

				if (isIdentifierStarted(c)) {
					stringStack = "";
					stringStack += c;
					state = STATE_ENTITY_NAME;
				} else if ((c == '}') && (sectionCurveBalance > 0)) {
					sectionCurveBalance--;
					currentEntity = currentSection.parentEntity;
					if (currentEntity instanceof Expression) currentExpression = (Expression) currentEntity;
					else if (currentEntity instanceof Section) currentSection = (Section) currentEntity;
					else if (currentEntity instanceof Array) currentArray = (Array) currentEntity;
					state = STATE_SECTION_CLOSE;
				} else {
					state = STATE_ERROR;
				}

			} else if (state == STATE_UNARY_MINUS) {

				if (isNumber(c)) {
					stringStack = "-";
					stringStack += c;
					state = STATE_NUMBER;
				} else {
					state = STATE_ERROR;
				}

			}

			// typical entity ending logic
			else if (
					isOneOf(state, STATE_STRING_END, STATE_SECTION_CLOSE, STATE_ARRAY_CLOSE, STATE_NUMBER, STATE_NUMBER_FLOAT, STATE_IDENTIFIER, STATE_PARENTHESIS_CLOSE, STATE_FUNCTION_OPEN) &&
					isOneOf(c, ',', ';', ']', ')')
					)
			{

				// expression-related states (excluding string) do not have duplicate ending states,
				// therefore, we have to finalize all expression stuff
				
				// however, we must handle 'move up' logic for STATE_STRING_END as well
				if (isOneOf(state, STATE_NUMBER, STATE_NUMBER_FLOAT, STATE_IDENTIFIER, STATE_PARENTHESIS_CLOSE, STATE_STRING_END)) {


					// state 'STATE_PARENTHESIS_CLOSE' might be an expression ending,
					// but ')' symbol is handled by another condition
					if ((state != STATE_PARENTHESIS_CLOSE) && (state != STATE_STRING_END)) {
						ExpressionElement e = currentExpressionElement.extendElement(stringStack);
						switch (state) {
						case STATE_NUMBER:
						case STATE_NUMBER_FLOAT: e.type = ExpressionElement.NUMBER; break;
						case STATE_IDENTIFIER: e.type = ExpressionElement.IDENTIFIER; break;
						}
					}

					// move up if expression really ends
					if (
							(currentExpression.parenthesisBalance == 0) &&
							(
								isOneOf(c, ';', ']') ||
								(((currentEntity.parentEntity != null) && (currentEntity.parentEntity instanceof Array) && (c == ',')))
							)
							)
					{
						currentEntity = currentExpression.parentEntity;
						if (currentEntity instanceof Expression) currentExpression = (Expression) currentEntity;
						else if (currentEntity instanceof Section) currentSection = (Section) currentEntity;
						else if (currentEntity instanceof Array) currentArray = (Array) currentEntity;
					}

				}

				if ((c == ';') && (currentEntity instanceof Section)) {
					state = STATE_STATEMENT_END;
				} else if ((c == ',') && (currentEntity instanceof Array)) {
					state = STATE_ARRAY_COMMA;
				} else if ((c == ',') && (currentEntity instanceof Expression) && (currentExpressionElement.type == ExpressionElement.FUNCTION_ARGUMENT)) {
					state = STATE_FUNCTION_ARGUMENT_COMMA;
				} else if ((c == ')') && (currentEntity instanceof Expression) && (currentExpressionElement.parenthesisBalance > 0)) {

					// arithmetic parenthesis
					
					ExpressionElement e = currentExpressionElement.extendElement(')');
					e.type = ExpressionElement.SYMBOL;

					currentExpressionElement.parenthesisBalance--;
					currentExpression.parenthesisBalance--;

					state = STATE_PARENTHESIS_CLOSE;
				} else if ((c == ')') && (currentEntity instanceof Expression) &&
						((currentExpressionElement.parenthesisBalance == 0) && (currentExpression.parenthesisBalance > 0)) &&
						(currentExpressionElement.type == ExpressionElement.FUNCTION_ARGUMENT)
						)
				{

					// function parenthesis

					// move up to function (F) and then to base expression (E)
					//  E
					//  |
					//  a b c F e f g
					//        |
					//        + ARGUMENT
					//        | |
					//        | a + b
					//        |
					//        + ARGUMENT
					//          |
					//          d + e
					ExpressionElement e = currentExpressionElement.parentExpressionElement.parentExpressionElement;
					//                                              ^                       ^
					//                                              |                       |
					//                       jump to function F  ---+                       |
					//                                        jump to base expression E  ---+
					//                                                         

					currentExpressionElement = e;

					currentExpressionElement.parenthesisBalance--;
					currentExpression.parenthesisBalance--;

					state = STATE_PARENTHESIS_CLOSE;
				} else if ((c == ']') && (arrayBracketBalance > 0) && (currentEntity instanceof Array)) {
					arrayBracketBalance--;
					currentEntity = currentArray.parentEntity;
					if (currentEntity instanceof Expression) currentExpression = (Expression) currentEntity;
					else if (currentEntity instanceof Section) currentSection = (Section) currentEntity;
					else if (currentEntity instanceof Array) currentArray = (Array) currentEntity;
					state = STATE_ARRAY_CLOSE;
				} else {
					state = STATE_ERROR;
				}

			}

			else if (state == STATE_SECTION_CLOSE) {

				if ((c == '.') && (sectionCurveBalance == 0)) {
					state = STATE_FINAL;
				} else {
					state = STATE_ERROR;
				}

			} else if (state == STATE_STRING_END) {

				state = STATE_ERROR;

			} else if (state == STATE_NUMBER) {

				if (isNumber(c)) {
					stringStack += c;
					state = STATE_NUMBER;
				} else if (c == '.') {
					stringStack += c;
					state = STATE_NUMBER_FLOAT;
				} else {
					state = STATE_ERROR;
				}

			} else if (state == STATE_NUMBER_FLOAT) {

				if (isNumber(c)) {
					stringStack += c;
					state = STATE_NUMBER_FLOAT;
				} else {
					state = STATE_ERROR;
				}

			} else if (state == STATE_IDENTIFIER) {

				if (isIdentifierContinued(c)) {
					stringStack += c;
					state = STATE_IDENTIFIER;
				} else if (c == '(') {

					currentExpressionElement.parenthesisBalance++;
					currentExpression.parenthesisBalance++;

					ExpressionElement e = currentExpressionElement.extendElement(stringStack);
					e.type = ExpressionElement.FUNCTION;

					e = e.extendElement("ARGUMENT");
					e.type = ExpressionElement.FUNCTION_ARGUMENT;

					currentExpressionElement = e;

					//functionParenthesisBalance++;
					state = STATE_FUNCTION_OPEN;
				} else if (state == STATE_PARENTHESIS_CLOSE) {
					
				} else {
					state = STATE_ERROR;
				}

			} else {
				state = STATE_ERROR;
			}

			position++;
		}

		//System.out.println("state = " + state + ", line = " + line + ", column = " + column);

		if (state != STATE_FINAL) {
			this.errorLine = line;
			this.errorColumn = column;
			if (state == STATE_ERROR) {
				if ((errorText == null) || errorText.isEmpty()) this.errorText = "syntax error, unexpected character '" + c + "'";
			}
			else this.errorText = "unexpected end of script";

			throw new IumlsException(this, this.errorText, position, line, column);

		}

		

	}

	//============= typical checks =============
	
	private boolean isIdentifierStarted(char c) {
		return ((c >= 'a') && (c <= 'z')) ||
				((c >= 'A') && (c <= 'Z')) ||
				(c == '_');
	}

	private boolean isIdentifierContinued(char c) {
		return isIdentifierStarted(c) ||
				((c >= '0') && (c <= '9')) ||
				isOneOf(c, '.', '$');
	}

	private boolean isWhitespace(char c) {
		return ((c == ' ') || (c == '\t') || (c == '\n') || (c == '\r'));
	}

	private boolean isOneOf(int state, int ...list) {
		for (int f = 0; f < list.length; f++) {
			if (list[f] == state) return true;
		}
		return false;
	}

	private boolean isOneOf(char c, char ...list) {
		for (int f = 0; f < list.length; f++) {
			if (list[f] == c) return true;
		}
		return false;
	}

	private boolean isEntityStarted(char c) {
		return 
				isOneOf(c, '[', '#') ||
				isExpressionStarted(c);
	}

	private boolean isExpressionStarted(char c) {
		return 
				isIdentifierStarted(c) ||
				isNumber(c) ||
				isOneOf(c, '-', '(', '"');
	}

	private boolean isNumber(char c) {
		return ((c >= '0' && c <= '9'));
	}

	private boolean isOperatorStarted(char c) {
		Iterator<String> o = this.operators.iterator();
		boolean found = false;
		while (!found && o.hasNext()) {
			found = (o.next().charAt(0) == c);
		}
		return found;
	}

	private boolean isOperatorContinued(char c, String stack) {
		String __stack = stack + c;
		Iterator<String> o = this.operators.iterator();
		boolean found = false;
		while (!found && o.hasNext()) {
			String s = o.next();
			found = (s.substring(0, Math.min(s.length(), __stack.length())).equals(__stack));
		}
		return found;
	}

	private String cutOffComments(String text) {

		final int STATE_START = 0;
		final int STATE_REGULAR = 1;
		final int STATE_IN_LINE_COMMENT = 2;
		final int STATE_IN_BLOCK_COMMENT = 3;
		final int STATE_IN_STRING = 4;
		final int STATE_IN_STRING_ESCAPE = 5;

		String outputString = "";

		String buffer = "";
		
		int pos = 0;
		int state = STATE_START;

		while (pos < text.length()) {
			char c = text.charAt(pos);
			if ((state == STATE_START) || (state == STATE_REGULAR)) {
				if (c == '"') {
					state = STATE_IN_STRING;
					outputString += c;
				} else {
					switch (isCommentStarted(buffer, c)) {
					case 1: buffer += c; break;
					case 2: state = STATE_IN_LINE_COMMENT; outputString += flushBufferInComment(buffer + c); buffer = ""; break;
					case 3: state = STATE_IN_BLOCK_COMMENT; outputString += flushBufferInComment(buffer + c); buffer = ""; break;
					default: outputString += buffer + c; buffer = "";
					}
				}
			} else if (state == STATE_IN_LINE_COMMENT) {
				if (isCommentLineEnded(c)) {
					state = STATE_REGULAR;
				}
				outputString += flushBufferInComment(String.valueOf(c));
			} else if (state == STATE_IN_BLOCK_COMMENT) {
				switch (isCommentBlockEnded(buffer, c)) {
				case 1: buffer += c; break;
				case 2: state = STATE_REGULAR; outputString += flushBufferInComment(buffer + c); buffer = ""; break;
				default: outputString += flushBufferInComment(buffer + c); buffer = "";
				}
			} else if (state == STATE_IN_STRING) {
				if (c == '/') {
					state = STATE_IN_STRING_ESCAPE;
				} else if (c == '"') {
					state = STATE_REGULAR;
				}
				outputString += c;
			} else if (state == STATE_IN_STRING_ESCAPE) {
				state = STATE_IN_STRING;
				outputString += c;
			}

			pos++;
		}

		//System.out.println(outputString);
		return outputString;
	}

	private String flushBufferInComment(String buffer) {
		String s = "";
		for (int f = 0; f < buffer.length(); f++) {
			char c = buffer.charAt(f);
			switch (c) {
			case '\n':
			case '\r': s += c; break;
			default: s += ' ';
			}
		}
		return s;
	}

	private int isCommentStarted(String buffer, char c) {
		// 0 - not
		// 1 - started/continued
		// 2 - finished, line
		// 3 - finished, block
		if ((buffer.length() + 1 == COMMENT_LINE_START.length()) && ((buffer + c).equals(COMMENT_LINE_START))) return 2;
		else if ((buffer.length() + 1 == COMMENT_LINE_START.length()) && ((buffer + c).equals(COMMENT_BLOCK_START))) return 3;
		else if (
				(((buffer.length() + 1) < COMMENT_LINE_START.length()) && (buffer + c).equals(COMMENT_LINE_START.substring(0, buffer.length() + 1))) ||
				(((buffer.length() + 1) < COMMENT_BLOCK_START.length()) && (buffer + c).equals(COMMENT_BLOCK_START.substring(0, buffer.length() + 1)))
				)
		{
			return 1;
		} else {
			return 0;
		}
	}

	private boolean isCommentLineEnded(char c) {
		return isOneOf(c, '\n', '\r');
	}

	private int isCommentBlockEnded(String buffer, char c) {
		// 0 - not
		// 1 - continued
		// 2 - finished
		if ((buffer.length() + 1 == COMMENT_BLOCK_END.length()) && ((buffer + c).equals(COMMENT_BLOCK_END))) return 2;
		else if (
				((buffer.length() + 1) < COMMENT_BLOCK_END.length()) && (buffer + c).equals(COMMENT_BLOCK_END.substring(0, buffer.length() + 1))
				)
		{
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * Returns parser tree as its root element.
	 * @return root of tree;
	 */
	public Section getRootSection() {
		return this.rootSection;
	}

	@Override
	public String toString() {
		return this.rootSection.toString();
	}

	/**
	 * Return string representation of grammar error with line numbers.
	 * @return text representation of grammar error
	 */
	public String getErrorInNotation() {
		String text = this.notationText;
		text = text.replaceAll("\r", " ");
		text = text.replaceAll("\t", " ");
		String[] lines = text.split("\n");
		String s = "";
		if ((this.errorText != null) && !this.errorText.isEmpty()) {
			s += "PARSER ERROR:\n";
			s += "\t'" + this.errorText + "'\n";
			s += "\tat line " + this.errorLine + " and column " + this.errorColumn + ".\n";
		}
		for (int line = 1; line <= lines.length; line++) {
			s += String.format("%1$3d: ", line) + lines[line - 1] + "\n";
			if ((this.errorText != null) && !this.errorText.isEmpty() && (line == this.errorLine)) {
				for (int c = 0; c < this.errorColumn + 5 - 1; c++) {
					if (c <= 4) s += " ";
					else s += "-";
				}
				s += "^\n";
			}
		}
		return s;		
	}

}
