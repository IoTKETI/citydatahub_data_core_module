package kr.re.keti.sc.dataservicebroker.subscription.controller;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdNoExistTypeException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdResourceNotFoundException;
import kr.re.keti.sc.dataservicebroker.subscription.service.SubscriptionSVC;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO;
import kr.re.keti.sc.dataservicebroker.util.HttpHeadersUtil;
import kr.re.keti.sc.dataservicebroker.util.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CsourceRegistrationSubscriptionController {

    @Autowired
    private SubscriptionSVC subscriptionSVC;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${datacore.http.binding.response.log.yn:N}")
    private String isResponseLog;
    @Value("${entity.retrieve.default.limit:1000}")
    private Integer defaultLimit;
    @Value("${entity.default.storage}")
    protected DataServiceBrokerCode.BigDataStorageType bigDataStorageType;

    /**
     * Create Context Source Registration Subscription
     * @param response
     * @param subscriptionVO 사용자 요청 body
     */
    @PostMapping("/csourceSubscriptions")
    public void createContextSourceRegistrationSubscription(HttpServletRequest request, 
    														HttpServletResponse response,
                                                            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                                                            @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
    														@RequestBody SubscriptionVO subscriptionVO) throws Exception {

        log.info("create csourceSubscriptions request. contentType={}, link={}, subscriptionVO={}", contentType, link, subscriptionVO);

        // 1. 파라미터 유효성 체크
        if (!subscriptionVO.getType().equalsIgnoreCase(DataServiceBrokerCode.JsonLdType.SUBSCRIPTION.getCode())) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include valid type");
        }

        subscriptionVO.setType(DataServiceBrokerCode.JsonLdType.CSOURCE_REGISTRATION_SUBSCRIPTION.getCode());

        Integer result = null;
        String id = null;
        try {
            // 구독 id가 없는 경우, 생성
            id = subscriptionVO.getId();
            if (id == null) {
                id = makeRandomCsourceRegistrationSubscriptionId();
                subscriptionVO.setId(id);
            }

            validateContext(subscriptionVO, contentType);

            // 2. 구독 생성 요청
            List<String> links = HttpHeadersUtil.extractLinkUris(link);
            result = subscriptionSVC.createSubscription(subscriptionVO, links);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.ALREADY_EXISTS, "Already Exists. csourceSubscriptionID=" + subscriptionVO.getId(), e);
        } catch (NgsiLdNoExistTypeException e) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, e.getMessage(), e);
        }

        // 3. 요청 결과 확인
        if (result != null) {
            response.setStatus(HttpStatus.CREATED.value());
            HashMap<String, String> resultMap = new HashMap<>();
            resultMap.put("id", id);
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(Constants.CHARSET_ENCODING);
            response.getWriter().print(objectMapper.writeValueAsString(resultMap));
        } else {
            //  생성간 에러가 있는 경우, 예외 발생
            log.error("HTTP Binding ERROR");
        }
    }

    /**
     * 구독 리스트 조회 (Query Subscriptions)
     * @param response
     * @param accept
     * @param limit    Maximum number of subscriptions to be retrieved
     * @throws Exception
     */
    @LogExecutionTime
    @GetMapping(value = "/csourceSubscriptions")
    public @ResponseBody void querySubscriptions(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                                 @RequestParam(value = "limit", required = false) Integer limit,
                                                 @RequestParam(value = "offset", required = false) Integer offset) throws Exception {

        log.info("query csourceSubscriptions request. accept={}, limit={}, offset={}", accept, limit, offset);

        // 1. subscription 조회
        Integer totalCount = subscriptionSVC.querySubscriptionsCount(limit, offset, DataServiceBrokerCode.JsonLdType.CSOURCE_REGISTRATION_SUBSCRIPTION);
        List<SubscriptionVO> resultList = subscriptionSVC.querySubscriptions(limit, offset, DataServiceBrokerCode.JsonLdType.CSOURCE_REGISTRATION_SUBSCRIPTION);

        // 2. 설정에 property 조건에 따라 response body를 로그로 남김
        if (DataServiceBrokerCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + objectMapper.writeValueAsString(resultList));
        }

        if(!Constants.APPLICATION_LD_JSON_VALUE.equals(accept)) {
            if(!ValidateUtil.isEmptyData(resultList)) {
                for(SubscriptionVO subscriptionVO : resultList) {
                    subscriptionVO.setContext(null);
                }
            }
        }

        HttpHeadersUtil.addPaginationLinkHeader(bigDataStorageType, request, response, accept, limit, offset, totalCount, defaultLimit);
        response.getWriter().print(objectMapper.writeValueAsString(resultList));
    }

    /**
     * 구독 조회 by subscriptionId
     * @param subscriptionId 요청 subscriptionId
     * @return
     */
    @LogExecutionTime
    @GetMapping("/csourceSubscriptions/{subscriptionId}")
    public @ResponseBody void retrieveSubscription(HttpServletResponse response,
                                                   @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                                   @PathVariable String subscriptionId) throws Exception {

        log.info("retrieve csourceSubscriptions request. accept={}, subscriptionId={}", accept, subscriptionId);

        // 1. 구독 단건 조회
        SubscriptionVO subscriptionVO = subscriptionSVC.retrieveSubscription(subscriptionId);

        // 2. 조회된 subscription가 없을 경우, ResourceNotFound 처리
        // It is used when a client provided a subscription identifier (URI) not known to the system, see clause 6.3.2.
        if (subscriptionVO == null) {
            throw new NgsiLdResourceNotFoundException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID, "There is no an existing Subscription which id");
        }
        // 3. 설정에 property 조건에 따라 response body를 로그로 남김
        if (DataServiceBrokerCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + objectMapper.writeValueAsString(subscriptionVO));
        }

        if(!Constants.APPLICATION_LD_JSON_VALUE.equals(accept)) {
            subscriptionVO.setContext(null);
        }
        response.getWriter().print(objectMapper.writeValueAsString(subscriptionVO));
    }


    /**
     * 구독 삭제  (Delete Subscription)
     * @param response
     * @param subscriptionId 삭제 대상 subscriptionId
     */
    @DeleteMapping("/csourceSubscriptions/{subscriptionId}")
    public void delete(HttpServletRequest request,
                       HttpServletResponse response,
                       @PathVariable String subscriptionId) throws Exception {

        log.info("delete csourceSubscriptions request. subscriptionId={}", subscriptionId);

        // 1. 리소스 삭제
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
    @PatchMapping("/csourceSubscriptions/{subscriptionId}")
    public void updateSubscription(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                                   @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                                   @PathVariable("subscriptionId") String subscriptionId,
                                   @RequestBody SubscriptionVO subscriptionVO) throws Exception {

        log.info("update csourceSubscription request. contentType={}, link={}, subscriptionVO={}, subscriptionId={}",
                contentType, link, subscriptionVO, subscriptionId);

        subscriptionVO.setId(subscriptionId);

        validateContext(subscriptionVO, contentType);

        List<String> links = HttpHeadersUtil.extractLinkUris(link);
        Integer resultCnt = subscriptionSVC.updateSubscription(subscriptionId, subscriptionVO, links);

        if (resultCnt > 0) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            //일치하는 subscriptionId 없는 경우
            throw new NgsiLdResourceNotFoundException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID, "There is no an existing Subscription which id");
        }

    }

    /**
     * 5.8.1.4
     * 구독 생성 시, SubscriptionId가 없을 경우, 자동 생성
     * If the subscription document does not include a Subscription identifier,
     * a new identifier (URI) shall be automatically generated by the implementation.
     * @return
     */
    private String makeRandomCsourceRegistrationSubscriptionId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String id = Constants.PREFIX_CSOURCE_REGISTRATION_SUBSCRIPTION_ID + uuid.substring(0, 10);
        return id;
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
}
