package org.palettelabs.iumls.parser;

public class NotationElement {

	protected int column;
	protected int line;
	protected int position;

	public int getPosition() {
		return this.position;
	}

	public int getLine() {
		return this.line;
	}

	public int getColumn() {
		return this.column;
	}

	protected String padString(String pattern, int count) {
		String s = "";
		for (int f = 0; f < count; f++) s += pattern;
		return s;
	}

}
