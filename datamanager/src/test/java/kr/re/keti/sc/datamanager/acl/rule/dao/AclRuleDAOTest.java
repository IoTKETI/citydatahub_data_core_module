package kr.re.keti.sc.datamanager.acl.rule.dao;

import kr.re.keti.sc.datamanager.acl.rule.vo.AclRuleVO;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.configuration.security.AASTokenUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AclRuleDAOTest {

    @Autowired
    private AclRuleDAO aclRuleDAO;

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void createAclDataset() {
        AclRuleVO testAclRuleVO = new AclRuleVO();
        testAclRuleVO.setId(AASTokenUtil.createUuid());
//        testAclRuleVO.setDatasetId("testdataset001");
//        testAclRuleVO.setCondition(DataManagerCode.AclRuleCondition.ALL);
        testAclRuleVO.setClientId("TEST_CLIENT_ID");
        testAclRuleVO.setUserId("TEST_USER_ID");

        Integer result = aclRuleDAO.createAclRule(testAclRuleVO);


    }

    @Test
    void updateAclDataset() {
        AclRuleVO testAclRuleVO = new AclRuleVO();

//        testAclRuleVO.setDatasetId("testdataset002");
//        testAclRuleVO.setCondition(DataManagerCode.AclRuleCondition.ALL);
        testAclRuleVO.setClientId("TEST_CLIENT_ID");
        testAclRuleVO.setUserId("TEST_USER_ID");

        Integer result = aclRuleDAO.updateAclRule(testAclRuleVO);
        System.out.println(result);

    }

    @Test
    void deleteAclDataset() {

        AclRuleVO testAclRuleVO = new AclRuleVO();
        testAclRuleVO.setUserId("TEST_USER_ID");
        testAclRuleVO.setClientId("TEST_CLIENT_ID");

//        Integer result = aclDatasetDAO.deleteAclDataset(testAclDatasetVO);
//        System.out.println(result);


    }

    @Test
    void getAclDatasetVOList() {

        AclRuleVO aclRuleVO = new AclRuleVO();
//        aclDatasetVO.setClientId("Ud6WGtFacxrAbbWLHMLO");
        List<AclRuleVO> aclRuleVOS = aclRuleDAO.getAclRuleVOList(aclRuleVO);
        System.out.println(aclRuleVOS);

    }

    @Test
    void getAclDatasetVOById() {

        AclRuleVO aclRuleVO = aclRuleDAO.getAclRuleVOById("TEST");
        System.out.println(aclRuleVO);

    }

    @Test
    void getAclDatasetTotalCount() {

        Integer count = aclRuleDAO.getAclRuleTotalCount(null);

        System.out.println("count : " + count);
    }
}