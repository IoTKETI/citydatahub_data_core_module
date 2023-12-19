package kr.re.keti.sc.datacoreui.api.menu.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * This is the VO class used when responding to the menu role relation.
 * @FileName MenuRoleRelationResponseVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuRoleRelationResponseVO {
	/** Role assign menu list */
	private List<MenuIdVO> roleAssignMenu;
	/** Role unassign menu list */
	private List<MenuIdVO> noRoleAssignMenu;
}
