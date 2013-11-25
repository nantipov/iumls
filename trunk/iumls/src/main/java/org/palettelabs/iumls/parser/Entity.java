package org.palettelabs.iumls.parser;


public class Entity extends NotationElement {

	protected String name = "<void>";
	protected Entity parentEntity;

	public String getName() {
		return this.name;
	}

	public Entity getParentEntity() {
		return this.parentEntity;
	}

	public String toString(int level) {
		return padString(" ", level) + "ENTITY '" + this.name + "'";
	}

	@Override
	public String toString() {
		return toString(0);
	}

}
