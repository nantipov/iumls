package org.palettelabs.iumls.computer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.palettelabs.iumls.IumlsException;
import org.palettelabs.iumls.VariableValue;
import org.palettelabs.iumls.parser.Expression;
import org.palettelabs.iumls.parser.ExpressionElement;
import org.palettelabs.iumls.parser.NotationElement;
import org.palettelabs.iumls.utils.NumberConverter;

public class Computer {

	private Map<String, Library> libraries = new HashMap<String, Library>();
	private Map<String, OperatorData> operators = new HashMap<String, Computer.OperatorData>();

	private static class OperatorData {
		int priority;
		String libraryName;
	}

	public Computer() {
		addLibrary("system", new SystemLibrary());
		addLibrary("math", new MathLibrary());

		registerOperator("==", "system", 1);
		registerOperator("!=", "system", 1);
		registerOperator("||", "system", 2);
		registerOperator("&&", "system", 3);
		registerOperator(">", "system", 4);
		registerOperator("<", "system", 4);
		registerOperator(">=", "system", 4);
		registerOperator("<=", "system", 4);

		registerOperator("+", "math", 5);
		registerOperator("-", "math", 5);
		registerOperator("*", "math", 6);
		registerOperator("/", "math", 6);

	}

	public void addLibrary(String name, Library library) {
		this.libraries.put(name, library);
	}

	public void registerOperator(String operatorString, String libraryName, int priority) {
		OperatorData data;
		if (this.operators.containsKey(operatorString)) {
			data = this.operators.get(operatorString);
		} else {
			data = new OperatorData();
			this.operators.put(operatorString, data);
		}
		data.libraryName = libraryName;
		data.priority = priority;
	}

	public VariableValue compute(Expression expression) throws IumlsException {
		return compute(expression.getBaseElement());
	}

	private VariableValue compute(ExpressionElement element) throws IumlsException {

		// translate to RPN (initially designed by Edsger Dijkstra)
		Stack<ExpressionElement> stack = new Stack<ExpressionElement>();
		LinkedList<ExpressionElement> output = new LinkedList<ExpressionElement>();
		for (ExpressionElement e : element.getElements()) {
			switch (e.getType()) {
				case ExpressionElement.NUMBER:
				case ExpressionElement.FUNCTION:
				case ExpressionElement.GENERIC_NODE:
				case ExpressionElement.IDENTIFIER:
				case ExpressionElement.STRING:
					output.addLast(e);
					break;
				case ExpressionElement.OPERATOR:
					while ((!stack.isEmpty()) && (stack.peek() != null) && (stack.peek().getType() == ExpressionElement.OPERATOR)
							&& (getOperatorPriority(e.getData()) <= getOperatorPriority(stack.peek().getData()))) {
						output.addLast(stack.pop());
					}
					stack.push(e);
					break;
				case ExpressionElement.SYMBOL:
					if (e.getData().equals("("))
						stack.push(e);
					else if (e.getData().equals(")")) {
						while ((stack.peek() != null) && !((stack.peek().getType() == ExpressionElement.SYMBOL) && stack.peek().getData().equals("("))) {
							output.addLast(stack.pop());
						}
						if ((stack.peek() != null) && (stack.peek().getType() == ExpressionElement.SYMBOL) && stack.peek().getData().equals("(")) {
							stack.pop();
						}
					} else {
						output.addLast(e);
					}
					break;
			}
		}
		while (!stack.isEmpty()) {
			output.addLast(stack.pop());
		}

		// compute RPN
		NotationElement currentNotationElement = null;
		String currentElementName = "";
		try {
			LinkedList<ExpressionElement> input = output;
			Stack<VariableValue> stackValues = new Stack<VariableValue>();
			for (ExpressionElement e : input) {
				currentNotationElement = e;
				switch (e.getType()) {
					case ExpressionElement.OPERATOR:
						currentElementName = "an operator '" + e.getData() + "'";
						VariableValue[] argsO = new VariableValue[2];
						argsO[1] = stackValues.pop();
						argsO[0] = stackValues.pop();
						stackValues.push(evaluateOperator(e.getData(), argsO));
						break;
					case ExpressionElement.NUMBER:
						currentElementName = "a number '" + e.getData() + "'";
						stackValues.push(new VariableValue(NumberConverter.toDouble(e.getData())));
						break;
					case ExpressionElement.STRING:
						currentElementName = "a string '" + e.getData() + "'";
						stackValues.push(new VariableValue(e.getData()));
						break;
					case ExpressionElement.IDENTIFIER:
						currentElementName = "an identifier '" + e.getData() + "'";
						String libraryNameI = "system";
						String valueI = e.getData();
						if (e.getData().indexOf(".") > -1) {
							libraryNameI = e.getData().substring(0, e.getData().indexOf("."));
							valueI = e.getData().substring(e.getData().indexOf(".") + 1);
						}
						stackValues.push(evaluateLibraryVariable(libraryNameI, valueI));
						break;
					case ExpressionElement.FUNCTION:
						currentElementName = "a function '" + e.getData() + "'";
						String libraryNameF = "system";
						String valueF = e.getData();
						if (e.getData().indexOf(".") > -1) {
							libraryNameF = e.getData().substring(0, e.getData().indexOf("."));
							valueF = e.getData().substring(e.getData().indexOf(".") + 1);
						}
						// check if all function arguments have a tree

						// function might has an empty argument, it's a known parser behavioral

						// go through an arguments list and remove empty ones
						// (new list implementation to obey parser immutable structure approach)
						List<ExpressionElement> __elements = new ArrayList<ExpressionElement>(e.getElements().size());
						for (ExpressionElement __e : e.getElements()) {
							if (!__e.getElements().isEmpty())
								__elements.add(__e);
						}

						VariableValue[] argsF = new VariableValue[__elements.size()];
						int t = 0;
						for (ExpressionElement argumentElement : __elements) {
							argsF[t] = compute(argumentElement);
							t++;
						}
						stackValues.push(executeLibraryMethod(libraryNameF, valueF, argsF));
						break;
					default:
						stack.push(e);
						break;
				}
			}
			return stackValues.pop();
		} catch (Exception exc) {
			throw new IumlsException("runtime exception on evaluating " + currentElementName, exc, currentNotationElement.getPosition(),
					currentNotationElement.getLine(), currentNotationElement.getColumn());
		}

	}

