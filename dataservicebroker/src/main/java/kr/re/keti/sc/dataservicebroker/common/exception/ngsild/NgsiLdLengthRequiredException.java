package kr.re.keti.sc.dataservicebroker.common.exception.ngsild;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.exception.BaseException;

public class NgsiLdLengthRequiredException extends BaseException {

	private static final long serialVersionUID = -1598584115757128699L;

	public NgsiLdLengthRequiredException(ErrorCode errorCode ) {
		super( errorCode );
	}

	public NgsiLdLengthRequiredException(ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	public NgsiLdLengthRequiredException(ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	public NgsiLdLengthRequiredException(ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}