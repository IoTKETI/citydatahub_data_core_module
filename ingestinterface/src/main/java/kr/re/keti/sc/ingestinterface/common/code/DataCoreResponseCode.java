package kr.re.keti.sc.ingestinterface.common.code;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Response code class
 */
public enum DataCoreResponseCode {


    OK("2000", "OK", "", HttpStatus.OK),
    CREATED("2001", "Created", "", HttpStatus.CREATED),
    DELETED("2002", "OK", "", HttpStatus.OK),
    CHANGE("2004", "OK", "", HttpStatus.NO_CONTENT),

    // request error
    INVALID_REQUEST("4000", "Request Syntax Error", "http://citydatahub.kr/errors/InvalidRequest", HttpStatus.BAD_REQUEST),
    BAD_REQUEST_DATA("4100", "Bad Request Data", "http://citydatahub.kr/errors/BadRequestData", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("4001", "Unauthorized", "http://citydatahub.kr/errors/Unauthorized", HttpStatus.UNAUTHORIZED),
    RESOURCE_NOT_FOUND("4004", "Resource Not Found", "http://citydatahub.kr/errors/ResourceNotFound", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED("4005", "Requested API is not defined", "http://citydatahub.kr/errors/MethodNotAllowed", HttpStatus.METHOD_NOT_ALLOWED),
    NOT_ACCEPTABLE("4006", "Response Serialization Format Not Supported", "http://citydatahub.kr/errors/NotAcceptable", HttpStatus.NOT_ACCEPTABLE),
    CONFLICT("4009", "Already Exists", "http://citydatahub.kr/errors/AlreadyExists", HttpStatus.CONFLICT),
    UNSUPPORTED_MEDIA_TYPE("4015", "Request Serialization Format Not Supported", "http://citydatahub.kr/errors/UnsupportedMediaType", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    OPERATION_NOT_SUPPORTED("4220", "Operation Not Supported", "http://citydatahub.kr/errors/OperationNotSupported", HttpStatus.UNPROCESSABLE_ENTITY),


    //  internal error
    INTERNAL_SERVER_ERROR("5000", "Internal Error", "http://citydatahub.kr/errors/InternalError", HttpStatus.INTERNAL_SERVER_ERROR),
    LENGTH_REQUIRED("4011", "request entity too large", "", HttpStatus.LENGTH_REQUIRED),
    //    BAD_REQUEST("4000", "Bad Request", HttpStatus.BAD_REQUEST),
    //    MANDATORY_PARAMETER_MISSING("4101", "Mandatory Parameter Missing", HttpStatus.BAD_REQUEST),
    //    INVAILD_PARAMETER_TYPE("4102", "Invaild Parameter Type", HttpStatus.BAD_REQUEST),
    ;

    private final String detailResponseCode;
    private final String detailDescription;
    private final String detailType;
    private final HttpStatus httpStatusCode;

    private DataCoreResponseCode(String detailResponseCode, String detailDescription, String detailType, HttpStatus httpStatusCode) {
        this.detailResponseCode = detailResponseCode;
        this.detailDescription = detailDescription;
        this.detailType = detailType;
        this.httpStatusCode = httpStatusCode;
    }

    private static final Map<String, DataCoreResponseCode> valueMap = new HashMap<>(DataCoreResponseCode.values().length);

    static {
        for (DataCoreResponseCode it : values()) {
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

    public static DataCoreResponseCode fromDetailResponseCode(String detailResponseCode) {
        return valueMap.get(detailResponseCode);
    }
}