package kr.re.keti.sc.pushagent.common.exception;

import kr.re.keti.sc.pushagent.common.code.DataServiceBrokerCode.ErrorCode;

public abstract class BaseException extends RuntimeException {

	private static final long serialVersionUID = 6697553987008675632L;

	protected ErrorCode errorCode = null;
	private boolean isNgsiLd = false;

	public BaseException( ErrorCode errorCode ) {
		this.errorCode = errorCode;
	}

	public BaseException( ErrorCode errorCode, String msg ) {
		super( msg );
		this.errorCode = errorCode;
	}

	public BaseException( ErrorCode errorCode, Throwable throwable ) {
		super( throwable );
		this.errorCode = errorCode;
	}

	public BaseException( ErrorCode errorCode, String msg, Throwable throwable ) {
		super( msg, throwable );
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode.getCode();
	}

	public boolean isNgsiLd() {
		return isNgsiLd;
	}

	public void setNgsiLd(boolean isNgsiLd) {
		this.isNgsiLd = isNgsiLd;
	}
}
