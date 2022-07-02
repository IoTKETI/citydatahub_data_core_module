package kr.re.keti.sc.dataservicebroker.entities.datalifecycle;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetRetrieveSVC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetSVC;
import kr.re.keti.sc.dataservicebroker.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.dataservicebroker.entities.datalifecycle.dao.DataLifeCyleDAO;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataLifeCycleProcessor {

    @Autowired
    private DatasetRetrieveSVC datasetRetrieveSVC;
    @Autowired
    private DataLifeCyleDAO dataLifeCyleDAO;
    @Autowired
	private DataModelManager dataModelManager;


    @Scheduled(cron = "${datacore.data.life.cyle.cron}")
    public void excute() {


        List<DatasetBaseVO> datasetBaseVOList = datasetRetrieveSVC.getDatasetVOList();

        for (DatasetBaseVO datasetBaseVO : datasetBaseVOList) {

            String dataModelId = datasetBaseVO.getDataModelId();
            String datasetId = datasetBaseVO.getId();
            Integer storageRetention = datasetBaseVO.getStorageRetention();

            if (storageRetention == null || storageRetention == 0) {

                log.info("storageRetention(" + storageRetention + ") value is invald ");
                continue;
            }

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

            Date lifeCyleDate = Date.from(storageRetentionDayAgo.atZone(ZoneId.systemDefault()).toInstant());

            dataLifeCyleDAO.deleteEntity(tableName, datasetId, lifeCyleDate);
            dataLifeCyleDAO.deleteEntity(partialHistTableName, datasetId, lifeCyleDate);
            dataLifeCyleDAO.deleteEntity(fullHistTableName, datasetId, lifeCyleDate);

        }
    }
}
