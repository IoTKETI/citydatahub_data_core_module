package kr.re.keti.sc.pushagent.common.exception;

import kr.re.keti.sc.pushagent.common.code.DataServiceBrokerCode.ErrorCode;

public class InternalServerErrorException extends BaseException {

	private static final long serialVersionUID = -1598584115757128682L;

	public InternalServerErrorException( ErrorCode errorCode ) {
		super( errorCode );
	}

	public InternalServerErrorException( ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	public InternalServerErrorException( ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	public InternalServerErrorException( ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}