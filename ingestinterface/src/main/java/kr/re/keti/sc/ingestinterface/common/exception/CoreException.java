package kr.re.keti.sc.ingestinterface.common.exception;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ErrorCode;

/**
 * CoreException 에러 정의 클래스
 */
public class CoreException extends BaseException {

	private static final long serialVersionUID = 2684690196700308787L;

	public CoreException( ErrorCode errorCode ) {
		super( errorCode );
	}

	public CoreException( ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	public CoreException( ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	public CoreException( ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}