package kr.re.keti.sc.dataservicebroker.util;

import java.util.ArrayList;
import java.util.List;
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
}
