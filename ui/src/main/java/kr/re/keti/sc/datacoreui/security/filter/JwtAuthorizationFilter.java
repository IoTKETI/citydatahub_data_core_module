package kr.re.keti.sc.datacoreui.security.filter;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import kr.re.keti.sc.datacoreui.api.menu.service.MenuSVC;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRoleBaseVO;
import kr.re.keti.sc.datacoreui.api.menu.vo.MenuRoleRelationResponseVO;
import kr.re.keti.sc.datacoreui.security.exception.JwtAuthentioncationException;
import kr.re.keti.sc.datacoreui.security.exception.JwtAuthorizationException;
import kr.re.keti.sc.datacoreui.security.service.DataCoreUiSVC;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for JWT authorization filter.
 * @FileName JwtAuthorizationFilter.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {
	private AuthenticationEntryPoint entryPoint;
	
    private MenuSVC menuSVC;
    
    private DataCoreUiSVC dataCoreUiSVC;

    /**
     * Constructor of JwtAuthorizationFilter class
     * @param entryPoint		AuthenticationEntryPoint
     * @param menuSVC			MenuSVC class
     * @param dataCoreUiSVC		DataCoreUiSVC class
     */
    public JwtAuthorizationFilter(AuthenticationEntryPoint entryPoint, MenuSVC menuSVC, DataCoreUiSVC dataCoreUiSVC) {
        this.entryPoint = entryPoint;
        this.menuSVC = menuSVC;
        this.dataCoreUiSVC = dataCoreUiSVC;
    }

    /**
     * Check authentication
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                throw new JwtAuthentioncationException("JwtAuthorizationFilter : No Authentication Exist");
            }

            if (!isAccessible(authentication.getAuthorities(), request, response)) {
            	dataCoreUiSVC.logout(request, response, authentication.getPrincipal());
                throw new JwtAuthorizationException(authentication.getPrincipal() + " has not role about the request");
            }

            chain.doFilter(request, response);

        } catch (AuthenticationException e) {
            entryPoint.commence(request, response, e);
        } catch (JSONException e) {
			log.error("Logout failed.", e);
		}
    }

    /**
     * Check whether the user has access.
     * @param roles		GrantedAuthority
     * @return			accessible: true, no accessible: false
     */
    private boolean isAccessible(Collection<? extends GrantedAuthority> roles, HttpServletRequest request, HttpServletResponse response) {
        for (GrantedAuthority role : roles) {
        	ResponseEntity<MenuRoleBaseVO> menuRoleBase = menuSVC.getMenuRole(role.getAuthority());
        	
        	if (menuRoleBase != null && !ValidateUtil.isEmptyData(menuRoleBase.getBody())) {
        		ResponseEntity<MenuRoleRelationResponseVO> menuRoleRelationVO = menuSVC.getMenuRoleRelationByRoleId(role.getAuthority());
        		if (menuRoleRelationVO == null || ValidateUtil.isEmptyData(menuRoleRelationVO.getBody())) {
        			log.info("{} is and unauthorized user", role.getAuthority());
        			
        			return false;
        		}
        		
        		return true;
        	}
        }
        return false;
    }
}
