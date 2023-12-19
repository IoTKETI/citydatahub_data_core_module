package kr.re.keti.sc.dataservicebroker.csource.controller;

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
public class CsourceRegistrationControllerTest {

  @Autowired
  private MockMvc mvc;

  String inputData =
    "{\"context\":\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"description\":\"ForTTD\",\"endpoint\":\"http://my.csource.org:1026\",\"expires\":\"9999-11-17T20:48:09,290+09:00\",\"id\":\"TDD\",\"information\":[{\"entities\":[{\"id\":\"urn:datahub:OffStreetParking:yatap_01\",\"type\":\"http://kr.citydatahub.OffStreetParking:1.0\"}],\"properties\":[[\"address\",\"streedAddress\"]],\"relationships\":[[\"inAccident\"]]}]}";
  String inputData_update =
    "{\"context\":\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"description\":\"ForTTDUpdate\",\"endpoint\":\"http://my.csource.org:1026\",\"expires\":\"9999-11-17T20:48:09,290+09:00\",\"id\":\"TDD\",\"information\":[{\"entities\":[{\"id\":\"urn:datahub:OffStreetParking:yatap_01\",\"type\":\"http://kr.citydatahub.OffStreetParking:1.0\"}],\"properties\":[[\"address\",\"streedAddress\"]],\"relationships\":[[\"inAccident\"]]}]}";
  String query_response_compare =
    "[{\"id\":\"TDD\",\"type\":\"ContextSourceRegistration\",\"description\":\"ForTTD\",\"information\":[{\"entities\":[{\"id\":\"urn:datahub:OffStreetParking:yatap_01\",\"type\":\"http://kr.citydatahub.OffStreetParking:1.0\"}]}],\"endpoint\":\"http://my.csource.org:1026\"}]";
  String retrieve_response_compare =
    "{\"id\":\"TDD\",\"type\":\"ContextSourceRegistration\",\"description\":\"ForTTDUpdate\",\"information\":[{\"entities\":[{\"id\":\"urn:datahub:OffStreetParking:yatap_01\",\"type\":\"http://kr.citydatahub.OffStreetParking:1.0\"}]}],\"endpoint\":\"http://my.csource.org:1026\"}";
  String error_inputData =
    "{\"context\":\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"description\":\"For TTD\"";

  @Test
  void testRegisterContextSource() throws Exception {
    /*
     201 Created TDD
*/
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/csourceRegistrations")
          .content(inputData)
          .contentType(MediaType.APPLICATION_JSON)
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
            .get("/csourceRegistrations/TDD")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/csourceRegistrations/TDD")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  void testQueryContextSourceRegistrations() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/csourceRegistrations")
          .content(inputData)
          .contentType(MediaType.APPLICATION_JSON)
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
            .get("/csourceRegistrations/TDD")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .get("/csourceRegistrations")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Query=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/csourceRegistrations/TDD")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  void testUpdateContextSource() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/csourceRegistrations")
          .content(inputData)
          .contentType(MediaType.APPLICATION_JSON)
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
            .get("/csourceRegistrations/TDD")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .patch("/csourceRegistrations/TDD")
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
            .delete("/csourceRegistrations/TDD")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  void testRetrieveContextSourceRegistration() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/csourceRegistrations")
          .content(inputData)
          .contentType(MediaType.APPLICATION_JSON)
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
            .get("/csourceRegistrations/TDD")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Retrieve=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");

    mvc
      .perform(
        MockMvcRequestBuilders
          .delete("/csourceRegistrations/TDD")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andDo(print());
  }

  @Test
  void testDeleteContextSource() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/csourceRegistrations")
          .content(inputData)
          .contentType(MediaType.APPLICATION_JSON)
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
            .get("/csourceRegistrations/TDD")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete("/csourceRegistrations/TDD")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Delete=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");
  }
}
