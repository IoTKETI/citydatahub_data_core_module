package kr.re.keti.sc.datamanager.util;

import org.codehaus.jettison.json.JSONException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.code.ResponseCode;
import kr.re.keti.sc.datamanager.common.exception.BadRequestException;
import kr.re.keti.sc.datamanager.common.exception.ErrorPayload;
import kr.re.keti.sc.datamanager.common.exception.InternalServerErrorException;
import kr.re.keti.sc.datamanager.common.exception.LengthRequiredException;
import kr.re.keti.sc.datamanager.common.exception.OperationNotSupportedException;
import kr.re.keti.sc.datamanager.common.exception.ProvisionException;
import kr.re.keti.sc.datamanager.common.exception.ResourceNotFoundException;
import kr.re.keti.sc.datamanager.common.exception.ngsild.NgsiLdResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * error payload util class
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
            errorPayload = new ErrorPayload(ResponseCode.METHOD_NOT_ALLOWED.getDetailType(), ResponseCode.METHOD_NOT_ALLOWED.getReasonPhrase(), e.getMessage());
        } else if (e instanceof BadRequestException) {
            BadRequestException exception = (BadRequestException) e;
            String errorCode = exception.getErrorCode();
            if (errorCode.equals(DataManagerCode.ErrorCode.ALREADY_EXISTS.getCode())) {
                // CREATE 시, 중복 id가 있을 경우, 에러에 대한 공통 처리 (409)
                errorPayload = new ErrorPayload(ResponseCode.CONFLICT.getDetailType(), ResponseCode.CONFLICT.getReasonPhrase(), e.getMessage());
            } else {
                // 잘 못된 요청에 대한 에러 공통 처리 (400)
                errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), ResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), e.getMessage());
            }

        } else if (e instanceof ResourceNotFoundException) {
            // 존재하지 않는 리소스 요청에 대한 에러 공통 처리 (404)
            errorPayload = new ErrorPayload(ResponseCode.RESOURCE_NOT_FOUND.getDetailType(), ResponseCode.RESOURCE_NOT_FOUND.getReasonPhrase(), e.getMessage());
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            errorPayload = new ErrorPayload(ResponseCode.METHOD_NOT_ALLOWED.getDetailType(), ResponseCode.METHOD_NOT_ALLOWED.getReasonPhrase(), e.getMessage());
        } else if (e instanceof HttpMediaTypeNotSupportedException) {
            errorPayload = new ErrorPayload(ResponseCode.UNSUPPORTED_MEDIA_TYPE.getDetailType(), ResponseCode.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(), e.getMessage());
        } else if (e instanceof LengthRequiredException) {
            errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), ResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), e.getMessage());
        } else if (e instanceof HttpMessageNotReadableException) {
            errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), ResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), "No HttpInputMessage available");
        } else if (e instanceof InternalServerErrorException) {
            errorPayload = new ErrorPayload(ResponseCode.INTERNAL_SERVER_ERROR.getDetailType(), ResponseCode.INTERNAL_SERVER_ERROR.getReasonPhrase(), e.getMessage());
        } else if (e instanceof java.sql.SQLException) {
            errorPayload = new ErrorPayload(ResponseCode.INTERNAL_SERVER_ERROR.getDetailType(), ResponseCode.INTERNAL_SERVER_ERROR.getReasonPhrase(), "sql error");
        } else if (e instanceof JsonProcessingException) {
            errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), ResponseCode.INVALID_REQUEST.getReasonPhrase(), "json parsing error");
        } else if (e instanceof JSONException) {
            errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), ResponseCode.INVALID_REQUEST.getReasonPhrase(), "json parsing error");
        } else if (e instanceof OperationNotSupportedException) {
            errorPayload = new ErrorPayload(ResponseCode.OPERATION_NOT_SUPPORTED.getDetailType(), ResponseCode.OPERATION_NOT_SUPPORTED.getReasonPhrase(), "The operation is not supported");
        } else if (e instanceof NgsiLdResourceNotFoundException) {
            errorPayload = new ErrorPayload(ResponseCode.RESOURCE_NOT_FOUND.getDetailType(), ResponseCode.RESOURCE_NOT_FOUND.getReasonPhrase(), e.getMessage());
        } else if (e instanceof ProvisionException) {
        	Integer statusCode = ((ProvisionException) e).getProvisioningStatusCode();
        	if(statusCode != null && statusCode == 400) {
        		errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), "provisioning error", e.getMessage());
        	} else {
        		errorPayload = new ErrorPayload(ResponseCode.INTERNAL_SERVER_ERROR.getDetailType(), "provisioning error", e.getMessage());
        	}
        }
        log.warn(e.getLocalizedMessage(), e);

        return errorPayload;
    }

}
