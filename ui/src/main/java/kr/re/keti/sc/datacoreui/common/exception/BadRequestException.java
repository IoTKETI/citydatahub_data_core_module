package kr.re.keti.sc.datacoreui.common.exception;

import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode.ErrorCode;

/**
 * Bad request exception class
 * @FileName BadRequestException.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
public class BadRequestException extends BaseException {

	private static final long serialVersionUID = 325647261102179280L;

	/**
	 * Bad request exception with error code
	 * @param errorCode		ErrorCode
	 */
	public BadRequestException( ErrorCode errorCode ) {
		super( errorCode );
	}

	/**
	 * Bad request exception with error code and message
	 * @param errorCode		ErrorCode
	 * @param msg			Error message
	 */
	public BadRequestException( ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	/**
	 * Bad request exception with error code and throwable
	 * @param errorCode		ErrorCode
	 * @param throwable		Throwable
	 */
	public BadRequestException( ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	/**
	 * Bad request exception with error code and message and throwable
	 * @param errorCode		ErrorCode
	 * @param msg			Error message
	 * @param throwable		Throwable
	 */
	public BadRequestException( ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}