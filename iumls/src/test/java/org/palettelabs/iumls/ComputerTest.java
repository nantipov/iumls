package org.palettelabs.iumls;

import junit.framework.Assert;

import org.junit.Test;
import org.palettelabs.iumls.computer.Computer;
import org.palettelabs.iumls.parser.Expression;
import org.palettelabs.iumls.parser.Parser;

public class ComputerTest {

	@Test
	public void general_arithmetic1_test() throws IumlsException {
		// parse
		Parser p = new Parser("#root{ a = 1/2 + math.abs(-10.5);}");
		p.parse();
		// get expression
		Expression exp = (Expression) p.getRootSection().get(0);
		// compute
		Computer c = new Computer();
		VariableValue value = c.compute(exp);
		Assert.assertEquals(11, value.asInteger());
	}

}
