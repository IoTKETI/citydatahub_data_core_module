package kr.re.keti.sc.dataservicebroker.provisioning.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.BindException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WebAppConfiguration
public class ProvisioningControllerTest {

  @Autowired
  private MockMvc mvc;

  @Test
  void testProvisionDataModels() throws Exception {
    String inputData_datamodel =
      "{\"data\":\"{\\\"attributes\\\":[{\\\"name\\\":\\\"location\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"GeoJson\\\",\\\"attributeType\\\":\\\"GeoProperty\\\",\\\"hasObservedAt\\\":true,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"https://uri.etsi.org/ngsi-ld/location\\\"},{\\\"name\\\":\\\"objects\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"Object\\\",\\\"objectMembers\\\":[{\\\"name\\\":\\\"interger\\\",\\\"valueType\\\":\\\"Integer\\\"},{\\\"name\\\":\\\"boolean\\\",\\\"valueType\\\":\\\"Boolean\\\"},{\\\"name\\\":\\\"string\\\",\\\"valueType\\\":\\\"String\\\"},{\\\"name\\\":\\\"date\\\",\\\"valueType\\\":\\\"Date\\\"},{\\\"name\\\":\\\"double\\\",\\\"valueType\\\":\\\"Double\\\"}],\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"https://uri.etsi.org/ngsi-ld/hasObjects\\\"},{\\\"name\\\":\\\"observationSpace\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"GeoJson\\\",\\\"attributeType\\\":\\\"GeoProperty\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"https://uri.etsi.org/ngsi-ld/observationSpace\\\"},{\\\"name\\\":\\\"testArrayBoolean\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"ArrayBoolean\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\\\"},{\\\"name\\\":\\\"testArrayDouble\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"ArrayDouble\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\\\"},{\\\"name\\\":\\\"testArrayInteger\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"ArrayInteger\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\\\"},{\\\"name\\\":\\\"testArrayObject\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"ArrayObject\\\",\\\"objectMembers\\\":[{\\\"name\\\":\\\"testArrObjString\\\",\\\"valueType\\\":\\\"String\\\"},{\\\"name\\\":\\\"testArrObjInteger\\\",\\\"valueType\\\":\\\"Integer\\\"},{\\\"name\\\":\\\"testArrObjDouble\\\",\\\"valueType\\\":\\\"Double\\\"},{\\\"name\\\":\\\"testArrObjBoolean\\\",\\\"valueType\\\":\\\"Boolean\\\"},{\\\"name\\\":\\\"testArrObjDate\\\",\\\"valueType\\\":\\\"Date\\\"}],\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\\\"},{\\\"name\\\":\\\"testArrayString\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"ArrayString\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\\\"},{\\\"name\\\":\\\"testBoolean\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"Boolean\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"childAttributes\\\":[{\\\"name\\\":\\\"testInteger\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"Integer\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testInteger\\\"},{\\\"name\\\":\\\"testObject\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"Object\\\",\\\"objectMembers\\\":[{\\\"name\\\":\\\"string\\\",\\\"valueType\\\":\\\"String\\\"}],\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testObject\\\"},{\\\"name\\\":\\\"testString\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"String\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testString\\\"}],\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\\\"},{\\\"name\\\":\\\"testDate\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"Date\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testDate\\\"},{\\\"name\\\":\\\"testDouble\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"Double\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testDouble\\\"},{\\\"name\\\":\\\"testGeoJson\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"GeoJson\\\",\\\"attributeType\\\":\\\"GeoProperty\\\",\\\"hasObservedAt\\\":true,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\\\"},{\\\"name\\\":\\\"testInteger\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"Integer\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testInteger\\\"},{\\\"name\\\":\\\"testRelationshipString\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"String\\\",\\\"attributeType\\\":\\\"Relationship\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\\\"},{\\\"name\\\":\\\"testString\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"String\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"childAttributes\\\":[],\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testString\\\"}],\\\"context\\\":[\\\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\\\",\\\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\\\"],\\\"description\\\":\\\"string\\\",\\\"id\\\":\\\"TestModel3\\\",\\\"name\\\":\\\"TestModel3\\\",\\\"type\\\":\\\"TestModel3\\\",\\\"typeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\\\"}\",\"eventTime\":\"9999-11-15T20:10:00.000+09:00\",\"eventType\":\"created\",\"requestId\":\"string\",\"to\":\"/datamodels\"}";
      //"{\"data\":{\"attributes\":[{\"attributeType\":\"Property\",\"attributeUri\":\"https://uri.etsi.org/ngsi-ld/hasObjects\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"objects\",\"objectMembers\":[{\"name\":\"interger\",\"valueType\":\"Integer\"},{\"name\":\"boolean\",\"valueType\":\"Boolean\"},{\"name\":\"string\",\"valueType\":\"String\"},{\"name\":\"date\",\"valueType\":\"Date\"},{\"name\":\"double\",\"valueType\":\"Double\"}],\"valueType\":\"Object\"},{\"attributeType\":\"GeoProperty\",\"attributeUri\":\"https://uri.etsi.org/ngsi-ld/observationSpace\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"observationSpace\",\"valueType\":\"GeoJson\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testArrayBoolean\",\"valueType\":\"ArrayBoolean\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testArrayDouble\",\"valueType\":\"ArrayDouble\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testArrayInteger\",\"valueType\":\"ArrayInteger\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testArrayObject\",\"objectMembers\":[{\"name\":\"testArrObjString\",\"valueType\":\"String\"},{\"name\":\"testArrObjInteger\",\"valueType\":\"Integer\"},{\"name\":\"testArrObjDouble\",\"valueType\":\"Double\"},{\"name\":\"testArrObjBoolean\",\"valueType\":\"Boolean\"},{\"name\":\"testArrObjDate\",\"valueType\":\"Date\"}],\"valueType\":\"ArrayObject\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testArrayString\",\"valueType\":\"ArrayString\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\",\"childAttributes\":[{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testInteger\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testInteger\",\"valueType\":\"Integer\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testObject\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testObject\",\"objectMembers\":[{\"name\":\"string\",\"valueType\":\"String\"}],\"valueType\":\"Object\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testString\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testString\",\"valueType\":\"String\"}],\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testBoolean\",\"valueType\":\"Boolean\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testDate\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testDate\",\"valueType\":\"Date\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testDouble\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testDouble\",\"valueType\":\"Double\"},{\"attributeType\":\"GeoProperty\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\",\"hasObservedAt\":true,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testGeoJson\",\"valueType\":\"GeoJson\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testInteger\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testInteger\",\"valueType\":\"Integer\"},{\"attributeType\":\"Relationship\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testRelationshipString\",\"valueType\":\"String\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testString\",\"childAttributes\":[],\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testString\",\"valueType\":\"String\"}],\"context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"createdAt\":\"2022-03-29T04:04:28.836Z\",\"id\":\"zzTestModelDJ22\",\"modifiedAt\":\"2022-10-25T04:12:09.291Z\",\"name\":\"TestModel3\",\"type\":\"TestModel3\",\"typeUri\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\"},\"eventTime\":\"2023-02-07T13:28:40.604+09:00\",\"eventType\":\"created\",\"requestId\":\"b7121267-9876-4d23-9dc8-7c9793c6c5d6\",\"to\":\"/datamodels\"}";
    /*
     201 Created TDD
*/
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/provision/datamodels")
          .content(inputData_datamodel)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header(
            "Content-Length",
            String.valueOf(inputData_datamodel.length())
          )
      )
      .andExpect(status().isCreated())
      .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();    
    System.out.println("=====================Post=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");

