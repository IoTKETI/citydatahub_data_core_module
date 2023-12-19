package kr.re.keti.sc.datacoreusertool.security.filter;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import kr.re.keti.sc.datacoreusertool.security.exception.JwtAuthentioncationException;
import kr.re.keti.sc.datacoreusertool.security.exception.JwtAuthrorizationException;

/**
 * Class for JWT authorization filter.
 * @FileName JwtAuthorizationFilter.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
public class JwtAuthorizationFilter extends OncePerRequestFilter {
	private AuthenticationEntryPoint entryPoint;

	/**
	 * Constructor of JwtAuthorizationFilter class
	 * @param entryPoint	AuthenticationEntryPoint
	 */
    public JwtAuthorizationFilter(AuthenticationEntryPoint entryPoint) {
        this.entryPoint = entryPoint;
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

            if (!isAccessible(authentication.getAuthorities(), request)) {
                throw new JwtAuthrorizationException(authentication.getPrincipal() + " has not role about the request");
            }

            chain.doFilter(request, response);

        } catch (AuthenticationException e) {
            entryPoint.commence(request, response, e);
        }
    }
    
    private static final SimpleGrantedAuthority adminRole = new SimpleGrantedAuthority("Core_Admin");
    private static final SimpleGrantedAuthority userRole = new SimpleGrantedAuthority("Marketplace_User");

    /**
     * Check whether the user has access.
     * @param roles		GrantedAuthority
     * @return			accessible: true, no accessible: false
     */
    private boolean isAccessible(Collection<? extends GrantedAuthority> roles, HttpServletRequest request) {
        if (roles.contains(adminRole) || roles.contains(userRole)) {
            return true;
        }
        
        return false;
    }
}
