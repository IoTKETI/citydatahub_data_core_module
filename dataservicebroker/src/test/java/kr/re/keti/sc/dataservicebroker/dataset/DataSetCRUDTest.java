package kr.re.keti.sc.dataservicebroker.dataset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.DataServiceBrokerApplication;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelSVC;
import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetSVC;
import kr.re.keti.sc.dataservicebroker.dataset.vo.DatasetBaseVO;

@SpringBootTest
@ContextConfiguration(classes = DataServiceBrokerApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class DataSetCRUDTest {

    @Value("${entity.default.storage}")
    private String datastorage;

    @Autowired
    private DataModelSVC dataModelSVC;

    @Autowired
    private DatasetSVC datasetSVC;

    @Autowired
    private DatasetRetrieveSVC datasetRetrieveSVC;

    private ObjectMapper om = new ObjectMapper();

    // Create------------------------------

    @Test
    @DisplayName("데이터 셋 생성 테스트")
    public void SetTest_001_Create_DataSet() throws Exception {

        // Given: 테스트 데이터 준비
        String requestId = UUID.randomUUID().toString();
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

        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리: 테스트 종료 후 데이터 셋 삭제, 데이터 모델 삭제
            datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
            dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
        }
    }

    // 이슈 정리 필요 => 기대값: Bad Request / 결과값: 에러 안 발생(Why? : 데이터 모델 존재하는지 체크 안함)
    @Test
    @DisplayName("데이터 모델이 없을 때 데이터 셋 생성 테스트")
    public void SetTest_002_Create_DataSet_WithoutDataModel() throws Exception {

        // Given: 테스트 데이터 준비
        String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataset = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
                TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);

        // When: 데이터 모델 생성
        // 하지 않음

        try {
            // 데이터 셋 생성
            Assertions.assertThrows(BadRequestException.class, () -> {
                datasetSVC.createDataset(inputData_dataset, requestId, now);
            }, "BadRequestException was expected");
            
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리: 테스트 종료 후 데이터 셋 삭제
            datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
        }
    }
    
    // 노션, 데이터모델 테스트 이슈 1번 참조, 20231214기분 중복 생성시 업데이트 처리 됨
    @Test
    @DisplayName("동일한 ID로 데이터 셋을 중복 생성하는 테스트")
    public void SetTest_003_Create_DuplicateDataSet_WithSameId() throws Exception {

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
            // When: 데이터 모델, 셋 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

            datasetSVC.createDataset(inputData_dataset, requestId, now);
            // 데이터 셋 중복 생성 시도 및 예외 검증 -> 업데이트 처리
            // assertThrows(DuplicateKeyException.class, () -> {
                //     datasetSVC.createDataset(inputData_dataset, requestId, now);
                // }, "DuplicateKeyException was expected");
            datasetSVC.createDataset(inputData_dataset, requestId, now);
        } finally {
            // Then: 정리: 테스트 종료 후 데이터 셋 삭제, 데이터 모델 삭제
            datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
            dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
        }
    }

    // 노션, 데이터모델 테스트 이슈 2번 참조
    @Test
    @DisplayName("다른 ID를 갖는 두 개의 데이터 셋 생성 테스트")
    public void SetTest_004_Create_TwoDataSets_WithDifferentIds() throws Exception {
        // Given: 데이터 모델 준비
        Date now = new Date();
        
        final String requestId = UUID.randomUUID().toString();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        String inputData_dataModel = String.format(
            new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);
            
        // 1번 데이터 셋 준비
        final String requestId_1 = UUID.randomUUID().toString() + "1";
        final String TEST_DATASET_ID_1 = UUID.randomUUID().toString();
        String inputData_dataset_1 = String.format(
            new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
            TEST_DATASET_ID_1, TEST_DATASET_ID_1, TEST_DATAMODEL_ID);
            
        // 2번 데이터 셋 준비
        final String requestId_2 = UUID.randomUUID().toString() + "2";
        final String TEST_DATASET_ID_2 = UUID.randomUUID().toString();
        String inputData_dataset_2 = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
                TEST_DATASET_ID_2, TEST_DATASET_ID_2, TEST_DATAMODEL_ID);

        try {
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
            // 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset_1, requestId_1, now);
            // When: 두 번째, 다른 ID를 갖는 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset_2, requestId_2, now);
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리: 두개의 데이터 셋, 데이터 모델 삭제
            datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID_1, "");
            datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID_2, "");
            dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
        }
    }    

    @Test
    @DisplayName("올바르지 않은 형식(id 없는)의 데이터 셋 생성 테스트")
    public void SetTest_005_Create_DataSet_WithoutId() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        String inputData_dataSet_Str =  new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json")));

        Map<String, Object> inputData_dataSet_Map = om.readValue(inputData_dataSet_Str, new TypeReference<Map<String, Object>>() {});
        inputData_dataSet_Map.put("id", String.format((String) inputData_dataSet_Map.get("id"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("name", String.format((String) inputData_dataSet_Map.get("name"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("dataModelId", String.format((String) inputData_dataSet_Map.get("dataModelId"), TEST_DATAMODEL_ID));

        // 데이터 셋 맵에서 id 제거
        inputData_dataSet_Map.remove("id");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
                datasetSVC.createDataset(om.writeValueAsString(inputData_dataSet_Map), requestId, now);
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
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(name 없는)의 데이터 셋 생성 테스트")
    public void SetTest_006_Create_DataSet_WithoutName() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        String inputData_dataSet_Str =  new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json")));

        Map<String, Object> inputData_dataSet_Map = om.readValue(inputData_dataSet_Str, new TypeReference<Map<String, Object>>() {});
        inputData_dataSet_Map.put("id", String.format((String) inputData_dataSet_Map.get("id"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("name", String.format((String) inputData_dataSet_Map.get("name"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("dataModelId", String.format((String) inputData_dataSet_Map.get("dataModelId"), TEST_DATAMODEL_ID));

        // 데이터 셋 맵에서 name 제거
        inputData_dataSet_Map.remove("name");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
                datasetSVC.createDataset(om.writeValueAsString(inputData_dataSet_Map), requestId, now);
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
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(dataModelId 없는)의 데이터 셋 생성 테스트")
    public void SetTest_007_Create_DataSet_WithoutDataModelId() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        String inputData_dataSet_Str =  new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json")));

        Map<String, Object> inputData_dataSet_Map = om.readValue(inputData_dataSet_Str, new TypeReference<Map<String, Object>>() {});
        inputData_dataSet_Map.put("id", String.format((String) inputData_dataSet_Map.get("id"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("name", String.format((String) inputData_dataSet_Map.get("name"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("dataModelId", String.format((String) inputData_dataSet_Map.get("dataModelId"), TEST_DATAMODEL_ID));

        // 데이터 셋 맵에서 dataModelId 제거
        inputData_dataSet_Map.remove("dataModelId");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
                datasetSVC.createDataset(om.writeValueAsString(inputData_dataSet_Map), requestId, now);
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
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(비어 있는)의 데이터 셋 생성 테스트")
    public void SetTest_008_Create_DataSet_Null() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        String inputData_dataSet_Str = "";

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                datasetSVC.createDataset(inputData_dataSet_Str, requestId, now);
            }, "DataIntegrityViolationException was expected");
        } catch (BadRequestException e) {
            System.out.printf("BadRequestException occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    // Read------------------------------

    @Test
    @DisplayName("존재하는 데이터 셋 조회 테스트")
    public void SetTest_009_Retrieve_ExistingDataSet() throws Exception {

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
            
            // When: 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset, requestId, now);

            // 데이터 셋 조회 및 검증
            DatasetBaseVO retrievedDataSet = datasetRetrieveSVC.getDatasetVOById(TEST_DATASET_ID);
            assertNotNull(retrievedDataSet);
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    // 이슈 정리 필요
    @Test
    @DisplayName("데이터 모델을 안 만들었을 때, 데이터 셋 조회 테스트")
    public void SetTest_010_Retrieve_DataSet_WithoutDataModel() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataset = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json"))),
                TEST_DATASET_ID, TEST_DATASET_ID, TEST_DATAMODEL_ID);

        try {
            // 데이터 모델 생성
            // 안함
            
            // When: 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset, requestId, now);

            // 데이터 셋 조회 및 검증
            DatasetBaseVO retrievedDataSet = datasetRetrieveSVC.getDatasetVOById(TEST_DATASET_ID);
            assertNull(retrievedDataSet);
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("데이터 셋을 안 만들었을 때, 데이터 셋 조회 테스트")
    public void SetTest_011_Retrieve_DataSet_WithoutDataset() throws Exception {
    
        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        // When: 데이터 셋을 안 만들었을 때

        try {
            // 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
    
            // 데이터 셋 조회 및 검증
            DatasetBaseVO retrievedDataSet = datasetRetrieveSVC.getDatasetVOById(TEST_DATASET_ID);
            assertNull(retrievedDataSet);
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }  

    @Test
    @DisplayName("데이터 셋 생성 후, 올바르지 않은 Data Set ID값의 데이터 셋 조회 테스트")
    public void SetTest_012_Retrieve_WithInvalidDataSetId_AfterCreation() throws Exception {
    
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

            // When: 데이터 셋 생성
            datasetSVC.createDataset(inputData_dataset, requestId, now);
    
            // 잘못된 ID 값으로 데이터 모델 조회 및 검증
            assertNull(datasetRetrieveSVC.getDatasetVOById("올바르지않아요"), "잘못된 ID로 조회한 결과는 null이어야 함");
            assertNull(datasetRetrieveSVC.getDatasetVOById("#"), "잘못된 ID로 조회한 결과는 null이어야 함");
            assertNull(datasetRetrieveSVC.getDatasetVOById("?"), "잘못된 ID로 조회한 결과는 null이어야 함");
            assertNull(datasetRetrieveSVC.getDatasetVOById("~"), "잘못된 ID로 조회한 결과는 null이어야 함");
    
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    // Update------------------------------

    @Test
    @DisplayName("존재하는 데이터 셋 id 업데이트 테스트")
    public void SetTest_013_Update_id_ExistingDataSet() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        String inputData_dataSet_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json")));

        Map<String, Object> inputData_dataSet_Map = om.readValue(inputData_dataSet_Str,
                new TypeReference<Map<String, Object>>() {
                });
        inputData_dataSet_Map.put("id", String.format((String) inputData_dataSet_Map.get("id"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("name", String.format((String) inputData_dataSet_Map.get("name"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("dataModelId",
                String.format((String) inputData_dataSet_Map.get("dataModelId"), TEST_DATAMODEL_ID));

        // 데이터 셋 생성
        datasetSVC.createDataset(om.writeValueAsString(inputData_dataSet_Map), requestId, now);


        // TODO: 업데이트 전 ID과 업데이트 후의 ID 둘 다 있는지 확인하기
        try {
            // When: 데이터 셋 업데이트
            String updatedId = "UpdatedTestModel3";
            inputData_dataSet_Map.put("id", updatedId);

            System.out.println("DEDEDEBUG" + inputData_dataSet_Map);

            String updatedDataSetStr = om.writeValueAsString(inputData_dataSet_Map);
            datasetSVC.updateDataset("/datasets/" + TEST_DATASET_ID, updatedDataSetStr, requestId, now);

            DatasetBaseVO updatedDataSet = datasetRetrieveSVC.getDatasetVOById(TEST_DATASET_ID);
            assertEquals("UpdatedTestModel3", updatedDataSet.getId(),
                    "The id of the data set should be updated to 'UpdatedTestModel3'");
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }  

    @Test
    @DisplayName("존재하는 데이터 셋 name 업데이트 테스트")
    public void SetTest_014_Update_name_ExistingDataSet() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        String inputData_dataSet_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json")));

        Map<String, Object> inputData_dataSet_Map = om.readValue(inputData_dataSet_Str,
                new TypeReference<Map<String, Object>>() {
                });
        inputData_dataSet_Map.put("id", String.format((String) inputData_dataSet_Map.get("id"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("name", String.format((String) inputData_dataSet_Map.get("name"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("dataModelId",
                String.format((String) inputData_dataSet_Map.get("dataModelId"), TEST_DATAMODEL_ID));

        // 데이터 셋 생성
        datasetSVC.createDataset(om.writeValueAsString(inputData_dataSet_Map), requestId, now);

        try {
            // When: 데이터 셋 업데이트
            String updatedName = "UpdatedTestModel3";
            inputData_dataSet_Map.put("name", updatedName);

            // map 에 name이 657번 라인에 넣은 값으로 변경되었는지 확인 필요
            // map에 name라는 key가 유일한지 확인 필요

            System.out.println("DEDEDEBUG" + inputData_dataSet_Map);

            String updatedDataSetStr = om.writeValueAsString(inputData_dataSet_Map);
            datasetSVC.updateDataset("/datasets/" + TEST_DATASET_ID, updatedDataSetStr, requestId, now);

            DatasetBaseVO updatedDataSet = datasetRetrieveSVC.getDatasetVOById(TEST_DATASET_ID);
            assertEquals("UpdatedTestModel3", updatedDataSet.getName(),
                    "The name of the data set should be updated to 'UpdatedTestModel3'");
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    @DisplayName("존재하는 데이터 셋 dataModelId 업데이트 테스트")
    public void SetTest_015_Update_dataModelId_ExistingDataSet() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        String inputData_dataSet_Str =  new String(
            Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json")));
        
        Map<String, Object> inputData_dataSet_Map = om.readValue(inputData_dataSet_Str, new TypeReference<Map<String, Object>>() {});
        inputData_dataSet_Map.put("id", String.format((String) inputData_dataSet_Map.get("id"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("name", String.format((String) inputData_dataSet_Map.get("name"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("dataModelId", String.format((String) inputData_dataSet_Map.get("dataModelId"), TEST_DATAMODEL_ID));

        // 데이터 셋 생성
        datasetSVC.createDataset(om.writeValueAsString(inputData_dataSet_Map), requestId, now);
        
        try {
            // When: 데이터 셋 업데이트
            String updatedDataModelId = "UpdatedTestModel3";
            inputData_dataSet_Map.put("name", updatedDataModelId);
            
            String updatedDataSetStr = om.writeValueAsString(inputData_dataSet_Map);
            datasetSVC.updateDataset("/datasets/" + TEST_DATASET_ID, updatedDataSetStr, requestId, now);

            DatasetBaseVO updatedDataSet = datasetRetrieveSVC.getDatasetVOById(TEST_DATASET_ID);
            assertEquals("UpdatedTestModel3", updatedDataSet.getDataModelId(), "The dataModelId of the data set should be updated to 'UpdatedTestModel3'");
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }    
    
    @Test
    @DisplayName("존재하지 않는 데이터 셋 대상의 업데이트 테스트")
    public void SetTest_016_Update_NonExistingDataSet() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        String inputData_dataSet_Str =  new String(
            Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json")));
        
        Map<String, Object> inputData_dataSet_Map = om.readValue(inputData_dataSet_Str, new TypeReference<Map<String, Object>>() {});
        inputData_dataSet_Map.put("id", String.format((String) inputData_dataSet_Map.get("id"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("name", String.format((String) inputData_dataSet_Map.get("name"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("dataModelId", String.format((String) inputData_dataSet_Map.get("dataModelId"), TEST_DATAMODEL_ID));

        // 데이터 셋 생성
        // 안함
        
        try {
            // When: 데이터 셋 업데이트
            String updatedDataModelId = "UpdatedTestModel3";
            inputData_dataSet_Map.put("name", updatedDataModelId);
            
            String updatedDataSetStr = om.writeValueAsString(inputData_dataSet_Map);
            datasetSVC.updateDataset("/datasets/" + TEST_DATASET_ID, updatedDataSetStr, requestId, now);

            // 존재 하지 않을 때 신규 데이터 셋이 생성됨을 검증
            DatasetBaseVO updatedDataSet = datasetRetrieveSVC.getDatasetVOById(TEST_DATASET_ID);
            assertEquals("UpdatedTestModel3", updatedDataSet.getName(), "The name of the data set should be updated to 'UpdatedTestModel3'");
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }    

    @Test
    @DisplayName("올바르지 않은 형식(id 없는)의 데이터 셋 업데이트 테스트")
    public void SetTest_017_Update_DataSet_WithoutId() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        String inputData_dataSet_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json")));

        Map<String, Object> inputData_dataSet_Map = om.readValue(inputData_dataSet_Str,
                new TypeReference<Map<String, Object>>() {
                });
        inputData_dataSet_Map.put("id", String.format((String) inputData_dataSet_Map.get("id"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("name", String.format((String) inputData_dataSet_Map.get("name"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("dataModelId",
                String.format((String) inputData_dataSet_Map.get("dataModelId"), TEST_DATAMODEL_ID));

        // 데이터 셋 생성
        datasetSVC.createDataset(om.writeValueAsString(inputData_dataSet_Map), requestId, now);

        try {
            // When: 데이터 셋 업데이트
            inputData_dataSet_Map.remove("id");

            System.out.println("DEDEDEBUG" + inputData_dataSet_Map);

            String updatedDataSetStr = om.writeValueAsString(inputData_dataSet_Map);
            datasetSVC.updateDataset("/datasets/" + TEST_DATASET_ID, updatedDataSetStr, requestId, now);

            DatasetBaseVO updatedDataSet = datasetRetrieveSVC.getDatasetVOById(TEST_DATASET_ID);
            assertNull(updatedDataSet.getId());
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    @DisplayName("올바르지 않은 형식(name 없는)의 데이터 셋 업데이트 테스트")
    public void SetTest_018_Update_DataSet_WithoutName() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        String inputData_dataSet_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json")));

        Map<String, Object> inputData_dataSet_Map = om.readValue(inputData_dataSet_Str,
                new TypeReference<Map<String, Object>>() {
                });
        inputData_dataSet_Map.put("id", String.format((String) inputData_dataSet_Map.get("id"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("name", String.format((String) inputData_dataSet_Map.get("name"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("dataModelId",
                String.format((String) inputData_dataSet_Map.get("dataModelId"), TEST_DATAMODEL_ID));

        // 데이터 셋 생성
        datasetSVC.createDataset(om.writeValueAsString(inputData_dataSet_Map), requestId, now);

        try {
            // When: 데이터 셋 업데이트
            inputData_dataSet_Map.remove("name");

            System.out.println("DEDEDEBUG" + inputData_dataSet_Map);

            String updatedDataSetStr = om.writeValueAsString(inputData_dataSet_Map);
            datasetSVC.updateDataset("/datasets/" + TEST_DATASET_ID, updatedDataSetStr, requestId, now);

            DatasetBaseVO updatedDataSet = datasetRetrieveSVC.getDatasetVOById(TEST_DATASET_ID);
            assertNull(updatedDataSet.getName());
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(dataModelId 없는)의 데이터 셋 업데이트 테스트")
    public void SetTest_019_Update_DataSet_WithoutDataModelId() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        String inputData_dataSet_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json")));

        Map<String, Object> inputData_dataSet_Map = om.readValue(inputData_dataSet_Str,
                new TypeReference<Map<String, Object>>() {
                });
        inputData_dataSet_Map.put("id", String.format((String) inputData_dataSet_Map.get("id"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("name", String.format((String) inputData_dataSet_Map.get("name"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("dataModelId",
                String.format((String) inputData_dataSet_Map.get("dataModelId"), TEST_DATAMODEL_ID));

        // 데이터 셋 생성
        datasetSVC.createDataset(om.writeValueAsString(inputData_dataSet_Map), requestId, now);

        try {
            // When: 데이터 셋 업데이트
            inputData_dataSet_Map.remove("dataModelId");

            System.out.println("DEDEDEBUG" + inputData_dataSet_Map);

            String updatedDataSetStr = om.writeValueAsString(inputData_dataSet_Map);
            datasetSVC.updateDataset("/datasets/" + TEST_DATASET_ID, updatedDataSetStr, requestId, now);

            DatasetBaseVO updatedDataSet = datasetRetrieveSVC.getDatasetVOById(TEST_DATASET_ID);
            assertNull(updatedDataSet.getDataModelId());
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(datasetItems 없는)의 데이터 셋 업데이트 테스트")
    public void SetTest_020_Update_DataSet_WithoutDatasetItems() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        String inputData_dataSet_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json")));

        Map<String, Object> inputData_dataSet_Map = om.readValue(inputData_dataSet_Str,
                new TypeReference<Map<String, Object>>() {
                });
        inputData_dataSet_Map.put("id", String.format((String) inputData_dataSet_Map.get("id"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("name", String.format((String) inputData_dataSet_Map.get("name"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("dataModelId",
                String.format((String) inputData_dataSet_Map.get("dataModelId"), TEST_DATAMODEL_ID));

        // 데이터 셋 생성
        datasetSVC.createDataset(om.writeValueAsString(inputData_dataSet_Map), requestId, now);

        try {
            // When: 데이터 셋 업데이트
            inputData_dataSet_Map.remove("datasetItems");

            System.out.println("DEDEDEBUG" + inputData_dataSet_Map);

            String updatedDataSetStr = om.writeValueAsString(inputData_dataSet_Map);
            datasetSVC.updateDataset("/datasets/" + TEST_DATASET_ID, updatedDataSetStr, requestId, now);

            DatasetBaseVO updatedDataSet = datasetRetrieveSVC.getDatasetVOById(TEST_DATASET_ID);
            assertNull(updatedDataSet.getDatasetItems());
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    @DisplayName("올바르지 않은 형식(내용 없는)의 데이터 셋 업데이트 테스트")
    public void SetTest_021_Update_DataSet_Null() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        String inputData_dataSet_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataset.json")));

        Map<String, Object> inputData_dataSet_Map = om.readValue(inputData_dataSet_Str,
                new TypeReference<Map<String, Object>>() {
                });
        inputData_dataSet_Map.put("id", String.format((String) inputData_dataSet_Map.get("id"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("name", String.format((String) inputData_dataSet_Map.get("name"), TEST_DATASET_ID));
        inputData_dataSet_Map.put("dataModelId",
                String.format((String) inputData_dataSet_Map.get("dataModelId"), TEST_DATAMODEL_ID));

        // 데이터 셋 생성
        datasetSVC.createDataset(om.writeValueAsString(inputData_dataSet_Map), requestId, now);

        try {
            // When: 데이터 셋 업데이트
            String updatedDataSetStr = "";
            System.out.println("DEDEDEBUG" + updatedDataSetStr);

            Assertions.assertThrows(BadRequestException.class, () -> {
                datasetSVC.updateDataset("/datasets/" + TEST_DATASET_ID, updatedDataSetStr, requestId, now);
            }, "BadRequestException was expected");

            DatasetBaseVO updatedDataSet = datasetRetrieveSVC.getDatasetVOById(TEST_DATASET_ID);
            assertNull(updatedDataSet.getName());
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    // Delete---------------------------------

    @Test
    @DisplayName("존재하는 데이터 셋 삭제 테스트")
    public void SetTest_022_Delete_ExistingDataSet() throws Exception {

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

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        // 데이터 셋 생성
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        try {
            // When: 데이터 셋 삭제
            datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");

            // 잘 삭제 되었는지 확인
            DatasetBaseVO updatedDataSet = datasetRetrieveSVC.getDatasetVOById(TEST_DATASET_ID);
            assertNull(updatedDataSet);
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("존재하지 않는 데이터 셋 삭제 테스트")
    public void SetTest_023_Delete_NonExistingDataSet() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        // 데이터 셋 생성
        // 안함

        try {
            // When: 데이터 셋 삭제
            datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");

        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                datasetSVC.deleteDataset("/datasets/" + TEST_DATASET_ID, "");
                dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

}