package kr.re.keti.sc.ingestinterface.common.exception;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ErrorCode;

/**
 * BadRequest 에러 정의 클래스
 */
public class BadRequestException extends BaseException {

	private static final long serialVersionUID = 325647261102179280L;

	public BadRequestException( ErrorCode errorCode ) {
		super( errorCode );
	}

	public BadRequestException( ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	public BadRequestException( ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	public BadRequestException( ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}