package kr.re.keti.sc.dataservicebroker.entities.controller.http;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.CompareResponse;
import kr.re.keti.sc.dataservicebroker.common.TestModel3CRUD;


import java.net.BindException;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
// import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
// import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
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
public class EntityControllerTest {

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
  void testCreate001_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
    
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
        
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

  /*
   * 409 Already exist
   */
  @Test
  void testCreate001_03() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
    
      inputData = "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
      
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
      
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entities")
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isConflict())
          .andDo(print());
          
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/AlreadyExists\",\"title\":\"Already Exists\",\"detail\":\"Invalid Request Content. Already exists entityId=urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testCreate001_04() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
      /*
      201 Created TDD
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
            .header(
              "Link",
              "<http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld>,<https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld>"
            )
            .header("rel", "http://uri.citydatahub.kr/ngsi-ld/TestModel3")
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";

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
  void testCreate001_06() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
    
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}";
      /*
      400 Bad Request TDD
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities")
            .content(inputData)
            .contentType("application/json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isBadRequest())
        .andDo(print());
        
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"Invalid Request Content. @context parameter cannot be used when contentType=application/json\"}";
        
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
  void testCreate001_07() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
    
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}";
      /*
      201 Bad Request TDD
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
          
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
  void testCreate001_08() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}";
      /*
      400 Bad Request TDD
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
        
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"Invalid Request Content. No match attribute full uri. attribute name=testArrayBoolean, dataModel attribute uri=http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean but ingest attribute uri=null\"}";
        
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
  void testCreate001_09() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
      
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}";
      /*
      400 Bad Request TDD
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
            .header(
              "Link",
              "<http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld>,<https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld>"
            )
        )
        .andExpect(status().isBadRequest())
        .andDo(print());
        
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"Invalid Request Content. Link Header cannot be used when contentType=application/ld+json\"}";
        
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
  void testDelete002_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}";
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";

      compareResponse.compareResponseBody(resultActions, responseExample);
    } finally {
      /*
      204 No Content
      */
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

      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Delete=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");

      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testDelete002_02() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
      
      /*
      404 No Content
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/thisisaninval")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isBadRequest())
        .andDo(print());
        
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"Invalid Request Content. entityId is not in URN format. id=thisisaninval\"}";
        
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Delete=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
    } finally {
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testDelete002_03() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
      
      /*
      404 No Content
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
        )
        .andExpect(status().isNotFound())
        .andDo(print());
        
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/ResourceNotFound\",\"title\":\"Resource Not Found\",\"detail\":\"Invalid Request Content. Not exists entityId=urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"}";
        
      compareResponse.compareResponseBody(resultActions, responseExample);

      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Delete=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
    } finally {
      testModel3CRUD.deleteTestModel3(); 
    }
  }

  @Test
  void testBatchEntityCreation003_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "[{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}]";
      /*
        201 No Content
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entityOperations/create")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());
        
      responseExample = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";

      compareResponse.comparePostBody(resultActions, responseExample);

      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
  
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities")
              .accept(MediaType.APPLICATION_JSON)
              .param("id", "urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "[{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}]";
  
      compareResponse.compareListResponseBody(resultActions, responseExample);
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
  void testBatchEntityCreation003_04() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "[{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}]";
      /*
        204 No Content
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entityOperations/create")
            .content(inputData)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
            .header(
              "Link",
              "<http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld>,<https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld>"
            )
        )
        .andExpect(status().isCreated())
        .andDo(print());
        
      responseExample = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";

      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");

      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities")
              .accept(MediaType.APPLICATION_JSON)
              .param("id", "urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "[{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}]";
          
      compareResponse.compareListResponseBody(resultActions, responseExample);
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
  void testBatchEntityCreation003_07() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "[{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}]";
      /*
        201 No Content
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entityOperations/create")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());
        
      responseExample = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";
        
      compareResponse.comparePostBody(resultActions, responseExample);

      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");

      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities")
              .accept(MediaType.APPLICATION_JSON)
              .param("id", "urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "[{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}]";
          
      compareResponse.compareListResponseBody(resultActions, responseExample);

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
  void testBatchEntityUpsert004_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData = "[{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}]";
      /*
      201 No Content
      */
      ResultActions resultActions = mvc
          .perform(
              MockMvcRequestBuilders
                  .post("/entityOperations/upsert")
                  .content(inputData)
                  .contentType("application/ld+json")
                  .accept(MediaType.APPLICATION_JSON)
                  .characterEncoding("utf-8")
                  .header("Content-Length", String.valueOf(inputData.length()))
                  .param("options", "replace"))
          .andExpect(status().isCreated())
          .andDo(print());

      responseExample = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";

      compareResponse.comparePostBody(resultActions, responseExample);

      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");

      resultActions = mvc
          .perform(
              MockMvcRequestBuilders
                  .get("/entities")
                  .accept(MediaType.APPLICATION_JSON)
                  .param("id", "urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e"))
          .andExpect(status().isOk())
          .andDo(print());

      responseExample = "[{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}]";

      compareResponse.compareListResponseBody(resultActions, responseExample);

      /*
      204 No Content
      */
      String body = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";

      resultActions = mvc
          .perform(
              MockMvcRequestBuilders
                  .post("/entityOperations/delete")
                  .content(body)
                  .contentType(MediaType.APPLICATION_JSON)
                  .accept(MediaType.APPLICATION_JSON)
                  .characterEncoding("utf-8")
                  .header("Content-Length", String.valueOf(body.length())))
          .andExpect(status().isNoContent())
          .andDo(print());
    } finally {
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testBatchEntityUpsert004_02() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      //entity create
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
      
      compareResponse.compareResponseBody(resultActions, responseExample);
        
      //if existing then update
      inputData =
        "[{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}]";
      /*
      204 No Content
    */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/upsert")
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
      
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      String body = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";
      /*
      204 No Content
     */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/delete")
              .content(body)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(body.length()))
          )
          .andExpect(status().isNoContent())
          //.andExpect(content().string("{\"id\":\"TDD\"}"))
          .andDo(print());
  
      // if not existing then create
      inputData =
        "[{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}]";
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/upsert")
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isCreated())
          .andDo(print());
          
      responseExample = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";
          
      compareResponse.comparePostBody(resultActions, responseExample);
  
      mvcResult = resultActions.andReturn();
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      body = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";
      /*
      204 No Content
     */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/delete")
              .content(body)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(body.length()))
          )
          .andExpect(status().isNoContent())
          .andDo(print());
    } finally {
      testModel3CRUD.deleteTestModel3();      
    }
  }

  @Test
  void testBatchEntityUpsert004_03() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      //entity create
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      //if existing then update
      inputData =
        "[{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[true,true]},\"type\":\"TestModel3\"}]";
  
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/upsert")
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[true,true]}}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      String body = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/delete")
              .content(body)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(body.length()))
          )
          .andExpect(status().isNoContent())
          .andDo(print());
    } finally {
      testModel3CRUD.deleteTestModel3();     
    }
  }

  @Test
  void testBatchEntityUpsert004_04() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      //entity create
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
        
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      //if existing then update
      inputData =
        "[{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[true,true]},\"type\":\"TestModel3\"}]";
      /*
      204 No Content
    */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/upsert")
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .param("option", "update")
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
        
        responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[true,true]}}";
        
        compareResponse.compareResponseBody(resultActions, responseExample);   
  
      String body = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/delete")
              .content(body)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(body.length()))
          )
          .andExpect(status().isNoContent())
          .andDo(print());
    } finally {
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testBatchEntityUpdate005_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
  
          /* 동시성 문제가 간헐적으로 error로 인해 스레드 대기 */
          Thread.sleep(7000);
  
          responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
          
          compareResponse.compareResponseBody(resultActions, responseExample);
  
      inputData =
        "[{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[true,true]}}]";
      /*
      204 No Content
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/update")
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
              .param("options", "overwrite")
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[true,true]}}";
       
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      String body = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/delete")
              .content(body)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(body.length()))
          )
          .andExpect(status().isNoContent())
          .andDo(print());
    } finally {
      testModel3CRUD.deleteTestModel3(); 
    }
  }

  @Test
  void testBatchEntityUpdate005_02() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
        
      compareResponse.compareResponseBody(resultActions, responseExample); 
  
      inputData =
        "[{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[true,true]}}]";
       /*
      204 No Content
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/update")
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
              .param("option", "NoOverwrite")
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
  
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[true,true]}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      String body = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";
      /*
      204 No Content
     */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/delete")
              .content(body)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(body.length()))
          )
          .andExpect(status().isNoContent())
          .andDo(print());
    } finally {
      testModel3CRUD.deleteTestModel3();  
    }
  }

  @Test
  void testBatchEntityUpdate005_03() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      String inputData =
        "[{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}]";
      /*
      207 No content
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entityOperations/update")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
            .param("option", "Overwrite")
        )
        .andExpect(status().isMultiStatus())
        .andDo(print());
  
       // failed
      responseExample = "{\"errors\":[{\"entityId\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"error\":{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/ResourceNotFound\",\"title\":\"Resource Not Found\",\"detail\":\"Invalid Request Content. Not exists entityId=urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"}}]}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
       
      // succeed
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
      resultActions =
        mvc
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      inputData =
        "[{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[true,true]}}]";
      /*
      204 No Content
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/update")
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
              .param("option", "NoOverwrite")
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[true,true]}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      String body = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";
      /*
      204 No Content
     */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/delete")
              .content(body)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(body.length()))
          )
          .andExpect(status().isNoContent())
          .andDo(print());
    } finally {
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testBatchEntityDelete006_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "[{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}]";
      /*
         201 No Content
      */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entityOperations/create")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());
        
      responseExample = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";
      
      compareResponse.comparePostBody(resultActions, responseExample);
  
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
  
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
        
      String body = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/delete")
              .content(body)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(body.length()))
          )
          .andExpect(status().isNoContent())
          .andDo(print());
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
    } finally {
      testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testBatchEntityDelete006_02() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      //fail
      String body = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";
      /*
      207 No Content
     */
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entityOperations/delete")
            .content(body)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(body.length()))
        )
        .andExpect(status().isMultiStatus())
        .andDo(print());
        
      responseExample = "{\"errors\":[{\"entityId\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"error\":{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/ResourceNotFound\",\"title\":\"Resource Not Found\",\"detail\":\"Invalid Request Content. Not exists entityId=urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"}}]}";
        
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      inputData =
        "[{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}]";
      /*
         201 No Content
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/create")
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isCreated())
          .andDo(print());
          
      responseExample = "[\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\"]";
  
      compareResponse.comparePostBody(resultActions, responseExample);
  
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
          
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .post("/entityOperations/delete")
              .content(body)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(body.length()))
          )
          .andExpect(status().isNoContent())
          .andDo(print());
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
    } finally {
      testModel3CRUD.deleteTestModel3();    
    }
  }

  @Test
  void testAppendEntityAttributes010_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
     
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\", \"datasetId\":\"TestModel3\"}";
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
      
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\"}";

      compareResponse.compareResponseBody(resultActions, responseExample);

      /*
       204 No Content
      */
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      resultActions =
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
    } finally {
      testModel3CRUD.deleteTestModel3();    
    }
  }

  @Test
  void testAppendEntityAttributes010_02() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
    
      /*
      204 No Content
      */
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}";
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities/thisisaninvaliduri/attrs")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isBadRequest())
        .andDo(print());
        
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"Invalid Request Content. entityId is not in URN format. id=thisisaninvaliduri\"}";
        
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
  void testAppendEntityAttributes010_03() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      /*
       404 No Content
      */
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}";
      ResultActions resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .post("/entities/urn:datahub:TestModel/attrs")
            .content(inputData)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isNotFound())
        .andDo(print());
        
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/ResourceNotFound\",\"title\":\"Resource Not Found\",\"detail\":\"Invalid Request Content. Not exists entityId=urn:datahub:TestModel\"}";
        
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
  
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\", \"datasetId\":\"TestModel3\"}";
      resultActions =
        mvc
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
      /*
       400 No Content attribute
      */
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testinvalid\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}";
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
          
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"invalid key : testinvalid\"}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");

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
  void testUpdateEntityAttributes011_01() throws Exception {
    try {
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\", \"datasetId\":\"TestModel3\"}";
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[true,true]},\"type\":\"TestModel3\"}";
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[true,true]}}";
  
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
  void testUpdateEntityAttributes011_02() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\", \"datasetId\":\"TestModel3\"}";
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
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testinvalidattr\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}";
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isOk())
          .andDo(print());
      
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
      /*
      204 No Content
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .patch("/entities/invalidurl/attrs")
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isBadRequest())
          .andDo(print());
          
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"Invalid Request Content. entityId is not in URN format. id=invalidurl\"}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testUpdateEntityAttributes011_03() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
    
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\", \"datasetId\":\"TestModel3\"}";
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testinvalidattr\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}";
      /*
      400 bad Request invalid attr
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
          
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"invalid key : testinvalidattr\"}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
  
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}";
      /*
      400 bad request invalid id
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .patch("/entities/invalidid/attrs")
              .content(inputData)
              .contentType("application/ld+json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
          )
          .andExpect(status().isBadRequest())
          .andDo(print());
          
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"Invalid Request Content. entityId is not in URN format. id=invalidid\"}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      mvcResult = resultActions.andReturn();
      System.out.println("=====================Post=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testPartialUpdateWithAttrId012_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[true,true]}}";
      /*
      204 No Content
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .patch(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs/testArrayBoolean"
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[true,true]}}";
  
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
  void testPartialUpdateWithAttrId012_02() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
    
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
          
          responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
        
          compareResponse.compareResponseBody(resultActions, responseExample);
  
      inputData =
        "{\"testinvalidattr\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\"}";
      /*
      204 No Content
      */
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .patch(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs/testArrayBoolean"
              )
              .content(inputData)
              .contentType("application/json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
              .header(
                "Link",
                "<http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld>,<https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld>"
              )
          )
          .andExpect(status().isBadRequest())
          .andDo(print());
          
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"invalid key : testinvalidattr\"}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
  
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
  
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
          
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
  void testPartialUpdateWithAttrId012_03() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
    
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      inputData =
        "{\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\"}";
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .patch(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e_testinvalid/attrs/testArrayBoolean"
              )
              .content(inputData)
              .contentType("application/json")
              .accept(MediaType.APPLICATION_JSON)
              .characterEncoding("utf-8")
              .header("Content-Length", String.valueOf(inputData.length()))
              .header(
                "Link",
                "<http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld>,<https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld>"
              )
          )
          .andExpect(status().isNotFound())
          .andDo(print());
          
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/ResourceNotFound\",\"title\":\"Resource Not Found\",\"detail\":\"Invalid Request Content. Not exists entityId=urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e_testinvalid\"}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
          
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
          
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
  void testDeleteAttr013_02() throws Exception {
    if (datastorage.equals("rdb")) {
      try{
        testModel3CRUD.createTestModel3();

        inputData =
          "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
            
        responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
  
        compareResponse.compareResponseBody(resultActions, responseExample);
  
        resultActions =
          mvc
            .perform(
              MockMvcRequestBuilders
                .delete("/entities/invalidurl/attrs/testArrayBoolean")
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
            )
            .andExpect(status().isBadRequest())
            .andDo(print());
            
        responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"Invalid Request Content. entityId is not in URN format. id=invalidurl\"}";
            
        compareResponse.compareResponseBody(resultActions, responseExample);
  
        MvcResult mvcResult = resultActions.andReturn();
        System.out.println("=====================Post=====================");
        System.out.println(mvcResult.getResponse().getContentAsString());
        System.out.println("=====================End=====================");

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

  @Test
  // @ConditionalOnExpression(datastorage.equals())
  void testDeleteAttr013_03() throws Exception {
    if (datastorage.equals("rdb")) {
      try{
        testModel3CRUD.createTestModel3();
        
        inputData =
            "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
              
          responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
  
          compareResponse.compareResponseBody(resultActions, responseExample);
      
          resultActions =
            mvc
              .perform(
                MockMvcRequestBuilders
                  .delete(
                    "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e_test/attrs/testArrayBoolean"
                  )
                  .contentType("application/ld+json")
                  .accept(MediaType.APPLICATION_JSON)
                  .characterEncoding("utf-8")
              )
              .andExpect(status().isNotFound())
              .andDo(print());
              
          responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/ResourceNotFound\",\"title\":\"Resource Not Found\",\"detail\":\"Invalid Request Content. Not exists entityId=urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e_test\"}";
  
          compareResponse.compareResponseBody(resultActions, responseExample);
      
          MvcResult mvcResult = resultActions.andReturn();
          System.out.println("=====================Post=====================");
          System.out.println(mvcResult.getResponse().getContentAsString());
          System.out.println("=====================End=====================");
      
          resultActions =
            mvc
              .perform(
                MockMvcRequestBuilders
                  .delete(
                    "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e/attrs/testArrayBooleanNotknown"
                  )
                  .contentType("application/ld+json")
                  .accept(MediaType.APPLICATION_JSON)
                  .characterEncoding("utf-8")
              )
              .andExpect(status().isNotFound())
              .andDo(print());
            
          responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/ResourceNotFound\",\"title\":\"Resource Not Found\",\"detail\":\"Not exists Entity Attribute. entityId=urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e, attrId=testArrayBooleanNotknown\"}";
              
          compareResponse.compareResponseBody(resultActions, responseExample);
      
          mvcResult = resultActions.andReturn();
          System.out.println("=====================Post=====================");
          System.out.println(mvcResult.getResponse().getContentAsString());
          System.out.println("=====================End=====================");
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

  @Test
  void testGetEntityById018_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      /*
      201 Created TDD
      */
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testGetEntityById018_02() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
 
      /*
      201 Created TDD
      */
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
              .get("/entities/invalidurl")
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isBadRequest())
          .andDo(print());
          
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"Invalid request parameter. entityId is not in URN format. id=invalidurl\"}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testGetEntityById018_03() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      /*
      201 Created TDD
      */
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
              .get(
                "/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e_test"
              )
              .accept(MediaType.APPLICATION_JSON)
          )
          .andExpect(status().isNotFound())
          .andDo(print());
          
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/ResourceNotFound\",\"title\":\"Resource Not Found\",\"detail\":\"There is no Entity instance with the requested identifier.\"}";
        
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testGetEntityById018_04() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      /*
      201 Created TDD
      */
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
              .param("options", "keyValues")
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample ="{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":[false,true]}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);   
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testGetEntityById018_06() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      /*
      201 Created TDD
      */
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
        
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
  
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/entities/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .accept(MediaType.APPLICATION_JSON)
              .header(
                "Link",
                "<http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld>,<https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld>"
              )
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testGetEntityById019_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
  
      /*
      201 Created TDD
      */
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
              .get("/entities")
              .accept(MediaType.APPLICATION_JSON)
              .param("id", "urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              // .param("attrs", "testArrayBoolean")
              .contentType("application/ld+json")
              .header(
                "Link",
                "<http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld>,<https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld>"
              )
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "[{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}]";
          
      compareResponse.compareListResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testGetEntityById019_03() throws Exception {
    try{
      testModel3CRUD.createTestModel3();
      /*
      201 Created TDD
      */
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
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
              .get("/entities")
              .accept(MediaType.APPLICATION_JSON)
              .param("id", "invalidurl")
              .param("attrs", "testArrayBoolean")
              .contentType("application/ld+json")
              .header(
                "Link",
                "<http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld>,<https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld>"
              )
          )
          .andExpect(status().isBadRequest())
          .andDo(print());
          
      responseExample = "{\"type\":\"https://uri.etsi.org/ngsi-ld/errors/BadRequestData\",\"title\":\"Bad request data\",\"detail\":\"Invalid request parameter. entityId is not in URN format. id=invalidurl\"}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testGetEntityById019_04() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      /*
      201 Created TDD
      */
      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
  
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
              .get("/entities")
              .accept(MediaType.APPLICATION_JSON)
              .param("options", "keyValues")
              .param("id", "urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e")
              .contentType("application/json")
          )
          .andExpect(status().isOk())
          .andDo(print());
  
      responseExample ="[{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":[false,true]}]";
          
      compareResponse.compareListResponseBody(resultActions, responseExample);
              
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testGetEntityTypes022_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
  
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
              
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/types")
              .accept(MediaType.APPLICATION_JSON)
              .contentType("application/json")
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "{\"id\":\"urn:ngsi-ld:EntityTypeList:37418953\",\"type\":\"EntityTypeList\",\"typeList\":[\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\"]}";
          
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testGetEntityTypes023_01() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
  
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
          
      responseExample = "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      resultActions =
        mvc
          .perform(
            MockMvcRequestBuilders
              .get("/types")
              .accept(MediaType.APPLICATION_JSON)
              .contentType("application/json")
              .param("details", "True")
          )
          .andExpect(status().isOk())
          .andDo(print());
          
      responseExample = "[{\"id\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"type\":\"EntityType\",\"typeName\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"attributeNames\":[\"https://uri.etsi.org/ngsi-ld/location\",\"https://uri.etsi.org/ngsi-ld/hasObjects\",\"https://uri.etsi.org/ngsi-ld/observationSpace\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\",\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\",\"http://uri.citydatahub.kr/ngsi-ld/testDate\",\"http://uri.citydatahub.kr/ngsi-ld/testDouble\",\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\",\"http://uri.citydatahub.kr/ngsi-ld/testInteger\",\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\",\"http://uri.citydatahub.kr/ngsi-ld/testString\"]}]";
  
      compareResponse.compareListResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testGetEntityCount() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
        "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
  
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
  
      resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .get("/entitycount")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());
        
      responseExample = "{\"totalCount\":1}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testGetEntity() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
      "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
  
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
        
      resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .get("/entities")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());
        
      responseExample = "[{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]}}]";
  
      compareResponse.compareListResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
  void testGetAttribute() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
      "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
  
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
      
      resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .get("/attributes")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());
        
      responseExample = "{\"id\":\"urn:ngsi-ld:AttributeList:58208329\",\"type\":\"AttributeList\",\"attributeList\":[\"http://uri.citydatahub.kr/ngsi-ld/testArrayInteger\",\"http://uri.citydatahub.kr/ngsi-ld/testRelationshipString\",\"https://uri.etsi.org/ngsi-ld/observationSpace\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayString\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayDouble\",\"http://uri.citydatahub.kr/ngsi-ld/testInteger\",\"http://uri.citydatahub.kr/ngsi-ld/testGeoJson\",\"http://uri.citydatahub.kr/ngsi-ld/testDate\",\"http://uri.citydatahub.kr/ngsi-ld/testString\",\"http://uri.citydatahub.kr/ngsi-ld/testBoolean\",\"https://uri.etsi.org/ngsi-ld/location\",\"http://uri.citydatahub.kr/ngsi-ld/testDouble\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayBoolean\",\"http://uri.citydatahub.kr/ngsi-ld/testArrayObject\",\"https://uri.etsi.org/ngsi-ld/hasObjects\"]}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
        
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");


        
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
      // testModel3CRUD.deleteTestModel3();
    }
  }

  @Test
  void testGetAttributeInformation() throws Exception {
    try{
      testModel3CRUD.createTestModel3();

      inputData =
      "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"datasetId\":\"TestModel3\",\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
  
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
  
      resultActions = mvc
        .perform(
          MockMvcRequestBuilders
            .get("/attributes/urn:ngsi-ld:AttributeList:58208329")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());
        
      responseExample = "{\"attributeCount\":0}";
  
      compareResponse.compareResponseBody(resultActions, responseExample);
  
      MvcResult mvcResult = resultActions.andReturn();
      System.out.println("=====================Query=====================");
      System.out.println(mvcResult.getResponse().getContentAsString());
      System.out.println("=====================End=====================");
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
