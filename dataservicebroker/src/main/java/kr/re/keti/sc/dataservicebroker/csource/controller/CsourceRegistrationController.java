package kr.re.keti.sc.dataservicebroker.csource.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.JsonLdType;
import kr.re.keti.sc.dataservicebroker.common.code.SubscriptionCode.TriggerReason;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdNoExistTypeException;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.csource.service.CsourceRegistrationSVC;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationVO;
import kr.re.keti.sc.dataservicebroker.notification.vo.CsourceNotificationVO;
import kr.re.keti.sc.dataservicebroker.subscription.service.SubscriptionSVC;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionBaseDaoVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionRetrieveVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO;
import kr.re.keti.sc.dataservicebroker.util.HttpHeadersUtil;
import kr.re.keti.sc.dataservicebroker.util.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CsourceRegistrationController {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CsourceRegistrationSVC csourceRegistrationSVC;
    @Autowired
    private SubscriptionSVC subscriptionSVC;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${entity.retrieve.default.limit:1000}")
    private Integer defaultLimit;
    @Value("${entity.default.storage}")
    protected DataServiceBrokerCode.BigDataStorageType bigDataStorageType;
    

    /**
     * context source 생성 (* registerContextSource)
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param csourceRegistrationVO create csource registration data
     * @throws Exception create error
     */
    @PostMapping("/csourceRegistrations")
    public void registerContextSource(HttpServletRequest request,
                                      HttpServletResponse response,
                                      @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                                      @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                                      @RequestBody CsourceRegistrationVO csourceRegistrationVO) throws Exception {

    	log.info("create ContextSource request. contentType={}, link={}, csourceRegistrationVO={}",
                contentType, link, csourceRegistrationVO);

        try {
            // 1. context 정보 유효성 체크
            validateAndSetContext(csourceRegistrationVO, contentType, link);

            // 2. csourceRegistrations 생성
            Integer result = csourceRegistrationSVC.createCsourceRegistration(csourceRegistrationVO);

            // 3. 요청 결과 확인
            if (result != null) {
                response.setStatus(HttpStatus.CREATED.value());
                response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(Constants.CHARSET_ENCODING);
                
                CsourceRegistrationVO responseVO = new CsourceRegistrationVO();
                responseVO.setId(csourceRegistrationVO.getId());
                responseVO.setType(null);
                response.getWriter().print(objectMapper.writeValueAsString(responseVO));

                // 4. 구독여부 확인 및 Notification 발송
                // TODO: 로직 서비스만들어서 이동 및 상세 조건 구현
                csourceNotification(TriggerReason.NEWLY_MATCHING, csourceRegistrationVO);

            } else {
                //  생성간 에러가 있는 경우, 예외 발생
                log.error("HTTP Binding ERROR");
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.ALREADY_EXISTS, "Already Exists. csourceRegistrationID=" + csourceRegistrationVO.getId(), e);
        } catch (NgsiLdNoExistTypeException e) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include valid type", e);
        }
    }



    /**
     * context source 수정 (* updateContextSource)
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param csourceRegistrationVO update csource registration data
     * @param registrationId csource registration id
     * @throws Exception update error
     */
    @PatchMapping("/csourceRegistrations/{registrationId}")
    public void updateContextSource(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                                    @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                                    @RequestBody CsourceRegistrationVO csourceRegistrationVO,
                                    @PathVariable String registrationId) throws Exception {

        log.info("update ContextSource request. contentType={}, link={}, csourceRegistrationVO={}, registrationId={}",
                contentType, link, csourceRegistrationVO, registrationId);
        
        try {
            // 1. csourceRegistrations 존재 여부 체크
            csourceRegistrationSVC.retrieveCsourceRegistration(registrationId);

            // 2. context 정보 유효성 체크
            validateAndSetContext(csourceRegistrationVO, contentType, link);

            // 3. csourceRegistrations 정보 업데이트
            csourceRegistrationVO.setId(registrationId);
            csourceRegistrationSVC.updateCsourceRegistration(csourceRegistrationVO);

            // 4. 구독여부 확인 및 Notification 발송
            // TODO: 로직 서비스만들어서 이동 및 상세 조건 구현
            csourceNotification(TriggerReason.UPDATED, csourceRegistrationVO);

        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.ALREADY_EXISTS, "Already Exists. csourceRegistrationID=" + csourceRegistrationVO.getId(), e);
        } catch (NgsiLdNoExistTypeException e) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include valid type", e);
        }
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }


    /**
     * context source 삭제 (* delete ContextSource)
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param registrationId csource registration id
     * @throws Exception delete error
     */
    @DeleteMapping("/csourceRegistrations/{registrationId}")
    public void deleteContextSource(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @PathVariable String registrationId) throws Exception {

        log.info("delete ContextSource request. registrationId={}", registrationId);

        try {
            // 1. csourceRegistrations id가 없는 경우, 에러
            CsourceRegistrationVO csourceRegistrationVO = csourceRegistrationSVC.retrieveCsourceRegistration(registrationId);

            // 2. csourceRegistrations 생성 요청
            csourceRegistrationSVC.deleteCsourceRegistration(registrationId);

            // 3. 구독여부 확인 및 Notification 발송
            // TODO: 로직 서비스만들어서 이동 및 상세 조건 구현
            csourceNotification(TriggerReason.NO_LONGER_MATCHING, csourceRegistrationVO);

        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.ALREADY_EXISTS, "Already Exists. csourceRegistrationID=" + registrationId, e);
        } catch (NgsiLdNoExistTypeException e) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include valid type", e);
        }
    }


    @GetMapping(value = "/csourceRegistrations/{registrationId}")
    public @ResponseBody void retrieveContextSourceRegistration(HttpServletResponse response,
                                                                @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                                                @PathVariable String registrationId) throws Exception {

        log.info("retrieve ContextSource request. accept={}, registrationId={}", accept, registrationId);

        CsourceRegistrationVO csourceRegistrationVO = csourceRegistrationSVC.retrieveCsourceRegistration(registrationId);

        if(!Constants.APPLICATION_LD_JSON_VALUE.equals(accept)) {
            csourceRegistrationVO.setContext(null);
        }

        response.getWriter().print(objectMapper.writeValueAsString(csourceRegistrationVO));
    }

    @GetMapping(value = "/csourceRegistrations")
    public @ResponseBody void queryContextSourceRegistrations(HttpServletRequest request,
                                                              HttpServletResponse response,
                                                              @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                                              @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                                                              @ModelAttribute QueryVO queryVO) throws Exception {

        log.info("query ContextSource request. accept={}, link={}, queryVO={}", accept, link, queryVO);

        // 1. link 추출 및 설정
        queryVO.setLinks(HttpHeadersUtil.extractLinkUris(link));

        // 2. subscription 조회
        Integer totalCount = csourceRegistrationSVC.queryCsourceRegistrationsCount(queryVO);
        List<CsourceRegistrationVO> csourceRegistrationVOs = csourceRegistrationSVC.queryCsourceRegistrations(queryVO);

        if(!Constants.APPLICATION_LD_JSON_VALUE.equals(accept)) {
            if(!ValidateUtil.isEmptyData(csourceRegistrationVOs)) {
                for(CsourceRegistrationVO csourceRegistrationVO : csourceRegistrationVOs) {
                    csourceRegistrationVO.setContext(null);
                }
            }
        }

        // 3. 응답 link 헤더 추가
        HttpHeadersUtil.addPaginationLinkHeader(bigDataStorageType, request, response, accept, queryVO.getLimit(), queryVO.getOffset(), totalCount, defaultLimit);
        response.getWriter().print(objectMapper.writeValueAsString(csourceRegistrationVOs));
    }

    /**
     * ContextResource Notification 발송
     *
     * @param triggerReason
     * @param csourceRegistrationVO
     */
    private void csourceNotification(TriggerReason triggerReason, CsourceRegistrationVO csourceRegistrationVO) {

    	// 1. 구독여부 확인
    	List<SubscriptionVO> subscriptionVOs = null;
    	try {
    		subscriptionVOs = subscriptionSVC.querySubscriptions(Integer.MAX_VALUE, 0, JsonLdType.CSOURCE_REGISTRATION_SUBSCRIPTION);
		} catch (JsonProcessingException e) {
			log.error("csourceNotification querySubscription error", e);
		}
    	
    	if(subscriptionVOs != null && subscriptionVOs.size() > 0) {
    		Date notifiedAt = new Date();
    		for (SubscriptionVO subscriptionVO : subscriptionVOs) {

    			if(subscriptionVO.getNotification() == null
    					|| subscriptionVO.getNotification().getEndpoint() == null
    					|| subscriptionVO.getNotification().getEndpoint().getUri() == null) {
    				continue;
    			}

                try {
                    // 3. Notification 발송
                    CsourceNotificationVO csourceNotificationVO = new CsourceNotificationVO();
                    csourceNotificationVO.setId(UUID.randomUUID().toString().replace("-", ""));
                    csourceNotificationVO.setSubscriptionId(subscriptionVO.getId());
                    csourceNotificationVO.setNotifiedAt(notifiedAt);
                    csourceNotificationVO.setTriggerReason(triggerReason);
                    csourceNotificationVO.setData(Arrays.asList(csourceRegistrationVO));

                    String notificationUri = subscriptionVO.getNotification().getEndpoint().getUri();
                    String sendMessage = objectMapper.writeValueAsString(csourceNotificationVO);

                    log.debug("Send HTTP Csource Notification. subsciprionId={}, data={}", subscriptionVO.getId(), sendMessage);

                    // header
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                    HttpEntity<String> entity = new HttpEntity<>(sendMessage, headers);

                    long startTime = System.currentTimeMillis();
                    ResponseEntity<String> responseEntity = restTemplate.postForEntity(subscriptionVO.getNotification().getEndpoint().getUri(), entity, String.class);
                    long elapsedTime = System.currentTimeMillis() - startTime;

                    if (responseEntity == null) {
                        log.warn("HTTP Csource Notification Response is null. subsciprionId={}, endpointUri={}, elapsedTime={}ms, data={}",
                        		subscriptionVO.getId(), notificationUri, elapsedTime, sendMessage);
                    } else {
                        log.info("HTTP Csource Notification Response code={}, subsciprionId={}, elapsedTime={}ms, data={}",
                                responseEntity.getStatusCodeValue(), subscriptionVO.getId(), elapsedTime, sendMessage);
                    }
                } catch (Exception e) {
                    log.error("HTTP Csource Notification error.", e);
                }
            }
    	}
        
        List<CsourceRegistrationVO.Information> informations = csourceRegistrationVO.getInformation();
        if (informations == null || informations.size() == 0) return;

        for (CsourceRegistrationVO.Information information : informations) {
            if (information.getEntities() == null || information.getEntities().size() == 0) {
                continue;
            }

            List<CsourceRegistrationVO.EntityInfo> entities = information.getEntities();
            if (entities != null && entities.size() > 0) {

                // 2. 구독 중복조회 방지를 위한 subscription id를 key로 가진 Map 생성
                Map<String, SubscriptionBaseDaoVO> subscriptionMap = new HashMap<>();

                for (CsourceRegistrationVO.EntityInfo entityInfo : entities) {
                    SubscriptionRetrieveVO subscriptionRetrieveVO = new SubscriptionRetrieveVO();
                    subscriptionRetrieveVO.setEntityId(entityInfo.getId());
                    subscriptionRetrieveVO.setEntityIdPattern(entityInfo.getIdPattern());
                    subscriptionRetrieveVO.setEntityType(entityInfo.getType());
                    subscriptionRetrieveVO.setType(JsonLdType.CSOURCE_REGISTRATION_SUBSCRIPTION.getCode());
                    subscriptionRetrieveVO.setIsActive(true);
                    List<SubscriptionBaseDaoVO> subscriptionBaseDaoVOs = subscriptionSVC.retrieveSubscriptionByEntityId(subscriptionRetrieveVO);
                    if (subscriptionBaseDaoVOs != null) {
                        for (SubscriptionBaseDaoVO subscriptionBaseDaoVO : subscriptionBaseDaoVOs) {
                            subscriptionMap.put(subscriptionBaseDaoVO.getId(), subscriptionBaseDaoVO);
                        }
                    }
                }
                Date notifiedAt = new Date();
                if (!subscriptionMap.isEmpty()) {
                    
                }
            }
        }
    }

    private void validateAndSetContext(CsourceRegistrationVO csourceRegistrationVO, String contentType, String link) {
        // accept가 application/json 인 경우
        if(Constants.APPLICATION_JSON_VALUE.equals(contentType)) {
            // contentType이 application/json인 경우 @context 입력불가
            if(!ValidateUtil.isEmptyData(csourceRegistrationVO.getContext())) {
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                        "Invalid Request Content. @context parameter cannot be used in contentType=application/json");
            }

            // link header가 존재할 경우 추출하여 context 필드에 값 세팅
            List<String> links = HttpHeadersUtil.extractLinkUris(link);
            csourceRegistrationVO.setContext(links);
        }
    }
}
