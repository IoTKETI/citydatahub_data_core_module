package kr.re.keti.sc.datacoreui.util;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode;
import kr.re.keti.sc.datacoreui.common.code.ResponseCode;
import kr.re.keti.sc.datacoreui.common.exception.BadRequestException;
import kr.re.keti.sc.datacoreui.common.exception.ErrorPayload;

/**
 * Utility for error
 * @FileName ErrorUtil.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
public class ErrorUtil {

    private static final Logger logger = LoggerFactory.getLogger(ErrorUtil.class);


    /**
     * Returns the DB Column name and type of Attribute.
     * ex) NAME::text
     *
     * @param reqAttrName
     * @return
     */
    public static String getColumnNameWithType(String reqAttrName) {

        String reqRootAttrName = "";
        String reqChildAttributeName = "";
        String columnName;
        if (reqAttrName.contains("[")) {

            reqRootAttrName = reqAttrName.split("\\[")[0];
            reqChildAttributeName = StringUtils.substringBetween(reqAttrName, "[", "]");

            columnName = reqRootAttrName + "_" + reqChildAttributeName;

        } else {

            reqRootAttrName = reqAttrName;

            columnName = reqRootAttrName;
        }

        columnName = columnName.replace(".", "_");

        return columnName;
    }


    /**
     * Change Exception to ErrorPayload
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
            if (errorCode.equals(DataCoreUiCode.ErrorCode.ALREADY_EXISTS.getCode())) {
                // Common handling of errors when there are duplicate ids during CREATE (409)
                errorPayload = new ErrorPayload(ResponseCode.CONFLICT.getDetailType(), ResponseCode.CONFLICT.getReasonPhrase(), e.getMessage());
            } else {
                // Error Common Handling for Bad Requests (400)
                errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), ResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), e.getMessage());
            }

        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            errorPayload = new ErrorPayload(ResponseCode.METHOD_NOT_ALLOWED.getDetailType(), ResponseCode.METHOD_NOT_ALLOWED.getReasonPhrase(), e.getMessage());
        } else if (e instanceof HttpMediaTypeNotSupportedException) {
            errorPayload = new ErrorPayload(ResponseCode.UNSUPPORTED_MEDIA_TYPE.getDetailType(), ResponseCode.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(), e.getMessage());
        } else if (e instanceof HttpMessageNotReadableException) {
            errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), ResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), "No HttpInputMessage available");
        } else if (e instanceof java.sql.SQLException) {
            errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), ResponseCode.BAD_REQUEST_DATA.getReasonPhrase(), "query error");
        } else if (e instanceof JsonProcessingException) {
            errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), ResponseCode.INVALID_REQUEST.getReasonPhrase(), "json parsing error");
        } else if (e instanceof JSONException) {
            errorPayload = new ErrorPayload(ResponseCode.BAD_REQUEST_DATA.getDetailType(), ResponseCode.INVALID_REQUEST.getReasonPhrase(), "json parsing error");
        }

        return errorPayload;
    }

}
