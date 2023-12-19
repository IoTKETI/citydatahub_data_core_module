package kr.re.keti.sc.ingestinterface.common.configuration.security.interceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.service.security.AASSVC;
import kr.re.keti.sc.ingestinterface.common.vo.security.AASUserDetailsVO;
import kr.re.keti.sc.ingestinterface.externalplatformauthentication.service.ExternalPlatformAuthenticationSVC;
import kr.re.keti.sc.ingestinterface.externalplatformauthentication.vo.ExternalPlatformAuthenticationBaseVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExternelPlatformInterceptor extends HandlerInterceptorAdapter {

	@Autowired
    private ObjectMapper objectMapper;

    @Autowired
    ExternalPlatformAuthenticationSVC externalPlatformAuthenticationSVC;
    @Autowired
    AASSVC aasSVC;

    @Value("${security.external.platform.useYn:N}")
    private String securityExternalPlatformUseYn;
    @Value("#{'${security.headers.admin.value}'.split(',')}")
    private List<String> adminUserRoleList;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        //인증을 사용하지 않을 경우, SKIP
        if (securityExternalPlatformUseYn.equals(IngestInterfaceCode.UseYn.NO)) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AASUserDetailsVO aasUserDetailsVO = (AASUserDetailsVO) authentication.getPrincipal();

        String clientId = aasUserDetailsVO.getClientId();
        ExternalPlatformAuthenticationBaseVO externalPlatformAuthenticationBaseVO = externalPlatformAuthenticationSVC.getExternalPlatformAuthenticationBaseVOByClientId(clientId);

        // 인증 정보가 없는 경우, 예외처리
        if (externalPlatformAuthenticationBaseVO == null){
            throw new AccessDeniedException("access denied");
        }


        // 수신 IP 검증
        isReceptionIPs(request, externalPlatformAuthenticationBaseVO);

        // client ID 검증
        isReceptionClientIDs(clientId, externalPlatformAuthenticationBaseVO);

        // datasetId 검증
        isReceptionDatasetIDs(request, externalPlatformAuthenticationBaseVO);

        // 데이터 인스턴스 검증
        isMatchingDataInstancePrefix(request, externalPlatformAuthenticationBaseVO);

        return true;
    }

    /**
     * 수신 허용 되는 IP 여부 체크
     * @param request HttpServletRequest
     * @param externalPlatformAuthenticationBaseVO external platform authentication data
     */
    public void isReceptionIPs(HttpServletRequest request, ExternalPlatformAuthenticationBaseVO externalPlatformAuthenticationBaseVO) {

        String ip = getIp(request);
        List<String> receptionIps = externalPlatformAuthenticationBaseVO.getReceptionIps();

        if (receptionIps.contains("*")) {
            return;
        }
        if (receptionIps.contains(ip)) {
            return;
        }

        throw new AccessDeniedException("ip address is not allowed");
    }

    /**
     * 수신 허용 되는 클라이언트 ID 여부 체크
     * @param clientId clientId
     * @param externalPlatformAuthenticationBaseVO external platform authentication data
     */
    public void isReceptionClientIDs(String clientId, ExternalPlatformAuthenticationBaseVO externalPlatformAuthenticationBaseVO) {
        List<String> receptionClientIds = externalPlatformAuthenticationBaseVO.getReceptionClientIds();


        if (receptionClientIds.contains("*")) {
            return;
        }
        if (receptionClientIds.contains(clientId)) {
            return;
        }

        throw new AccessDeniedException("client ID is not allowed");
    }

    /**
     * 수신 허용 되는 데이터 셋 ID 여부 체크
     * @param request HttpServletRequest
     * @param externalPlatformAuthenticationBaseVO external platform authentication data
     * @throws IOException json parsing error
     */
    public void isReceptionDatasetIDs(HttpServletRequest request, ExternalPlatformAuthenticationBaseVO externalPlatformAuthenticationBaseVO) throws IOException {

        List<String> receptionClientIds = externalPlatformAuthenticationBaseVO.getReceptionDatasetIds();


        if (receptionClientIds.contains("*")) {
            return;
        }

        String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        HashMap<String, Object> map = this.objectMapper.readValue(body, HashMap.class);
        String datasetId = (String) map.get("datasetId");
        if (receptionClientIds.contains(datasetId)) {
            return;
        }

        throw new AccessDeniedException("dataset ID is not allowed");
    }


    /**
     * 데이터 인스턴스 prefix 일치 여부 체크
     * @param request HttpServletRequest
     * @param externalPlatformAuthenticationBaseVO external platform authentication data
     * @throws IOException json parsing error
     */
    public void isMatchingDataInstancePrefix(HttpServletRequest request, ExternalPlatformAuthenticationBaseVO externalPlatformAuthenticationBaseVO) throws IOException {

        String dataInstancePrefix = externalPlatformAuthenticationBaseVO.getDataInstancePrefix();


        if (dataInstancePrefix == null) {
            return;
        }

        String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        HashMap<String, Object> map = this.objectMapper.readValue(body, HashMap.class);
        List<HashMap<String, Object>> entities = (List<HashMap<String, Object>>) map.get("entities");
        for (HashMap entity : entities) {

            String id = entity.get("id").toString();
            if (!id.startsWith(dataInstancePrefix)) {
                throw new AccessDeniedException("Data instance prefix is not allowed");
            }
        }
    }


    public String getIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");
        log.info(">>>> X-FORWARDED-FOR : " + ip);

        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
            log.info(">>>> Proxy-Client-IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP"); // 웹로직
            log.info(">>>> WL-Proxy-Client-IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
            log.info(">>>> HTTP_CLIENT_IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            log.info(">>>> HTTP_X_FORWARDED_FOR : " + ip);
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        log.info(">>>> Result : IP Address : " + ip);

        return ip;

    }

}
