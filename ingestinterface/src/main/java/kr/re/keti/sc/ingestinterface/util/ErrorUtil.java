package kr.re.keti.sc.ingestinterface.util;

import org.codehaus.jettison.json.JSONException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.re.keti.sc.ingestinterface.common.code.DataCoreResponseCode;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.exception.BadRequestException;
import kr.re.keti.sc.ingestinterface.common.exception.ErrorPayload;
import kr.re.keti.sc.ingestinterface.common.exception.InternalServerErrorException;
import kr.re.keti.sc.ingestinterface.common.exception.LengthRequiredException;
import kr.re.keti.sc.ingestinterface.common.exception.OperationNotSupportedException;
import kr.re.keti.sc.ingestinterface.common.exception.ParseException;
import kr.re.keti.sc.ingestinterface.common.exception.ResourceNotFoundException;
import kr.re.keti.sc.ingestinterface.common.exception.ngsild.NgsiLdResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Error payload util class
 */
@Slf4j
public class ErrorUtil {

    /**
     * Exception to ErrorPayload로 변경
     * @param e
     * @return
     */
    public static ErrorPayload convertExceptionToErrorPayload(Exception e) {

        ErrorPayload errorPayload = null;

        if (e instanceof NoHandlerFoundException) {
            errorPayload = new ErrorPayload(DataCoreResponseCode.METHOD_NOT_ALLOWED.getDetailType(), DataCoreResponseCode.METHOD_NOT_ALLOWED.getReasonPhrase(), e.getMessage());
        } else if (e instanceof BadRequestException) {
            BadRequestException exception = (BadRequestException) e;
            String errorCode = exception.getErrorCode();
            if (errorCode.equals(IngestInterfaceCode.ErrorCode.ALREADY_EXISTS.getCode())) {
                // CREATE 시, 중복 id가 있을 경우, 에러에 대한 공통 처리 (409)
                errorPayload = new ErrorPayload(DataCoreResponseCode.CONFLICT.getDetailType(), DataCoreResponseCode.CONFLICT.getReasonPhrase(), e.getMessage());
            } else {
                // 잘 못된 요청에 대한 에러 공통 처리 (400)
                errorPayload = new ErrorPayload(DataCoreResponseCode.BAD_REQUEST_DATA.getDetailType(), DataCoreResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), e.getMessage());
            }

        } else if (e instanceof ResourceNotFoundException) {
            // 존재하지 않는 리소스 요청에 대한 에러 공통 처리 (404)
            errorPayload = new ErrorPayload(DataCoreResponseCode.RESOURCE_NOT_FOUND.getDetailType(), DataCoreResponseCode.RESOURCE_NOT_FOUND.getReasonPhrase(), e.getMessage());
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            errorPayload = new ErrorPayload(DataCoreResponseCode.METHOD_NOT_ALLOWED.getDetailType(), DataCoreResponseCode.METHOD_NOT_ALLOWED.getReasonPhrase(), e.getMessage());
        } else if (e instanceof HttpMediaTypeNotSupportedException) {
            errorPayload = new ErrorPayload(DataCoreResponseCode.UNSUPPORTED_MEDIA_TYPE.getDetailType(), DataCoreResponseCode.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(), e.getMessage());
        } else if (e instanceof LengthRequiredException) {
            errorPayload = new ErrorPayload(DataCoreResponseCode.LENGTH_REQUIRED.getDetailType(), DataCoreResponseCode.LENGTH_REQUIRED.getReasonPhrase(), e.getMessage());
        } else if (e instanceof HttpMessageNotReadableException) {
            errorPayload = new ErrorPayload(DataCoreResponseCode.BAD_REQUEST_DATA.getDetailType(), DataCoreResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), "No HttpInputMessage available");
        } else if (e instanceof InternalServerErrorException) {
            errorPayload = new ErrorPayload(DataCoreResponseCode.INTERNAL_SERVER_ERROR.getDetailType(), DataCoreResponseCode.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Internal Server Error");
        } else if (e instanceof java.sql.SQLException) {
        	errorPayload = new ErrorPayload(DataCoreResponseCode.INTERNAL_SERVER_ERROR.getDetailType(), DataCoreResponseCode.INTERNAL_SERVER_ERROR.getReasonPhrase(), "sql error");
        } else if (e instanceof JsonProcessingException) {
            errorPayload = new ErrorPayload(DataCoreResponseCode.BAD_REQUEST_DATA.getDetailType(), DataCoreResponseCode.INVALID_REQUEST.getReasonPhrase(), "json parsing error");
        } else if (e instanceof JSONException) {
            errorPayload = new ErrorPayload(DataCoreResponseCode.BAD_REQUEST_DATA.getDetailType(), DataCoreResponseCode.INVALID_REQUEST.getReasonPhrase(), "json parsing error");
        } else if (e instanceof OperationNotSupportedException) {
            errorPayload = new ErrorPayload(DataCoreResponseCode.OPERATION_NOT_SUPPORTED.getDetailType(), DataCoreResponseCode.OPERATION_NOT_SUPPORTED.getReasonPhrase(), "The operation is not supported");
        } else if (e instanceof ParseException) {
            errorPayload = new ErrorPayload(DataCoreResponseCode.BAD_REQUEST_DATA.getDetailType(), DataCoreResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), e.getMessage());
        } else if (e instanceof NgsiLdResourceNotFoundException) {
            errorPayload = new ErrorPayload(DataCoreResponseCode.RESOURCE_NOT_FOUND.getDetailType(), DataCoreResponseCode.RESOURCE_NOT_FOUND.getReasonPhrase(), e.getMessage());
        } else if (e instanceof Exception) {
            errorPayload = new ErrorPayload(DataCoreResponseCode.INTERNAL_SERVER_ERROR.getDetailType(), DataCoreResponseCode.INTERNAL_SERVER_ERROR.getReasonPhrase(), e.getMessage());
        }
        log.warn(e.getLocalizedMessage(), e);

        return errorPayload;
    }

}
