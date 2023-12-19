package kr.re.keti.sc.dataservicebroker.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;

@Component
public class HttpHeadersUtil {

	private static Pattern LINK_URI_PATTERN = Pattern.compile("<(?<link>[^<>]*)>");
	
    private static String PRIMARY_ACCEPT;

    @Value("${entity.retrieve.primary.accept:application/json}")
	public void setPrimaryAccept (String primaryAccept) {
    	HttpHeadersUtil.PRIMARY_ACCEPT = primaryAccept;
	}
	
    /**
     *  "application/json" 로 요청한 경우, link에 context 정보 추가
     * @param response
     * @param accept
     * @param contexts
     */
    public static void addContextLinkHeader(HttpServletResponse response, String accept, List<String> contexts) {
        if (accept.equals(MediaType.APPLICATION_JSON_VALUE) && !ValidateUtil.isEmptyData(contexts)) {
            StringBuilder contextLinkStrBuilder = new StringBuilder();
            String tmpLink = response.getHeader(HttpHeaders.LINK);
            List<String> linkContentList = new ArrayList<>();
            if (!ValidateUtil.isEmptyData(tmpLink)) {
                linkContentList = new ArrayList(Arrays.asList(StringUtils.splitPreserveAllTokens(tmpLink, ",")));
            }
            contextLinkStrBuilder.append("<");
            contextLinkStrBuilder.append(StringUtils.join(contexts, ","));
            contextLinkStrBuilder.append(">");
            String contextLinkStr = contextLinkStrBuilder.toString();
            linkContentList.add(contextLinkStr);
            response.setHeader(HttpHeaders.LINK, StringUtils.join(linkContentList, ","));
        }
    }

    /**
     * @param response
     * @param request
     * @param accept
     * @param limit
     * @param offset
     * @param totalCount
     * @param defaultLimit //Link: <http://localhost:8080/currency?page=2&size=25>; rel="next",<http://localhost:8080/currency?page=4&size=25>; rel="last"
     */
    public static void addPaginationLinkHeader(DataServiceBrokerCode.BigDataStorageType bigDataStorageType, HttpServletRequest request, HttpServletResponse response, String accept, Integer limit, Integer offset, Integer totalCount, Integer defaultLimit) {

        if (bigDataStorageType != DataServiceBrokerCode.BigDataStorageType.RDB) {
            // RDB인 경우만 지원
            return;
        }
        if (totalCount == null) {
            return;
        }

        if (limit == null) {
            limit = defaultLimit;
        }
        if (offset == null) {
            offset = 0;
        }

        if (totalCount <= limit) {
            // total 보다 limit가 작으면 아무것도 할 필요가 없음
            return;
        }

        if (limit > defaultLimit) {
            limit = defaultLimit;
        }

        List<String> linkHeaders = new ArrayList<>();

        int contentOffset = offset + limit;


        if ((offset > 0) || (totalCount < contentOffset)) {

            //add prev page
            int prevOffset = offset - limit;

            if (prevOffset < 0) {
                prevOffset = 0;
            }
            String prevHeader = generateLinkPointer("prev", request, accept, prevOffset, limit);
            if (prevHeader != null) {
                linkHeaders.add(prevHeader);
            }
        }


        if (totalCount > contentOffset) {
            //add next page
            int nextOffset = limit + offset;
            String nextHeader = generateLinkPointer("next", request, accept, nextOffset, limit);

            if (nextHeader != null) {
                linkHeaders.add(nextHeader);
            }
        }

        //link 내 type(application/ld+json) 정보 추가
        if (!ValidateUtil.isEmptyData(linkHeaders) && linkHeaders.size() > 0) {
            String typeMsg = " type=\"" + accept + "\"";
            linkHeaders.add(typeMsg);
        }

        String responseLink = StringUtils.join(linkHeaders, ",");


        response.setHeader(HttpHeaders.LINK, responseLink);

    }

    /**
     * link 헤더의 pointer 정보 생성
     *
     * @param type
     * @param request
     * @param accept
     * @param offset
     * @param limit
     * @return
     */
    private static String generateLinkPointer(String type, HttpServletRequest request, String accept, Integer offset, Integer limit) {

        String requestURI = request.getRequestURI();
//        String typeMsg = "; type=\"" + accept + "\"";
        String relMsg = "; rel=\"" + type + "\"";


        StringBuilder prevLinkStrBuilder = new StringBuilder();

        prevLinkStrBuilder.append("<");
        prevLinkStrBuilder.append(requestURI);
        prevLinkStrBuilder.append("?");

        Map<String, String[]> requestParamMap = new HashMap<>(request.getParameterMap());

        requestParamMap.put(DataServiceBrokerCode.DbConditionType.OFFSET.getCode(), new String[]{offset.toString()});
        requestParamMap.put(DataServiceBrokerCode.DbConditionType.LIMIT.getCode(), new String[]{limit.toString()});

        int idx = 0;
        for (Map.Entry<String, String[]> entry : requestParamMap.entrySet()) {

            if (idx != 0) {
                prevLinkStrBuilder.append("&");
            }

            String key = entry.getKey();
            String value = entry.getValue()[0];

            prevLinkStrBuilder.append(key);
            prevLinkStrBuilder.append("=");
            prevLinkStrBuilder.append(value);
            idx++;
        }

        prevLinkStrBuilder.append(">");
//        prevLinkStrBuilder.append(typeMsg);
        prevLinkStrBuilder.append(relMsg);

        return prevLinkStrBuilder.toString();
    }

    public static MultiValueMap<String, String> getDefaultHeaders() {
        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        headerMap.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headerMap;
    }
    
    public static List<String> extractLinkUris(String link) {
    	if(ValidateUtil.isEmptyData( link)) return null;
    	List<String> result = null;
		Matcher matcherForUpdate = LINK_URI_PATTERN.matcher(link);

		while(matcherForUpdate.find()) {
			if(result == null) {
				result = new ArrayList<>();
			}
			String linkStr = matcherForUpdate.group();
			result.add(linkStr.replace("<", "").replace(">", ""));
		}
		return result;
    }
    
    public static String getPrimaryAccept(String requestAccept) {

        if(ValidateUtil.isEmptyData(requestAccept)) {
            return requestAccept;
        }

        // 요청 accept가 다건인 경우
        if (requestAccept.contains(Constants.ACCEPT_ALL)) {
            return PRIMARY_ACCEPT;
        } else if (requestAccept.contains(PRIMARY_ACCEPT)) {
            return PRIMARY_ACCEPT;
        } else if (requestAccept.contains(Constants.APPLICATION_LD_JSON_VALUE)) {
            return Constants.APPLICATION_LD_JSON_VALUE;
        } else if (requestAccept.contains(Constants.APPLICATION_JSON_VALUE)) {
            return Constants.APPLICATION_JSON_VALUE;
        } else if (requestAccept.contains(Constants.APPLICATION_GEO_JSON_VALUE)) {
            return Constants.APPLICATION_GEO_JSON_VALUE;
        }

        return requestAccept;
    }

}