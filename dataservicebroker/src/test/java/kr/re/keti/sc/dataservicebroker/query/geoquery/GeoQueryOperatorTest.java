package kr.re.keti.sc.dataservicebroker.query.geoquery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import kr.re.keti.sc.dataservicebroker.common.TestEnvironment;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityVO;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.entities.service.EntityRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.entities.vo.EntityRetrieveVO;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
public class GeoQueryOperatorTest {

    private static final String TEST_DATAMODEL_ID = "TestModel3";
    private static final String TEST_DATASET_ID = "TestModel3Flow";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestEnvironment testEnvironment;

    @Autowired
    private EntityRetrieveSVC entityRetrieveSVC;

    @BeforeAll
    public void setup() throws Exception {
        testEnvironment.setup(TEST_DATAMODEL_ID, TEST_DATASET_ID);
        setupTestData();
    }

    @AfterAll
    public void cleanup() {
        cleanupTestData();
        testEnvironment.cleanup(TEST_DATAMODEL_ID, TEST_DATASET_ID);
    }

    public void setupTestData() throws Exception {
        // Test data setup logic
        String requestBody = "";

        try {
            requestBody = new String(Files.readAllBytes(Paths.get("src/test/resources/inputData_geoQueryTest.json")));
            if (requestBody.isEmpty()) {
                fail("Request body is empty");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to read the input data file");
            return;
        }

        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/entityOperations/create")
                                .content(requestBody)
                                .contentType("application/ld+json")
                                .accept(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .header("Content-Length", String.valueOf(requestBody.length())))
                .andDo(print());

        MvcResult mvcResult = resultActions.andReturn();
        System.out.println("=====================Post====================");
        System.out.println(mvcResult.getResponse().getContentAsString());
        System.out.println("=====================End=====================");
    }

    public void cleanupTestData() {
        // Test data cleanup logic
        ResultActions resultActions;
        String[] ids = {
                "urn:datahub:TestModel3:geoQueryUnitTest:PointData:YatapStation",
                "urn:datahub:TestModel3:geoQueryUnitTest:MultiPointData:YatapStation",
                "urn:datahub:TestModel3:geoQueryUnitTest:LineStringData:YatapStation",
                "urn:datahub:TestModel3:geoQueryUnitTest:MultiLineStringData:YatapStation",
                "urn:datahub:TestModel3:geoQueryUnitTest:PolygonData:YatapStation",
                "urn:datahub:TestModel3:geoQueryUnitTest:MultiPolygonData:YatapStation"
        };

        for (String id : ids) {
            try {
                resultActions = mvc
                        .perform(
                                MockMvcRequestBuilders
                                        .delete("/entities/" + id)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8"))
                        .andDo(print());

                MvcResult mvcResult = resultActions.andReturn();
                System.out.println("=====================Delete==================");
                System.out.println(mvcResult.getResponse().getContentAsString());
                System.out.println("=====================End=====================");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private QueryVO setupQueryVO(String geometry, String georel, String coordinates) {
        QueryVO queryVO = new QueryVO();
        queryVO.setType("TestModel3");
        queryVO.setGeoproperty("testGeoJson");
        queryVO.setGeometry(geometry);
        queryVO.setGeorel(georel);
        queryVO.setCoordinates(coordinates);
        queryVO.setLinks(Arrays.asList(

                "http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld",
                "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld"));
        return queryVO;
    }

    private void executeAndValidateQuery(String testName, QueryVO queryVO, Integer expectedTotalCount) {
        try {
            System.out.println("[Debug] =====TEST START===============");
            System.out.println("[Debug] " + testName);
            // System.out.println("[Debug] QueryVO: " + queryVO);

            EntityRetrieveVO entityRetrieveVO = null;
            try {
                entityRetrieveVO = entityRetrieveSVC.getEntity(queryVO, "", "application/json", null);
            } catch (Exception e) {
                System.out.println("[Debug] [Error] Exception during getEntity call: " + e.getMessage());
                e.printStackTrace();
            }

            // 결과 객체가 null인 경우 테스트 실패로 처리
            if (entityRetrieveVO == null) {
                System.out.println("[Debug] [Error] entityRetrieveVO is null");
                fail("entityRetrieveVO is null");
            }

            System.out.println("[Debug] entityTotalCount: " + entityRetrieveVO.getTotalCount());

            entityRetrieveVO.getEntities().forEach(entity -> {
                CommonEntityVO commonEntity = (CommonEntityVO) entity;
                commonEntity.forEach((key, value) -> {
                    System.out.println("[Debug] " + key + ": " + value);
                });
            });

            // 단언문을 사용하여 결과 검증
            assertEquals(expectedTotalCount, entityRetrieveVO.getTotalCount(), "Expected total count");

        } catch (Exception e) {
            System.out.println("[Debug] [Error] Exception occurred in test: " + testName);
            e.printStackTrace();
            fail("[Debug] Test failed due to exception: " + e.getMessage());
        } finally {
            System.out.println("[Debug] =====TEST END=================");
        }
    }

    @Test
    public void Geo_TC001_Point_near_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Point",
                "near;maxDistance==127420000",
                "[127.12871304788501,37.41129752305784]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 6);
    }

    @Test
    public void Geo_TC002_Point_disjoint_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Point",
                "disjoint",
                "[0, 0]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 6);
    }

    @Test
    public void Geo_TC003_Point_intersects_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Point",
                "intersects",
                "[127.12886670350753, 37.41131350496035]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC004_Point_within_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Point",
                "within",
                "[127.12871304788501, 37.41129752305784]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 1);
    }

    @Test
    public void Geo_TC005_Point_contains_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Point",
                "contains",
                "[127.12871304788501, 37.41129752305784]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 3);
    }

    @Test
    public void Geo_TC006_Point_overlaps_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Point",
                "overlaps",
                "[127.12871304788501, 37.41129752305784]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC007_MultiPoint_near_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPoint",
                "near;maxDistance==999",
                "[[127.0,37.0],[127.0,37.001]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 1);
    }

