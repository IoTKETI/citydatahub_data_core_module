package kr.re.keti.sc.datacoreui.security.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import kr.re.keti.sc.datacoreui.api.menu.service.MenuSVC;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuBaseVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRetrieveVO;
import kr.re.keti.sc.datacoreui.common.component.Properties;
import kr.re.keti.sc.datacoreui.common.service.DataCoreRestSVC;
import kr.re.keti.sc.datacoreui.security.vo.AccessTokenFormVO;
import kr.re.keti.sc.datacoreui.security.vo.RefreshTokenFormVO;
import kr.re.keti.sc.datacoreui.security.vo.UserVO;
import kr.re.keti.sc.datacoreui.util.ConvertUtil;
import kr.re.keti.sc.datacoreui.util.CryptoUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for Data core UI service.
 * @FileName DataCoreUiSVC.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Slf4j
@Service
public class DataCoreUiSVC {
	
	@Autowired
	private Properties properties;
	
	@Autowired
	private DataCoreRestSVC dataCoreRestSVC;
	
	@Autowired
	private MenuSVC menuSVC;
	
	final static String AUTHORIZATION = "Authorization";
	final static String AUTHORIZATION_CODE = "authorization_code";
	final static String CODE = "code";
	final static String STATE = "state";
	final static String ACCESS_TOKEN = "access_token";
	final static String AUTHTOKEN = "authToken";
	final static String REFRESHTOKEN = "refreshtoken";
	final static String REFRESH_TOKEN = "refresh_token";
	final static String CHAUT = "chaut";
	final static Integer COOKIE_MAX_AGE = 60*60*1;	// 1 hours
	
	/**
	 * Create login uri for SSO authentication.
	 * @return	Login uri
	 */
	public String getLoginUri(HttpServletRequest request) {
		String authorizeUri = properties.getUserAuthorizationUri();
		String redirectUri = properties.getRedirectUri();
		String clientId = properties.getClientId();
		String state = "";
		String loginUri = "";
		String sessionId = request.getSession().getId();
		
		try {
			if (sessionId != null) {
				state = CryptoUtil.stringToSHA256(sessionId);
			}
		} catch (NoSuchAlgorithmException e) {
			log.error("Fail to create state.", e);
		}
		
		loginUri = authorizeUri + "?response_type=code"
				+ "&redirect_uri=" + redirectUri
				+ "&client_id=" + clientId
				+ "&state=" + state;
		
		log.debug("getLoginUri() - loginUri:{}", loginUri);
		
		return loginUri;
	}

	/**
	 * Set the access token to the cookie
	 */
	public void getAccessToken(HttpServletRequest request, HttpServletResponse response) {		
		AccessTokenFormVO form = new AccessTokenFormVO();
    	form.setGrant_type(AUTHORIZATION_CODE);
    	form.setCode(request.getParameter(CODE));
    	form.setRedirect_uri(properties.getRedirectUri());
    	form.setClient_id(properties.getClientId());
    	form.setClient_secret(properties.getClientSecret());
    	
    	try {
    		ResponseEntity<String> result = dataCoreRestSVC.post(properties.getAccessTokenUri(), null, null, form, null, String.class);
        	
    		if(result != null && result.getBody() != null) {
    			String tokenJson = result.getBody();
    			setTokenToSessionAndCookie(request, response, tokenJson);
    		}
    	} catch(Exception e) {
    		log.error("Failed to get access_token.", e);
    	}
	}
	
	/**
	 * Get a refresh token from the SSO server.
	 * @return	Success: true, Failed: false
	 */
	public boolean getRefreshToken(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> header = new HashMap<String, String>();
		RefreshTokenFormVO form = new RefreshTokenFormVO();
		String authorization = getAuthorization();
		
		header.put(AUTHORIZATION, "Basic " + authorization);
    	form.setGrant_type(REFRESH_TOKEN);
    	form.setRefresh_token((String) request.getSession().getAttribute(REFRESHTOKEN));
    	
    	try {
    		ResponseEntity<String> result = dataCoreRestSVC.post(properties.getAccessTokenUri(), null, header, form, null, String.class);
        	
        	if(result != null && result.getBody() !=null) {
        		String tokenJson = result.getBody();
        		setTokenToSessionAndCookie(request, response, tokenJson);
        		return true;
        	}
    	} catch(Exception e) {
    		log.error("Failed to get refresh_token.", e);
    		return false;
    	}
    	
    	return false;
	}

	/**
	 * Set token to session and cookie
	 * @param tokenJson		Json type token
	 */
	private void setTokenToSessionAndCookie(HttpServletRequest request, HttpServletResponse response, String tokenJson) {
    	Map<String, Object> tokenMap = ConvertUtil.jsonToMap(tokenJson);
    	String accessToken = (String) tokenMap.get(ACCESS_TOKEN);
		request.getSession().setAttribute(AUTHTOKEN, accessToken);
		request.getSession().setAttribute(REFRESHTOKEN, (String) tokenMap.get(REFRESH_TOKEN));
		
		Cookie setCookie = new Cookie(CHAUT, accessToken);
		setCookie.setPath("/");
		setCookie.setMaxAge(COOKIE_MAX_AGE);
		response.addCookie(setCookie);
	}

	/**
	 * Get public key or JWT
	 * @param jwtToken	Json type token
	 * @return			Public key
	 */
	public ResponseEntity<String> getPublicKey(String jwt) {
		Map<String, String> headers = new HashMap<String, String>();
    	headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
    	
		return dataCoreRestSVC.get(properties.getPublicKeyUri(), null, headers, null, null, String.class);
	}

