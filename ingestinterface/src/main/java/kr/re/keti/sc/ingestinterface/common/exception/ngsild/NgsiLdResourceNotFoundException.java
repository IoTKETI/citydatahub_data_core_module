package kr.re.keti.sc.ingestinterface.common.exception.ngsild;


import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.exception.BaseException;

/**
 * NgsiLd Resource Not Found 에러 처리 클래스
 */
public class NgsiLdResourceNotFoundException extends BaseException {

	private static final long serialVersionUID = 325647261102179280L;

	public NgsiLdResourceNotFoundException(IngestInterfaceCode.ErrorCode errorCode ) {
		super( errorCode );
	}

	public NgsiLdResourceNotFoundException(IngestInterfaceCode.ErrorCode errorCode, String msg ) {
		super( errorCode, msg );
//		this.errorCode = errorCode;
	}

	public NgsiLdResourceNotFoundException(IngestInterfaceCode.ErrorCode errorCode, Throwable throwable ) {
		super( errorCode, throwable );
//		this.errorCode = errorCode;
	}

	public NgsiLdResourceNotFoundException(IngestInterfaceCode.ErrorCode errorCode, String msg, Throwable throwable ) {
		super( errorCode, msg, throwable );
//		this.errorCode = errorCode;
	}
}