	private Library getLibrary(String name) throws IumlsException {
		if (libraries.containsKey(name)) {
			return libraries.get(name);
		} else {
			throw new IumlsException("IUMLS::Computer:: no library '" + name + "' found");
		}
	}

	private VariableValue executeLibraryMethod(String libraryName, String methodName, VariableValue[] arguments) throws IumlsException {
		Library lib = getLibrary(libraryName);
		Class<?> __class = lib.getClass();
		Method[] methods = __class.getMethods();

		//System.out.println(__class.getName() + ": " + methodName + ": " + libraryName);

		int f = 0;
		Method foundMethod = null;
		while ((foundMethod == null) && (f < methods.length)) {
			Method method = methods[f];
			Class<?>[] parameters = method.getParameterTypes();
			boolean isLastVararg = (parameters.length > 0)
					&& parameters[parameters.length - 1].isArray()
					&& isOneOf(parameters[parameters.length - 1].getName(), "[Z", "[Lorg.palettelabs.iumls.VariableValue;", "[Ljava.lang.Integer;",
							"[Ljava.lang.Boolean;", "[Ljava.lang.Double;", "[Ljava.lang.Long;", "[Ljava.lang.String;", "[D", "[I", "[J");

			String annotatedMethodName = "";

			if (method.isAnnotationPresent(org.palettelabs.iumls.computer.Method.class)) {
				org.palettelabs.iumls.computer.Method methodAnnotation = method
						.getAnnotation(org.palettelabs.iumls.computer.Method.class);
				annotatedMethodName = methodAnnotation.value();
			}

			if ((annotatedMethodName == null) || annotatedMethodName.isEmpty())
				annotatedMethodName = method.getName();

			if (method.isAnnotationPresent(org.palettelabs.iumls.computer.Method.class) && (annotatedMethodName.equals(methodName))
					&& ((parameters.length == arguments.length) || ((parameters.length < arguments.length) && isLastVararg))
					&& method.getReturnType().getName().equals("org.palettelabs.iumls.VariableValue")) {
				boolean matched = true;
				for (int n = 0; n < parameters.length; n++) {

					matched = isSupportedType(parameters[n]) || ((n == parameters.length - 1) && isLastVararg);

					if (!matched)
						break;
				}
				if (matched)
					foundMethod = method;
			}
			f++;
		}

		// if no method found check if default method handler exists
		boolean defaultMethod = false;
		if (foundMethod == null) {
			while ((foundMethod == null) && (f < methods.length)) {
				Method method = methods[f];
				if (
						method.isAnnotationPresent(DefaultMethodHandler.class) &&
						method.getReturnType().getName().equals("org.palettelabs.iumls.VariableValue") &&
						method.getParameterTypes().length == 2 &&
						method.getParameterTypes()[0].getName().equals("java.lang.String") &&
						method.getParameterTypes()[1].getName().equals("[Lorg.palettelabs.iumls.VariableValue")
						)
				{
					foundMethod = method;
					defaultMethod = true;
				}
				f++;
			}
		}

		// if neither regular method nor default method was found 
		if (foundMethod == null)
			throw new IumlsException("IUMLS::Computer: no method '" + methodName + "' found in library '" + libraryName + "'");

		try {
			Class<?>[] parameters = foundMethod.getParameterTypes();
			Object[] methodArguments = new Object[parameters.length];
			for (int n = 0; n < parameters.length; n++) {
				if (defaultMethod && (n == 0)) {
					methodArguments[n] = methodName;
				} else if (!parameters[n].isArray()) {
					// regular argument
					methodArguments[n] = fillWithValue(parameters[n], arguments[n]);

				} else {
					// array argument (it must be a last argument considering previous checks)
					int arraySize = arguments.length - parameters.length + 1;
					// create an array
					Object o = null;
					if (parameters[n].getName().equals("[I"))
						o = new int[arraySize];
					else if (parameters[n].getName().equals("[Ljava.lang.Integer;"))
						o = new Integer[arraySize];
					else if (parameters[n].getName().equals("[Z"))
						o = new boolean[arraySize];
					else if (parameters[n].getName().equals("[Ljava.lang.Boolean;"))
						o = new Boolean[arraySize];
					else if (parameters[n].getName().equals("[J"))
						o = new long[arraySize];
					else if (parameters[n].getName().equals("[Ljava.lang.Long;"))
						o = new Long[arraySize];
					else if (parameters[n].getName().equals("[D"))
						o = new double[arraySize];
					else if (parameters[n].getName().equals("[Ljava.lang.Double;"))
						o = new Double[arraySize];
					else if (parameters[n].getName().equals("[Ljava.lang.String;"))
						o = new String[arraySize];
					else if (parameters[n].getName().equals("[Lorg.palettelabs.iumls.VariableValue;"))
						o = new VariableValue[arraySize];
					methodArguments[n] = o;
					// fill it with values
					for (int j = 0; j < arraySize; j++) {
						int argumentIndex = n + j;
						if (parameters[n].getName().equals("[I"))
							((int[]) o)[j] = arguments[argumentIndex].asInteger();
						else if (parameters[n].getName().equals("[Ljava.lang.Integer;"))
							((Integer[]) o)[j] = new Integer(arguments[argumentIndex].asInteger());
						else if (parameters[n].getName().equals("[Z"))
							((boolean[]) o)[j] = arguments[argumentIndex].asBoolean();
						else if (parameters[n].getName().equals("[Ljava.lang.Boolean;"))
							((Boolean[]) o)[j] = new Boolean(arguments[argumentIndex].asBoolean());
						else if (parameters[n].getName().equals("[J"))
							((long[]) o)[j] = arguments[argumentIndex].asLong();
						else if (parameters[n].getName().equals("[Ljava.lang.Long;"))
							((Long[]) o)[j] = new Long(arguments[argumentIndex].asLong());
						else if (parameters[n].getName().equals("[D"))
							((double[]) o)[j] = arguments[argumentIndex].asDouble();
						else if (parameters[n].getName().equals("[Ljava.lang.Double;"))
							((Double[]) o)[j] = new Double(arguments[argumentIndex].asDouble());
						else if (parameters[n].getName().equals("[Ljava.lang.String;"))
							((String[]) o)[j] = arguments[argumentIndex].asString();
						else if (parameters[n].getName().equals("[Lorg.palettelabs.iumls.VariableValue;"))
							((VariableValue[]) o)[j] = arguments[argumentIndex];
					}
				}
			}
			Object obj = foundMethod.invoke(lib, methodArguments);
			return (VariableValue) obj;
		} catch (InvocationTargetException e0) {
			Throwable c = e0.getCause();
			if (c instanceof IumlsException)
				throw (IumlsException) c;
			else
				throw new IumlsException("IUMLS::Computer: couldn't invoke library method (library='" + libraryName + "', methodName='" + methodName
						+ "')", c);
		} catch (Exception e) {
			throw new IumlsException("IUMLS::Computer: couldn't invoke library method (library='" + libraryName + "', methodName='" + methodName
					+ "')", e);
		}
	}

