package kr.re.keti.sc.ingestinterface.controller.http;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.ingestinterface.common.code.Constants;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.exception.BadRequestException;
import kr.re.keti.sc.ingestinterface.common.exception.ResourceNotFoundException;
import kr.re.keti.sc.ingestinterface.verificationhistory.service.VerificationHistorySVC;
import kr.re.keti.sc.ingestinterface.verificationhistory.vo.VerificationHistoryBaseVO;
import lombok.extern.slf4j.Slf4j;

/**
 * 품질 이력 조회 API HTTP controller class
 */
@RestController
@Slf4j
public class VerificationHistoryController {

    @Autowired
    private VerificationHistorySVC verificationHistorySVC;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${datacore.http.binding.response.log.yn:N}")
    private String isResponseLog;

    

    /**
     * retrieve verification history
     * @param response HttpServletResponse
     * @param accept http request accept header
     * @param verificationHistoryBaseVO retrieve condition
     * @throws Exception retireve error
     */
    @GetMapping(value = "/verificationHistory")
    public @ResponseBody
    void getVerificationHistory(HttpServletResponse response,
                                @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                VerificationHistoryBaseVO verificationHistoryBaseVO) throws Exception {

        StringBuilder requestParams = new StringBuilder();
        requestParams.append("accept=").append(accept)
                .append(", params(verificationHistoryBaseVO)=").append(verificationHistoryBaseVO.toString());

        log.info("request msg='{}'", requestParams);

        // 1. 필수 파라미터 체크
        checkMandatoryField(verificationHistoryBaseVO);

        // 2. 리소스 전체 갯수
        Integer totalCount = verificationHistorySVC.getVerificationHistoryTotalCount(verificationHistoryBaseVO);

        // 3. 리소스 조회
        List<VerificationHistoryBaseVO> resultList = verificationHistorySVC.getVerificationHistory(verificationHistoryBaseVO);

        // 4. 설정에 property 조건에 따라 response body를 로그로 남김
        if (IngestInterfaceCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + objectMapper.writeValueAsString(resultList));
        }
        // 5. 리소스 전체 갯수 헤더에 추가
        response.addHeader(Constants.TOTAL_COUNT, Integer.toString(totalCount));
        // 6. 응답문
        response.getWriter().print(objectMapper.writeValueAsString(resultList));
    }


    /**
     * retrieve verification history by key
     * @param response HttpServletResponse
     * @param accept http request accept header
     * @param seq history sequence
     * @throws Exception retrieve error
     */
    @GetMapping(value = "/verificationHistory/{seq}")
    public @ResponseBody void getVerificationHistoryBySeq(HttpServletResponse response,
                                                          @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                                          @PathVariable Integer seq) throws Exception {

        StringBuilder requestParams = new StringBuilder();
        requestParams.append("accept=").append(accept)
                .append(", params(seq)=").append(seq);

        log.info("request msg='{}'", requestParams);

        // 1. 필수 파라미터 체크
        if (seq == null) {
            throw new BadRequestException(IngestInterfaceCode.ErrorCode.INVALID_PARAMETER, "should include seq");
        }

        // 2. 리소스 조회
        VerificationHistoryBaseVO result = verificationHistorySVC.getVerificationHistoryBySeq(seq);

        // 3. 조회된 resource가 없을 경우, ResourceNotFound 처리
        if (result == null) {
            throw new ResourceNotFoundException(IngestInterfaceCode.ErrorCode.NOT_EXIST_ID, "There is no an existing item which seq");
        }


        // 4. 설정에 property 조건에 따라 response body를 로그로 남김
        if (IngestInterfaceCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + objectMapper.writeValueAsString(result));
        }

        // 5. 응답문
        response.getWriter().print(objectMapper.writeValueAsString(result));
    }

    /**
     * retrieve verification history count
     * @param response HttpServletResponse
     * @param accept http request accept header
     * @param verificationHistoryBaseVO retrieve condition
     * @throws Exception retrieve error
     */
    @GetMapping(value = "/verificationHistory/count")
    public @ResponseBody void getVerificationHistoryCount(HttpServletResponse response,
                                                          @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                                          VerificationHistoryBaseVO verificationHistoryBaseVO) throws Exception {

        StringBuilder requestParams = new StringBuilder();
        requestParams.append("accept=").append(accept)
                .append(", params(verificationHistoryBaseVO)=").append(verificationHistoryBaseVO.toString());

        log.info("request msg='{}'", requestParams);

        // 1. 필수 파라미터 체크
        checkMandatoryField(verificationHistoryBaseVO);
        // 2. 리소스 조회
        Object resultMap = verificationHistorySVC.getVerificationHistoryCount(verificationHistoryBaseVO);

        // 3. 결과가 없는 경우, 규격을 맞추기 위하 빈 map으로 처리
        if (resultMap == null) {
            resultMap = new HashMap<>();
        }

        // 4. 설정에 property 조건에 따라 response body를 로그로 남김
        if (IngestInterfaceCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + objectMapper.writeValueAsString(resultMap));
        }
        // 5. 응답문
        response.getWriter().print(objectMapper.writeValueAsString(resultMap));
    }

    /**
     * 검색 필수 조건 필터링답 (startTime and endTime)
     *
     * @param verificationHistoryBaseVO
     */
    private void checkMandatoryField(VerificationHistoryBaseVO verificationHistoryBaseVO) {

        if (verificationHistoryBaseVO.getStartTime() == null || verificationHistoryBaseVO.getEndTime() == null) {
            throw new BadRequestException(IngestInterfaceCode.ErrorCode.INVALID_PARAMETER, "should include startTime and endTime");
        }
    }

}
