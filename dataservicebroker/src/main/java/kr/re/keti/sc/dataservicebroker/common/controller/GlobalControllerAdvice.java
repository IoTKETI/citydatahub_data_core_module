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

@ControllerAdvice //(basePackages = {"kr.re.keti.sc.dataservicebroker.controller", "error"}) 404 에러 처리를 위해 주석처리
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
     * URL not found 에러 공통 처리 (404)
     * <p>
     * 잘 못된 HTTP method + url 요청이 올 Method not allowed  경우로 처리
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
     * 서버 내부 오류가 있을 경우, 공통 에러 처리 (500)
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

            // CREATE 시, 중복 id가 있을 경우, 에러에 대한 공통 처리 (409)
            ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
            if (request.getHeader(debugMessageKey) != null) {
                errorPayload.setDebugMessage(makeDebugMessage(e));
            }
            return new ResponseEntity<>(errorPayload, headers, HttpStatus.CONFLICT);

        } else {

            // 잘 못된 요청에 대한 에러 공통 처리 (400)
            ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
            if (request.getHeader(debugMessageKey) != null) {
                errorPayload.setDebugMessage(makeDebugMessage(e));
            }
            return new ResponseEntity<>(errorPayload, headers, HttpStatus.BAD_REQUEST);

        }

    }

    /**
     * 개발자 정의 에러 공통 처리
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(NgsiLdBadRequestException.class)
    public ResponseEntity<ErrorPayload> ngsiLdBadRequestException(HttpServletRequest request, NgsiLdBadRequestException e) {

        String errorCode = e.getErrorCode();

        if (errorCode.equals(DataServiceBrokerCode.ErrorCode.ALREADY_EXISTS.getCode())) {

            // CREATE 시, 중복 id가 있을 경우, 에러에 대한 공통 처리 (409)
            ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
            if (request.getHeader(debugMessageKey) != null) {
                errorPayload.setDebugMessage(makeDebugMessage(e));
            }
            return new ResponseEntity<>(errorPayload, headers, HttpStatus.CONFLICT);

        } else {

            // 잘 못된 요청에 대한 에러 공통 처리 (400)
            ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
            if (request.getHeader(debugMessageKey) != null) {
                errorPayload.setDebugMessage(makeDebugMessage(e));
            }
            return new ResponseEntity<>(errorPayload, headers, HttpStatus.BAD_REQUEST);

        }

    }


    /**
     * 관련된 resource를 찾지 못함에 따른 에러 공통 처리 (400
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(NgsiLdResourceNotFoundException.class)
    public ResponseEntity<ErrorPayload> resourceNotFoundException(HttpServletRequest request, NgsiLdResourceNotFoundException e) {

        // 존재하지 않는 리소스 요청에 대한 에러 공통 처리 (404)
        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, HttpStatus.NOT_FOUND);

    }

    /**
     * 잘 못된 HTTP Method 요청에 대한 에러 공통 처리 (405)
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
     * Not Acceptable 요청에 대한 공통 에러 처리 (406)
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
     * Unsupported Media Type 요청에 대한 공통 에러 처리 (415)
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
     * POST 요청 시, Content-Length header가 없을 경우 예외 처리
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
     * POST body가 오류가 있을 경우, 공통 에러 처리 (400)
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
     * 서버 내부 오류가 있을 경우, 공통 에러 처리 (500)
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
     * DB 커넥션 에러 시, 공통 처리 (500)
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
     * JSON 파싱 시, 에러가 날 경우 공통 처리 (400)
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
     * JSON 파싱 시, 에러가 날 경우 공통 처리 (400)
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
     * 존재하지 않는 엔티티 타입 명시 시 에러 처리 (200)
     * <p>
     * ex) http://{{hostname}}/entities?type=StreetParking,
     * 요청 메시지에는 이상이 없어 검색을 수행할 수 있음.
     * 단, 해당하는 결과가 없으므로 200 OK에 empty entities array 반환
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
     * 유효하지 않은 Context 정보에 대한 에러 공통 처리 (503)
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(NgsiLdContextNotAvailableException.class)
    public ResponseEntity<ErrorPayload> contextNotAvailableException(HttpServletRequest request, NgsiLdContextNotAvailableException e) {

        // 유효하지 않은 Context 정보에 대한 에러 공통 처리 (503)
        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, HttpStatus.SERVICE_UNAVAILABLE);

    }
    
    /**
     * 인증 오류 예외 처리
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
        //filter에서 예외 처리 된 경우, ErrorUtil 로직을 타지 않음
        log.warn(e.getLocalizedMessage());

        return new ResponseEntity<>(errorPayload, headers, ResponseCode.UNAUTHORIZED.getHttpStatusCode());

    }


    /**
     * 인증 오류 예외 처리
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
     * 미 개발 operation 예외 처리
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
     * 상세 error message(debugMessage)를 포함하는 ErrorPayload 생성
     *
     * @param e
     * @return
     */
    private String makeDebugMessage(Exception e) {

        StringBuilder errorMsg = new StringBuilder();
        List<Throwable> throwableList = ExceptionUtils.getThrowableList(e);

        //발생한 throwable 레벨보다 application cause 레벨이 클 경우
        // 발생한 cause 레벨의 길이만큼만 조회함
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

