package kr.re.keti.sc.datamanager.util;

import java.util.ArrayList;
import java.util.List;

import kr.re.keti.sc.datamanager.common.code.Constants;

/**
 * String util class
 */
public class StringUtil {

	public static String camelToDbStyle(String str) {
		String regex = "([A-Z])";
		String replacement = Constants.COLUMN_DELIMITER + "$1";
        String value = str.substring(0, 1).toLowerCase() + str.substring(1, str.length()).replaceAll(regex, replacement).toLowerCase();
        return value;
	}

	public static String arrayStrToDbStyle(List<String> strList) {

		StringBuilder sb = new StringBuilder();
		for(int i=0; i<strList.size(); i++) {
			if(i > 0) sb.append(Constants.COLUMN_DELIMITER);
			sb.append(strList.get(i));
		}

		return sb.toString();
	}

	public static List<String> getHierarchyAttributeIds(String attributeId) {
		List<String> hierarchyAttributeIds = new ArrayList<>();

		if(attributeId.contains("\\[")) {
			String[] attributeIdArr = attributeId.split("\\[");
			for(String id : attributeIdArr) {
				hierarchyAttributeIds.add(id.replaceAll("]", ""));
			}
		} else if(attributeId.contains("\\.")) {
			String[] attributeIdArr = attributeId.split("\\.");
			for(String id : attributeIdArr) {
				hierarchyAttributeIds.add(id.replaceAll("\\.", ""));
			}
		} else {
			hierarchyAttributeIds.add(attributeId);
		}
		return hierarchyAttributeIds;
	}
}
