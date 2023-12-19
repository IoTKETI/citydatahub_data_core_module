package kr.re.keti.sc.dataservicebroker.TestModel3;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.re.keti.sc.dataservicebroker.common.CompareResponse;
import kr.re.keti.sc.dataservicebroker.common.TestModel3CRUD;

import java.net.BindException;
import java.util.Date;
import java.util.HashMap;
// import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
// import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.util.*;
import org.springframework.util.LinkedMultiValueMap;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WebAppConfiguration
public class TestModel3CRUDTest {

  @Autowired
  private MockMvc mvc;

  @Value("${entity.default.storage}")
  private String datastorage;

  @Autowired
  private TestModel3CRUD testModel3CRUD;

  @Autowired
  private CompareResponse compareResponse;

  private String responseExample;

  private String inputData;

  @Test
  void testCreateCR_BV_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"datasetId\":\"TestModel3\", \"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";
      /*
      201 Created TDD
      */
        ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());
        
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
    
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
    
      compareResponse.compareResponseBody(resultActions, responseExample);

    } finally {
      ResultActions resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNoContent())
        .andDo(print());
    
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCR_BV_02() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

    inputData =
      "{\"datasetId\":\"TestModel3\", \"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";
    /*
     201 Created TDD
    */
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/entities")
          .content(inputData)
          .contentType("application/ld+json")
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header("Content-Length", String.valueOf(inputData.length()))
      )
      .andExpect(status().isCreated())
      .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Post=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());
    
    responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
                
    compareResponse.compareResponseBody(resultActions, responseExample);
    } finally {
      ResultActions resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNoContent())
        .andDo(print());
        
    testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCR_BV_03() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
    
      inputData =
        "{\"datasetId\":\"TestModel3\", \"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";
      /*
        201 Created TDD
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());

      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");

      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
    } finally {
      ResultActions resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNoContent())
        .andDo(print());
        
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCR_BI_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
        
      inputData =
        "{\"datasetId\":\"TestModel3\", \"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true],\"observedAt\":\"2023-06-18T15:00:00.000Z\"},\"location\":{\"observedAt\": \"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12,-5]}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12,-5]}},\"testDoubleArray\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"objects\":{\"type\":\"Property\",\"value\":{\"interger\":5,\"boolean\":true,\"string\":\"test\",\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1}},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12,-5]},\"observedAt\":\"2023-06-18T15:00:00.000Z\"},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testString\":{\"type\":\"Property\",\"value\":\"test\"},\"type\":\"TestModel3\"}";
      /*
      201 Created TDD
      */        
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isBadRequest())
        .andDo(print());

      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"invalid key : testDoubleArray\"}";

      compareResponse.compareResponseBody(resultActions, responseExample);
      
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");

    } finally {
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCR_BI_02() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
      
      inputData =
        "{\"datasetId\":\"TestModel3\", \"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":\"invalid-string\",\"observedAt\":\"2023-06-18T15:00:00.000Z\"},\"location\":{\"observedAt\": \"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":\"invalid-string\"},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12,-5]}},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"objects\":{\"type\":\"Property\",\"value\":{\"interger\":5,\"boolean\":true,\"string\":\"test\",\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1}},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12,-5]},\"observedAt\":\"2023-06-18T15:00:00.000Z\"},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testString\":{\"type\":\"Property\",\"value\":\"test\"},\"type\":\"TestModel3\"}";
      /*
      201 Created TDD
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isBadRequest())
        .andDo(print());
      
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"fullVO to daoVO parsing ERROR. entityType=TestModel3, id=urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"}";
      
      compareResponse.compareResponseBody(resultActions, responseExample);
      
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");

    } finally {
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCR_BI_03() throws Exception {
    try {
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"datasetId\":\"TestModel3\", \"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true],\"observedAt\":\"2023-06-18T15:00:00.000Z\"},\"location\":{\"observedAt\": \"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":\"invalid-string\"},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12,-5]}},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"objects\":{\"type\":\"Property\",\"value\":{\"interger\":5,\"boolean\":true,\"string\":\"test\",\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1}},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12,-5]},\"observedAt\":\"2023-06-18T15:00:00.000Z\"},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testString\":{\"type\":\"Property\",\"value\":\"test\"},\"type\":\"TestModel3\"}";
      /*
      201 Created TDD
      */      
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isBadRequest())
        .andDo(print());
        
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"fullVO to daoVO parsing ERROR. entityType=TestModel3, id=urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"}";  
        
      compareResponse.compareResponseBody(resultActions, responseExample);

      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
    } finally {
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test

  void testCreateCA_BV_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
    
      inputData =
        "{\"datasetId\":\"TestModel3\", \"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";
      /*
       201 Created TDD
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());
        
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
      
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      inputData =
        "{\"datasetId\":\"TestModel3\", \"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
      /*
       204 No Content
      */    
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs"
              )
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isNoContent())
          .andDo(print());
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
  
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
    } finally {
      ResultActions resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNoContent())
        .andDo(print());
        
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCA_BV_02() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"datasetId\":\"TestModel3\", \"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";
      /*
       201 Created TDD
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());
        
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
              
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      inputData =
        "{\"datasetId\":\"TestModel3\", \"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
      /*
       204 No Content
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs"
              )
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isNoContent())
          .andDo(print());
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
  
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
    } finally {
      ResultActions resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNoContent())
        .andDo(print());
        
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCA_BV_03() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
    
      inputData =
        "{\"datasetId\":\"TestModel3\", \"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";
      /*
       201 Created TDD
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());
        
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      inputData =
        "{\"datasetId\":\"TestModel3\", \"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
      /*
       204 No Content
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs"
              )
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isNoContent())
          .andDo(print());
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
  
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
    } finally {
      ResultActions resultActions = mvc
          .perform(
              MockMvcRequestBuilders
                  .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
                  .contentType(MediaType.APPLICATION_JSON)
                  .accept(MediaType.APPLICATION_JSON)
                  .characterEncoding("utf-8"))
          .andExpect(status().isNoContent())
          .andDo(print());

      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCA_BI_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";

      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());
        
        resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());

      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";

      compareResponse.compareResponseBody(resultActions, responseExample);

      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":\"invalid-string\",\"observedAt\":\"2023-06-18T15:00:00.000Z\"},\"location\":{\"observedAt\": \"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12,-5]}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12,-5]}},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"objects\":{\"type\":\"Property\",\"value\":{\"interger\":5,\"boolean\":true,\"string\":\"test\",\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1}},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12,-5]},\"observedAt\":\"2023-06-18T15:00:00.000Z\"},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testString\":{\"type\":\"Property\",\"value\":\"test\"},\"type\":\"TestModel3\"}";
      /*
      201 Created TDD
      */   
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs"
              )
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isBadRequest())
          .andDo(print());
          
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"Invalid Attribute Type. attributeId=testArrayBoolean, valueType=ARRAY_BOOLEAN, value=invalid-string\"}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);

      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
        
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";

      compareResponse.compareResponseBody(resultActions, responseExample);
    } finally {
      ResultActions resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNoContent())
        .andDo(print());
        
    testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCA_BI_02() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
      
      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";

      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());

      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":\"invalid-string\",\"observedAt\":\"2023-06-18T15:00:00.000Z\"}}";
      /*
      201 Created TDD
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs"
              )
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isBadRequest())
          .andDo(print());
        
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"Invalid Attribute Type. attributeId=testArrayBoolean, valueType=ARRAY_BOOLEAN, value=invalid-string\"}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);

      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";

      compareResponse.compareResponseBody(resultActions, responseExample);
      
    } finally {
      ResultActions resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNoContent())
        .andDo(print());
        
      testModel3CRUD.deleteTestModel3();
    } 
  }

  @Test
  void testCreateCA_BI_03() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
      
      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";

      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());

      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\": \"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":\"invalid-string\"},\"type\":\"TestModel3\"}";
      
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs"
              )
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isBadRequest())
          .andDo(print());
          
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"fullVO to daoVO parsing ERROR. entityType=TestModel3, id=urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);

      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
      
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
    } finally {
      ResultActions resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNoContent())
        .andDo(print());

      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCU_BV_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";
      /*
      201 Created TDD
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());

      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
      
      compareResponse.compareResponseBody(resultActions, responseExample);

      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[true]},\"type\":\"TestModel3\"}";
      /*
        204 No Content
      */      
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .patch(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs"
              )
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isNoContent())
          .andDo(print());

      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");

      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);

    } finally {
      ResultActions resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNoContent())
        .andDo(print());
        
    testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCU_BV_02() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";
      /*
      201 Created TDD
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());
        
      resultActions =
        mvc
        .perform(
          MockMvcRequestBuilders
            .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());
        
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
    
      compareResponse.compareResponseBody(resultActions, responseExample);

      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"location\":{\"observedAt\": \"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12,-6]}},\"type\":\"TestModel3\"}";
      /*
        204 No Content
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .patch(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs"
              )
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isNoContent())
          .andDo(print());

      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");

      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-6.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";

      compareResponse.compareResponseBody(resultActions, responseExample);
    } finally {
      ResultActions resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNoContent())
        .andDo(print());
        
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCU_BV_03() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";
      /*
      201 Created TDD
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());

      resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());
        
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
        
      compareResponse.compareResponseBody(resultActions, responseExample);
        
      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"location\":{\"observedAt\": \"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12,-6]}},\"type\":\"TestModel3\"}";
      /*
      204 No Content
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .patch(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs"
              )
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isNoContent())
          .andDo(print());

      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");

      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-6.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
    } finally {
      ResultActions resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNoContent())
        .andDo(print());
        
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCU_BI_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";
      /*
      201 Created TDD
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());
        
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
      
      compareResponse.compareResponseBody(resultActions, responseExample);

      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"location\":{\"observedAt\": \"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":\"invalid-String\"},\"type\":\"TestModel3\"}";
      /*
      204 No Content
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .patch(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs"
              )
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isBadRequest())
          .andDo(print());

      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");

      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";

      compareResponse.compareResponseBody(resultActions, responseExample);
    } finally {
      ResultActions resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNoContent())
        .andDo(print());
        
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCU_BI_02() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
          
      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";
      /*
      201 Created TDD
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());
        
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
        
      compareResponse.compareResponseBody(resultActions, responseExample);

      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":\"invalid-Value\",\"observedAt\":\"2023-06-18T15:00:00.000Z\"},\"type\":\"TestModel3\"}";
      /*
        204 No Content
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .patch(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs"
              )
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isBadRequest())
          .andDo(print());

      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");

      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";

      compareResponse.compareResponseBody(resultActions, responseExample);
    } finally {
      ResultActions resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNoContent())
        .andDo(print());
        
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testCreateCU_BI_03() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
      
      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"objects\":{\"type\":\"Property\",\"value\":{\"boolean\":true,\"date\":\"2023-06-18T15:00:00.000Z\",\"double\":0.1,\"interger\":5,\"string\":\"test\"}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testArrayBoolean\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"Property\",\"value\":[false,true]},\"testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjString\":\"string\"},{\"testArrObjInteger\":10},{\"testArrObjDouble\":0.1},{\"testArrObjBoolean\":true},{\"testArrObjDate\":\"2023-11-17T20:48:09,290+09:00\"}]},\"testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"testBoolean\":{\"type\":\"Property\",\"value\":true},\"testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T20:48:09,290+09:00\"},\"testDouble\":{\"type\":\"Property\",\"value\":0.1},\"testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[-12,-5],\"type\":\"Point\"}},\"testInteger\":{\"type\":\"Property\",\"value\":10},\"testRelationshipString\":{\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\",\"type\":\"Relationship\"},\"testString\":{\"type\":\"Property\",\"value\":\"valuestring\"},\"type\":\"TestModel3\"}";
      /*
      201 Created TDD
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());

      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
      
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);

      inputData =
        "{\"datasetId\":\"TestModel3\",\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"location\":{\"observedAt\": \"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":\"invalid-String\"},\"type\":\"TestModel3\"}";
      /*
        204 No Content
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .patch(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs"
              )
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isBadRequest())
          .andDo(print());

      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");


      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"location\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"objects\":{\"type\":\"Property\",\"value\":{\"date\":\"2023-06-18T15:00:00.000Z\",\"boolean\":true,\"string\":\"test\",\"double\":0.1,\"interger\":5}},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\":{\"type\":\"Property\",\"value\":[0.0,1.1]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\":{\"type\":\"Property\",\"value\":[1,2]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\":{\"type\":\"Property\",\"value\":[{\"testArrObjInteger\":10,\"testArrObjDate\":\"2023-11-17T11:48:09.290Z\",\"testArrObjString\":\"string\",\"testArrObjDouble\":0.1,\"testArrObjBoolean\":true}]},\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\":{\"type\":\"Property\",\"value\":[\"test1\",\"test2\"]},\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\":{\"type\":\"Property\",\"value\":true},\"http://uri.citydatahub.kr/ngsi-ld/testDate\":{\"type\":\"Property\",\"value\":\"2023-11-17T11:48:09.290Z\"},\"http://uri.citydatahub.kr/ngsi-ld/testDouble\":{\"type\":\"Property\",\"value\":0.1},\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\":{\"observedAt\":\"2023-06-18T15:00:00.000Z\",\"type\":\"GeoProperty\",\"value\":{\"type\":\"Point\",\"coordinates\":[-12.0,-5.0]}},\"http://uri.citydatahub.kr/ngsi-ld/testInteger\":{\"type\":\"Property\",\"value\":10},\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\":{\"type\":\"Relationship\",\"object\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e1\"},\"http://uri.citydatahub.kr/ngsi-ld/testString\":{\"type\":\"Property\",\"value\":\"valuestring\"}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
    } finally {
      ResultActions resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNoContent())
        .andDo(print());
        
    testModel3CRUD.deleteTestModel3();
    }    
  }
}
