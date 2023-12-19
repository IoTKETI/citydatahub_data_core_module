package kr.re.keti.sc.datamanager.common.exception;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ErrorCode;

/**
 * LengthRequiredException 에러 정의 클래스
 */
public class LengthRequiredException extends BaseException {

	private static final long serialVersionUID = -1598584115757128699L;

	public LengthRequiredException(ErrorCode errorCode ) {
		super( errorCode );
	}

	public LengthRequiredException(ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	public LengthRequiredException(ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	public LengthRequiredException(ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}