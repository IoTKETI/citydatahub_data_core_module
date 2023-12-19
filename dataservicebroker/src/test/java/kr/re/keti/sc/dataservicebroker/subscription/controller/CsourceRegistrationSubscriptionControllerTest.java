package kr.re.keti.sc.dataservicebroker.subscription.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WebAppConfiguration
public class CsourceRegistrationSubscriptionControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper mapper;

  String inputData =
    //"{\"csf\":\"string\",\"description\":\"string\",\"datasetIds\":[\"JangLogSet\"],\"entities\":[{\"id\":\"urn:datahub:OffStreetParking:yatap_01\",\"type\":\"http://kr.citydatahub.OffStreetParking:1.0\"}],\"expires\":\"9999-11-15T20:10:00.000+09:00\",\"geoQ\":{\"georel\":\"near;maxDistance==2000\"},\"id\":\"urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription\",\"isActive\":true,\"name\":\"string\",\"notification\":{\"attributes\":[\"http://speed\"],\"endpoint\":{\"accept\":\"application/json\",\"uri\":\"http://my.endpoint.org/notify\"},\"format\":\"normalized\"},\"q\":\"string\",\"temporalQ\":{\"timerel\":\"after\",\"time\":\"9999-11-15T20:10:00.000+09:00\",\"endTime\":\"9999-11-15T20:10:00.000+09:00\",\"timeproperty\":\"String\"},\"throttling\":1,\"type\":\"Subscription\",\"watchedAttributes\":[\"http://address\"]}";
    "{\"csf\":\"string\",\"datasetIds\":[\"TestModel3\"],\"description\":\"string\",\"entities\":[{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"http://uri.citydatahub.kr/ngsi-ld/TestModel3\"}],\"expires\":\"2023-11-15T20:10:00.000+09:00\",\"geoQ\":{\"georel\":\"near;maxDistance==2000\"},\"id\":\"urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscriptiontest\",\"isActive\":true,\"name\":\"string\",\"notification\":{\"attributes\":[\"http://speed\"],\"endpoint\":{\"accept\":\"application/json\",\"uri\":\"http://my.endpoint.org/notify\"},\"format\":\"normalized\"},\"q\":\"string\",\"temporalQ\":{\"endTime\":\"2023-11-15T20:10:00.000+09:00\",\"time\":\"2023-11-15T20:10:00.000+09:00\",\"timeproperty\":\"String\",\"timerel\":\"after\"},\"throttling\":1,\"type\":\"Subscription\",\"watchedAttributes\":[\"http://address\"]}";
  String inputData_update =
    "{\"csf\":\"string\",\"description\":\"string\",\"datasetIds\":[\"JangLogSet\"],\"entities\":[{\"id\":\"urn:datahub:OffStreetParking:yatap_01\",\"type\":\"http://kr.citydatahub.OffStreetParking:1.0\"}],\"expires\":\"9999-11-15T20:10:00.000+09:00\",\"geoQ\":{\"georel\":\"near;maxDistance==2000\"},\"id\":\"urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription\",\"isActive\":true,\"name\":\"stringUpdate\",\"notification\":{\"attributes\":[\"http://speed\"],\"endpoint\":{\"accept\":\"application/json\",\"uri\":\"http://my.endpoint.org/notify\"},\"format\":\"normalized\"},\"q\":\"string\",\"temporalQ\":{\"timerel\":\"after\",\"time\":\"9999-11-15T20:10:00.000+09:00\",\"endTime\":\"9999-11-15T20:10:00.000+09:00\",\"timeproperty\":\"String\"},\"throttling\":1,\"type\":\"Subscription\",\"watchedAttributes\":[\"http://address\"]}";

  @Test
  void testCreateContextSourceRegistrationSubscription() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/csourceSubscriptions")
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
            .get(
              "/csourceSubscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscriptiontest"
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
              "/csourceSubscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscriptiontest"
            )
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent())
        .andDo(print());
  }

  @Test
  void testQuerySubscriptions() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/csourceSubscriptions")
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
            .get(
              "/csourceSubscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscriptiontest"
            )
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .get("/csourceSubscriptions")
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
            .delete(
              "/csourceSubscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscriptiontest"
            )
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent())
        .andDo(print());
  }

  @Test
  void testUpdateSubscription() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .patch(
            "/csourceSubscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription"
          )
          .content(inputData_update)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header("Content-Length", String.valueOf(inputData_update.length()))
      )
      .andExpect(status().isOk())
      .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Post=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");
  }

  @Test
  void testRetrieveSubscription() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/csourceSubscriptions")
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
            .get(
              "/csourceSubscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscriptiontest"
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
              "/csourceSubscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscriptiontest"
            )
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent())
        .andDo(print());
  }

  @Test
  void testDelete() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/csourceSubscriptions")
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
            .get(
              "/csourceSubscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscriptiontest"
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
              "/csourceSubscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscriptiontest"
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
