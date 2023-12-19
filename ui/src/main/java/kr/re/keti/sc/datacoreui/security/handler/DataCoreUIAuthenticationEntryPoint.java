package kr.re.keti.sc.datacoreui.security.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import kr.re.keti.sc.datacoreui.security.exception.JwtAuthentioncationException;
import kr.re.keti.sc.datacoreui.security.exception.JwtAuthorizationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Data Core UI authentication entry point
 * @FileName DataCoreUIAuthenticationEntryPoint.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Slf4j
public class DataCoreUIAuthenticationEntryPoint implements AuthenticationEntryPoint {

	/**
	 * Authentication commence
	 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		log.warn("Auth Failed", authException);
        if (authException instanceof JwtAuthentioncationException) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        } else if (authException instanceof JwtAuthorizationException) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, authException.getMessage());
        }
	}

}
