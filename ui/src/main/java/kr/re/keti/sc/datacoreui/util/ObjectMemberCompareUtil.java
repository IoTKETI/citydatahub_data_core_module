package kr.re.keti.sc.datacoreui.util;

import java.util.Comparator;

import kr.re.keti.sc.datacoreui.api.datamodel.vo.ObjectMemberVO;

/**
 * Utility for comparing obejct member.
 * @FileName ObjectMemberCompareUtil.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
public class ObjectMemberCompareUtil implements Comparator<ObjectMemberVO> {

	/**
	 * Compare object member.
	 */
	@Override
	public int compare(ObjectMemberVO objectMember1, ObjectMemberVO objectMember2) {
		String firstValue = objectMember1.getName();
		String secondValue = objectMember2.getName();
		
		// Order by asc
		return firstValue.compareTo(secondValue);
	}

}
