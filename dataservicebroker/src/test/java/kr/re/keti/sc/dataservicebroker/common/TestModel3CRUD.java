package kr.re.keti.sc.dataservicebroker.common;

import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import lombok.Data;

@Data
@Component
public class TestModel3CRUD {

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

    @Value("${entity.default.storage}")
    private String datastorage;

    private String requestId = "6372e9fd-c007-4882-902d-6d964f4fbd5d";

    private Date now = new Date();

    private String dataModelId = "TestModel3";

    private String datasetId = "TestModel3";



    public void createTestModel3() {

        String inputData_datamodel = "{\"context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"TestModel3\",\"type\":\"TestModel3\",\"typeUri\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"name\":\"TestModel3\",\"attributes\":[{\"name\":\"location\",\"isRequired\":false,\"valueType\":\"GeoJson\",\"attributeType\":\"GeoProperty\",\"hasObservedAt\":true,\"hasUnitCode\":false,\"attributeUri\":\"https://uri.etsi.org/ngsi-ld/location\"},{\"name\":\"objects\",\"isRequired\":false,\"valueType\":\"Object\",\"objectMembers\":[{\"name\":\"interger\",\"valueType\":\"Integer\"},{\"name\":\"boolean\",\"valueType\":\"Boolean\"},{\"name\":\"string\",\"valueType\":\"String\"},{\"name\":\"date\",\"valueType\":\"Date\"},{\"name\":\"double\",\"valueType\":\"Double\"}],\"attributeType\":\"Property\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"attributeUri\":\"https://uri.etsi.org/ngsi-ld/hasObjects\"},{\"name\":\"observationSpace\",\"isRequired\":false,\"valueType\":\"GeoJson\",\"attributeType\":\"GeoProperty\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"attributeUri\":\"https://uri.etsi.org/ngsi-ld/observationSpace\"},{\"name\":\"testArrayBoolean\",\"isRequired\":false,\"valueType\":\"ArrayBoolean\",\"attributeType\":\"Property\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\"},{\"name\":\"testArrayDouble\",\"isRequired\":false,\"valueType\":\"ArrayDouble\",\"attributeType\":\"Property\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\"},{\"name\":\"testArrayInteger\",\"isRequired\":false,\"valueType\":\"ArrayInteger\",\"attributeType\":\"Property\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\"},{\"name\":\"testArrayObject\",\"isRequired\":false,\"valueType\":\"ArrayObject\",\"objectMembers\":[{\"name\":\"testArrObjString\",\"valueType\":\"String\"},{\"name\":\"testArrObjInteger\",\"valueType\":\"Integer\"},{\"name\":\"testArrObjDouble\",\"valueType\":\"Double\"},{\"name\":\"testArrObjBoolean\",\"valueType\":\"Boolean\"},{\"name\":\"testArrObjDate\",\"valueType\":\"Date\"}],\"attributeType\":\"Property\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\"},{\"name\":\"testArrayString\",\"isRequired\":false,\"valueType\":\"ArrayString\",\"attributeType\":\"Property\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\"},{\"name\":\"testBoolean\",\"isRequired\":false,\"valueType\":\"Boolean\",\"attributeType\":\"Property\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"childAttributes\":[{\"name\":\"testInteger\",\"isRequired\":false,\"valueType\":\"Integer\",\"attributeType\":\"Property\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testInteger\"},{\"name\":\"testObject\",\"isRequired\":false,\"valueType\":\"Object\",\"objectMembers\":[{\"name\":\"string\",\"valueType\":\"String\"}],\"attributeType\":\"Property\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testObject\"},{\"name\":\"testString\",\"isRequired\":false,\"valueType\":\"String\",\"attributeType\":\"Property\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testString\"}],\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\"},{\"name\":\"testDate\",\"isRequired\":false,\"valueType\":\"Date\",\"attributeType\":\"Property\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testDate\"},{\"name\":\"testDouble\",\"isRequired\":false,\"valueType\":\"Double\",\"attributeType\":\"Property\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testDouble\"},{\"name\":\"testGeoJson\",\"isRequired\":false,\"valueType\":\"GeoJson\",\"attributeType\":\"GeoProperty\",\"hasObservedAt\":true,\"hasUnitCode\":false,\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\"},{\"name\":\"testInteger\",\"isRequired\":false,\"valueType\":\"Integer\",\"attributeType\":\"Property\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testInteger\"},{\"name\":\"testRelationshipString\",\"isRequired\":false,\"valueType\":\"String\",\"attributeType\":\"Relationship\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\"},{\"name\":\"testString\",\"isRequired\":false,\"valueType\":\"String\",\"attributeType\":\"Property\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"childAttributes\":[],\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testString\"}],\"createdAt\":\"2022-03-29T04:04:28.836Z\",\"modifiedAt\":\"2022-10-25T04:12:09.291Z\"}";

        String inputData_dataset = "{\"id\":\"TestModel3\",\"name\":\"TestModel3\",\"updateInterval\":\"-\",\"category\":\"환경\",\"providerOrganization\":\"-\",\"providerSystem\":\"-\",\"isProcessed\":\"원천데이터\",\"ownership\":\"-\",\"license\":\"CCBY\",\"datasetItems\":\"-\",\"targetRegions\":\"-\",\"qualityCheckEnabled\":false,\"dataModelId\":\"TestModel3\"}";

        String inputData_datasetflow = "{\"historyStoreType\":\"all\",\"enabled\":true,\"bigDataStorageTypes\":[\""+ getDatastorage() + "\"]}";

        //데이터 모델 생성
        dataModelSVC.processCreate("/datamodels", inputData_datamodel, requestId, now);

        //데이터 모델 조회
        DataModelBaseVO dataModelBaseVO = dataModelRetrieveSVC.getDataModelBaseVOById(dataModelId);
        if (dataModelBaseVO == null) {
            throw new BadRequestException(ErrorCode.NOT_EXIST_ID, "Not Exists. dataModelId =" + dataModelId);

        }

        //데이터 셋 생성
        datasetSVC.createDataset(inputData_dataset, requestId, now);

        //데이터 셋 조회
        DatasetBaseVO datasetBaseVO = datasetRetrieveSVC.getDatasetVOById(datasetId);
        if (datasetBaseVO == null) {
            throw new BadRequestException(ErrorCode.NOT_EXIST_ID, "Not Exists. datasetId =" + datasetId);

        }

        //데이터 셋 플로우 생성
        datasetFlowSVC.createDatasetFlow("/datasets/TestModel3/flow", inputData_datasetflow, requestId, now);

        //데이터 셋 플로우 조회
        DatasetFlowBaseVO datasetFlowBaseVO = datasetFlowRetrieveSVC.getDatasetFlowBaseVOById(datasetId);
        if (datasetFlowBaseVO == null) {
            throw new BadRequestException(ErrorCode.NOT_EXIST_ID, "Not Exists data set flow. datasetId = " + datasetId);
        }

    }
    
    public void deleteTestModel3() {
        //데이터 셋 플로우 삭제
        datasetFlowSVC.deleteDatasetFlow("/datasets/TestModel3/flow", "");

        //데이터 셋 삭제
        datasetSVC.deleteDataset("/datasets/TestModel3", "");

        //데이터 모델 삭제
        dataModelSVC.processDelete("/datamodels/TestModel3",dataModelId ,requestId, now);
        
    }

 


}
