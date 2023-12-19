package kr.re.keti.sc.datamanager.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * parameter validate util class
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
        if (!(value instanceof String)) {
            return false;
        }
        return true;
    }

    public static boolean isValidStringMaxLength(Object value, String maxLength) {
        // 최대 길이 체크
        if (maxLength != null) {
            Integer integerMaxLength = Integer.parseInt(maxLength);
            if (integerMaxLength > 0) {
                if (((String) value).length() > integerMaxLength) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isValidIntegerMaxLength(Object value, String maxLength) {
        // 최대 길이 체크
        if (maxLength != null) {
            Integer integerMaxLength = Integer.parseInt(maxLength);
            if (integerMaxLength > 0) {
                if ((Integer) value > integerMaxLength) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isValidDoubleMaxLength(Object value, String maxLength) {
        // 최대 길이 체크
        if (maxLength != null) {
            Double doubleMaxLength = Double.parseDouble(maxLength);
            if (doubleMaxLength > 0) {
                if ((Double) value > doubleMaxLength) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isValidStringMinLength(Object value, String minLength) {
        // 최대 길이 체크
        if (minLength != null) {
            Integer integerMinLength = Integer.parseInt(minLength);
            if (integerMinLength > 0) {
                if (((String) value).length() < integerMinLength) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isValidIntegerMinLength(Object value, String minLength) {
        // 최대 길이 체크
        if (minLength != null) {
            Integer integerMinLength = Integer.parseInt(minLength);
            if (integerMinLength > 0) {
                if ((Integer) value < integerMinLength) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isValidDoubleMinLength(Object value, String minLength) {
        // 최대 길이 체크
        if (minLength != null) {
            Double doubleMaxLength = Double.parseDouble(minLength);
            if (doubleMaxLength > 0.0) {
                if ((Double) value < doubleMaxLength) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isValidStringEnum(Object value, List<Object> valueEnum) {
        // valueEnum 과 일치 여부 체크
        if (valueEnum != null) {
            for (Object allowValue : valueEnum) {
                if (!allowValue.equals(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isIntegerObject(Object value) {
        if (value instanceof Integer) {
            return true;
        }
        return false;
    }

    public static boolean isDoubleObject(Object value) {
        if (value instanceof Double) {
            return true;
        } else if (value instanceof Float) {
            return true;
        }
        return false;
    }

    public static boolean isArrayStringObject(Object value) {
        if (!(value instanceof ArrayList)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        ArrayList<Object> list = (ArrayList<Object>) value;
        for (Object listEntity : list) {
            if (!isStringObject(listEntity)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidArrayStringMaxLength(Object value, String maxLength) {
        if (!isArrayStringObject(value)) {
            return false;
        }

        // 최대 길이 체크
        if (maxLength != null) {
            Integer integerMaxLength = Integer.parseInt(maxLength);
            if (integerMaxLength > 0) {
                @SuppressWarnings("unchecked")
                ArrayList<String> list = (ArrayList<String>) value;
                for (String listEntity : list) {
                    if (listEntity.length() > integerMaxLength) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static boolean isValidArrayIntegerMaxLength(Object value, String maxLength) {
        if (!isArrayStringObject(value)) {
            return false;
        }

        // 최대 길이 체크
        if (maxLength != null) {
            Integer integerMaxLength = Integer.parseInt(maxLength);
            if (integerMaxLength > 0) {
                @SuppressWarnings("unchecked")
                ArrayList<Integer> list = (ArrayList<Integer>) value;
                for (Integer listEntity : list) {
                    if (listEntity > integerMaxLength) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static boolean isValidArrayDoubleMaxLength(Object value, String maxLength) {
        if (!isArrayStringObject(value)) {
            return false;
        }

        // 최대 길이 체크
        if (maxLength != null) {
            Double doubleMaxLength = Double.parseDouble(maxLength);
            if (doubleMaxLength > 0) {
                @SuppressWarnings("unchecked")
                ArrayList<Double> list = (ArrayList<Double>) value;
                for (Double listEntity : list) {
                    if (listEntity > doubleMaxLength) {
                        return false;
                    }
                }
            }
        }

        return true;
    }


    public static boolean isValidArrayStringMinLength(Object value, String minLength) {
        if (!isArrayStringObject(value)) {
            return false;
        }

        // 최대 길이 체크
        if (minLength != null) {
            Integer integerMinLength = Integer.parseInt(minLength);
            if (integerMinLength > 0) {
                @SuppressWarnings("unchecked")
                ArrayList<String> list = (ArrayList<String>) value;
                for (String listEntity : list) {
                    if (listEntity.length() < integerMinLength) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static boolean isValidArrayIntegerMinLength(Object value, String minLength) {
        if (!isArrayStringObject(value)) {
            return false;
        }

        // 최대 길이 체크
        if (minLength != null) {
            Integer integerMinLength = Integer.parseInt(minLength);
            if (integerMinLength > 0) {
                @SuppressWarnings("unchecked")
                ArrayList<Integer> list = (ArrayList<Integer>) value;
                for (Integer listEntity : list) {
                    if (listEntity < integerMinLength) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static boolean isValidArrayDoubleMinLength(Object value, String minLength) {
        if (!isArrayStringObject(value)) {
            return false;
        }

        // 최대 길이 체크
        if (minLength != null) {
            Double doubleMinLength = Double.parseDouble(minLength);
            if (doubleMinLength > 0) {
                @SuppressWarnings("unchecked")
                ArrayList<Double> list = (ArrayList<Double>) value;
                for (Double listEntity : list) {
                    if (listEntity < doubleMinLength) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static boolean isValidArrayStringEnum(Object value, List<Object> valueEnum) {
        if (!isArrayStringObject(value)) {
            return false;
        }

        // valueEnum 과 일치 여부 체크
        if (valueEnum != null) {
            @SuppressWarnings("unchecked")
            ArrayList<String> list = (ArrayList<String>) value;
            for (String listEntity : list) {
                for (Object allowValue : valueEnum) {
                    if (!allowValue.equals(listEntity)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static boolean isArrayIntegerObject(Object value) {
        if (!(value instanceof ArrayList)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        ArrayList<Object> list = (ArrayList<Object>) value;
        for (Object listEntity : list) {
            if (!isIntegerObject(listEntity)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isArrayDoubleObject(Object value) {
        if (!(value instanceof ArrayList)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        ArrayList<Object> list = (ArrayList<Object>) value;
        for (Object listEntity : list) {
            if (!isDoubleObject(listEntity)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isMapObject(Object value) {
        if (value instanceof Map) {
            return true;
        }
        return true;
    }
}
