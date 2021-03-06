package kr.re.keti.sc.dataservicebroker.datasetflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.StorageType;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.InternalServerErrorException;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.service.hive.HiveTableSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.sqlprovider.BigdataTableSqlProvider;
import kr.re.keti.sc.dataservicebroker.datamodel.sqlprovider.RdbTableSqlProvider;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelBaseVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelVO;
import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.dao.DatasetFlowDAO;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.DatasetFlowBaseVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.DatasetFlowProvisioningVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.RetrieveDatasetFlowBaseVO;
import kr.re.keti.sc.dataservicebroker.entities.controller.kafka.consumer.KafkaConsumerManager;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DatasetFlowSVC {

    private final DatasetFlowDAO datasetFlowDAO;
	private final DatasetFlowRetrieveSVC datasetFlowRetrieveSVC;
    private final DatasetRetrieveSVC datasetRetrieveSVC;
	private final DataModelSVC dataModelSVC;
    private final DataModelRetrieveSVC dataModelRetrieveSVC;
    private final DataModelManager dataModelManager;
    private final BigdataTableSqlProvider hiveDataModelSqlProvider;
    private final RdbTableSqlProvider rdbDataModelSqlProvider;
    private final KafkaConsumerManager kafkaConsumerManager;
	private final HiveTableSVC hiveTableSVC;
    private final ObjectMapper objectMapper;

    private final Pattern URI_PATTERN_DATASET_FLOW = Pattern.compile("/datasets/(?<datasetId>.+[^/])/flow");

	public DatasetFlowSVC(
			DatasetFlowDAO datasetFlowDAO,
			DatasetFlowRetrieveSVC datasetFlowRetrieveSVC,
			DatasetRetrieveSVC datasetRetrieveSVC,
			DataModelSVC dataModelSVC,
			DataModelRetrieveSVC dataModelRetrieveSVC,
			DataModelManager dataModelManager,
			BigdataTableSqlProvider hiveDataModelSqlProvider,
			RdbTableSqlProvider rdbDataModelSqlProvider,
			KafkaConsumerManager kafkaConsumerManager,
			@Nullable HiveTableSVC hiveTableSVC,
			ObjectMapper objectMapper
	) {
		this.datasetFlowDAO = datasetFlowDAO;
		this.datasetFlowRetrieveSVC = datasetFlowRetrieveSVC;
		this.datasetRetrieveSVC = datasetRetrieveSVC;
		this.dataModelSVC = dataModelSVC;
		this.dataModelRetrieveSVC = dataModelRetrieveSVC;
		this.dataModelManager = dataModelManager;
		this.hiveDataModelSqlProvider = hiveDataModelSqlProvider;
		this.rdbDataModelSqlProvider = rdbDataModelSqlProvider;
		this.kafkaConsumerManager = kafkaConsumerManager;
		this.hiveTableSVC = hiveTableSVC;
		this.objectMapper = objectMapper;
	}

	@PostConstruct
	private void initDatasetFlow() {
		// 1. ????????? ??? ?????? ?????? ??????
		List<DatasetFlowBaseVO> datasetFlowBaseVOs = datasetFlowDAO.getDatasetFlowBaseVOList();
		if(datasetFlowBaseVOs != null && datasetFlowBaseVOs.size() > 0) {
			for(DatasetFlowBaseVO datasetFlowBaseVO : datasetFlowBaseVOs) {
				// enabled ??? ?????? ?????? pass
				if(!datasetFlowBaseVO.getEnabled()) {
					continue;
				}
				// 2. ????????? ??? ????????? ?????? ????????? ????????? ??????
				kafkaConsumerManager.registKafkaConsumer(datasetFlowBaseVO.getDatasetId());
			}
		}
	}


	/**
	 * ????????? ??? ?????? ??????
	 * @param to ????????? ??? ?????? ?????? ?????? ?????? url
     * @param requestBody ?????? Body
     * @param requestId Provisioning Request Id
     * @param eventTime Provisioning Request Time
	 */
    public void createDatasetFlow(String to, String requestBody, String requestId, Date eventTime) {

    	// 1. Request URI?????? ????????? ??????
		Matcher matcherForUpdate = URI_PATTERN_DATASET_FLOW.matcher(to);

    	if(matcherForUpdate.find()) {
			String datasetId = matcherForUpdate.group("datasetId");

			// 2. ?????? ????????? ??????
			DatasetFlowProvisioningVO datasetFlowProvisioningVO = null;
			try {
				datasetFlowProvisioningVO = objectMapper.readValue(requestBody, DatasetFlowProvisioningVO.class);
			} catch (IOException e) {
				throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
	                    "Invalid Parameter. body=" + requestBody);
			}
			datasetFlowProvisioningVO.setDatasetId(datasetId);
			DatasetFlowBaseVO datasetFlowBaseVO = datasetFlowVOToDatasetFlowBaseVO(datasetFlowProvisioningVO);

			// 3. ????????? ??????
			// 3-1. ????????? ??? ?????? ??? ???????????? ??????
			DatasetFlowBaseVO retrieveDatasetFlowBaseVO = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(datasetId);
			if(retrieveDatasetFlowBaseVO != null) {
				// 3. ??????????????? ?????? ?????? IngestInterface ?????????????????? DB ?????? ????????? ??????
				if(alreadyProcessByOtherInstance(requestId, eventTime, retrieveDatasetFlowBaseVO)) {
		        	// ?????? Instance?????? DB ???????????? ?????? ?????????????????? ????????? ???????????? ?????? ??????
					DatasetBaseVO datasetBaseVO = dataModelManager.getDatasetCache(retrieveDatasetFlowBaseVO.getDatasetId());
					dataModelManager.reloadDataModelCache(datasetBaseVO.getDataModelId());
					dataModelManager.putDatasetFlowCache(retrieveDatasetFlowBaseVO);
		            return;
		        } else {
		        	// ?????? ??????????????? ????????????
		        	updateDatasetFlow("/datasets/" + datasetId + "/flow", requestBody, requestId, eventTime);
		        	return;
	        	}
			}
			
			// 3-2. ????????? ??? ???????????? ??????
			DatasetBaseVO datasetBaseVO = datasetRetrieveSVC.getDatasetVOById(datasetId);
			if(datasetBaseVO == null) {
				throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
	                    "Dataset is not exists. datasetId=" + datasetId);
			}

			// 3-3. ??????????????? ???????????? ??????
			DataModelBaseVO dataModelBaseVO = dataModelRetrieveSVC.getDataModelBaseVOById(datasetBaseVO.getDataModelId());
			DataModelCacheVO dataModelCacheVO = dataModelManager.getDataModelVOCacheById(dataModelBaseVO.getId());

			if(dataModelBaseVO == null || dataModelCacheVO == null) {
				throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
	                    "DataModel is not exists. datasetId=" + datasetId + ", dataModel id=" + datasetBaseVO.getDataModelId());
			}

			if(datasetFlowBaseVO.getEnabled()) {
				// 4. ??????????????? DDL ?????? ????????? ??????
				if(useBigDataStorage(datasetFlowBaseVO.getBigDataStorageTypes())) {

					try {
						createBigdataTable(dataModelCacheVO.getDataModelVO(), datasetFlowBaseVO.getBigDataStorageTypes());
					} catch (Exception e) {
						throw new InternalServerErrorException(ErrorCode.CREATE_ENTITY_TABLE_ERROR,
			                    "Create Bigdata Table error. datasetId=" + datasetId + ", dataModel=" + dataModelBaseVO.getDataModel());
					}
				}
				
				if(useRdbStorage(datasetFlowBaseVO.getBigDataStorageTypes())) {
					String ddl = rdbDataModelSqlProvider.generateCreateTableDdl(dataModelCacheVO);
					dataModelSVC.executeDdl(ddl, StorageType.RDB);
				}

				// ???????????? ????????? ?????? ?????? ????????? ?????? ?????? default RDB ??????
				if(isNullStorageType(datasetFlowBaseVO.getBigDataStorageTypes())) {
					String ddl = rdbDataModelSqlProvider.generateCreateTableDdl(dataModelCacheVO);
					dataModelSVC.executeDdl(ddl, StorageType.RDB);
				}
			}
	
	    	// 5. ???????????? ?????? ?????? ??????
			datasetFlowBaseVO.setProvisioningRequestId(requestId);
			datasetFlowBaseVO.setProvisioningEventTime(eventTime);
	        datasetFlowDAO.createDatasetFlow(datasetFlowBaseVO);

	        // 6. ?????????????????? ?????? Storage??? ???????????? ?????? ??? ??????
			updateDataModelStorageType(datasetFlowBaseVO, dataModelBaseVO);

			// 7. ?????? ??????
	        dataModelManager.putDataModelCache(dataModelBaseVO);
	        dataModelManager.putDatasetFlowCache(datasetFlowBaseVO);
	
		    if(datasetFlowBaseVO.getEnabled()) {
		        // 8. Start Kafka Consumer
	        	kafkaConsumerManager.registKafkaConsumer(datasetId);
	        }

	     // 404
    	} else {
 			throw new BadRequestException(ErrorCode.NOT_EXIST_ID);
 		}
    }

	private void updateDataModelStorageType(DatasetFlowBaseVO datasetFlowBaseVO, DataModelBaseVO dataModelBaseVO) {
		List<BigDataStorageType> oldStorageTypes = dataModelBaseVO.getCreatedStorageTypes();
		List<BigDataStorageType> newStorageTypes = null;
		if(ValidateUtil.isEmptyData(datasetFlowBaseVO.getBigDataStorageTypes())) {
			newStorageTypes = new ArrayList<>(); // default RDB
			newStorageTypes.add(BigDataStorageType.RDB);
		} else {
			newStorageTypes = datasetFlowBaseVO.getBigDataStorageTypes();
		}

		if(oldStorageTypes != null) {
			newStorageTypes.addAll(oldStorageTypes);
			newStorageTypes = newStorageTypes.stream().distinct().collect(Collectors.toList());
		}

		dataModelBaseVO.setCreatedStorageTypes(newStorageTypes);
		dataModelSVC.updateDataModelStorage(dataModelBaseVO);
	}


	/**
     * HBase ??? Hive ????????? ??????
     * @param dataModelVO ??????????????? ??????
     * @param bigDataStorageTypes ???????????? ?????? ?????? ?????????
     * @throws Exception
     */
    private void createBigdataTable(DataModelVO dataModelVO, List<BigDataStorageType> bigDataStorageTypes) throws Exception {
    	String ddl = hiveDataModelSqlProvider.generateCreateTableDdl(dataModelVO);
		String[] sqls = ddl.split(" CREATEHIVETABLE ");
		
		boolean storeInHbase = false;
		boolean bothStore = false;

		if (bigDataStorageTypes != null && !bigDataStorageTypes.isEmpty()) {
			if (bigDataStorageTypes.contains(BigDataStorageType.HBASE)) {
				storeInHbase = true;

				if (bigDataStorageTypes.contains(BigDataStorageType.HIVE)) {
					bothStore = true;
				}
			}
		}
		
		log.debug("Is this flow Stored in HBase? " + (storeInHbase? "yes": "no"));

		for (String sql : sqls) {
			String fullSql = hiveTableSVC.getFullDdl(sql, storeInHbase,"");
			hiveTableSVC.createTable(fullSql);
		}

		if (bothStore) {
			storeInHbase = false;
			for (String sql : sqls) {
				String fullSql = hiveTableSVC.getFullDdl(sql, storeInHbase,"");
				hiveTableSVC.createTable(fullSql);
			}
		}    	
    }
    
    /**
     * ????????? ??? ?????? ??????
     * @param to ????????? ??? ?????? ?????? ?????? ?????? url
     * @param requestBody ?????? Body
     * @param requestId Provisioning Request Id
     * @param eventTime Provisioning Request Time
     */
	public void updateDatasetFlow(String to, String requestBody, String requestId, Date eventTime) {

		// 1. Request URI?????? ????????? ??????
		Matcher matcherForUpdate = URI_PATTERN_DATASET_FLOW.matcher(to);

    	if(matcherForUpdate.find()) {
			String datasetId = matcherForUpdate.group("datasetId");

			// 2. ?????? ????????? ??????
			DatasetFlowProvisioningVO datasetFlowProvisioningVO = null;
			try {
				datasetFlowProvisioningVO = objectMapper.readValue(requestBody, DatasetFlowProvisioningVO.class);
			} catch (IOException e) {
				throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
	                    "Invalid Parameter. body=" + requestBody);
			}
			datasetFlowProvisioningVO.setDatasetId(datasetId);

			// 3. ????????? ??? ?????? ??? ???????????? ??????
			DatasetFlowBaseVO retrieveDatasetFlowBaseVO = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(datasetId);
			if(retrieveDatasetFlowBaseVO == null) {
        		createDatasetFlow(to, requestBody, requestId, eventTime);
        		return;
			}

			// 4. ??????????????? ?????? ?????? IngestInterface ?????????????????? DB ?????? ????????? ??????
			if(alreadyProcessByOtherInstance(requestId, eventTime, retrieveDatasetFlowBaseVO)) {
	        	// ?????? Instance?????? DB ???????????? ?????? ?????????????????? ????????? ???????????? ?????? ??????
				DatasetBaseVO datasetBaseVO = dataModelManager.getDatasetCache(retrieveDatasetFlowBaseVO.getDatasetId());
				dataModelManager.reloadDataModelCache(datasetBaseVO.getDataModelId());
				dataModelManager.putDatasetFlowCache(retrieveDatasetFlowBaseVO);
	            return;
	        }

			// 5. DatasetFlow ????????????
			DatasetFlowBaseVO datasetFlowBaseVO = datasetFlowVOToDatasetFlowBaseVO(datasetFlowProvisioningVO);
			datasetFlowBaseVO.setProvisioningRequestId(requestId);
			datasetFlowBaseVO.setProvisioningEventTime(eventTime);

	        int result = datasetFlowDAO.updateDatasetFlow(datasetFlowBaseVO);

	        if(result == 0) {
	        	throw new BadRequestException(ErrorCode.NOT_EXIST_ID,
	                    "Not Exists. datasetId=" + datasetId);
	        }

			// 6. ?????????????????? ?????? Storage??? ???????????? ?????? ??? ??????
			DatasetBaseVO datasetBaseVO = dataModelManager.getDatasetCache(retrieveDatasetFlowBaseVO.getDatasetId());
			DataModelBaseVO dataModelBaseVO = dataModelRetrieveSVC.getDataModelBaseVOById(datasetBaseVO.getDataModelId());
			updateDataModelStorageType(datasetFlowBaseVO, dataModelBaseVO);

	        // 7. ????????????
	        dataModelManager.putDataModelCache(dataModelBaseVO);
	        dataModelManager.putDatasetFlowCache(datasetFlowBaseVO);

	        if(datasetFlowBaseVO.getEnabled()) {
	        	if(!kafkaConsumerManager.isRunKafkaConsumer(datasetId)) {
	        		kafkaConsumerManager.registKafkaConsumer(datasetFlowBaseVO.getDatasetId());
	        	}
	        } else if(!datasetFlowBaseVO.getEnabled()){
	        	kafkaConsumerManager.deregistKafkaConsumer(datasetId);
	        }

	    // 404
    	} else {
 			throw new BadRequestException(ErrorCode.NOT_EXIST_ID);
 		}

    }

	/**
	 * ????????? ??? ?????? ??????
	 * @param to ????????? ??? ?????? ?????? ?????? ?????? url
     * @param requestBody ?????? Body
	 */
    public void deleteDatasetFlow(String to, String requestBody) {

    	Matcher matcherForDelete = URI_PATTERN_DATASET_FLOW.matcher(to);

    	if(matcherForDelete.find()) {
			String datasetId = matcherForDelete.group("datasetId");

			// 1. ???????????? ?????? ??? ????????? ??????
			DatasetFlowBaseVO retrieveDatasetFlowBaseVO = new DatasetFlowBaseVO();
	    	retrieveDatasetFlowBaseVO.setDatasetId(datasetId);
			DatasetFlowBaseVO datasetFlowBaseVO = datasetFlowDAO.getDatasetFlowBaseVOById(retrieveDatasetFlowBaseVO);

	        if (datasetFlowBaseVO != null) {
	        	// 2. DatasetFlow ?????? ??????
		        datasetFlowDAO.deleteDatasetFlow(datasetFlowBaseVO);
	        }

	        // 3. ????????????
	        dataModelManager.removeDatasetFlowCache(datasetId);

	        // 4. Stop Kafka Consumer
	        kafkaConsumerManager.deregistKafkaConsumer(datasetId);

	     // 404
 		} else {
 			throw new BadRequestException(ErrorCode.NOT_EXIST_ID);
 		}
    }


    

    /**
     * DatasetFlow API ?????? ??????????????? DB?????? VO ??? ??????
     * @param datasetFlowVO
     * @return
     */
    private DatasetFlowBaseVO datasetFlowVOToDatasetFlowBaseVO(DatasetFlowProvisioningVO datasetFlowProvisioningVO) {
    	
    	DatasetFlowBaseVO datasetFlowBaseVO = new DatasetFlowBaseVO();
    	datasetFlowBaseVO.setDatasetId(datasetFlowProvisioningVO.getDatasetId());
    	datasetFlowBaseVO.setDescription(datasetFlowProvisioningVO.getDescription());
    	datasetFlowBaseVO.setHistoryStoreType(datasetFlowProvisioningVO.getHistoryStoreType());
    	datasetFlowBaseVO.setBigDataStorageTypes(datasetFlowProvisioningVO.getBigDataStorageTypes());
    	datasetFlowBaseVO.setEnabled(datasetFlowProvisioningVO.getEnabled());

		return datasetFlowBaseVO;
	}


    /**
     * ????????? ?????? ?????? ???????????? ????????? ??? ?????? ?????? ??????
     * @param namespace ??????????????? namespace
     * @param type ??????????????? type
     * @param version ??????????????? version
     * @return
     */
    public List<DatasetFlowBaseVO> getDatasetFlowByDataModel(String dataModelId) {
    	RetrieveDatasetFlowBaseVO retrieveDatasetFlowBaseVO = new RetrieveDatasetFlowBaseVO();
    	retrieveDatasetFlowBaseVO.setDataModelId(dataModelId);
    	return datasetFlowDAO.getDatasetFlowBaseVOByDataModel(retrieveDatasetFlowBaseVO);
    }

    /**
     * ???????????? ?????? ?????? ??????
     * @param bigDataStorageTypeList
     * @return
     */
    private boolean useBigDataStorage(List<BigDataStorageType> bigDataStorageTypeList) {
    	if(bigDataStorageTypeList == null || bigDataStorageTypeList.size() == 0) {
    		return false;
    	}

    	for(BigDataStorageType bigDataStorageType : bigDataStorageTypeList) {
    		if(bigDataStorageType == BigDataStorageType.HIVE
    				|| bigDataStorageType == BigDataStorageType.HBASE) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * RDB ?????? ?????? ??????
     * @param bigDataStorageTypeList
     * @return
     */
    private boolean useRdbStorage(List<BigDataStorageType> bigDataStorageTypeList) {
    	if(bigDataStorageTypeList == null || bigDataStorageTypeList.size() == 0) {
    		return false;
    	}

    	for(BigDataStorageType bigDataStorageType : bigDataStorageTypeList) {
    		if(bigDataStorageType == BigDataStorageType.RDB) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * RDB ?????? ?????? ??????
     * @param bigDataStorageTypeList
     * @return
     */
    private boolean isNullStorageType(List<BigDataStorageType> bigDataStorageTypeList) {
    	if(bigDataStorageTypeList == null || bigDataStorageTypeList.size() == 0) {
    		return true;
    	}

    	return false;
    }

    /**
     * ??????????????? ?????? ?????? IngestInterface ?????????????????? DB ?????? ????????? ??????
     * @param requestId Provisioning Request Id
     * @param eventTime Provisioning Event Time
     * @param retrieveDatasetFlowBaseVO DB?????? ????????? ????????? ??? ?????? ??????
     * @return
     */
    private boolean alreadyProcessByOtherInstance(String requestId, Date eventTime, DatasetFlowBaseVO retrieveDatasetFlowBaseVO) {
		// ??????????????? ?????? ?????? IngestInterface ?????????????????? DB ?????? ????????? ??????
    	if(requestId.equals(retrieveDatasetFlowBaseVO.getProvisioningRequestId())
    			&& eventTime.getTime() >= retrieveDatasetFlowBaseVO.getProvisioningEventTime().getTime()) {
    		return true;
    	}
    	return false;
	}
}

