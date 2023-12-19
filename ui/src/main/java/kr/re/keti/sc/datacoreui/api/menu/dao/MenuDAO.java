package kr.re.keti.sc.datacoreui.api.menu.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import kr.re.keti.sc.datacoreui.api.menu.vo.AccessableMenuRetrieveVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuBaseVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRetrieveVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRoleBaseVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRoleRelationBaseVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRoleRetrieveVO;

/**
 * Menu management DAO class.
 * @FileName MenuDAO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Repository
public class MenuDAO {
	@Autowired
	private SqlSessionTemplate sqlSession;

	/**
	 * Create menu
	 * @param menuBaseVO
	 * @return
	 */
	public int createMenu(MenuBaseVO menuBaseVO) {
		return sqlSession.insert("datacoreui.menu.createMenu", menuBaseVO);
	}

	/**
	 * Update menu
	 * @param menuBaseVO
	 * @return
	 */
	public int updateMenu(MenuBaseVO menuBaseVO) {
		return sqlSession.update("datacoreui.menu.updateMenu", menuBaseVO);
	}

	/**
	 *  Delete menu
	 * @param menuRetrieveVO
	 * @return
	 */
	public int deleteMenu(MenuRetrieveVO menuRetrieveVO) {
		return sqlSession.delete("datacoreui.menu.deleteMenu", menuRetrieveVO);
	}

	/**
	 * Select menu
	 * @param menuRetrieveVO
	 * @return
	 */
	public MenuBaseVO getMenu(MenuRetrieveVO menuRetrieveVO) {
		return sqlSession.selectOne("datacoreui.menu.selectMenu", menuRetrieveVO);
	}

	/**
	 * Select list of menu
	 * @param menuRetrieveVO
	 * @return
	 */
	public List<MenuBaseVO> getMenus(MenuRetrieveVO menuRetrieveVO) {
		return sqlSession.selectList("datacoreui.menu.selectMenus", menuRetrieveVO);
	}
	
	/**
	 * Select list of access menu
	 * @param menuRoleId
	 * @return
	 */
	public List<MenuBaseVO> getAccessMenus(AccessableMenuRetrieveVO accessableMenuRetrieveVO) {
		return sqlSession.selectList("datacoreui.menu.selectAccessMenus", accessableMenuRetrieveVO);
	}

	/**
	 * Create menu role
	 * @param menuRoleBaseVO
	 * @return
	 */
	public int createMenuRole(MenuRoleBaseVO menuRoleBaseVO) {
		return sqlSession.insert("datacoreui.menu.createMenuRole", menuRoleBaseVO);
	}

	/**
	 * Update menu role
	 * @param menuRoleBaseVO
	 * @return
	 */
	public int updateMenuRole(MenuRoleBaseVO menuRoleBaseVO) {
		return sqlSession.update("datacoreui.menu.updateMenuRole", menuRoleBaseVO);
	}

	/**
	 * Delete menu role
	 * @param id
	 * @return
	 */
	public int deleteMenuRole(String id) {
		return sqlSession.delete("datacoreui.menu.deleteMenuRole", id);
	}

	/**
	 * Select menu role
	 * @param id
	 * @return
	 */
	public MenuRoleBaseVO getMenuRole(String id) {
		return sqlSession.selectOne("datacoreui.menu.selectMenuRole", id);
	}

	/**
	 * Select list of menu role
	 * @param menuRoleRetrieveVO
	 * @return
	 */
	public List<MenuRoleBaseVO> getMenuRoles(MenuRoleRetrieveVO menuRoleRetrieveVO) {
		return sqlSession.selectList("datacoreui.menu.selectMenuRoles", menuRoleRetrieveVO);
	}

	/**
	 * Create menu role relation
	 * @param menuRoleId
	 * @param menuIds
	 * @return
	 */
	@Transactional
	public int createMenuRoleRelation(String menuRoleId, List<String> menuIds) {
		MenuRoleRelationBaseVO menuRoleRelationBaseVO = new MenuRoleRelationBaseVO();
		menuRoleRelationBaseVO.setMenuRoleId(menuRoleId);
		
		int cnt = 0;
		for(String menuId : menuIds) {
			menuRoleRelationBaseVO.setMenuId(menuId);
			int result = sqlSession.insert("datacoreui.menu.createMenuRoleRelation", menuRoleRelationBaseVO);
			cnt += result;
		}
		
		if(menuIds.size() == cnt) {
			return 1;
		} else {
			return 0;
		}
	}
	
	/**
	 * Update menu role relation
	 * @param menuRoleId
	 * @param menuIds
	 * @return
	 */
	@Transactional
	public int updateMenuRoleRelation(String menuRoleId, List<String> menuIds) {
		// 1. Retrieve menuRoleReleation
		List<MenuRoleRelationBaseVO> menuRoleRelationList = getMenuRoleRelationByRoleId(menuRoleId);
		
		if(menuRoleRelationList == null || menuRoleRelationList.size() < 1) {
			return 0;
		}
		
		// 2. Remove from menuRoleRelation if not present in menuIds
		MenuRoleRelationBaseVO menuRoleRelationBaseVO = new MenuRoleRelationBaseVO();
		menuRoleRelationBaseVO.setMenuRoleId(menuRoleId);
		
		for(MenuRoleRelationBaseVO menuRoleRelation : menuRoleRelationList) {
			boolean isExist = false;
			if(menuRoleRelation == null) {
				break;
			} else if (menuRoleRelation.getMenuId() == null ) {
				continue;
			}
			
			for(String menuId : menuIds) {
				if(menuRoleRelation.getMenuId().equals(menuId)) {
					isExist = true;
					break;
				}
			}
			
			if(!isExist) {
				menuRoleRelationBaseVO.setMenuId(menuRoleRelation.getMenuId());
				sqlSession.delete("datacoreui.menu.deleteMenuRoleRelation", menuRoleRelationBaseVO);
			}
		}
		
		// 3. Register only menuId that does not exist in menuRoleRelation
		for(String menuId : menuIds) {
			boolean isExist = false;
			if(menuId == null) {
				continue;
			}
			
			for(MenuRoleRelationBaseVO menuRoleRelation : menuRoleRelationList) {
				if(menuId.equals(menuRoleRelation.getMenuId())) {
					isExist = true;
					break;
				}
			}
			
			if(!isExist) {
				menuRoleRelationBaseVO.setMenuId(menuId);
				sqlSession.insert("datacoreui.menu.createMenuRoleRelation", menuRoleRelationBaseVO);
			}
		}
		
		return 1;
	}

	/**
	 * Delete menu role relation
	 * @param menuRoleId
	 * @param menuIds
	 * @return
	 */
	@Transactional
	public int deleteMenuRoleRelation(String menuRoleId, List<String> menuIds) {
		MenuRoleRelationBaseVO menuRoleRelationBaseVO = new MenuRoleRelationBaseVO();
		menuRoleRelationBaseVO.setMenuRoleId(menuRoleId);
		
		int cnt = 0;
		for(String menuId : menuIds) {
			menuRoleRelationBaseVO.setMenuId(menuId);
			int result = sqlSession.delete("datacoreui.menu.deleteMenuRoleRelation", menuRoleRelationBaseVO);
			cnt += result;
		}
		
		if(menuIds.size() == cnt) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * Select menu role relation by role ID
	 * @param menuRoleId
	 * @return
	 */
	public List<MenuRoleRelationBaseVO> getMenuRoleRelationByRoleId(String menuRoleId) {
		return sqlSession.selectList("datacoreui.menu.selectMenuRoleRelations", menuRoleId);
	}

}
