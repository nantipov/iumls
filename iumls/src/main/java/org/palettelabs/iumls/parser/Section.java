package org.palettelabs.iumls.parser;

import java.util.ArrayList;
import java.util.List;

public class Section extends Entity {

	protected String sectionName;

	protected List<Entity> entities = new ArrayList<Entity>();

	public boolean isEmpty() {
		return this.entities.isEmpty();
	}

	public int size() {
		return this.entities.size();
	}

	public Entity get(int index) {
		return this.entities.get(index);
	}

	/**
	 * @return the name
	 */
	public String getSectionName() {
		return sectionName;
	}

	/**
	 * @return the entities
	 */
	public List<Entity> getEntities() {
		return entities;
	}

	@Override
	public String toString(int level) {
		String s = super.toString(level) + ":: SECTION: '" + this.sectionName + "' {";
		for (Entity e: this.entities) {
			s += "\n" + e.toString(level + 1) + ",";
		}
		if (!this.entities.isEmpty()) {
			s = s.substring(0, s.length() - 1);
			s += "\n" + padString(" ", level);
		}
		s += "}";
		return s;
	}

	@Override
	public String toString() {
		return toString(0);
	}

}
