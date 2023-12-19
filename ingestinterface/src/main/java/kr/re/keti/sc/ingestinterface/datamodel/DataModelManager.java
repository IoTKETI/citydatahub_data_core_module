package kr.re.keti.sc.ingestinterface.datamodel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.ingestinterface.acl.rule.service.AclRuleSVC;
import kr.re.keti.sc.ingestinterface.acl.rule.vo.AclRuleVO;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ErrorCode;
import kr.re.keti.sc.ingestinterface.common.exception.BadRequestException;
import kr.re.keti.sc.ingestinterface.datamodel.service.DataModelSVC;
import kr.re.keti.sc.ingestinterface.datamodel.vo.ContextVO;
import kr.re.keti.sc.ingestinterface.datamodel.vo.DataModelBaseVO;
import kr.re.keti.sc.ingestinterface.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.ingestinterface.dataset.service.DatasetSVC;
import kr.re.keti.sc.ingestinterface.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.ingestinterface.jsonldcontext.service.JsonldContextSVC;
import kr.re.keti.sc.ingestinterface.jsonldcontext.vo.JsonldContextBaseVO;
import kr.re.keti.sc.ingestinterface.jsonldcontext.vo.JsonldContextCacheVO;
import kr.re.keti.sc.ingestinterface.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 * In-memory cacing manager class
 *  - dataModel, dataSet, aclRule, JsonldContext Caching
 * </pre>
 *
 */
@Component
@Slf4j
public class DataModelManager {

	@Autowired
	private DataModelSVC dataModelSVC;
	@Autowired
	private DatasetSVC datasetSVC;
	@Autowired
	private AclRuleSVC aclRuleSVC;
	@Autowired
	private JsonldContextSVC jsonldContextSVC;

	/** DataModelBaseVO 메모리 저장 캐쉬 */
	private Map<String, DataModelCacheVO> dataModelCache;
	/** DatasetBaseVO 메모리 저장 캐쉬 */
	private Map<String, DatasetBaseVO> datasetCache;
	/** AclRuleVO 메모리 저장 캐쉬 */
	private Map<String, AclRuleVO> aclRuleCache;
	/** Context 정보 저장 캐쉬 */
	private Map<String, JsonldContextCacheVO> contextCache;
	/** lock Object */
	private final Object lock = new Object();
	@Autowired
    private ObjectMapper objectMapper;
	@Autowired
    private RestTemplate restTemplate;

	@Value("${cache.jsonld-context.max-age-second:3600}")
	private Integer defaultJsonldContextCacheAge;

	public DataModelManager() {
		dataModelCache = new HashMap<String, DataModelCacheVO>();
		datasetCache = new HashMap<String, DatasetBaseVO>();
		aclRuleCache = new HashMap<String, AclRuleVO>();
		contextCache = new HashMap<>();
	}

	@PostConstruct
	public void init() {
		loadAllCache();
	}

	public DataModelCacheVO getDataModelVOCacheById(String dataModelId) {
        if (dataModelId == null) {
            return null;
        }
        return dataModelCache.get(dataModelId);
    }

    public DataModelCacheVO getDataModelVOCacheByType(String dataModelType) {

    	// type이 full url 인 경우
    	if(dataModelType != null && dataModelType.startsWith("http")) {
    		for(DataModelCacheVO dataModelCacheVO : dataModelCache.values()) {
    			if(dataModelCacheVO.getTypeUri().equals(dataModelType)) {
    				return dataModelCacheVO;
    			}
    		}
    		
    	}
        return null;
    }

    public DataModelCacheVO getDataModelVOCacheByContext(List<String> context, String dataModelType) {

    	// type이 full url 인 경우 캐쉬에서 조회
    	if(dataModelType != null && dataModelType.startsWith("http")) {
    		for(DataModelCacheVO dataModelCacheVO : dataModelCache.values()) {
    			if(dataModelCacheVO.getTypeUri().equals(dataModelType)) {
    				return dataModelCacheVO;
    			}
    		}
    	}

		// type이 short name 인 경우 context 정보와 short name 조합으로 조회
    	// cache 먼저 조회하고 없을 경우 http 연결하여 획득 후 cache에 적재
    	Map<String, String> contextMap = contextToFlatMap(context);
    	if(contextMap != null) {
    		String dataModelTypeFullUri = contextMap.get(dataModelType);
        	for(DataModelCacheVO dataModelCacheVO : dataModelCache.values()) {
    			if(dataModelCacheVO.getTypeUri().equals(dataModelTypeFullUri)) {
    				return dataModelCacheVO;
    			}
    		}
    	}
    	
        return null;
    }


