package org.palettelabs.iumls.computer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks method as a trap for variable name handling in case of
 * no actual field/method was found.
 * 
 * <br>
 * 
 * Target method must have the only 'String' argument which is
 * a name of variable. Return type is {@link org.palettelabs.iumls.VariableValue}.
 * <br>
 * <b>Example 1</b>
 * <pre>
 * &#64;DefaultVariableHandler
 * public VariableValue handleVariable(String variableName) {
 *   return new VariableValue("everything you want");
 * }
 * </pre>
 * 
 * @author Nikolay Antipov
 *
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultVariableHandler {

}
