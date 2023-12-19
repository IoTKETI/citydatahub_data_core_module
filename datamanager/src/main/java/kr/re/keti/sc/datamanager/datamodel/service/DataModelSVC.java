package kr.re.keti.sc.datamanager.datamodel.service;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ErrorCode;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionEventType;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionServerType;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisioningSubUri;
import kr.re.keti.sc.datamanager.common.exception.BadRequestException;
import kr.re.keti.sc.datamanager.common.exception.ProvisionException;
import kr.re.keti.sc.datamanager.datamodel.dao.DataModelDAO;
import kr.re.keti.sc.datamanager.datamodel.vo.Attribute;
import kr.re.keti.sc.datamanager.datamodel.vo.ContextVO;
import kr.re.keti.sc.datamanager.datamodel.vo.DataModelBaseVO;
import kr.re.keti.sc.datamanager.dataset.dao.DatasetDAO;
import kr.re.keti.sc.datamanager.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.datamanager.provisioning.service.ProvisioningSVC;
import kr.re.keti.sc.datamanager.provisioning.service.ProvisioningSVC.KafkaProvisioningType;
import kr.re.keti.sc.datamanager.provisioning.vo.ProvisionNotiVO;
import kr.re.keti.sc.datamanager.provisioning.vo.ProvisionResultVO;
import kr.re.keti.sc.datamanager.provisionserver.service.ProvisionServerSVC;
import kr.re.keti.sc.datamanager.provisionserver.vo.ProvisionServerBaseVO;
import kr.re.keti.sc.datamanager.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * 데이터모델 관리 서비스 클래스
 *  - 데이터 모델 정보를 생성/수정/삭제하고 기 등록된 Provisioning 대상 서버로 생성/수정/삭제 이벤트를 Provisioning 한다
 *  - 데이터 모델 정보 조회 기능을 제공한다
 * </pre>
 */
@Service
@Slf4j
public class DataModelSVC {

    @Autowired
    private DataModelDAO dataModelDAO;
    @Autowired
    private DatasetDAO datasetDAO;
    @Autowired
    private ProvisionServerSVC provisionServerSVC;
    @Autowired
    private ProvisioningSVC provisioningSVC;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestTemplate restTemplate;

    public List<DataModelBaseVO> getDataModelBaseVOList(DataModelBaseVO dataModelBaseVO) {
        return dataModelDAO.getDataModelBaseVOList(dataModelBaseVO);
    }
    public Integer getDataModelBaseVOListTotalCount(DataModelBaseVO dataModelBaseVO) {
        return dataModelDAO.getDataModelBaseVOListTotalCount(dataModelBaseVO);
    }

    public DataModelBaseVO getDataModelBaseVOById(String dataModelId) {
        DataModelBaseVO dataModelBaseVO = new DataModelBaseVO();
        dataModelBaseVO.setId(dataModelId);
        return dataModelDAO.getDataModelBaseVOById(dataModelBaseVO);
    }

    public int createDataModelBaseVO(DataModelBaseVO dataModelBaseVO) {
        return dataModelDAO.createDataModelBaseVO(dataModelBaseVO);
    }

    public int fullUpdateDataModelBaseVO(DataModelBaseVO dataModelBaseVO) {
        return dataModelDAO.updateDataModelBase(dataModelBaseVO);
    }

    public int deleteDataModelBaseVO(DataModelBaseVO dataModelBaseVO) {
        // 데이터 모델 정보 삭제
        return dataModelDAO.deleteDataModelBaseVO(dataModelBaseVO);
    }

    public int updateDataModelProvisioning(DataModelBaseVO dataModelBaseVO) {
        return dataModelDAO.updateDataModelProvisioning(dataModelBaseVO);
    }

    public DatasetBaseVO getEnabledDatasetByDataModelBaseVO(DatasetBaseVO datasetBaseVO) {
    	return datasetDAO.getEnabledDatasetByDataModelBaseVO(datasetBaseVO);
    }

    public List<String> getDeduplicatedDataModelNameSpace() {
        List<String> namespaces = dataModelDAO.getDeduplicatedDataModelNamespaces();
        return namespaces;
    }

    public List<String> getDeduplicatedDataModelTypes(DataModelBaseVO dataModelBaseVO) {

        List<String> types = dataModelDAO.getDeduplicatedDataModelType(dataModelBaseVO);
        return types;
    }

    public List<String> getDeduplicatedDataModelVersions(DataModelBaseVO dataModelBaseVO) {
        List<String> versions = dataModelDAO.getDeduplicatedDataModelVersion(dataModelBaseVO);
        return versions;
    }


    public void setDataModelDAO(DataModelDAO dataModelDAO) {
        this.dataModelDAO = dataModelDAO;
    }

    /**
     * 데이터 모델 정보 Provisioning
     * @param provisioningData Provisioning 전송 body 정보
     * @param provisionEventType Provisioning 이벤트 유형
     * @param requestUri 요청 uri
     * @throws ProvisionException Provisioning 중 Exception 발생
     */
    public ProvisionNotiVO provisionDataModel(String provisioningData, ProvisionEventType provisionEventType, String requestUri) throws ProvisionException {

    	// 1. Provisioning 전송 데이터 생성
		ProvisionNotiVO provisionNotiVO = new ProvisionNotiVO();
		provisionNotiVO.setRequestId(UUID.randomUUID().toString());
		provisionNotiVO.setEventTime(new Date());
		provisionNotiVO.setEventType(provisionEventType);
		provisionNotiVO.setTo(requestUri);
		provisionNotiVO.setData(provisioningData);

    	// 2. Provisioning
    	// 2-1. DataServiceBroker
		provisionDataModel(provisionNotiVO, ProvisionServerType.DATA_SERVICE_BROKER);
    	
    	// 2-2. IngestInterface
    	provisionDataModel(provisionNotiVO, ProvisionServerType.INGEST_INTERFACE);

    	// 3. Kafka Event 전송
    	provisioningSVC.sendKafkaEvent(KafkaProvisioningType.DATA_MODEL, provisionNotiVO);
    	
    	return provisionNotiVO;
	}