    @Test
    public void Geo_TC008_MultiPoint_disjoint_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPoint",
                "disjoint",
                "[[127.0,37.0],[127.0,38.0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 5);
    }

    @Test
    public void Geo_TC009_MultiPoint_intersects_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPoint",
                "intersects",
                "[[127.0,37.0],[127.0,38.0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 1);
    }

    @Test
    public void Geo_TC010_MultiPoint_within_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPoint",
                "within",
                "[[127.0,37.0],[127.0,38.0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC011_MultiPoint_contains_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPoint",
                "contains",
                "[[127.0,37.0],[127.0,38.0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC012_MultiPoint_overlaps_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPoint",
                "overlaps",
                "[[127.0,37.0],[127.0,38.0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 1);
    }

    @Test
    public void Geo_TC013_LineString_near_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "LineString",
                "near;minDistance==500",
                "[[40.7128,-74.006],[41.7128,-74.006]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 4);
    }

    @Test
    public void Geo_TC014_LineString_disjoint_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "LineString",
                "disjoint",
                "[[40.7128,-74.006],[41.7128,-74.006]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 4);
    }

    @Test
    public void Geo_TC015_LineString_intersects_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "LineString",
                "intersects",
                "[[40.7128,-74.006],[41.7128,-75.006]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC016_LineString_within_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "LineString",
                "within",
                "[[40.7128,-74.006],[41.7128,-74.006]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 1);
    }

    @Test
    public void Geo_TC017_LineString_contains_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "LineString",
                "contains",
                "[[40.7128,-74.006],[41.7128,-74.006]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC018_LineString_overlaps_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "LineString",
                "overlaps",
                "[[41.2128,-74.006],[42.2128,-74.006]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC019_MultiLineString_near_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiLineString",
                "near;maxDistance==5000;minDistance==0",
                "[[[127.12886670350753,37.41131350496035],[127.12899047233589,37.41134510424966]],[[127.12886173811995,37.41121638874425],[127.1290009780509,37.41125149910977]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 3);
    }

    @Test
    public void Geo_TC020_MultiLineString_disjoint_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiLineString",
                "disjoint",
                "[[[127.12904246576102,37.41113730826733],[127.12920440911893,37.41119234111842]],[[127.12904974079567,37.411096696475184],[127.12920417131863,37.4111557076356]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 6);
    }

    @Test
    public void Geo_TC021_MultiLineString_intersects_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiLineString",
                "intersects",
                "[[[127.12886670350753,37.41131350496035],[127.12899047233589,37.41134510424966]],[[127.12886173811995,37.41121638874425],[127.1290009780509,37.41125149910977]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC022_MultiLineString_within_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiLineString",
                "within",
                "[[[40.7128,-74.0060],[41.7128,-74.0060]],[[40.7128,-73.0060],[41.7128,-73.0060]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC023_MultiLineString_contains_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiLineString",
                "contains",
                "[[[40.8128,-74.006],[41.4128,-74.006]],[[40.9128,-74.006],[41.3128,-74.006]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC024_MultiLineString_overlaps_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiLineString",
                "overlaps",
                "[[[41.2128,-74.0060],[42.2128,-74.0060]],[[41.2128,-75.0060],[42.2128,-75.0060]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC025_Polygon_near_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Polygon",
                "near;maxDistance==500",
                "[[[127.12845064888569,37.411435828265155],[127.12845064888569,37.411072436074434],[127.12893909372298,37.411072436074434],[127.12893909372298,37.411435828265155],[127.12845064888569,37.411435828265155]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 3);
    }

    @Test
    public void Geo_TC026_Polygon_disjoint_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Polygon",
                "disjoint",
                "[[[127.12845064888569,37.411435828265155],[127.12845064888569,37.411072436074434],[127.12893909372298,37.411072436074434],[127.12893909372298,37.411435828265155],[127.12845064888569,37.411435828265155]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 3);
    }

    @Test
    public void Geo_TC027_Polygon_intersects_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Polygon",
                "intersects",
                "[[[127.12874076583017,37.41151281488993],[127.12874076583017,37.41125433072958],[127.12908160709418,37.41125433072958],[127.12908160709418,37.41151281488993],[127.12874076583017,37.41151281488993]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC028_Polygon_within_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Polygon",
                "within",
                "[[[127.12845064888569,37.411435828265155],[127.12845064888569,37.411072436074434],[127.12893909372298,37.411072436074434],[127.12893909372298,37.411435828265155],[127.12845064888569,37.411435828265155]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 3);
    }

    @Test
    public void Geo_TC029_Polygon_contains_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Polygon",
                "contains",
                "[[[127.12845064888569,37.411435828265155],[127.12845064888569,37.411072436074434],[127.12893909372298,37.411072436074434],[127.12893909372298,37.411435828265155],[127.12845064888569,37.411435828265155]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC030_Polygon_overlaps_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Polygon",
                "overlaps",
                "[[[127.128900,37.411300],[127.129100,37.411300],[127.129100,37.411500],[127.128900,37.411500],[127.128900,37.411300]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC031_MultiPolygon_near_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPolygon",
                "near;maxDistance==500",
                "[[[[127.12907920354172,37.41121633030777],[127.12907920354172,37.41107857116114],[127.12928487034009,37.41107857116114],[127.12928487034009,37.41121633030777],[127.12907920354172,37.41121633030777]]],[[[127.12908446692734,37.4110215678886],[127.12908446692734,37.41089603238646],[127.12928635020108,37.41089603238646],[127.12928635020108,37.4110215678886],[127.12908446692734,37.4110215678886]]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 3);
    }

    @Test
    public void Geo_TC032_MultiPolygon_disjoint_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPolygon",
                "disjoint",
                "[[[[127.12907920354172,37.41121633030777],[127.12907920354172,37.41107857116114],[127.12928487034009,37.41107857116114],[127.12928487034009,37.41121633030777],[127.12907920354172,37.41121633030777]]],[[[127.12908446692734,37.4110215678886],[127.12908446692734,37.41089603238646],[127.12928635020108,37.41089603238646],[127.12928635020108,37.4110215678886],[127.12908446692734,37.4110215678886]]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 6);
    }

    @Test
    public void Geo_TC033_MultiPolygon_intersects_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPolygon",
                "intersects",
                "[[[[127.12852496185383,37.411375265325006],[127.12852496185383,37.410716433784174],[127.12866339609866,37.410716433784174],[127.12866339609866,37.411375265325006],[127.12852496185383,37.411375265325006]]],[[[127.1287404327498,37.411374037092315],[127.1287404327498,37.41071693268778],[127.12887501376719,37.41071693268778],[127.12887501376719,37.411374037092315],[127.1287404327498,37.411374037092315]]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC034_MultiPolygon_within_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPolygon",
                "within",
                "[[[[127.12841757026297,37.411459768262716],[127.12841757026297,37.410586539960974],[127.1290026270899,37.410586539960974],[127.1290026270899,37.411459768262716],[127.12841757026297,37.411459768262716]]],[[[127.12839271490725,37.41147951069789],[127.12839271490725,37.41057135329187],[127.12902365854444,37.41057135329187],[127.12902365854444,37.41147951069789],[127.12839271490725,37.41147951069789]]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC035_MultiPolygon_contains_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPolygon",
                "contains",
                "[[[[127.12845064888569,37.411435828265155],[127.12845064888569,37.411072436074434],[127.12893909372298,37.411072436074434],[127.12893909372298,37.411435828265155],[127.12845064888569,37.411435828265155]],[[127.12846138131374,37.410962998025866],[127.12846138131374,37.41061494462876],[127.12894694177623,37.41061494462876],[127.12894694177623,37.410962998025866],[127.12846138131374,37.410962998025866]]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC036_MultiPolygon_overlaps_valid(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPolygon",
                "overlaps",
                "[[[[127.12872021895328,37.41150367356637],[127.12872021895328,37.41052707039988],[127.1291239638885,37.41052707039988],[127.1291239638885,37.41150367356637],[127.12872021895328,37.41150367356637]]],[[[127.12835151602985,37.4115073040373],[127.12835151602985,37.41053433143628],[127.12868670050574,37.41053433143628],[127.12868670050574,37.4115073040373],[127.12835151602985,37.4115073040373]]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 2);
    }

    @Test
    public void Geo_TC037_Point_near_(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Point",
                "near;maxDistance==1000",
                "[0,0]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC038_Point_disjoint_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Point",
                "disjoint",
                "[0,0]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 6);
    }

    @Test
    public void Geo_TC039_Point_intersects_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Point",
                "intersects",
                "[0,0]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC040_Point_within_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Point",
                "within",
                "[0,0]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC041_Point_contains_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Point",
                "contains",
                "[0,0]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC042_Point_overlaps_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Point",
                "overlaps",
                "[0,0]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC043_MultiPoint_near_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPoint",
                "near;maxDistance==1000",
                "[[100,100],[0,0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC044_MultiPoint_disjoint_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPoint",
                "disjoint",
                "[[0,0],[0,0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 6);
    }

    @Test
    public void Geo_TC045_MultiPoint_intersects_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPoint",
                "intersects",
                "[[0,0],[0,0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC046_MultiPoint_within_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPoint",
                "within",
                "[[0,0],[0,0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC047_MultiPoint_contains_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPoint",
                "contains",
                "[[0,0],[0,0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC048_MultiPoint_overlaps_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPoint",
                "overlaps",
                "[[0,0],[0,0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC049_LineString_near_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "LineString",
                "near;maxDistance==0",
                "[[0,0],[0,0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC050_LineString_disjoint_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "LineString",
                "disjoint",
                "[[0,0],[0,0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 6);
    }

    @Test
    public void Geo_TC051_LineString_intersects_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "LineString",
                "intersects",
                "[[0,0],[0,0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC052_LineString_within_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "LineString",
                "within",
                "[[0,0],[0,0]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC053_LineString_contains_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "LineString",
                "contains",
                "[[0,0],[100,100]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC054_LineString_overlaps_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "LineString",
                "overlaps",
                "[[0,0],[100,100]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC055_MultiLineString_near_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiLineString",
                "near;maxDistance==0;minDistance==1000",
                "[[[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC056_MultiLineString_disjoint_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiLineString",
                "disjoint",
                "[[[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 6);
    }

    @Test
    public void Geo_TC057_MultiLineString_intersects_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiLineString",
                "intersects",
                "[[[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC058_MultiLineString_within_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiLineString",
                "within",
                "[[[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC059_MultiLineString_contains_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiLineString",
                "contains",
                "[[[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC060_MultiLineString_overlaps_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiLineString",
                "overlaps",
                "[[[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC061_Polygon_near_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Polygon",
                "near;maxDistance==1000",
                "[[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC062_Polygon_disjoint_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Polygon",
                "disjoint",
                "[[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 6);
    }

    @Test
    public void Geo_TC063_Polygon_intersects_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Polygon",
                "intersects",
                "[[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC064_Polygon_within_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Polygon",
                "within",
                "[[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC065_Polygon_contains_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Polygon",
                "contains",
                "[[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC066_Polygon_overlaps_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "Polygon",
                "overlaps",
                "[[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC067_MultiPolygon_near_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPolygon",
                "near;maxDistance==1000",
                "[[[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC068_MultiPolygon_disjoint_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPolygon",
                "disjoint",
                "[[[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 6);
    }

    @Test
    public void Geo_TC069_MultiPolygon_intersects_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPolygon",
                "intersects",
                "[[[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC070_MultiPolygon_within_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPolygon",
                "within",
                "[[[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC071_MultiPolygon_contains_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPolygon",
                "contains",
                "[[[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }

    @Test
    public void Geo_TC072_MultiPolygon_overlaps_ZeroCoordinates(TestInfo testInfo) {
        QueryVO queryVO = setupQueryVO(

                "MultiPolygon",
                "overlaps",
                "[[[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0],[0.0,0.0]]]]");

        executeAndValidateQuery(testInfo.getDisplayName(), queryVO, 0);
    }
}