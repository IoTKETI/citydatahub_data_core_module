package kr.re.keti.sc.dataservicebroker.acl.rule.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.acl.rule.dao.AclRuleDAO;
import kr.re.keti.sc.dataservicebroker.acl.rule.vo.AclRuleVO;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AclRuleSVC {

    @Autowired
    AclRuleDAO aclRuleDAO;
    @Autowired
    private DataModelManager dataModelManager;
    @Autowired
    private ObjectMapper objectMapper;
    /** dataset request uri pattern */
    private final Pattern URI_PATTERN_ACL_RULES = Pattern.compile("/acl/rules/(?<id>.+)");

    /**
     * 접근제어(데이터 셋) 정보 생성
     *
     * @param requestBody 요청 Body
     * @param requestId   Provisioning Request Id
     * @param eventTime   Provisioning Request Time
     */
    public void createAclRule(String requestBody, String requestId, Date eventTime) {
        // 1. 수신 데이터 파싱
        AclRuleVO aclRuleVO = null;
        try {
            aclRuleVO = objectMapper.readValue(requestBody, AclRuleVO.class);
        } catch (IOException e) {
            throw new BadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                    "Invalid Parameter. body=" + requestBody);
        }

        // 2. 기 존재여부 확인
        AclRuleVO retrieveAclRuleVO = aclRuleDAO.getAclRuleVOById(aclRuleVO.getId());
        if (retrieveAclRuleVO != null) {
            // 3. 이중화되어 있는 다른 IngestInterface 인스턴스에서 DB 입력 했는지 체크
            if (alreadyProcessByOtherInstance(requestId, eventTime, retrieveAclRuleVO)) {
                // 다른 Instance에서 DB 업데이트 이미 처리했으므로 캐쉬만 로딩하고 성공 처리
                //TODO
//	 			dataModelManager.putDatasetCache(retrieveAclDatasetVO);
                return;
            } else {
                // 이미 존재하므로 업데이트 처리
                updateAclRule("/acl/rules/" + retrieveAclRuleVO.getId(), requestBody, requestId, eventTime);
                return;
            }
        }

        // 4. 데이터셋 정보 저장
        aclRuleVO.setProvisioningRequestId(requestId);
        aclRuleVO.setProvisioningEventTime(eventTime);
        aclRuleDAO.createAclRule(aclRuleVO);

        // 5. 데이터셋 캐쉬 로딩
        dataModelManager.putAclRuleCache(aclRuleVO);

    }

    /**
     * 접근제어(데이터 셋) 정보 수정
     *
     * @param to          데이터 셋 정보 수정 요청 수신 url
     * @param requestBody 요청 Body
     * @param requestId   Provisioning Request Id
     * @param eventTime   Provisioning Request Time
     */
    public void updateAclRule(String to, String requestBody, String requestId, Date eventTime) {
        // 1. Request URI에서 식별자 추출
        Matcher matcherForUpdate = URI_PATTERN_ACL_RULES.matcher(to);

        if (matcherForUpdate.find()) {
            String id = matcherForUpdate.group("id");

            // 2. 수신 데이터 파싱
            AclRuleVO aclRuleVO = null;
            try {
                aclRuleVO = objectMapper.readValue(requestBody, AclRuleVO.class);
            } catch (IOException e) {
                throw new BadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                        "Invalid Parameter. body=" + requestBody);
            }
            aclRuleVO.setId(id);

            // 3. 기 존재여부 확인
            AclRuleVO retrieveAclRuleVO = aclRuleDAO.getAclRuleVOById(id);
            if (retrieveAclRuleVO == null) {
                log.info("Create(Upsert) acl-Dataset. requestURI={}", to);
                createAclRule(requestBody, requestId, eventTime);
                return;
            }

            // 4. 이중화되어 있는 다른 IngestInterface 인스턴스에서 DB 입력 했는지 체크
            if (alreadyProcessByOtherInstance(requestId, eventTime, retrieveAclRuleVO)) {
                // 다른 Instance에서 DB 업데이트 이미 처리했으므로 캐쉬만 로딩하고 성공 처리
                dataModelManager.putAclRuleCache(retrieveAclRuleVO);
                return;
            }

            // 5. Dataset 업데이트
            aclRuleVO.setProvisioningRequestId(requestId);
            aclRuleVO.setProvisioningEventTime(eventTime);
            int result = aclRuleDAO.updateAclRule(aclRuleVO);

            if (result == 0) {
                throw new BadRequestException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID,
                        "Not Exists. id=" + id);
            }

            // 6. set dataset Cache
            dataModelManager.putAclRuleCache(aclRuleVO);

            // 404
        } else {
            throw new BadRequestException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID);
        }
    }

    /**
     * 데이터 셋 정보 삭제
     *
     * @param to          데이터 셋 정보 수정 요청 수신 url
     * @param requestBody 요청 Body
     */
    public void deleteAclRule(String to, String requestBody) {
        // 1. Request URI에서 식별자 추출
        Matcher matcherForUpdate = URI_PATTERN_ACL_RULES.matcher(to);

        if (matcherForUpdate.find()) {
            String id = matcherForUpdate.group("id");

            AclRuleVO retrieveAclRuleVO = aclRuleDAO.getAclRuleVOById(id);

            if (retrieveAclRuleVO != null) {
                // 2. Dataset 삭제
                int result = aclRuleDAO.deleteAclRule(id);

                if (result == 0) {
                    throw new BadRequestException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID,
                            "Not Exists. id=" + id);
                }
            }

            // 3. 데이터셋 캐쉬 삭제
            dataModelManager.removeAclRuleCache(id);

            // 404
        } else {
            throw new BadRequestException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID);
        }
    }

    /**
     * 이중화되어 있는 다른 IngestInterface 인스턴스에서 DB 입력 했는지 체크
     *
     * @param requestId             Provisioning Request Id
     * @param eventTime             Provisioning Request Time
     * @param retrieveDatasetBaseVO DB에서 조회한 데이터 셋 정보 VO
     * @return
     */
    private boolean alreadyProcessByOtherInstance(String requestId, Date eventTime, AclRuleVO retrieveDatasetBaseVO) {
        // 이중화되어 있는 다른 IngestInterface 인스턴스에서 DB 입력 했는지 체크
        if (requestId.equals(retrieveDatasetBaseVO.getProvisioningRequestId())
                && eventTime.getTime() >= retrieveDatasetBaseVO.getProvisioningEventTime().getTime()) {
            return true;
        }
        return false;
    }

}
