package kr.re.keti.sc.dataservicebroker.entities.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.datafederation.service.DataFederationProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdResourceNotFoundException;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityVO;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.csource.CsourceRegistrationManager;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationVO;
import kr.re.keti.sc.dataservicebroker.datafederation.service.DataFederationService;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.entities.service.hbase.HbaseEntitySVC;
import kr.re.keti.sc.dataservicebroker.entities.service.hive.HiveEntitySVC;
import kr.re.keti.sc.dataservicebroker.entities.service.rdb.RdbEntitySVC;
import kr.re.keti.sc.dataservicebroker.entities.vo.EntityRetrieveVO;
import kr.re.keti.sc.dataservicebroker.util.QueryUtil;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

@Service
@Slf4j
public class EntityRetrieveSVC {

	private final RdbEntitySVC rdbEntitySVC;
	private final HiveEntitySVC hiveEntitySVC;
	private final HbaseEntitySVC hbaseEntitySVC;
	private final DataModelManager dataModelManager;
	private final CsourceRegistrationManager csourceRegistrationManager;
	private final RestTemplate restTemplate;
	private final DataFederationProperty federationProperty;

	private final String BROKER_URI_GET_ENTITY = "/entities";
	private final String BROKER_URI_GET_ENTITY_COUNT = "/entitycount";
	private final String BROKER_URI_GET_TEMPORAL_ENTITY = "/temporal/entities";
	private final String BROKER_URI_GET_TEMPORAL_ENTITY_COUNT = "/temporal/entitycount";
	
	@Value("${entity.default.storage:rdb}")
	private BigDataStorageType defaultStorageType;
	@Value("${entity.validation.id-pattern.enabled:true}")
	private Boolean validateIdPatternEnabled;

	public EntityRetrieveSVC(
			RdbEntitySVC rdbDynamicEntitySVC,
			@Nullable HiveEntitySVC hiveDynamicEntitySVC,
			@Nullable HbaseEntitySVC hbaseDynamicEntitySVC,
			DataModelManager dataModelManager,
			CsourceRegistrationManager csourceRegistrationManager,
			RestTemplate restTemplate,
			DataFederationProperty federationProperty
	) {
		this.rdbEntitySVC = rdbDynamicEntitySVC;
		this.hiveEntitySVC = hiveDynamicEntitySVC;
		this.hbaseEntitySVC = hbaseDynamicEntitySVC;
		this.dataModelManager = dataModelManager;
		this.csourceRegistrationManager = csourceRegistrationManager;
		this.restTemplate = restTemplate;
		this.federationProperty = federationProperty;
	}

