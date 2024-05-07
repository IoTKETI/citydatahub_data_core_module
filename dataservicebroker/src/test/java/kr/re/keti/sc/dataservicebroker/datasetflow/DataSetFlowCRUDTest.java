package kr.re.keti.sc.dataservicebroker.datasetflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.DataServiceBrokerApplication;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelSVC;

import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetSVC;

import kr.re.keti.sc.dataservicebroker.datasetflow.service.DatasetFlowSVC;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.DatasetFlowBaseVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.service.DatasetFlowRetrieveSVC;

@SpringBootTest
@ContextConfiguration(classes = DataServiceBrokerApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class DataSetFlowCRUDTest {

    @Value("${entity.default.storage}")
    private String datastorage;

    @Autowired
    private DataModelSVC dataModelSVC;

    @Autowired
    private DatasetSVC datasetSVC;

    @Autowired
    private DatasetFlowSVC datasetFlowSVC;

    @Autowired
    private DatasetFlowRetrieveSVC datasetFlowRetrieveSVC;

    private ObjectMapper om = new ObjectMapper();

    // Create------------------------------

    @Test
    @DisplayName("데이터 셋 플로우 생성 테스트")
    public void SetFlowTest_001_Create_DataSetFlow() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
                TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        String inputData_datasetFlow = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json"))), TEST_DATASET_ID);
                
        try {
            // When: 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
            
            // 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset, requestId, now);

            // 데이터 셋 플로우 생성
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", inputData_datasetFlow, requestId, now);

        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("데이터 모델이 없을 때 데이터 셋 플로우 생성 테스트")
    public void SetFlowTest_002_Create_DataSetFlow_WithoutDataModel() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataset = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
                TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        String inputData_datasetFlow = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json"))), TEST_DATASET_ID);
                
        try {
            // When: 데이터 모델 생성
            // 안함
            
            // 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset, requestId, now);

            // 데이터 셋 플로우 생성
            Assertions.assertThrows(NullPointerException.class, () -> {
                datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", inputData_datasetFlow, requestId, now);
            }, "NullPointerException was expected");

        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    @DisplayName("데이터 셋이 없을 때 데이터 셋 플로우 생성 테스트")
    public void SetFlowTest_003_Create_DataSetFlow_WithoutDataSet() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_datasetFlow = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json"))),
                TEST_DATASET_ID);

        try {
            // When: 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

            // 데이터 셋 생성
            // 안함

            // 데이터 셋 플로우 생성
            Assertions.assertThrows(BadRequestException.class, () -> {
                datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", inputData_datasetFlow, requestId, now);
            }, "BadRequestException was expected");

        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    @DisplayName("데이터 모델과 데이터 셋이 없을 때 데이터 셋 플로우 생성 테스트")
    public void SetFlowTest_004_Create_DataSetFlow_WithoutDataModelAndDataSet() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();
        
        String inputData_datasetFlow = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json"))), TEST_DATASET_ID);
                
        try {
            // When: 데이터 모델 생성
            // 안함
            
            // 데이터 셋 생성
            // 안함

            // 데이터 셋 플로우 생성
            Assertions.assertThrows(BadRequestException.class, () -> {
                datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", inputData_datasetFlow, requestId, now);
            }, "BadRequestException was expected");

        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    @DisplayName("동일한 ID로 데이터 셋 플로우를 중복 생성하는 테스트")
    public void SetFlowTest_005_Create_DuplicateDataSetFlow_WithSameId() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
                TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        String inputData_datasetFlow = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json"))), TEST_DATASET_ID);
                
        try {
            // When: 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
            
            // 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset, requestId, now);

            // 데이터 셋 플로우 생성
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", inputData_datasetFlow, requestId, now);
            
            // 데이터 셋 중복 생성 시도 및 예외 검증 -> 업데이트 처리
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", inputData_datasetFlow, requestId, now);

        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("다른 ID를 갖는 두 개의 데이터 셋 플로우 생성 테스트")
    public void SetFlowTest_006_Create_TwoDataSetFlows_WithDifferentIds() throws Exception {
        // Given: 데이터 모델 준비
        Date now = new Date();
        
        final String requestId = UUID.randomUUID().toString();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        String inputData_dataModel = String.format(
            new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);
            
        // 1번 데이터 셋 플로우 준비
        final String requestId_1 = UUID.randomUUID().toString() + "1";
        final String TEST_DATASET_ID_1 = UUID.randomUUID().toString();

        String inputData_dataset_1 = String.format(
            new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
                TEST_DATASET_ID_1, TEST_DATASET_ID_1, TEST_DATAMODEL_ID);

        String inputData_datasetFlow_1 = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json"))), TEST_DATASET_ID_1);
            
        // 2번 데이터 셋 플로우 준비
        final String requestId_2 = UUID.randomUUID().toString() + "2";
        final String TEST_DATASET_ID_2 = UUID.randomUUID().toString();

        String inputData_dataset_2 = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
                TEST_DATASET_ID_2, TEST_DATASET_ID_2, TEST_DATAMODEL_ID);

        String inputData_datasetFlow_2 = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json"))), TEST_DATASET_ID_2);


        try {
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
            // 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset_1, requestId_1, now);
            datasetSVC.createDataset(inputData_dataset_2, requestId_2, now);
            // When: 서로다른 ID를 갖는 데이터 셋 플로우를 생성
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID_1 + "/flow", inputData_datasetFlow_1, requestId, now);
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID_2 + "/flow", inputData_datasetFlow_2, requestId, now);
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리: 두개의 데이터 셋, 데이터 모델 삭제
            datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID_1 + "/flow", "");
            datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID_2 + "/flow", "");
            datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID_1, "");
            datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID_2, "");
            dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
        }
    }    

    @Test
    @DisplayName("올바르지 않은 형식(historyStoreType 없는)의 데이터 셋 플로우 생성 테스트")
    public void SetFlowTest_007_Create_DataSetFlow_WithoutHistoryStoreType() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
        TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        String inputData_datasetFlow_Str =  new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json")));

        Map<String, Object> inputData_datasetFlow_Map = om.readValue(inputData_datasetFlow_Str,
                new TypeReference<Map<String, Object>>() {
                });
        
        // 데이터 셋 플로우맵에서 historyStoreType 제거
        inputData_datasetFlow_Map.remove("historyStoreType");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
                datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            }, "DataIntegrityViolationException was expected");
        } catch (DataIntegrityViolationException e) {
            System.out.printf("DataIntegrityViolationException occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(enabled 없는)의 데이터 셋 플로우 생성 테스트")
    public void SetFlowTest_008_Create_DataSetFlow_WithoutEnabled() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
        TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        String inputData_datasetFlow_Str =  new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json")));

        Map<String, Object> inputData_datasetFlow_Map = om.readValue(inputData_datasetFlow_Str,
                new TypeReference<Map<String, Object>>() {
                });
        
        // 데이터 셋 플로우맵에서 enabled 제거
        inputData_datasetFlow_Map.remove("enabled");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(NullPointerException.class, () -> {
                datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            }, "NullPointerException was expected");
        } catch (NullPointerException e) {
            System.out.printf("NullPointerException occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(bigDataStorageTypes 없는)의 데이터 셋 플로우 생성 테스트")
    public void SetFlowTest_009_Create_DataSetFlow_WithoutBigDataStorageTypes() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
        TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        String inputData_datasetFlow_Str =  new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json")));

        Map<String, Object> inputData_datasetFlow_Map = om.readValue(inputData_datasetFlow_Str,
                new TypeReference<Map<String, Object>>() {
                });
        
        // 데이터 셋 플로우맵에서 bigDataStorageTypes 제거
        inputData_datasetFlow_Map.remove("bigDataStorageTypes");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(NullPointerException.class, () -> {
                datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            }, "NullPointerException was expected");
        } catch (NullPointerException e) {
            System.out.printf("NullPointerException occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(비어 있는)의 데이터 셋 생성 테스트")
    public void SetFlowTest_010_Create_DataSetFlow_Null() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
        TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        // When: 비어있는 datasetFlow
        String inputData_datasetFlow = "";

        try {
            Assertions.assertThrows(BadRequestException.class, () -> {
                datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", inputData_datasetFlow, requestId, now);
            }, "BadRequestException was expected");
        } catch (BadRequestException e) {
            System.out.printf("BadRequestException occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    // Read------------------------------

    @Test
    @DisplayName("존재하는 데이터 셋 플로우 조회 테스트")
    public void SetFlowTest_011_Retrieve_ExistingDataSetFlow() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
                TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        String inputData_datasetFlow = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json"))), TEST_DATASET_ID);
                
        try {
            // 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
            
            // 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset, requestId, now);

            // 데이터 셋 플로우 생성
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", inputData_datasetFlow, requestId, now);
            
            // When: 데이터 셋 플로우 조회
            DatasetFlowBaseVO datasetFlowBaseVO = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(TEST_DATASET_ID);
            assertNotNull(datasetFlowBaseVO);

        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("데이터 모델을 안 만들었을 때, 데이터 셋 플로우 조회 테스트")
    public void SetFlowTest_012_Retrieve_DataSetFlow_WithoutDataModel() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataset = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
                TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        String inputData_datasetFlow = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json"))), TEST_DATASET_ID);
                
        try {
            // 데이터 모델 생성
            // 안함
            
            // 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset, requestId, now);

            // 데이터 셋 플로우 생성
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", inputData_datasetFlow, requestId, now);
            
            // When: 데이터 셋 플로우 조회 및 검증
            DatasetFlowBaseVO datasetFlowBaseVO = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(TEST_DATASET_ID);
            assertNull(datasetFlowBaseVO);
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("데이터 셋을 안 만들었을 때, 데이터 셋 플로우 조회 테스트")
    public void SetFlowTest_013_Retrieve_DataSetFlow_WithoutDataSet() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_datasetFlow = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json"))),
                TEST_DATASET_ID);

        try {
            // 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

            // 데이터 셋 생성
            // 안함

            // 데이터 셋 플로우 생성
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", inputData_datasetFlow, requestId, now);

            // When: 데이터 셋 플로우 조회 및 검증
            DatasetFlowBaseVO datasetFlowBaseVO = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(TEST_DATASET_ID);
            assertNull(datasetFlowBaseVO);
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("데이터 셋 플로우을 안 만들었을 때, 데이터 셋 플로우 조회 테스트")
    public void SetFlowTest_014_Retrieve_DataSetFlow_WithoutDatasetFlow() throws Exception {
    
        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
                TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
                
        try {
            // 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
            
            // 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset, requestId, now);

            // 데이터 셋 플로우 생성
            // 안함
            
            // When: 데이터 셋 플로우 조회 및 검증
            DatasetFlowBaseVO datasetFlowBaseVO = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(TEST_DATASET_ID);
            assertNull(datasetFlowBaseVO);
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("데이터 셋 플로우 생성 후, 올바르지 않은 Data Set ID값의 데이터 셋 플로우 조회 테스트")
    public void SetFlowTest_015_Retrieve_WithInvalidDataSetFlowId_AfterCreation() throws Exception {
    
        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();
    
        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
                TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        String inputData_datasetFlow = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json"))), TEST_DATASET_ID);

        try {
            // 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

            // 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset, requestId, now);

            // 데이터 셋 플로우 생성
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", inputData_datasetFlow, requestId, now);
    
            // When: 잘못된 ID 값으로 데이터 모델 조회 및 검증
            assertNull(datasetFlowRetrieveSVC.getDatasetFlowBaseVOById("올바르지않아요"), "잘못된 ID로 조회한 결과는 null이어야 함");
            assertNull(datasetFlowRetrieveSVC.getDatasetFlowBaseVOById("#"), "잘못된 ID로 조회한 결과는 null이어야 함");
            assertNull(datasetFlowRetrieveSVC.getDatasetFlowBaseVOById("?"), "잘못된 ID로 조회한 결과는 null이어야 함");
            assertNull(datasetFlowRetrieveSVC.getDatasetFlowBaseVOById("~"), "잘못된 ID로 조회한 결과는 null이어야 함");
    
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    // Update------------------------------

    // 이슈, historyStoreType을 all의 올바른 업데이트 파라미터는?
    @Test
    @DisplayName("존재하는 데이터 셋 플로우 historyStoreType 업데이트 테스트")
    public void SetFlowTest_016_Update_historyStoreType_ExistingDataSetFlow() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
        TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        String inputData_datasetFlow_Str =  new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json")));

        Map<String, Object> inputData_datasetFlow_Map = om.readValue(inputData_datasetFlow_Str,
                new TypeReference<Map<String, Object>>() {
                });

        try {
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow",
                    om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            
            // When: 데이터 셋 플로우 업데이트
            String updatedHistoryStoreType = "hello";
            inputData_datasetFlow_Map.put("hello", updatedHistoryStoreType);

            datasetFlowSVC.updateDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow",
                    om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            
            DatasetFlowBaseVO updatedDataSetFlow = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(TEST_DATASET_ID);
            assertEquals("hello", updatedDataSetFlow.getHistoryStoreType(),
                    "The historyStoreType of the data set flow should be updated to 'hello'");       

        } catch (DataIntegrityViolationException e) {
            System.out.printf("DataIntegrityViolationException occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    // 이슈: 업데이트 안됨
    @Test
    @DisplayName("존재하는 데이터 셋 enabled 업데이트 테스트")
    public void SetFlowTest_017_Update_enabled_ExistingDataSetFlow() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
        TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        String inputData_datasetFlow_Str =  new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json")));

        Map<String, Object> inputData_datasetFlow_Map = om.readValue(inputData_datasetFlow_Str,
                new TypeReference<Map<String, Object>>() {
                });

        try {
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow",
                    om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            
            // When: 데이터 셋 플로우 업데이트
            Boolean updatedEnabled = false;
            inputData_datasetFlow_Map.put("enabled", updatedEnabled);

            datasetFlowSVC.updateDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow",
                    om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            
            System.out.println("QWER"+om.writeValueAsString(inputData_datasetFlow_Map));
            
            DatasetFlowBaseVO updatedDataSetFlow = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(TEST_DATASET_ID);
            
            System.out.println("ASDF"+updatedDataSetFlow.getEnabled());
            
            assertEquals(false, updatedDataSetFlow.getEnabled(),
                    "The enabled of the data set flow should be updated to 'false'");       

        } catch (DataIntegrityViolationException e) {
            System.out.printf("DataIntegrityViolationException occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    // 이슈: 업데이트 안됨
    @Test
    @DisplayName("존재하는 데이터 셋 bigDataStorageTypes 업데이트 테스트")
    public void SetFlowTest_018_Update_bigDataStorageTypes_ExistingDataSetFlow() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
        TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        String inputData_datasetFlow_Str =  new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json")));

        Map<String, Object> inputData_datasetFlow_Map = om.readValue(inputData_datasetFlow_Str,
                new TypeReference<Map<String, Object>>() {
                });

        try {
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow",
                    om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            
            // When: 데이터 셋 플로우 업데이트
            inputData_datasetFlow_Map.put("bigDataStorageTypes", List.of("hbase"));

            datasetFlowSVC.updateDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow",
                    om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            
            System.out.println("QWER"+om.writeValueAsString(inputData_datasetFlow_Map));
            
            DatasetFlowBaseVO updatedDataSetFlow = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(TEST_DATASET_ID);
            
            System.out.println("ASDF"+updatedDataSetFlow.getBigDataStorageTypes());
            
            assertEquals("[HBASE]", updatedDataSetFlow.getBigDataStorageTypes(),
                    "The bigDataStorageTypes of the data set flow should be updated to 'hbase'");       

        } catch (DataIntegrityViolationException e) {
            System.out.printf("DataIntegrityViolationException occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    // 이슈: 업데이트 안됨
    @Test
    @DisplayName("존재하지 않는 데이터 셋 플로우 대상의 업데이트 테스트")
    public void SetFlowTest_019_Update_NonExistingDataSetFlow() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
        TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        String inputData_datasetFlow_Str =  new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json")));

        Map<String, Object> inputData_datasetFlow_Map = om.readValue(inputData_datasetFlow_Str,
                new TypeReference<Map<String, Object>>() {
                });

        try {
            // 데이터 셋 플로우 생성 안함
            
            // When: 데이터 셋 플로우 업데이트
            Boolean updatedEnabled = false;
            inputData_datasetFlow_Map.put("enabled", updatedEnabled);

            datasetFlowSVC.updateDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow",
                    om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            
            System.out.println("QWER"+om.writeValueAsString(inputData_datasetFlow_Map));
            
            DatasetFlowBaseVO updatedDataSetFlow = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(TEST_DATASET_ID);
            
            System.out.println("ASDF"+updatedDataSetFlow.getEnabled());
            
            assertEquals(false, updatedDataSetFlow.getEnabled(),
                    "The enabled of the data set flow should be updated to 'false'");       

        } catch (DataIntegrityViolationException e) {
            System.out.printf("DataIntegrityViolationException occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    // 이슈: 올바르지 않은 요청은 무시되는것인가?
    @Test
    @DisplayName("올바르지 않은 형식(historyStoreType 없는)의 데이터 셋 플로우 업데이트 테스트")
    public void SetFlowTest_020_Update_DataSetFlow_WithoutId() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
        TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        String inputData_datasetFlow_Str =  new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json")));

        Map<String, Object> inputData_datasetFlow_Map = om.readValue(inputData_datasetFlow_Str,
                new TypeReference<Map<String, Object>>() {
                });

        try {
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow",
                    om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            
            // When: 데이터 셋 플로우 에서 historyStoreType 제거
            inputData_datasetFlow_Map.remove("historyStoreType");

            datasetFlowSVC.updateDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow",
                    om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            
            DatasetFlowBaseVO updatedDataSetFlow = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(TEST_DATASET_ID);
            assertNull(updatedDataSetFlow.getHistoryStoreType());       

        } catch (DataIntegrityViolationException e) {
            System.out.printf("DataIntegrityViolationException occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    // 이슈: 올바르지 않은 요청은 무시되는것인가?
    @Test
    @DisplayName("올바르지 않은 형식(enabled 없는)의 데이터 셋 플로우 업데이트 테스트")
    public void SetFlowTest_021_Update_DataSetFlow_WithoutEnabled() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
        TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        String inputData_datasetFlow_Str =  new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json")));

        Map<String, Object> inputData_datasetFlow_Map = om.readValue(inputData_datasetFlow_Str,
                new TypeReference<Map<String, Object>>() {
                });

        try {
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow",
                    om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            
            // When: 데이터 셋 플로우 에서 enabled 제거
            inputData_datasetFlow_Map.remove("enabled");

            datasetFlowSVC.updateDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow",
                    om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            
            DatasetFlowBaseVO updatedDataSetFlow = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(TEST_DATASET_ID);
            assertNull(updatedDataSetFlow.getEnabled());       

        } catch (DataIntegrityViolationException e) {
            System.out.printf("DataIntegrityViolationException occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    // 이슈: 올바르지 않은 요청은 무시되는것인가?
    @Test
    @DisplayName("올바르지 않은 형식(bigDataStorageTypes 없는)의 데이터 셋 플로우 업데이트 테스트")
    public void SetFlowTest_022_Update_DataSetFlow_WithoutBigDataStorageTypes() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
        TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        String inputData_datasetFlow_Str =  new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json")));

        Map<String, Object> inputData_datasetFlow_Map = om.readValue(inputData_datasetFlow_Str,
                new TypeReference<Map<String, Object>>() {
                });

        try {
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow",
                    om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            
            // When: 데이터 셋 플로우 에서 bigDataStorageTypes 제거
            inputData_datasetFlow_Map.remove("bigDataStorageTypes");

            datasetFlowSVC.updateDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow",
                    om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            
            DatasetFlowBaseVO updatedDataSetFlow = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(TEST_DATASET_ID);
            assertNull(updatedDataSetFlow.getBigDataStorageTypes());       

        } catch (DataIntegrityViolationException e) {
            System.out.printf("DataIntegrityViolationException occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(내용 없는)의 데이터 셋 업데이트 테스트")
    public void SetFlowTest_023_Update_DataSetFlow_WithoutAttributes() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
        new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
        TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        String inputData_datasetFlow_Str =  new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json")));

        Map<String, Object> inputData_datasetFlow_Map = om.readValue(inputData_datasetFlow_Str,
                new TypeReference<Map<String, Object>>() {
                });

        try {
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow",
                    om.writeValueAsString(inputData_datasetFlow_Map), requestId, now);
            
            // When
            Assertions.assertThrows(BadRequestException.class, () -> {
                datasetFlowSVC.updateDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "{}", requestId, now);
            }, "BadRequestException was expected");
            
            DatasetFlowBaseVO updatedDataSetFlow = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(TEST_DATASET_ID);
            assertNull(updatedDataSetFlow.getBigDataStorageTypes());       

        } catch (DataIntegrityViolationException e) {
            System.out.printf("DataIntegrityViolationException occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    // Delete---------------------------------

    @Test
    @DisplayName("존재하는 데이터 셋 플로우 삭제 테스트")
    public void SetFlowTest_024_Delete_ExistingDataSetFlow() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
                TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        
        String inputData_datasetFlow = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_datasetFlow.json"))), TEST_DATASET_ID);
                
        try {
            // When: 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
            
            // 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset, requestId, now);

            // 데이터 셋 플로우 생성
            datasetFlowSVC.createDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", inputData_datasetFlow, requestId, now);

            // When: 데이터 셋 플로우 삭제
            datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");

            // 데이터 모델이 삭제되었는지 조회 및 검증
            DatasetFlowBaseVO datasetFlowBaseVO = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(TEST_DATASET_ID);
            assertNull(datasetFlowBaseVO);
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    // 이슈: 포스트맨에서는 존재하지 않는것을 삭제 시 400 Bad Request 뜨는데, 여기서는 아무것도 안 뜸
    @Test
    @DisplayName("존재하지 않는 데이터 셋 플로우 삭제 테스트")
    public void SetFlowTest_025_Delete_NonExistingDataSetFlow() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        String inputData_dataset = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
                TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);
        

        try {
            // When: 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
            
            // 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset, requestId, now);

            // 데이터 셋 플로우 생성
            // 안함

            // When: 데이터 셋 플로우 삭제
            datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");

            // Then: 아무 일도 없었다.
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // 정리 코드 실행
            try {
                datasetFlowSVC.deleteDatasetFlow("/datasets/" + TEST_DATASET_ID + "/flow", "");
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

}