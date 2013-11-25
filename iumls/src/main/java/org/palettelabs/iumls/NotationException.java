package org.palettelabs.iumls;

import org.palettelabs.iumls.parser.NotationElement;
import org.palettelabs.iumls.parser.Parser;


public class NotationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int position;
	private int line;
	private int column;

	private Parser parser;
	private NotationElement element;

	public NotationException(String errorText, int position, int line, int column) {
		super(errorText + "\n at line " + line + " column " + column);
		this.position = position;
		this.line = line;
		this.column = column;
	}

	public NotationException(Parser parser, String errorText, int position, int line, int column) {
		super(errorText + "\n at line " + line + " column " + column);
		this.parser = parser;
		this.position = position;
		this.line = line;
		this.column = column;
	}
	
	public NotationException(String errorText, Throwable e, int position, int line, int column) {
		super(errorText + "\n at line " + line + " column " + column, e);
		this.position = position;
		this.line = line;
		this.column = column;
	}

	public NotationException(String errorText, NotationElement element) {
		super(errorText + "\n at line " + element.getLine() + " column " + element.getColumn());
		this.position = element.getPosition();
		this.line = element.getLine();
		this.column = element.getColumn();
		this.element = element;
	}

	public NotationException(String errorText, Throwable e, NotationElement element) {
		super(errorText + "\n at line " + element.getLine() + " column " + element.getColumn(), e);
		this.position = element.getPosition();
		this.line = element.getLine();
		this.column = element.getColumn();
		this.element = element;
	}

	public int getPosition() {
		return this.position;
	}

	public int getLine() {
		return this.line;
	}

	public int getColumn() {
		return this.column;
	}
	
	public NotationElement getElement() {
		return this.element;
	}

	public String getParserErrorOutput() {
		if (this.parser != null) return this.parser.getErrorInNotation();
		else return "";
	}

}