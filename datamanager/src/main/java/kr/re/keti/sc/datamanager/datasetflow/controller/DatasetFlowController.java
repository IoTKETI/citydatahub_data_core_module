package kr.re.keti.sc.datamanager.datasetflow.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ErrorCode;
import kr.re.keti.sc.datamanager.common.exception.BadRequestException;
import kr.re.keti.sc.datamanager.dataset.service.DatasetSVC;
import kr.re.keti.sc.datamanager.datasetflow.service.DatasetFlowSVC;
import kr.re.keti.sc.datamanager.datasetflow.vo.DatasetFlowBaseVO;
import kr.re.keti.sc.datamanager.datasetflow.vo.DatasetFlowVO;
import kr.re.keti.sc.datamanager.datasetflow.vo.DatasetFlowVO.TargetTypeVO;
import kr.re.keti.sc.datamanager.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * 데이터 셋 흐름 관리 HTTP Controller 클래스
 *  - 데이터 셋 흐름 정보를 생성/수정/삭제하고 기 등록된 Provisioning 대상 서버로 생성/수정/삭제 이벤트를 Provisioning 한다
 *  - 데이터 셋 흐름 정보 조회 기능을 제공한다
 * </pre>
 */
@RestController
@Slf4j
public class DatasetFlowController {

    @Autowired
    private DatasetSVC datasetSVC;
    @Autowired
    private DatasetFlowSVC datasetFlowSVC;

    /**
     * create dataset flow
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param datasetId dataset id
     * @param datasetFlowVO create dataset flow data
     * @throws Exception create error
     */
    @PostMapping("/datasets/{datasetId:.+}/flow")
    public void create(HttpServletRequest request,
                       HttpServletResponse response,
                       @PathVariable String datasetId,
                       @RequestBody DatasetFlowVO datasetFlowVO) throws Exception {

        log.info("DatasetFlow CREATE request. datasetId={}, datasetFlowVO={}", datasetId, datasetFlowVO);

        datasetFlowVO.setDatasetId(datasetId);

        // 1. 필수값 체크
        validateParam(datasetFlowVO);
        
        // 2. 기 존재여부 체크
        if(datasetFlowSVC.getDatasetFlowVOById(datasetId) != null) {
        	throw new BadRequestException(DataManagerCode.ErrorCode.ALREADY_EXISTS,
                    "Already Exists. datasetId=" + datasetId);
        }

        // 3. 데이터 셋 존재 여부 체크
        if(datasetSVC.getDatasetVOById(datasetId) == null) {
        	throw new BadRequestException(DataManagerCode.ErrorCode.NOT_EXISTS_DATASET,
                    "Not Exists Dataset. datasetId=" + datasetId);
        }

        // 4. DatasetFlow 정보 생성 및 Provisioning
        datasetFlowSVC.createDatasetFlow(datasetFlowVO, request.getRequestURI());
        
        response.setStatus(HttpStatus.CREATED.value());
    }


    /**
     * update dataset flow
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param datasetId dataset id
     * @param datasetFlowVO update dataset flow data
     * @throws Exception update error
     */
    @PutMapping("/datasets/{datasetId:.+}/flow")
    public void update(HttpServletRequest request,
                       HttpServletResponse response,
                       @PathVariable String datasetId,
                       @RequestBody DatasetFlowVO datasetFlowVO) throws Exception {

    	log.info("DatasetFlow UPDATE request. datasetId={}, datasetFlowVO={}", datasetId, datasetFlowVO);

        datasetFlowVO.setDatasetId(datasetId);

        // 1. 기 존재여부 체크
        if(datasetFlowSVC.getDatasetFlowVOById(datasetId) == null) {
        	throw new BadRequestException(DataManagerCode.ErrorCode.NOT_EXISTS_DATASETFLOW,
                    "Not Exists. datasetId=" + datasetId);
        }

        // 2. DatasetFlow 정보 수정 및 Provisioning
        datasetFlowSVC.updateDatasetFlow(datasetFlowVO, request.getRequestURI());

        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    /**
     * delete dataset flow
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param datasetId dataset id
     * @throws Exception delete error
     */
    @DeleteMapping("/datasets/{datasetId:.+}/flow")
    public void delete(HttpServletRequest request,
                       HttpServletResponse response,
                       @PathVariable String datasetId) throws Exception {

        log.info("DatasetFlow DELETE request datasetId=" + datasetId);

        // 1. 기 존재여부 체크
        DatasetFlowBaseVO retrieveDatasetFlowBaseVO = datasetFlowSVC.getDatasetFlowBaseVOById(datasetId);
        if(retrieveDatasetFlowBaseVO == null) {
        	throw new BadRequestException(DataManagerCode.ErrorCode.NOT_EXISTS_DATASETFLOW,
                    "Not Exists. datasetId=" + datasetId);
        }
        
        // 2. DatasetFlow 정보 삭제 Provisioning 및 삭제
        datasetFlowSVC.deleteDatasetFlow(retrieveDatasetFlowBaseVO, request.getRequestURI());

        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    /**
     * provisioning datasetFlow to provision server
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param datasetId dataset id
     * @throws Exception provisioning error
     */
    @PostMapping("/datasets/{datasetId:.+}/flow/provisioning")
    public void provisioningDatasetFlow(HttpServletRequest request,
                                        HttpServletResponse response,
                                        @PathVariable String datasetId) throws Exception {

    	log.info("Provisioning DatasetFlow request. datasetId={}, datasetFlowVO={}", datasetId);

        // 1. 기 존재여부 체크
    	DatasetFlowBaseVO datasetFlowBaseVO = datasetFlowSVC.getDatasetFlowBaseVOById(datasetId);
        if(datasetFlowBaseVO == null) {
        	throw new BadRequestException(DataManagerCode.ErrorCode.NOT_EXISTS_DATASETFLOW, "Not Exists. datasetId=" + datasetId);
        }

        // 2. DatasetFlow 정보 수정 및 Provisioning
        String requestUri = request.getRequestURI().replace("/provisioning", "");
        datasetFlowSVC.provisioningDatasetFlow(datasetFlowBaseVO, requestUri);

        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    /**
     * retrieve dataset flow by id
     * @param response HttpServletResponse
     * @param accept request http accept header
     * @param datasetId dataset id
     * @return data model data
     * @throws Exception retrieve error
     */
    @GetMapping("/datasets/{datasetId:.+}/flow")
    public @ResponseBody
    DatasetFlowVO getDatasetFlowById(HttpServletResponse response,
                                   @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                   @PathVariable String datasetId) throws Exception {

        return datasetFlowSVC.getDatasetFlowVOById(datasetId);

    }
    
    /**
     * 데이터 셋 흐름 정보 생성 시 필수 파라미터 확인
     * @param datasetFlowVO 데이터 셋 흐름VO
     */
    private void validateParam(DatasetFlowVO datasetFlowVO) {
		if(ValidateUtil.isEmptyData(datasetFlowVO.getDatasetId())) {
        	throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'datasetId'");
        }
        
        if(datasetFlowVO.getHistoryStoreType() == null) {
        	throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'historyStoreType'");
        }
        
        if(ValidateUtil.isEmptyData(datasetFlowVO.getTargetTypes())) {
        	throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'targetTypes'");
        }
        
        for(TargetTypeVO targetType : datasetFlowVO.getTargetTypes()) {
        	if(targetType.getType() == null) {
        		throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'targetType.target'");
        	}
        }
	}

}
