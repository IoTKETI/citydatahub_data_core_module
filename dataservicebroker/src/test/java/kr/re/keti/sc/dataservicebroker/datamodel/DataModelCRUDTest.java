package kr.re.keti.sc.dataservicebroker.datamodel;

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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.DataServiceBrokerApplication;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelBaseVO;

@SpringBootTest
@ContextConfiguration(classes = DataServiceBrokerApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class DataModelCRUDTest {

    @Value("${entity.default.storage}")
    private String datastorage;

    @Autowired
    private DataModelSVC dataModelSVC;

    @Autowired
    private DataModelRetrieveSVC dataModelRetrieveSVC;

    private ObjectMapper om = new ObjectMapper();

    // Create------------------------------

    @Test
    @DisplayName("데이터 모델 생성 테스트")
    public void ModelTest_001_Create_DataModel() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        try {
            // When: 데이터 모델 생성 요청
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

            // Then: 생성 요청 성공 후 아무런 반환 값 없음

        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // 정리 작업: 테스트 종료 후 데이터 모델 삭제
            dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
        }
    }

    // 노션, 데이터모델 테스트 이슈 1번 참조
    @Test
    @DisplayName("동일한 ID로 데이터 모델을 중복 생성하는 테스트")
    public void ModelTest_002_Create_DuplicateDataModel_WithSameId() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        // When: 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

        try {
            // Then: 데이터 모델 중복 생성 시도 및 예외 검증
            assertThrows(DuplicateKeyException.class, () -> {
                dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
            }, "DuplicateKeyException was expected");
        } finally {
            // 테스트 종료 후 정리: 데이터 모델 삭제
            dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
        }
    }

    // 노션, 데이터모델 테스트 이슈 2번 참조
    @Test
    @DisplayName("다른 ID를 갖는 두 개의 데이터 모델 생성 테스트")
    public void ModelTest_003_Create_TwoDataModels_WithDifferentIds() throws Exception {

        // Given: 데이터 모델 준비
        Date now = new Date();
        
        final String requestId_1 = UUID.randomUUID().toString() + "0";
        final String TEST_DATAMODEL_ID_1 = UUID.randomUUID().toString() + "0";
        String inputData_dataModel_1 = String.format(
            new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
            TEST_DATAMODEL_ID_1);
    
        final String requestId_2 = UUID.randomUUID().toString() + "1";
        final String TEST_DATAMODEL_ID_2 = UUID.randomUUID().toString() + "1";
        String inputData_dataModel_2 = String.format(
            new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
            TEST_DATAMODEL_ID_2);
    
        try {
            // When: 첫 번째 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel_1, requestId_1, now);

            // Then: 두 번째, 다른 ID를 갖는 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel_2, requestId_2, now);
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // 정리: 두 데이터 모델 삭제
            dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID_1, TEST_DATAMODEL_ID_1, requestId_1, now);
            dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID_2, TEST_DATAMODEL_ID_2, requestId_2, now);
        }
    }    

    @Test
    @DisplayName("올바르지 않은 형식(context 없는)의 데이터 모델 생성 테스트")
    public void ModelTest_004_Create_DataModel_WithoutContext() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 맵에서 context 제거
        inputData_dataModel_Map.remove("context");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(id 없는)의 데이터 모델 생성 테스트")
    public void ModelTest_005_Create_DataModel_WithoutId() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 맵에서 id 제거
        inputData_dataModel_Map.remove("id");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(NullPointerException.class, () -> {
                dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
            }, "NullPointerException was expected");
        } catch (BadRequestException e) {
            System.out.printf("NullPointerException occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(type 없는)의 데이터 모델 생성 테스트")
    public void ModelTest_006_Create_DataModel_WithoutType() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 맵에서 type 제거
        inputData_dataModel_Map.remove("type");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(NullPointerException.class, () -> {
                dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    // 노션, 데이터모델 테스트 이슈 4번 참조
    @Test
    @DisplayName("올바르지 않은 형식(typeUrl 없는)의 데이터 모델 생성 테스트")
    public void ModelTest_007_Create_DataModel_WithoutTypeUrl() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 맵에서 typeUrl 제거
        inputData_dataModel_Map.remove("typeUrl");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(name 없는)의 데이터 모델 생성 테스트")
    public void ModelTest_008_Create_DataModel_WithoutName() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 맵에서 name 제거
        inputData_dataModel_Map.remove("name");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    @DisplayName("올바르지 않은 형식(attributes 없는)의 데이터 모델 생성 테스트")
    public void ModelTest_009_Create_DataModel_WithoutAttributes() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 맵에서 attributes 제거
        inputData_dataModel_Map.remove("attributes");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(NullPointerException.class, () -> {
                dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    @DisplayName("올바르지 않은 형식(createdAt 없는)의 데이터 모델 생성 테스트")
    public void ModelTest_010_Create_DataModel_WithoutCreatedAt() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 맵에서 createdAt 제거
        inputData_dataModel_Map.remove("createdAt");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(modifiedAt 없는)의 데이터 모델 생성 테스트")
    public void ModelTest_011_Create_DataModel_WithoutModifiedAt() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 맵에서 modifiedAt 제거
        inputData_dataModel_Map.remove("modifiedAt");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    // Read------------------------------

    @Test
    @DisplayName("존재하는 데이터 모델 조회 테스트")
    public void ModelTest_012_Retrieve_ExistingDataModel() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);

        try {
            // When: 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);

            // Then: 데이터 모델 조회 및 검증
            DataModelBaseVO retrievedDataModel = dataModelRetrieveSVC.getDataModelBaseVOById(TEST_DATAMODEL_ID);
            assertNotNull(retrievedDataModel, "조회된 데이터 모델은 null이 아니어야 함");
            assertEquals(TEST_DATAMODEL_ID, retrievedDataModel.getId(), "조회된 데이터 모델의 ID가 예상과 일치해야 함");
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // 테스트 종료 후 정리: 데이터 모델 삭제
            dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
        }
    }

    @Test
    @DisplayName("데이터 모델를 안 만들었을 때, 데이터 모델 조회 테스트")
    public void ModelTest_013_Retrieve_DataModel_WithoutCreating() throws Exception {
    
        // Given: 테스트 데이터 준비
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
    
        // When: 데이터 모델 생성 없이 조회
        DataModelBaseVO retrievedDataModel = dataModelRetrieveSVC.getDataModelBaseVOById(TEST_DATAMODEL_ID);
    
        // Then: 조회 결과 검증 - 데이터 모델이 null이어야 함
        assertNull(retrievedDataModel, "데이터 모델이 생성되지 않았으므로, 조회 결과는 null이어야 함");
    }    

    @Test
    @DisplayName("데이터 모델 생성 후, 올바르지 않은 Data Model ID값의 데이터 모델 조회 테스트")
    public void ModelTest_014_Retrieve_WithInvalidDataModelId_AfterCreation() throws Exception {
    
        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
    
        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);
    
        try {
            // When: 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
    
            // Then: 잘못된 ID 값으로 데이터 모델 조회 및 검증
            assertNull(dataModelRetrieveSVC.getDataModelBaseVOById("올바르지않아요"), "잘못된 ID로 조회한 결과는 null이어야 함");
            assertNull(dataModelRetrieveSVC.getDataModelBaseVOById("#"), "잘못된 ID로 조회한 결과는 null이어야 함");
            assertNull(dataModelRetrieveSVC.getDataModelBaseVOById("?"), "잘못된 ID로 조회한 결과는 null이어야 함");
            assertNull(dataModelRetrieveSVC.getDataModelBaseVOById("~"), "잘못된 ID로 조회한 결과는 null이어야 함");
    
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // 테스트 종료 후 정리: 데이터 모델 삭제
            dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
        }
    }

    // Update------------------------------

    // 노션, 데이터모델 테스트 이슈 5번 참조
    @Test
    @DisplayName("존재하는 데이터 모델 업데이트 테스트")
    public void ModelTest_015_Update_ExistingDataModel() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        
        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));
    
        try {
            // 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
    
            // When: 데이터 모델 업데이트
            String updatedName = "UpdatedTestModel3";
            inputData_dataModel_Map.put("name", updatedName);

            String updatedDataModelStr = om.writeValueAsString(inputData_dataModel_Map);
            dataModelSVC.processUpdate("/datamodels/" + TEST_DATAMODEL_ID, updatedDataModelStr, requestId, now);
    
            // Then: 업데이트된 내용을 검증
            DataModelBaseVO updatedDataModel = dataModelRetrieveSVC.getDataModelBaseVOById(TEST_DATAMODEL_ID);
            assertEquals("UpdatedTestModel3", updatedDataModel.getName(), "The name of the data model should be updated to 'UpdatedTestModel3'");
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // 테스트 종료 후 정리 => 데이터 모델 삭제
            dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
        }
    }    

    // 노션, 데이터모델 테스트 이슈 5번 참조
    @Test
    @DisplayName("존재하지 않는 데이터 모델 대상의 업데이트 테스트")
    public void ModelTest_016_Update_NonExistingDataModel() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
    
        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));
    
        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));
    
        try {
            // When: 데이터 모델 업데이트 (존재하지 않을 경우 UPSERT 처리)
            String updatedName = "UpdatedTestModel3";
            inputData_dataModel_Map.put("name", updatedName);
            
            String updatedDataModelStr = om.writeValueAsString(inputData_dataModel_Map);
            dataModelSVC.processUpdate("/datamodels/" + TEST_DATAMODEL_ID, updatedDataModelStr, requestId, now);
    
            // Then: 업데이트된 내용을 검증
            DataModelBaseVO updatedDataModel = dataModelRetrieveSVC.getDataModelBaseVOById(TEST_DATAMODEL_ID);
            assertNull(updatedDataModel.getName(), "The name of the data model should be updated to 'UpdatedTestModel3'");
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        } finally {
            // 테스트 종료 후 정리 (UPSERT된 데이터 모델 삭제)
            dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(context 없는)의 데이터 모델 업데이트 테스트")
    public void ModelTest_017_Update_DataModel_WithoutContext() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);

        // 데이터 모델 맵에서 context 제거
        inputData_dataModel_Map.remove("context");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                dataModelSVC.processUpdate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("올바르지 않은 형식(id 없는)의 데이터 모델 업데이트 테스트")
    public void ModelTest_018_Update_DataModel_WithoutId() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);

        // 데이터 모델 맵에서 id 제거
        inputData_dataModel_Map.remove("id");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                dataModelSVC.processUpdate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
            }, "BadRequestException was expected");
        } catch (BadRequestException e) {
            System.out.printf("BadRequestException  occurred: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.out.printf("Unexpected occurred: " + e.getMessage(), e);
            throw e;
        } finally {
            // Then: 정리 코드 실행
            try {
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    @DisplayName("올바르지 않은 형식(type 없는)의 데이터 모델 업데이트 테스트")
    public void ModelTest_019_Update_DataModel_WithoutType() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
        
        // 데이터 모델 맵에서 type 제거
        inputData_dataModel_Map.remove("type");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                dataModelSVC.processUpdate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    @DisplayName("올바르지 않은 형식(typeUrl 없는)의 데이터 모델 업데이트 테스트")
    public void ModelTest_20_Update_DataModel_WithoutTypeUrl() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);

        // 데이터 모델 맵에서 typeUrl 제거
        inputData_dataModel_Map.remove("typeUrl");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                dataModelSVC.processUpdate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    @DisplayName("올바르지 않은 형식(name 없는)의 데이터 모델 업데이트 테스트")
    public void ModelTest_021_Update_DataModel_WithoutName() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);

        // 데이터 모델 맵에서 name 제거
        inputData_dataModel_Map.remove("name");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                dataModelSVC.processUpdate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    @DisplayName("올바르지 않은 형식(attributes 없는)의 데이터 모델 업데이트 테스트")
    public void ModelTest_022_Update_DataModel_WithoutAttributes() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);

        // 데이터 모델 맵에서 attributes 제거
        inputData_dataModel_Map.remove("attributes");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                dataModelSVC.processUpdate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    @DisplayName("올바르지 않은 형식(createdAt 없는)의 데이터 모델 업데이트 테스트")
    public void ModelTest_023_Update_DataModel_WithoutCreatedAt() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);

        // 데이터 모델 맵에서 createdAt 제거
        inputData_dataModel_Map.remove("createdAt");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                dataModelSVC.processUpdate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    @DisplayName("올바르지 않은 형식(modifiedAt 없는)의 데이터 모델 업데이트 테스트")
    public void ModelTest_024_Update_DataModel_WithoutModifiedAt() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {
                });
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);

        // 데이터 모델 맵에서 modifiedAt 제거
        inputData_dataModel_Map.remove("modifiedAt");

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                dataModelSVC.processUpdate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId,
                        now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
