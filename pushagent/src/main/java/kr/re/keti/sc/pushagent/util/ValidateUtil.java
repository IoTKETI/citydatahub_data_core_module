package kr.re.keti.sc.pushagent.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

	public static boolean isValidStringEnum(Object value, List<Object> valueEnum) {
		// valueEnum 과 일치 여부 체크

		//1. valueEnum이 없는 경우 true 리턴
		if(valueEnum == null) return true;

		//2. valueEnum 중 하나라도 맞으면 true 리턴
		if(valueEnum != null) {
    		for(Object allowValue : valueEnum) {
    			if(allowValue.equals(value)) {
    				return true;
    			}
    		}
    	}
    	return false;
    }

    public static boolean isIntegerObject(Object value) {
    	if(value instanceof Integer) {
    		return true;
    	}
    	return false;
    }

    public static boolean isValidIntegerGreaterThanOrEqualTo(Object value, Double greaterThanOrEqualTo) {

		if (greaterThanOrEqualTo != null) {
			if (((Integer) value) < greaterThanOrEqualTo.intValue()) {
				return false;
			}
		}
		return true;
	}
    
    public static boolean isValidIntegerGreaterThan(Object value, Double greaterThan) {

		if (greaterThan != null) {
			if (((Integer) value) <= greaterThan.intValue()) {
				return false;
			}
		}
		return true;
	}
    
    public static boolean isValidIntegerLessThanOrEqualTo(Object value, Double lessThanOrEqualTo) {

		if (lessThanOrEqualTo != null) {
			if (((Integer) value) > lessThanOrEqualTo.intValue()) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidIntegerLessThan(Object value, Double lessThan) {

		if (lessThan != null) {
			if (((Integer) value) >= lessThan.intValue()) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidIntegerEnum(Object value, List<Object> valueEnum) {
		// valueEnum 과 일치 여부 체크

		//1. valueEnum이 없는 경우 true 리턴
		if(valueEnum == null) return true;

		//2. valueEnum 중 하나라도 맞으면 true 리턴
		if(valueEnum != null) {
			for(Object allowValue : valueEnum) {
				if(allowValue.equals(value)) {
					return true;
				}
			}
		}
		return false;
	}
	
    public static boolean isDoubleObject(Object value) {
		if (value instanceof Double) {
			return true;
		} else if (value instanceof Float) {
			return true;
		} else if (value instanceof Integer) {
			// 실수형 타입에 Integer 허용 요청 처리
			//https://doublepkr.monday.com/boards/674155218/pulses/835779284/posts/829087079
			return true;
		}
    	return false;
    }

    public static boolean isValidDoubleGreaterThanOrEqualTo(Object value, Double greaterThanOrEqualTo) {

		if (greaterThanOrEqualTo != null) {
			if (((Double) value) < greaterThanOrEqualTo) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidDoubleGreaterThan(Object value, Double greaterThan) {

		if (greaterThan != null) {
			if (((Double) value) <= greaterThan) {
				return false;
			}
		}
		return true;
	}


	public static boolean isValidDoubleLessThanOrEqualTo(Object value, Double lessThanOrEqualTo) {

		if (lessThanOrEqualTo != null) {
			if (((Double) value) > lessThanOrEqualTo) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidDoubleLessThan(Object value, Double lessThan) {

		if (lessThan != null) {
			if (((Double) value) >= lessThan) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidDoubleEnum(Object value, List<Object> valueEnum) {
		// valueEnum 과 일치 여부 체크

		//1. valueEnum이 없는 경우 true 리턴
		if(valueEnum == null) return true;

		//2. valueEnum 중 하나라도 맞으면 true 리턴
		if(valueEnum != null) {
			for(Object allowValue : valueEnum) {
				if(allowValue.equals(value)) {
					return true;
				}
			}
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

    public static boolean isValidArrayStringEnum(Object value, List<Object> valueEnum) {
		if(!isArrayStringObject(value)) {
			return false;
		}


		//1. valueEnum이 없는 경우 true 리턴
		if(valueEnum == null) return true;

		// valueEnum 과 일치 여부 체크
		// valueEnum 과 일치 여부 체크
		if (valueEnum != null) {
			@SuppressWarnings("unchecked")
			ArrayList<String> list = (ArrayList<String>) value;
			boolean checkFlag = true;
			for (String listEntity : list) {
				if (!valueEnum.contains(listEntity)) {
					checkFlag = false;
				}
			}
			return checkFlag;
		}

		return false;
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

    public static boolean isValidArrayIntegerGreaterThanOrEqualTo(Object value, Double greaterThanOrEqualTo) {
		if(!isArrayIntegerObject(value)) {
			return false;
		}

		if(!ValidateUtil.isEmptyData(greaterThanOrEqualTo)) {
			@SuppressWarnings("unchecked")
			ArrayList<Integer> list = (ArrayList<Integer>)value;
			for(Integer listEntity : list) {
				try {
					if(!isValidIntegerGreaterThanOrEqualTo(listEntity, greaterThanOrEqualTo)) {
						return false;
					}
				} catch(NumberFormatException e) {
					continue;
				}
			}
		}
		return true;
	}


	public static boolean isValidArrayIntegerGreaterThan(Object value, Double greaterThan) {
		if(!isArrayIntegerObject(value)) {
			return false;
		}

		if(!ValidateUtil.isEmptyData(greaterThan)) {
			@SuppressWarnings("unchecked")
			ArrayList<Integer> list = (ArrayList<Integer>)value;
			for(Integer listEntity : list) {
				try {
					if(!isValidIntegerGreaterThan(listEntity, greaterThan)) {
						return false;
					}
				} catch(NumberFormatException e) {
					continue;
				}
			}
		}
		return true;
	}


	public static boolean isValidArrayIntegerLessOrEqualTo(Object value, Double lessThanOrEqualTo) {
		if(!isArrayIntegerObject(value)) {
			return false;
		}

		if(!ValidateUtil.isEmptyData(lessThanOrEqualTo)) {
			@SuppressWarnings("unchecked")
			ArrayList<Integer> list = (ArrayList<Integer>)value;
			for(Integer listEntity : list) {
				try {
					if(!isValidIntegerLessThanOrEqualTo(listEntity, lessThanOrEqualTo)) {
						return false;
					}
				} catch(NumberFormatException e) {
					continue;
				}
			}
		}
		return true;
	}


	public static boolean isValidArrayIntegerLessThan(Object value, Double lessThan) {
		if(!isArrayIntegerObject(value)) {
			return false;
		}

		if(!ValidateUtil.isEmptyData(lessThan)) {
			@SuppressWarnings("unchecked")
			ArrayList<Integer> list = (ArrayList<Integer>)value;
			for(Integer listEntity : list) {
				try {
					if(!isValidIntegerLessThan(listEntity, lessThan)) {
						return false;
					}
				} catch(NumberFormatException e) {
					continue;
				}
			}
		}
		return true;
	}


	public static boolean isValidArrayIntegerEnum(Object value, List<Object> valueEnum) {
		if(!isArrayIntegerObject(value)) {
			return false;
		}


		//1. valueEnum이 없는 경우 true 리턴
		if(valueEnum == null) return true;

		// valueEnum 과 일치 여부 체크
		if (valueEnum != null) {
			@SuppressWarnings("unchecked")
			ArrayList<Integer> list = (ArrayList<Integer>) value;
			boolean checkFlag = true;
			for (Integer listEntity : list) {
				if (!valueEnum.contains(listEntity)) {
					checkFlag = false;
				}
			}
			return checkFlag;
		}

		return false;
	}
	
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

    public static boolean isValidArrayDoubleGreaterThanOrEqualTo(Object value, Double greaterThanOrEqualTo) {
		if(!isArrayDoubleObject(value)) {
			return false;
		}

		if(!ValidateUtil.isEmptyData(greaterThanOrEqualTo)) {
			@SuppressWarnings("unchecked")
			ArrayList<Double> list = (ArrayList<Double>)value;
			for(Double listEntity : list) {
				try {
					if(!isValidDoubleGreaterThanOrEqualTo(listEntity, greaterThanOrEqualTo)) {
						return false;
					}
				} catch(NumberFormatException e) {
					continue;
				}
			}
		}
		return true;
	}


	public static boolean isValidArrayDoubleGreaterThan(Object value, Double greaterThan) {
		if(!isArrayDoubleObject(value)) {
			return false;
		}

		if(!ValidateUtil.isEmptyData(greaterThan)) {
			@SuppressWarnings("unchecked")
			ArrayList<Double> list = (ArrayList<Double>)value;
			for(Double listEntity : list) {
				try {
					if(!isValidDoubleGreaterThan(listEntity, greaterThan)) {
						return false;
					}
				} catch(NumberFormatException e) {
					continue;
				}
			}
		}
		return true;
	}


	public static boolean isValidArrayDoubleLessThanOrEqualTo(Object value, Double lessThanOrEqualTo) {
		if(!isArrayDoubleObject(value)) {
			return false;
		}

		if(!ValidateUtil.isEmptyData(lessThanOrEqualTo)) {
			@SuppressWarnings("unchecked")
			ArrayList<Double> list = (ArrayList<Double>)value;
			for(Double listEntity : list) {
				try {
					if(!isValidDoubleLessThanOrEqualTo(listEntity, lessThanOrEqualTo)) {
						return false;
					}
				} catch(NumberFormatException e) {
					continue;
				}
			}
		}
		return true;
	}


	public static boolean isValidArrayDoubleLessThan(Object value, Double lessThan) {
		if(!isArrayDoubleObject(value)) {
			return false;
		}

		if(!ValidateUtil.isEmptyData(lessThan)) {
			@SuppressWarnings("unchecked")
			ArrayList<Double> list = (ArrayList<Double>)value;
			for(Double listEntity : list) {
				try {
					if(!isValidDoubleLessThan(listEntity, lessThan)) {
						return false;
					}
				} catch(NumberFormatException e) {
					continue;
				}
			}
		}
		return true;
	}


	public static boolean isValidArrayDoubleEnum(Object value, List<Object> valueEnum) {
		if(!isArrayDoubleObject(value)) {
			return false;
		}

		//1. valueEnum이 없는 경우 true 리턴
		if(valueEnum == null) return true;

		// valueEnum 과 일치 여부 체크
		if (valueEnum != null) {
			@SuppressWarnings("unchecked")
			ArrayList<Double> list = (ArrayList<Double>) value;
			boolean checkFlag = true;
			for (Double listEntity : list) {
				if (!valueEnum.contains(listEntity)) {
					checkFlag = false;
				}
			}
			return checkFlag;
		}

		return false;
	}
	
    public static boolean isMapObject(Object value) {
    	if(value instanceof Map) {
    		return true;
    	}
    	return true;
    }
}
