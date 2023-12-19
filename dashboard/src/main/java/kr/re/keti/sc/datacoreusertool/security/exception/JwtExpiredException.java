package kr.re.keti.sc.datacoreusertool.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * JWT expired exception class.
 * @FileName JwtExpiredException.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
public class JwtExpiredException extends AuthenticationException {
	/**
	 * Constructor of JwtExpiredException(message)
	 * @param msg	Error message
	 */
	public JwtExpiredException(String msg) {
		super(msg);
	}
	
	/**
	 * Constructor of JwtExpiredException(message)
	 * @param msg	Error message
	 * @param t		Throwable
	 */
	public JwtExpiredException(String msg, Throwable t) {
		super(msg, t);
	}
}
