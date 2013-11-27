package org.palettelabs.iumls.computer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Marks method/attribute as 'library' variable, use 'value' for specifying the name of variable
 * or leave it empty to apply method/attribute with its own name. Marked member must always return
 * {@link org.palettelabs.iumls.VariableValue}.
 * <br>
 * <b>Example 1</b>
 * <pre>
 * &#64;Variable
 * public VariableValue gravityConstant = new VariableValue(9.81);
 * </pre>
 * <br>
 * <b>Example 2</b>
 * <pre>
 * &#64;Variable("true")
 * public VariableValue __true = new VariableValue(true);
 * </pre>
 * <br>
 * <b>Example 3</b>
 * <pre>
 * &#64;Variable
 * public VariableValue currentTime() {
 *   return new VariableValue(new Date());
 * }
 * </pre>
 * <br>
 * <b>Example 4</b>
 * <pre>
 * &#64;Variable("A")
 * public VariableValue the_A_Resolver {
 *   return new VariableValue(); // returns null by default :-)
 * }
 * </pre>
 * 
 * 
 * @author Nikolay Antipov
 *
 */
@Target(value = {ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Variable {

	public String value() default "";

}
