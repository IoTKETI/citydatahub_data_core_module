package kr.re.keti.sc.datamanager.util;

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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import kr.re.keti.sc.datamanager.common.code.Constants;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.exception.LengthRequiredException;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP Request header filter class (Extends Spring OncePerRequestFilter)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class HeaderCheckFilter extends OncePerRequestFilter {

    private final String EXTENDED_MEDIATYPE_APPLICATION_LD_JSON  = "application/ld+json";
    private final String EXTENDED_MEDIATYPE_APPLICATION_MERGE_PATCH_JSON  = "application/merge-patch+json";

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

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

        List<String> acceptNameList = Arrays.asList(new String[]{MediaType.APPLICATION_JSON_VALUE, EXTENDED_MEDIATYPE_APPLICATION_LD_JSON});
        String accept = request.getHeader(HttpHeaders.ACCEPT);

        if (acceptNameList.contains(accept)) {

            if (accept.equalsIgnoreCase(MediaType.ALL_VALUE)) {

                request.setAttribute(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            }
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(Constants.CHARSET_ENCODING);

            return response;
        } else {


            log.warn("Accept header shall be application/json, application/ld+json or */*");
            resolver.resolveException(request, response, null, new HttpMediaTypeNotSupportedException("Accept header shall be application/json, application/ld+json or */*"));
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

        List<String> accetedContentTypeList = Arrays.asList(MediaType.APPLICATION_JSON_VALUE, "application/ld+json");
        String contentType = request.getContentType();

        if (request.getHeader(HttpHeaders.CONTENT_LENGTH) == null) {

            log.warn("Content-Length header shall include the length of the input payload.");
            resolver.resolveException(request, response, null, new LengthRequiredException(DataManagerCode.ErrorCode.LENGTH_REQUIRED
                    , "Content-Length header shall include the length of the input payload."));

            return null;
        }

        if (!accetedContentTypeList.contains(contentType)) {
            log.warn("Accept header shall be " + StringUtils.join(accetedContentTypeList, ","));

            resolver.resolveException(request, response, null, new HttpMediaTypeNotSupportedException("Accept header shall be " + StringUtils.join(accetedContentTypeList, ",")));
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

        List<String> accetedContentTypeList = Arrays.asList(MediaType.APPLICATION_JSON_VALUE, EXTENDED_MEDIATYPE_APPLICATION_LD_JSON, EXTENDED_MEDIATYPE_APPLICATION_MERGE_PATCH_JSON);

        if (request.getHeader(HttpHeaders.CONTENT_LENGTH) == null) {
            log.warn("Content-Length header shall include the length of the input payload.");
            resolver.resolveException(request, response, null, new LengthRequiredException(DataManagerCode.ErrorCode.LENGTH_REQUIRED
                    , "Content-Length header shall include the length of the input payload."));

            return null;
        }

        if (!accetedContentTypeList.contains(contentType)) {
            log.warn("Accept header shall be " + StringUtils.join(accetedContentTypeList, ","));
            resolver.resolveException(request, response, null, new HttpMediaTypeNotSupportedException("Accept header shall be " + StringUtils.join(accetedContentTypeList, ",")));
            return null;

        }

        return response;
    }

}