package kr.re.keti.sc.datacoreusertool.controller.web;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.re.keti.sc.datacoreusertool.common.component.Properties;
import kr.re.keti.sc.datacoreusertool.security.service.UserToolSecuritySVC;

/**
 * Class of data core view controller
 * @FileName DataCoreViewController.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Controller
public class DataCoreViewController implements ErrorController {
	
	@Autowired
	private UserToolSecuritySVC userToolSecuritySVC;
	
	@Autowired
	private Properties properties;
	
	final static String LANG_CD = "langCd";
	
	/**
     * When the page is refreshed In spa development, 
     * refreshing the screen means server-side rendering, 
     * so you need to convert the screen to index.html.
     * @return String
     * @throws IOException 
     */
	@GetMapping({ "/", "/error" })
    public String redirectRoot(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
		Cookie setCookie = new Cookie(LANG_CD, properties.getLanguageCode());
		setCookie.setPath("/");
		response.addCookie(setCookie);
		
        return "index.html";
    }
	
	/**
     * When requesting accesstoken url, it responds by obtaining a token.
     * @throws IOException	Throw an exception when an error occurs.
     */
	@GetMapping("/accesstoken")
	@ResponseBody
    public void getAccessToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
    	userToolSecuritySVC.getAccessToken(request, response);
    	
		String contextPath = request.getContextPath();
		response.sendRedirect(contextPath + "/");
    }
	
	/**
     * Responds to user ID when requesting user url.
     * @return				User ID
     * @throws Exception	Throw an exception when an error occurs.
     */
	@GetMapping("/userId")
    public ResponseEntity<String> getUserId(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	return userToolSecuritySVC.getUserId(request);
    }
    
	/**
     * Logout processing when requesting logout url.
     * @return				Http status
     * @throws Exception	Throw an exception when an IO error occurs.
     */
    @GetMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	userToolSecuritySVC.logout(request, response);
    	
    	return ResponseEntity.ok().build();
    }
    
    /**
     * Get google api key
     * @return				Google api key
     * @throws Exception	Throw an exception when an IO error occurs.
     */
    @GetMapping("/getapikey")
    public ResponseEntity<String> getApiKey(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	return ResponseEntity.ok().body(properties.getGoogleApiKey());
    }

    /**
     * Response error page
     */
    @Override
    public String getErrorPath() {
        return "/error";
    }
}