    /**
     * 데이터 모델 정보 Provisioning
     * @param provisionNotiVO Provisioning 전송 VO
     * @param provisionServerType Provisionig 대상 서버 유형
     */
	private void provisionDataModel(ProvisionNotiVO provisionNotiVO, ProvisionServerType provisionServerType) {
		
		// 1. Provisioning 대상 서버 조회
		List<ProvisionServerBaseVO> provisionServerVOs = provisionServerSVC.getProvisionServerVOByType(provisionServerType);
		
		// 2. Provisioning 처리
		List<ProvisionResultVO> provisionResultVOs = provisioningSVC.provisioning(provisionServerType, 
				provisionServerVOs, provisionNotiVO, ProvisioningSubUri.DATA_MODEL);
		
		log.info("DataModel Provisioning Result. {}", provisionResultVOs);

		// 3. 결과 처리
		boolean processResult = false;
		
		// Provisionig 대상 미 존재하므로 성공 처리
		if(provisionResultVOs == null || provisionResultVOs.size() == 0) {
			processResult = true;
		} else {
			
			// 동일한 타입군 서버 중 1대라도 Provisioning 성공 시 성공으로 판단
			for(ProvisionResultVO provisionResultVO : provisionResultVOs) {
				if(provisionResultVO.getResult()) {
					processResult = true;
				}
			}
		}

		// 동일한 타입군 서버 전체 실패 시 첫번 째 Exception throw
		// TODO: 에러내역 전파에서 DataCore UI 에서 사용자에게 정보 전달
		if(!processResult) {
			for(ProvisionResultVO provisionResultVO : provisionResultVOs) {
				if(!provisionResultVO.getResult()) {
					throw provisionResultVO.getProvisionException();
				}
			}
		}
	}

	 public void setAttributeContextUri(List<Attribute> attributes, Map<String, String> contextMap) {
		for(Attribute attribute : attributes) {
			String attributeContextUri = contextMap.get(attribute.getName());
			
			List<Attribute> childAttributes = attribute.getChildAttributes();
			if(childAttributes != null && childAttributes.size() > 0) {
				setAttributeContextUri(childAttributes, contextMap);
			}

			if(attributeContextUri != null) {
				attribute.setAttributeUri(attributeContextUri);
			}
		}
	}
	
	public Map<String, String> contextToFlatMap(Object context, Map<String, String> contextMap) {
    	if(context == null) {
    		return contextMap;
    	}

    	if(contextMap == null) {
    		contextMap = new HashMap<>();
    	}

		if(context instanceof String) {
			if(((String) context).startsWith("http")) {
				// http로 시작할 경우 uri 정보가 있는 것이기 때문에 http get 조회
				MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
		        headerMap.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		        headerMap.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
				RequestEntity<Void> requestEntity = new RequestEntity<>(headerMap, HttpMethod.GET, URI.create((String)context));
				
				ResponseEntity<String> responseEntity = null;
				try {
					responseEntity = restTemplate.exchange(requestEntity, String.class);
				} catch (RestClientException e) {
					throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Retrieve @context error. "
							+ "message=" + e.getMessage() + ", contextUri=" + (String)context);
				}
				if(responseEntity.getStatusCode() == HttpStatus.OK) {
					if(ValidateUtil.isEmptyData(responseEntity.getBody())) {
						throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Retrieve @context error. body is empty. contextUri=" + context);
					}
					ContextVO contextVO = null;
					try {
						contextVO = objectMapper.readValue(responseEntity.getBody(), ContextVO.class);
						if(contextVO == null) {
							throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Retrieve @context error. body is empty. contextUri=" + context);
						}
					} catch (IOException e) {
						throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Retrieve @context error. body is empty. contextUri=" + context, e);
					}
					contextToFlatMap(contextVO.getContext(), contextMap);
				} else {
					throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Retrieve @context error. "
							+ "Invalid responseCode=" + responseEntity.getStatusCode() + ". contextUri=" + context);
				}
			}
		} else if(context instanceof Map) {

			Map<String, Object> contextInnerMap = (Map)context;
			for(Map.Entry<String, Object> entry : contextInnerMap.entrySet()) {
				String entryKey = entry.getKey();
				Object entryValue = entry.getValue();

				String value = null;
				if(entryValue instanceof String) {
					value = (String)entryValue;
				} else if(entryValue instanceof Map) {
					value = (String)((Map)entryValue).get("@id");
					// @type 은?
				}

				if(value != null && !value.startsWith("http") && value.contains(":")) {
					String[] valueArr = value.split(":", 2);
					String valueReferenceUri = (String)contextInnerMap.get(valueArr[0]);
					if(!ValidateUtil.isEmptyData(valueReferenceUri)) {
						value = valueReferenceUri + valueArr[1];
					}
				}

				if(value != null && value.startsWith("http")) {
					contextMap.put(entryKey, value);
				} else {
					log.debug("DataModel @context attribute value is not uri. entryKey={}, entryValue={}, value={}", entryKey, entryValue, value);
				}
			}
		} else if(context instanceof List) {
			for(Object innerContext : ((List)context)) {
				contextToFlatMap(innerContext, contextMap);
			}
		} else {
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Retrieve @context error. Unsupported class type. type=" + context.getClass());
		}
		
		return contextMap;
    }
	
}
