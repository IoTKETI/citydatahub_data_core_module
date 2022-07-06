package kr.re.keti.sc.dataservicebroker.entities.service.rdb;

import static kr.re.keti.sc.dataservicebroker.common.code.Constants.DEFAULT_SRID;

import java.util.*;

import org.postgis.PGgeometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeValueType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DbColumnType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DefaultAttributeKey;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DefaultDbColumnName;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.HistoryStoreType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.Operation;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.PropertyKey;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.RetrieveOptions;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.UseYn;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.vo.AttributeVO;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityFullVO;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityVO;
import kr.re.keti.sc.dataservicebroker.common.vo.EntityProcessVO;
import kr.re.keti.sc.dataservicebroker.common.vo.GeoPropertiesVO;
import kr.re.keti.sc.dataservicebroker.common.vo.GeoPropertyVO;
import kr.re.keti.sc.dataservicebroker.common.vo.ProcessResultVO;
import kr.re.keti.sc.dataservicebroker.common.vo.PropertiesVO;
import kr.re.keti.sc.dataservicebroker.common.vo.PropertyVO;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.common.vo.RelationshipVO;
import kr.re.keti.sc.dataservicebroker.common.vo.RelationshipsVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityFullVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelStorageMetadataVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.ObjectMember;
import kr.re.keti.sc.dataservicebroker.entities.dao.EntityDAOInterface;
import kr.re.keti.sc.dataservicebroker.entities.service.DefaultEntitySVC;
import kr.re.keti.sc.dataservicebroker.util.DateUtil;
import kr.re.keti.sc.dataservicebroker.util.ObservedAtReverseOrder;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RdbEntitySVC extends DefaultEntitySVC {

	@Value("${entity.retrieve.include.datasetid:N}")
	private String retrieveIncludeDatasetid; // 조회 시 datasetId 포함여부
	@Value("${entity.default.history.store.type:full}")
	private String defaultHistoryStoreType; // 데이터 셋 정보가 없는 경우 기본 이력 저장 유형

    @Autowired
    protected ObjectMapper objectMapper;

    @Override
	protected String getTableName(DataModelCacheVO dataModelCacheVO) {
		return dataModelCacheVO.getDataModelStorageMetadataVO().getRdbTableName();
	}
    
    @Override
    protected BigDataStorageType getStorageType() {
    	return BigDataStorageType.RDB;
    }

    @Override
	public void setEntityDAOInterface(EntityDAOInterface<DynamicEntityDaoVO> entityDAO) {
    	this.entityDAO = entityDAO;
	}

    @Override
    protected Object getGeometryAttrData(DynamicEntityDaoVO dynamicEntityDaoVO, String columnName) {
        PGgeometry pGgeometry = (PGgeometry) dynamicEntityDaoVO.get(columnName);
        if(pGgeometry != null) {
            return pGgeometry.getGeometry();
        }
        return null;
    }

    @Override
    protected Object getArrayAttrData(AttributeValueType attributeValueType, DynamicEntityDaoVO dynamicEntityDaoVO, String columnName) {
        return dynamicEntityDaoVO.get(columnName);
    }

    /**
     * Operation 정상처리 항목 이력 저장
     *
     * @param entityProcessVOList 처리VO리스트
     */
    @Override
    public void storeEntityStatusHistory(List<EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO>> entityProcessVOList) {

        // 1. 이력저장 대상 리스트 객체 생성
        List<DynamicEntityDaoVO> createPartialHistoryVOList = new ArrayList<>();
        List<DynamicEntityDaoVO> createFullHistoryTargetVOList = new ArrayList<>();

        StringBuilder logMessage = new StringBuilder();

        for (EntityProcessVO<DynamicEntityFullVO, DynamicEntityDaoVO> entityProcessVO : entityProcessVOList) {

            DynamicEntityDaoVO entityDaoVO = entityProcessVO.getEntityDaoVO();
            ProcessResultVO processResultVO = entityProcessVO.getProcessResultVO();

            // 2. 실패 항목은 이력저장에서 제외
            if (!processResultVO.isProcessResult()) {
                continue;
            }

            if (logMessage.length() == 0) {
                logMessage.append("Process SUCCESS.");
                if(entityProcessVO.getDatasetId() != null) {
                    logMessage.append(" datasetId=").append(entityProcessVO.getDatasetId());
                } else {
                    logMessage.append(" Not include datasetId");
                }
            }
            logMessage.append(System.lineSeparator()).append("\t")
                    .append("eventTime=").append(DateUtil.dateToDbFormatString(entityDaoVO.getModifiedAt()))
                    .append(", id=").append(entityDaoVO.getId())
                    .append(", processOperation=").append(processResultVO.getProcessOperation());

            // 3. 실제 수행된 Operation 값 설정
            entityDaoVO.setOperation(processResultVO.getProcessOperation());

            // 4. 이력저장 목록 추가
            HistoryStoreType historyStoreType = null;
            if(entityProcessVO.getDatasetId() != null) {
            	historyStoreType = dataModelManager.getHistoryStoreType(entityProcessVO.getDatasetId());
            } else {
            	historyStoreType = HistoryStoreType.parseType(defaultHistoryStoreType);
            }

            // PARTIAL, FULL 모두 저장
            if(historyStoreType == HistoryStoreType.ALL) {
            	createFullHistoryTargetVOList.add(entityDaoVO);
                createPartialHistoryVOList.add(entityDaoVO);
            // FULL 이력만 저장
            } else if(historyStoreType == HistoryStoreType.FULL) {
            	createFullHistoryTargetVOList.add(entityDaoVO);
            // PARTIAL 이력만 저장
            } else if(historyStoreType == HistoryStoreType.PARTIAL) {
            	createPartialHistoryVOList.add(entityDaoVO);
            // 이력 저장하지 않음
            } else if(historyStoreType == HistoryStoreType.NONE) {

            } else {
            	// default 'FULL'
            	createFullHistoryTargetVOList.add(entityDaoVO);
            }
        }

        if (logMessage.length() > 0) log.info(logMessage.toString());

        // 5. 이력 벌크 저장
        // 5-1 Partial 이력 저장 (요청받은 Entity 파라미터값만 저장)
        if (createPartialHistoryVOList != null && createPartialHistoryVOList.size() > 0) {
        	
        	// property 별 created_at 조회를 위한 Entity 최종값 조회
            for (DynamicEntityDaoVO entityDaoVO : createFullHistoryTargetVOList) {
            	// Delete 는 제외
                if (Operation.DELETE_ENTITY == entityDaoVO.getOperation()) {
                    continue;
                }

                QueryVO queryVO = new QueryVO();
                queryVO.setId(entityDaoVO.getId());
                queryVO.setDatasetId(entityDaoVO.getDatasetId());
                queryVO.setType(entityDaoVO.getEntityType());
                queryVO.setOptions(RetrieveOptions.SYS_ATTRS.getCode());
                queryVO.setLinks(entityDaoVO.getContext());
                DynamicEntityDaoVO entityFullDaoVO = entityDAO.selectById(queryVO, true);
                if (entityFullDaoVO == null) {
                    log.warn("Store entity Partial history error. Now exist Entity id=" + entityDaoVO.getId());
                    continue;
                }

                for(Map.Entry<String, Object> entry : entityDaoVO.entrySet()) {
                	String key = entry.getKey();

                	// _created_at 으로 끝나는 컬럼 데이터
                	if(key.endsWith(Constants.COLUMN_DELIMITER + PropertyKey.CREATED_AT.getCode())) {
                		if(entityFullDaoVO.containsKey(key.toLowerCase())) {
                			entityDaoVO.put(key, entityFullDaoVO.get(key.toLowerCase()));
                		}
                	}
                }
            }

            try {
                entityDAO.bulkCreateHist(createPartialHistoryVOList);
            } catch (Exception e) {
                log.error("Store entity PARTIAL history error", e);
            }
        }

        // 5-2. Full 이력 저장 (Entity 의 최종값 조회 후 이력에 모든 파라미터값 저장)
        if (createFullHistoryTargetVOList != null && createFullHistoryTargetVOList.size() > 0) {

            List<DynamicEntityDaoVO> createFullHistoryVOList = new ArrayList<>();

            // Full 이력저장을 위한 Entity 최종값 조회
            for (DynamicEntityDaoVO entityDaoVO : createFullHistoryTargetVOList) {

                // Delete 는 Full 이력 저장하지 않음
                if (Operation.DELETE_ENTITY == entityDaoVO.getOperation()) {
                    continue;
                }

                QueryVO queryVO = new QueryVO();
                queryVO.setId(entityDaoVO.getId());
                queryVO.setDatasetId(entityDaoVO.getDatasetId());
                queryVO.setType(entityDaoVO.getEntityType());
                queryVO.setOptions(RetrieveOptions.SYS_ATTRS.getCode());
                queryVO.setLinks(entityDaoVO.getContext());
                DynamicEntityDaoVO entityFullDaoVO = entityDAO.selectById(queryVO, true);
                if (entityFullDaoVO == null) {
                    log.warn("Store entity FULL history error. Now exist Entity id=" + entityDaoVO.getId());
                    continue;
                }

                entityFullDaoVO.setDbTableName(entityDaoVO.getDbTableName());
                entityFullDaoVO.setDbColumnInfoVOMap(entityDaoVO.getDbColumnInfoVOMap());
                entityFullDaoVO.setDatasetId(entityDaoVO.getDatasetId());
                entityFullDaoVO.setEntityType(entityDaoVO.getEntityType());
                entityFullDaoVO.setOperation(entityDaoVO.getOperation());
                entityFullDaoVO.setCreatedAt(entityDaoVO.getCreatedAt());
                entityFullDaoVO.setModifiedAt(entityDaoVO.getModifiedAt());

                // DB입력을 위한 Array 및 Geo타입 데이터 변환 처리
                // lower case -> camel(원래 명칭)으로 변환 (TODO: 개선)
                Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = entityDaoVO.getDbColumnInfoVOMap();
                for (Map.Entry<String, DataModelDbColumnVO> entry : dbColumnInfoVOMap.entrySet()) {

                	String columnName = entry.getKey().toLowerCase();

                	// Ingest 요청으로 받은 값은 최종값 조회 데이터보다 우선적으로 사용 (property의 createdAt값 제외)
                	Object receivedValue = entityDaoVO.get(entry.getKey());
                	if(receivedValue != null && !columnName.endsWith(Constants.COLUMN_DELIMITER + PropertyKey.CREATED_AT.getCode().toLowerCase())) {
                		entityFullDaoVO.put(columnName, receivedValue);
                		continue;
                	}

                	DataModelDbColumnVO dbColumnInfoVO = entry.getValue();
                	Object value = entityFullDaoVO.get(columnName.toLowerCase());
                	if(value != null) {
                		// String[] -> List<String>, Integer[] -> List<Integer>, Float[] -> List<Float> 변환처리
                        if (dbColumnInfoVO.getColumnType() == DbColumnType.ARRAY_VARCHAR
                                || dbColumnInfoVO.getColumnType() == DbColumnType.ARRAY_INTEGER
                                || dbColumnInfoVO.getColumnType() == DbColumnType.ARRAY_FLOAT) {
                            Object[] valueArr = (Object[]) value;
                            List<Object> valueList = new ArrayList<>(valueArr.length);
                            for (int i = 0; i < valueArr.length; i++) {
                                valueList.add(valueArr[i]);
                            }
                            entityFullDaoVO.put(columnName, valueList);

                        // Timestamp[] 를 List<Date> 변환처리
                        } else if (dbColumnInfoVO.getColumnType() == DbColumnType.ARRAY_TIMESTAMP) {
                            Object[] dateArr = (Object[]) value;
                            List<Date> dateList = new ArrayList<>(dateArr.length);
                            for (int i = 0; i < dateArr.length; i++) {
                                dateList.add((Date) dateArr[i]);
                            }
                            entityFullDaoVO.put(columnName, dateList);

                        // PGgeometry -> GeoJson 변환처리
                        } else if (dbColumnInfoVO.getColumnType() == DbColumnType.GEOMETRY_4326) {

                            PGgeometry pGgeometry = (PGgeometry) value;
                            if(pGgeometry != null) {
                            	try {
                            		String geoJson = objectMapper.writeValueAsString(pGgeometry.getGeometry());
									entityFullDaoVO.put(columnName, geoJson);
									entityFullDaoVO.put(columnName.replace(Constants.GEO_PREFIX_4326, Constants.GEO_PREFIX_3857), geoJson);
								} catch (Exception e) {
									log.error("Store entity FULL history pGgeometry parsing error.", e);
								}
                            }
                        }
                        continue;
                	}
                }
                createFullHistoryVOList.add(entityFullDaoVO);
            }

            try {
                entityDAO.bulkCreateFullHist(createFullHistoryVOList);
            } catch (Exception e) {
                log.error("Store entity FULL history error", e);
            }
        }
    }
}