	private VariableValue evaluateLibraryVariable(String libraryName, String variableName) throws IumlsException {
		Library lib = getLibrary(libraryName);
		Class<?> __class = lib.getClass();
		//Field[] fields = __class.getDeclaredFields();
		Field[] fields = __class.getFields();

		//System.out.println(__class.getName() + ": " + variableName + ": " + libraryName);

		Field field = null;

		boolean found = false;
		int t = 0;

		while (!found && (t < fields.length)) {

			field = fields[t];

			//System.out.println(__class.getName() + ": " + field.getName() + ": " + (field.isAnnotationPresent(Variable.class)));

			if (field.isAnnotationPresent(Variable.class)) {
				String annotatedVariableName = "";
				Variable variableAnnotation = field.getAnnotation(Variable.class);
				annotatedVariableName = variableAnnotation.value();

				if ((annotatedVariableName == null) || annotatedVariableName.isEmpty())
					annotatedVariableName = field.getName();

				found = (annotatedVariableName.equals(variableName)) && (field.getType().getName().equals(VariableValue.class.getName()));
			}

			t++;
		}

		// return value if it was founded
		if (found) {
			try {
				VariableValue v = (VariableValue) field.get(lib);
				v.setName(libraryName + "." + variableName);
				return v;
			} catch (Exception e) {
				Throwable c = e.getCause();
				if (c instanceof IumlsException)
					throw (IumlsException) c;
				else
					throw new IumlsException("IUMLS::Computer: couldn't extract library variable (library='" + libraryName + "', variableName='"
							+ variableName + "')", c);
			}
		}

		// look into methods
		Method[] methods = __class.getMethods();

		Method method = null;

		t = 0;
		while (!found && (t < methods.length)) {

			method = methods[t];

			if (method.isAnnotationPresent(Variable.class)) {
				String annotatedVariableName = "";
				Variable variableAnnotation = method.getAnnotation(Variable.class);
				annotatedVariableName = variableAnnotation.value();

				if ((annotatedVariableName == null) || annotatedVariableName.isEmpty())
					annotatedVariableName = method.getName();

				found = (annotatedVariableName.equals(variableName)) && (method.getReturnType() == VariableValue.class)
						&& (method.getParameterTypes().length == 0);
			}

			t++;
		}

		// return value if it was founded
		if (found) {
			try {

				Object obj = method.invoke(lib, new Object[0]);

				return (VariableValue) obj;
			} catch (Exception e) {
				Throwable c = e.getCause();
				if (c instanceof IumlsException)
					throw (IumlsException) c;
				else
					throw new IumlsException("IUMLS::Computer: couldn't extract library variable (library='" + libraryName + "', variableName='"
							+ variableName + "')", c);
			}
		}

		// check if library has a default variable handler
		t = 0;
		while (!found && (t < methods.length)) {

			method = methods[t];

			if (method.isAnnotationPresent(DefaultVariableHandler.class)) {

				found =	(method.getReturnType() == VariableValue.class) &&
						(method.getParameterTypes().length == 1) &&
						(method.getParameterTypes()[0].getName().equals("java.lang.String"));
			}

			t++;
		}

		// return value if it was founded
		if (found) {
			try {

				Object[] args = {variableName};
				Object obj = method.invoke(lib, args);

				return (VariableValue) obj;
			} catch (Exception e) {
				Throwable c = e.getCause();
				if (c instanceof IumlsException)
					throw (IumlsException) c;
				else
					throw new IumlsException("IUMLS::Computer: couldn't extract library variable (library='" + libraryName + "', variableName='" + variableName + "')", c);
			}
		}

		throw new IumlsException("IUMLS::Computer: no variable '" + variableName + "' found in library '" + libraryName + "'");

	}

