package org.palettelabs.iumls;

import junit.framework.Assert;

import org.junit.Test;
import org.palettelabs.iumls.parser.Parser;

public class ParserTest {

	@Test
	public void general_test() {
		String notationText =
				"#notation_root {\r\n" + 
				"	// check line comments\r\n" + 
				"	\r\n" + 
				"	\r\n" + 
				"	/* check block comments */\r\n" + 
				"	\r\n" + 
				"	\r\n" + 
				"	/*multi-\r\n" + 
				"	         line\r\n" + 
				"			       block\r\n" + 
				"				           comments*/\r\n" + 
				"\r\n" + 
				"	a = b;\r\n" + 
				"	a = #section {\r\n" + 
				"	   /*\r\n" + 
				"	      nested comment\r\n" + 
				"	   */\r\n" + 
				"	   \r\n" + 
				"	   array = [\"orange\", [\"nested_array_item1\", \"ok, apple\"]];\r\n" + 
				"	};\r\n" + 
				"	\r\n" + 
				"	c = f(x / 2 * f1(45.23242 + 128.0) * -1);\r\n" + 
				"}";
		genericParserTest(notationText);
	}

	@Test
	public void nested_arrays_test() {
		String notationText =
				"#notation_root {\r\n" + 
				"	array = [\r\n" + 
				"		[\r\n" + 
				"			60, 70, 80, [90]\r\n" + 
				"		],\r\n" + 
				"		50,\r\n" + 
				"		[100, [101, [102, [103]]]]\r\n" + 
				"	];\r\n" + 
				"}";
		genericParserTest(notationText);
	}

	private void genericParserTest(String notationText) {
		try {
			Parser parser = new Parser(notationText);
			parser.parse();
		} catch (IumlsException e) {
			e.printStackTrace();
			System.err.println(e.getParserErrorOutput());
			Assert.fail(e.getMessage());
		}
	}
	
}
