package kr.re.keti.sc.datacoreusertool.common.exception;

import org.springframework.http.HttpStatus;

import kr.re.keti.sc.datacoreusertool.common.vo.ClientExceptionPayloadVO;

/**
 * Data core UI exception class
 * @FileName DataCoreUIException.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
public class DataCoreUIException extends BaseException {

	private static final long serialVersionUID = 325647261102179280L;
	
	HttpStatus statusCode = null;
	ErrorPayload errorPayload = null;
	
	/**
	 * Data core UI exception with statusCode and clientExeptionPayload
	 * @param statusCode					HttpStatus
	 * @param clientExceptionPayloadVO		ClientExceptionPayloadVO
	 */
	public DataCoreUIException(HttpStatus statusCode, ClientExceptionPayloadVO clientExceptionPayloadVO) {
		super( null, clientExceptionPayloadVO.getDetail() );
		this.statusCode = statusCode;
		if(clientExceptionPayloadVO != null) {
			this.errorPayload = new ErrorPayload(clientExceptionPayloadVO.getType(), clientExceptionPayloadVO.getTitle(), clientExceptionPayloadVO.getDetail());
		}
	}
	
	/**
	 * Data core UI exception with status code
	 * @param statusCode	HttpStatus
	 */
	public DataCoreUIException(HttpStatus statusCode) {
		super( null, statusCode.getReasonPhrase() );
		this.statusCode = statusCode;
		this.errorPayload = new ErrorPayload(statusCode.getReasonPhrase(), statusCode.name(), statusCode.getReasonPhrase());
	}

	/**
	 * Get http status
	 * @return	Http status code
	 */
	public HttpStatus getHttpStatus() {
		return this.statusCode;
	}
	
	/**
	 * Get error payload
	 * @return		Error payload
	 */
	public ErrorPayload getErrorPayload() {
		return this.errorPayload;
	}
}