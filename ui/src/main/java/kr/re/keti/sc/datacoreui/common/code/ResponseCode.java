package kr.re.keti.sc.datacoreui.common.code;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

/**
 * Code Class of HTTP Response
 * @FileName ResponseCode.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
public enum ResponseCode {


	OK("2000", "OK", "", HttpStatus.OK),
	CREATED("2001", "Created", "", HttpStatus.CREATED),
	DELETED("2002", "OK", "", HttpStatus.OK),
	CHANGE("2004", "OK", "", HttpStatus.NO_CONTENT),
	//    BAD_REQUEST("4000", "Bad Request", HttpStatus.BAD_REQUEST),
	INVALID_REQUEST("4000", "Invalid request", "https://uri.etsi.org/ngsi-ld/errors/InvalidRequest", HttpStatus.BAD_REQUEST),
	BAD_REQUEST_DATA("4100", "Bad request data", "http://uri.etsi.org/ngsi-ld/errors/BadRequestData", HttpStatus.BAD_REQUEST),

	//    UNAUTHORIZED("4001", "Unauthorized", HttpStatus.UNAUTHORIZED),
	RESOURCE_NOT_FOUND("4004", "Resource Not Found", "http://uri.etsi.org/ngsi-ld/errors/ResourceNotFound", HttpStatus.NOT_FOUND),
	METHOD_NOT_ALLOWED("4005", "Method Not Allowed", "", HttpStatus.METHOD_NOT_ALLOWED),
	//    NOT_ACCEPTABLE("4006", "Not Acceptable", HttpStatus.NOT_ACCEPTABLE),
	UNSUPPORTED_MEDIA_TYPE("4015", "Unsupported Media Type", "", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

	//    MANDATORY_PARAMETER_MISSING("4101", "Mandatory Parameter Missing", HttpStatus.BAD_REQUEST),
	//    INVAILD_PARAMETER_TYPE("4102", "Invaild Parameter Type", HttpStatus.BAD_REQUEST),
	INTERNAL_SERVER_ERROR("5000", "Internal Server Error", "http://uri.etsi.org/ngsi-ld/errors/InternalError", HttpStatus.INTERNAL_SERVER_ERROR),

	CONFLICT("4009", "Already Exists", "http://uri.etsi.org/ngsi-ld/errors/AlreadyExists", HttpStatus.CONFLICT),
	OPERATION_NOT_SUPPORTED("4220", "Unprocessable Entity", "http://uri.etsi.org/ngsi-ld/errors/OperationNotSupported", HttpStatus.UNPROCESSABLE_ENTITY),
	LENGTH_REQUIRED("4011", "request entity too large", "", HttpStatus.LENGTH_REQUIRED),
	;

	private final String detailResponseCode;
	private final String detailDescription;
	private final String detailType;
	private final HttpStatus httpStatusCode;

	private ResponseCode(String detailResponseCode, String detailDescription, String detailType, HttpStatus httpStatusCode) {
		this.detailResponseCode = detailResponseCode;
		this.detailDescription = detailDescription;
		this.detailType = detailType;
		this.httpStatusCode = httpStatusCode;
	}

	private static final Map<String, ResponseCode> valueMap = new HashMap<>(ResponseCode.values().length);

	static {
		for (ResponseCode it : values()) {
			valueMap.put(it.getDetailCode(), it);
		}
	}

	public String getDetailCode() {
		return this.detailResponseCode;
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

	public static ResponseCode fromDetailResponseCode(String detailResponseCode) {
		return valueMap.get(detailResponseCode);
	}
}