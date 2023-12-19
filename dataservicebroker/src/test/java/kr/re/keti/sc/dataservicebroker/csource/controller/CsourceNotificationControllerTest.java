package kr.re.keti.sc.dataservicebroker.csource.controller;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.notification.vo.CsourceNotificationVO;
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
public class CsourceNotificationControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper mapper;

  String inputData =
    "{\"data\":[{\"description\":\"csourceRegistrations Data\",\"endpoint\":\"http://my.csource.org:1026\",\"expires\":\"9999-11-17T20:48:09,290+09:00\",\"id\":\"string\",\"information\":[{\"entities\":[{\"id\":\"urn:datahub:OffStreetParking:yatap_01\",\"type\":\"http://kr.citydatahub.OffStreetParking:1.0\"}],\"properties\":[[\"address\",\"streedAddress\"]],\"relationships\":[[\"inAccident\"]]}],\"location\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[[127.11132,37.393653]],\"type\":\"MultiPolygon\"}},\"managementInterval\":{},\"name\":\"string\",\"observationInterval\":{\"start\":\"2020-11-17T20:48:09,290+09:00\"},\"observationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[[127.11132,37.393653]],\"type\":\"MultiPolygon\"}},\"operationSpace\":{\"type\":\"GeoProperty\",\"value\":{\"coordinates\":[[127.11132,37.393653]],\"type\":\"MultiPolygon\"}},\"type\":\"ContextSourceRegistration\"}],\"expires\":\"2021-11-15T20:10:00,000+09:00\",\"id\":\"TDD\",\"name\":\"string\",\"subscriptionId\":\"TDD\",\"triggerReason\":\"NEWLY_MATCHING\"}";
  String error_inputData =
    "{\"data\":[{\"description\":\"csourceRegistrations Data\"";

  @Test
  void testNotiContextSource() throws Exception {
    CsourceNotificationVO csourceRegistrationVO = new CsourceNotificationVO();
    csourceRegistrationVO =
      mapper.readValue(inputData, CsourceNotificationVO.class);

    String content = mapper.writeValueAsString(csourceRegistrationVO);
    ResultActions resultActions = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/csourceNotifications")
          .content(inputData)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header("Content-Length", String.valueOf(content.length()))
      )
      .andExpect(status().isOk())
      .andExpect(content().string(""))
      .andDo(print());

    MvcResult mvcResult = resultActions.andReturn();
    System.out.println("=====================Post=====================");
    System.out.println(mvcResult.getResponse().getContentAsString());
    System.out.println("=====================End=====================");
    /*
     400 Request parameters error exception TDD
*/
    ResultActions resultActions3 = mvc
      .perform(
        MockMvcRequestBuilders
          .post("/csourceRegistrations")
          .content(error_inputData)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .header("Content-Length", String.valueOf(error_inputData.length()))
      )
      .andExpect(status().isBadRequest())
      .andExpect(
        (
          rslt ->
            assertTrue(
              rslt
                .getResolvedException()
                .getClass()
                .isAssignableFrom(HttpMessageNotReadableException.class)
            )
        )
      )
      .andDo(print());
  }
}
