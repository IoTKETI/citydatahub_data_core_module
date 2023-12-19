package kr.re.keti.sc.dataservicebroker.datamodel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import kr.re.keti.sc.dataservicebroker.acl.rule.service.AclRuleRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.acl.rule.service.AclRuleSVC;
import kr.re.keti.sc.dataservicebroker.acl.rule.vo.AclRuleVO;
import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeValueType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DbColumnType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.HistoryStoreType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.PropertyKey;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdContextNotAvailableException;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.ContextVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelBaseVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelStorageMetadataVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.ObjectMember;
import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetSVC;
import kr.re.keti.sc.dataservicebroker.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.service.DatasetFlowRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.datasetflow.service.DatasetFlowSVC;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.DatasetFlowBaseVO;
import kr.re.keti.sc.dataservicebroker.jsonldcontext.service.JsonldContextSVC;
import kr.re.keti.sc.dataservicebroker.jsonldcontext.vo.JsonldContextBaseVO;
import kr.re.keti.sc.dataservicebroker.jsonldcontext.vo.JsonldContextCacheVO;
import kr.re.keti.sc.dataservicebroker.util.QueryUtil;
import kr.re.keti.sc.dataservicebroker.util.StringUtil;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class DataModelManager {

  private final DataModelRetrieveSVC dataModelRetrieveSVC;
  private final DatasetRetrieveSVC datasetRetrieveSVC;
  private final DatasetFlowRetrieveSVC datasetFlowRetrieveSVC;
  private final AclRuleRetrieveSVC aclRuleRetrieveSVC;
  private final JsonldContextSVC jsonldContextSVC;
  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;

  @Value("${cache.jsonld-context.max-age-second:3600}")
  private Integer defaultJsonldContextCacheAge;

  /** DataModelBaseVO 메모리 저장 캐쉬 */
  private Map<String, DataModelCacheVO> dataModelCache;
  /** DatasetBaseVO 메모리 저장 캐쉬 */
  private Map<String, DatasetBaseVO> datasetCache;
  /** DatasetFlowBaseVO 메모리 저장 캐쉬 */
  private Map<String, DatasetFlowBaseVO> datasetFlowCache;
  /** AclRuleVO 메모리 저장 캐쉬 */
  private Map<String, AclRuleVO> aclRuleCache;
  /** cache load lock object */
  private final Object lock = new Object();

  private Map<String, JsonldContextCacheVO> contextCache;

  public DataModelManager(
    DataModelRetrieveSVC dataModelRetrieveSVC,
    DatasetRetrieveSVC datasetRetrieveSVC,
    DatasetFlowRetrieveSVC datasetFlowRetrieveSVC,
    AclRuleRetrieveSVC aclRuleRetrieveSVC,
    JsonldContextSVC jsonldContextSVC,
    ObjectMapper objectMapper,
    RestTemplate restTemplate
  ) {
    this.dataModelRetrieveSVC = dataModelRetrieveSVC;
    this.datasetRetrieveSVC = datasetRetrieveSVC;
    this.datasetFlowRetrieveSVC = datasetFlowRetrieveSVC;
    this.aclRuleRetrieveSVC = aclRuleRetrieveSVC;
    this.jsonldContextSVC = jsonldContextSVC;
    this.objectMapper = objectMapper;
    this.restTemplate = restTemplate;

    dataModelCache = new ConcurrentHashMap<>();
    datasetCache = new HashMap<>();
    datasetFlowCache = new HashMap<>();
    aclRuleCache = new HashMap<>();
    contextCache = new HashMap<>();
  }

  @PostConstruct
  public void init() {
    loadAllCache();
  }

  public List<DataModelCacheVO> getDataModelVOListCache() {
    if (dataModelCache.isEmpty()) {
      return null;
    }

    return dataModelCache
      .values()
      .stream()
      .collect(Collectors.toCollection(ArrayList::new));
  }

  public DataModelCacheVO getDataModelVOCacheById(String dataModelId) {
    if (dataModelId == null) {
      return null;
    }
    return dataModelCache.get(dataModelId);
  }

  public DataModelCacheVO getDataModelVOCacheByType(String dataModelType) {
    // type이 full url 인 경우
    if (dataModelType != null && dataModelType.startsWith("http")) {
      for (DataModelCacheVO dataModelCacheVO : dataModelCache.values()) {
        if (
          dataModelCacheVO.getDataModelVO().getTypeUri().equals(dataModelType)
        ) {
          return dataModelCacheVO;
        }
      }
    }
    return null;
  }

  public DataModelCacheVO getDataModelVOCacheByContext(
    List<String> context,
    String dataModelType
  ) {
    // type이 full url 인 경우 캐쉬에서 조회
    if (dataModelType != null && dataModelType.startsWith("http")) {
      for (DataModelCacheVO dataModelCacheVO : dataModelCache.values()) {
        if (
          dataModelType.equals(dataModelCacheVO.getDataModelVO().getTypeUri())
        ) {
          return dataModelCacheVO;
        }
      }
    }

    // type이 short name 인 경우 context 정보와 short name 조합으로 조회
    // cache 먼저 조회하고 없을 경우 http 연결하여 획득 후 cache에 적재
    Map<String, String> contextMap = contextToFlatMap(context);
    if (contextMap != null) {
      String dataModelTypeFullUri = contextMap.get(dataModelType);
      if (dataModelTypeFullUri != null) {
        for (DataModelCacheVO dataModelCacheVO : dataModelCache.values()) {
          if (
            dataModelTypeFullUri.equals(
              dataModelCacheVO.getDataModelVO().getTypeUri()
            )
          ) {
            return dataModelCacheVO;
          }
        }
      }
    }

    return null;
  }

  /**
   * entity 검색조건에 해당하는 dataModel 정보 조회
   * @param links http link header (context uri 정보)
   * @param queryVO query entity 정보
   * @return
   */
  public List<DataModelCacheVO> getTargetDataModelByQueryUri(
    List<String> links,
    QueryVO queryVO
  ) {
    List<DataModelCacheVO> resultDataModels = new ArrayList<>();

    // q-query와 geo-qery 대상 모델 조회
    List<DataModelCacheVO> queryTargetDataModels = queryGeoAndQQueryTargetModel(
      links,
      queryVO
    );

    // attrs 검색조건이 있는 경우
    boolean includeAttrsQuery = QueryUtil.includeAttrsQuery(queryVO);
    if (includeAttrsQuery) {
      // 검색조건 attrs 대상 모델 조회
      List<DataModelCacheVO> attrsDataModels = new ArrayList<>();
      List<String> queryAttrNameList = queryVO.getAttrs();
      List<String> fullUriAttrs = convertAttrNameToFullUri(
        links,
        queryAttrNameList
      );

      for (DataModelCacheVO dataModelCacheVO : dataModelCache.values()) {
        // attrs를 보유하고 있는 모든 attribute가 있는 datamodel리스트 추출
        if (
          includeAttrInDataModel(
            dataModelCacheVO.getDataModelVO(),
            fullUriAttrs
          )
        ) {
          attrsDataModels.add(dataModelCacheVO);
        }
      }

      // attrs 검색조건을 충족하지 못하는 모델은 제외
      for (DataModelCacheVO attrsDataModel : attrsDataModels) {
        if (queryTargetDataModels.contains(attrsDataModel)) {
          resultDataModels.add(attrsDataModel);
        }
      }
      // attrs 검색조건이 없는 경우
    } else {
      resultDataModels = queryTargetDataModels;
    }

    return resultDataModels;
  }

  private List<DataModelCacheVO> queryGeoAndQQueryTargetModel(
    List<String> links,
    QueryVO queryVO
  ) {
    List<DataModelCacheVO> resultDataModels = new ArrayList<>();

    boolean includeQQuery = QueryUtil.includeQQuery(queryVO);
    boolean includeGeoQuery = QueryUtil.includeGeoQuery(queryVO);

    // q-query 대상 모델 조회
    List<DataModelCacheVO> qQueryDataModels = new ArrayList<>();
    if (includeQQuery) {
      List<String> queryAttrNameList = QueryUtil.extractQueryFieldNames(
        queryVO
      );
      List<String> fullUriAttrs = convertAttrNameToFullUri(
        links,
        queryAttrNameList
      );

      for (DataModelCacheVO dataModelCacheVO : dataModelCache.values()) {
        // q에 해당하는 모든 attribute가 있는 datamodel리스트 추출
        if (
          includeAttrInDataModel(
            dataModelCacheVO.getDataModelVO(),
            fullUriAttrs
          )
        ) {
          qQueryDataModels.add(dataModelCacheVO);
        }
      }
    }

    // geo-query 대상 모델 조회
    List<DataModelCacheVO> geoQueryDataModels = new ArrayList<>();
    if (includeGeoQuery) {
      String geoPropertyName = queryVO.getGeoproperty() == null
        ? Constants.LOCATION_ATTR_DEFAULT_NAME
        : queryVO.getGeoproperty();
      List<String> fullUriAttrs = convertAttrNameToFullUri(
        links,
        Arrays.asList(geoPropertyName)
      );

      for (DataModelCacheVO dataModelCacheVO : dataModelCache.values()) {
        // geoProperty가 있는 datamodel리스트 추출
        if (
          includeAttrInDataModel(
            dataModelCacheVO.getDataModelVO(),
            fullUriAttrs
          )
        ) {
          geoQueryDataModels.add(dataModelCacheVO);
        }
      }
    }

    // q-query와 geo-query조건 모두 만족해야하는 경우
    if (includeQQuery && includeGeoQuery) {
      // 검색조건에 공통으로 보유한 모델만 반환
      for (DataModelCacheVO qQueryDataModel : qQueryDataModels) {
        if (geoQueryDataModels.contains(qQueryDataModel)) {
          resultDataModels.add(qQueryDataModel);
        }
      }
      // q-query 조건 만족해야하는 경우
    } else if (includeQQuery && !includeGeoQuery) {
      resultDataModels = qQueryDataModels;
      // geo-query 조건 만족해야하는 경우
    } else if (!includeQQuery && includeGeoQuery) {
      resultDataModels = geoQueryDataModels;
      // 전체 모델 조회
    } else {
      resultDataModels.addAll(dataModelCache.values());
    }
    return resultDataModels;
  }

  public void validateGeoAndQQuery (
          List<String> links,
          QueryVO queryVO,
          DataModelCacheVO dataModelCacheVO
  ) {
    boolean includeQQuery = QueryUtil.includeQQuery(queryVO);
    boolean includeGeoQuery = QueryUtil.includeGeoQuery(queryVO);

    // q-query attriute 검증
    if (includeQQuery) {
      List<String> queryAttrNameList = QueryUtil.extractQueryFieldNames(queryVO);
      List<String> fullUriAttrs = convertAttrNameToFullUri(links, queryAttrNameList);

      if (!includeAttrInDataModel(dataModelCacheVO.getDataModelVO(), fullUriAttrs)) {
        throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid attrs. q=" + queryVO.getQ() + ", link=" + links);
      }
    }

    // geo-query attriute 검증
    if (includeGeoQuery) {
      String geoPropertyName = queryVO.getGeoproperty() == null
              ? Constants.LOCATION_ATTR_DEFAULT_NAME
              : queryVO.getGeoproperty();
      List<String> fullUriAttrs = convertAttrNameToFullUri(links, Arrays.asList(geoPropertyName));

      if (!includeAttrInDataModel(dataModelCacheVO.getDataModelVO(), fullUriAttrs)) {
        throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER,
                "Invalid attrs. geoRel=" + queryVO.getGeorel()
                        + "geoMetry=" + queryVO.getGeometry()
                        + "coordinates=" + queryVO.getCoordinates()
                        + ", link=" + links);
      }
    }
  }


  private boolean includeAttrInDataModel(
    DataModelVO dataModelVO,
    List<String> fullUriAttrs
  ) {
    if (dataModelVO == null || ValidateUtil.isEmptyData(fullUriAttrs)) {
      return false;
    }
    return includeAttrInDataModel(dataModelVO.getAttributes(), fullUriAttrs);
  }

  private boolean includeAttrInDataModel(
          List<Attribute> attributes,
          List<String> fullUriAttrs
  ) {
    for (String fullUriAttr : fullUriAttrs) {
      boolean isMatch = false;
      for (Attribute attribute : attributes) {
        if (!ValidateUtil.isEmptyData(attribute.getChildAttributes())) {
          if(includeAttrInDataModel(attribute.getChildAttributes(), new ArrayList<>(Arrays.asList(fullUriAttr)))) {
            isMatch = true;
            break;
          }
        }
        if (fullUriAttr.equals(attribute.getAttributeUri())) {
          isMatch = true;
          break;
        }
      }
      if (!isMatch) {
        return false;
      }
    }
    return true;
  }

  /**
   * 데이터 셋 흐름 캐쉬정보에서 이력저장유형 조회
   *
   * @param datasetId 데이터 셋 아이디
   * @return
   */
  public HistoryStoreType getHistoryStoreType(String datasetId) {
    if (datasetId == null) return null;
    return datasetFlowCache.get(datasetId).getHistoryStoreType();
  }

  /**
   * DataModelBaseVO Cache 로딩
   */
  private void loadAllCache() {
    synchronized (lock) {
      if (!dataModelCache.isEmpty()) {
        log.info("CLEAR DataModel Cache. size={}", dataModelCache.size());
        log.info("CLEAR Dataset Cache. size={}", datasetCache.size());
        log.info("CLEAR DatasetFlow Cache. size={}", datasetFlowCache.size());
        log.info("CLEAR aclRuleCache Cache. size={}", aclRuleCache.size());
        dataModelCache.clear();
        datasetCache.clear();
        datasetFlowCache.clear();
        aclRuleCache.clear();
      }

      // 1. 데이터모델 캐쉬 로딩
      List<DataModelBaseVO> dataModelBaseVOList = dataModelRetrieveSVC.getDataModelBaseVOList();
      if (dataModelBaseVOList != null) {
        for (DataModelBaseVO dataModelBaseVO : dataModelBaseVOList) {
          putDataModelCache(dataModelBaseVO);
        }
      }

      // 2. 데이터셋 캐쉬 로딩
      List<DatasetBaseVO> datasetBaseVOList = datasetRetrieveSVC.getDatasetVOList();
      if (datasetBaseVOList != null) {
        for (DatasetBaseVO datasetBaseVO : datasetBaseVOList) {
          putDatasetCache(datasetBaseVO);
        }
      }

      // 3. 데이터셋 흐름 캐쉬 로딩
      List<DatasetFlowBaseVO> datasetFlowBaseVOList = datasetFlowRetrieveSVC.getDatasetFlowBaseVOList();
      if (datasetFlowBaseVOList != null) {
        for (DatasetFlowBaseVO datasetFlowBaseVO : datasetFlowBaseVOList) {
          putDatasetFlowCache(datasetFlowBaseVO);
        }
      }

      // 4. 접근제어 룰  캐쉬 로딩
      List<AclRuleVO> aclRuleVOList = aclRuleRetrieveSVC.getAclRuleVOList(null);
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
    DataModelCacheVO dataModelCacheVO = new DataModelCacheVO();
    try {
      // 1. dataModel 정보 세팅
      DataModelVO dataModelVO = objectMapper.readValue(
        dataModelBaseVO.getDataModel(),
        DataModelVO.class
      );
      dataModelCacheVO.setDataModelVO(dataModelVO);

      // 2. storageMetadata 세팅
      DataModelStorageMetadataVO storageMetadataVO = null;
      if (dataModelBaseVO.getStorageMetadata() != null) {
        storageMetadataVO =
          objectMapper.readValue(
            dataModelBaseVO.getStorageMetadata(),
            DataModelStorageMetadataVO.class
          );
      } else {
        // 하위 호환을 위해 storageMetadata가 DB에 없는 경우 생성
        storageMetadataVO =
          createDataModelStorageMetadata(
            dataModelVO,
            null,
            dataModelBaseVO.getCreatedStorageTypes()
          );
      }
      dataModelCacheVO.setDataModelStorageMetadataVO(storageMetadataVO);

      // 3. createdStorageTypes 정보 세팅
      dataModelCacheVO.setCreatedStorageTypes(
        dataModelBaseVO.getCreatedStorageTypes()
      );
    } catch (IOException e) {
      throw new BadRequestException(
        ErrorCode.INVALID_DATAMODEL,
        "StorageMetadata parsing error. storageMetadata=" +
        dataModelBaseVO.getStorageMetadata(),
        e
      );
    }

    // 4. 생성된 CacheVO를 인메모리 Map에 적재
    dataModelCache.put(dataModelBaseVO.getId(), dataModelCacheVO);

    log.info("PUT DataModel cache. id={}", dataModelBaseVO.getId());
  }

  public void reloadDataModelCache(String dataModelId) {
    log.info("RELOAD DataModel cache. id={}", dataModelId);

    DataModelBaseVO dataModelBaseVO = dataModelRetrieveSVC.getDataModelBaseVOById(
      dataModelId
    );
    if (dataModelBaseVO == null) {
      return;
    }

    putDataModelCache(dataModelBaseVO);
  }

  public void removeDataModelCache(String id) {
    dataModelCache.remove(id);
    log.info("REMOVE DataModel cache. id={}", id);
  }

  public void putDatasetCache(DatasetBaseVO datasetBaseVO) {
    if (datasetBaseVO != null) {
      log.info("PUT Dataset cache. datasetId={}", datasetBaseVO.getId());
      datasetCache.put(datasetBaseVO.getId(), datasetBaseVO);
    }
  }

  public DatasetBaseVO getDatasetCache(String datasetId) {
    if (ValidateUtil.isEmptyData(datasetId)) {
      return null;
    }
    return datasetCache.get(datasetId);
  }

  public void putDatasetFlowCache(DatasetFlowBaseVO datasetFlowBase) {
    if (datasetFlowBase != null) {
      log.info(
        "PUT DatasetFlow cache. datasetId={}",
        datasetFlowBase.getDatasetId()
      );
      datasetFlowCache.put(datasetFlowBase.getDatasetId(), datasetFlowBase);
    }
  }

  public DatasetFlowBaseVO getDatasetFlowCache(String datasetId) {
    if (ValidateUtil.isEmptyData(datasetId)) {
      return null;
    }
    return datasetFlowCache.get(datasetId);
  }

  public DataModelCacheVO getDataModelCacheByDatasetId(String datasetId) {
    DatasetBaseVO datasetBaseVO = datasetCache.get(datasetId);
    if (datasetBaseVO != null) {
      String dataModelId = datasetBaseVO.getDataModelId();
      if (!ValidateUtil.isEmptyData(dataModelId)) {
        return getDataModelVOCacheById(dataModelId);
      }
    }
    return null;
  }

  public void removeDatasetCache(String datasetId) {
    log.info("REMOVE Dataset cache. datasetId={}", datasetId);
    datasetCache.remove(datasetId);
  }

  public void removeDatasetFlowCache(String datasetId) {
    log.info("REMOVE DatasetFlow cache. datasetId={}", datasetId);
    datasetFlowCache.remove(datasetId);
  }

  public void putAclRuleCache(AclRuleVO aclRuleVO) {
    if (aclRuleVO != null) {
      log.info(
        "PUT Acl rule cache. id={}",
        aclRuleVO.getId() + ", datasetId=" + aclRuleVO.getResourceId()
      );
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
   *
   * @param dataModelVO dataModel 정보 VO
   * @param beforeStorageMetadata
   * @param createdStorageTypes
   * @return
   */
  public DataModelStorageMetadataVO createDataModelStorageMetadata(
    DataModelVO dataModelVO,
    DataModelStorageMetadataVO beforeStorageMetadata,
    List<BigDataStorageType> createdStorageTypes
  ) {
    // 1. dataModel 정보 파싱
    DataModelStorageMetadataVO storageMetadataVO = new DataModelStorageMetadataVO();

    // 2. 테이블명 생성
    storageMetadataVO.setRdbTableName(
      generateRdbTableName(dataModelVO.getId())
    );
    storageMetadataVO.setHiveTableName(
      generateHiveTableName(dataModelVO.getId())
    );

    // 3. 추후 DB입력/조회 시 사용될 DbColumnInfoVO 를 생성
    Map<String, DataModelDbColumnVO> beforeDbColumnVOMap = beforeStorageMetadata ==
      null
      ? null
      : beforeStorageMetadata.getDbColumnInfoVOMap();
    attributesToDbColumnVO(
      storageMetadataVO.getDbColumnInfoVOMap(),
      beforeDbColumnVOMap,
      null,
      dataModelVO.getAttributes()
    );

    return storageMetadataVO;
  }

  /**
   * Attribute 정보에 해당되는 DB컬럼정보 생성
   *
   * @param dbColumnInfoVOMap  DB컬럼정보VO 맵
   * @param parentHierarchyIds attribute 계층 id 리스트
   * @param rootAttributes     attribute 리스트
   */
  private void attributesToDbColumnVO(
    Map<String, DataModelDbColumnVO> dbColumnInfoVOMap,
    Map<String, DataModelDbColumnVO> beforeDbColumnVOMap,
    List<String> parentHierarchyIds,
    List<Attribute> rootAttributes
  ) {
    if (rootAttributes == null) {
      return;
    }

    for (Attribute rootAttribute : rootAttributes) {
      attributesToDbColumnVO(
        dbColumnInfoVOMap,
        beforeDbColumnVOMap,
        parentHierarchyIds,
        rootAttribute
      );
    }
  }

  /**
   * Attribute 정보에 해당되는 DB컬럼정보 생성
   * @param dbColumnVOMap  DB컬럼정보VO 맵
   * @param parentHierarchyIds attribute 계층 id 리스트
   * @param rootAttribute      attribute
   */
  public void attributesToDbColumnVO(
    Map<String, DataModelDbColumnVO> dbColumnVOMap,
    Map<String, DataModelDbColumnVO> beforeDbColumnVOMap,
    List<String> parentHierarchyIds,
    Attribute rootAttribute
  ) {
    if (rootAttribute == null) {
      return;
    }

    List<String> currentHierarchyIds = new ArrayList<>();
    if (parentHierarchyIds != null && parentHierarchyIds.size() > 0) {
      currentHierarchyIds.addAll(parentHierarchyIds);
    }
    currentHierarchyIds.add(rootAttribute.getName());

    // 1. type이 Property인 경우
    if (rootAttribute.getAttributeType() == AttributeType.PROPERTY) {
      // 1-1. value type이 object인 경우
      if (rootAttribute.getValueType() == AttributeValueType.OBJECT) {
        List<ObjectMember> objectMembers = rootAttribute.getObjectMembers();
        if (objectMembers != null) {
          objectTypeToCache(
            currentHierarchyIds,
            objectMembers,
            dbColumnVOMap,
            beforeDbColumnVOMap
          );
        }
        // 1-2. value type이 array object인 경우
      } else if (
        rootAttribute.getValueType() == AttributeValueType.ARRAY_OBJECT
      ) {
        List<ObjectMember> objectMembers = rootAttribute.getObjectMembers();
        if (objectMembers != null) {
          for (ObjectMember objectMember : objectMembers) {
            List<String> hierarchyAttributeIds = new ArrayList<>(
              currentHierarchyIds
            );
            hierarchyAttributeIds.add(objectMember.getName());
            DataModelDbColumnVO dbColumnInfoVO = createDbColumnInfoVO(
              hierarchyAttributeIds,
              objectMember.getValueType(),
              objectMember.getMaxLength(),
              objectMember.getIsRequired(),
              true,
              beforeDbColumnVOMap
            );
            dbColumnVOMap.put(
              dbColumnInfoVO.getColumnName().toLowerCase(),
              dbColumnInfoVO
            );
          }
        }
        // 1-3. value type이 String, Integer, Double, Date, ArrayString, ArrayInteger, ArrayDouble인 경우
      } else {
        DataModelDbColumnVO dbColumnInfoVO = createDbColumnInfoVO(
          currentHierarchyIds,
          rootAttribute.getValueType(),
          rootAttribute.getMaxLength(),
          rootAttribute.getIsRequired(),
          false,
          beforeDbColumnVOMap
        );
        dbColumnVOMap.put(
          dbColumnInfoVO.getColumnName().toLowerCase(),
          dbColumnInfoVO
        );
      }
      // 2. type이 GepProperty인 경우
    } else if (rootAttribute.getAttributeType() == AttributeType.GEO_PROPERTY) {
      List<DataModelDbColumnVO> dbColumnInfoVOList = createGeoDbColumnInfoVO(
        currentHierarchyIds,
        rootAttribute.getIsRequired(),
        dbColumnVOMap
      );
      for (DataModelDbColumnVO dbColumnInfoVO : dbColumnInfoVOList) {
        dbColumnVOMap.put(
          dbColumnInfoVO.getColumnName().toLowerCase(),
          dbColumnInfoVO
        );
      }
      // 3. type이 Relationship인 경우
    } else if (rootAttribute.getAttributeType() == AttributeType.RELATIONSHIP) {
      DataModelDbColumnVO dbColumnInfoVO = createDbColumnInfoVO(
        currentHierarchyIds,
              rootAttribute.getValueType() == null ? AttributeValueType.STRING : rootAttribute.getValueType(),
        rootAttribute.getMaxLength(),
        rootAttribute.getIsRequired(),
        false,
        beforeDbColumnVOMap
      );
      dbColumnVOMap.put(
        dbColumnInfoVO.getColumnName().toLowerCase(),
        dbColumnInfoVO
      );
    }

    // 4. hasObservedAt=true 인 경우
    if (
      rootAttribute.getHasObservedAt() != null &&
      rootAttribute.getHasObservedAt()
    ) {
      List<String> hierarchyAttributeIds = new ArrayList<>(currentHierarchyIds);
      hierarchyAttributeIds.add(PropertyKey.OBSERVED_AT.getCode());
      DataModelDbColumnVO dbColumnInfoVO = createDbColumnInfoVO(
        hierarchyAttributeIds,
        AttributeValueType.DATE,
        null,
        null,
        false,
        beforeDbColumnVOMap
      );
      dbColumnVOMap.put(
        dbColumnInfoVO.getColumnName().toLowerCase(),
        dbColumnInfoVO
      );
    }

    // 5. has property or relationship 이 존재하는 경우
    if (rootAttribute.getChildAttributes() != null) {
      attributesToDbColumnVO(
        dbColumnVOMap,
        beforeDbColumnVOMap,
        currentHierarchyIds,
        rootAttribute.getChildAttributes()
      );
    }

    // 6. hasUnitCode=true 인 경우
    if (
      rootAttribute.getHasUnitCode() != null && rootAttribute.getHasUnitCode()
    ) {
      List<String> hierarchyAttributeIds = new ArrayList<>(currentHierarchyIds);
      hierarchyAttributeIds.add(PropertyKey.UNIT_CODE.getCode());
      DataModelDbColumnVO dbColumnInfoVO = createDbColumnInfoVO(
        hierarchyAttributeIds,
        AttributeValueType.STRING,
        null,
        null,
        false,
        beforeDbColumnVOMap
      );
      dbColumnVOMap.put(
        dbColumnInfoVO.getColumnName().toLowerCase(),
        dbColumnInfoVO
      );
    }

    // 7. createdAt
    List<String> HierarchyCreatedatAttributeIds = new ArrayList<>(
      currentHierarchyIds
    );
    HierarchyCreatedatAttributeIds.add(PropertyKey.CREATED_AT.getCode());
    DataModelDbColumnVO createdatDbColumnInfoVO = createDbColumnInfoVO(
      HierarchyCreatedatAttributeIds,
      AttributeValueType.DATE,
      null,
      null,
      false,
      beforeDbColumnVOMap
    );
    dbColumnVOMap.put(
      createdatDbColumnInfoVO.getColumnName().toLowerCase(),
      createdatDbColumnInfoVO
    );

    // 8. modifiedAt
    List<String> hierarchyModifiedatAttributeIds = new ArrayList<>(
      currentHierarchyIds
    );
    hierarchyModifiedatAttributeIds.add(PropertyKey.MODIFIED_AT.getCode());
    DataModelDbColumnVO modifiedatDbColumnInfoVO = createDbColumnInfoVO(
      hierarchyModifiedatAttributeIds,
      AttributeValueType.DATE,
      null,
      null,
      false,
      beforeDbColumnVOMap
    );
    dbColumnVOMap.put(
      modifiedatDbColumnInfoVO.getColumnName().toLowerCase(),
      modifiedatDbColumnInfoVO
    );
  }

  /**
   * Object형태의 Property Cache 로딩
   * @param parentHierarchyIds 계층구조 부모 AttributeId
   * @param objectMembers ChildAttribute
   * @param dbColumnInfoVOMap
   * @param beforeDbColumnVOMap
   */
  private void objectTypeToCache(
    List<String> parentHierarchyIds,
    List<ObjectMember> objectMembers,
    Map<String, DataModelDbColumnVO> dbColumnInfoVOMap,
    Map<String, DataModelDbColumnVO> beforeDbColumnVOMap
  ) {
    for (ObjectMember objectMember : objectMembers) {
      List<String> currentHierarchyIds = new ArrayList<>(parentHierarchyIds);
      currentHierarchyIds.add(objectMember.getName());

      if (
        objectMember.getValueType() == AttributeValueType.OBJECT &&
        objectMember.getObjectMembers() != null
      ) {
        objectTypeToCache(
          currentHierarchyIds,
          objectMember.getObjectMembers(),
          dbColumnInfoVOMap,
          beforeDbColumnVOMap
        );
      } else {
        DataModelDbColumnVO dbColumnInfoVO = createDbColumnInfoVO(
          currentHierarchyIds,
          objectMember.getValueType(),
          objectMember.getMaxLength(),
          objectMember.getIsRequired(),
          false,
          beforeDbColumnVOMap
        );
        dbColumnInfoVOMap.put(
          dbColumnInfoVO.getColumnName().toLowerCase(),
          dbColumnInfoVO
        );
      }
    }
  }

  /**
   * dataModel Cache 에 포함될 DbColumnInfoVO 정보 생성
   *
   * @param hierarchyAttributeIds 계층구조 attributeId 리스트 (list에 부모->자식 순의 계층구조로 attribute id가 저장됨)
   * @param valueType             attribute value type
   * @param isArrayObject         array object 여부
   * @return DbColumnInfoVO
   */
  private DataModelDbColumnVO createDbColumnInfoVO(
    List<String> hierarchyAttributeIds,
    AttributeValueType valueType,
    String maxLength,
    Boolean isRequired,
    boolean isArrayObject,
    Map<String, DataModelDbColumnVO> beforeDbColumnVOMap
  ) {
    // 1. column name 설정
    String columnName = null;
    // 기존에 이미 생성되어 있던 attribute인 경우 컬럼명을 그대로 사용
    if (beforeDbColumnVOMap != null) {
      for (DataModelDbColumnVO dbMetadata : beforeDbColumnVOMap.values()) {
        if (
          hierarchyAttributeIds.equals(dbMetadata.getHierarchyAttributeIds())
        ) {
          columnName = dbMetadata.getColumnName();
          break;
        }
      }
    }

    if (columnName == null) {
      // 신규 생성
      columnName = generateDbColumnName(hierarchyAttributeIds);
    }

    // 2. Db Column type 생성 (java의 value type과 1:1 매핑)
    DbColumnType columnType = null;
    if (isArrayObject) {
      columnType = arrayObjectValueTypeToDbColumnType(valueType);
    } else {
      columnType = valueTypeToDbColumnType(valueType);
    }

    // 3. dbColumnInfo 객체 생성 및 반환
    DataModelDbColumnVO dbColumnInfoVO = new DataModelDbColumnVO();
    dbColumnInfoVO.setHierarchyAttributeIds(hierarchyAttributeIds);
    dbColumnInfoVO.setDaoAttributeId(columnName.toLowerCase());
    dbColumnInfoVO.setColumnName(columnName.toLowerCase());
    dbColumnInfoVO.setColumnType(columnType);
    dbColumnInfoVO.setMaxLength(maxLength);
    dbColumnInfoVO.setIsNotNull(isRequired);
    return dbColumnInfoVO;
  }

  /**
   * dataModel Cache 에 포함될 Geo type의 DbColumnInfoVO 정보 생성
   *
   * @param hierarchyAttributeIds 계층구조 attributeId 리스트 (list에 부모->자식 순의 계층구조로 attribute id가 저장됨)
   * @return List<DbColumnInfoVO> Geotype은 기본적으로 4326과 3857 타입의 2개의 컬럼으로 생성되기 때문에 리스트형태로 반환
   */
  private List<DataModelDbColumnVO> createGeoDbColumnInfoVO(
    List<String> hierarchyAttributeIds,
    Boolean isRequired,
    Map<String, DataModelDbColumnVO> dbColumnVOMap
  ) {
    // 1. column name 설정
    String columnName = null;
    // 기존에 이미 생성되어 있던 attribute인 경우 컬럼명을 그대로 사용
    for (DataModelDbColumnVO dbMetadata : dbColumnVOMap.values()) {
      if (hierarchyAttributeIds.equals(dbMetadata.getHierarchyAttributeIds())) {
        columnName = dbMetadata.getColumnName();
        break;
      }
    }

    if (columnName == null) {
      // 신규 생성
      columnName = generateDbColumnName(hierarchyAttributeIds);
    }

    List<DataModelDbColumnVO> geoDbColumnInfoVOList = new ArrayList<>();

    // 2. 4326 Geo 컬럼 정보 생성
    DataModelDbColumnVO geo4326ColumnInfoVO = new DataModelDbColumnVO();
    geo4326ColumnInfoVO.setHierarchyAttributeIds(hierarchyAttributeIds);
    geo4326ColumnInfoVO.setDaoAttributeId(
      columnName.toLowerCase() + Constants.GEO_PREFIX_4326
    );
    geo4326ColumnInfoVO.setColumnName(
      columnName.toLowerCase() + Constants.GEO_PREFIX_4326
    );
    geo4326ColumnInfoVO.setColumnType(DbColumnType.GEOMETRY_4326);
    geo4326ColumnInfoVO.setIsNotNull(isRequired);
    geoDbColumnInfoVOList.add(geo4326ColumnInfoVO);

    // 3. 3857 Geo 컬럼 정보 생성
    DataModelDbColumnVO geo3857ColumnInfoVO = new DataModelDbColumnVO();
    geo3857ColumnInfoVO.setHierarchyAttributeIds(hierarchyAttributeIds);
    geo3857ColumnInfoVO.setDaoAttributeId(
      columnName.toLowerCase() + Constants.GEO_PREFIX_3857
    );
    geo3857ColumnInfoVO.setColumnName(
      columnName.toLowerCase() + Constants.GEO_PREFIX_3857
    );
    geo3857ColumnInfoVO.setColumnType(DbColumnType.GEOMETRY_3857);
    geo3857ColumnInfoVO.setIsNotNull(isRequired);
    geoDbColumnInfoVOList.add(geo3857ColumnInfoVO);

    return geoDbColumnInfoVOList;
  }

  /**
   * Java value type에 해당하는 Db column type조회
   *
   * @param attributeValueType attribute value type
   * @return DbColumnType
   */
  public DbColumnType valueTypeToDbColumnType(
    AttributeValueType attributeValueType
  ) {
    switch (attributeValueType) {
      case STRING:
        return DbColumnType.VARCHAR;
      case INTEGER:
        return DbColumnType.INTEGER;
      case LONG:
        return DbColumnType.BIGINT;
      case DOUBLE:
        return DbColumnType.FLOAT;
      case DATE:
        return DbColumnType.TIMESTAMP;
      case BOOLEAN:
        return DbColumnType.BOOLEAN;
      case ARRAY_STRING:
        return DbColumnType.ARRAY_VARCHAR;
      case ARRAY_INTEGER:
        return DbColumnType.ARRAY_INTEGER;
      case ARRAY_LONG:
        return DbColumnType.ARRAY_BIGINT;
      case ARRAY_DOUBLE:
        return DbColumnType.ARRAY_FLOAT;
      case ARRAY_BOOLEAN:
        return DbColumnType.ARRAY_BOOLEAN;
      case GEO_JSON:
        return DbColumnType.GEOMETRY_4326;
      default:
        return null;
    }
  }

  /**
   * java value type arrayObject 에 해당하는 Db column type 조회
   *
   * @param attributeValueType attribute value type
   * @return DbColumnType
   */
  public DbColumnType arrayObjectValueTypeToDbColumnType(
    AttributeValueType attributeValueType
  ) {
    switch (attributeValueType) {
      case STRING:
        return DbColumnType.ARRAY_VARCHAR;
      case INTEGER:
        return DbColumnType.ARRAY_INTEGER;
      case LONG:
        return DbColumnType.ARRAY_BIGINT;
      case DOUBLE:
        return DbColumnType.ARRAY_FLOAT;
      case DATE:
        return DbColumnType.ARRAY_TIMESTAMP;
      case BOOLEAN:
        return DbColumnType.ARRAY_BOOLEAN;
      default:
        return null;
    }
  }

  /**
   * 데이터모델 정보 기반 테이블명 생성
   *
   * @param id 데이터모델 아이디
   * @param postFix   테이블 뒤에 붙을 명칭 (Partial이력, Full이력테이블명 생성을 위해 사용)
   * @return 생성된 테이블명
   */
  public String generateRdbTableName(String id, String postFix) {
    StringBuilder tableName = new StringBuilder();
    tableName.append(id.replace("https://", "").replace("http://", ""));
    if (!ValidateUtil.isEmptyData(postFix)) {
      tableName.append(postFix);
    }

    return tableName.toString();
  }

  /**
   * 데이터모델 정보 기반 테이블명 생성
   *
   * @param id 데이터모델 아이디
   * @return 생성된 테이블명
   */
  public String generateRdbTableName(String id) {
    return generateRdbTableName(id, null);
  }

  /**
   *  데이터모델 정보 기반 테이블명 생성
   * @param id
   * @param postFix
   * @return 생성된 테이블명
   */
  public String generateHiveTableName(String id, String postFix) {
    StringBuilder tableName = new StringBuilder();
    tableName.append(id);
    if (!ValidateUtil.isEmptyData(postFix)) {
      tableName.append(postFix);
    }

    return StringUtil.removeSpecialCharAndLower(tableName.toString());
  }

  /**
   * 데이터모델 정보 기반 테이블명 생성
   * @param id
   * @return
   */
  public String generateHiveTableName(String id) {
    return generateHiveTableName(id, null);
  }

  /**
   *
   * attribute name이 short 인 경우 full uri 로 변경하여 반환
   * @param links
   * @param attributeNames
   */
  public List<String> convertAttrNameToFullUri(
    List<String> links,
    List<String> attributeNames
  ) {
    if (ValidateUtil.isEmptyData(attributeNames)) {
      return null;
    }

    List<String> fullUriAttrs = new ArrayList<>();

    Map<String, String> contextMap = contextToFlatMap(links);

    for (String attributeName : attributeNames) {
      // attribute name이 full uri 인 경우
      if (attributeName.startsWith("http")) {
        fullUriAttrs.add(attributeName);
        continue;
      }

      // attribute name이 short 형태인 경우
      if (contextMap == null) {
        throw new NgsiLdBadRequestException(
          ErrorCode.INVALID_PARAMETER,
          "Invalid attrs. name=" + attributeName + ", link=" + links
        );
      }

      if (contextMap.containsKey(attributeName)) {
        fullUriAttrs.add(contextMap.get(attributeName));
      } else {
        throw new NgsiLdBadRequestException(
          ErrorCode.INVALID_PARAMETER,
          "Invalid attrs. name=" + attributeName + ", link=" + links
        );
      }
    }

    return fullUriAttrs;
  }

  public Map<String, String> contextToFlatMap(List<String> contextUris) {
    if (contextUris == null) {
      return null;
    }

    Map<String, String> contextTotalMap = new HashMap<>();
    for (String contextUri : contextUris) {
      Map<String, String> contextMap = null;

      // 1. cache에서 context 정보 조회
      JsonldContextCacheVO jsonldContextCacheVO = contextCache.get(contextUri);
      if (
        jsonldContextCacheVO != null &&
        jsonldContextCacheVO.getExpireDatetime() != null && // 만료시간(필수)이 없는 경우 HTTP를 통한 조회
        new Date().before(jsonldContextCacheVO.getExpireDatetime())
      ) {
        contextMap = jsonldContextCacheVO.getContextFlatMap();
      }

      if (contextMap == null) {
        // 2. Cache에 없거나 만료시간이 지난 경우 DB에서 정보 조회 (만료시간이 지나지 않은 정보만 조회)
        JsonldContextBaseVO jsonldContextBaseVO = jsonldContextSVC.getJsonldContextByUrl(
          contextUri,
          new Date()
        );
        if (jsonldContextBaseVO != null) {
          try {
            contextMap =
              objectMapper.readValue(
                jsonldContextBaseVO.getRefinedPayload(),
                new TypeReference<Map<String, String>>() {}
              );
            // 2-1. DB조회 결과 Cache 에 입력
            putJsonldContextCache(
              contextUri,
              contextMap,
              jsonldContextBaseVO.getExpireDatetime()
            );
          } catch (Exception e) {
            log.warn("contextToFlatMap parse warn", e);
          }
        }
      }

      if (contextMap == null) {
        // 3. DB에서 조회되지 않는 경우 HTTP URI 에 접근하여 조회
        JsonldContextBaseVO jsonldContextBaseVO = getContextByUri(
          (String) contextUri
        );

        try {
          ContextVO contextVO = objectMapper.readValue(
            jsonldContextBaseVO.getPayload(),
            ContextVO.class
          );
          if (contextVO == null) {
            throw new NgsiLdContextNotAvailableException(
              ErrorCode.INVALID_CONTEXT_URI,
              "Retrieve @context error. body is empty. contextUri=" + contextUri
            );
          }

          contextMap = contextToFlatMap(contextVO.getContext(), null);

          // 3-1. HTTP 로 조회된 결과 DB 입력
          jsonldContextSVC.upsertJsonldContext(
            contextUri,
            jsonldContextBaseVO.getPayload(),
            objectMapper.writeValueAsString(contextMap),
            jsonldContextBaseVO.getExpireDatetime()
          );
          // 3-2. HTTP 로 조회된 결과 캐시 입력
          putJsonldContextCache(
            contextUri,
            contextMap,
            jsonldContextBaseVO.getExpireDatetime()
          );
        } catch (IOException e) {
          throw new NgsiLdContextNotAvailableException(
            ErrorCode.INVALID_CONTEXT_URI,
            "Retrieve @context error. body is empty. contextUri=" + contextUri,
            e
          );
        }
      }

      if (contextMap.isEmpty()) {
        log.warn("Get context result is empty. contextUri={}", contextUri);
      } else {
        // context에 중복 short name이 있는 경우 contextUri 입력 순서대로 override함
        contextTotalMap.putAll(contextMap);
      }
    }
    // 전체 context 결과 반환



    return contextTotalMap;
  }

  private Map<String, String> contextToFlatMap(
    Object context,
    Map<String, String> contextMap
  ) {
    if (context == null) {
      return contextMap;
    }

    if (contextMap == null) {
      contextMap = new HashMap<>();
    }

    if (context instanceof String) {
      if (((String) context).startsWith("http")) {
        JsonldContextBaseVO jsonldContextBaseVO = getContextByUri(
          (String) context
        );

        ContextVO contextVO = null;
        try {
          contextVO =
            objectMapper.readValue(
              jsonldContextBaseVO.getPayload(),
              ContextVO.class
            );
          if (contextVO == null) {
            throw new NgsiLdContextNotAvailableException(
              ErrorCode.INVALID_CONTEXT_URI,
              "Retrieve @context error. body is empty. contextUri=" +
              (String) context
            );
          }
        } catch (IOException e) {
          throw new NgsiLdContextNotAvailableException(
            ErrorCode.INVALID_CONTEXT_URI,
            "Retrieve @context error. body is empty. contextUri=" +
            (String) context,
            e
          );
        }
        contextToFlatMap(contextVO.getContext(), contextMap);
      }
    } else if (context instanceof Map) {
      Map<String, Object> contextInnerMap = (Map) context;
      for (Map.Entry<String, Object> entry : contextInnerMap.entrySet()) {
        String entryKey = entry.getKey();
        Object entryValue = entry.getValue();

        String value = null;
        if (entryValue instanceof String) {
          value = (String) entryValue;
        } else if (entryValue instanceof Map) {
          value = (String) ((Map) entryValue).get("@id");
          // @type 은?
        }

        if (value != null && !value.startsWith("http") && value.contains(":")) {
          String[] valueArr = value.split(":", 2);
          String valueReferenceUri = (String) contextInnerMap.get(valueArr[0]);
          if (!ValidateUtil.isEmptyData(valueReferenceUri)) {
            value = valueReferenceUri + valueArr[1];
          }
        }

        if (value != null && value.startsWith("http")) {
          contextMap.put(entryKey, value);
        } else {
          log.debug(
            "@context attribute value is not uri. entryKey={}, entryValue={}, value={}",
            entryKey,
            entryValue,
            value
          );
        }
      }
    } else if (context instanceof List) {
      for (Object innerContext : ((List) context)) {
        contextToFlatMap(innerContext, contextMap);
      }
    } else {
      throw new NgsiLdContextNotAvailableException(
        ErrorCode.INVALID_CONTEXT_URI,
        "Retrieve @context error. Unsupported class type. type=" +
        context.getClass()
      );
    }

    return contextMap;
  }

  private JsonldContextBaseVO getContextByUri(String contextUri) {
    MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
    headerMap.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    headerMap.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
    RequestEntity<Void> requestEntity = new RequestEntity<>(
      headerMap,
      HttpMethod.GET,
      URI.create(contextUri)
    );

    ResponseEntity<String> responseEntity = null;
    try {
      responseEntity = restTemplate.exchange(requestEntity, String.class);
    } catch (RestClientException e) {
      throw new NgsiLdContextNotAvailableException(
        ErrorCode.INVALID_CONTEXT_URI,
        "Retrieve @context error. " +
        "message=" +
        e.getMessage() +
        ", contextUri=" +
        contextUri
      );
    }

    if (responseEntity.getStatusCode() == HttpStatus.OK) {
      if (ValidateUtil.isEmptyData(responseEntity.getBody())) {
        throw new NgsiLdContextNotAvailableException(
          ErrorCode.INVALID_CONTEXT_URI,
          "Retrieve @context error. body is empty. contextUri=" + contextUri
        );
      }

      JsonldContextBaseVO jsonldContextBaseVO = new JsonldContextBaseVO();
      jsonldContextBaseVO.setUrl(contextUri);
      jsonldContextBaseVO.setPayload(responseEntity.getBody());

      int jsonldContextCacheAge = defaultJsonldContextCacheAge;
      try {
        String cacheControlHeader = responseEntity
          .getHeaders()
          .getCacheControl();
        if (
          !ValidateUtil.isEmptyData(cacheControlHeader) &&
          (
            cacheControlHeader.contains("max-age=") ||
            cacheControlHeader.contains("s-maxage=")
          )
        ) {
          String[] cacheControlHeaderArr = cacheControlHeader.split(",");
          for (String cacheControlHeaderPair : cacheControlHeaderArr) {
            String[] cacheControlHeaderPairArr = cacheControlHeaderPair.split(
              "="
            );
            // TODO: max-age와 s-maxage 중 우선순위가 무엇인지 체크 필요
            if ("max-age".equals(cacheControlHeaderPairArr[0])) {
              jsonldContextCacheAge =
                Integer.parseInt(cacheControlHeaderPairArr[1]);
              break;
            } else if ("s-maxage".equals(cacheControlHeaderPairArr[0])) {
              jsonldContextCacheAge =
                Integer.parseInt(cacheControlHeaderPairArr[1]);
              break;
            }
          }
        }
      } catch (Exception e) {
        log.warn("getContextByUri cache-control parsing error", e);
      }

      jsonldContextBaseVO.setExpireDatetime(
        new Date(System.currentTimeMillis() + jsonldContextCacheAge * 1000)
      );

      return jsonldContextBaseVO;
    } else {
      throw new NgsiLdContextNotAvailableException(
        ErrorCode.INVALID_CONTEXT_URI,
        "Retrieve @context error. " +
        "Invalid responseCode=" +
        responseEntity.getStatusCode() +
        ", contextUri=" +
        contextUri
      );
    }
  }

  public String generateDbColumnName(List<String> hierarchyAttrNames) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < hierarchyAttrNames.size(); i++) {
      if (i > 0) sb.append(Constants.COLUMN_DELIMITER);
      sb.append(hierarchyAttrNames.get(i));
    }

    if (
      sb.toString().getBytes().length <=
      Constants.POSTGRES_COLUMN_NAME_MAX_LENTH
    ) {
      return sb.toString();
    }

    String random4ByteString = StringUtil.generateRandomString(4);

    //      1) 63byte가 넘는 경우 1차적으로 아래와 같이 시도 (중략 컨셉-제안해주신 패턴에서 앞에 rootAttributeName 만 추가)
    //       - rootAttributeName + ﻿4바이트 랜덤 String + Property Name 또는 object member name + observed_at, created_at 등의 자체 생성 내용﻿
    //       - ﻿예시 : ﻿transmissivityvolume_﻿XXXX_predictedat_observedat
    sb.setLength(0);
    sb
      .append(hierarchyAttrNames.get(0))
      .append(Constants.COLUMN_DELIMITER)
      .append(random4ByteString);

    if (
      hierarchyAttrNames.size() > 2 &&
      isSystemPostfixField(
        hierarchyAttrNames.get(hierarchyAttrNames.size() - 1)
      )
    ) {
      sb
        .append(Constants.COLUMN_DELIMITER)
        .append(hierarchyAttrNames.get(hierarchyAttrNames.size() - 2));
    }
    sb
      .append(Constants.COLUMN_DELIMITER)
      .append(hierarchyAttrNames.get(hierarchyAttrNames.size() - 1));

    if (
      sb.toString().getBytes().length <=
      Constants.POSTGRES_COLUMN_NAME_MAX_LENTH
    ) {
      return sb.toString();
    }

    //      2) 그래도 63byte가 넘는 경우 아래와 같이 시도
    //       - ﻿4바이트 랜덤 String + Property Name 또는 object member name + observed_at, created_at 등의 자체 생성 내용﻿
    //       - 예시: ﻿﻿﻿XXXX_predictedat_observedat
    sb.setLength(0);
    sb.append(random4ByteString);

    if (
      hierarchyAttrNames.size() > 2 &&
      isSystemPostfixField(
        hierarchyAttrNames.get(hierarchyAttrNames.size() - 1)
      )
    ) {
      sb
        .append(Constants.COLUMN_DELIMITER)
        .append(hierarchyAttrNames.get(hierarchyAttrNames.size() - 2));
    }
    sb
      .append(Constants.COLUMN_DELIMITER)
      .append(hierarchyAttrNames.get(hierarchyAttrNames.size() - 1));

    if (
      sb.toString().getBytes().length <=
      Constants.POSTGRES_COLUMN_NAME_MAX_LENTH
    ) {
      return sb.toString();
    }

    //      3) ﻿그래도 넘는 경우 4바이트 랜덤 String + observed_at, created_at 등의 자체 생성 내용
    //       - ﻿ 예시) ﻿XXXX_observedat
    sb.setLength(0);
    sb.append(random4ByteString);

    if (
      isSystemPostfixField(
        hierarchyAttrNames.get(hierarchyAttrNames.size() - 1)
      )
    ) {
      sb
        .append(Constants.COLUMN_DELIMITER)
        .append(hierarchyAttrNames.get(hierarchyAttrNames.size() - 1));
    }

    return sb.toString();
  }

  private boolean isSystemPostfixField(String attributeName) {
    if (
      PropertyKey.OBSERVED_AT.getCode().equals(attributeName) ||
      PropertyKey.CREATED_AT.getCode().equals(attributeName) ||
      PropertyKey.MODIFIED_AT.getCode().equals(attributeName) ||
      PropertyKey.UNIT_CODE.getCode().equals(attributeName)
    ) {
      return true;
    }
    return false;
  }

  public String getColumnNameByStorageMetadata(
    DataModelStorageMetadataVO storageMetadataVO,
    List<String> hierarchyAttrNames
  ) {
    if (
      storageMetadataVO == null ||
      storageMetadataVO.getDbColumnInfoVOMap() == null ||
      hierarchyAttrNames == null
    ) {
      return null;
    }

    for (DataModelDbColumnVO dataModelDbColumnVO : storageMetadataVO
      .getDbColumnInfoVOMap()
      .values()) {
      if (
        hierarchyAttrNames.equals(
          dataModelDbColumnVO.getHierarchyAttributeIds()
        )
      ) {
        return dataModelDbColumnVO.getColumnName().toLowerCase();
      }
    }
    return null;
  }

  public List<String> getColumnNamesByStorageMetadata(
    DataModelStorageMetadataVO storageMetadataVO,
    List<String> hierarchyAttrNames
  ) {
    if (
      storageMetadataVO == null ||
      storageMetadataVO.getDbColumnInfoVOMap() == null ||
      hierarchyAttrNames == null
    ) {
      return null;
    }

    List<String> columnNames = null;
    for (DataModelDbColumnVO dataModelDbColumnVO : storageMetadataVO
      .getDbColumnInfoVOMap()
      .values()) {
      if (
        hierarchyAttrNames.equals(
          dataModelDbColumnVO.getHierarchyAttributeIds()
        )
      ) {
        if (columnNames == null) {
          columnNames = new ArrayList<>();
        }
        columnNames.add(dataModelDbColumnVO.getColumnName().toLowerCase());
      }
    }
    return columnNames;
  }

  private void putJsonldContextCache(
    String url,
    Map<String, String> jsonldContextFlatMap,
    Date expireDate
  ) {
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
    dataModelCache = null;
  }
}
