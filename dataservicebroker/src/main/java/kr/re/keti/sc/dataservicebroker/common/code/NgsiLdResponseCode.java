package kr.re.keti.sc.dataservicebroker.common.code;

import org.springframework.http.HttpStatus;

/**
 * HTTP Response 코드 클래스
 */
public enum NgsiLdResponseCode {


	OK("OK", "", HttpStatus.OK),
	CREATED("Created", "", HttpStatus.CREATED),
	DELETED("OK", "", HttpStatus.OK),
	CHANGE("OK", "", HttpStatus.NO_CONTENT),
	INVALID_REQUEST("Invalid request", "https://uri.etsi.org/ngsi-ld/errors/InvalidRequest", HttpStatus.BAD_REQUEST),
	BAD_REQUEST_DATA("Bad request data", "https://uri.etsi.org/ngsi-ld/errors/BadRequestData", HttpStatus.BAD_REQUEST),

	RESOURCE_NOT_FOUND("Resource Not Found", "https://uri.etsi.org/ngsi-ld/errors/ResourceNotFound", HttpStatus.NOT_FOUND),
	METHOD_NOT_ALLOWED("Method Not Allowed", "", HttpStatus.METHOD_NOT_ALLOWED),
	UNSUPPORTED_MEDIA_TYPE("Unsupported Media Type", "", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
	NOT_ACCEPTABLE("Not Acceptable", "", HttpStatus.NOT_ACCEPTABLE),

	INTERNAL_SERVER_ERROR("Internal Server Error", "https://uri.etsi.org/ngsi-ld/errors/InternalError", HttpStatus.INTERNAL_SERVER_ERROR),

	CONFLICT("Already Exists", "https://uri.etsi.org/ngsi-ld/errors/AlreadyExists", HttpStatus.CONFLICT),
	OPERATION_NOT_SUPPORTED("Unprocessable Entity", "https://uri.etsi.org/ngsi-ld/errors/OperationNotSupported", HttpStatus.UNPROCESSABLE_ENTITY),
	LENGTH_REQUIRED("request entity too large", "", HttpStatus.LENGTH_REQUIRED),
	CONTEXT_NOT_AVAILABLE("Context Not Available", "https://uri.etsi.org/ngsi-ld/errors/LdContextNotAvailable", HttpStatus.SERVICE_UNAVAILABLE),
	;

	private final String detailDescription;
	private final String detailType;
	private final HttpStatus httpStatusCode;

	private NgsiLdResponseCode(String detailDescription, String detailType, HttpStatus httpStatusCode) {
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