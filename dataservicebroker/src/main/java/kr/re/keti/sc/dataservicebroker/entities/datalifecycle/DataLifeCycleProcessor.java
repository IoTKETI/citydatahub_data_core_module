package kr.re.keti.sc.dataservicebroker.entities.datalifecycle;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.dataset.vo.DatasetBaseVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.datasetflow.service.DatasetFlowRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.DatasetFlowBaseVO;

import kr.re.keti.sc.dataservicebroker.entities.datalifecycle.dao.DataLifeCycleDAO;
import kr.re.keti.sc.dataservicebroker.entities.datalifecycle.dao.hive.HiveTableDatalifeDAO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@EnableScheduling
public class DataLifeCycleProcessor {

    @Autowired
    private DatasetRetrieveSVC datasetRetrieveSVC;
    @Autowired
    private DataLifeCycleDAO dataLifeCycleDAO;
    @Autowired
	private DataModelManager dataModelManager;
    @Autowired
    private DatasetFlowRetrieveSVC datasetFlowRetrieveSVC;
    @Autowired(required = false)
    private HiveTableDatalifeDAO hiveTableDAO;


    @Scheduled(cron = "${datacore.data.life.cycle.cron}")
    public void excute() {
        List<DatasetFlowBaseVO> datasetFlowBaseVOList = datasetFlowRetrieveSVC.getDatasetFlowBaseVOList();
        for (DatasetFlowBaseVO datasetFlowBaseVO : datasetFlowBaseVOList) {
            String datasetId = datasetFlowBaseVO.getDatasetId();
            List<BigDataStorageType> storageTypes= datasetFlowBaseVO.getBigDataStorageTypes();
            

            DatasetBaseVO datasetBaseVO = datasetRetrieveSVC.getDatasetVOById(datasetId);
            String dataModelId = datasetBaseVO.getDataModelId();
            Integer storageRetention = datasetBaseVO.getStorageRetention();

            if (storageRetention == null || storageRetention == 0) {
                log.info("storageRetention(" + storageRetention + ") value is invald ");
            }else{
                for(BigDataStorageType storageType : storageTypes){
                    if(storageType == BigDataStorageType.RDB) {
                        rdb(dataModelId, storageRetention, datasetId);
                    }
                    if(storageType == BigDataStorageType.HIVE || storageType == BigDataStorageType.HBASE) {
                        hive(dataModelId, storageRetention, datasetId);
                    }
                }
            }


        }
    }

    public void rdb(String dataModelId, Integer storageRetention, String datasetId){
            StringBuilder tableNameBuilder = new StringBuilder();

            tableNameBuilder
                    .append(Constants.SCHEMA_NAME)
                    .append(".")
                    .append("\"")
                    .append(dataModelManager.generateRdbTableName(dataModelId));

            String tableName = tableNameBuilder.toString() + "\"";
            String fullHistTableName = tableNameBuilder.toString() + Constants.PARTIAL_HIST_TABLE_PREFIX + "\"";
            String partialHistTableName = tableNameBuilder.toString() + Constants.FULL_HIST_TABLE_PREFIX + "\"";


            LocalDateTime now = LocalDateTime.now(); // 현재시간
            LocalDateTime storageRetentionDayAgo = now.minusDays(storageRetention);


            Date lifeCycleDate = Date.from(storageRetentionDayAgo.atZone(ZoneId.systemDefault()).toInstant());
            
            dataLifeCycleDAO.deleteEntity(tableName, datasetId, lifeCycleDate);
            dataLifeCycleDAO.deleteEntity(partialHistTableName, datasetId, lifeCycleDate);
            dataLifeCycleDAO.deleteEntity(fullHistTableName, datasetId, lifeCycleDate);   

    }

    public void hive(String dataModelId, Integer storageRetention, String datasetId){
        StringBuilder tableNameBuilder = new StringBuilder();

        tableNameBuilder
                .append(dataModelManager.generateHiveTableName(dataModelId));

        String tableName = tableNameBuilder.toString();
        String fullHistTableName = tableNameBuilder.toString() + "fullhist";
        String partialHistTableName = tableNameBuilder.toString() + "partialhist";


        LocalDateTime now = LocalDateTime.now(); // 현재시간
        LocalDateTime storageRetentionDayAgo = now.minusDays(storageRetention);


        Date lifeCycleDate = Date.from(storageRetentionDayAgo.atZone(ZoneId.systemDefault()).toInstant());

        hiveTableDAO.deleteEntity(tableName, datasetId, lifeCycleDate);
        hiveTableDAO.deleteEntity(partialHistTableName, datasetId, lifeCycleDate);
        hiveTableDAO.deleteEntity(fullHistTableName, datasetId, lifeCycleDate);
    }
}
