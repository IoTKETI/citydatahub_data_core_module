package kr.re.keti.sc.dataservicebroker.entities.bulkprocessor;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.dataservicebroker.common.bulk.IBulkProcessor;
import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.Operation;
import kr.re.keti.sc.dataservicebroker.common.exception.CoreException;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityFullVO;
import kr.re.keti.sc.dataservicebroker.common.vo.EntityProcessVO;
import kr.re.keti.sc.dataservicebroker.common.vo.IngestMessageVO;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.entities.service.EntitySVCInterface;
import kr.re.keti.sc.dataservicebroker.notification.NotificationManager;
import kr.re.keti.sc.dataservicebroker.notification.vo.NotificationProcessVO;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EntityBulkProcessor<T1 extends CommonEntityFullVO, T2 extends CommonEntityDaoVO> implements IBulkProcessor<IngestMessageVO> {

	/** Operation 처리 서비스 */
	@Autowired
	@Qualifier("rdbDynamicEntitySVC")
	private EntitySVCInterface<T1, T2> rdbEntitySVC;
	@Autowired(required = false)
	@Qualifier("hiveDynamicEntitySVC")
	private EntitySVCInterface<T1, T2> hiveEntitySVC;
	@Autowired(required = false)
	@Qualifier("hbaseDynamicEntitySVC")
	private EntitySVCInterface<T1, T2> hbaseEntitySVC;
	@Autowired
	private NotificationManager notificationManager;
	@Autowired
	private DataModelManager dataModelManager;
	@Value("${entity.default.storage:rdb}")
    private String defaultStorageType;

	/** 에러데이터 기록용 로거 */
	private final Logger errorDataLogger = LoggerFactory.getLogger(Constants.KAFKA_REQUEST_ERROR_LOGGER_NAME);

	@Override
	public Object processBulk(List<IngestMessageVO> requestMessageVOList) {

		List<EntityProcessVO<T1, T2>> processVOList = null;
		
		// 1. Operation 별 벌크 처리
		// 데이터셋 아이디 동일 (모두 동일한 토픽에서 얻은 데이터)
		String datasetId = requestMessageVOList.get(0).getDatasetId();
		List<BigDataStorageType> bigDataStorageTypes = null;
		if(!ValidateUtil.isEmptyData(datasetId)) {
			bigDataStorageTypes = dataModelManager.getDatasetFlowCache(datasetId).getBigDataStorageTypes();

		// 데이터 셋 아이디가 없는 요청 (ex. HTTP) 인 경우 설정에 있는 entity.default.storage 값 기반 동작
		} else {
			BigDataStorageType defaultBigDataStorageType = BigDataStorageType.parseType(defaultStorageType);
			if(defaultBigDataStorageType != null) {
				bigDataStorageTypes = Arrays.asList(defaultBigDataStorageType);
			}
		}
		
		// 하위버전 호환을 위해 설정이 없는 경우 디폴트 RDB 적재
		if(bigDataStorageTypes == null || bigDataStorageTypes.size() == 0) {
			processVOList = rdbEntitySVC.processBulk(requestMessageVOList);
			// 에러 발생 시 이력 저장
	        storeErrorHistory(processVOList);

		} else {
			// RDB 적재
			if(bigDataStorageTypes.contains(BigDataStorageType.RDB)) {
				processVOList = rdbEntitySVC.processBulk(requestMessageVOList);
				// 에러 발생 시 이력 저장
		        storeErrorHistory(processVOList);
			}
			// HIVE 적재
			if(bigDataStorageTypes.contains(BigDataStorageType.HIVE)) {
				processVOList = hiveEntitySVC.processBulk(requestMessageVOList);
				// 에러 발생 시 이력 저장
		        storeErrorHistory(processVOList);
			}
			// HBASE 적재 (추후 분기하여 구현)
			if (bigDataStorageTypes.contains(BigDataStorageType.HBASE)) {
				processVOList = hiveEntitySVC.processBulk(requestMessageVOList);
				// 에러 발생 시 이력 저장
				storeErrorHistory(processVOList);
			}
		}

        // 2. Notification 전송 여부 체크 및 전송
        processNotification(processVOList);

        // 3. 에러 이력 저장
//        storeErrorHistory(processVOList);

        return processVOList;
	}

    /**
     * HTTP Notification 전송
     * @param entityProcessVOList 처리대상VO리스트
     */
    private void processNotification(List<EntityProcessVO<T1, T2>> entityProcessVOList) {

    	if(!notificationManager.enableChangeEventNotification()) {
    		return;
    	}

    	for (EntityProcessVO<T1, T2> entityProcessVO : entityProcessVOList) {
            // 처리결과가 성공인 경우
            if (entityProcessVO.getProcessResultVO().isProcessResult()) {
            	
            	T2 entityDaoVO = entityProcessVO.getEntityDaoVO();

            	// 실제 수행된 Operation이 DELETE_ENTITY 인 경우 Notification 전송 하지 않음
            	Operation processOperation = entityProcessVO.getProcessResultVO().getProcessOperation();
            	if(processOperation == Operation.DELETE_ENTITY) {
            		continue;
            	}

            	NotificationProcessVO notificationProcessVO = new NotificationProcessVO();
            	notificationProcessVO.setEntityId(entityDaoVO.getId());
            	notificationProcessVO.setEntityType(entityProcessVO.getEntityFullVO().getType());
            	notificationProcessVO.setEntityTypeUri(entityProcessVO.getDataModelCacheVO().getDataModelVO().getTypeUri());
            	notificationProcessVO.setDatasetId(entityProcessVO.getDatasetId());
            	notificationProcessVO.setEventTime(entityDaoVO.getModifiedAt());
            	notificationProcessVO.setRequestEntityFullVO(entityProcessVO.getEntityFullVO());
            	notificationManager.produceData(notificationProcessVO);
            }
    	}
    }

    /**
     * 처리결과가 false인 항목들에 대해 에러 처리
     * @param entityProcessVOList 처리대상VO리스트
     */
    private void storeErrorHistory(List<EntityProcessVO<T1, T2>> entityProcessVOList) {

        for (EntityProcessVO<T1, T2> entityProcessVO : entityProcessVOList) {
            // result 가 실패인 경우
            if (!entityProcessVO.getProcessResultVO().isProcessResult()) {
                this.storeErrorHistory(entityProcessVO);
            }
        }
    }

    /**
     * 에러 이력 저장
     * @param entityProcessVO 에러 이력 저장 대상 VO
     */
    private void storeErrorHistory(EntityProcessVO<T1, T2> entityProcessVO) {

    	StringBuilder errorLog = new StringBuilder();
    	
        errorLog.append("DatasetId=").append(entityProcessVO.getDatasetId())
                .append(", ").append(entityProcessVO.getProcessResultVO().toString())
                .append(", Content=").append(entityProcessVO.getContent());

        log.warn(errorLog.toString());
        errorDataLogger.warn(errorLog.toString());
    }

	@Override
	public Object processSingle(IngestMessageVO object) {
		throw new CoreException(ErrorCode.NOT_SUPPORTED_METHOD, "EntityBulkProcessor not support processSingle.");
	}
}