package kr.re.keti.sc.dataservicebroker.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdLengthRequiredException;

/**
 * HTTP Request header filter class (Extends Spring OncePerRequestFilter)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HeaderCheckFilter extends OncePerRequestFilter {

    private final String SUPPORT_MEDIATYPE_APPLICATION_GEO_JSON_API = "/entities";

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;
    @Value("${entity.retrieve.primary.accept:application/json}")
    private String primaryAccept;

    private List<String> allowAcceptNameList = Arrays.asList(Constants.ACCEPT_ALL, Constants.APPLICATION_JSON_VALUE, Constants.APPLICATION_LD_JSON_VALUE, Constants.APPLICATION_GEO_JSON_VALUE);
    private List<String> allowPostContentTypeList = Arrays.asList(Constants.APPLICATION_JSON_VALUE, Constants.APPLICATION_LD_JSON_VALUE);
    private List<String> allowPatchContentTypeList = Arrays.asList(Constants.APPLICATION_JSON_VALUE, Constants.APPLICATION_LD_JSON_VALUE, Constants.APPLICATION_MERGE_PATCH_JSON_VALUE);


    /**
     * controller annotation으로 처리 불가능한 HTTP header 관련 필터링
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String method = request.getMethod();

        // 6.3.4 HTTP request preconditions
        // POST, PATCH method 관련 조건
        // Content-Length header shall include the length of the input payload.
        if (method.equals(HttpMethod.GET.name())) {

            response = filterGETMethod(request, response);

        } else if (method.equals(HttpMethod.POST.name())) {

            response = filterPOSTMethod(request, response);

        } else if (method.equals(HttpMethod.PATCH.name())) {

            response = filterPATCHMethod(request, response);

        }

        if (response != null) {
            filterChain.doFilter(request, response);

        } else {

            return;
        }

    }

    /**
     * GET method 필수 헤더 필터링
     *
     * @param request
     * @param response
     * @return
     */
    private HttpServletResponse filterGETMethod(HttpServletRequest request, HttpServletResponse response) {

        // ETSI GS CIM 009 V1.1.1 (2019-01)
        // Accept header shall be "application/json", "application/ld+json" or "*/*".
        // A non-present accept header is also allowed. In that particular case "application/json" shall be assumed


        // ETSI GS CIM 009 V1.2.2 (2020-02)로 변경 적용
        // For GET HTTP requests implementations shall check the following preconditions:
        //• Accept header shall include (or define a media range that can be expanded to) "application/json" or "application/ld+json".

    	
    	// ETSI GS CIM 009 V1.4.1 (2021-11)로 변경 적용
        // For GET HTTP requests implementations shall check the following preconditions:
        // •	Accept header shall include (or define a media range that can be expanded to):
    	// -	"application/ld+json"
    	// -	"application/geo+json"
    	// -	"application/json"
    	
        String accept = request.getHeader(HttpHeaders.ACCEPT);

        // 요청 accept가 다건인 경우
        if(accept != null) {
            if (accept.contains(Constants.ACCEPT_ALL)) {
                accept = primaryAccept;
            } else if (accept.contains(primaryAccept)) {
                accept = primaryAccept;
            } else if (accept.contains(Constants.APPLICATION_LD_JSON_VALUE)) {
                accept = Constants.APPLICATION_LD_JSON_VALUE;
            } else if (accept.contains(Constants.APPLICATION_JSON_VALUE)) {
                accept = Constants.APPLICATION_JSON_VALUE;
            } else if (accept.contains(Constants.APPLICATION_GEO_JSON_VALUE)) {
                accept = Constants.APPLICATION_GEO_JSON_VALUE;
            }

            // Request URI 가 /entities 일 경우에만 application/geo+json 지원
            if (accept.contains(Constants.APPLICATION_GEO_JSON_VALUE)) {
                if (!request.getRequestURI().startsWith(SUPPORT_MEDIATYPE_APPLICATION_GEO_JSON_API)) {
                    logger.warn("application/geo+json is supported only for /entities requests.");
                    resolver.resolveException(request, response, null, new HttpMediaTypeNotAcceptableException("application/geo+json is supported only for /entities requests."));
                    return null;
                }
            }
        }

        boolean validAccept = false;
        for(String allowAcceptName : allowAcceptNameList) {
            if(accept.contains(allowAcceptName)) {
                validAccept = true;
            }
        }

        if (validAccept) {
            response.setHeader(HttpHeaders.CONTENT_TYPE, accept);
            response.setCharacterEncoding(Constants.CHARSET_ENCODING);
            return response;
        } else {
            logger.warn("Accept header shall be application/json, application/ld+json, application/geo+json or */*");
            resolver.resolveException(request, response, null, new HttpMediaTypeNotAcceptableException("Accept header shall either be application/json or application/ld+json for GET request."));
            return null;
        }
    }


    /**
     * POST 요청 필수 헤더 필터링
     *
     * @param request
     * @param response
     * @return
     */
    private HttpServletResponse filterPOSTMethod(HttpServletRequest request, HttpServletResponse response) {

        String contentType = request.getContentType();

        if (request.getHeader(HttpHeaders.CONTENT_LENGTH) == null) {

            logger.warn("Content-Length header shall include the length of the input payload.");
            resolver.resolveException(request, response, null, new NgsiLdLengthRequiredException(DataServiceBrokerCode.ErrorCode.LENGTH_REQUIRED
                    , "Content-Length header shall include the length of the input payload."));

            return null;
        }

        boolean isValidContentType = false;
        for(String allowContentType : allowPostContentTypeList) {
            if(contentType.contains(allowContentType)) {
                isValidContentType = true;
            }
        }

        if (!isValidContentType) {
            logger.warn("Content-Type header shall be " + StringUtils.join(allowPostContentTypeList, ",") + ". Request Content-Type=" + contentType);
            resolver.resolveException(request, response, null, new HttpMediaTypeNotSupportedException("Content-Type header shall be " + StringUtils.join(allowPostContentTypeList, ",")));
            return null;
        }

        return response;
    }


    /**
     * PATCH 요청 필수 헤더 필터링
     *
     * @param request
     * @param response
     * @return
     */
    private HttpServletResponse filterPATCHMethod(HttpServletRequest request, HttpServletResponse response) {

        String contentType = request.getContentType();

        if (request.getHeader(HttpHeaders.CONTENT_LENGTH) == null) {
            logger.warn("Content-Length header shall include the length of the input payload.");
            resolver.resolveException(request, response, null, new NgsiLdLengthRequiredException(DataServiceBrokerCode.ErrorCode.LENGTH_REQUIRED
                    , "Content-Length header shall include the length of the input payload."));
            return null;
        }

        boolean isValidContentType = false;
        for(String allowContentType : allowPatchContentTypeList) {
            if(contentType.contains(allowContentType)) {
                isValidContentType = true;
            }
        }

        if (!isValidContentType) {
            logger.warn("Content-Type header shall be " + StringUtils.join(allowPatchContentTypeList, ",") + ". Request Content-Type=" + contentType);
            resolver.resolveException(request, response, null, new HttpMediaTypeNotSupportedException("Content-Type header shall be " + StringUtils.join(allowPatchContentTypeList, ",")));
            return null;
        }

        return response;
    }

}