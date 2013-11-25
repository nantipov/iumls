package org.palettelabs.iumls.parser;

import java.util.ArrayList;
import java.util.List;


public class ExpressionElement extends NotationElement {

	public static final int GENERIC_NODE = -1;
	public static final int SYMBOL = 0;
	public static final int STRING = 1;
	public static final int NUMBER = 2;
	public static final int IDENTIFIER = 3;
	public static final int FUNCTION = 4;
	public static final int FUNCTION_ARGUMENT = 5;
	public static final int OPERATOR = 6;

	protected String data = "<void>";
	protected int type = 0;

	protected List<ExpressionElement> elements = new ArrayList<ExpressionElement>();

	protected ExpressionElement parentExpressionElement;
	protected Expression parentExpression;

	protected int parenthesisBalance = 0;

	protected ExpressionElement(Expression parentExpression) {
		this.parentExpression = parentExpression;
	}

	protected ExpressionElement extendElement(char c) {
		ExpressionElement e = new ExpressionElement(this.parentExpression);
		e.parentExpressionElement = this;
		e.data = String.valueOf(c);
		this.elements.add(e);
		return e;
	}

	protected ExpressionElement extendElement(String s) {
		ExpressionElement e = new ExpressionElement(this.parentExpression);
		e.parentExpressionElement = this;
		e.data = s;
		this.elements.add(e);
		return e;
	}

	public String getData() {
		return this.data;
	}

	public int getType() {
		return this.type;
	}

	public List<ExpressionElement> getElements() {
		return this.elements;
	}

	public String toString(int level) {
		String s = "(" + this.type + ")" + " '" + this.data + "'";
		if (!this.elements.isEmpty()) s += ":";
		for (ExpressionElement e: this.elements) {
			s += "\n" + padString(" ", level + 1) + e.toString(level + 1);
		}
		if (!this.elements.isEmpty()) {
			s += "\n" + padString(" ", level);
		}
		return s;
	}

	@Override
	public String toString() {
		return toString(0);
	}

}
