package org.palettelabs.iumls.computer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Marks method as 'library' operator, use 'value' for specifying the name of operator.
 * Marked method must always return
 * {@link org.palettelabs.iumls.VariableValue}, input arguments (always two arguments) must be:
 * <ul>
 * 	<li>int;</li>
 *  <li>long;</li>
 *  <li>double;</li>
 *  <li>Integer;</li>
 *  <li>Long;</li>
 *  <li>Double;</li>
 *  <li>String;</li>
 *  <li>org.palettelabs.iumls.VariableValue.</li>
 * </ul>
 * <br>
 * <b>Example 1</b>
 * <pre>
 * &#64;Operator("+")
 * public VariableValue plus(int a, int b) {
 *   return new VariableValue(a + b);
 * }
 * </pre>
 * 
 * @author Nikolay Antipov
 *
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Operator {

	public String value();

}
