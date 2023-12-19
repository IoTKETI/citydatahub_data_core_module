package kr.re.keti.sc.pushagent.common.exception.ngsild;

import kr.re.keti.sc.pushagent.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.pushagent.common.exception.BaseException;

public class NgsiLdBadRequestException extends BaseException {

	private static final long serialVersionUID = 325647261102179280L;

	public NgsiLdBadRequestException( ErrorCode errorCode ) {
		super( errorCode );
	}

	public NgsiLdBadRequestException( ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	public NgsiLdBadRequestException( ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	public NgsiLdBadRequestException( ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}