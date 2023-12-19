package kr.re.keti.sc.datacoreui.common.exception;

import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode.ErrorCode;

/**
 * Base exception class
 * @FileName BaseException.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
public abstract class BaseException extends RuntimeException {

	private static final long serialVersionUID = 6697553987008675632L;

	ErrorCode errorCode = null;

	/**
	 * Base exception with errorCode
	 * @param errorCode		ErrorCode
	 */
	public BaseException( ErrorCode errorCode ) {
		this.errorCode = errorCode;
	}

	/**
	 * Base exception with error code and message
	 * @param errorCode		ErrorCode
	 * @param msg			Error message
	 */
	public BaseException( ErrorCode errorCode, String msg ) {
		super( msg );
		this.errorCode = errorCode;
	}

	/**
	 * Base exception with error code and throwable
	 * @param errorCode		ErrorCode
	 * @param throwable		Throwable
	 */
	public BaseException( ErrorCode errorCode, Throwable throwable ) {
		super( throwable );
		this.errorCode = errorCode;
	}

	/**
	 * Base exception with error code and message and throwable
	 * @param errorCode		ErrorCode
	 * @param msg			Error message
	 * @param throwable		Throwable
	 */
	public BaseException( ErrorCode errorCode, String msg, Throwable throwable ) {
		super( msg, throwable );
		this.errorCode = errorCode;
	}

	/**
	 * Get error code
	 * @return	Error code
	 */
	public String getErrorCode() {
		return errorCode.getCode();
	}
}