	private VariableValue evaluateLibraryOperator(String libraryName, String operatorString, VariableValue[] args) throws IumlsException {

		Library lib = getLibrary(libraryName);
		Class<?> __class = lib.getClass();

		Method[] methods = __class.getMethods();
		Method method = null;

		boolean found = false;

		int t = 0;
		while (!found && (t < methods.length)) {

			method = methods[t];

			if (method.isAnnotationPresent(Operator.class)) {
				String annotatedOperatorName = "";
				Operator operatorAnnotation = method.getAnnotation(Operator.class);
				annotatedOperatorName = operatorAnnotation.value();

				found = (annotatedOperatorName.equals(operatorString)) && (method.getReturnType().getName().equals(VariableValue.class.getName()))
						&& (method.getParameterTypes().length == 2) && isSupportedType(method.getParameterTypes()[0])
						&& isSupportedType(method.getParameterTypes()[1]);
			}

			t++;
		}

		if (found) {
			try {
				Object[] argumentsArray = new Object[2];

				argumentsArray[0] = fillWithValue(method.getParameterTypes()[0], args[0]);
				argumentsArray[1] = fillWithValue(method.getParameterTypes()[1], args[1]);

				Object obj = method.invoke(lib, argumentsArray);

				return (VariableValue) obj;
			} catch (Exception e) {
				Throwable c = e.getCause();
				if (c instanceof IumlsException) throw (IumlsException) c;
				else
					throw new IumlsException("IUMLS::Computer: couldn't extract library variable (library='" + libraryName + "', operatorString='"
							+ operatorString + "')", c);
			}
		}

		throw new IumlsException("IUMLS::Computer: no operator '" + operatorString + "' found in library '" + libraryName + "'");

	}

