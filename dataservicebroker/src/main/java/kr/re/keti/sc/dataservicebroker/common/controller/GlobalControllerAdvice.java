package kr.re.keti.sc.dataservicebroker.common.controller;

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
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.ResponseCode;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ErrorPayload;
import kr.re.keti.sc.dataservicebroker.common.exception.InternalServerErrorException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdContextNotAvailableException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdInternalServerErrorException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdLengthRequiredException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdNoExistTypeException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdOperationNotSupportedException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdResourceNotFoundException;
import kr.re.keti.sc.dataservicebroker.util.ErrorUtil;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice //(basePackages = {"kr.re.keti.sc.dataservicebroker.controller", "error"}) 404 ?????? ????????? ?????? ????????????
@Slf4j
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
     * URL not found ?????? ?????? ?????? (404)
     * <p>
     * ??? ?????? HTTP method + url ????????? ??? Method not allowed  ????????? ??????
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
        return new ResponseEntity<>(errorPayload, headers, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * ?????? ?????? ????????? ?????? ??????, ?????? ?????? ?????? (500)
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ErrorPayload> internalServerErrorException(HttpServletRequest request, InternalServerErrorException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));

        }
        return new ResponseEntity<>(errorPayload, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorPayload> badRequestException(HttpServletRequest request, BadRequestException e) {

        String errorCode = e.getErrorCode();

        if (errorCode.equals(DataServiceBrokerCode.ErrorCode.ALREADY_EXISTS.getCode())) {

            // CREATE ???, ?????? id??? ?????? ??????, ????????? ?????? ?????? ?????? (409)
            ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
            if (request.getHeader(debugMessageKey) != null) {
                errorPayload.setDebugMessage(makeDebugMessage(e));
            }
            return new ResponseEntity<>(errorPayload, headers, HttpStatus.CONFLICT);

        } else {

            // ??? ?????? ????????? ?????? ?????? ?????? ?????? (400)
            ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
            if (request.getHeader(debugMessageKey) != null) {
                errorPayload.setDebugMessage(makeDebugMessage(e));
            }
            return new ResponseEntity<>(errorPayload, headers, HttpStatus.BAD_REQUEST);

        }

    }

    /**
     * ????????? ?????? ?????? ?????? ??????
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(NgsiLdBadRequestException.class)
    public ResponseEntity<ErrorPayload> ngsiLdBadRequestException(HttpServletRequest request, NgsiLdBadRequestException e) {

        String errorCode = e.getErrorCode();

        if (errorCode.equals(DataServiceBrokerCode.ErrorCode.ALREADY_EXISTS.getCode())) {

            // CREATE ???, ?????? id??? ?????? ??????, ????????? ?????? ?????? ?????? (409)
            ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
            if (request.getHeader(debugMessageKey) != null) {
                errorPayload.setDebugMessage(makeDebugMessage(e));
            }
            return new ResponseEntity<>(errorPayload, headers, HttpStatus.CONFLICT);

        } else {

            // ??? ?????? ????????? ?????? ?????? ?????? ?????? (400)
            ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
            if (request.getHeader(debugMessageKey) != null) {
                errorPayload.setDebugMessage(makeDebugMessage(e));
            }
            return new ResponseEntity<>(errorPayload, headers, HttpStatus.BAD_REQUEST);

        }

    }


    /**
     * ????????? resource??? ?????? ????????? ?????? ?????? ?????? ?????? (400
     *
     * @param request
     * @param e
     * @return
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
     * ??? ?????? HTTP Method ????????? ?????? ?????? ?????? ?????? (405)
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
        return new ResponseEntity<>(errorPayload, headers, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Not Acceptable ????????? ?????? ?????? ?????? ?????? (406)
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorPayload> notAcceptableMediaTypeStatusException(HttpServletRequest request, HttpMediaTypeNotAcceptableException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));

        }
        return new ResponseEntity<>(errorPayload, headers, HttpStatus.NOT_ACCEPTABLE);
    }
    
    /**
     * Unsupported Media Type ????????? ?????? ?????? ?????? ?????? (415)
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
        return new ResponseEntity<>(errorPayload, headers, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
//

    /**
     * POST ?????? ???, Content-Length header??? ?????? ?????? ?????? ??????
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(NgsiLdLengthRequiredException.class)
    public ResponseEntity<ErrorPayload> lengthRequiredException(HttpServletRequest request, NgsiLdLengthRequiredException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));

        }
        return new ResponseEntity<>(errorPayload, headers, HttpStatus.LENGTH_REQUIRED);
    }

    /**
     * POST body??? ????????? ?????? ??????, ?????? ?????? ?????? (400)
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
        return new ResponseEntity<ErrorPayload>(errorPayload, headers, HttpStatus.BAD_REQUEST);
    }

    /**
     * ?????? ?????? ????????? ?????? ??????, ?????? ?????? ?????? (500)
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(NgsiLdInternalServerErrorException.class)
    public ResponseEntity<ErrorPayload> internalServerErrorException(HttpServletRequest request, NgsiLdInternalServerErrorException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));

        }
        return new ResponseEntity<>(errorPayload, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * DB ????????? ?????? ???, ?????? ?????? (500)
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
        return new ResponseEntity<>(errorPayload, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * JSON ?????? ???, ????????? ??? ?????? ?????? ?????? (400)
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

        return new ResponseEntity<>(errorPayload, headers, HttpStatus.BAD_REQUEST);
    }

    /**
     * JSON ?????? ???, ????????? ??? ?????? ?????? ?????? (400)
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
        return new ResponseEntity<>(errorPayload, headers, HttpStatus.BAD_REQUEST);
    }



    /**
     * ???????????? ?????? ????????? ?????? ?????? ??? ?????? ?????? (200)
     * <p>
     * ex) http://{{hostname}}/entities?type=StreetParking,
     * ?????? ??????????????? ????????? ?????? ????????? ????????? ??? ??????.
     * ???, ???????????? ????????? ???????????? 200 OK??? empty entities array ??????
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(NgsiLdNoExistTypeException.class)
    public void noExistTypeException(HttpServletRequest request, HttpServletResponse response, NgsiLdNoExistTypeException e) throws IOException {
        response.getWriter().print(objectMapper.writeValueAsString(new ArrayList<>()));
    }


    /**
     * ???????????? ?????? Context ????????? ?????? ?????? ?????? ?????? (503)
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(NgsiLdContextNotAvailableException.class)
    public ResponseEntity<ErrorPayload> contextNotAvailableException(HttpServletRequest request, NgsiLdContextNotAvailableException e) {

        // ???????????? ?????? Context ????????? ?????? ?????? ?????? ?????? (503)
        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, HttpStatus.SERVICE_UNAVAILABLE);

    }
    
    /**
     * ?????? ?????? ?????? ??????
     *
     * @param request
     * @param response
     * @param e
     * @return
     * @throws IOException
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorPayload> accessDeniedException(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException {

        ErrorPayload errorPayload = new ErrorPayload(ResponseCode.UNAUTHORIZED.getDetailType(), ResponseCode.UNAUTHORIZED.getReasonPhrase(), e.getMessage());
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        //filter?????? ?????? ?????? ??? ??????, ErrorUtil ????????? ?????? ??????
        log.warn(e.getLocalizedMessage());

        return new ResponseEntity<>(errorPayload, headers, ResponseCode.UNAUTHORIZED.getHttpStatusCode());

    }


    /**
     * ?????? ?????? ?????? ??????
     *
     * @param request
     * @param response
     * @param e
     * @return
     * @throws IOException
     */
    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public ResponseEntity<ErrorPayload> duplicateKeyException(HttpServletRequest request, HttpServletResponse response, org.springframework.dao.DuplicateKeyException e) throws IOException {

        ErrorPayload errorPayload = new ErrorPayload(ResponseCode.CONFLICT.getDetailType(), ResponseCode.CONFLICT.getReasonPhrase(), "already exist");
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, ResponseCode.CONFLICT.getHttpStatusCode());

    }

    /**
     * ??? ?????? operation ?????? ??????
     *
     * @param request
     * @param response
     * @param e
     * @return
     * @throws IOException
     */
    @ExceptionHandler(NgsiLdOperationNotSupportedException.class)
    public ResponseEntity<ErrorPayload> operationNotSupportedException(HttpServletRequest request, HttpServletResponse response, NgsiLdOperationNotSupportedException e) throws IOException {

        ErrorPayload errorPayload = new ErrorPayload(ResponseCode.OPERATION_NOT_SUPPORTED.getDetailType(), ResponseCode.OPERATION_NOT_SUPPORTED.getReasonPhrase(), "The operation is not supported");
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, HttpStatus.UNPROCESSABLE_ENTITY);

    }


    /**
     * ?????? error message(debugMessage)??? ???????????? ErrorPayload ??????
     *
     * @param e
     * @return
     */
    private String makeDebugMessage(Exception e) {

        StringBuilder errorMsg = new StringBuilder();
        List<Throwable> throwableList = ExceptionUtils.getThrowableList(e);

        //????????? throwable ???????????? application cause ????????? ??? ??????
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