	/**
	 * DataModelBaseVO,DatasetBaseVO Cache 로딩
	 */
	private void loadAllCache() {
		synchronized(lock) {
			if(!dataModelCache.isEmpty()) {
				log.info("CLEAR DataModel Cache. size={}", dataModelCache.size());
				log.info("CLEAR Dataset Cache. size={}", datasetCache.size());
				dataModelCache.clear();
				datasetCache.clear();
				aclRuleCache.clear();
			}

			// 1. 데이터모델 캐쉬 로딩
			List<DataModelBaseVO> dataModelBaseVOList = dataModelSVC.getDataModelBaseVOList();
			if(dataModelBaseVOList != null) {
				for(DataModelBaseVO dataModelBaseVO : dataModelBaseVOList) {
					putDataModelCache(dataModelBaseVO);
				}
			}
			
			// 2. 데이터셋 캐쉬 로딩
			List<DatasetBaseVO> datasetBaseVOList = datasetSVC.getDatasetVOList();
			if(datasetBaseVOList != null) {
				for(DatasetBaseVO datasetBaseVO : datasetBaseVOList) {
					putDatasetCache(datasetBaseVO);
				}
			}

			// 4. 접근제어 룰  캐쉬 로딩
			List<AclRuleVO> aclRuleVOList = aclRuleSVC.getAclRuleVOList(null);
			if (aclRuleVOList != null) {
				for (AclRuleVO aclRuleVO : aclRuleVOList) {
					putAclRuleCache(aclRuleVO);
				}
			}
		}
	}

	/**
	 * DataModel Cache 추가
	 * @param dataModelBaseVO 데이터모델 정보
	 */
	public void putDataModelCache(DataModelBaseVO dataModelBaseVO) {
		// 1. 데이터 파싱 시 사용할 DataModel 캐시 정보 생성
		DataModelCacheVO dataModelCacheVO = createDataModelCacheVO(dataModelBaseVO);
		
		// 2. 생성된 CacheVO를 인메모리 Map에 적재
		dataModelCache.put(dataModelBaseVO.getId(), dataModelCacheVO);
		
		log.info("PUT DataModel cache. id={}", dataModelCacheVO.getId());
	}

	public DataModelCacheVO getDataModelCacheByDatasetId(String datasetId) {
		DatasetBaseVO datasetBaseVO = datasetCache.get(datasetId);
		if(datasetBaseVO != null) {
			String dataModelId = datasetBaseVO.getDataModelId();
			if(!ValidateUtil.isEmptyData(dataModelId)) {
				return getDataModelVOCacheById(dataModelId);
			}
		}
		return null;
	}

	public void removeDatasetCache(String datasetId) {
		log.info("REMOVE Dataset cache. datasetId={}", datasetId);
		datasetCache.remove(datasetId);
	}
	
	public void removeDataModelCache(String id) {
        dataModelCache.remove(id);
        log.info("REMOVE DataModel cache. id={}", id);
    }
	
	public void putDatasetCache(DatasetBaseVO datasetBaseVO) {
		if(datasetBaseVO != null) {
			log.info("PUT Dataset cache. datasetId={}", datasetBaseVO.getId());
			datasetCache.put(datasetBaseVO.getId(), datasetBaseVO);
		}
	}
	
	public DatasetBaseVO getDatasetCache(String datasetId) {
		if(ValidateUtil.isEmptyData(datasetId)) {
			return null;
		}
		return datasetCache.get(datasetId);
	}



	public void putAclRuleCache(AclRuleVO aclRuleVO) {
		if (aclRuleVO != null) {
			log.info("PUT Dataset cache. id={}", aclRuleVO.getId());
			aclRuleCache.put(aclRuleVO.getId(), aclRuleVO);
		}
	}

	public AclRuleVO getAclRuleCache(String id) {
		if (ValidateUtil.isEmptyData(id)) {
			return null;
		}
		return aclRuleCache.get(id);
	}


