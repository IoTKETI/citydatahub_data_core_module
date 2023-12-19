package kr.re.keti.sc.dataservicebroker.entities.controller.http;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdOperationNotSupportedException;
import kr.re.keti.sc.dataservicebroker.common.service.security.AASSVC;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityVO;
import kr.re.keti.sc.dataservicebroker.common.vo.EntityCountVO;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.entities.service.EntityRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.util.HttpHeadersUtil;
import kr.re.keti.sc.dataservicebroker.util.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class TemporalEntityController {

    @Autowired
    protected EntityRetrieveSVC entityRetrieveSVC;
    @Autowired
    private AASSVC aasSVC;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${entity.default.storage:rdb}")
    private BigDataStorageType defaultStorageType;
    @Value("${datacore.http.binding.response.log.yn:N}")
    private String isResponseLog;
    @Value("${security.acl.useYn:N}")
    private String securityAclUseYn;
    @Value("${entity.retrieve.primary.accept:application/json}")
    private String primaryAccept;
    @Value("${entity.default.context-uri}")
    private String defaultContextUri;

    /**
     * 이력 데이터 건수 조회
     *
     * @param response
     * @param queryVO  사용자 요청 파라미터
     * @throws Exception
     */
    @LogExecutionTime
    @GetMapping("/temporal/entitycount")
    public @ResponseBody
    void getEntityCount(HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestHeader(value = HttpHeaders.ACCEPT) String accept,
                        @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                        @ModelAttribute QueryVO queryVO) throws Exception {

        log.info("temporal entitiesCount request msg='{}'", queryVO.toString());


        // 1. 접근권한 체크
        if (securityAclUseYn.equals(DataServiceBrokerCode.UseYn.YES.getCode())) {
            aasSVC.checkRetriveAccessRule(request, queryVO);
        }

        List<String> links = HttpHeadersUtil.extractLinkUris(link);
        queryVO.setLinks(getLinkOrDefault(links));

        // 2. 리소스 조회
        Integer totalCount = entityRetrieveSVC.getTemporalEntityCount(queryVO, request.getQueryString(), link);

        EntityCountVO entityCountVO = new EntityCountVO();
        entityCountVO.setTotalCount(totalCount);
        entityCountVO.setType(queryVO.getType());

        // 3. 설정에 property 조건에 따라 response body를 로그로 남김
        String jsonResult = objectMapper.writeValueAsString(entityCountVO);
        log.info("response body : " + jsonResult);

        response.getWriter().print(jsonResult);
    }

    /**
     * 이력 데이터 조회
     *
     * @param response
     * @param accept
     * @param queryVO  사용자 요청 파라미터
     * @throws Exception
     */
    @LogExecutionTime
    @GetMapping("/temporal/entities")
    public @ResponseBody
    void getEntity(HttpServletRequest request,
                   HttpServletResponse response,
                   @RequestHeader(value = HttpHeaders.ACCEPT) String accept,
                   @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
                   @ModelAttribute QueryVO queryVO) throws Exception {

        StringBuilder requestParams = new StringBuilder();
        requestParams.append("accept=").append(accept)
                .append(", params(queryVO)=").append(queryVO.toString());

        log.info("request msg='{}'", requestParams);

        // 1. 접근권한 체크
        if (securityAclUseYn.equals(DataServiceBrokerCode.UseYn.YES.getCode())) {
            aasSVC.checkRetriveAccessRule(request, queryVO);
        }

        List<String> links = HttpHeadersUtil.extractLinkUris(link);
        queryVO.setLinks(getLinkOrDefault(links));

        accept = HttpHeadersUtil.getPrimaryAccept(accept);

        // 2. 리소스 조회
        List<CommonEntityVO> resultList = entityRetrieveSVC.getTemporalEntity(queryVO, request.getQueryString(), accept, link);

        // 3. 설정에 property 조건에 따라 response body를 로그로 남김
        if (DataServiceBrokerCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + objectMapper.writeValueAsString(resultList));
        }

        HttpHeadersUtil.addContextLinkHeader(response, accept, getLinkHeaderByAccept(accept, links));
        response.getWriter().print(objectMapper.writeValueAsString(resultList));

    }

    /**
     * 이력 데이터 조회 by ID
     *
     * @param response
     * @param accept
     * @param queryVO  사용자 요청 파라미터
     * @throws Exception
     */
    @LogExecutionTime
    @GetMapping("/temporal/entities/{id:.+}")
    public @ResponseBody
    void getEntityById(HttpServletRequest request,
    				   HttpServletResponse response,
    				   @RequestHeader(HttpHeaders.ACCEPT) String accept,
    				   @RequestHeader(value = HttpHeaders.LINK, required = false) String link,
    				   @PathVariable String id,
    				   @ModelAttribute QueryVO queryVO) throws Exception {

        StringBuilder requestParams = new StringBuilder();
        requestParams.append("accept=").append(accept)
                .append(", params(queryVO)=").append(queryVO.toString());

        log.info("request msg='{}'", requestParams);

        // 1. 접근권한 체크
        if (securityAclUseYn.equals(DataServiceBrokerCode.UseYn.YES.getCode())) {
            aasSVC.checkRetriveAccessRule(request, queryVO);
        }

        List<String> links = HttpHeadersUtil.extractLinkUris(link);
        queryVO.setLinks(getLinkOrDefault(links));

        accept = HttpHeadersUtil.getPrimaryAccept(accept);

        // 2. 리소스 조회
        CommonEntityVO resultList = entityRetrieveSVC.getTemporalEntityById(queryVO, request.getQueryString(), accept, link);

        // 3. 설정에 property 조건에 따라 response body를 로그로 남김
        if (DataServiceBrokerCode.UseYn.YES.getCode().equalsIgnoreCase(isResponseLog)) {
            log.info("response body : " + objectMapper.writeValueAsString(resultList));
        }

        HttpHeadersUtil.addContextLinkHeader(response, accept, getLinkHeaderByAccept(accept, links));
        response.getWriter().print(objectMapper.writeValueAsString(resultList));

    }

    /**
     * 이력 데이터 건수 조회 By Id
     *
     * @param response
     * @param queryVO  사용자 요청 파라미터
     * @throws Exception
     */
    @LogExecutionTime
    @GetMapping("/temporal/entitycount/{id:.+}")
    public @ResponseBody
    void getEntityCountById(HttpServletRequest request,
    						HttpServletResponse response,
    						@PathVariable String id,
    						@RequestHeader(value = HttpHeaders.LINK, required = false) String link,
    						@ModelAttribute QueryVO queryVO) throws Exception {

        log.info("temporal entitiesCount by Id request msg='{}'", queryVO.toString());

        // 1. 데이터셋 접근권한 체크
        if (securityAclUseYn.equals(DataServiceBrokerCode.UseYn.YES.getCode())) {
            aasSVC.checkRetriveAccessRule(request, queryVO);
        }

        List<String> links = HttpHeadersUtil.extractLinkUris(link);
        queryVO.setLinks(getLinkOrDefault(links));

        // 2. 조회할 storageType 설정
        BigDataStorageType dataStorageType = BigDataStorageType.parseType(queryVO.getDataStorageType());
        if (dataStorageType == null) {
            dataStorageType = defaultStorageType;
        }

        // 3. 리소스 조회
        int totalCount = entityRetrieveSVC.getTemporalEntityCount(queryVO, request.getQueryString(), link);

        EntityCountVO entityCountVO = new EntityCountVO();
        entityCountVO.setTotalCount(totalCount);
        entityCountVO.setType(queryVO.getType());

        // 4. 설정에 property 조건에 따라 response body를 로그로 남김
        String jsonResult = objectMapper.writeValueAsString(entityCountVO);
        log.info("response body : " + jsonResult);

        response.getWriter().print(jsonResult);
    }


    /**
     * (진행 안함) Create or Update Temporal Representation of Entities, 6.18.3.1
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @PostMapping("/temporal/entities")
    public void creatOrUpdateTemporalRepresentationOfEntities(HttpServletRequest request, HttpServletResponse response) throws Exception {

        throw new NgsiLdOperationNotSupportedException(DataServiceBrokerCode.ErrorCode.OPERATION_NOT_SUPPORTED, "The operation is not supported");

    }


    /**
     * (진행 안함) Add Attributes to Temporal Representation of an Entity, 6.20.3.1
     *
     * @param request
     * @param response
     * @param id
     * @throws Exception
     */
    @PostMapping("/temporal/entities/{id:.+}/attrs/")
    public void addAttributesToTemporalRepresentationOfAnEntity(HttpServletRequest request
            , HttpServletResponse response
            , @PathVariable("id") String id
    ) throws Exception {

        throw new NgsiLdOperationNotSupportedException(DataServiceBrokerCode.ErrorCode.OPERATION_NOT_SUPPORTED, "The operation is not supported");

    }

    /**
     * (진행 안함) Delete Attribute from Temporal Representation of an Entity, 6.21.3.1
     *
     * @param request
     * @param response
     * @param id
     * @param attrId
     * @throws Exception
     */
    @DeleteMapping("/temporal/entities/{id:.+}/attrs/{attrId:.+}")
    public void deleteAttributeFromTemporalRepresentationOfAnEntity(HttpServletRequest request
            , HttpServletResponse response
            , @PathVariable("id") String id
            , @PathVariable("attrId") String attrId
    ) throws Exception {
        throw new NgsiLdOperationNotSupportedException(DataServiceBrokerCode.ErrorCode.OPERATION_NOT_SUPPORTED, "The operation is not supported");

    }


    /**
     * (진행 안함) Modify Attribute instance in Temporal Representaion of an Entity, 6.22.3.1
     *
     * @param request
     * @param response
     * @param id
     * @param attrId
     * @param instanceId
     * @throws Exception
     */
    @PatchMapping("/temporal/entities/{id:.+}/attrs/{attrId:.+}/{instanceId:.+}")
    public void modifyAttributeInstanceInTemporalRepresentaionOfAnEntity(HttpServletRequest request
            , HttpServletResponse response
            , @PathVariable("id") String id
            , @PathVariable("attrId") String attrId
            , @PathVariable("instanceId") String instanceId
    ) throws Exception {
        throw new NgsiLdOperationNotSupportedException(DataServiceBrokerCode.ErrorCode.OPERATION_NOT_SUPPORTED, "The operation is not supported");

    }


    /**
     * (진행 안함) Delete Attribute instance from Temporal Representation of an Entity, 6.22.3.2
     *
     * @param request
     * @param response
     * @param id
     * @param attrId
     * @param instanceId
     * @throws Exception
     */
    @DeleteMapping("/temporal/entities/{id:.+}/attrs/{attrId:.+}/{instanceId:.+}")
    public void deleteAttributeInstanceFromTemporalRepresentationOfAnEntity(HttpServletRequest request
            , HttpServletResponse response
            , @PathVariable("id") String id
            , @PathVariable("attrId") String attrId
            , @PathVariable("instanceId") String instanceId
    ) throws Exception {
        throw new NgsiLdOperationNotSupportedException(DataServiceBrokerCode.ErrorCode.OPERATION_NOT_SUPPORTED, "The operation is not supported");

    }

    /**
     * (진행 안함) Delete Temporal Representaion of and Entity, 6.19.3.2
     *
     * @param request
     * @param response
     * @param id
     * @throws Exception
     */
    @DeleteMapping("/temporal/entities/{id:.+}")
    public void deleteTemporalRepresentaionOfAndEntity(HttpServletRequest request
            , HttpServletResponse response
            , @PathVariable("id") String id
    ) throws Exception {
        throw new NgsiLdOperationNotSupportedException(DataServiceBrokerCode.ErrorCode.OPERATION_NOT_SUPPORTED, "The operation is not supported");

    }

    private String getPrimaryAccept(String requestAccept) {

        if(ValidateUtil.isEmptyData(requestAccept)) {
            return requestAccept;
        }

        // 요청 accept가 다건인 경우
        if (requestAccept.contains(Constants.ACCEPT_ALL)) {
            return primaryAccept;
        } else if (requestAccept.contains(primaryAccept)) {
            return primaryAccept;
        } else if (requestAccept.contains(Constants.APPLICATION_LD_JSON_VALUE)) {
            return Constants.APPLICATION_LD_JSON_VALUE;
        } else if (requestAccept.contains(Constants.APPLICATION_JSON_VALUE)) {
            return Constants.APPLICATION_JSON_VALUE;
        } else if (requestAccept.contains(Constants.APPLICATION_GEO_JSON_VALUE)) {
            return Constants.APPLICATION_GEO_JSON_VALUE;
        }

        return requestAccept;
    }

    private List<String> getLinkHeaderByAccept(String accept, List<String> link) {

        if (Constants.APPLICATION_LD_JSON_VALUE.equals(accept)) {
            return null;
        }

        if (!ValidateUtil.isEmptyData(link)) {
            return link;
        }
        return Collections.singletonList(defaultContextUri);
    }

    private List<String> getLinkOrDefault(List<String> link) {
        if (!ValidateUtil.isEmptyData(link)) {
            return link;
        }
        return Collections.singletonList(defaultContextUri);
    }

}