    String updateData_datamodel =
      "{\"data\":\"{\\\"attributes\\\":[{\\\"name\\\":\\\"location\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"GeoJson\\\",\\\"attributeType\\\":\\\"GeoProperty\\\",\\\"hasObservedAt\\\":true,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"https://uri.etsi.org/ngsi-ld/location\\\"},{\\\"name\\\":\\\"objects\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"Object\\\",\\\"objectMembers\\\":[{\\\"name\\\":\\\"interger\\\",\\\"valueType\\\":\\\"Integer\\\"},{\\\"name\\\":\\\"boolean\\\",\\\"valueType\\\":\\\"Boolean\\\"},{\\\"name\\\":\\\"string\\\",\\\"valueType\\\":\\\"String\\\"},{\\\"name\\\":\\\"date\\\",\\\"valueType\\\":\\\"Date\\\"},{\\\"name\\\":\\\"double\\\",\\\"valueType\\\":\\\"Double\\\"}],\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":true,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"https://uri.etsi.org/ngsi-ld/hasObjects\\\"},{\\\"name\\\":\\\"observationSpace\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"GeoJson\\\",\\\"attributeType\\\":\\\"GeoProperty\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"https://uri.etsi.org/ngsi-ld/observationSpace\\\"},{\\\"name\\\":\\\"testArrayBoolean\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"ArrayBoolean\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\\\"},{\\\"name\\\":\\\"testArrayDouble\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"ArrayDouble\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\\\"},{\\\"name\\\":\\\"testArrayInteger\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"ArrayInteger\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\\\"},{\\\"name\\\":\\\"testArrayObject\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"ArrayObject\\\",\\\"objectMembers\\\":[{\\\"name\\\":\\\"testArrObjString\\\",\\\"valueType\\\":\\\"String\\\"},{\\\"name\\\":\\\"testArrObjInteger\\\",\\\"valueType\\\":\\\"Integer\\\"},{\\\"name\\\":\\\"testArrObjDouble\\\",\\\"valueType\\\":\\\"Double\\\"},{\\\"name\\\":\\\"testArrObjBoolean\\\",\\\"valueType\\\":\\\"Boolean\\\"},{\\\"name\\\":\\\"testArrObjDate\\\",\\\"valueType\\\":\\\"Date\\\"}],\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\\\"},{\\\"name\\\":\\\"testArrayString\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"ArrayString\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\\\"},{\\\"name\\\":\\\"testBoolean\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"Boolean\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"childAttributes\\\":[{\\\"name\\\":\\\"testInteger\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"Integer\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testInteger\\\"},{\\\"name\\\":\\\"testObject\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"Object\\\",\\\"objectMembers\\\":[{\\\"name\\\":\\\"string\\\",\\\"valueType\\\":\\\"String\\\"}],\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testObject\\\"},{\\\"name\\\":\\\"testString\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"String\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testString\\\"}],\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\\\"},{\\\"name\\\":\\\"testDate\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"Date\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testDate\\\"},{\\\"name\\\":\\\"testDouble\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"Double\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testDouble\\\"},{\\\"name\\\":\\\"testGeoJson\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"GeoJson\\\",\\\"attributeType\\\":\\\"GeoProperty\\\",\\\"hasObservedAt\\\":true,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\\\"},{\\\"name\\\":\\\"testInteger\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"Integer\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testInteger\\\"},{\\\"name\\\":\\\"testRelationshipString\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"String\\\",\\\"attributeType\\\":\\\"Relationship\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\\\"},{\\\"name\\\":\\\"testString\\\",\\\"isRequired\\\":false,\\\"valueType\\\":\\\"String\\\",\\\"attributeType\\\":\\\"Property\\\",\\\"hasObservedAt\\\":false,\\\"hasUnitCode\\\":false,\\\"childAttributes\\\":[],\\\"attributeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/testString\\\"}],\\\"context\\\":[\\\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\\\",\\\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\\\"],\\\"description\\\":\\\"string\\\",\\\"id\\\":\\\"TestModel3\\\",\\\"name\\\":\\\"TestModel3\\\",\\\"type\\\":\\\"TestModel3\\\",\\\"typeUri\\\":\\\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\\\"}\",\"eventTime\":\"9999-11-15T20:10:00.000+09:00\",\"eventType\":\"updated\",\"requestId\":\"string\",\"to\":\"/datamodels/TestModel3\"}";
      //"{\"data\":{\"attributes\":[{\"attributeType\":\"Property\",\"attributeUri\":\"https://uri.etsi.org/ngsi-ld/hasObjects\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"objects\",\"objectMembers\":[{\"name\":\"interger\",\"valueType\":\"Integer\"},{\"name\":\"boolean\",\"valueType\":\"Boolean\"},{\"name\":\"string\",\"valueType\":\"String\"},{\"name\":\"date\",\"valueType\":\"Date\"},{\"name\":\"double\",\"valueType\":\"Double\"}],\"valueType\":\"Object\"},{\"attributeType\":\"GeoProperty\",\"attributeUri\":\"https://uri.etsi.org/ngsi-ld/observationSpace\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"observationSpace\",\"valueType\":\"GeoJson\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testArrayBoolean\",\"valueType\":\"ArrayBoolean\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testArrayDouble\",\"valueType\":\"ArrayDouble\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testArrayInteger\",\"valueType\":\"ArrayInteger\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testArrayObject\",\"objectMembers\":[{\"name\":\"testArrObjString\",\"valueType\":\"String\"},{\"name\":\"testArrObjInteger\",\"valueType\":\"Integer\"},{\"name\":\"testArrObjDouble\",\"valueType\":\"Double\"},{\"name\":\"testArrObjBoolean\",\"valueType\":\"Boolean\"},{\"name\":\"testArrObjDate\",\"valueType\":\"Date\"}],\"valueType\":\"ArrayObject\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testArrayString\",\"valueType\":\"ArrayString\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\",\"childAttributes\":[{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testInteger\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testInteger\",\"valueType\":\"Integer\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testObject\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testObject\",\"objectMembers\":[{\"name\":\"string\",\"valueType\":\"String\"}],\"valueType\":\"Object\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testString\",\"hasObservedAt\":true,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testString\",\"valueType\":\"String\"}],\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testBoolean\",\"valueType\":\"Boolean\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testDate\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testDate\",\"valueType\":\"Date\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testDouble\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testDouble\",\"valueType\":\"Double\"},{\"attributeType\":\"GeoProperty\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\",\"hasObservedAt\":true,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testGeoJson\",\"valueType\":\"GeoJson\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testInteger\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testInteger\",\"valueType\":\"Integer\"},{\"attributeType\":\"Relationship\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\",\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testRelationshipString\",\"valueType\":\"String\"},{\"attributeType\":\"Property\",\"attributeUri\":\"http://uri.citydatahub.kr/ngsi-ld/testString\",\"childAttributes\":[],\"hasObservedAt\":false,\"hasUnitCode\":false,\"isRequired\":false,\"name\":\"testString\",\"valueType\":\"String\"}],\"context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"createdAt\":\"2022-03-29T04:04:28.836Z\",\"id\":\"zzTestModelDJ22\",\"modifiedAt\":\"2022-10-25T04:12:09.291Z\",\"name\":\"TestModel3\",\"type\":\"TestModel3\",\"typeUri\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\"},\"eventTime\":\"2023-02-07T13:28:40.604+09:00\",\"eventType\":\"updated\",\"requestId\":\"b7121267-9876-4d23-9dc8-7c9793c6c5d6\",\"to\":\"/datamodels/zzTestModelDJ25\"}";
      ResultActions resultActions_update = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/provision/datamodels")
          .content(updateData_datamodel)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header(
            "Content-Length",
            String.valueOf(updateData_datamodel.length())
          )
      )
      .andExpect(status().isNoContent())
      .andDo(print());

    MvcResult mvcResult_update = resultActions_update.andReturn();
    System.out.println("=====================Post=====================");
    System.out.println(mvcResult_update.getResponse().getContentAsString());
    System.out.println("=====================End=====================");
  }

  @Test
  void testProvisionDatasets() throws Exception {
    String inputData_dataset =
      "{\"data\":\"{\\\"classification\\\":\\\"CITY\\\",\\\"createdAt\\\":\\\"2022-10-11T16:53:49.711+09:00\\\",\\\"id\\\":\\\"TestModel3\\\",\\\"metasetId\\\":\\\"TestModel3\\\",\\\"name\\\":\\\"TestModel3\\\",\\\"ownership\\\":\\\"ADMIN\\\",\\\"qualityCheckEnabled\\\":true}\",\"eventTime\":\"2023-11-15T20:10:00.000+09:00\",\"eventType\":\"created\",\"requestId\":\"9d910130-07c9-4418-945b-a7cc13ff8188\",\"to\":\"/datamodels/TestModel3\"}";
    /*
     201 Created TDD
*/

    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/provision/datasets")
          .content(inputData_dataset)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header("Content-Length", String.valueOf(inputData_dataset.length()))
      )
      .andExpect(status().isCreated())
      .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Post=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");
  }

  @Test
  void testProvisionDatasetFlows() throws Exception {
    String inputData_datasetflow =
      "{\"data\":\"{\\\"bigDataStorageTypes\\\":[\\\"rdb\\\"],\\\"datasetId\\\":\\\"TestModel3\\\",\\\"description\\\":\\\"description\\\",\\\"enabled\\\":true,\\\"historyStoreType\\\":\\\"none\\\"}\",\"eventTime\":\"2022-10-11T16:53:49.711+09:00\",\"eventType\":\"created\",\"requestId\":\"string\",\"to\":\"/datasets/TestModel3/flow\"}";
    /*
    201 Created TDD
*/
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/provision/datasetflows")
          .content(inputData_datasetflow)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header(
            "Content-Length",
            String.valueOf(inputData_datasetflow.length())
          )
      )
      .andExpect(status().isCreated())
      .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Post=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");
  }

  @Test
  void testProvisionAclRule() throws Exception {
    String inputData =
      "{\"data\":\"{\\\"id\\\":\\\"test\\\",\\\"userId\\\":\\\"test\\\",\\\"clientId\\\":\\\"test\\\",\\\"resourceId\\\":\\\"test\\\",\\\"resourceType\\\":\\\"DATASET\\\",\\\"condition\\\":\\\"AND\\\",\\\"operation\\\":[\\\"create\\\"]}\",\"eventTime\":\"2022-10-11T16:53:49.711+09:00\",\"eventType\":\"created\",\"requestId\":\"string\",\"to\":\"/datasets/Test3/flow\"}";
    /*
    201 Created TDD
*/
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/provision/acl/rules")
          .content(inputData)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header("Content-Length", String.valueOf(inputData.length()))
      )
      .andExpect(status().isCreated())
      //  .andExpect(content().string("{\"id\":\"TDD\"}"))
      .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Post=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");
  }
}
