package kr.re.keti.sc.dataservicebroker.subscription.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
public class SubscriptionControllerTest {

  @Autowired
  private MockMvc mvc;

  String inputData =
    "{\"id\":\"urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription\",\"type\":\"Subscription\",\"entities\":[{\"type\":\"TestModel3\"}],\"notification\":{\"attributes\":[\"http://speed\"],\"endpoint\":{\"accept\":\"application/json\",\"uri\":\"http://my.endpoint.org/notify\"},\"format\":\"keyValues\"},\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"]}";
  String Subscript =
    "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"entities\":[{\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}],\"id\":\"urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription\",\"isActive\":true,\"notification\":{\"attributes\":[\"http://speed\"],\"endpoint\":{\"accept\":\"application/json\",\"uri\":\"http://my.endpoint.org/notify\"}},\"type\":\"Subscription\"}";
  String inputData_update =
    "{\"csf\":\"string\",\"datasetIds\":[\"Test3\"],\"description\":\"stringUpdate\",\"entities\":[{\"id\":\"urn:datahub:OffStreetParking:yatap_01\",\"type\":\"http://211.253.243.121/context/Test3#Test3\"}],\"expires\":\"9999-11-15T20:10:00.000+09:00\",\"geoQ\":{\"georel\":\"near;maxDistance==2000\"},\"id\":\"urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription\",\"isActive\":true,\"name\":\"string\",\"notification\":{\"attributes\":[\"http://speed\"],\"endpoint\":{\"accept\":\"application/json\",\"uri\":\"http://my.endpoint.org/notify\"},\"format\":\"normalized\"},\"q\":\"string\",\"temporalQ\":{\"endTime\":\"9999-11-15T20:10:00.000+09:00\",\"time\":\"9999-11-15T20:10:00.000+09:00\",\"timeproperty\":\"String\",\"timerel\":\"after\"},\"throttling\":1,\"type\":\"Subscription\",\"watchedAttributes\":[\"http://address\"]}";

  @Test
  void testCreateSubscription028_03() throws Exception {
    String inputData =
      "{\"id\":\"urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription\",\"type\":\"Subscription\",\"entities\":[{\"type\":\"TestModel3\"}],\"notification\":{\"attributes\":[\"http://speed\"],\"endpoint\":{\"accept\":\"application/json\",\"uri\":\"http://my.endpoint.org/notify\"},\"format\":\"keyValues\"},\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"]}";
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/subscriptions")
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
            .get(
              "/subscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription"
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
              "/subscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription"
            )
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent())
        .andDo(print());
  }

  @Test
  void testUpdateSubscription029_05() throws Exception {
    String inputData =
      "{\"id\":\"urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription\",\"type\":\"Subscription\",\"entities\":[{\"type\":\"TestModel3\"}],\"notification\":{\"attributes\":[\"http://speed\"],\"endpoint\":{\"accept\":\"application/json\",\"uri\":\"http://my.endpoint.org/notify\"},\"format\":\"keyValues\"},\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"]}";
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/subscriptions")
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
              "/subscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription"
            )
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(print());

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .patch(
              "/subscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription"
            )
            .content(inputData_update)
            .contentType("application/ld+json")
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

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .get(
              "/subscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription"
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
              "/subscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription"
            )
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent())
        .andDo(print());
  }

  @Test
  void testRetrieveSubscription030_03() throws Exception {
    String inputData =
      "{\"id\":\"urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription\",\"type\":\"Subscription\",\"entities\":[{\"type\":\"TestModel3\"}],\"notification\":{\"attributes\":[\"http://speed\"],\"endpoint\":{\"accept\":\"application/json\",\"uri\":\"http://my.endpoint.org/notify\"},\"format\":\"keyValues\"},\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"]}";
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/subscriptions")
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
              "/subscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription"
            )
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
              "/subscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription"
            )
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent())
        .andDo(print());
  }

  @Test
  void testDelete032_03() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/subscriptions")
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
              "/subscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription"
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
              "/subscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription"
            )
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent())
        .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Post=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");
  }

  @Test
  void testCreateSubscription046_01() throws Exception {
    String inputData_entity =
      "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/entities")
          .content(inputData_entity)
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
            .post("/subscriptions")
            .content(Subscript)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .header("Content-Length", String.valueOf(Subscript.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Post=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");

    //pathch entity
    inputData =
      "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testinvalidattr\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}";
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

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete(
              "/subscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription"
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
  void testCreateSubscription046_04() throws Exception {
    String inputData_entity =
      "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/entities")
          .content(inputData_entity)
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
            .post("/subscriptions")
            .content(Subscript)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .header("Content-Length", String.valueOf(Subscript.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Post=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");

    //pathch entity
    inputData =
      "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testinvalidattr\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}";
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

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete(
              "/subscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription"
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
  void testCreateSubscription046_09() throws Exception {
    String inputData_entity =
      "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"testArrayBoolean\":{\"type\":\"Property\",\"value\":[false,true]},\"id\":\"urn:datahub:TestModel3:70-b3-d5-67-60-00-5c-1e\",\"type\":\"TestModel3\"}";
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/entities")
          .content(inputData_entity)
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
            .post("/subscriptions")
            .content(Subscript)
            .contentType("application/ld+json")
            .accept(MediaType.APPLICATION_JSON)
            .header("Content-Length", String.valueOf(Subscript.length()))
        )
        .andExpect(status().isCreated())
        .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Post=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");

    //pathch entity
    inputData =
      "{\"@context\":[\"http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld\",\"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld\"],\"type\":\"TestModel3\",\"datasetId\":\"TestModel3\",\"testinvalidattr\":{\"type\":\"Property\",\"value\":[false,true]},\"type\":\"TestModel3\"}";
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

    resultActions =
      mvc
        .perform(
          MockMvcRequestBuilders
            .delete(
              "/subscriptions/urn:ngsi-ld:CsourceRegistrationSubscription:myCsourceRegistrationSubscription"
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
  void testQuerySubscriptions() throws Exception {
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .get("/subscriptions")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Query=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");
  }
}