	public CommonEntityVO getEntityById(QueryVO queryVO, String queryString, String accept, String link) {

		validateEntityId(queryVO);

		// standalone 으로 동작
		if (federationProperty.getStandalone()) {
			return queryEntityByIdStandalone(queryVO, accept);
			
		// data-registry 연계
		} else {

			// 2. 조회 대상이 되는 Csource Registration 검색
			List<CsourceRegistrationVO> csourceRegistrationVOs = queryTargetCsourceRegistration(queryVO);

			// 3. 목록의 서비스브로커로 요청 전송 ( 내 자신이 포함되는 경우 내 자신도 Query )
			CommonEntityVO entity = null;
			if(!ValidateUtil.isEmptyData(csourceRegistrationVOs)) {

				for(CsourceRegistrationVO csourceRegistrationVO : csourceRegistrationVOs) {

					// 3-1. 대상이 내 자신 서비스브로커 경우 Method 직접 호출
					if(federationProperty.getCsource().getId().equals(csourceRegistrationVO.getId())) {

						try {
							entity = queryEntityByIdStandalone(queryVO, accept);
							if(entity != null) {
								return entity;
							}
						} catch (NgsiLdResourceNotFoundException e) { }

					// 3-2. 대상이 원격 서비스브로커인 경우 HTTP 로 요청
					} else {
						List<String> alreadyTraversedCsourceIds = getAlreadyTraversedCSourceIds(csourceRegistrationVOs, csourceRegistrationVO.getId());
						queryString = buildQueryStringIfNeedAlreadyTraverse(queryString, alreadyTraversedCsourceIds);
						
						String requestUri = csourceRegistrationVO.getEndpoint() + BROKER_URI_GET_ENTITY + "/" + queryVO.getId();
						entity = queryToOtherServiceBroker(requestUri, queryString, accept, link, new ParameterizedTypeReference<CommonEntityVO>() {});
						if(entity != null) {
							return entity;
						}
					}
				}
			}
			
			if(entity == null) {
				throw new NgsiLdResourceNotFoundException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID,
						"There is no Entity instance with the requested identifier.");
			}
			return entity;
		}
	}

	public EntityRetrieveVO getEntityStandalone(QueryVO queryVO, String queryString, String accept, String link) {
		return queryEntityStandalone(queryVO, accept);
	}

	public EntityRetrieveVO getEntity(QueryVO queryVO, String queryString, String accept, String link) {

		// standalone 으로 동작
		if (federationProperty.getStandalone()) {
			return queryEntityStandalone(queryVO, accept);

		// data-registry 연계
		} else {

			// 2. 조회 대상이 되는 Csource Registration 검색
			List<CsourceRegistrationVO> csourceRegistrationVOs = queryTargetCsourceRegistration(queryVO);

			// 3. 목록의 서비스브로커로 요청 전송 ( 내 자신이 포함되는 경우 내 자신도 Query )
			List<EntityRetrieveVO> entityRetrieveVOs = new ArrayList<>();
			if(!ValidateUtil.isEmptyData(csourceRegistrationVOs)) {

				for(CsourceRegistrationVO csourceRegistrationVO : csourceRegistrationVOs) {

					// 3-1. 대상이 내 자신 서비스브로커 경우 Method 직접 호출
					if(federationProperty.getCsource().getId().equals(csourceRegistrationVO.getId())) {
						EntityRetrieveVO entityRetrieveVO = queryEntityStandalone(queryVO, accept);
						entityRetrieveVOs.add(entityRetrieveVO);
					// 3-2. 대상이 원격 서비스브로커인 경우 HTTP 로 요청
					} else {
						List<String> alreadyTraversedCsourceIds = getAlreadyTraversedCSourceIds(csourceRegistrationVOs, csourceRegistrationVO.getId());
						queryString = buildQueryStringIfNeedAlreadyTraverse(queryString, alreadyTraversedCsourceIds);
						
						String requestUri = csourceRegistrationVO.getEndpoint() + BROKER_URI_GET_ENTITY;
						List<CommonEntityVO> entities = queryToOtherServiceBroker(requestUri, queryString, accept, link, new ParameterizedTypeReference<List<CommonEntityVO>>() {});
						if(entities != null) {
							EntityRetrieveVO entityRetrieveVO = new EntityRetrieveVO();
							entityRetrieveVO.setEntities(entities);
							entityRetrieveVO.setTotalCount(entities.size());
							entityRetrieveVOs.add(entityRetrieveVO);
						}
					}
				}
			}

			// 4. 결과 merge
			EntityRetrieveVO entityRetrieveVO = new EntityRetrieveVO();
			entityRetrieveVO.setEntities(new ArrayList<>());
			entityRetrieveVO.setTotalCount(0);
			for(EntityRetrieveVO innerEntityRetrieveVO : entityRetrieveVOs) {
				if(innerEntityRetrieveVO.getEntities() != null) {
					entityRetrieveVO.getEntities().addAll(innerEntityRetrieveVO.getEntities());
				}
				if(innerEntityRetrieveVO.getTotalCount() != null) {
					entityRetrieveVO.setTotalCount(entityRetrieveVO.getTotalCount() + innerEntityRetrieveVO.getTotalCount());
				}
			}
			Collections.sort(entityRetrieveVO.getEntities());
			List<CommonEntityVO> entities = extractSubListWithPaging(entityRetrieveVO.getEntities(), queryVO.getLimit(), queryVO.getOffset());
			entityRetrieveVO.setEntities(entities);
			
			return entityRetrieveVO;

		}
	}

	public Integer getEntityCount(QueryVO queryVO, String queryString, String link) {

		// standalone 으로 동작
		if (federationProperty.getStandalone()) {
			return queryEntityCountStandalone(queryVO);
			
		// data-registry 연계
		} else {

			// 2. 조회 대상이 되는 Csource Registration 검색
			List<CsourceRegistrationVO> csourceRegistrationVOs = queryTargetCsourceRegistration(queryVO);

			// 3. 목록의 서비스브로커로 요청 전송 ( 내 자신이 포함되는 경우 내 자신도 Query )
			Integer totalCount = 0;
			if(!ValidateUtil.isEmptyData(csourceRegistrationVOs)) {

				for(CsourceRegistrationVO csourceRegistrationVO : csourceRegistrationVOs) {

					// 3-1. 대상이 내 자신 서비스브로커 경우 Method 직접 호출
					if(federationProperty.getCsource().getId().equals(csourceRegistrationVO.getId())) {
						
						totalCount += queryEntityCountStandalone(queryVO);

					// 3-2. 대상이 원격 서비스브로커인 경우 HTTP 로 요청
					} else {

						List<String> alreadyTraversedCsourceIds = getAlreadyTraversedCSourceIds(csourceRegistrationVOs, csourceRegistrationVO.getId());
						queryString = buildQueryStringIfNeedAlreadyTraverse(queryString, alreadyTraversedCsourceIds);
						
						String requestUri = csourceRegistrationVO.getEndpoint() + BROKER_URI_GET_ENTITY_COUNT;
						totalCount += queryToOtherServiceBroker(requestUri, queryString, null, link, new ParameterizedTypeReference<Integer>() {});
					}
				}
			}
			return totalCount;
		}
	}
	
	private String buildQueryStringIfNeedAlreadyTraverse(String queryString, List<String> alreadyTraversedCsourceIds) {
		
		Map<String, String> queryMap = null;
		if(!ValidateUtil.isEmptyData(queryString)) {
			queryMap = QueryUtil.queryStringToMap(queryString);
			queryMap.remove("alreadyTraversedCSourceIds");
		}

		if(!ValidateUtil.isEmptyData(alreadyTraversedCsourceIds)) {
			if(queryMap == null) {
				queryMap = new HashMap<>();
			}
			queryMap.put("alreadyTraversedCSourceIds", String.join(",", alreadyTraversedCsourceIds));
		}
		
		return QueryUtil.mapToQueryString(queryMap);
	}

	private List<String> getAlreadyTraversedCSourceIds(List<CsourceRegistrationVO> csourceRegistrationVOs, String csourceRegistration) {
		List<String> alreadyTraversedCSourceIds = new ArrayList<>();
		for(CsourceRegistrationVO csourceRegistrationVO : csourceRegistrationVOs) {
			if(!csourceRegistration.equals(csourceRegistrationVO.getId())) {
				alreadyTraversedCSourceIds.add(csourceRegistrationVO.getId());
			}
		}
		return alreadyTraversedCSourceIds;
	}

	private EntityRetrieveVO queryEntityStandalone(QueryVO queryVO, String accept) {
		// 1. 검색용 entity id 리스트 설정
		if (queryVO.getId() != null) {
			String id = queryVO.getId();
			String[] idList = id.split(",");
			queryVO.setSearchIdList(Arrays.asList(idList));
		}
		validateEntityId(queryVO);

		// 2. 조회할 storageType 설정
		BigDataStorageType dataStorageType = BigDataStorageType.parseType(queryVO.getDataStorageType());
		if (dataStorageType == null) {
			dataStorageType = defaultStorageType;
		}

		// 3. 리소스 조회
		List<CommonEntityVO> entities = null;
		Integer totalCount = null;
		if (BigDataStorageType.RDB == dataStorageType) {
			totalCount = rdbEntitySVC.selectCount(queryVO);
			entities = rdbEntitySVC.selectAll(queryVO, accept);

		} else if (BigDataStorageType.HIVE == dataStorageType) {

			entities = hiveEntitySVC.selectAll(queryVO, accept);

		} else if (BigDataStorageType.HBASE == dataStorageType) {
			// hiveEntitySVC랑 거의 같음
			entities = hbaseEntitySVC.selectAll(queryVO, accept);
		} else {
			// default
			totalCount = rdbEntitySVC.selectCount(queryVO);
			entities = rdbEntitySVC.selectAll(queryVO, accept);
		}

		EntityRetrieveVO entityRetrieveVO = new EntityRetrieveVO();
		entityRetrieveVO.setTotalCount(totalCount);
		entityRetrieveVO.setEntities(entities);
		return entityRetrieveVO;
	}

	public CommonEntityVO queryEntityByIdStandalone(QueryVO queryVO, String accept) {

		// 1. 조회할 storageType 설정
		BigDataStorageType dataStorageType = BigDataStorageType.parseType(queryVO.getDataStorageType());
		if (dataStorageType == null) {
			dataStorageType = defaultStorageType;
		}

		// 2. 리소스 조회
		CommonEntityVO entity = null;
		if (BigDataStorageType.RDB == dataStorageType) {
			entity = rdbEntitySVC.selectById(queryVO, accept, false);
		} else if (BigDataStorageType.HIVE == dataStorageType) {
			entity = hiveEntitySVC.selectById(queryVO, accept, false);
		} else if (BigDataStorageType.HBASE == dataStorageType) {
			entity = hbaseEntitySVC.selectById(queryVO, accept, false);
		} else {
			entity = rdbEntitySVC.selectById(queryVO, accept, false);
		}

		// 3. 조회된 resource가 없을 경우, ResourceNotFound 처리
		if (entity == null) {
			throw new NgsiLdResourceNotFoundException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID,
					"There is no Entity instance with the requested identifier.");
		}

		return entity;
	}
	
	public Integer queryEntityCountStandalone(QueryVO queryVO) {
		// 1. 검색용 entity id 리스트 설정
		if (queryVO.getId() != null) {
			String id = queryVO.getId();
			String[] idList = id.split(",");
			queryVO.setSearchIdList(Arrays.asList(idList));
		}
		validateEntityId(queryVO);

		// 2. 조회할 storageType 설정
		BigDataStorageType dataStorageType = BigDataStorageType.parseType(queryVO.getDataStorageType());
		if (dataStorageType == null) {
			dataStorageType = defaultStorageType;
		}

		// 3. 리소스 조회
		int totalCount = 0;
		if (BigDataStorageType.RDB == dataStorageType) {
			totalCount = rdbEntitySVC.selectCount(queryVO);

		} else if (BigDataStorageType.HIVE == dataStorageType) {
			totalCount = hiveEntitySVC.selectCount(queryVO);

		} else if (BigDataStorageType.HBASE == dataStorageType) {
			totalCount = hiveEntitySVC.selectCount(queryVO);
			
		} else {
			totalCount = rdbEntitySVC.selectCount(queryVO);
		}

		return totalCount;
	}

	public Integer queryTemporalEntityCountStandalone(QueryVO queryVO) {
		// 1. 검색용 entity id 리스트 확인
		if (queryVO.getId() != null) {
			String id = queryVO.getId();
			String[] idList = id.split(",");
			queryVO.setSearchIdList(Arrays.asList(idList));
		}
		validateEntityId(queryVO);

		// 2. 조회할 storageType 설정
		BigDataStorageType dataStorageType = BigDataStorageType.parseType(queryVO.getDataStorageType());
		if (dataStorageType == null) {
			dataStorageType = defaultStorageType;
		}

		// 3. 리소스 조회
		int totalCount = 0;
		if (BigDataStorageType.RDB == dataStorageType) {
			totalCount = rdbEntitySVC.selectTemporalCount(queryVO);

		} else if (BigDataStorageType.HIVE == dataStorageType) {
			totalCount = hiveEntitySVC.selectTemporalCount(queryVO);

		} else if (BigDataStorageType.HBASE == dataStorageType) {
			totalCount = hbaseEntitySVC.selectTemporalCount(queryVO);
		} else {
			totalCount = rdbEntitySVC.selectTemporalCount(queryVO);
		}

		return totalCount;
	}

	private void validateEntityId(QueryVO queryVO) {
		if(validateIdPatternEnabled) {
			if(queryVO.getId() != null) {
				if (!ValidateUtil.isValidUrn(queryVO.getId())) {
					throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
							"Invalid request parameter. entityId is not in URN format. id=" + queryVO.getId());
				}
			}

			if(queryVO.getSearchIdList() != null) {
				for (String entityId : queryVO.getSearchIdList()) {
					if (!ValidateUtil.isValidUrn(entityId)) {
						throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
								"Invalid request parameter. entityId is not in URN format. id=" + entityId);
					}
				}
			}
		}
	}

	public List<CommonEntityVO> getTemporalEntity(QueryVO queryVO, String queryString, String accept, String link) {

		// standalone 으로 동작
		if (federationProperty.getStandalone()) {
			return queryTemporalEntityStandalone(queryVO, accept);
			
		// data-registry 연계
		} else {

			// 2. 조회 대상이 되는 Csource Registration 검색
			List<CsourceRegistrationVO> csourceRegistrationVOs = queryTargetCsourceRegistration(queryVO);

			// 3. 목록의 서비스브로커로 요청 전송 ( 내 자신이 포함되는 경우 내 자신도 Query )
			List<CommonEntityVO> totalEntities = new ArrayList<>();
			if(!ValidateUtil.isEmptyData(csourceRegistrationVOs)) {

				for(CsourceRegistrationVO csourceRegistrationVO : csourceRegistrationVOs) {

					// 3-1. 대상이 내 자신 서비스브로커 경우 Method 직접 호출
					if(federationProperty.getCsource().getId().equals(csourceRegistrationVO.getId())) {
						List<CommonEntityVO> entities = queryTemporalEntityStandalone(queryVO, accept);
						if(entities != null) {
							totalEntities.addAll(entities);
						}
					// 3-2. 대상이 원격 서비스브로커인 경우 HTTP 로 요청
					} else {
						List<String> alreadyTraversedCsourceIds = getAlreadyTraversedCSourceIds(csourceRegistrationVOs, csourceRegistrationVO.getId());
						queryString = buildQueryStringIfNeedAlreadyTraverse(queryString, alreadyTraversedCsourceIds);
						
						String requestUri = csourceRegistrationVO.getEndpoint() + BROKER_URI_GET_TEMPORAL_ENTITY;
						List<CommonEntityVO> entities = queryToOtherServiceBroker(requestUri, queryString, accept, link, new ParameterizedTypeReference<List<CommonEntityVO>>() {});
						if(entities != null) {
							totalEntities.addAll(entities);
						}
					}
				}

				// 4. 결과 merge
				Collections.sort(totalEntities);
			}
			return extractSubListWithPaging(totalEntities, queryVO.getLimit(), queryVO.getOffset());
		}
	}
	
	public CommonEntityVO getTemporalEntityById(QueryVO queryVO, String queryString, String accept, String link) {

		// standalone 으로 동작
		if (federationProperty.getStandalone()) {
			return queryTemporalEntityByIdStandalone(queryVO, accept);
			
		// data-registry 연계
		} else {

			// 2. 조회 대상이 되는 Csource Registration 검색
			List<CsourceRegistrationVO> csourceRegistrationVOs = queryTargetCsourceRegistration(queryVO);

			// 3. 목록의 서비스브로커로 요청 전송 ( 내 자신이 포함되는 경우 내 자신도 Query )
			if(!ValidateUtil.isEmptyData(csourceRegistrationVOs)) {

				for(CsourceRegistrationVO csourceRegistrationVO : csourceRegistrationVOs) {

					// 3-1. 대상이 내 자신 서비스브로커 경우 Method 직접 호출
					if(federationProperty.getCsource().getId().equals(csourceRegistrationVO.getId())) {
						
						try {
							CommonEntityVO entity = queryTemporalEntityByIdStandalone(queryVO, accept);
							if(entity != null) {
								return entity;
							}
						} catch (NgsiLdResourceNotFoundException e) { }
					// 3-2. 대상이 원격 서비스브로커인 경우 HTTP 로 요청
					} else {
						List<String> alreadyTraversedCsourceIds = getAlreadyTraversedCSourceIds(csourceRegistrationVOs, csourceRegistrationVO.getId());
						queryString = buildQueryStringIfNeedAlreadyTraverse(queryString, alreadyTraversedCsourceIds);
						
						String requestUri = csourceRegistrationVO.getEndpoint() + BROKER_URI_GET_TEMPORAL_ENTITY + "/" + queryVO.getId();
						CommonEntityVO entity = queryToOtherServiceBroker(requestUri, queryString, accept, link, new ParameterizedTypeReference<CommonEntityVO>() {});
						if(entity != null) {
							return entity;
						}
					}
				}
			}
			return null;
		}
	}
	
	public List<CommonEntityVO> queryTemporalEntityStandalone(QueryVO queryVO, String accept) {
		// 검색용 entity id 리스트 확인
		if (queryVO.getId() != null) {

			String id = queryVO.getId();
			String[] idList = id.split(",");
			queryVO.setSearchIdList(Arrays.asList(idList));
		}
		validateEntityId(queryVO);

		// 3. 조회할 storageType 설정
		BigDataStorageType dataStorageType = BigDataStorageType.parseType(queryVO.getDataStorageType());
		if (dataStorageType == null) {
			dataStorageType = defaultStorageType;
		}

		// 4. 리소스 조회
		List<CommonEntityVO> resultList = null;
		if (BigDataStorageType.RDB == dataStorageType) {
			resultList = rdbEntitySVC.selectTemporal(queryVO, accept);

		} else if (BigDataStorageType.HIVE == dataStorageType) {
			resultList = hiveEntitySVC.selectTemporal(queryVO, accept);

		} else if (BigDataStorageType.HBASE == dataStorageType) {
			resultList = hbaseEntitySVC.selectTemporal(queryVO, accept);
		} else {
			resultList = rdbEntitySVC.selectTemporal(queryVO, accept);
		}
		return resultList;
	}

	public CommonEntityVO queryTemporalEntityByIdStandalone(QueryVO queryVO, String accept) {

		validateEntityId(queryVO);

		// 1. 조회할 storageType 설정
		BigDataStorageType dataStorageType = BigDataStorageType.parseType(queryVO.getDataStorageType());
		if (dataStorageType == null) {
			dataStorageType = defaultStorageType;
		}

		// 2. 리소스 조회
		CommonEntityVO resultList = null;
		if (BigDataStorageType.RDB == dataStorageType) {
			resultList = rdbEntitySVC.selectTemporalById(queryVO, accept);

		} else if (BigDataStorageType.HIVE == dataStorageType) {
			resultList = hiveEntitySVC.selectTemporalById(queryVO, accept);
		} else if (BigDataStorageType.HBASE == dataStorageType) {
			resultList = hbaseEntitySVC.selectTemporalById(queryVO, accept);
		} else {
			resultList = rdbEntitySVC.selectTemporalById(queryVO, accept);
		}

		// 3. 조회된 resource가 없을 경우 처리
		if (resultList == null) {
			resultList = new CommonEntityVO();
		}
		return resultList;
	}

	public Integer getTemporalEntityCount(QueryVO queryVO, String queryString, String link) {

		// standalone 으로 동작
		if (federationProperty.getStandalone()) {
			return queryTemporalEntityCountStandalone(queryVO);
			
		// data-registry 연계
		} else {

			// 2. 조회 대상이 되는 Csource Registration 검색
			List<CsourceRegistrationVO> csourceRegistrationVOs = queryTargetCsourceRegistration(queryVO);

			// 3. 목록의 서비스브로커로 요청 전송 ( 내 자신이 포함되는 경우 내 자신도 Query )
			Integer totalCount = 0;
			if(!ValidateUtil.isEmptyData(csourceRegistrationVOs)) {

				for(CsourceRegistrationVO csourceRegistrationVO : csourceRegistrationVOs) {

					// 3-1. 대상이 내 자신 서비스브로커 경우 Method 직접 호출
					if(federationProperty.getCsource().getId().equals(csourceRegistrationVO.getId())) {
						
						totalCount += queryTemporalEntityCountStandalone(queryVO);

					// 3-2. 대상이 원격 서비스브로커인 경우 HTTP 로 요청
					} else {

						List<String> alreadyTraversedCsourceIds = getAlreadyTraversedCSourceIds(csourceRegistrationVOs, csourceRegistrationVO.getId());
						queryString = buildQueryStringIfNeedAlreadyTraverse(queryString, alreadyTraversedCsourceIds);
						
						String requestUri = csourceRegistrationVO.getEndpoint() + BROKER_URI_GET_TEMPORAL_ENTITY_COUNT;
						totalCount += queryToOtherServiceBroker(requestUri, queryString, null, link, new ParameterizedTypeReference<Integer>() {});
					}
				}
			}

			return totalCount;
		}
	}
	

	private List<CsourceRegistrationVO> queryTargetCsourceRegistration(QueryVO queryVO) {

		log.info("QueryTargetCsourceRegistration. type={}, q-query={}, cSourceRegistrationIds={}, alreadyTraversedCSourceIds={}"
				, queryVO.getType(), queryVO.getQ(), queryVO.getCSourceRegistrationIds(), queryVO.getAlreadyTraversedCSourceIds());

		// 1. 전체 csourceRegistration 캐시 조회
		List<CsourceRegistrationVO> targetCsourceRegistrationVO = csourceRegistrationManager.getCsourceRegistrationAllCache();

		// 2. query에 csourceRegistrationIds 가 포함된 경우 (해당 serviceBroker 로만 요청을 전달하기 위해 나머지 Csource 를 필터링 함)
		if (!ValidateUtil.isEmptyData(queryVO.getCSourceRegistrationIds())) {
			targetCsourceRegistrationVO = csourceRegistrationManager.getCsourceRegistrationCacheByIds(queryVO.getCSourceRegistrationIds());
		}
		
		// 3. query에 alreadyTraversedCSourceIds가 포함된 경우 필터링 (serviceBroker 간 순환 호출을 방지하기 위한 파라미터)
		if (!ValidateUtil.isEmptyData(queryVO.getAlreadyTraversedCSourceIds())) {
			if(!ValidateUtil.isEmptyData(targetCsourceRegistrationVO)) {
				List<CsourceRegistrationVO> filteredCsourceRegistrationVO = new ArrayList<>();
				for(CsourceRegistrationVO csourceRegistrationVO : targetCsourceRegistrationVO) {
					if(!queryVO.getAlreadyTraversedCSourceIds().contains(csourceRegistrationVO.getId())) {
						filteredCsourceRegistrationVO.add(csourceRegistrationVO);
					}
				}
				targetCsourceRegistrationVO = filteredCsourceRegistrationVO;
			}
		}

		// 4. EntityInfo (entityId, type, entityIdPattern) 기반 조회
		if (includeCsourceEntityInfo(queryVO)) {
			List<CsourceRegistrationVO> csourceRegistrationVOs = getCsourceRegistrationCacheContainType(targetCsourceRegistrationVO, queryVO);
			log.info("QueryTargetCsourceRegistration return csourceRegistrationVOs={}", csourceRegistrationVOs);
			return csourceRegistrationVOs;
		}

		// 5. q-query 기반 조회
		List<String> qQueryPropertyNames = QueryUtil.extractQueryFieldNames(queryVO);
		if(qQueryPropertyNames != null) {
			List<CsourceRegistrationVO> csourceRegistrationVOs =  getCsourceRegistrationCacheContainProperty(targetCsourceRegistrationVO, queryVO, qQueryPropertyNames);
			log.info("QueryTargetCsourceRegistration return csourceRegistrationVOs={}", csourceRegistrationVOs);
			return csourceRegistrationVOs;
		}

		// 6. 모든 Csource에 매칭되지 않는 경우 전체 Csource 를 반환하여 전체 ServiceBroker 를 조회하도록 함
		log.info("QueryTargetCsourceRegistration return csourceRegistrationVOs={}", targetCsourceRegistrationVO);
		return targetCsourceRegistrationVO;
	}

	private boolean includeCsourceEntityInfo(QueryVO queryVO) {
		if (!ValidateUtil.isEmptyData(queryVO.getType())
			|| !ValidateUtil.isEmptyData(queryVO.getId())
			|| !ValidateUtil.isEmptyData(queryVO.getIdPattern())) {
			return true;
		}
		return false;
	}

	public List<CsourceRegistrationVO> getCsourceRegistrationCacheContainType(
			List<CsourceRegistrationVO> csourceRegistrationCache,
			QueryVO queryVO
	) {

		List<CsourceRegistrationVO> result = new ArrayList<>();

		List<String> queryEntityIds = null;
		List<String> queryEntityTypes = null;

		if(!ValidateUtil.isEmptyData(queryVO.getId())) {
			queryEntityIds = Arrays.asList(queryVO.getId().split(","));
		}

		if(!ValidateUtil.isEmptyData(queryVO.getType())) {
			queryEntityTypes = Arrays.asList(queryVO.getType().split(","));

			// context 정보 조회
			Map<String, String> contextMap = dataModelManager.contextToFlatMap(queryVO.getLinks());

			if(!ValidateUtil.isEmptyData(queryEntityTypes) && contextMap != null) {
				for(int i=0; i<queryEntityTypes.size(); i++) {
					String type = queryEntityTypes.get(i);
					// type이 uri 형태가 아닌 경우 uri 형태로 변환
					if(!type.startsWith("http")) {
						type = contextMap.get(type);
						if(type == null) {
							throw new NgsiLdBadRequestException(ErrorCode.NOT_EXIST_ENTITY, "Not Exist EntityTypes . Link=" + String.join(",", queryVO.getLinks()));
						}
						queryEntityTypes.set(i, type);
					}
				}
			}
		}

		if(!ValidateUtil.isEmptyData(queryEntityIds) || !ValidateUtil.isEmptyData(queryEntityTypes)) {
			for (CsourceRegistrationVO csourceRegistrationVO : csourceRegistrationCache) {
				List<CsourceRegistrationVO.Information> informations = csourceRegistrationVO.getInformation();
				if (informations != null) {
					for (CsourceRegistrationVO.Information information : informations) {
						List<CsourceRegistrationVO.EntityInfo> entities = information.getEntities();
						if (entities != null) {
							for (CsourceRegistrationVO.EntityInfo entityInfo : entities) {

								if(!ValidateUtil.isEmptyData(queryEntityTypes)
										&& queryEntityTypes.contains(entityInfo.getType())) {
									result.add(csourceRegistrationVO);
									break;
								}

								if(!ValidateUtil.isEmptyData(queryEntityIds)) {
									if(!ValidateUtil.isEmptyData(entityInfo.getIdPattern())) {
										Pattern p = Pattern.compile(entityInfo.getIdPattern());
										for(String entityId : queryEntityIds) {
											Matcher m = p.matcher(entityId);
											if(m.matches()) {
												result.add(csourceRegistrationVO);
												break;
											}
										}
									} else {
										if(queryEntityIds.contains(entityInfo.getId())) {
											result.add(csourceRegistrationVO);
											break;
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return result.stream().distinct().collect(Collectors.toList());
	}
	
	public List<CsourceRegistrationVO> getCsourceRegistrationCacheContainProperty(
			List<CsourceRegistrationVO> csourceRegistrationCache,
			QueryVO queryVO,
			List<String> qQueryPropertyNames
	) {

		if (ValidateUtil.isEmptyData(qQueryPropertyNames)) {
			return null;
		}

		// context 정보 조회
		Map<String, String> contextMap = dataModelManager.contextToFlatMap(queryVO.getLinks());

		List<String> qQueryPropertyFullUris = new ArrayList<>();
		for(String qQueryPropertyName : qQueryPropertyNames) {
			if(qQueryPropertyName.startsWith("http")) {
				qQueryPropertyFullUris.add(qQueryPropertyName);

			} else {
				if(contextMap == null) {
					throw new NgsiLdBadRequestException(ErrorCode.NOT_EXIST_ENTITY, "Not Exist q-query parameters in Context. Link=" + (queryVO.getLinks() == null ? "null" : String.join(",", queryVO.getLinks())));
				}
				String propertyFullUri = contextMap.get(qQueryPropertyName);
				if(propertyFullUri == null) {
					throw new NgsiLdBadRequestException(ErrorCode.NOT_EXIST_ENTITY, "Not Exist q-query parameters in Context. Link=" + (queryVO.getLinks() == null ? "null" : String.join(",", queryVO.getLinks())));
				}
				qQueryPropertyFullUris.add(propertyFullUri); // full uri 로 컨버팅
			}
		}

		List<CsourceRegistrationVO> result = new ArrayList<>();
		for (CsourceRegistrationVO csourceRegistrationVO : csourceRegistrationCache) {
			List<CsourceRegistrationVO.Information> informations = csourceRegistrationVO.getInformation();
			if (informations != null) {
				for (CsourceRegistrationVO.Information information : informations) {
					List<String> propertiesAndRelationships = new ArrayList<>();
					if (!ValidateUtil.isEmptyData(information.getPropertyNames())) {
						propertiesAndRelationships.addAll(information.getPropertyNames());
					}
					if (!ValidateUtil.isEmptyData(information.getRelationshipNames())) {
						propertiesAndRelationships.addAll(information.getRelationshipNames());
					}

					if (!ValidateUtil.isEmptyData(propertiesAndRelationships)) {
						for(String propertyName : qQueryPropertyFullUris) {
							if(propertiesAndRelationships.contains(propertyName)) {
								result.add(csourceRegistrationVO);
								break;
							}
						}
					}
				}
			}
		}
		return result.stream().distinct().collect(Collectors.toList());
	}
	
	private List<CommonEntityVO> extractSubListWithPaging(List<CommonEntityVO> totalCommonEntityVOs, Integer limit, Integer offset) {
        Integer startIndex = 0;
        Integer endIndex = totalCommonEntityVOs.size();

        if (limit != null && offset != null) {
            if (endIndex > (limit + offset)) {
                endIndex = limit + offset;
            }
            if (offset > endIndex) {
                startIndex = endIndex;
            } else {
                startIndex = offset;
            }
            totalCommonEntityVOs = totalCommonEntityVOs.subList(startIndex, endIndex);
        } else if (limit != null && offset == null) {
            if (endIndex > limit) {
                endIndex = limit;
            }
            totalCommonEntityVOs = totalCommonEntityVOs.subList(startIndex, endIndex);
        }

        return totalCommonEntityVOs;
    }
	
	
	public <T> T queryToOtherServiceBroker(String requestUri, String queryString, String accept, String link, ParameterizedTypeReference<T> returnType) {

        StringBuilder requestUriBuilder = new StringBuilder(requestUri);
        if(!ValidateUtil.isEmptyData(queryString)) {
        	requestUriBuilder.append("?").append(queryString);
        }
        
        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        headerMap.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if(accept != null) {
        	headerMap.set(HttpHeaders.ACCEPT, accept);
        }
        if(link != null) {
        	headerMap.set(HttpHeaders.LINK, link);
        }
        
        try {
			return requestExchange(requestUriBuilder.toString(), HttpMethod.GET, null, returnType, Arrays.asList(HttpStatus.OK, HttpStatus.NOT_FOUND), headerMap);
		} catch(Exception e) {
			log.error("queryToOtherServiceBroker error. requstUri={}, queryString={}, accept={}, link={}", requestUri, queryString, accept, link, e);
		}
		return null;
    }
	
	
	public <T1, T2> T2 requestExchange(String requestUri, HttpMethod httpMethod, T1 bodyObj, ParameterizedTypeReference<T2> returnType, List<HttpStatus> suceessStatus, MultiValueMap<String, String> headerMap) throws HttpClientErrorException {

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(requestUri).build();
        
        RequestEntity<T1> requestEntity = null;
        if(bodyObj == null) {
            requestEntity = new RequestEntity<>(headerMap, httpMethod, uriComponents.toUri());
        } else {
            requestEntity = new RequestEntity<>(bodyObj, headerMap, httpMethod, uriComponents.toUri());
        }

        log.info("HTTP API request. requestUri={}, method={}, header={}, body={}", requestUri, httpMethod, headerMap, bodyObj);

        ResponseEntity<T2> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(requestEntity, returnType);

            log.info("HTTP API response. statusCode={}, response={}", responseEntity.getStatusCode(), responseEntity.getBody());

            if(suceessStatus.contains(responseEntity.getStatusCode())) {
                return responseEntity.getBody();
            } else {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "HTTP API request error. "
                        + " responseCode=" + responseEntity.getStatusCode() + ", requestUri=" + requestUri + ", method=" + httpMethod);
            }

        } catch(HttpClientErrorException e) {
            if(!suceessStatus.contains(e.getStatusCode())) {
                throw e;
            }
        }
        return null;
    }

}
