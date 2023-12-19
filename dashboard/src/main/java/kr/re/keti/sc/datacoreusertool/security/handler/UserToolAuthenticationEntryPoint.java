package kr.re.keti.sc.datacoreusertool.security.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import kr.re.keti.sc.datacoreusertool.security.exception.JwtAuthentioncationException;
import kr.re.keti.sc.datacoreusertool.security.exception.JwtAuthrorizationException;

/**
 * Data Core User Tool authentication entry point
 * @FileName UserToolAuthenticationEntryPoint.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
public class UserToolAuthenticationEntryPoint implements AuthenticationEntryPoint {
	
	private final Logger logger = LoggerFactory.getLogger(UserToolAuthenticationEntryPoint.class);

	/**
	 * Authentication commence
	 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		logger.warn("Auth Failed", authException);
        if (authException instanceof JwtAuthentioncationException) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        } else if (authException instanceof JwtAuthrorizationException) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, authException.getMessage());
        }
	}

}
