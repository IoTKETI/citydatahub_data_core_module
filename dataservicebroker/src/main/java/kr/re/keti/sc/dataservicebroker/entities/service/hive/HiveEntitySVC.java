package kr.re.keti.sc.dataservicebroker.entities.service.hive;

import static kr.re.keti.sc.dataservicebroker.common.code.Constants.DEFAULT_SRID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeValueType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DefaultAttributeKey;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DefaultDbColumnName;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.HistoryStoreType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.PropertyKey;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.UseYn;
import kr.re.keti.sc.dataservicebroker.common.vo.AttributeVO;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityFullVO;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityVO;
import kr.re.keti.sc.dataservicebroker.common.vo.EntityProcessVO;
import kr.re.keti.sc.dataservicebroker.common.vo.GeoPropertyVO;
import kr.re.keti.sc.dataservicebroker.common.vo.ProcessResultVO;
import kr.re.keti.sc.dataservicebroker.common.vo.PropertyVO;
import kr.re.keti.sc.dataservicebroker.common.vo.RelationshipVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityFullVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.ObjectMember;
import kr.re.keti.sc.dataservicebroker.entities.dao.EntityDAOInterface;
import kr.re.keti.sc.dataservicebroker.entities.service.DefaultEntitySVC;
import kr.re.keti.sc.dataservicebroker.util.DateUtil;
import kr.re.keti.sc.dataservicebroker.util.ObservedAtReverseOrder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HiveEntitySVC extends DefaultEntitySVC {

	@Value("${entity.retrieve.include.datasetid:N}")
	private String retrieveIncludeDatasetid; // 조회 시 datasetId 포함여부
	@Value("${entity.default.history.store.type:full}")
	private String defaultHistoryStoreType; // 데이터 셋 정보가 없는 경우 기본 이력 저장 유형

    @Override
	protected String getTableName(DataModelCacheVO dataModelCacheVO) {
		return dataModelCacheVO.getDataModelStorageMetadataVO().getHiveTableName();
	}
    
    @Override
    protected BigDataStorageType getStorageType() {
    	return BigDataStorageType.HIVE;
    }

    @Override
	public void setEntityDAOInterface(EntityDAOInterface<DynamicEntityDaoVO> entityDAO) {
    	this.entityDAO = entityDAO;
	}

    @Override
    protected Object getGeometryAttrData(DynamicEntityDaoVO dynamicEntityDaoVO, String columnName) {
        if (dynamicEntityDaoVO.get(columnName) != null && dynamicEntityDaoVO.get(columnName) instanceof String) {
            Object geometry = dynamicEntityDaoVO.get(columnName);

            if (geometry != null) {
                Map<String, Object> convertedGeometry = null;
                try {
                    convertedGeometry = objectMapper.readValue((String) geometry, Map.class);
                    return convertedGeometry.get("geometry");
                } catch (JsonProcessingException e) {
                    log.error("Error while parsing geometry", e);
                }
            }
        }
        return null;
    }

    @Override
    protected Object getArrayAttrData(AttributeValueType attributeValueType, DynamicEntityDaoVO dynamicEntityDaoVO, String columnName) {

        String arrayValue = (String) dynamicEntityDaoVO.get(columnName);

        if (arrayValue != null) {
            String[] values = arrayValue.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
            List<Object> castedValues = new ArrayList<>();

            for (String value : values) {
                if (attributeValueType == AttributeValueType.ARRAY_INTEGER) {
                    castedValues.add(Integer.parseInt(value));
                } else if (attributeValueType == AttributeValueType.ARRAY_DOUBLE) {
                    castedValues.add(Double.parseDouble(value));
                } else if (attributeValueType == AttributeValueType.ARRAY_BOOLEAN) {
                    castedValues.add(Boolean.parseBoolean(value));
                } else if (attributeValueType == AttributeValueType.ARRAY_STRING) {
                    castedValues.add(value.replace("{", "").replace("}", "").replace("\"", ""));
                }
            }
            return castedValues;
        }
        return null;
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
                logMessage.append(entityProcessVO.getDatasetId()).append(" Process SUCCESS");
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
            	// default 'PARTIAL'
            	createFullHistoryTargetVOList.add(entityDaoVO);
            }
        }

        if (logMessage.length() > 0) log.info(logMessage.toString());

        // 5. 이력 벌크 저장
        // 5-1 Partial 이력 저장 (요청받은 Entity 파라미터값만 저장)
        if (createPartialHistoryVOList != null && createPartialHistoryVOList.size() > 0) {
            try {
            	entityDAO.bulkCreateHist(createPartialHistoryVOList);
            } catch (Exception e) {
                log.error("Store entity PARTIAL history error", e);
            }
        }

        // 5-2. Full 이력 저장 (Entity 의 최종값 조회 후 이력에 모든 파라미터값 저장)
        /*
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
                DynamicEntityDaoVO entityFullDaoVO = entityDAO.selectById(queryVO, true);
                if (entityFullDaoVO == null) {
                    log.warn("Store entity FULL history error. Now exist Entity id=" + entityDaoVO.getId());
                    continue;
                }

                entityFullDaoVO.setDbTableName(StringUtil.removeSpecialCharAndLower(entityDaoVO.getDbTableName()));
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
                    DataModelDbColumnVO dbColumnInfoVO = entry.getValue();
                    Object value = entityFullDaoVO.get(columnName.toLowerCase());
                    if (value != null) {
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
                            Object geom = value;
                            entityFullDaoVO.put(columnName, geom);
                            entityFullDaoVO.put(columnName + "_idx", hiveTableSVC.getIndex(columnName + "_idx", entityFullDaoVO.getDbTableName(), entityFullDaoVO.getId()));
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
        */
        if (createFullHistoryTargetVOList != null && createFullHistoryTargetVOList.size() > 0) {
            try {
                entityDAO.bulkCreateFullHist(createFullHistoryTargetVOList);
            } catch (Exception e) {
                log.error("Store entity FULL history error", e);
            }
        }
    }

}