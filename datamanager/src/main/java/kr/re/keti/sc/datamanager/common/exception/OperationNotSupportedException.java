package kr.re.keti.sc.datamanager.common.exception;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ErrorCode;

/**
 * OperationNotSupportedException 에러 정의 클래스
 */
public class OperationNotSupportedException extends BaseException {

	private static final long serialVersionUID = 325647261102179289L;

	public OperationNotSupportedException(ErrorCode errorCode ) {
		super( errorCode );
	}

	public OperationNotSupportedException(ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	public OperationNotSupportedException(ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	public OperationNotSupportedException(ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}