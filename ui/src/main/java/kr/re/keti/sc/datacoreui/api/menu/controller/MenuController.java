package kr.re.keti.sc.datacoreui.api.menu.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.re.keti.sc.datacoreui.api.menu.service.MenuSVC;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuBaseVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRetrieveVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRoleBaseVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRoleRelationResponseVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRoleRetrieveVO;
import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode.ErrorCode;
import kr.re.keti.sc.datacoreui.common.component.Properties;
import kr.re.keti.sc.datacoreui.common.exception.BadRequestException;
import kr.re.keti.sc.datacoreui.security.service.DataCoreUiSVC;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for menu management API calls.
 * @FileName MenuController.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Slf4j
@RestController
public class MenuController {
	
	@Autowired
	private MenuSVC menuSVC;
	
	@Autowired
	private DataCoreUiSVC dataCoreUiSVC;
	
	@Autowired
	private Properties properties;

	/**
	 * Create UI menu
	 * @param menuBaseVO 	MenuBase VO
	 * @return				Result of UI menu creation.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PostMapping(value="/menu")
	public <T> ResponseEntity<T> createMenu(HttpServletRequest request, HttpServletResponse response,
			@RequestBody MenuBaseVO menuBaseVO) throws Exception {
		
		log.info("[UI API] createMenu - menuBaseVO: {}", menuBaseVO);
		
		// 1. Check required values
		if(!checkMandatoryMenuValues(menuBaseVO)) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST, "Missing required value.");
		}
		
		// 2. Set the creator information
		Object principal = dataCoreUiSVC.getPrincipal(request);
		if (principal != null) {
			menuBaseVO.setCreatorId(principal.toString());
		}
		
		if (menuBaseVO.getLangCd() == null) {
			menuBaseVO.setLangCd(properties.getLangCd());
		}
		
		// 3. Create menu
		ResponseEntity<T> reslt = menuSVC.createMenu(menuBaseVO);
		
		return reslt;
	}
	
	/**
	 * Update UI menu
	 * @param id        	Menu ID
	 * @param menuBaseVO 	MenuBaseVO object (update data)
	 * @return				Result of update UI menu.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PatchMapping(value="/menu/{id}")
	public <T> ResponseEntity<T> updateMenu(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String id,
			@RequestBody MenuBaseVO menuBaseVO) throws Exception {
		log.info("[UI API] updateMenu - id: {}, menuBaseVO: {}", id, menuBaseVO);
		
		// 1. Set the modifier information
		Object principal = dataCoreUiSVC.getPrincipal(request);
		if (principal != null) {
			menuBaseVO.setModifierId(principal.toString());
		}
		
		// 2. Update menu
		ResponseEntity<T> reslt = menuSVC.updateMenu(id, menuBaseVO);
		
		return reslt;
	}
	
	/**
	 * Delete UI menu
	 * @param id          	Menu id
	 * @return				Result of delete UI menu.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@DeleteMapping(value="/menu/{id}")
	public <T> ResponseEntity<T> deleteMenu(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String id, 
			@RequestParam(value="langCd", required=false) String langCd) throws Exception {
		log.info("[UI API] deleteMenu - id: {}, langCd: {}", id, langCd);
		
		if (langCd == null) {
			langCd = properties.getLangCd();
		}
		
		// 1. Delete menu
		ResponseEntity<T> reslt = menuSVC.deleteMenu(id, langCd);
		
		return reslt;
	}
	
	/**
	 * Retrieve menu
	 * @param id        	Menu ID
	 * @return				UI menu information retrieved by menu ID.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/menu/{id}")
	public ResponseEntity<MenuBaseVO> getMenu(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String id, 
			@RequestParam(value="langCd", required=false) String langCd) throws Exception {
		
		log.info("[UI API] getMenu - id: {}, langCd: {}", id, langCd);
		
		if (langCd == null) {
			langCd = properties.getLangCd();
		}
		
		// 1. Retrieve menu
		ResponseEntity<MenuBaseVO> reslt = menuSVC.getMenu(id, langCd);
		
		return reslt;
	}
	
	/** 
	 * Retrieve multiple menu
	 * @param menuRetrieveVO	MenuRetrieve VO
	 * @return					List of UI menu retrieved by MenuRetrieveVO.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@GetMapping(value="/menu")
	public ResponseEntity<List<MenuBaseVO>> getMenus(HttpServletRequest request, HttpServletResponse response,
			MenuRetrieveVO menuRetrieveVO) throws Exception {
		log.info("[UI API] getMenu - menuRetrieveVO: {}", menuRetrieveVO);
		
		if (menuRetrieveVO == null) {
			menuRetrieveVO = new MenuRetrieveVO();
		}
		
		//TODO: client must fill in langCd if necessary
		if (menuRetrieveVO.getLangCd() == null) {
			menuRetrieveVO.setLangCd(properties.getLangCd());
		}
		
		// 1. Retrieve multiple menu
		ResponseEntity<List<MenuBaseVO>> reslt = menuSVC.getMenus(menuRetrieveVO);
		
		return reslt;
	}
	
	/**
	 * Create menu role
	 * @param menuRoleBaseVO	MenuRoleBase VO
	 * @return					Result of UI menu role creation.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@PostMapping(value="/menurole")
	public <T> ResponseEntity<T> createMenuRole(HttpServletRequest request, HttpServletResponse response,
			@RequestBody MenuRoleBaseVO menuRoleBaseVO) throws Exception {
		
		log.info("[UI API] createMenuRole - menuRoleBaseVO: {}", menuRoleBaseVO);
		
		// 1. Check required values
		if(!checkMandatoryMenuRoleValues(menuRoleBaseVO)) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST, "Missing required value.");
		}
		
		// 2. Set the creator information
		Object principal = dataCoreUiSVC.getPrincipal(request);
		if (principal != null) {
			menuRoleBaseVO.setCreatorId(principal.toString());
		}
		
		// 3. Create menu role
		ResponseEntity<T> reslt = menuSVC.createMenuRole(menuRoleBaseVO);
		
		return reslt;
	}
	
	/**
	 * Update menu role
	 * @param id               	Menu role ID
	 * @param menuRoleBaseVO   	MenuRoleBaseVO object (update data)
	 * @return					Result of update UI menu role.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@PatchMapping(value="/menurole/{id}")
	public <T> ResponseEntity<T> updateMenuRole(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String id,
			@RequestBody MenuRoleBaseVO menuRoleBaseVO) throws Exception {
		log.info("[UI API] updateMenuRole - id: {}, menuRoleBaseVO: {}", id, menuRoleBaseVO);
		
		// 1. Set the modifier information
		Object principal = dataCoreUiSVC.getPrincipal(request);
		if (principal != null) {
			menuRoleBaseVO.setModifierId(principal.toString());
		}
		
		// 2. Update menu role
		ResponseEntity<T> reslt = menuSVC.updateMenuRole(id, menuRoleBaseVO);
		
		return reslt;
	}
	
	/**
	 * Delete menu role
	 * @param id          	Menu role ID
	 * @return				Result of delete menu role.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@DeleteMapping(value="/menurole/{id}")
	public <T> ResponseEntity<T> deleteMenuRole(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String id) throws Exception {
		log.info("[UI API] deleteMenuRole - id: {}", id);
		
		// 1. Delete menu role
		ResponseEntity<T> reslt = menuSVC.deleteMenuRole(id);
		
		return reslt;
	}
	
	/**
	 * Retrieve menu role
	 * @param id           	Menu role ID
	 * @return				UI menu role retrieved by menu role id.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/menurole/{id}")
	public ResponseEntity<MenuRoleBaseVO> getMenuRole(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String id) throws Exception {
		log.info("[UI API] getMenuRole - id: {}", id);
		
		// 1. Retrieve menu role
		ResponseEntity<MenuRoleBaseVO> reslt = menuSVC.getMenuRole(id);
		
		return reslt;
	}
	
	/**
	 * Retrieve multiple menu role
	 * @param menuRoleRetrieveVO   	MenuRoleRetrieveVO object
	 * @return						List of UI menu role retrieved by MenuRoleRetrieveVO.
	 * @throws Exception			Throw an exception when an error occurs.
	 */
	@GetMapping(value="/menurole")
	public ResponseEntity<List<MenuRoleBaseVO>> getMenuRoles(HttpServletRequest request, HttpServletResponse response,
			MenuRoleRetrieveVO menuRoleRetrieveVO) throws Exception {
		log.info("[UI API] getMenuRoles - menuRoleRetrieveVO: {}", menuRoleRetrieveVO);
		
		// 1. Retrieve multiple menu role
		ResponseEntity<List<MenuRoleBaseVO>> reslt = menuSVC.getMenuRoles(menuRoleRetrieveVO);
		
		return reslt;
	}

