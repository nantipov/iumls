package org.palettelabs.iumls;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class VariableValue implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6771307486677657084L;
	public static final int TCP_TYPE_INTEGER = 0;
	public static final int TCP_TYPE_LONG = 1;
	public static final int TCP_TYPE_DOUBLE = 2;
	public static final int TCP_TYPE_STRING = 3;
	public static final int TCP_TYPE_DATE = 4;
	public static final int TCP_TYPE_BOOLEAN = 5;
	public static final int TCP_TYPE_BUFFER = 6;

	private String name = "dummy";
	private int type = 0;
	private int intValue = 0;
	private long longValue = 0;
	private double doubleValue = 0.0;
	private String stringValue = null;
	private Date dateValue = null;
	private boolean booleanValue = false;
	private byte[] bufferValue = null;
	private String typeName = "dummy";
	private boolean isNull = true;

	// for internal purposes (refer to LightItem)
	protected int internalFieldPosition = 0;

	public VariableValue() {
	}

	public VariableValue(int value) {
		set(value);
	}

	public VariableValue(long value) {
		set(value);
	}

	public VariableValue(double value) {
		set(value);
	}

	public VariableValue(String value) {
		set(value);
	}

	public VariableValue(Date value) {
		set(value);
	}

	public VariableValue(boolean value) {
		set(value);
	}

	public VariableValue(byte[] value) {
		set(value);
	}

	public VariableValue(VariableValue value) {
		set(value, true);
	}

	public int getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void set(int value) {
		type = TCP_TYPE_INTEGER;
		intValue = value;
		isNull = false;
	}

	public void set(long value) {
		type = TCP_TYPE_LONG;
		longValue = value;
		isNull = false;
	}

	public void set(double value) {
		type = TCP_TYPE_DOUBLE;
		doubleValue = value;
		isNull = false;
	}

	public void set(String value) {
		type = TCP_TYPE_STRING;
		stringValue = value;
		isNull = (value == null);
	}

	public void set(Date value) {
		type = TCP_TYPE_DATE;
		dateValue = value;
		isNull = (value == null);
	}

	public void set(boolean value) {
		type = TCP_TYPE_BOOLEAN;
		booleanValue = value;
		isNull = false;
	}

	public void set(byte[] value) {
		type = TCP_TYPE_BUFFER;
		bufferValue = value;
		isNull = (value == null);
	}

	public void set(VariableValue value) {
		set(value, true);
	}

	private void set(VariableValue value, boolean overwriteName) {
		if (value == null) {
			this.isNull = true;
		} else {
			this.type = value.getType();
			this.typeName = value.getTypeName();
			if (overwriteName)
				this.name = value.name;
			this.bufferValue = value.bufferValue;
			this.dateValue = value.dateValue;
			this.doubleValue = value.doubleValue;
			this.intValue = value.intValue;
			this.longValue = value.longValue;
			this.stringValue = value.stringValue;
			this.booleanValue = value.booleanValue;
			this.isNull = value.isNull;
		}
	}

	public int asInteger() {
		if (type == TCP_TYPE_INTEGER)
			return intValue;
		else if (type == TCP_TYPE_LONG)
			return (int) longValue;
		else if (type == TCP_TYPE_DOUBLE)
			return (int) doubleValue;
		else if (type == TCP_TYPE_STRING)
			return toInteger(stringValue, 0);
		else if (type == TCP_TYPE_BOOLEAN)
			return booleanValue ? 1 : 0;
		else if ((type == TCP_TYPE_BUFFER) && (bufferValue != null))
			return bufferValue.length;
		else
			return 0;
	}

	public long asLong() {
		if (type == TCP_TYPE_INTEGER)
			return intValue;
		else if (type == TCP_TYPE_LONG)
			return longValue;
		else if (type == TCP_TYPE_DOUBLE)
			return (long) doubleValue;
		else if (type == TCP_TYPE_STRING)
			return toLong(stringValue, 0);
		else if (type == TCP_TYPE_BOOLEAN)
			return booleanValue ? 1L : 0L;
		else if (type == TCP_TYPE_DATE)
			return dateValue.getTime();
		else if ((type == TCP_TYPE_BUFFER) && (bufferValue != null))
			return bufferValue.length;
		else
			return 0;
	}

	public double asDouble() {
		if (type == TCP_TYPE_INTEGER)
			return intValue;
		else if (type == TCP_TYPE_LONG)
			return longValue;
		else if (type == TCP_TYPE_DOUBLE)
			return doubleValue;
		else if (type == TCP_TYPE_STRING)
			return toDouble(stringValue, 0.0);
		else if (type == TCP_TYPE_BOOLEAN)
			return booleanValue ? 1.0 : 0.0;
		else if ((type == TCP_TYPE_BUFFER) && (bufferValue != null))
			return bufferValue.length;
		else
			return 0.0;
	}

	public String asString() {
		if (type == TCP_TYPE_INTEGER)
			return String.valueOf(intValue);
		else if (type == TCP_TYPE_LONG)
			return String.valueOf(longValue);
		else if (type == TCP_TYPE_DOUBLE)
			return String.valueOf(doubleValue);
		else if (type == TCP_TYPE_STRING)
			return stringValue;
		else if (type == TCP_TYPE_BOOLEAN)
			return String.valueOf(booleanValue);
		else if (type == TCP_TYPE_DATE)
			return new SimpleDateFormat().format(dateValue);
		else if (type == TCP_TYPE_BUFFER)
			return "[BUFFER DATA]"; //TODO: print HEX
		else
			return "<unrecognized string>";
	}

	public Date asDate() {
		if (type == TCP_TYPE_INTEGER)
			return new Date(intValue);
		else if (type == TCP_TYPE_LONG)
			return new Date(longValue);
		else if (type == TCP_TYPE_DOUBLE)
			return new Date((int) doubleValue);
		else if (type == TCP_TYPE_STRING)
			try {
				return new SimpleDateFormat().parse(stringValue);
			} catch (ParseException e) {
				return new Date(System.currentTimeMillis());
			}
		else if ((type == TCP_TYPE_DATE) && (dateValue != null))
			return dateValue;
		else
			return new Date(System.currentTimeMillis());
	}

	public byte[] asBuffer() {
		if (type == TCP_TYPE_STRING)
			return stringValue.getBytes();
		else if (type == TCP_TYPE_BUFFER)
			return bufferValue;
		else
			return new byte[1];
	}

	public boolean asBoolean() {
		if (type == TCP_TYPE_INTEGER)
			return intValue != 0;
		else if (type == TCP_TYPE_LONG)
			return longValue != 0;
		else if (type == TCP_TYPE_DOUBLE)
			return doubleValue > 0;
		else if (type == TCP_TYPE_STRING)
			return Boolean.valueOf(stringValue);
		else if (type == TCP_TYPE_BOOLEAN)
			return booleanValue;
		else if (type == TCP_TYPE_DATE)
			return true;
		else if (type == TCP_TYPE_BUFFER)
			return bufferValue.length > 0;
		else
			return false;
	}

	public void assignVariable(VariableValue value) {
		set(value, false);
	}

	/**
	 * @return the typeName
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * @param typeName
	 *            the typeName to set
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public String toString() {
		if (isNull())
			return "<null>";
		else
			return "\"" + asString() + "\"";
	}

	public static int toInteger(String asString) {
		return Integer.parseInt(asString);
	}

	public static int toInteger(String asString, int defaultValue) {
		try {
			return Integer.parseInt(asString);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static long toLong(String asString) {
		return Long.parseLong(asString);
	}

	public static long toLong(String asString, long defaultValue) {
		try {
			return Long.parseLong(asString);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static double toDouble(String asString) {
		return Double.parseDouble(asString);
	}

	public static double toDouble(String asString, double defaultValue) {
		try {
			return Double.parseDouble(asString);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * @return the isNull
	 */
	public boolean isNull() {
		return isNull;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		VariableValue clone = (VariableValue) super.clone();
		clone.type = this.type;
		clone.typeName = new String(this.typeName);
		clone.name = new String(this.name);
		clone.bufferValue = this.bufferValue != null ? this.bufferValue.clone() : null;
		try {
			clone.dateValue = new Date(this.dateValue.getTime());
		} catch (NullPointerException e) {
			clone.dateValue = null;
		}
		clone.doubleValue = this.doubleValue;
		clone.intValue = this.intValue;
		clone.longValue = this.longValue;
		clone.stringValue = new String(this.stringValue);
		clone.booleanValue = this.booleanValue;
		clone.isNull = this.isNull;
		return clone;
	}

}
