package kr.re.keti.sc.datacoreui.controller.web;

import java.io.IOException;
import java.util.List;

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

import kr.re.keti.sc.datacoreui.api.menu.vo.MenuBaseVO;
import kr.re.keti.sc.datacoreui.common.component.Properties;
import kr.re.keti.sc.datacoreui.security.service.DataCoreUiSVC;
import kr.re.keti.sc.datacoreui.security.vo.UserVO;

/**
 * Class of data core view controller
 * @FileName DataCoreViewController.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Controller
public class DataCoreViewController implements ErrorController {
	
	@Autowired 
	DataCoreUiSVC dataCoreUiSVC;
	
	@Autowired
	private Properties properties;
	
	final static String LANG_CD = "langCd";
	
    /**
     * When the page is refreshed In spa development, 
     * refreshing the screen means server-side rendering, 
     * so you need to convert the screen to index.html.
     * @return String		html page name
     * @throws IOException 	Throw an exception when an IO error occurs.
     */
    @GetMapping({ "/", "/error" })
    public String redirectRoot(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
    	Cookie setCookie = new Cookie(LANG_CD, properties.getLangCd());
		setCookie.setPath("/");
		response.addCookie(setCookie);
		
        return "index.html";
    }

    /**
     * When accessing dataModels url, the screen must be converted to index.html.
     * @return				html page name
     * @throws IOException	Throw an exception when an IO error occurs.
     */
    @GetMapping("/dataModels")
    public String dataModels(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return "index.html";
    }

    /**
     * When accessing the datasetView url, you need to convert the screen to index.html.
     * @return String		html page name
     * @throws IOException	Throw an exception when an IO error occurs.
     */
    @GetMapping("/datasetView")
    public String datasetView(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return "index.html";
    }

    /**
     * When accessing the provisionServerView url, you need to convert the screen to index.html.
     * @return String		html page name
     * @throws IOException	Throw an exception when an IO error occurs.
     */
    @GetMapping("/provisionServerView")
    public String provisionServerView(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return "index.html";
    }

    /**
     * When accessing the externalPlatformView url, you need to convert the screen to index.html.
     * @return String		html page name
     * @throws IOException	Throw an exception when an IO error occurs.
     */
    @GetMapping("/externalPlatformView")
    public String externalPlatformView(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return "index.html";
    }

    /**
     * When accessing the verificationHistoryView url, you need to convert the screen to index.html.
     * @return String		html page name
     * @throws IOException	Throw an exception when an IO error occurs.
     */
    @GetMapping("/verificationHistoryView")
    public String verificationHistoryView(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return "index.html";
    }
    
    /**
     * When requesting accesstoken url, it responds by obtaining a token.
     * @throws IOException	Throw an exception when an error occurs.
     */
    @GetMapping("/accesstoken")
    public @ResponseBody void getAccessToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
    	dataCoreUiSVC.getAccessToken(request, response);
    	
		String contextPath = request.getContextPath();
		response.sendRedirect(contextPath + "/");
    }
    
    /**
     * When requesting accessmenu url, it responds with a list of accessible menus.
     * @return				List of menu
     * @throws IOException	Throw an exception when an IO error occurs.
     */
    @GetMapping("/accessmenu")
    public ResponseEntity<List<MenuBaseVO>> getAccessMenu(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
    	String langCd = properties.getLangCd();
    	// default English
    	if(langCd == null) {
    		langCd = "en";
    	}
    	
    	return dataCoreUiSVC.getAccessMenu(request, langCd);
    }
    
    /**
     * Responds to user information when requesting user url.
     * @return				User information
     * @throws Exception	Throw an exception when an error occurs.
     */
    @GetMapping("/user")
    public ResponseEntity<UserVO> getUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	return dataCoreUiSVC.getUser(request);
    }
    
    /**
     * Responds to user ID when requesting user url.
     * @return				User ID
     * @throws Exception	Throw an exception when an error occurs.
     */
    @GetMapping("/userId")
    public ResponseEntity<String> getUserId(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	return dataCoreUiSVC.getUserId(request);
    }
    
    /**
     * Logout processing when requesting logout url.
     * @return				Http status
     * @throws Exception	Throw an exception when an IO error occurs.
     */
    @GetMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	dataCoreUiSVC.logout(request, response, null);
    	
    	return ResponseEntity.ok().build();
    }

    /**
     * Response error page
     */
    @Override
    public String getErrorPath() {
        return "/error";
    }
}
