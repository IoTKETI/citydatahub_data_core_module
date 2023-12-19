package kr.re.keti.sc.dataservicebroker.common.exception.ngsild;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.exception.BaseException;

public class NgsiLdOperationNotSupportedException extends BaseException {

	private static final long serialVersionUID = 325647261102179289L;

	public NgsiLdOperationNotSupportedException(ErrorCode errorCode ) {
		super( errorCode );
	}

	public NgsiLdOperationNotSupportedException(ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	public NgsiLdOperationNotSupportedException(ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	public NgsiLdOperationNotSupportedException(ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}