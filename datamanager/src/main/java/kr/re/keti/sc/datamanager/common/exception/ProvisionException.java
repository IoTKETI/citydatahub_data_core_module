package kr.re.keti.sc.datamanager.common.exception;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ErrorCode;

/**
 * ProvisionException 에러 정의 클래스
 */
public class ProvisionException extends BaseException {

	private static final long serialVersionUID = 8197788900905860252L;
	private Integer provisioningStatusCode;

	public ProvisionException( ErrorCode errorCode, Integer provisioningStatusCode) {
		super( errorCode );
		this.provisioningStatusCode = provisioningStatusCode;
	}

	public ProvisionException( ErrorCode errorCode, Integer provisioningStatusCode, String msg) {
		super( errorCode, msg );
		this.provisioningStatusCode = provisioningStatusCode;
		this.errorCode = errorCode;
	}

	public ProvisionException( ErrorCode errorCode, Integer provisioningStatusCode, Throwable throwable ) {
		super( errorCode, throwable );
		this.provisioningStatusCode = provisioningStatusCode;
		this.errorCode = errorCode;
	}

	public ProvisionException( ErrorCode errorCode, Integer provisioningStatusCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
		this.provisioningStatusCode = provisioningStatusCode;
		this.errorCode = errorCode;
	}

	public Integer getProvisioningStatusCode() {
		return provisioningStatusCode;
	}
}