	private VariableValue evaluateOperator(String operatorString, VariableValue[] args) throws IumlsException {
		OperatorData data = this.operators.get(operatorString);
		String libraryName = null;
		if (data != null)
			libraryName = data.libraryName;
		else
			libraryName = "system";
		return evaluateLibraryOperator(libraryName, operatorString, args);
	}

	public int getOperatorPriority(String operatorString) {
		if (this.operators.containsKey(operatorString)) {
			return this.operators.get(operatorString).priority;
		} else {
			return -1;
		}
	}

	private Object fillWithValue(Class<?> __class, VariableValue value) {

		Object obj = null;

		if (__class.getName().equals("int"))
			obj = value.asInteger();
		else if (__class.getName().equals("java.lang.Integer"))
			obj = new Integer(value.asInteger());
		else if (__class.getName().equals("boolean"))
			obj = value.asBoolean();
		else if (__class.getName().equals("java.lang.Boolean"))
			obj = new Boolean(value.asBoolean());
		else if (__class.getName().equals("long"))
			obj = value.asLong();
		else if (__class.getName().equals("java.lang.Long"))
			obj = new Long(value.asLong());
		else if (__class.getName().equals("double"))
			obj = value.asDouble();
		else if (__class.getName().equals("java.lang.Double"))
			obj = new Double(value.asDouble());
		else if (__class.getName().equals("java.lang.String"))
			obj = value.asString();
		else if (__class.getName().equals("org.palettelabs.iumls.VariableValue"))
			obj = value;

		return obj;
	}

	private boolean isSupportedType(Class<?> __class) {
		return isOneOf(__class.getName(), "int", "java.lang.Integer", "boolean", "java.lang.Boolean", "long", "java.lang.Long", "double", "java.lang.Double",
				"java.lang.String", "org.palettelabs.iumls.VariableValue");
	}

	private boolean isOneOf(String s, String... list) {
		for (int f = 0; f < list.length; f++) {
			if (list[f].equals(s))
				return true;
		}
		return false;
	}

}
