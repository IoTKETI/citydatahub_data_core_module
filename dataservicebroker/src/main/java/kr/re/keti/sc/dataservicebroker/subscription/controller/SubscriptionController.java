package kr.re.keti.sc.dataservicebroker.subscription.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdContextNotAvailableException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdNoExistTypeException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdResourceNotFoundException;
import kr.re.keti.sc.dataservicebroker.subscription.service.SubscriptionSVC;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO.EntityInfo;
import kr.re.keti.sc.dataservicebroker.util.HttpHeadersUtil;
import kr.re.keti.sc.dataservicebroker.util.LogExecutionTime;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class SubscriptionController {

    @Autowired
    private SubscriptionSVC subscriptionSVC;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DataModelManager dataModelManager;

    @Value("${datacore.http.binding.response.log.yn:N}")
    private String isResponseLog;
    @Value("${entity.retrieve.default.limit:1000}")
    private Integer defaultLimit;
    @Value("${entity.default.storage}")
    protected DataServiceBrokerCode.BigDataStorageType bigDataStorageType;
    @Value("${entity.default.context-uri}")
    private String defaultContextUri;

    /**
     * 구독 리스트 조회 (Query Subscriptions)
     * @param response
     * @param accept
     * @param limit   Maximum number of subscriptions to be retrieved
     * @throws Exception
     */
    @LogExecutionTime
    @GetMapping(value = "/subscriptions")
    public @ResponseBody void querySubscriptions(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                                 @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                                                 @RequestParam(value = "limit", required = false) Integer limit,
                                                 @RequestParam(value = "offset", required = false) Integer offset ) throws Exception {

        log.info("query Subscriptions request. accept={}, limit={}, offset={}", accept, limit, offset);

        // 1. subscription 조회
        Integer totalCount = subscriptionSVC.querySubscriptionsCount(limit, offset, DataServiceBrokerCode.JsonLdType.SUBSCRIPTION);
        List<SubscriptionVO> resultList = subscriptionSVC.querySubscriptions(limit, offset, DataServiceBrokerCode.JsonLdType.SUBSCRIPTION);

        // 2. 설정에 property 조건에 따라 response body를 로그로 남김
        if (DataServiceBrokerCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + objectMapper.writeValueAsString(resultList));
        }

        String primaryAccept = HttpHeadersUtil.getPrimaryAccept(accept);
        
        List<String> links = getLinkOrDefault(HttpHeadersUtil.extractLinkUris(link));

        if(!ValidateUtil.isEmptyData(resultList)) {
            resultList.forEach(subscriptionVO -> {
                subscriptionVO.expandTerm(
                        dataModelManager.contextToFlatMap(links),
                        dataModelManager.contextToFlatMap(subscriptionVO.getContext())
                );
                if(Constants.APPLICATION_LD_JSON_VALUE.equals(primaryAccept)) {
                    subscriptionVO.setContext(links);
                } else {
                    subscriptionVO.setContext(null);
                }
            });
        }

        HttpHeadersUtil.addPaginationLinkHeader(bigDataStorageType, request, response, accept, limit, offset, totalCount, defaultLimit);
        HttpHeadersUtil.addContextLinkHeader(response, accept, getLinkHeaderByAccept(accept, links));

        response.getWriter().print(objectMapper.writeValueAsString(resultList));
    }

    /**
     * 구독 조회 by subscriptionId
     * @param subscriptionId 요청 subscriptionId
     * @return
     */
    @LogExecutionTime
    @GetMapping("/subscriptions/{subscriptionId}")
    public @ResponseBody
    void retrieveSubscription(	HttpServletResponse response, 
					    		@RequestHeader(HttpHeaders.ACCEPT) String accept,
                                @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
					    		@PathVariable String subscriptionId) throws Exception {

        log.info("retrieve Subscriptions request. accept={}, subscriptionId={}", accept, subscriptionId);

        // 1. 구독 단건 조회
        SubscriptionVO result = subscriptionSVC.retrieveSubscription(subscriptionId);

        // 2. 조회된 subscription가 없을 경우, ResourceNotFound 처리
        // It is used when a client provided a subscription identifier (URI) not known to the system, see clause 6.3.2.
        if (result == null) {
            throw new NgsiLdResourceNotFoundException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID, "There is no an existing Subscription which id");
        }
        // 3. 설정에 property 조건에 따라 response body를 로그로 남김
        if (DataServiceBrokerCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + objectMapper.writeValueAsString(result));
        }
        
        String primaryAccept = HttpHeadersUtil.getPrimaryAccept(accept);

        List<String> links = getLinkOrDefault(HttpHeadersUtil.extractLinkUris(link));

        result.expandTerm(
                dataModelManager.contextToFlatMap(links),
                dataModelManager.contextToFlatMap(result.getContext())
        );

        if(Constants.APPLICATION_LD_JSON_VALUE.equals(primaryAccept)) {
            result.setContext(links);
        } else {
            result.setContext(null);
        }

        HttpHeadersUtil.addContextLinkHeader(response, accept, getLinkHeaderByAccept(accept, links));
        response.getWriter().print(objectMapper.writeValueAsString(result));

    }

    /**
     * 구독 생성 (Create Subscription)
     * @param response
     * @param subscriptionVO 사용자 요청 body
     */
    @PostMapping("/subscriptions")
    public void createSubscription(	HttpServletRequest request,
    								HttpServletResponse response,
                                    @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                                    @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
    								@RequestBody SubscriptionVO subscriptionVO) throws Exception {

        log.info("create Subscription request. contentType={}, link={}, subscriptionVO={}", contentType, link, subscriptionVO);

        // 1. 구독 type 추가 (* ContextSourceRegistration와 구분)
        validateTypeParameter(subscriptionVO.getType());
        subscriptionVO.setType(DataServiceBrokerCode.JsonLdType.SUBSCRIPTION.getCode());

        Integer result = null;
        try {
            // 1. context 정보 유효성 체크
            validateContext(subscriptionVO, contentType);

            // 2. 구독 생성 요청
            List<String> links = HttpHeadersUtil.extractLinkUris(link);
            result = subscriptionSVC.createSubscription(subscriptionVO, links);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.ALREADY_EXISTS, "Already Exists. subscriptionID=" + subscriptionVO.getId(), e);
        } catch (NgsiLdNoExistTypeException e) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, e.getMessage(), e);
        } catch (NgsiLdContextNotAvailableException e) {
        	throw e;
        }

        // 3. 요청 결과 확인
        if (result != null) {
            response.setStatus(HttpStatus.CREATED.value());
            SubscriptionVO resultVO = new SubscriptionVO();
            resultVO.setId(subscriptionVO.getId());
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(Constants.CHARSET_ENCODING);
            response.getWriter().print(objectMapper.writeValueAsString(resultVO));
        } else {
            //  생성간 에러가 있는 경우, 예외 발생
            log.error("HTTP Binding ERROR");
        }
    }

    private void validateTypeParameter(String type) {
        if(!DataServiceBrokerCode.JsonLdType.SUBSCRIPTION.getCode().equals(type)) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                    "should equal type=" + DataServiceBrokerCode.JsonLdType.SUBSCRIPTION.getCode());
        }
    }

    /**
     * 구독 삭제  (Delete Subscription)
     * @param response
     * @param subscriptionId 삭제 대상 subscriptionId
     */
    @DeleteMapping("/subscriptions/{subscriptionId}")
    public void delete(HttpServletRequest request,
                       HttpServletResponse response,
                       @PathVariable String subscriptionId) throws Exception {

        log.info("delete Subscription request. subscriptionId={}", subscriptionId);

        // 1. 리소스 삭제 요청
        Integer result = subscriptionSVC.deleteSubscription(subscriptionId);
        // 2. 요청 결과 확인
        if (result > 0) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            //일치하는 subscriptionId 없는 경우
            throw new NgsiLdResourceNotFoundException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID, "There is no an existing Subscription which id");
        }
    }


    /**
     * 구독 업데이트 (Update Subscription)
     * @param response
     * @param subscriptionId 업데이트 대상 attribute 항목
     * @param subscriptionVO 사용자 요청 body
     */
    @PatchMapping("/subscriptions/{subscriptionId}")
    public void updateSubscription(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                                   @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                                   @RequestBody SubscriptionVO subscriptionVO,
                                   @PathVariable("subscriptionId") String subscriptionId) throws Exception {

        log.info("update Subscription request. contentType={}, link={}, subscriptionVO={}, subscriptionId={}",
                contentType, link, subscriptionVO, subscriptionId);

        // 1. 구독 type 추가 (* ContextSourceRegistration와 구분)
        subscriptionVO.setId(subscriptionId);
        subscriptionVO.setType(DataServiceBrokerCode.JsonLdType.SUBSCRIPTION.getCode());

        // 2. context 정보 유효성 체크
        validateContext(subscriptionVO, contentType);

        // 3. 구독 업데이트 요청
        List<String> links = HttpHeadersUtil.extractLinkUris(link);
        Integer resultCnt = subscriptionSVC.updateSubscription(subscriptionId, subscriptionVO, links);

        // 3. 요청 결과 확인
        if (resultCnt > 0) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            //일치하는 subscriptionId 없는 경우
            throw new NgsiLdResourceNotFoundException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID, "There is no an existing Subscription which id");
        }

    }

    private void validateContext(SubscriptionVO subscriptionVO, String contentType) {
        // accept가 application/json 인 경우
        if(Constants.APPLICATION_JSON_VALUE.equals(contentType)) {
            // contentType이 application/json인 경우 @context 입력불가
            if(!ValidateUtil.isEmptyData(subscriptionVO.getContext())) {
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                        "Invalid Request Content. @context parameter cannot be used in contentType=application/json");
            }

        // accept가 application/ld+json 인 경우
        } else if(Constants.APPLICATION_LD_JSON_VALUE.equals(contentType)) {
            if(ValidateUtil.isEmptyData(subscriptionVO.getContext())) {
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                        "Invalid Request Content. @context is empty. contentType=application/ld+json");
            }
        }
    }

    private List<String> getLinkHeaderByAccept(String accept, List<String> link) {

        if (Constants.APPLICATION_LD_JSON_VALUE.equals(accept)) {
            return null;
        }

        if (!ValidateUtil.isEmptyData(link)) {
            return link;
        }
        return Collections.singletonList(defaultContextUri);
    }

    private List<String> getLinkOrDefault(List<String> link) {
        if (!ValidateUtil.isEmptyData(link)) {
            return link;
        }
        return Collections.singletonList(defaultContextUri);
    }

}
