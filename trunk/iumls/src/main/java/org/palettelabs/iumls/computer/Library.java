package org.palettelabs.iumls.computer;

/**
 * Describes regular library of methods.<br>
 * Each method has mandatory signature:<br>
 * <br>
 * public {@link} VariableValue <b>name</b>(<i>&lt;args&gt;</i>) throws
 * CalcuttaToolException,<br>
 * where <i>args</i> arguments, each argument could be either int, long, double,
 * String or {@link} VariableValue.<br>
 * <br>
 * Special methods for variables retrieving:<br>
 * public {@link} VariableValue <i>getVariable_<b>name</b></i>(<b>with no
 * arguments</b>) throws CalcuttaToolException.
 * 
 * @author nikolay.antipov
 * 
 */
public interface Library {


}
