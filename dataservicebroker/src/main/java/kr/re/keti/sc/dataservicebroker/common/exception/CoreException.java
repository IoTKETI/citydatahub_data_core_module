package kr.re.keti.sc.dataservicebroker.common.exception;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;

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