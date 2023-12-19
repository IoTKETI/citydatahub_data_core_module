package kr.re.keti.sc.pushagent.common.exception;

import kr.re.keti.sc.pushagent.common.code.DataServiceBrokerCode.ErrorCode;

public class NotificationException extends BaseException {

	private static final long serialVersionUID = 3803740953287780618L;

	public NotificationException( ErrorCode errorCode ) {
		super( errorCode );
	}

	public NotificationException( ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
		this.errorCode = errorCode;
	}

	public NotificationException( ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.errorCode = errorCode;
	}

	public NotificationException( ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.errorCode = errorCode;
	}
}