	/**
	 * For logout, logout is processed to the SSO server and the session and cookie are cleared.
	 * @param object			User ID
	 * @throws JSONException	Throw an exception when a json parsing error occurs.
	 * @throws IOException		Throw an exception when an IO error occurs.
	 */
	public void logout(HttpServletRequest request, HttpServletResponse response, Object object) throws JSONException, IOException {
		Object principal = getPrincipal(request);
		
		if(principal != null) {
			UserVO user = new UserVO();
			
			user.setUserId(principal.toString());
			Map<String, String> headers = new HashMap<String, String>();
	    	headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + request.getSession().getAttribute(AUTHTOKEN));
			
	    	// SSO Logout
			dataCoreRestSVC.post(properties.getLogoutUri(), null, headers, user, null, Void.class);
		} else if (object != null) {
			UserVO user = new UserVO();
			
			user.setUserId(object.toString());
			Map<String, String> headers = new HashMap<String, String>();
	    	headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + request.getSession().getAttribute(AUTHTOKEN));
			
	    	// SSO Logout
			dataCoreRestSVC.post(properties.getLogoutUri(), null, headers, user, null, Void.class);
		}
			
		// Clear cookie and session
		Cookie setCookie = new Cookie(CHAUT, null);
		setCookie.setPath("/");
		setCookie.setMaxAge(0);
		response.addCookie(setCookie);
		request.getSession().invalidate();
		response.sendRedirect("/");
	}

	/**
	 * Get user information
	 * @return		User information
	 */
	public ResponseEntity<UserVO> getUser(HttpServletRequest request) {
		ResponseEntity<UserVO> user = null;
		
		// test data
		if (!properties.getSpringSecurityEnabled()) {
			return ResponseEntity.ok().body(getTestUser());
		}
		
		Object principal = getPrincipal(request);
		
		if(principal != null) {
			String userId = principal.toString();
			String pathUri = "/" + userId;
			Map<String, String> headers = new HashMap<String, String>();
	    	headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + request.getSession().getAttribute(AUTHTOKEN));
	    	
	    	user = dataCoreRestSVC.get(properties.getUserInfoUri(), pathUri, headers, null, null, UserVO.class);
		} else {
			return null;
		}
		
		return ResponseEntity.ok().body(user.getBody());
	}

	/**
	 * Get user ID
	 * @return	User ID
	 */
	public ResponseEntity<String> getUserId(HttpServletRequest request) {
		
		// test data
		if (!properties.getSpringSecurityEnabled()) {
			return ResponseEntity.ok().body("cityhub10");
		}
		
		Object securityContextObject = request.getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
		if (securityContextObject != null) {
			SecurityContext securityContext = (SecurityContext) securityContextObject;
			Authentication authentication = securityContext.getAuthentication();
			if(authentication !=null && authentication.getPrincipal() != null) {
				return ResponseEntity.ok().body(authentication.getPrincipal().toString());
			}
		}
		
		return ResponseEntity.badRequest().build();
	}
	
	/**
	 * Get principal information from request
	 * @return	Principal
	 */
	public Object getPrincipal(HttpServletRequest request) {
		Object securityContextObject = request.getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
		
		if (securityContextObject != null) {
			SecurityContext securityContext = (SecurityContext) securityContextObject;
			Authentication authentication = securityContext.getAuthentication();
			
			if(authentication !=null && authentication.getPrincipal() !=null) {
				return authentication.getPrincipal();
			}
		}
		
		return null;
	}
	
	/**
	 * Test user data
	 * @return	Test user
	 */
	private UserVO getTestUser() {
		UserVO user = new UserVO();
		user.setUserId("cityhub10");
		user.setName("홍길동");
		user.setNickname("홍길동");
		user.setPhone("010-1234-5678");
		
		return user;
	}

	/**
	 * Respond to the list of accessible menus.
	 * @return	List of accessible menus
	 */
	public ResponseEntity<List<MenuBaseVO>> getAccessMenu(HttpServletRequest request, String langCd) {
		if (!properties.getSpringSecurityEnabled()) {
			MenuRetrieveVO menuRetrieveVO = new MenuRetrieveVO();
			menuRetrieveVO.setEnabled(true);
			menuRetrieveVO.setLangCd(langCd);
			return menuSVC.getMenus(menuRetrieveVO);
		}
		
		Object securityContextObject = request.getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);		

		if (securityContextObject != null) {
			SecurityContext securityContext = (SecurityContext) securityContextObject;
			Authentication authentication = securityContext.getAuthentication();
			
			if(authentication !=null && authentication.getPrincipal() !=null) {
				for(GrantedAuthority authority : authentication.getAuthorities()) {
					ResponseEntity<List<MenuBaseVO>> reslt = menuSVC.getAccessMenus(authority.getAuthority(), langCd);
					if(reslt != null 
							&& reslt.getBody() != null
							&& reslt.getBody().size() > 0) {
						return reslt;
					}
				};
			}
		}
		
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).build();
	}
	
	/**
	 * Get information of authorization
	 * @return	Authorization information
	 */
	private String getAuthorization() {
		Encoder encoder = Base64.getEncoder();
		String authorization = properties.getClientId() + ":" + properties.getClientSecret();

		return encoder.encodeToString(authorization.getBytes());
	}
}
