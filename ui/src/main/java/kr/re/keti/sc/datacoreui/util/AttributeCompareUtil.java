package kr.re.keti.sc.datacoreui.util;

import java.util.Comparator;

import kr.re.keti.sc.datacoreui.api.datamodel.vo.AttributeVO;

/**
 * Utility for attribute comparison.
 * @FileName AttributeCompareUtil.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
public class AttributeCompareUtil implements Comparator<AttributeVO> {

	/**
	 * attribute comparison
	 */
	@Override
	public int compare(AttributeVO attribute1, AttributeVO attribute2) {
		String firstValue = attribute1.getName();
		String secondValue = attribute2.getName();
		
		// Order by asc
		return firstValue.compareTo(secondValue);
	}

}
