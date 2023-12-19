package kr.re.keti.sc.datacoreui.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * JWT authorization exception class.
 * @FileName JwtAuthorizationException.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
public class JwtAuthorizationException extends AuthenticationException {
	/**
	 * Constructor of JwtAuthorizationException(message)
	 * @param msg	Error message
	 */
	public JwtAuthorizationException(String msg) {
        super(msg);
    }

	/**
	 * Constructor of JwtAuthorizationException(message, throwable)
	 * @param msg	Error message
	 * @param t		Throwable
	 */
    public JwtAuthorizationException(String msg, Throwable t) {
        super(msg, t);
    }
}
