package kr.re.keti.sc.dataservicebroker.util;

import org.codehaus.jettison.json.JSONException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.NgsiLdResponseCode;
import kr.re.keti.sc.dataservicebroker.common.code.ResponseCode;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ErrorPayload;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdContextNotAvailableException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdInternalServerErrorException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdLengthRequiredException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdOperationNotSupportedException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;

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
            errorPayload = new ErrorPayload(NgsiLdResponseCode.METHOD_NOT_ALLOWED.getDetailType(), NgsiLdResponseCode.METHOD_NOT_ALLOWED.getReasonPhrase(), e.getMessage());
        
        // API 에러 처리
        } else if (e instanceof BadRequestException) {
        	BadRequestException exception = (BadRequestException) e;
            String errorCode = exception.getErrorCode();
            if (errorCode.equals(DataServiceBrokerCode.ErrorCode.ALREADY_EXISTS.getCode())) {
                // CREATE 시, 중복 id가 있을 경우, 에러에 대한 공통 처리 (409)
                errorPayload = new ErrorPayload(ResponseCode.CONFLICT.getDetailType(), ResponseCode.CONFLICT.getReasonPhrase(), e.getMessage());
            } else {
                // 잘 못된 요청에 대한 에러 공통 처리 (400)
                errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), ResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), e.getMessage());
            }
            
        // NGSI-LD 에러 처리        
        } else if (e instanceof NgsiLdBadRequestException) {
            NgsiLdBadRequestException exception = (NgsiLdBadRequestException) e;
            String errorCode = exception.getErrorCode();
            if (errorCode.equals(DataServiceBrokerCode.ErrorCode.ALREADY_EXISTS.getCode())) {
                // CREATE 시, 중복 id가 있을 경우, 에러에 대한 공통 처리 (409)
                errorPayload = new ErrorPayload(NgsiLdResponseCode.CONFLICT.getDetailType(), NgsiLdResponseCode.CONFLICT.getReasonPhrase(), e.getMessage());
            } else {
                // 잘 못된 요청에 대한 에러 공통 처리 (400)
                errorPayload = new ErrorPayload(NgsiLdResponseCode.BAD_REQUEST_DATA.getDetailType(), NgsiLdResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), e.getMessage());
            }
        } else if (e instanceof NgsiLdResourceNotFoundException) {
            // 존재하지 않는 리소스 요청에 대한 에러 공통 처리 (404)
            errorPayload = new ErrorPayload(NgsiLdResponseCode.RESOURCE_NOT_FOUND.getDetailType(), NgsiLdResponseCode.RESOURCE_NOT_FOUND.getReasonPhrase(), e.getMessage());
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            errorPayload = new ErrorPayload(NgsiLdResponseCode.METHOD_NOT_ALLOWED.getDetailType(), NgsiLdResponseCode.METHOD_NOT_ALLOWED.getReasonPhrase(), e.getMessage());
        } else if (e instanceof HttpMediaTypeNotSupportedException) {
            errorPayload = new ErrorPayload(NgsiLdResponseCode.UNSUPPORTED_MEDIA_TYPE.getDetailType(), NgsiLdResponseCode.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(), e.getMessage());
        } else if (e instanceof HttpMediaTypeNotAcceptableException) {
            errorPayload = new ErrorPayload(NgsiLdResponseCode.NOT_ACCEPTABLE.getDetailType(), NgsiLdResponseCode.NOT_ACCEPTABLE.getReasonPhrase(), e.getMessage());
        } else if (e instanceof NgsiLdLengthRequiredException) {
            errorPayload = new ErrorPayload(NgsiLdResponseCode.LENGTH_REQUIRED.getDetailType(), NgsiLdResponseCode.LENGTH_REQUIRED.getReasonPhrase(), e.getMessage());
        } else if (e instanceof HttpMessageNotReadableException) {
            errorPayload = new ErrorPayload(NgsiLdResponseCode.BAD_REQUEST_DATA.getDetailType(), NgsiLdResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), "No HttpInputMessage available");
        } else if (e instanceof NgsiLdInternalServerErrorException) {
            errorPayload = new ErrorPayload(NgsiLdResponseCode.INTERNAL_SERVER_ERROR.getDetailType(), NgsiLdResponseCode.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Internal Server Error");
        } else if (e instanceof java.sql.SQLException) {
        	errorPayload = new ErrorPayload(ResponseCode.INTERNAL_SERVER_ERROR.getDetailType(), ResponseCode.INTERNAL_SERVER_ERROR.getReasonPhrase(), "sql error");
        } else if (e instanceof JsonProcessingException) {
            errorPayload = new ErrorPayload(NgsiLdResponseCode.BAD_REQUEST_DATA.getDetailType(), NgsiLdResponseCode.INVALID_REQUEST.getReasonPhrase(), "json parsing error");
        } else if (e instanceof JSONException) {
            errorPayload = new ErrorPayload(NgsiLdResponseCode.BAD_REQUEST_DATA.getDetailType(), NgsiLdResponseCode.INVALID_REQUEST.getReasonPhrase(), "json parsing error");
        } else if (e instanceof NgsiLdOperationNotSupportedException) {
            errorPayload = new ErrorPayload(NgsiLdResponseCode.OPERATION_NOT_SUPPORTED.getDetailType(), NgsiLdResponseCode.OPERATION_NOT_SUPPORTED.getReasonPhrase(), "The operation is not supported");
        } else if (e instanceof NgsiLdContextNotAvailableException) {
            errorPayload = new ErrorPayload(NgsiLdResponseCode.CONTEXT_NOT_AVAILABLE.getDetailType(), NgsiLdResponseCode.CONTEXT_NOT_AVAILABLE.getReasonPhrase(), "Unreachable URL is provided in @context");
        } else if (e instanceof Exception) {
            errorPayload = new ErrorPayload(NgsiLdResponseCode.INTERNAL_SERVER_ERROR.getDetailType(), NgsiLdResponseCode.INTERNAL_SERVER_ERROR.getReasonPhrase(), e.getMessage());
        }
        log.warn(e.getLocalizedMessage(), e);
        return errorPayload;
    }

}
