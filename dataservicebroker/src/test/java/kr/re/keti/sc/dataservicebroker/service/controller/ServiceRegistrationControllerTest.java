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
public class ServiceRegistrationControllerTest {

  @Autowired
  private MockMvc mvc;

  String inputData =
    "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"ServiceRegistration\",\"name\":\"testname\",\"description\":\"testdescription\",\"information\":[{\"services\":[{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"name\":\"nametest\",\"type\":\"typetest\",\"idPattern\":\"test\",\"endpoint\":\"test\",\"input\":{\"type\":\"test\",\"attribs\":[{\"attribname\":\"test\",\"datatype\":\"test\"}]},\"output\":{\"type\":\"test\",\"attribs\":[{\"attribname\":\"test\",\"datatype\":\"test\"}]}}]}]}";
  String inputData_update =
    "{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"ServiceRegistration\",\"name\":\"testname\",\"description\":\"testdescription_update\",\"information\":[{\"services\":[{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"name\":\"nametest\",\"type\":\"typetest\",\"idPattern\":\"test\",\"endpoint\":\"test\",\"input\":{\"type\":\"test\",\"attribs\":[{\"attribname\":\"test\",\"datatype\":\"test\"}]},\"output\":{\"type\":\"test\",\"attribs\":[{\"attribname\":\"test\",\"datatype\":\"test\"}]}}]}]}";

  @Test
  void testCreateService() throws Exception {
    /*
     201 Created TDD
*/
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/serviceRegistry")
          .content(inputData)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header("Content-Length", String.valueOf(inputData.length()))
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
  }

  @Test
  void testRetrieveServices() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/serviceRegistry")
          .content(inputData)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header("Content-Length", String.valueOf(inputData.length()))
      )
      .andExpect(status().isOk())
      .andDo(print());

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .get("/serviceRegistry")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Retrieve=====================");
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
  }

  @Test
  void testUpdateService() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/serviceRegistry")
          .content(inputData)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header("Content-Length", String.valueOf(inputData.length()))
      )
      .andExpect(status().isOk())
      .andDo(print());

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .get("/serviceRegistry")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .put(
              "/serviceRegistry/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e"
            )
            .content(inputData_update)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .header("Content-Length", String.valueOf(inputData_update.length()))
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
            .delete(
              "/serviceRegistry/urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e"
            )
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent())
        .andDo(print());
  }

  @Test
  void testRetrieveService() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/serviceRegistry")
          .content(inputData)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header("Content-Length", String.valueOf(inputData.length()))
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

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Retrieve=====================");
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
  }

  @Test
  void testDeleteService() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/serviceRegistry")
          .content(inputData)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header("Content-Length", String.valueOf(inputData.length()))
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

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Delete=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");
  }
}
