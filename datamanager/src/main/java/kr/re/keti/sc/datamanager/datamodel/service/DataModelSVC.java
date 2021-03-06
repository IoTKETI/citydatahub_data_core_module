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
 * ??????????????? ?????? ????????? ?????????
 *  - ????????? ?????? ????????? ??????/??????/???????????? ??? ????????? Provisioning ?????? ????????? ??????/??????/?????? ???????????? Provisioning ??????
 *  - ????????? ?????? ?????? ?????? ????????? ????????????
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
        // ????????? ?????? ?????? ??????
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
     * ????????? ?????? ?????? Provisioning
     * @param provisioningData Provisioning ?????? body ??????
     * @param provisionEventType Provisioning ????????? ??????
     * @param requestUri ?????? uri
     * @throws ProvisionException Provisioning ??? Exception ??????
     */
    public ProvisionNotiVO provisionDataModel(String provisioningData, ProvisionEventType provisionEventType, String requestUri) throws ProvisionException {

    	// 1. Provisioning ?????? ????????? ??????
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

    	// 3. Kafka Event ??????
    	provisioningSVC.sendKafkaEvent(KafkaProvisioningType.DATA_MODEL, provisionNotiVO);
    	
    	return provisionNotiVO;
	}


    /**
     * ????????? ?????? ?????? Provisioning
     * @param provisionNotiVO Provisioning ?????? VO
     * @param provisionServerType Provisionig ?????? ?????? ??????
     */
	private void provisionDataModel(ProvisionNotiVO provisionNotiVO, ProvisionServerType provisionServerType) {
		
		// 1. Provisioning ?????? ?????? ??????
		List<ProvisionServerBaseVO> provisionServerVOs = provisionServerSVC.getProvisionServerVOByType(provisionServerType);
		
		// 2. Provisioning ??????
		List<ProvisionResultVO> provisionResultVOs = provisioningSVC.provisioning(provisionServerType, 
				provisionServerVOs, provisionNotiVO, ProvisioningSubUri.DATA_MODEL);
		
		log.info("DataModel Provisioning Result. {}", provisionResultVOs);

		// 3. ?????? ??????
		boolean processResult = false;
		
		// Provisionig ?????? ??? ??????????????? ?????? ??????
		if(provisionResultVOs == null || provisionResultVOs.size() == 0) {
			processResult = true;
		} else {
			
			// ????????? ????????? ?????? ??? 1????????? Provisioning ?????? ??? ???????????? ??????
			for(ProvisionResultVO provisionResultVO : provisionResultVOs) {
				if(provisionResultVO.getResult()) {
					processResult = true;
				}
			}
		}

		// ????????? ????????? ?????? ?????? ?????? ??? ?????? ??? Exception throw
		// TODO: ???????????? ???????????? DataCore UI ?????? ??????????????? ?????? ??????
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
				// http??? ????????? ?????? uri ????????? ?????? ????????? ????????? http get ??????
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
					// @type ????
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
