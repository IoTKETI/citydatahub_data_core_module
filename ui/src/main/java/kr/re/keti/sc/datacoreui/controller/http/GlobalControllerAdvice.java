package kr.re.keti.sc.datacoreui.controller.http;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode;
import kr.re.keti.sc.datacoreui.common.code.ResponseCode;
import kr.re.keti.sc.datacoreui.common.exception.BadRequestException;
import kr.re.keti.sc.datacoreui.common.exception.DataCoreUIException;
import kr.re.keti.sc.datacoreui.common.exception.ErrorPayload;
import kr.re.keti.sc.datacoreui.util.ErrorUtil;

/**
 * Class for global controller advice
 * @FileName GlobalControllerAdvice.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@ControllerAdvice //(basePackages = {"kr.re.keti.sc.datacoreui.controller", "error"}) Comment out for 404 error handling
public class GlobalControllerAdvice {

    private final Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @Value("${datacore.http.binding.cause.msg.trace.key:x-detail-error-key}")
    private String debugMessageKey;

    @Value("${datacore.http.binding.cause.msg.level:1}")
    private int causeMessageLevel;

    private ObjectMapper objectMapper;
    private HttpHeaders headers;


    /**
     * Constructor of GlobalControllerAdvice class
     */
    public GlobalControllerAdvice() {
        this.objectMapper = new ObjectMapper();
        this.headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }


    /**
     * Common handling of URL not found error (404)
     * <p>
     * If an incorrect HTTP method + url request is received, it is treated as a Method not allowed case.
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorPayload> notFoundErrorException(HttpServletRequest request, NoHandlerFoundException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, ResponseCode.METHOD_NOT_ALLOWED.getHttpStatusCode());
    }


    /**
     * Common developer-defined error handling
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorPayload> badRequestException(HttpServletRequest request, BadRequestException e) {

        String errorCode = e.getErrorCode();

        if (errorCode.equals(DataCoreUiCode.ErrorCode.ALREADY_EXISTS.getCode())) {

            // Common handling of errors when there are duplicate ids during CREATE (409)
            ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
            if (request.getHeader(debugMessageKey) != null) {
                errorPayload.setDebugMessage(makeDebugMessage(e));
            }
            return new ResponseEntity<>(errorPayload, headers, ResponseCode.CONFLICT.getHttpStatusCode());

        } else {

            // Error Common Handling for Bad Requests (400)
            ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
            if (request.getHeader(debugMessageKey) != null) {
                errorPayload.setDebugMessage(makeDebugMessage(e));
            }
            return new ResponseEntity<>(errorPayload, headers, ResponseCode.BAD_REQUEST_DATA.getHttpStatusCode());

        }

    }

    /**
     * Error Common Handling for Invalid HTTP Method Request (405)
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorPayload> httpRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {


        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, ResponseCode.METHOD_NOT_ALLOWED.getHttpStatusCode());
    }

    /**
     * Handling Common Errors for Unsupported Media Type Requests (415)
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorPayload> unsupportedMediaTypeStatusException(HttpServletRequest request, HttpMediaTypeNotSupportedException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));

        }
        return new ResponseEntity<>(errorPayload, headers, ResponseCode.UNSUPPORTED_MEDIA_TYPE.getHttpStatusCode());
    }

    /**
     * Common error handling when POST body has an error (400)
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorPayload> httpMessageNotReadableException(HttpServletRequest request, HttpMessageNotReadableException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));

        }
        return new ResponseEntity<ErrorPayload>(errorPayload, headers, ResponseCode.BAD_REQUEST_DATA.getHttpStatusCode());
    }

    /**
     * Common handling in case of DB connection error (500)
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(java.sql.SQLException.class)
    public ResponseEntity<ErrorPayload> sqlException(HttpServletRequest request, java.sql.SQLException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));

        }
        return new ResponseEntity<>(errorPayload, headers, ResponseCode.INTERNAL_SERVER_ERROR.getHttpStatusCode());
    }


    /**
     * Common handling when an error occurs when parsing JSON (400)
     * //5.5.4. If the request payload body is not a valid JSON document then an error of type InvalidRequest shall be raised.
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorPayload> jsonParseException(HttpServletRequest request, JsonProcessingException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }

        return new ResponseEntity<>(errorPayload, headers, ResponseCode.INVALID_REQUEST.getHttpStatusCode());
    }

    /**
     * Common handling when an error occurs when parsing JSON (400)
     * //5.5.4. If the request payload body is not a valid JSON document then an error of type InvalidRequest shall be raised.
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(JSONException.class)
    public ResponseEntity<ErrorPayload> jsonException(HttpServletRequest request, JSONException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, ResponseCode.INVALID_REQUEST.getHttpStatusCode());
    }
    
    @ExceptionHandler(DataCoreUIException.class)
    public ResponseEntity<ErrorPayload> operationNotSupportedException(HttpServletRequest request, HttpServletResponse response, DataCoreUIException e) throws IOException {
        return new ResponseEntity<>(e.getErrorPayload(), headers, e.getHttpStatus());
    }


    /**
     * Create ErrorPayload with detailed error message (debugMessage)
     *
     * @param e
     * @return
     */
    private String makeDebugMessage(Exception e) {

        StringBuilder errorMsg = new StringBuilder();
        List<Throwable> throwableList = ExceptionUtils.getThrowableList(e);

        // If the application cause level is higher than the throwable level that occurred, 
        // only the length of the cause level that occurred is searched.
        int throwableLevel;
        if (causeMessageLevel > throwableList.size()) {
            throwableLevel = throwableList.size();
        } else {
            throwableLevel = causeMessageLevel;
        }

        for (int i = 0; i < throwableLevel; i++) {
            errorMsg.append(throwableList.get(i).getMessage());
        }
        return errorMsg.toString();
    }

}

