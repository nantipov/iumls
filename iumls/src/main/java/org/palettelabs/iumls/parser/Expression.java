package org.palettelabs.iumls.parser;


public class Expression extends Entity {

	protected ExpressionElement element = new ExpressionElement(this);

	protected Expression parentExpression;

	protected int parenthesisBalance = 0;

	public Expression getParentExpression() {
		return this.parentExpression;
	}

	public ExpressionElement getBaseElement() {
		return this.element;
	}

	@Override
	public String toString(int level) {
		return super.toString(level) + ":: EXPRESSION: (" + this.element.toString(level + 1) + ")";
	}

	@Override
	public String toString() {
		return toString(0);
	}

}
