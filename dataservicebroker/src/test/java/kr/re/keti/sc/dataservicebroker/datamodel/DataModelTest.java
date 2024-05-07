package kr.re.keti.sc.dataservicebroker.datamodel;

import kr.re.keti.sc.dataservicebroker.DataServiceBrokerApplication;
import kr.re.keti.sc.dataservicebroker.common.TestEnvironment;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.service.DataModelSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelBaseVO;
import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.dataset.service.DatasetSVC;
import kr.re.keti.sc.dataservicebroker.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.dataservicebroker.datasetflow.service.DatasetFlowRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.datasetflow.service.DatasetFlowSVC;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.DatasetFlowBaseVO;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@ContextConfiguration(classes = DataServiceBrokerApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class DataModelTest {

    @Value("${entity.default.storage}")
    private String datastorage;

    @Autowired
    private DataModelSVC dataModelSVC;
    
    @Autowired
    private DatasetSVC datasetSVC;
    
    @Autowired
    private DatasetFlowSVC datasetFlowSVC;

    @Autowired
    private DataModelRetrieveSVC dataModelRetrieveSVC;

    @Autowired
    private DatasetRetrieveSVC datasetRetrieveSVC;

    @Autowired
    private DatasetFlowRetrieveSVC datasetFlowRetrieveSVC;

    @Autowired
    private TestEnvironment testEnvironment;

    @BeforeEach
    public void setup() {
        System.out.println("\n==================== Setup Function Test ==================");
        System.out.println(dataModelSVC);
        System.out.println(datasetSVC);
        System.out.println(datasetFlowSVC);
        System.out.println(dataModelRetrieveSVC);
        System.out.println(datasetRetrieveSVC);
        System.out.println(datasetFlowRetrieveSVC);
        System.out.println("=============================================================\n");

        testEnvironment = new TestEnvironment(dataModelSVC, datasetSVC, datasetFlowSVC, dataModelRetrieveSVC,
                datasetRetrieveSVC, datasetFlowRetrieveSVC);
    }

    @Test
    @DisplayName("테스트 모델, 셋, 플로우")
    public void test_000() throws Exception {

        // 테스트 데이터 준비
        final String TEST_DATAMODEL_ID = UUID.randomUUID().toString();
        final String TEST_DATASET_ID = UUID.randomUUID().toString();

        // 데이터 모델 생성
        testEnvironment.setup(TEST_DATAMODEL_ID, TEST_DATASET_ID);

        // 데이터 모델 조회 및 검증
        DataModelBaseVO retrievedDataModel = dataModelRetrieveSVC.getDataModelBaseVOById(TEST_DATAMODEL_ID);
        assertNotNull(retrievedDataModel);
        assertEquals(TEST_DATAMODEL_ID, retrievedDataModel.getId());

        // 데이터 모델 정보 출력
        System.out.println("\n==================== Data Info =====================");
        System.out.println("TEST_DATAMODEL_ID:\t" + TEST_DATAMODEL_ID);
        System.out.println("TEST_DATASET_ID:\t" + TEST_DATASET_ID);
        System.out.println("====================================================\n");
        System.out.println("\n==================== Retrieved Data Model ====================");
        System.out.println(retrievedDataModel);
        System.out.println("=============================================================\n");

        // 데이터 셋 조회 및 검증
        DatasetBaseVO retrievedDataset = datasetRetrieveSVC.getDatasetVOById(TEST_DATASET_ID);
        assertNotNull(retrievedDataset);
        assertEquals(TEST_DATASET_ID, retrievedDataset.getId());

        // 데이터 셋 정보 출력
        System.out.println("==================== Retrieved Dataset ====================");
        System.out.println(retrievedDataset);
        System.out.println("===========================================================\n");

        // 데이터 셋 플로우 조회 및 검증
        DatasetFlowBaseVO retrievedDatasetFlow = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(TEST_DATASET_ID);
        assertNotNull(retrievedDatasetFlow);
        assertEquals(TEST_DATASET_ID, retrievedDatasetFlow.getDatasetId());

        // 데이터 셋 플로우 정보 출력
        System.out.println("==================== Retrieved Dataset Flow ====================");
        System.out.println(retrievedDatasetFlow);
        System.out.println("================================================================\n");

        // 테스트 종료 후 정리 => 데이터 적재 시 날아가니까, 지금은 비활성화 해 두기
        testEnvironment.cleanup(TEST_DATAMODEL_ID, TEST_DATASET_ID);
    }
    
}