	public void removeAclRuleCache(String id) {
		log.info("REMOVE Acl-Dataset cache. id={}", id);
		aclRuleCache.remove(id);
	}

	public List<AclRuleVO> getAclRuleCaches() {
		return new ArrayList<>(aclRuleCache.values());
	}

	/**
	 * 추후 패킷파싱 시 활용될 Cache VO 정보를 생성하여 반환
	 * @param dataModelBaseVO dataModel 정보 VO (DB저장된 정보)
	 * @return Cache VO
	 */
	private DataModelCacheVO createDataModelCacheVO(DataModelBaseVO dataModelBaseVO) {

		// 1. dataModel 정보 파싱
		DataModelCacheVO dataModelCacheVO = null;
		try {
			dataModelCacheVO = objectMapper.readValue(dataModelBaseVO.getDataModel(), DataModelCacheVO.class);
		} catch (IOException e) {
			log.warn("createDataModelCacheVO error.", e);
			return null;
		}

		return dataModelCacheVO;
	}

	public Map<String, String> contextToFlatMap(List<String> contextUris) {
		if(contextUris == null) {
			return null;
		}

		Map<String, String> contextTotalMap = new HashMap<>();
		for(String contextUri : contextUris) {

			Map<String, String> contextMap = null;

			// 1. cache에서 context 정보 조회
			JsonldContextCacheVO jsonldContextCacheVO = contextCache.get(contextUri);
			if(jsonldContextCacheVO != null
					&& jsonldContextCacheVO.getExpireDatetime() != null // 만료시간(필수)이 없는 경우 HTTP를 통한 조회
					&& new Date().before(jsonldContextCacheVO.getExpireDatetime())) {
				contextMap = jsonldContextCacheVO.getContextFlatMap();
			}

			if(contextMap == null) {
				// 2. Cache에 없거나 만료시간이 지난 경우 DB에서 정보 조회 (만료시간이 지나지 않은 정보만 조회)
				JsonldContextBaseVO jsonldContextBaseVO = jsonldContextSVC.getJsonldContextByUrl(contextUri, new Date());
				if(jsonldContextBaseVO != null) {
					try {
						contextMap = objectMapper.readValue(jsonldContextBaseVO.getRefinedPayload(), new TypeReference<Map<String, String>>() {});
						// 2-1. DB조회 결과 Cache 에 입력
						putJsonldContextCache(contextUri, contextMap, jsonldContextBaseVO.getExpireDatetime());
					} catch(Exception e) {
						log.warn("contextToFlatMap parse warn", e);
					}
				}
			}

			if(contextMap == null) {
				// 3. DB에서 조회되지 않는 경우 HTTP URI 에 접근하여 조회
				JsonldContextBaseVO jsonldContextBaseVO = getContextByUri((String) contextUri);

				try {
					ContextVO contextVO = objectMapper.readValue(jsonldContextBaseVO.getPayload(), ContextVO.class);
					if(contextVO == null) {
						throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER, "Retrieve @context error. body is empty. contextUri=" + contextUri);
					}

					contextMap = contextToFlatMap(contextVO.getContext(), null);

					// 3-1. HTTP 로 조회된 결과 DB 입력
					jsonldContextSVC.upsertJsonldContext(contextUri, jsonldContextBaseVO.getPayload(),
							objectMapper.writeValueAsString(contextMap), jsonldContextBaseVO.getExpireDatetime());
					// 3-2. HTTP 로 조회된 결과 캐시 입력
					putJsonldContextCache(contextUri, contextMap, jsonldContextBaseVO.getExpireDatetime());

				} catch (IOException e) {
					throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER, "Retrieve @context error. body is empty. contextUri=" + contextUri, e);
				}
			}

