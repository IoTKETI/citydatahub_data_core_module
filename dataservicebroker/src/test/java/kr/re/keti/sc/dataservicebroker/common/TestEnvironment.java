package kr.re.keti.sc.dataservicebroker.common;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelBaseVO;
import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetSVC;
import kr.re.keti.sc.dataservicebroker.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.service.DatasetFlowRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.datasetflow.service.DatasetFlowSVC;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.DatasetFlowBaseVO;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TestEnvironment {
    private final DataModelSVC dataModelSVC;
    private final DatasetSVC datasetSVC;
    private final DatasetFlowSVC datasetFlowSVC;

    private final DataModelRetrieveSVC dataModelRetrieveSVC;
    private final DatasetRetrieveSVC datasetRetrieveSVC;
    private final DatasetFlowRetrieveSVC datasetFlowRetrieveSVC;

    public void setup(String datamodelId, String datasetId) throws IOException {
        String requestId = UUID.randomUUID().toString();
        Date now = new Date();

        String inputData_dataModel = String.format(new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))), datamodelId);

        String inputData_dataset = String.format(new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))), datasetId, datasetId, datamodelId);

        String inputData_datasetFlow = String.format(new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json"))), datasetId);

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        // 데이터 모델 조회
        DataModelBaseVO dataModelBaseVO = dataModelRetrieveSVC.getDataModelBaseVOById(datamodelId);
        if (dataModelBaseVO == null) {
            throw new BadRequestException(ErrorCode.NOT_EXIST_ID, "Not Exists. dataModelId =" + datamodelId);
        }

        // 데이터 셋 생성
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        // 데이터 셋 조회
        DatasetBaseVO datasetBaseVO = datasetRetrieveSVC.getDatasetVOById(datasetId);
        if (datasetBaseVO == null) {
            throw new BadRequestException(ErrorCode.NOT_EXIST_ID, "Not Exists. datasetId =" + datasetId);
        }

        // 데이터 셋 플로우 생성
        datasetFlowSVC.createDatasetFlow("/datasets/" + datasetId + "/flow", inputData_datasetFlow, requestId, now);

        // 데이터 셋 플로우 조회
        DatasetFlowBaseVO datasetFlowBaseVO = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(datasetId);
        if (datasetFlowBaseVO == null) {
            throw new BadRequestException(ErrorCode.NOT_EXIST_ID, "Not Exists data set flow. datasetId = " + datasetId);
        }
    }

    public void cleanup(String datamodelId, String datasetId) {
        String requestId = UUID.randomUUID().toString();
        Date now = new Date();

        // 데이터 셋 플로우 삭제
        datasetFlowSVC.deleteDatasetFlow("/datasets/" + datasetId + "/flow", "");

        // 데이터 셋 삭제
        datasetSVC.deleteDataset("/datasets/" + datasetId, "");

        // 데이터 모델 삭제
        dataModelSVC.processDelete("/datamodels/" + datamodelId, datamodelId, requestId, now);
    }
}