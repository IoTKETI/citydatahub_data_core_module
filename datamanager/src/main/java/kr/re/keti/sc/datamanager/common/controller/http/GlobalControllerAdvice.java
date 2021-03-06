package kr.re.keti.sc.datamanager.common.controller.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.code.ResponseCode;
import kr.re.keti.sc.datamanager.common.exception.BadRequestException;
import kr.re.keti.sc.datamanager.common.exception.ErrorPayload;
import kr.re.keti.sc.datamanager.common.exception.InternalServerErrorException;
import kr.re.keti.sc.datamanager.common.exception.LengthRequiredException;
import kr.re.keti.sc.datamanager.common.exception.NoExistTypeException;
import kr.re.keti.sc.datamanager.common.exception.OperationNotSupportedException;
import kr.re.keti.sc.datamanager.common.exception.ProvisionException;
import kr.re.keti.sc.datamanager.common.exception.ResourceNotFoundException;
import kr.re.keti.sc.datamanager.common.exception.ngsild.NgsiLdResourceNotFoundException;
import kr.re.keti.sc.datamanager.util.ErrorUtil;

/**
 * HTTP ?????? ?????? ?????? ?????????
 */
@ControllerAdvice //(basePackages = {"kr.re.keti.sc.datamanager.controller", "error"}) 404 ?????? ????????? ?????? ????????????
public class GlobalControllerAdvice {

	@Autowired
    private ObjectMapper objectMapper;
	
    @Value("${datacore.http.binding.cause.msg.trace.key:x-detail-error-key}")
    private String debugMessageKey;
    @Value("${datacore.http.binding.cause.msg.level:1}")
    private int causeMessageLevel;
    
    private HttpHeaders headers;


    public GlobalControllerAdvice() {
        this.headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }


