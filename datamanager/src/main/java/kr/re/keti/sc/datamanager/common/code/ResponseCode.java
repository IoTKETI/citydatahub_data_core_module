package kr.re.keti.sc.datamanager.common.code;

import org.springframework.http.HttpStatus;

/**
 * HTTP Response code class
 */
public enum ResponseCode {


	OK("OK", "", HttpStatus.OK),
	CREATED("Created", "", HttpStatus.CREATED),
	DELETED("OK", "", HttpStatus.OK),
	CHANGE("NO_CONTENT", "", HttpStatus.NO_CONTENT),
	
	// request error
	INVALID_REQUEST("Invalid request", "http://citydatahub.kr/errors/InvalidRequest", HttpStatus.BAD_REQUEST),
	BAD_REQUEST_DATA("Bad request data", "http://citydatahub.kr/errors/BadRequestData", HttpStatus.BAD_REQUEST),
	UNAUTHORIZED("Unauthorized", "http://citydatahub.kr/errors/Unauthorized", HttpStatus.UNAUTHORIZED),
	RESOURCE_NOT_FOUND("Resource Not Found", "http://citydatahub.kr/errors/ResourceNotFound", HttpStatus.NOT_FOUND),
	METHOD_NOT_ALLOWED("Requested API is not defined", "http://citydatahub.kr/errors/MethodNotAllowed", HttpStatus.METHOD_NOT_ALLOWED),
	NOT_ACCEPTABLE("Request Serialization Format Not Supported", "http://citydatahub.kr/errors/UnsupportedMediaType", HttpStatus.NOT_ACCEPTABLE),
	CONFLICT("Already Exists", "http://citydatahub.kr/errors/AlreadyExists", HttpStatus.CONFLICT),
	UNSUPPORTED_MEDIA_TYPE("Request Serialization Format Not Supported", "http://citydatahub.kr/errors/UnsupportedMediaType", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
	OPERATION_NOT_SUPPORTED("Operation Not Supported", "http://citydatahub.kr/errors/OperationNotSupported", HttpStatus.UNPROCESSABLE_ENTITY),
	
	// internal error
	INTERNAL_SERVER_ERROR("Internal Server Error", "http://citydatahub.kr/errors/InternalError", HttpStatus.INTERNAL_SERVER_ERROR),
	;

	private final String detailDescription;
	private final String detailType;
	private final HttpStatus httpStatusCode;

	private ResponseCode(String detailDescription, String detailType, HttpStatus httpStatusCode) {
		this.detailDescription = detailDescription;
		this.detailType = detailType;
		this.httpStatusCode = httpStatusCode;
	}

	public String getReasonPhrase() {
		return detailDescription;
	}

	public String getDetailType() {
		return detailType;
	}

	public HttpStatus getHttpStatusCode() {
		return httpStatusCode;
	}
}