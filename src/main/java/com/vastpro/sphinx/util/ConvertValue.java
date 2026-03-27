package com.vastpro.sphinx.util;

public class ConvertValue {

	public static Integer toInteger(Object value) {
		if(value==null) {
			return null;
		}
		
		if(value instanceof Integer) {
			return (Integer) value;
		}
		
		if(value instanceof String) {
			try {
				return Integer.parseInt((String) value);
			}catch(NumberFormatException e) {
				return null;
			}
		}
		return null;
	}
}
