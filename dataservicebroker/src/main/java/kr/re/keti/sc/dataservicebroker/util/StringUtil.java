package kr.re.keti.sc.dataservicebroker.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;

public class StringUtil {

    public static String camelToDbStyle(String str) {
        String regex = "([A-Z])";
        String replacement = Constants.COLUMN_DELIMITER + "$1";
        String value = str.substring(0, 1).toLowerCase() + str.substring(1, str.length()).replaceAll(regex, replacement).toLowerCase();
        return value;
    }

    public static String arrayStrToDbStyle(List<String> strList) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strList.size(); i++) {
            if (i > 0) sb.append(Constants.COLUMN_DELIMITER);
            sb.append(strList.get(i));
        }

        return sb.toString();
    }

    public static List<String> getHierarchyAttributeIds(String attributeId) {
        List<String> hierarchyAttributeIds = new ArrayList<>();

        if (attributeId.contains("\\[")) {
            String[] attributeIdArr = attributeId.split("\\[");
            for (String id : attributeIdArr) {
                hierarchyAttributeIds.add(id.replaceAll("]", ""));
            }
        } else if (attributeId.contains("\\.")) {
            String[] attributeIdArr = attributeId.split("\\.");
            for (String id : attributeIdArr) {
                hierarchyAttributeIds.add(id.replaceAll("\\.", ""));
            }
        } else {
            hierarchyAttributeIds.add(attributeId);
        }
        return hierarchyAttributeIds;
    }

    public static String removeSpecialCharAndLower(String str) {
        String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
        str = str.replaceAll(match, "");
        return str.toLowerCase();
    }

    public static boolean checkURIPattern(String uri) {
    	if(uri == null) {
    		return false;
    	}

    	if(uri.startsWith("http")) {
    		String regex = "^(https?):\\/\\/([^:\\/\\s]+)(:([^\\/]*))?((\\/[^\\s/\\/]+)*)?\\/?([^#\\s\\?]*)(\\?([^#\\s]*))?(#(\\w*))?$";

            Pattern p = Pattern.compile(regex);

            boolean isValidUri = p.matcher(uri).matches();
            return isValidUri;
    	} else if(uri.startsWith("mqtt")) {
    		return true;
    	}
    	return false;
    }
    
    public static String generateRandomString(int lenth) {
    	if(lenth < 1) {
    		return "";
    	}

    	StringBuilder sb = new StringBuilder();
    	for(int i=0; i<lenth; i++) {
    		char c = (char)((int)(Math.random()*26+65));
        	sb.append(c);
    	}
    	return sb.toString();
    }

    /**
     * SQL 식별자를 큰따옴표로 감싼다 (PostgreSQL).
     *  - 숫자로 시작하는 이름(예: 5daysrainfallmax)은 따옴표 없이는 문법 오류이기 때문
     *  - 안의 큰따옴표는 "" 로 이스케이프
     *  - 이미 따옴표로 감싼 이름은 그대로 반환 (이중 인용 방지)
     * @param identifier 식별자
     * @return 따옴표로 감싼 식별자 (null 이면 null)
     */
    public static String quoteIdentifier(String identifier) {
        if (identifier == null) {
            return null;
        }
        if (isQuotedIdentifier(identifier)) {
            return identifier;
        }
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    /**
     * RDB 동적 컬럼명을 소문자로 바꾼 뒤 큰따옴표로 감싼다.
     *  - 기존 테이블의 동적 컬럼은 따옴표 없이 만들어져 PostgreSQL 이 소문자로 접어 두었다.
     *    소문자로 맞춘 뒤 감싸야 기존 컬럼과 같은 컬럼을 가리킨다.
     *  - 고정 컬럼(ID, DATASET_ID 등)에도 써도 안전하다 (모두 소문자로 접혀 있음)
     * @param columnName 컬럼명
     * @return 따옴표로 감싼 소문자 컬럼명 (null 이면 null)
     */
    public static String quoteColumnName(String columnName) {
        if (columnName == null) {
            return null;
        }
        if (isQuotedIdentifier(columnName)) {
            return columnName;
        }
        return quoteIdentifier(columnName.toLowerCase(Locale.ROOT));
    }

    private static boolean isQuotedIdentifier(String identifier) {
        return identifier.length() >= 2 && identifier.startsWith("\"") && identifier.endsWith("\"");
    }
}
