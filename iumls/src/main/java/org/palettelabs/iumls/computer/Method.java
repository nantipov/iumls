package org.palettelabs.iumls.computer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Marks method as 'library' method, use 'value' for specifying the name of method
 * or leave it empty to apply method with its own name. Marked method must always return
 * {@link org.palettelabs.iumls.VariableValue}, input arguments must be:
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
 * &#64;Method
 * public VariableValue ImFeelingLucky(String query) {
 *   java.net.URL = new java.net.URL("google.com");
 *   String searchResult = "";
 *   // do all searching stuff here
 *   return new VariableValue(searchResult);
 * }
 * </pre>
 * <br>
 * <b>Example 2</b>
 * <pre>
 * &#64;Method("class")
 * public VariableValue __class() {
 *   return new VariableValue(getClass().getName());
 * }
 * </pre>
 * 
 * @author Nikolay Antipov
 *
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Method {

	public String value() default "";

}
