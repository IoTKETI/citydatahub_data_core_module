package kr.re.keti.sc.ingestinterface.controller.http;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.ingestinterface.common.code.Constants;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.exception.BadRequestException;
import kr.re.keti.sc.ingestinterface.common.vo.PageRequest;
import kr.re.keti.sc.ingestinterface.externalplatformauthentication.service.ExternalPlatformAuthenticationSVC;
import kr.re.keti.sc.ingestinterface.externalplatformauthentication.vo.ExternalPlatformAuthenticationBaseVO;
import lombok.extern.slf4j.Slf4j;

/**
 * 외부 플랫폼 연동 플랫폼 관리 CRUD
 */
@RestController
@Slf4j
public class ExternalPlatformAuthenticationController {

    @Autowired
    private ExternalPlatformAuthenticationSVC externalPlatformAuthenticationSVC;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${datacore.http.binding.response.log.yn:N}")
    private String isResponseLog;

    /**
     * 외부 플랫폼 인증 생성
     *
     * @param response
     * @param requestBody 사용자 요청 body
     */
    @PostMapping("/externalplatform/authentication")
    public void createExternalplatformAuthentication(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) throws Exception {

        log.info("create external platform authentication  request requestBody=" + requestBody);

        //TODO CREATOR_ID 정보 입력 필요

        // 1. 객체 생성
        ExternalPlatformAuthenticationBaseVO externalPlatformAuthenticationBaseVO = objectMapper.readValue(requestBody, ExternalPlatformAuthenticationBaseVO.class);

        // 2. 정보 저장
        int result = externalPlatformAuthenticationSVC.createExternalPlatformAuthenticationBaseVO(externalPlatformAuthenticationBaseVO);

        // 3. 처리 결과 설정
        response.setStatus(HttpStatus.CREATED.value());
    }

    /**
     * 외부 플랫폼 인증 수정
     *
     * @param response
     * @param id
     * @throws Exception
     */
    @PatchMapping("/externalplatform/authentication/{id}")
    public void updateExternalplatformAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                     @RequestBody String requestBody, @PathVariable String id) throws Exception {

        // 1. 요청 파라미터 확인
        log.info("update external platform authentication request requestBody=" + requestBody + ", id=" + id);

        // 2. 외부 플랫폼 인증 속성 수정 객체 생성
        ExternalPlatformAuthenticationBaseVO externalPlatformAuthenticationBaseVO = objectMapper.readValue(requestBody, ExternalPlatformAuthenticationBaseVO.class);
        externalPlatformAuthenticationBaseVO.setId(id);
        // 3. 외부 플랫폼 인증 정보 저장
        int result = externalPlatformAuthenticationSVC.updateExternalPlatformAuthenticationBaseVO(externalPlatformAuthenticationBaseVO);

        if (result > 0) {
            // 4. 처리 결과 설정
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            throw new BadRequestException(IngestInterfaceCode.ErrorCode.NOT_EXIST_ID, "Not Exists. id=" + id);
        }

    }


    /**
     * 외부 플랫폼 인증 삭제
     *
     * @param response
     * @param id
     * @throws Exception
     */
    @DeleteMapping("/externalplatform/authentication/{id}")
    public void deleteExternalplatformAuthentication(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) throws Exception {

        // 1. 요청 파라미터 확인
        log.info("delete external platform authentication request id=" + id);

        // 2. 외부 플랫폼 인증 삭제 요청
        int result = externalPlatformAuthenticationSVC.deleteExternalPlatformAuthenticationBaseVO(id);
//
        if (result > 0) {
            // 3. 처리 결과 설정
            response.setStatus(HttpStatus.NO_CONTENT.value());

        } else {
            throw new BadRequestException(IngestInterfaceCode.ErrorCode.NOT_EXIST_ID, "Not Exists. id=" + id);
        }
    }


    /**
     * 외부 플랫폼 인증 조회 by ID
     * @param response HttpServletResponse
     * @param accept request http accept header
     * @param id external platform authentication id
     * @throws Exception retrieve error
     */
    @GetMapping("/externalplatform/authentication/{id}")
    public @ResponseBody
    void getExternalPlatformAuthenticationBaseVOById(HttpServletResponse response,
                                                     @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                                     @PathVariable String id) throws Exception {

        log.info("get external platform authentication request id=" + id);

        ExternalPlatformAuthenticationBaseVO externalPlatformAuthenticationBaseVO = externalPlatformAuthenticationSVC.getExternalPlatformAuthenticationBaseVOById(id);

        if (externalPlatformAuthenticationBaseVO == null) {
            throw new BadRequestException(IngestInterfaceCode.ErrorCode.NOT_EXIST_ID, "Not Exists. id=" + id);
        }


        // 4. 설정에 property 조건에 따라 response body를 로그로 남김
        if (IngestInterfaceCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + objectMapper.writeValueAsString(externalPlatformAuthenticationBaseVO));
        }
        response.getWriter().print(objectMapper.writeValueAsString(externalPlatformAuthenticationBaseVO));

    }

    /**
     * 외부 플랫폼 인증 리스트 조회
     * @param response HttpServletResponse
     * @param accept request http accept header
     * @param pageRequest paging condition
     * @throws Exception retrieve error
     */
    @GetMapping(value = "/externalplatform/authentication")
    public @ResponseBody void getExternalPlatformAuthenticationBaseVOList(HttpServletResponse response,
                                                                          @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                                                          PageRequest pageRequest) throws Exception {
        log.info("get external platform authentication");

        // 1. 리소스 전체 갯수
        Integer totalCount = externalPlatformAuthenticationSVC.getExternalPlatformAuthenticationBaseVOListTotalCount();

        // 2. 리소스 조회
        List<ExternalPlatformAuthenticationBaseVO> externalPlatformAuthenticationBaseVOs = new ArrayList<>();
        externalPlatformAuthenticationBaseVOs = externalPlatformAuthenticationSVC.getExternalPlatformAuthenticationBaseVOList(pageRequest);


        // 3. 설정에 property 조건에 따라 response body를 로그로 남김
        if (IngestInterfaceCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + objectMapper.writeValueAsString(externalPlatformAuthenticationBaseVOs));
        }
        // 4. 리소스 전체 갯수 헤더에 추가
        response.addHeader(Constants.TOTAL_COUNT, Integer.toString(totalCount));

        // 5. 응답문
        response.getWriter().print(objectMapper.writeValueAsString(externalPlatformAuthenticationBaseVOs));

    }

}
