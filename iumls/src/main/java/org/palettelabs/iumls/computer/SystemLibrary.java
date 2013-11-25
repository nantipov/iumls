package org.palettelabs.iumls.computer;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.palettelabs.iumls.VariableValue;

public class SystemLibrary implements Library {

	@Variable("true")
	public VariableValue __true = new VariableValue(true);

	@Variable("false")
	public VariableValue __false = new VariableValue(false);

	@Variable("null")
	public VariableValue __null  = new VariableValue();

	@Variable
	public VariableValue namedVariable = new VariableValue("hey");

	private VariableValue sysdate = new VariableValue();

	@Method
	public VariableValue sysout(VariableValue v) {
		System.out.println(v.asString());
		return v;
	}

	@Method
	public VariableValue syserr(VariableValue v) {
		System.err.println(v.asString());
		return v;
	}

	@Operator("==")
	public VariableValue operatorEqual(VariableValue v1, VariableValue v2) {
		if (v1.asString().equals(v2.asString())) return __true;
		else return __false;
	}

	@Operator("!=")
	public VariableValue operatorNotEqual(VariableValue v1, VariableValue v2) {
		if (!v1.asString().equals(v2.asString())) return __true;
		else return __false;
	}

	@Operator(">")
	public VariableValue operatorGreater(VariableValue v1, VariableValue v2) {
		if ((v1.getType() == VariableValue.TCP_TYPE_STRING) || (v2.getType() == VariableValue.TCP_TYPE_STRING)) {
			return v1.asString().compareTo(v2.asString()) > 0 ? this.__true : this.__false;
		} else {
			return v1.asLong() > v2.asLong() ? this.__true : this.__false;
		}
	}

	@Operator("<")
	public VariableValue operatorLess(VariableValue v1, VariableValue v2) {
		if ((v1.getType() == VariableValue.TCP_TYPE_STRING) || (v2.getType() == VariableValue.TCP_TYPE_STRING)) {
			return v1.asString().compareTo(v2.asString()) < 0 ? this.__true : this.__false;
		} else {
			return v1.asLong() > v2.asLong() ? this.__true : this.__false;
		}
	}

	@Operator(">=")
	public VariableValue operatorGreaterOrEqual(VariableValue v1, VariableValue v2) {
		if ((v1.getType() == VariableValue.TCP_TYPE_STRING) || (v2.getType() == VariableValue.TCP_TYPE_STRING)) {
			return v1.asString().compareTo(v2.asString()) >= 0 ? this.__true : this.__false;
		} else {
			return v1.asLong() > v2.asLong() ? this.__true : this.__false;
		}
	}

	@Operator("<=")
	public VariableValue operatorLessOrEqual(VariableValue v1, VariableValue v2) {
		if ((v1.getType() == VariableValue.TCP_TYPE_STRING) || (v2.getType() == VariableValue.TCP_TYPE_STRING)) {
			return v1.asString().compareTo(v2.asString()) <= 0 ? this.__true : this.__false;
		} else {
			return v1.asLong() > v2.asLong() ? this.__true : this.__false;
		}
	}

	@Operator("&&")
	public VariableValue operatorAnd(boolean v1, boolean v2) {
		return v1 && v2 ? this.__true : this.__false;
	}	

	@Operator("||")
	public VariableValue operatorOr(boolean v1, boolean v2) {
		return v1 || v2 ? this.__true : this.__false;
	}

	@Variable
	public VariableValue sysdate() {
		this.sysdate.set(System.currentTimeMillis());
		return this.sysdate;
	}

	@Method
	public VariableValue toDate(String v, String f) throws ParseException {
		return new VariableValue(new SimpleDateFormat(f).parse(v));
	}

	@Method
	public VariableValue not(boolean v) {
		return !v ? this.__true : this.__false;
	}

}
