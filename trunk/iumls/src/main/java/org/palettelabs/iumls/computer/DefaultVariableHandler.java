package org.palettelabs.iumls.computer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks method as a trap for method name handling in case of
 * no actual field/method was found.
 * 
 * <br>
 * 
 * Target method must have the only 'String' argument which is
 * a name of variable. Return type is 'VariableValue'.
 * 
 * @author nikolay.antipov
 *
 */
@Target(value = {ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultVariableHandler {

}
