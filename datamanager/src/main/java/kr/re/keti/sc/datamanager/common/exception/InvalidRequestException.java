package kr.re.keti.sc.datamanager.common.exception;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ErrorCode;

/**
 * InvalidRequestException 에러 정의 클래스
 */
public class InvalidRequestException extends BaseException {

	private static final long serialVersionUID = 325647261102179280L;

	public InvalidRequestException(ErrorCode errorCode ) {
		super( errorCode );
	}

	public InvalidRequestException(ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	public InvalidRequestException(ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	public InvalidRequestException(ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}