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
     * @param subscriptionVO ????????? ?????? body
     */
    @PostMapping("/csourceSubscriptions")
    public void createContextSourceRegistrationSubscription(HttpServletRequest request, 
    														HttpServletResponse response,
                                                            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                                                            @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
    														@RequestBody SubscriptionVO subscriptionVO) throws Exception {

        log.info("create csourceSubscriptions request. contentType={}, link={}, subscriptionVO={}", contentType, link, subscriptionVO);

        // 1. ???????????? ????????? ??????
        if (!subscriptionVO.getType().equalsIgnoreCase(DataServiceBrokerCode.JsonLdType.SUBSCRIPTION.getCode())) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include valid type");
        }

        subscriptionVO.setType(DataServiceBrokerCode.JsonLdType.CSOURCE_REGISTRATION_SUBSCRIPTION.getCode());

        Integer result = null;
        String id = null;
        try {
            // ?????? id??? ?????? ??????, ??????
            id = subscriptionVO.getId();
            if (id == null) {
                id = makeRandomCsourceRegistrationSubscriptionId();
                subscriptionVO.setId(id);
            }

            validateContext(subscriptionVO, contentType);

            // 2. ?????? ?????? ??????
            List<String> links = HttpHeadersUtil.extractLinkUris(link);
            result = subscriptionSVC.createSubscription(subscriptionVO, links);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.ALREADY_EXISTS, "Already Exists. csourceSubscriptionID=" + subscriptionVO.getId(), e);
        } catch (NgsiLdNoExistTypeException e) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, e.getMessage(), e);
        }

        // 3. ?????? ?????? ??????
        if (result != null) {
            response.setStatus(HttpStatus.CREATED.value());
            HashMap<String, String> resultMap = new HashMap<>();
            resultMap.put("id", id);
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(Constants.CHARSET_ENCODING);
            response.getWriter().print(objectMapper.writeValueAsString(resultMap));
        } else {
            //  ????????? ????????? ?????? ??????, ?????? ??????
            log.error("HTTP Binding ERROR");
        }
    }

    /**
     * ?????? ????????? ?????? (Query Subscriptions)
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

        // 1. subscription ??????
        Integer totalCount = subscriptionSVC.querySubscriptionsCount(limit, offset, DataServiceBrokerCode.JsonLdType.CSOURCE_REGISTRATION_SUBSCRIPTION);
        List<SubscriptionVO> resultList = subscriptionSVC.querySubscriptions(limit, offset, DataServiceBrokerCode.JsonLdType.CSOURCE_REGISTRATION_SUBSCRIPTION);

        // 2. ????????? property ????????? ?????? response body??? ????????? ??????
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
     * ?????? ?????? by subscriptionId
     * @param subscriptionId ?????? subscriptionId
     * @return
     */
    @LogExecutionTime
    @GetMapping("/csourceSubscriptions/{subscriptionId}")
    public @ResponseBody void retrieveSubscription(HttpServletResponse response,
                                                   @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                                   @PathVariable String subscriptionId) throws Exception {

        log.info("retrieve csourceSubscriptions request. accept={}, subscriptionId={}", accept, subscriptionId);

        // 1. ?????? ?????? ??????
        SubscriptionVO subscriptionVO = subscriptionSVC.retrieveSubscription(subscriptionId);

        // 2. ????????? subscription??? ?????? ??????, ResourceNotFound ??????
        // It is used when a client provided a subscription identifier (URI) not known to the system, see clause 6.3.2.
        if (subscriptionVO == null) {
            throw new NgsiLdResourceNotFoundException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID, "There is no an existing Subscription which id");
        }
        // 3. ????????? property ????????? ?????? response body??? ????????? ??????
        if (DataServiceBrokerCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + objectMapper.writeValueAsString(subscriptionVO));
        }

        if(!Constants.APPLICATION_LD_JSON_VALUE.equals(accept)) {
            subscriptionVO.setContext(null);
        }
        response.getWriter().print(objectMapper.writeValueAsString(subscriptionVO));
    }


    /**
     * ?????? ??????  (Delete Subscription)
     * @param response
     * @param subscriptionId ?????? ?????? subscriptionId
     */
    @DeleteMapping("/csourceSubscriptions/{subscriptionId}")
    public void delete(HttpServletRequest request,
                       HttpServletResponse response,
                       @PathVariable String subscriptionId) throws Exception {

        log.info("delete csourceSubscriptions request. subscriptionId={}", subscriptionId);

        // 1. ????????? ??????
        Integer result = subscriptionSVC.deleteSubscription(subscriptionId);
        // 2. ?????? ?????? ??????
        if (result > 0) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            //???????????? subscriptionId ?????? ??????
            throw new NgsiLdResourceNotFoundException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID, "There is no an existing Subscription which id");
        }
    }


    /**
     * ?????? ???????????? (Update Subscription)
     * @param response
     * @param subscriptionId ???????????? ?????? attribute ??????
     * @param subscriptionVO ????????? ?????? body
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
            //???????????? subscriptionId ?????? ??????
            throw new NgsiLdResourceNotFoundException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID, "There is no an existing Subscription which id");
        }

    }

    /**
     * 5.8.1.4
     * ?????? ?????? ???, SubscriptionId??? ?????? ??????, ?????? ??????
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
        // accept??? application/json ??? ??????
        if(Constants.APPLICATION_JSON_VALUE.equals(contentType)) {
            // contentType??? application/json??? ?????? @context ????????????
            if(!ValidateUtil.isEmptyData(subscriptionVO.getContext())) {
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                        "Invalid Request Content. @context parameter cannot be used in contentType=application/json");
            }

            // accept??? application/ld+json ??? ??????
        } else if(Constants.APPLICATION_LD_JSON_VALUE.equals(contentType)) {
            if(ValidateUtil.isEmptyData(subscriptionVO.getContext())) {
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                        "Invalid Request Content. @context is empty. contentType=application/ld+json");
            }
        }
    }
}
