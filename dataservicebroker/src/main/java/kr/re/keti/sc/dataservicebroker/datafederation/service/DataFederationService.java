package kr.re.keti.sc.dataservicebroker.datafederation.service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.exception.BaseException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityVO;
import kr.re.keti.sc.dataservicebroker.common.vo.GeoPropertyVO;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.entities.service.EntityRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.entities.vo.EntityRetrieveVO;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeType;
import kr.re.keti.sc.dataservicebroker.csource.CsourceRegistrationManager;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationVO;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationVO.EntityInfo;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationVO.Information;
import kr.re.keti.sc.dataservicebroker.datamodel.dao.DataModelDAO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelBaseVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO.NotificationParams;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO.NotificationParams.Endpoint;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataFederationService {

	private final CsourceRegistrationManager csourceRegistrationManager;
	private final DataModelDAO dataModelDAO;
	private final EntityRetrieveSVC entityRetrieveSVC;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	private final DataFederationProperty dataFederationProperty;

	public DataFederationService(
			CsourceRegistrationManager csourceRegistrationManager,
			DataModelDAO dataModelDAO,
			EntityRetrieveSVC entityRetrieveSVC,
			RestTemplate restTemplate,
			ObjectMapper objectMapper,
			DataFederationProperty dataFederationProperty
	) {
		this.csourceRegistrationManager = csourceRegistrationManager;
		this.dataModelDAO = dataModelDAO;
		this.entityRetrieveSVC = entityRetrieveSVC;
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
		this.dataFederationProperty = dataFederationProperty;
	}

	@PostConstruct
	public void init() {

		// data-federation 연계 여부 체크
		if (enableFederation()) {
			try {

				// data registry 에 csource 구독 정보 생성
				registerCsourceSubscription();

			} catch (Exception e) {
				log.error("DataFederationService initialize error", e);
			}
		}
	}

	public boolean enableFederation() {
		return !dataFederationProperty.getStandalone();
	}
	
	public String getFederationCsourceId() {
		if(dataFederationProperty == null || dataFederationProperty.getCsource() == null) {
			return null;
		}
		return dataFederationProperty.getCsource().getId();
	}

	public void retrieveAndCachingCsource() {

		log.info("Retrieve and caching csource by Data-Registry.");

		// 1. data-registry 정보 조회 및 캐싱
		List<CsourceRegistrationVO> registryCsourceRegistrationVOs = getCsourceRegistrations();
		if (registryCsourceRegistrationVOs != null) {

			log.info("Retrieve and caching csource by Data-Registry. csourceRegistrationVOs={}", registryCsourceRegistrationVOs);

			for (CsourceRegistrationVO csourceRegistrationVO : registryCsourceRegistrationVOs) {
				if(csourceRegistrationManager.getCsourceRegistrationCache(csourceRegistrationVO.getId()) == null) {
					csourceRegistrationManager.putCsourceRegistrationCache(csourceRegistrationVO);
				}
			}
		}
	}
	
	
    /**
     * Csource 전체 조회 요청
     * @return
     */
    public List<CsourceRegistrationVO> getCsourceRegistrations() {

    	String requestUri = dataFederationProperty.getDataRegistry().getBaseUri()
				+ dataFederationProperty.getDataRegistry().getSubUri().getCsource();
    	MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        headerMap.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headerMap.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		RequestEntity<Void> requestEntity = new RequestEntity<>(headerMap, HttpMethod.GET, URI.create(requestUri));
		
		ResponseEntity<List<CsourceRegistrationVO>> response = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<List<CsourceRegistrationVO>>() {});
    	
    	return response.getBody();
    }

    /**
     * Csource 전체 등록 요청
     * @return
     */
	public ResponseEntity<Void> registerCsource() {

		// 1. csource 정보 생성
		CsourceRegistrationVO csourceRegistrationVO = createCsourceRegistrationInfo();
		
		log.info("Regist cSource to Data-Registry. csourceRegistrationVO={}", csourceRegistrationVO);

    	// 2. csource 업데이트 요청 (업데이트 시도 후 미 존재 시 생성 요청)
		String dataRegistryCsourceUri = dataFederationProperty.getDataRegistry().getBaseUri()
				+ dataFederationProperty.getDataRegistry().getSubUri().getCsource();
    	try{
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<CsourceRegistrationVO> entity = new HttpEntity<>(csourceRegistrationVO, headers);
            ResponseEntity<Void> result = restTemplate.exchange(dataRegistryCsourceUri + "/" + csourceRegistrationVO.getId(),
                    HttpMethod.PATCH, entity, Void.class);
            return result;

    	} catch(HttpClientErrorException e) {
    		log.warn("HTTP CsourceRegistration Update Response code={}. Id={}",
					e.getRawStatusCode(), csourceRegistrationVO.getId(), e);
		} catch(Exception e) {
			log.warn("HTTP CsourceRegistration Update error. Id={}",
					csourceRegistrationVO.getId(), e);
		}
    	
    	// 3. 존재하지 않는 context source 인 경우 생성 요청
    	try{
    		return restTemplate.postForEntity(dataRegistryCsourceUri, csourceRegistrationVO, Void.class);
    	} catch(HttpClientErrorException e) {
    		log.warn("HTTP CsourceRegistration Create Response code={}. Id={}",
					e.getRawStatusCode(), csourceRegistrationVO.getId(), e);
		} catch(Exception e) {
			log.warn("HTTP CsourceRegistration Create error. Id={}",
					csourceRegistrationVO.getId(), e);
		}
    	
    	return null;
	}
	
	
    /**
     * Csource 구독 등록 요청
     * @return
     * @throws Exception
     */
    public ResponseEntity<Void> registerCsourceSubscription() {
        
        // 1. 구독 요청 객체 생성 
        SubscriptionVO subscriptionVO = new SubscriptionVO();
        subscriptionVO.setId(dataFederationProperty.getSubscription().getId());
        subscriptionVO.setType(DataServiceBrokerCode.JsonLdType.SUBSCRIPTION.getCode());
        subscriptionVO.setIsActive(true);
        NotificationParams notificationParams = new NotificationParams();
        Endpoint endpoint = new Endpoint();
        endpoint.setUri(dataFederationProperty.getSubscription().getEndpoint());
        endpoint.setAccept(MediaType.APPLICATION_JSON_VALUE);
        notificationParams.setEndpoint(endpoint);
        subscriptionVO.setNotification(notificationParams);
        
        // 2. 구독 생성 요청 (업데이트 시도 후 미 존재 시 생성 요청)
        // 2-1. 구독 업데이트 요청
		String dataRegistrySubscriptionUri = dataFederationProperty.getDataRegistry().getBaseUri()
				+ dataFederationProperty.getDataRegistry().getSubUri().getSubscription();
        try{
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<SubscriptionVO> entity = new HttpEntity<>(subscriptionVO, headers);
            ResponseEntity<Void> result = restTemplate.exchange(dataRegistrySubscriptionUri + "/" + subscriptionVO.getId(),
                    HttpMethod.PATCH, entity, Void.class);
            return result;
        } catch(HttpClientErrorException e) {
            log.warn("HTTP CsourceRegistration Update Response code={}. Id={}",
                    e.getRawStatusCode(), subscriptionVO.getId(), e);
        } catch(Exception e) {
            log.warn("HTTP CsourceRegistration Update error. Id={}",
                    subscriptionVO.getId(), e);
        }
        
        try{
	        // 2-2. 존재하지 않는 구독 요청인 경우 생성 요청
	        return restTemplate.postForEntity(dataRegistrySubscriptionUri, subscriptionVO, Void.class);
	    } catch(HttpClientErrorException e) {
	        log.warn("HTTP CsourceRegistration Update Response code={}. Id={}",
	                e.getRawStatusCode(), subscriptionVO.getId(), e);
	    } catch(Exception e) {
	        log.warn("HTTP CsourceRegistration Update error. Id={}",
	                subscriptionVO.getId(), e);
	    }
        
        return null;
    }

    public CsourceRegistrationVO createCsourceRegistrationInfo() {
		CsourceRegistrationVO csourceRegistrationVO = new CsourceRegistrationVO();
		
		// 1. 전체 DataModel 정보 조회
    	List<DataModelBaseVO> dataModelBaseVOs = dataModelDAO.getDataModelBaseVOList();
    	if(dataModelBaseVOs == null || dataModelBaseVOs.isEmpty()) {
    		return null;
    	}

    	// 2-1. CsourceRegistrationVO 기본 데이터 설정
    	csourceRegistrationVO.setId(dataFederationProperty.getCsource().getId());
    	csourceRegistrationVO.setType(DataServiceBrokerCode.JsonLdType.CSOURCE_REGISTRATION.getCode());
    	csourceRegistrationVO.setEndpoint(dataFederationProperty.getCsource().getEndpoint());
    	
    	// 2-2. 조회된 DataModel 기반 CsourceRegistrationVO(information) 데이터 생성
    	csourceRegistrationVO.setInformation(generateInformation(dataModelBaseVOs));

		// 2-3. location 정보 생성
		csourceRegistrationVO.setLocation(generateLocation());

    	return csourceRegistrationVO;
	}

	private GeoPropertyVO generateLocation() {
		String location = dataFederationProperty.getCsource().getLocation();
       		if(!ValidateUtil.isEmptyData(location)) {
			try {
				return objectMapper.readValue(location, GeoPropertyVO.class);
			} catch (IOException e) {
				log.warn("createCsourceRegistrationInfo error. invalid location. location={}", location, e);
			}
		}
		return null;
	}

	private List<Information> generateInformation(List<DataModelBaseVO> dataModelBaseVOs) {
		List<Information> informations = new ArrayList<Information>();
		for(DataModelBaseVO dataModelBaseVO : dataModelBaseVOs) {
			try {
				DataModelVO dataModel = objectMapper.readValue(dataModelBaseVO.getDataModel(), DataModelVO.class);
				Information information = dataModelToInformation(dataModel);
				informations.add(information);
			} catch (BaseException e) {
				log.warn("generateInformation warn. e={}, model typeUri={}", e.toString(), dataModelBaseVO.getTypeUri());
			} catch (IOException e) {
				log.warn("createCsourceRegistrationInfo error. invalid Model. dataModel={}", dataModelBaseVO.getDataModel(), e);
			} catch (Exception e) {
				log.warn("createCsourceRegistrationInfo error. invalid Model. dataModel={}", dataModelBaseVO.getDataModel(), e);
			}
		}
		return informations;
	}

	private Information dataModelToInformation(DataModelVO dataModel) {
		Information information = new Information();
		List<EntityInfo> entityInfos = new ArrayList<EntityInfo>();
		List<String> properties = new ArrayList<String>();
		List<String> relationships = new ArrayList<String>();

		// yml에 id-pattern 설정이
		//  - 있다면 : entityId를 조회하지 않고 yml에 잇는 id-pattern을 사용
		//  - 없다면 : entityId를 조회하여 사용 (id-pattern 값은 무시)
		String idPattern = getFederationCsourceIdPattern(dataModel.getTypeUri());
		if(!ValidateUtil.isEmptyData(idPattern)) {
			EntityInfo entityInfo = new EntityInfo();
			entityInfo.setIdPattern(idPattern);
			entityInfo.setType(dataModel.getTypeUri());
			entityInfos.add(entityInfo);

		} else {
			QueryVO queryVO = new QueryVO();
			if(!ValidateUtil.isEmptyData(dataModel.getTypeUri())) {
				queryVO.setType(dataModel.getTypeUri());
			} else {
				queryVO.setType(dataModel.getType());
				queryVO.setLinks(dataModel.getContext());
			}

			EntityRetrieveVO entityRetrieveVO = entityRetrieveSVC.getEntityStandalone(queryVO, null, Constants.APPLICATION_JSON_VALUE, null);
			if(!ValidateUtil.isEmptyData(entityRetrieveVO.getEntities())) {
				for(CommonEntityVO entityVO : entityRetrieveVO.getEntities()) {
					EntityInfo entityInfo = new EntityInfo();
					entityInfo.setId(entityVO.getId());
					entityInfo.setType(dataModel.getTypeUri());
					entityInfos.add(entityInfo);
				}
			} else {
				EntityInfo entityInfo = new EntityInfo();
				entityInfo.setType(dataModel.getTypeUri());
				entityInfos.add(entityInfo);
			}
		}

		
		List<Attribute> attributes = dataModel.getAttributes();
		for(Attribute attribute : attributes) {
			if(AttributeType.PROPERTY.equals(attribute.getAttributeType())) {
				properties.add(attribute.getAttributeUri());
			} else if(AttributeType.RELATIONSHIP.equals(attribute.getAttributeType())) {
				relationships.add(attribute.getAttributeUri());
			} else {
				// do nothing.
			}
		}

		information.setEntities(entityInfos);
		information.setPropertyNames(properties);
		information.setRelationshipNames(relationships);
		
		return information;
	}

	private String getFederationCsourceIdPattern(String typeUri) {
		if(ValidateUtil.isEmptyData(typeUri)) {
			return null;
		}

		List<EntityInfo> entityInfos = dataFederationProperty.getCsource().getEntityInfos();
		if(!ValidateUtil.isEmptyData(entityInfos)) {
			for (EntityInfo entityInfo : entityInfos) {
				if(typeUri.equals(entityInfo.getType()) && !ValidateUtil.isEmptyData(entityInfo.getIdPattern())) {
					return entityInfo.getIdPattern();
				}
			}
		}
		return null;
	}
}