@Test
    @DisplayName("올바르지 않은 형식(내용 없는)의 데이터 모델 업데이트 테스트")
    public void ModelTest_025_Update_DataModel_Null() throws Exception {

        // Given: 테스트 환경 설정
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();

        String inputData_dataModel_Str = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json")));

        Map<String, Object> inputData_dataModel_Map = om.readValue(inputData_dataModel_Str,
                new TypeReference<Map<String, Object>>() {});
        inputData_dataModel_Map.put("id", String.format((String) inputData_dataModel_Map.get("id"), TEST_DATAMODEL_ID));

        // 데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", om.writeValueAsString(inputData_dataModel_Map), requestId, now);

        try {
            // When: 테스트할 로직 실행
            Assertions.assertThrows(BadRequestException.class, () -> {
                dataModelSVC.processUpdate("/datamodels", "{}", requestId, now);
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
                if (TEST_DATAMODEL_ID != null) {
                    dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
                }
            } catch (Exception e) {
                System.out.printf("Error during cleanup: " + e.getMessage(), e);
            }
        }
    }
    
    // Delete---------------------------------

    @Test
    @DisplayName("존재하는 데이터 모델 삭제 테스트")
    public void ModelTest_026_Delete_ExistingDataModel() throws Exception {

        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
    
        String inputData_dataModel = String.format(
                new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_dataModel.json"))),
                TEST_DATAMODEL_ID);
    
        try {
            // 데이터 모델 생성
            dataModelSVC.processCreate("/datamodels", inputData_dataModel, requestId, now);
    
            // When: 모델 삭제
            dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
    
            // Then: 데이터 모델이 삭제되었는지 조회 및 검증
            DataModelBaseVO DataModel = dataModelRetrieveSVC.getDataModelBaseVOById(TEST_DATAMODEL_ID);
            assertNull(DataModel);
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        }
    }

    // 이슈: 포스트맨에서는 존재하지 않는것을 삭제 시 400 Bad Request 뜨는데, 여기서는 아무것도 안 뜸
    @Test
    @DisplayName("존재하지 않는 데이터 모델 삭제 테스트")
    public void ModelTest_027_Delete_NonExistingDataModel() throws Exception {
        
        // Given: 테스트 데이터 준비
        final String requestId = UUID.randomUUID().toString();
        Date now = new Date();
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
    
        try {
            // When: 존재하지 않는 모델 삭제 시도
            dataModelSVC.processDelete("/datamodels/" + TEST_DATAMODEL_ID, TEST_DATAMODEL_ID, requestId, now);
    
            // Then: 아무 일도 없었다.
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            throw e;
        }
    }    

}