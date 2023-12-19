package kr.re.keti.sc.ingestinterface.util;

import java.util.ArrayList;
import java.util.List;

import kr.re.keti.sc.ingestinterface.common.code.Constants;

/**
 * String util class
 */
public class StringUtil {

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
