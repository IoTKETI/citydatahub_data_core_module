package kr.re.keti.sc.ingestinterface.common.exception;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ErrorCode;

/**
 * ParseException 에러 정의 클래스
 */
public class ParseException extends BaseException {

	private static final long serialVersionUID = 2684690196700308787L;

	public ParseException( ErrorCode errorCode ) {
		super( errorCode );
	}

	public ParseException( ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	public ParseException( ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	public ParseException( ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}