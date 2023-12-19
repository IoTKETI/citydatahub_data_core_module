package kr.re.keti.sc.ingestinterface.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parameter validate util class
 */
public class ValidateUtil {

	public static boolean isEmptyData(Object ob) {
		if (ob == null || "".equals(ob.toString()))
			return true;
		return false;
	}

	public static boolean isEmptyData(List<?> ob) {
		if (ob == null || ob.size() == 0)
			return true;
		return false;
	}

	public static boolean isStringObject(Object value) {
		if(!(value instanceof String)) {
			return false;
		}
		return true;
	}

	public static boolean isValidStringMaxLength(Object value, String maxLength) {

		if(ValidateUtil.isEmptyData(maxLength)) {
			return true;
		}

		try {
			return isValidStringMaxLength(value, Integer.parseInt(maxLength));
		} catch(NumberFormatException e) {
			return true;
		}
	}

	public static boolean isValidStringMaxLength(Object value, Integer maxLength) {
		// 최대 길이 체크
		if(maxLength != null && maxLength > 0) {
			if(((String)value).length() > maxLength) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidStringMinLength(Object value, String minLength) {
		// 최소 길이 체크
		if(ValidateUtil.isEmptyData(minLength)) {
			return true;
		}

		try {
			return isValidStringMinLength(value, Integer.parseInt(minLength));
		} catch(NumberFormatException e) {
			return true;
		}
	}

	public static boolean isValidStringMinLength(Object value, Integer minLength) {
		// 최대 길이 체크
		if(minLength != null && minLength > 0) {
			if(((String)value).length() < minLength) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidEnum(Object value, List<Object> valueEnum) {
		// valueEnum 과 일치 여부 체크

		//1. valueEnum이 없는 경우 true 리턴
		if(valueEnum == null) return true;

		//2. valueEnum 중 하나라도 맞으면 true 리턴
		if(valueEnum != null) {
			String strValue = String.valueOf(value);
			for(Object allowValue : valueEnum) {
				if(strValue.equals(String.valueOf(allowValue))) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isIntegerObject(Object value) {
		try {
			Integer integer = new Integer(String.valueOf(value));
			return true;
		} catch (NumberFormatException e) {

		}
		return false;
	}

	public static boolean isLongObject(Object value) {
		try {
			Long l = new Long(String.valueOf(value));
			return true;
		} catch (NumberFormatException e) {

		}
		return false;
	}

	public static boolean isValidGreaterThanOrEqualTo(Object value, BigDecimal greaterThanOrEqualTo) {

		if (greaterThanOrEqualTo != null) {
			try {
				BigDecimal bigDecimal = new BigDecimal(String.valueOf(value));
				if (bigDecimal.compareTo(greaterThanOrEqualTo) == -1) {
					return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidGreaterThan(Object value, BigDecimal greaterThan) {

		if (greaterThan != null) {
			try {
				BigDecimal bigDecimal = new BigDecimal(String.valueOf(value));
				if (bigDecimal.compareTo(greaterThan) < 1) {
					return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidLessThanOrEqualTo(Object value, BigDecimal lessThanOrEqualTo) {

		if (lessThanOrEqualTo != null) {
			try {
				BigDecimal bigDecimal = new BigDecimal(String.valueOf(value));
				if (bigDecimal.compareTo(lessThanOrEqualTo) == 1) {
					return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidLessThan(Object value, BigDecimal lessThan) {

		if (lessThan != null) {
			try {
				BigDecimal bigDecimal = new BigDecimal(String.valueOf(value));
				if (bigDecimal.compareTo(lessThan) > -1) {
					return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}


	public static boolean isBigDecimalObject(Object value) {
		try {
			BigDecimal b = new BigDecimal(String.valueOf(value));
			return true;
		} catch (NumberFormatException e) {

		}
		return false;
	}


	public static boolean isDateObject(Object value) {
		if(value instanceof String) {
			try {
				DateUtil.strToDate((String)value);
				return true;
			} catch (ParseException e) {}
		}
		return false;
	}

	public static boolean isBooleanObject(Object value) {
		if(value instanceof Boolean) {
			return true;
		}
		return false;
	}

	public static boolean isArrayBooleanObject(Object value) {
		if(!(value instanceof ArrayList)) {
			return false;
		}

		@SuppressWarnings("unchecked")
		ArrayList<Object> list = (ArrayList<Object>)value;
		for(Object listEntity : list) {
			if(!isBooleanObject(listEntity)) {
				return false;
			}
		}

		return true;
	}

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

	public static boolean isValidArrayStringMinLength(Object value, String minLength) {
		if(!isArrayStringObject(value)) {
			return false;
		}

		// 최대 길이 체크
		if(!ValidateUtil.isEmptyData(minLength)) {
			@SuppressWarnings("unchecked")
			ArrayList<String> list = (ArrayList<String>)value;
			for(String listEntity : list) {
				try {
					if(!isValidStringMinLength(listEntity, Integer.parseInt(minLength))) {
						return false;
					}
				} catch(NumberFormatException e) {
					continue;
				}
			}
		}
		return true;
	}

	public static boolean isValidArrayStringMaxLength(Object value, String maxLength) {
		if(!isArrayStringObject(value)) {
			return false;
		}

		// 최대 길이 체크
		if(!ValidateUtil.isEmptyData(maxLength)) {
			@SuppressWarnings("unchecked")
			ArrayList<String> list = (ArrayList<String>)value;
			for(String listEntity : list) {
				try {
					if(!isValidStringMaxLength(listEntity, Integer.parseInt(maxLength))) {
						return false;
					}
				} catch(NumberFormatException e) {
					continue;
				}
			}
		}
		return true;
	}

	public static boolean isValidArrayEnum(Object value, List<Object> valueEnum) {

		//1. valueEnum이 없는 경우 true 리턴
		if(value == null || valueEnum == null) {
			return true;
		}

		if(!(value instanceof ArrayList)) {
			return false;
		}

		// valueEnum 과 일치 여부 체크
		List<String> valueStrList = ((List<Object>)value).stream().map( String::valueOf ).collect( Collectors.toList() );
		List<String> enumStrList = valueEnum.stream().map( String::valueOf ).collect( Collectors.toList() );

		for(String valueStr : valueStrList) {
			if(!enumStrList.contains(valueStr)) {
				return false;
			}
		}

		return true;
	}

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

	public static boolean isArrayLongObject(Object value) {
		if(!(value instanceof ArrayList)) {
			return false;
		}

		@SuppressWarnings("unchecked")
		ArrayList<Object> list = (ArrayList<Object>)value;
		for(Object listEntity : list) {
			if(!isLongObject(listEntity)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidArrayGreaterThanOrEqualTo(Object value, BigDecimal greaterThanOrEqualTo) {

		if(!(value instanceof ArrayList)) {
			return false;
		}

		if(!ValidateUtil.isEmptyData(greaterThanOrEqualTo)) {
			for(Object listEntity : (ArrayList<Object>)value) {
				if(!isValidGreaterThanOrEqualTo(listEntity, greaterThanOrEqualTo)) {
					return false;
				}
			}
		}
		return true;
	}


	public static boolean isValidArrayGreaterThan(Object value, BigDecimal greaterThan) {

		if(!(value instanceof ArrayList)) {
			return false;
		}

		if(!ValidateUtil.isEmptyData(greaterThan)) {
			for(Object listEntity : (ArrayList<Object>)value) {
				if(!isValidGreaterThan(listEntity, greaterThan)) {
					return false;
				}
			}
		}
		return true;
	}


	public static boolean isValidArrayLessThenOrEqualTo(Object value, BigDecimal lessThanOrEqualTo) {

		if(!(value instanceof ArrayList)) {
			return false;
		}

		if(!ValidateUtil.isEmptyData(lessThanOrEqualTo)) {
			for(Object listEntity : (ArrayList<Object>)value) {
				if(!isValidLessThanOrEqualTo(listEntity, lessThanOrEqualTo)) {
					return false;
				}
			}
		}
		return true;
	}


	public static boolean isValidArrayLessThan(Object value, BigDecimal lessThan) {

		if(!(value instanceof ArrayList)) {
			return false;
		}

		if(!ValidateUtil.isEmptyData(lessThan)) {
			for(Object listEntity : (ArrayList<Object>)value) {
				if(!isValidLessThan(listEntity, lessThan)) {
					return false;
				}
			}
		}
		return true;
	}


	public static boolean isArrayBigDecimalObject(Object value) {
		if(!(value instanceof ArrayList)) {
			return false;
		}

		@SuppressWarnings("unchecked")
		ArrayList<Object> list = (ArrayList<Object>)value;
		for(Object listEntity : list) {
			if(!isBigDecimalObject(listEntity)) {
				return false;
			}
		}
		return true;
	}


	public static boolean isMapObject(Object value) {
		if(value instanceof Map) {
			return true;
		}
		return true;
	}

	public static boolean isValidUrn(String urn) {
		if(isEmptyData(urn)) {
			return false;
		}

		if(!urn.startsWith("urn:")
				|| urn.split(":").length < 3) {
			return false;
		}

		return true;
	}
}
