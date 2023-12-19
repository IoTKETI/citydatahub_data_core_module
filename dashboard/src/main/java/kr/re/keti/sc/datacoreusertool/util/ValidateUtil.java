package kr.re.keti.sc.datacoreusertool.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility for validation
 * @FileName ValidateUtil.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
public class ValidateUtil {

	/**
	 * Check empty object
	 * @param ob	Check target object
	 * @return		Empty: true, Not empty: flase
	 */
	public static boolean isEmptyData(Object ob) {
		if (ob == null || "".equals(ob.toString()))
			return true;
		return false;
	}

	/**
	 * Check empty list of object
	 * @param ob	Check target object
	 * @return		Empty: true, Not empty: flase
	 */
	public static boolean isEmptyData(List<?> ob) {
		if (ob == null || ob.size() == 0)
			return true;
		return false;
	}

	/**
	 * Checks if the object is a string. 
	 * @param value	Check target object
	 * @return		String type: true, Not string type: false
	 */
	public static boolean isStringObject(Object value) {
    	if(!(value instanceof String)) {
    		return false;
    	}
    	return true;
    }

	/**
	 * Check max length of string value
	 * @param value			Check target object
	 * @param maxLength
	 * @return
	 */
	public static boolean isValidStringMaxLength(Object value, Integer maxLength) {
    	// check max length
    	if(maxLength != null && maxLength > 0) {
    		if(((String)value).length() > maxLength) {
    			return false;
    		}
		}
    	return true;
    }

	/**
	 * validate string enumeration.
	 * @param value			Check target object
	 * @param valueEnum		List of enum value
	 * @return				Valid: true, Invalid: false
	 */
	public static boolean isValidStringEnum(Object value, List<Object> valueEnum) {
    	// Check whether valueEnum matches or not
    	if(valueEnum != null) {
    		for(Object allowValue : valueEnum) {
    			if(!allowValue.equals(value)) {
    				return false;
    			}
    		}
    	}
    	return true;
    }

	/**
	 * Checks if an object is an Integer.
	 * @param value		Check target object
	 * @return			Integer type: true, Not Integer type: false
	 */
    public static boolean isIntegerObject(Object value) {
    	if(value instanceof Integer) {
    		return true;
    	}
    	return false;
    }

    /**
     * Checks if an object is an Double.
     * @param value		Check target object
     * @return			Double type: true, Not Double type: false
     */
    public static boolean isDoubleObject(Object value) {
    	if(value instanceof Double) {
    		return true;
    	} else if(value instanceof Float) {
    		return true;
    	}
    	return false;
    }

    /**
     * Checks if an object is an Date.
     * @param value		Check target object
     * @return			Date type: true, No Date type: false
     */
    public static boolean isDateObject(Object value) {
    	if(value instanceof String) {
            try {
                DateUtil.strToDate((String)value);
                return true;
            } catch (ParseException e) {}
    	}
    	return false;
    }

    /**
     * Checks if an object is an ArrayString.
     * @param value		Check target object
     * @return			Array String type: true, No array String type: false
     */
    public static boolean isArrayStringObject(Object value) {
    	if(!(value instanceof ArrayList)) {
    		return false;
    	}

    	@SuppressWarnings("unchecked")
		ArrayList<Object> list = (ArrayList<Object>)value;
		for(Object listEntity : list) {
			if(!isStringObject(listEntity)) {
				return false;
			}
		}

    	return true;
    }

    /**
     * Validate max length of ArrayString.
     * @param value			Check target object
     * @param maxLength		Max length
     * @return				Valid: true, Invalid: false
     */
    public static boolean isValidArrayStringMaxLength(Object value, Integer maxLength) {
    	if(!isArrayStringObject(value)) {
    		return false;
    	}

    	// check max length
    	if(maxLength != null && maxLength > 0) {
	    	@SuppressWarnings("unchecked")
			ArrayList<String> list = (ArrayList<String>)value;
			for(String listEntity : list) {
	    		if(listEntity.length() > maxLength) {
	    			return false;
	    		}
			}
    	}
    	return true;
    }

    /**
     * Check the enumeration of ArrayString.
     * @param value			Check target object
     * @param valueEnum		List of enum value
     * @return				Valid: true, Invalid: false
     */
    public static boolean isValidArrayStringEnum(Object value, List<Object> valueEnum) {
    	if(!isArrayStringObject(value)) {
    		return false;
    	}

    	// Check whether valueEnum matches or not
    	if(valueEnum != null) {
    		@SuppressWarnings("unchecked")
    		ArrayList<String> list = (ArrayList<String>)value;
    		for(String listEntity : list) {
    			for(Object allowValue : valueEnum) {
        			if(!allowValue.equals(listEntity)) {
        				return false;
        			}
        		}
    		}	
    	}

    	return true;
    }

    /**
     * Checks if an object is an integer array.
     * @param value		Check target object
     * @return			Array Integer type: true, No array Integer type: false
     */
    public static boolean isArrayIntegerObject(Object value) {
    	if(!(value instanceof ArrayList)) {
    		return false;
    	}

		@SuppressWarnings("unchecked")
		ArrayList<Object> list = (ArrayList<Object>)value;
		for(Object listEntity : list) {
			if(!isIntegerObject(listEntity)) {
				return false;
			}
		}
    	return true;
    }

    /**
     * Checks if an object is a double array.
     * @param value		Check target object
     * @return			Array Double type: true, No array Double type: false
     */
    public static boolean isArrayDoubleObject(Object value) {
    	if(!(value instanceof ArrayList)) {
    		return false;
    	}

		@SuppressWarnings("unchecked")
		ArrayList<Object> list = (ArrayList<Object>)value;
		for(Object listEntity : list) {
			if(!isDoubleObject(listEntity)) {
				return false;
			}
		}
    	return true;
    }

    /**
     * Checks if an object is a map.
     * @param value		Check target object
     * @return			Map type: true, No Map type: false
     */
    public static boolean isMapObject(Object value) {
    	if(value instanceof Map) {
    		return true;
    	}
    	return true;
    }
}
