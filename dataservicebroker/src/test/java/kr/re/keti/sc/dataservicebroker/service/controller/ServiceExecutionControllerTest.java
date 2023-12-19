package kr.re.keti.sc.dataservicebroker.service.controller;

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
public class ServiceExecutionControllerTest {

  @Autowired
  private MockMvc mvc;

  @Test
  void testServiceExecution() throws Exception {
    String inputData2 =
      "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"ServiceRegistration\",\"name\":\"testname\",\"description\":\"testdescription\",\"information\":[{\"services\":[{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"name\":\"nametest\",\"type\":\"typetest\",\"idPattern\":\"test\",\"endpoint\":\"test\",\"input\":{\"type\":\"TestModel3\",\"attribs\":[{\"attribname\":\"testArrayBoolean\",\"datatype\":\"Property\"}]},\"output\":{\"type\":\"test\",\"attribs\":[{\"attribname\":\"test\",\"datatype\":\"test\"}]}}]}]}";
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/serviceRegistry")
          .content(inputData2)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header("Content-Length", String.valueOf(inputData2.length()))
      )
      .andExpect(status().isOk())
      .andDo(print());

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .get(
              "/serviceRegistry/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e"
            )
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());

    String inputData =
      "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";

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

    /*
         200 Created TDD
    */
    inputData =
      "{\"executions\":[{\"entityId\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"entityType\":\"TestModel3\",\"id\":\"testid\",\"input\":{\"attribs\":[{\"attribname\":\"testArrayBoolean\",\"datatype\":\"Property\"}],\"type\":\"inputtypetest\"},\"name\":\"TestModel3\",\"type\":\"TestModel3\"}],\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"serviceRegistrationId\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"ServiceRegistration\"}";

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .post("/service/execute")
            .content(inputData)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData.length()))
        )
        .andExpect(status().isOk())
        .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Post=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete(
              "/serviceRegistry/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e"
            )
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent())
        .andDo(print());

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
  }

  @Test
  void testRetrieveServiceExecutionStatus() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .get(
            "/service/execute/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e"
          )
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Retrieve=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");
  }
}