    /**
     * <pre>
     * URL not found ?????? ?????? ?????? (404)
     * ??? ?????? HTTP method + url ????????? ??? Method not allowed ????????? ??????
     * </pre>
     * @param request HttpServletRequest
     * @param e NoHandlerFoundException
     * @return HTTP Response Entity
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
     * Provisioning ??? ?????? (400 or 500)
     * @param request HttpServletRequest
     * @param e ProvisionException
     * @return HTTP Response Entity
     */
    @ExceptionHandler(ProvisionException.class)
    public ResponseEntity<ErrorPayload> provisionException(HttpServletRequest request, ProvisionException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        
        HttpStatus httpStatus = HttpStatus.valueOf(e.getProvisioningStatusCode());
        if(httpStatus == null) {
        	httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<>(errorPayload, headers, httpStatus);
    }

    /**
     * ????????? ?????? ?????? ?????? ??????
     * @param request HttpServletRequest
     * @param e BadRequestException
     * @return HTTP Response Entity
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorPayload> badRequestException(HttpServletRequest request, BadRequestException e) {

        String errorCode = e.getErrorCode();

        if (errorCode.equals(DataManagerCode.ErrorCode.ALREADY_EXISTS.getCode())) {

            // CREATE ???, ?????? id??? ?????? ??????, ????????? ?????? ?????? ?????? (409)
            ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
            if (request.getHeader(debugMessageKey) != null) {
                errorPayload.setDebugMessage(makeDebugMessage(e));
            }
            return new ResponseEntity<>(errorPayload, headers, ResponseCode.CONFLICT.getHttpStatusCode());

        } else {

            // ??? ?????? ????????? ?????? ?????? ?????? ?????? (400)
            ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
            if (request.getHeader(debugMessageKey) != null) {
                errorPayload.setDebugMessage(makeDebugMessage(e));
            }
            return new ResponseEntity<>(errorPayload, headers, ResponseCode.BAD_REQUEST_DATA.getHttpStatusCode());

        }

    }


    /**
     * ????????? resource??? ?????? ????????? ?????? ?????? ?????? ?????? (400)
     * @param request HttpServletRequest
     * @param e ResourceNotFoundException
     * @return HTTP Response Entity
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorPayload> resourceNotFoundException(HttpServletRequest request, ResourceNotFoundException e) {

        // ???????????? ?????? ????????? ????????? ?????? ?????? ?????? ?????? (404)
        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, ResponseCode.RESOURCE_NOT_FOUND.getHttpStatusCode());

    }

    /**
     * ??? ?????? HTTP Method ????????? ?????? ?????? ?????? ?????? (405)
     * @param request HttpServletRequest
     * @param e HttpRequestMethodNotSupportedException
     * @return HTTP Response Entity
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
     * Unsupported Media Type ????????? ?????? ?????? ?????? ?????? (415)
     * @param request HttpServletRequest
     * @param e HttpMediaTypeNotSupportedException
     * @return HTTP Response Entity
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorPayload> unsupportedMediaTypeStatusException(HttpServletRequest request, HttpMediaTypeNotSupportedException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));

        }
        return new ResponseEntity<>(errorPayload, headers, ResponseCode.UNSUPPORTED_MEDIA_TYPE.getHttpStatusCode());
    }
//

    /**
     * POST ?????? ???, Content-Length header??? ?????? ?????? ?????? ??????
     * @param request HttpServletRequest
     * @param e LengthRequiredException
     * @return HTTP Response Entity
     */
    @ExceptionHandler(LengthRequiredException.class)
    public ResponseEntity<ErrorPayload> lengthRequiredException(HttpServletRequest request, LengthRequiredException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));

        }
        return new ResponseEntity<>(errorPayload, headers, ResponseCode.BAD_REQUEST_DATA.getHttpStatusCode());
    }

    /**
     * POST body??? ????????? ?????? ??????, ?????? ?????? ?????? (400)
     * @param request HttpServletRequest
     * @param e HttpMessageNotReadableException
     * @return HTTP Response Entity
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
     * ?????? ?????? ????????? ?????? ??????, ?????? ?????? ?????? (500)
     * @param request HttpServletRequest
     * @param e InternalServerErrorException
     * @return HTTP Response Entity
     */
    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ErrorPayload> internalServerErrorException(HttpServletRequest request, InternalServerErrorException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));

        }
        return new ResponseEntity<>(errorPayload, headers, ResponseCode.INTERNAL_SERVER_ERROR.getHttpStatusCode());
    }

    /**
     * DB ????????? ?????? ???, ?????? ?????? (500)
     * @param request HttpServletRequest
     * @param e SQLException
     * @return HTTP Response Entity
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
     * <pre>
     * JSON ?????? ???, ????????? ??? ?????? ?????? ?????? (400)
     *  - 5.5.4. If the request payload body is not a valid JSON document then an error of type InvalidRequest shall be raised.
     * </pre>
     * @param request HttpServletRequest
     * @param e JsonProcessingException
     * @return HTTP Response Entity
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
     * <pre>
     * JSON ?????? ???, ????????? ??? ?????? ?????? ?????? (400)
     *  - 5.5.4. If the request payload body is not a valid JSON document then an error of type InvalidRequest shall be raised.
     * </pre>
     * @param request HttpServletRequest
     * @param e JSONException
     * @return HTTP Response Entity
     */
    @ExceptionHandler(JSONException.class)
    public ResponseEntity<ErrorPayload> jsonException(HttpServletRequest request, JSONException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, ResponseCode.INVALID_REQUEST.getHttpStatusCode());
    }

    /**
     * <pre>
     * ???????????? ?????? ????????? ?????? ?????? ??? ?????? ?????? (200)
     *  ex) http://{{hostname}}/entities?type=StreetParking,
     *  ?????? ??????????????? ????????? ?????? ????????? ????????? ??? ??????.
     *  ???, ???????????? ????????? ???????????? 200 OK??? empty entities array ??????
     * </pre>
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param e NoExistTypeException
     * @throws IOException
     */
    @ExceptionHandler(NoExistTypeException.class)
    public void noExistTypeException(HttpServletRequest request, HttpServletResponse response, NoExistTypeException e) throws IOException {
        response.getWriter().print(objectMapper.writeValueAsString(new ArrayList<>()));
    }


    /**
     * ?????? ?????? ?????? ?????? (401)
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param e AccessDeniedException
     * @return HTTP Response Entity
     * @throws IOException
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorPayload> accessDeniedException(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException {

        ErrorPayload errorPayload = new ErrorPayload(ResponseCode.UNAUTHORIZED.getDetailType(), ResponseCode.UNAUTHORIZED.getReasonPhrase(), e.getMessage());
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, ResponseCode.UNAUTHORIZED.getHttpStatusCode());

    }
    /**
     * ??? ?????? operation ?????? ?????? (422)
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param e OperationNotSupportedException
     * @return HTTP Response Entity
     * @throws IOException
     */
    @ExceptionHandler(OperationNotSupportedException.class)
    public ResponseEntity<ErrorPayload> operationNotSupportedException(HttpServletRequest request, HttpServletResponse response, OperationNotSupportedException e) throws IOException {

        ErrorPayload errorPayload = new ErrorPayload(ResponseCode.OPERATION_NOT_SUPPORTED.getDetailType(), ResponseCode.OPERATION_NOT_SUPPORTED.getReasonPhrase(), "The operation is not supported");
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, ResponseCode.OPERATION_NOT_SUPPORTED.getHttpStatusCode());

    }


    /**
     * ????????? resource??? ?????? ????????? ?????? ?????? ?????? ?????? (404)
     * @param request HttpServletRequest
     * @param e NgsiLdResourceNotFoundException
     * @return HTTP Response Entity
     */
    @ExceptionHandler(NgsiLdResourceNotFoundException.class)
    public ResponseEntity<ErrorPayload> resourceNotFoundException(HttpServletRequest request, NgsiLdResourceNotFoundException e) {

        // ???????????? ?????? ????????? ????????? ?????? ?????? ?????? ?????? (404)
        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, HttpStatus.NOT_FOUND);

    }
    /**
     * ?????? error message(debugMessage)??? ???????????? ErrorPayload ??????
     * @param e Exception
     * @return error payload
     */
    private String makeDebugMessage(Exception e) {

        StringBuilder errorMsg = new StringBuilder();
        List<Throwable> throwableList = ExceptionUtils.getThrowableList(e);

        // ????????? throwable ???????????? application cause ????????? ??? ??????
        // ????????? cause ????????? ??????????????? ?????????
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

