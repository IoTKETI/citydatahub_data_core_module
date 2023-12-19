package kr.re.keti.sc.datamanager.acl.rule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.datamanager.acl.rule.vo.AclRuleVO;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("local")

class AclRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;
    ObjectMapper mapper = new ObjectMapper();

    @Test
    void getAclDatasets() throws Exception {

        AclRuleVO requestAclRuleVO = new AclRuleVO();

        ResultActions resultActions  = mockMvc.perform(MockMvcRequestBuilders.get("/acl/datasets")
                .content(mapper.writeValueAsString(requestAclRuleVO))
                .accept(MediaType.APPLICATION_JSON));
//                .contentType(MediaType.APPLICATION_JSON));
//                .andExpect(MockMvcResultMatchers.status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        System.out.println("=====================");
        System.out.println(mvcResult.getResponse().getContentAsString());
        System.out.println("=====================");

    }
}