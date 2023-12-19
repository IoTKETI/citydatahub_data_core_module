package kr.re.keti.sc.datacoreusertool.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * JWT authentication exception class.
 * @FileName JwtAuthentioncationException.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
public class JwtAuthentioncationException extends AuthenticationException {
	/**
	 * Constructor of JwtAuthentioncationException(message)
	 * @param msg	Error message
	 */
	public JwtAuthentioncationException(String msg) {
        super(msg);
    }

	/**
	 * Constructor of JwtAuthentioncationException(message, throwable)
	 * @param msg	Error message
	 * @param t		Throwable
	 */
    public JwtAuthentioncationException(String msg, Throwable t) {
        super(msg, t);
    }
}
