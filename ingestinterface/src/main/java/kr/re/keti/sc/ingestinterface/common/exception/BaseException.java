package kr.re.keti.sc.ingestinterface.common.exception;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ErrorCode;

/**
 * 커스텀 에러 처리 Base 클래스
 */
public abstract class BaseException extends RuntimeException {

	private static final long serialVersionUID = 6697553987008675632L;

	ErrorCode errorCode = null;

	public BaseException( ErrorCode errorCode ) {
		this.errorCode = errorCode;
	}

	public BaseException( ErrorCode errorCode, String msg ) {
		super( msg );
		this.errorCode = errorCode;
	}

	public BaseException( ErrorCode errorCode, Throwable throwable ) {
		super( throwable );
		this.errorCode = errorCode;
	}

	public BaseException( ErrorCode errorCode, String msg, Throwable throwable ) {
		super( msg, throwable );
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode.getCode();
	}
}
