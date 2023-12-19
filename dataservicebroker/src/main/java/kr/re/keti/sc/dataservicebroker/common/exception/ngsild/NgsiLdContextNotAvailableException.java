package kr.re.keti.sc.dataservicebroker.common.exception.ngsild;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.exception.BaseException;

public class NgsiLdContextNotAvailableException extends BaseException {

	private static final long serialVersionUID = 544681606241711471L;

	public NgsiLdContextNotAvailableException(ErrorCode errorCode ) {
		super( errorCode );
	}

	public NgsiLdContextNotAvailableException(ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	public NgsiLdContextNotAvailableException(ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	public NgsiLdContextNotAvailableException(ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}