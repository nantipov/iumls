package org.palettelabs.iumls.computer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks method as a trap for method name handling in case of
 * no actual method was found.
 * 
 * <br>
 * 
 * Target method must have two arguments:
 * <ul>
 * 	<li>notation method name as 'String';</li>
 *  <li>notation method arguments 'org.palettelabs.iumls.VariableValue[]'.</li>
 * </ul>
 * <br>
 * Return type is {@link org.palettelabs.iumls.VariableValue}.
 * <br>
 * <b>Example 1</b>
 * <pre>
 * &#64;DefaultMethodHandler
 * public VariableValue handleMethod(String methodName, VariableValue... values) {
 *   return new VariableValue("everything you want");
 * }
 * </pre>
 * 
 * @author Nikolay Antipov
 *
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultMethodHandler {

}
