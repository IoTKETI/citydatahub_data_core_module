package kr.re.keti.sc.datacoreusertool.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * JWT authorization exception class.
 * @FileName JwtAuthrorizationException.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
public class JwtAuthrorizationException extends AuthenticationException {
	/**
	 * Constructor of JwtAuthorizationException(message)
	 * @param msg	Error message
	 */
	public JwtAuthrorizationException(String msg) {
        super(msg);
    }

	/**
	 * Constructor of JwtAuthorizationException(message, throwable)
	 * @param msg	Error message
	 * @param t		Throwable
	 */
    public JwtAuthrorizationException(String msg, Throwable t) {
        super(msg, t);
    }
}
