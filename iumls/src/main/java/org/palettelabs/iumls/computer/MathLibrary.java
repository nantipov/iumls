package org.palettelabs.iumls.computer;

import org.palettelabs.iumls.VariableValue;

public class MathLibrary implements Library {

	@Operator("+")
	public VariableValue operatorPlus(VariableValue v1, VariableValue v2) {
		VariableValue v = new VariableValue();
		if (
				(v1.getType() == VariableValue.TCP_TYPE_STRING) ||
				(v2.getType() == VariableValue.TCP_TYPE_STRING))
		{
			v.set(v1.asString() + v2.asString());
		} else {
			v.set(v1.asLong() + v2.asLong());
		}
		return v;
	}

	@Operator("-")
	public VariableValue operatorMinus(long v1, long v2) {
		VariableValue v = new VariableValue(v1 - v2);
		return v;
	}

	@Operator("*")
	public VariableValue operatorMultiply(long v1, long v2) {
		VariableValue v = new VariableValue(v1 * v2);
		return v;
	}

	@Operator("/")
	public VariableValue operatorDivide(long v1, long v2) {
		VariableValue v = new VariableValue(v1 / v2);
		return v;
	}

}
