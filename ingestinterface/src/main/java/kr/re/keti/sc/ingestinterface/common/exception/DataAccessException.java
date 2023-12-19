package kr.re.keti.sc.ingestinterface.common.exception;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ErrorCode;

/**
 * DataAccessException 에러 정의 클래스
 */
public class DataAccessException extends BaseException {

	private static final long serialVersionUID = 1L;

	public DataAccessException( ErrorCode errorCode ) {
		super( errorCode );
	}

	public DataAccessException( ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	public DataAccessException( ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	public DataAccessException( ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}