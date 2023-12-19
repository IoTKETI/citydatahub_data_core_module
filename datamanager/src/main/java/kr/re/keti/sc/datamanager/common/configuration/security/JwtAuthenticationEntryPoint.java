package kr.re.keti.sc.datamanager.common.configuration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

	private static final long serialVersionUID = -7858869558953243875L;


	@Autowired
	@Qualifier("handlerExceptionResolver")
	private HandlerExceptionResolver resolver;


	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {

//		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
		resolver.resolveException(request, response, null, new org.springframework.security.access.AccessDeniedException("Access is denied"));


	}
}
