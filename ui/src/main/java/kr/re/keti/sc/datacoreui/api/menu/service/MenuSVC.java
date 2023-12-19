package kr.re.keti.sc.datacoreui.api.menu.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import kr.re.keti.sc.datacoreui.api.menu.dao.MenuDAO;
import kr.re.keti.sc.datacoreui.api.menu.vo.AccessableMenuRetrieveVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuBaseVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuIdVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRetrieveVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRoleBaseVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRoleRelationBaseVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRoleRelationResponseVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRoleRetrieveVO;
import lombok.extern.slf4j.Slf4j;

/**
 * A service class for menu management API calls.
 * @FileName MenuSVC.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Slf4j
@Service
public class MenuSVC {
	
	@Autowired
	private MenuDAO menuDAO;
	
	/**
	 * Create menu
	 * @param menuBaseVO	MenuBaseVO
	 * @return				Result of UI menu creation.
	 */
	public <T> ResponseEntity<T> createMenu(MenuBaseVO menuBaseVO) {
		if(menuBaseVO.getEnabled() == null) {
			menuBaseVO.setEnabled(true);
		}
		// Set the default language code to en(English)
		if(menuBaseVO.getLangCd() == null) {
			menuBaseVO.setLangCd("en");
		}
		
		try {
			menuDAO.createMenu(menuBaseVO);
		} catch(DuplicateKeyException e) {
			log.warn("Duplicate Menu Id. Id: {}", menuBaseVO.getId(), e);
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch(Exception e) {
			log.error("Fail to createMenu.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
				
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * Update menu
	 * @param id			Menu ID
	 * @param menuBaseVO	MenuBaseVO
	 * @return				Result of update UI menu.
	 */
	public <T> ResponseEntity<T> updateMenu(String id, MenuBaseVO menuBaseVO) {
		menuBaseVO.setId(id);

		try {
			menuDAO.updateMenu(menuBaseVO);
		} catch(Exception e) {
			log.error("Fail to updateMenu.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * Delete menu
	 * @param id	Menu ID
	 * @return		Result of delete UI menu.
	 */
	public <T> ResponseEntity<T> deleteMenu(String id, String langCd) {
		MenuRetrieveVO menuRetrieveVO = new MenuRetrieveVO();
		menuRetrieveVO.setId(id);
		menuRetrieveVO.setLangCd(langCd);
		
		try {
			menuDAO.deleteMenu(menuRetrieveVO);
		} catch(Exception e) {
			log.error("Fail to deleteMenu.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * Retrieve menu
	 * @param id	Menu ID
	 * @return		Menu information retrieved by menu ID.
	 */
	public ResponseEntity<MenuBaseVO> getMenu(String id, String langCd) {
		MenuBaseVO result = null;
		MenuRetrieveVO menuRetrieveVO = new MenuRetrieveVO();
		menuRetrieveVO.setId(id);
		menuRetrieveVO.setLangCd(langCd);
		
		try {
			result = menuDAO.getMenu(menuRetrieveVO);
		} catch(Exception e) {
			log.error("Fail to getMenu.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		if(result == null) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		return ResponseEntity.ok().body(result);
	}

	/**
	 * Retrieve list of menu
	 * @param menuRetrieveVO	MenuRetrieveVO (menu search condition)
	 * @return					List of menu information retrieved by MenuRetrieveVO.
	 */
	public ResponseEntity<List<MenuBaseVO>> getMenus(MenuRetrieveVO menuRetrieveVO) {
		List<MenuBaseVO> result = null;
		
		try {
			result = menuDAO.getMenus(menuRetrieveVO);
		} catch(Exception e) {
			log.error("Fail to getMenus.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		if(result == null || result.size() < 1) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		return ResponseEntity.ok().body(result);
	}
	
	/**
	 * Retrieve list of access menu
	 * @param menuRoleId	Menu role ID
	 * @return				List of menu information retrieved by menu role ID.
	 */
	public ResponseEntity<List<MenuBaseVO>> getAccessMenus(String menuRoleId, String langCd) {
		List<MenuBaseVO> result = null;
		AccessableMenuRetrieveVO accessableMenuRetrieveVO = new AccessableMenuRetrieveVO();
		
		accessableMenuRetrieveVO.setMenuRoleId(menuRoleId);
		accessableMenuRetrieveVO.setLangCd(langCd);
		
		try {
			result = menuDAO.getAccessMenus(accessableMenuRetrieveVO);
		} catch(Exception e) {
			log.error("Fail to getMenus.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		if(result == null || result.size() < 1) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		return ResponseEntity.ok().body(result);
	}

	/**
	 * Create menu role
	 * @param menuRoleBaseVO	MenuRoleBaseVO
	 * @return					Result of menu role creation.
	 */
	public <T> ResponseEntity<T> createMenuRole(MenuRoleBaseVO menuRoleBaseVO) {
		if(menuRoleBaseVO.getEnabled() == null) {
			menuRoleBaseVO.setEnabled(true);
		}
		
		try {
			menuDAO.createMenuRole(menuRoleBaseVO);
		} catch(DuplicateKeyException e) {
			log.warn("Duplicate MenuRole Id. Id: {}", menuRoleBaseVO.getId(), e);
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch(Exception e) {
			log.error("Fail to createMenuRole.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
				
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * Update menu role
	 * @param id				Menu role ID
	 * @param menuRoleBaseVO	MenuRoleBaseVO
	 * @return					Result of update menu role.
	 */
	public <T> ResponseEntity<T> updateMenuRole(String id, MenuRoleBaseVO menuRoleBaseVO) {
		menuRoleBaseVO.setId(id);

		try {
			menuDAO.updateMenuRole(menuRoleBaseVO);
		} catch(Exception e) {
			log.error("Fail to updateMenuRole.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * Delete menu role
	 * @param id	Menu role ID
	 * @return		Result of delete menu role.
	 */
	public <T> ResponseEntity<T> deleteMenuRole(String id) {
		try {
			menuDAO.deleteMenuRole(id);
		} catch(Exception e) {
			log.error("Fail to deleteMenuRole.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * Retrieve menu role
	 * @param id	Menu role ID
	 * @return		Menu role retrieved by menu role ID.
	 */
	public ResponseEntity<MenuRoleBaseVO> getMenuRole(String id) {
		MenuRoleBaseVO result = null;
		
		try {
			result = menuDAO.getMenuRole(id);
		} catch(Exception e) {
			log.error("Fail to getMenuRole.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		if(result == null) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		return ResponseEntity.ok().body(result);
	}

	/**
	 * Retrieve list of menu role
	 * @param menuRoleRetrieveVO	MenuRoleRetrieveVO
	 * @return						List of menu role retrieved by MenuRoleRetrieveVO.
	 */
	public ResponseEntity<List<MenuRoleBaseVO>> getMenuRoles(MenuRoleRetrieveVO menuRoleRetrieveVO) {
		List<MenuRoleBaseVO> result = null;
		
		try {
			result = menuDAO.getMenuRoles(menuRoleRetrieveVO);
		} catch(Exception e) {
			log.error("Fail to getMenuRoles.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		if(result == null || result.size() < 1) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		return ResponseEntity.ok().body(result);
	}

	/**
	 * Create menu role relation
	 * @param menuRoleId	Menu role ID
	 * @param menuIds		List of menu ID
	 * @return				Result of create menu role relation.
	 */
	public <T> ResponseEntity<T> createMenuRoleRelation(String menuRoleId, List<String> menuIds) {	
		try {
			menuDAO.createMenuRoleRelation(menuRoleId, menuIds);
		} catch(DuplicateKeyException e) {
			log.warn("Duplicate MenuRoleRelation. menuRoleId: {}", menuRoleId, e);
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch(Exception e) {
			log.error("Fail to createMenuRoleRelation.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
				
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * Update menu role relation
	 * @param menuRoleId	Menu role ID
	 * @param menuIds		List of menu ID
	 * @return				Result of update menu role relation.
	 */
	public <T> ResponseEntity<T> updateMenuRoleRelation(String menuRoleId, List<String> menuIds) {
		try {
			menuDAO.updateMenuRoleRelation(menuRoleId, menuIds);
		} catch(Exception e) {
			log.error("Fail to updateMenuRoleRelation.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * Delete menu role relation
	 * @param menuRoleId	Menu role ID
	 * @param menuIds		List of menu ID
	 * @return				Result of delete menu role relation.
	 */
	public <T> ResponseEntity<T> deleteMenuRoleRelation(String menuRoleId, List<String> menuIds) {
		try {
			menuDAO.deleteMenuRoleRelation(menuRoleId, menuIds);
		} catch(Exception e) {
			log.error("Fail to deleteMenuRoleRelation.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * Retrieve menu role relation by role ID
	 * @param menuRoleId	Menu role ID
	 * @return				Menu role relation retrieved by menu role ID.
	 */
	public ResponseEntity<MenuRoleRelationResponseVO> getMenuRoleRelationByRoleId(String menuRoleId) {
		MenuRoleRelationResponseVO result = new MenuRoleRelationResponseVO();
		List<MenuBaseVO> menuList = null;
		List<MenuRoleRelationBaseVO> menuRoleRelationList = null;
		
		try {
			// 1. Retrieve all menu
			menuList = menuDAO.getMenus(new MenuRetrieveVO());

			// 2. Retrieve menu role relationships
			menuRoleRelationList = menuDAO.getMenuRoleRelationByRoleId(menuRoleId);
			
			if(menuRoleRelationList == null || menuRoleRelationList.size() < 1) {
				return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
			}
			
			// 3. Classifying menus with and without privileges
			List<MenuIdVO> roleAssign = new ArrayList<MenuIdVO>();
			List<MenuIdVO> noRoleAssign = new ArrayList<MenuIdVO>();
			boolean isAssigned = false;
			for(MenuBaseVO menu : menuList) {
				MenuIdVO menuIdVO = new MenuIdVO();
				isAssigned = false;
				menuIdVO.setId(menu.getId());
				menuIdVO.setName(menu.getName());
				
				for (MenuRoleRelationBaseVO menuRoleRelation : menuRoleRelationList) {
					if(menuRoleRelation.getMenuId().equals(menu.getId())) {
						roleAssign.add(menuIdVO);
						isAssigned = true;
					}
				}
				if(!isAssigned) {
					noRoleAssign.add(menuIdVO);
				}
			}
			
			result.setRoleAssignMenu(roleAssign);
			result.setNoRoleAssignMenu(noRoleAssign);
		} catch(Exception e) {
			log.error("Fail to getMenuRoleRelation.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.ok().body(result);
	}
}
