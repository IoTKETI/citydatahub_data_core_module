package kr.re.keti.sc.ingestinterface.controller.http;

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

import kr.re.keti.sc.ingestinterface.common.code.DataCoreResponseCode;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.exception.BadRequestException;
import kr.re.keti.sc.ingestinterface.common.exception.ErrorPayload;
import kr.re.keti.sc.ingestinterface.common.exception.InternalServerErrorException;
import kr.re.keti.sc.ingestinterface.common.exception.LengthRequiredException;
import kr.re.keti.sc.ingestinterface.common.exception.NoExistTypeException;
import kr.re.keti.sc.ingestinterface.common.exception.OperationNotSupportedException;
import kr.re.keti.sc.ingestinterface.common.exception.ResourceNotFoundException;
import kr.re.keti.sc.ingestinterface.common.exception.ngsild.NgsiLdResourceNotFoundException;
import kr.re.keti.sc.ingestinterface.util.ErrorUtil;

/**
 * HTTP 에러 공통 처리 클래스
 */
@ControllerAdvice //(basePackages = {"kr.re.keti.sc.ingestinterface.controller", "error"}) 404 에러 처리를 위해 주석처리
public class GlobalControllerAdvice {

    @Value("${datacore.http.binding.cause.msg.trace.key:x-detail-error-key}")
    private String debugMessageKey;

    @Value("${datacore.http.binding.cause.msg.level:1}")
    private int causeMessageLevel;
    @Autowired
    private ObjectMapper objectMapper;
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
        return new ResponseEntity<>(errorPayload, headers, DataCoreResponseCode.METHOD_NOT_ALLOWED.getHttpStatusCode());
    }


    /**
     * 개발자 정의 에러 공통 처리
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorPayload> badRequestException(HttpServletRequest request, BadRequestException e) {

        String errorCode = e.getErrorCode();

        if (errorCode.equals(IngestInterfaceCode.ErrorCode.ALREADY_EXISTS.getCode())) {

            // CREATE 시, 중복 id가 있을 경우, 에러에 대한 공통 처리 (409)
            ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
            if (request.getHeader(debugMessageKey) != null) {
                errorPayload.setDebugMessage(makeDebugMessage(e));
            }
            return new ResponseEntity<>(errorPayload, headers, DataCoreResponseCode.CONFLICT.getHttpStatusCode());

        } else {

            // 잘 못된 요청에 대한 에러 공통 처리 (400)
            ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
            if (request.getHeader(debugMessageKey) != null) {
                errorPayload.setDebugMessage(makeDebugMessage(e));
            }
            return new ResponseEntity<>(errorPayload, headers, DataCoreResponseCode.BAD_REQUEST_DATA.getHttpStatusCode());

        }

    }


    /**
     * 관련된 resource를 찾지 못함에 따른 에러 공통 처리 (400
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorPayload> resourceNotFoundException(HttpServletRequest request, ResourceNotFoundException e) {

        // 존재하지 않는 리소스 요청에 대한 에러 공통 처리 (404)
        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, DataCoreResponseCode.RESOURCE_NOT_FOUND.getHttpStatusCode());

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
        return new ResponseEntity<>(errorPayload, headers, DataCoreResponseCode.METHOD_NOT_ALLOWED.getHttpStatusCode());
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
        return new ResponseEntity<>(errorPayload, headers, DataCoreResponseCode.UNSUPPORTED_MEDIA_TYPE.getHttpStatusCode());
    }
//

    /**
     * POST 요청 시, Content-Length header가 없을 경우 예외 처리
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(LengthRequiredException.class)
    public ResponseEntity<ErrorPayload> lengthRequiredException(HttpServletRequest request, LengthRequiredException e) {

        ErrorPayload errorPayload = ErrorUtil.convertExceptionToErrorPayload(e);
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));

        }
        return new ResponseEntity<>(errorPayload, headers, DataCoreResponseCode.LENGTH_REQUIRED.getHttpStatusCode());
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
        return new ResponseEntity<ErrorPayload>(errorPayload, headers, DataCoreResponseCode.BAD_REQUEST_DATA.getHttpStatusCode());
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
        return new ResponseEntity<>(errorPayload, headers, DataCoreResponseCode.INTERNAL_SERVER_ERROR.getHttpStatusCode());
    }

    /**
     * DB 커넥션 에러 시, 공통 처리 (400)
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
        return new ResponseEntity<>(errorPayload, headers, DataCoreResponseCode.BAD_REQUEST_DATA.getHttpStatusCode());
    }


    /**
     * JSON 파싱 시, 에러가 날 경우 공통 처리 (400)
     * //5.5.4. If the request payload body is not a valid JSON document then an error of type InvalidRequest shall be raised.
     *
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

        return new ResponseEntity<>(errorPayload, headers, DataCoreResponseCode.INVALID_REQUEST.getHttpStatusCode());
    }

    /**
     * JSON 파싱 시, 에러가 날 경우 공통 처리 (400)
     * //5.5.4. If the request payload body is not a valid JSON document then an error of type InvalidRequest shall be raised.
     *
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
        return new ResponseEntity<>(errorPayload, headers, DataCoreResponseCode.INVALID_REQUEST.getHttpStatusCode());
    }


    /**
     * <pre>
     * 존재하지 않는 엔티티 타입 명시 시 에러 처리 (200)
     *  ex) http://{{hostname}}/entities?type=StreetParking,
     *  요청 메시지에는 이상이 없어 검색을 수행할 수 있음.
     *  단, 해당하는 결과가 없으므로 200 OK에 empty entities array 반환
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

        ErrorPayload errorPayload = new ErrorPayload(DataCoreResponseCode.UNAUTHORIZED.getDetailType(), DataCoreResponseCode.UNAUTHORIZED.getReasonPhrase(), e.getMessage());
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, DataCoreResponseCode.UNAUTHORIZED.getHttpStatusCode());

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

        ErrorPayload errorPayload = new ErrorPayload(DataCoreResponseCode.CONFLICT.getDetailType(), DataCoreResponseCode.CONFLICT.getReasonPhrase(), "already exist");
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, DataCoreResponseCode.CONFLICT.getHttpStatusCode());

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
    @ExceptionHandler(OperationNotSupportedException.class)
    public ResponseEntity<ErrorPayload> operationNotSupportedException(HttpServletRequest request, HttpServletResponse response, OperationNotSupportedException e) throws IOException {

        ErrorPayload errorPayload = new ErrorPayload(DataCoreResponseCode.OPERATION_NOT_SUPPORTED.getDetailType(), DataCoreResponseCode.OPERATION_NOT_SUPPORTED.getReasonPhrase(), "The operation is not supported");
        if (request.getHeader(debugMessageKey) != null) {
            errorPayload.setDebugMessage(makeDebugMessage(e));
        }
        return new ResponseEntity<>(errorPayload, headers, DataCoreResponseCode.OPERATION_NOT_SUPPORTED.getHttpStatusCode());

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
}

