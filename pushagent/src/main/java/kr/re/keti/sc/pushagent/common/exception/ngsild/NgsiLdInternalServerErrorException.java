package kr.re.keti.sc.pushagent.common.exception.ngsild;

import kr.re.keti.sc.pushagent.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.pushagent.common.exception.BaseException;

public class NgsiLdInternalServerErrorException extends BaseException {

	private static final long serialVersionUID = -1598584115757128682L;

	public NgsiLdInternalServerErrorException( ErrorCode errorCode ) {
		super( errorCode );
	}

	public NgsiLdInternalServerErrorException( ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	public NgsiLdInternalServerErrorException( ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	public NgsiLdInternalServerErrorException( ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}