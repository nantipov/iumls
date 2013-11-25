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
 *  <li>notation method arguments 'VariableValue[]'.</li>
 * </ul>
 * <br>
 * Return type is 'VariableValue'.
 * 
 * @author nikolay.antipov
 *
 */
@Target(value = {ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultMethodHandler {

}