			if(contextMap.isEmpty()) {
				log.warn("Get context result is empty. contextUri={}", contextUri);
			} else {
				// context에 중복 short name이 있는 경우 contextUri 입력 순서대로 override함
				contextTotalMap.putAll(contextMap);
			}
		}
		// 전체 context 결과 반환
		return contextTotalMap;
	}

    private Map<String, String> contextToFlatMap(Object context, Map<String, String> contextMap) {
    	if(context == null) {
    		return contextMap;
    	}

    	if(contextMap == null) {
    		contextMap = new HashMap<>();
    	}

		if(context instanceof String) {
			if(((String) context).startsWith("http")) {
				JsonldContextBaseVO jsonldContextBaseVO = getContextByUri((String) context);

				ContextVO contextVO = null;
				try {
					contextVO = objectMapper.readValue(jsonldContextBaseVO.getPayload(), ContextVO.class);
					if(contextVO == null) {
						throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER, "Retrieve @context error. body is empty. contextUri=" + (String) context);
					}
				} catch (IOException e) {
					throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER, "Retrieve @context error. body is empty. contextUri=" + (String) context, e);
				}
				contextToFlatMap(contextVO.getContext(), contextMap);
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
					log.debug("@context attribute value is not uri. entryKey={}, entryValue={}, value={}", entryKey, entryValue, value);
				}
			}
		} else if(context instanceof List) {
			for(Object innerContext : ((List)context)) {
				contextToFlatMap(innerContext, contextMap);
			}
		} else {
			throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER, "Retrieve @context error. Unsupported class type. type=" + context.getClass());
		}
		
		return contextMap;
    }

	private JsonldContextBaseVO getContextByUri(String contextUri) {
		MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        headerMap.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headerMap.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
		RequestEntity<Void> requestEntity = new RequestEntity<>(headerMap, HttpMethod.GET, URI.create(contextUri));
		
		ResponseEntity<String> responseEntity = null;
		try {
			responseEntity = restTemplate.exchange(requestEntity, String.class);
		} catch (RestClientException e) {
			throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER, "Retrieve @context error. "
					+ "message=" + e.getMessage() + ", contextUri=" + contextUri);
		}

		if(responseEntity.getStatusCode() == HttpStatus.OK) {
			if(ValidateUtil.isEmptyData(responseEntity.getBody())) {
				throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER, "Retrieve @context error. body is empty. contextUri=" + contextUri);
			}


			JsonldContextBaseVO jsonldContextBaseVO = new JsonldContextBaseVO();
			jsonldContextBaseVO.setUrl(contextUri);
			jsonldContextBaseVO.setPayload(responseEntity.getBody());

			int jsonldContextCacheAge = defaultJsonldContextCacheAge;

			try {
				String cacheControlHeader = responseEntity.getHeaders().getCacheControl();
				if(!ValidateUtil.isEmptyData(cacheControlHeader)
						&& (cacheControlHeader.contains("max-age=")
						|| cacheControlHeader.contains("s-maxage="))) {
					String[] cacheControlHeaderArr = cacheControlHeader.split(",");
					for (String cacheControlHeaderPair : cacheControlHeaderArr) {
						String[] cacheControlHeaderPairArr = cacheControlHeaderPair.split("=");
						// TODO: max-age와 s-maxage 중 우선순위가 무엇인지 체크 필요
						if ("max-age".equals(cacheControlHeaderPairArr[0])) {
							jsonldContextCacheAge = Integer.parseInt(cacheControlHeaderPairArr[1]);
							break;
						} else if ("s-maxage".equals(cacheControlHeaderPairArr[0])) {
							jsonldContextCacheAge = Integer.parseInt(cacheControlHeaderPairArr[1]);
							break;
						}
					}
				}
			} catch(Exception e) {
				log.warn("getContextByUri cache-control parsing error", e);
			}

			jsonldContextBaseVO.setExpireDatetime(new Date(System.currentTimeMillis()+jsonldContextCacheAge*1000));

			return jsonldContextBaseVO;
		} else {
			throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER, "Retrieve @context error. "
					+ "Invalid responseCode=" + responseEntity.getStatusCode() + ". contextUri=" + contextUri);
		}
    }

	private void putJsonldContextCache(String url, Map<String, String> jsonldContextFlatMap, Date expireDate) {
		JsonldContextCacheVO jsonldContextCacheVO = new JsonldContextCacheVO();
		jsonldContextCacheVO.setUrl(url);
		jsonldContextCacheVO.setContextFlatMap(jsonldContextFlatMap);
		jsonldContextCacheVO.setExpireDatetime(expireDate);
		contextCache.put(url, jsonldContextCacheVO);
		log.info("PUT JsonldContext cache. url={}", url);
	}

	@PreDestroy
	public void destroy() {
		dataModelCache.clear();
		datasetCache.clear();
	}
	
}