	/**
	 * Create the menu role relationship
	 * @param menuRoleId    Menu role ID
	 * @param menuId        List of menu ID
	 * @return				Result of UI menu role relation creation.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PostMapping(value="/menurolrelation/{menuRoleId}")
	public <T> ResponseEntity<T> createMenuRoleRelation(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String menuRoleId,
			@RequestBody List<String> menuIds) throws Exception {
		
		log.info("[UI API] createMenuRoleRelation - menuRoleId: {}, menuIds: {}", menuRoleId, menuIds);
		
		// 1. Create the menu role relationship
		ResponseEntity<T> reslt = menuSVC.createMenuRoleRelation(menuRoleId, menuIds);
		
		return reslt;
	}
	
	/**
	 * Update the menu role relationship
	 * @param menuRoleId    Menu role ID
	 * @param menuId        List of menu ID (update data)
	 * @return				Result of update menu role relation.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@PatchMapping(value="/menurolrelation/{menuRoleId}")
	public <T> ResponseEntity<T> updateMenuRoleRelation(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String menuRoleId,
			@RequestBody List<String> menuIds) throws Exception {
		log.info("[UI API] updateMenuRoleRelation - menuRoleId: {}, menuIds: {}", menuRoleId, menuIds);
		
		ResponseEntity<T> reslt = menuSVC.updateMenuRoleRelation(menuRoleId, menuIds);
		
		return reslt;
	}
	
	/**
	 * Delete the menu role relationship
	 * @param menuRoleId    Menu role ID
	 * @param menuId        Menu ID
	 * @return				Result of delete menu role relation.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@DeleteMapping(value="/menurolrelation/{menuRoleId}")
	public <T> ResponseEntity<T> deleteMenuRoleRelation(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String menuRoleId,
			@RequestBody List<String> menuIds) throws Exception {
		log.info("[UI API] deleteMenuRoleRelation - menuRoleId: {}, menuId: {}", menuRoleId, menuIds);
		
		ResponseEntity<T> reslt = menuSVC.deleteMenuRoleRelation(menuRoleId, menuIds);
		
		return reslt;
	}
		
	/**
	 * Retrieve the menu role relationship (by menu role ID)
	 * @param menuRoleId    Menu role ID
	 * @return				Menu role relation retrieved by menu role ID.
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/menurolrelation/{menuRoleId}")
	public ResponseEntity<MenuRoleRelationResponseVO> getMenuRoleRelationByRoleId(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String menuRoleId) throws Exception {
		log.info("[UI API] getMenuRoleRelation - menuRoleId: {}", menuRoleId);
		
		ResponseEntity<MenuRoleRelationResponseVO> reslt = menuSVC.getMenuRoleRelationByRoleId(menuRoleId);
		
		return reslt;
	}

	/**
	 * Check menu required values
	 * @param 	menuBaseVO
	 * @return	valid: true, invalid: false
	 */
	private boolean checkMandatoryMenuValues(MenuBaseVO menuBaseVO) {
		if(menuBaseVO == null) {
			return false;
		}
		
		if(ValidateUtil.isEmptyData(menuBaseVO.getId())
				|| ValidateUtil.isEmptyData(menuBaseVO.getName())
				|| ValidateUtil.isEmptyData(menuBaseVO.getLevel()))
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check menu role required values
	 * @param 	menuRoleBaseVO
	 * @return	valid: true, invalid: false
	 */
	private boolean checkMandatoryMenuRoleValues(MenuRoleBaseVO menuRoleBaseVO) {
		if(menuRoleBaseVO == null) {
			return false;
		}
		
		if(ValidateUtil.isEmptyData(menuRoleBaseVO.getId())
				|| ValidateUtil.isEmptyData(menuRoleBaseVO.getName())) {
			return false;
		}
		
		return true;
	}
}
