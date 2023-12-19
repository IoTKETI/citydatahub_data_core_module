package kr.re.keti.sc.datamanager.provisionserver.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import kr.re.keti.sc.datamanager.common.code.Constants;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ErrorCode;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionProtocol;
import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionServerType;
import kr.re.keti.sc.datamanager.common.datamapperhandler.ProvisionProtocolTypePropertyEditor;
import kr.re.keti.sc.datamanager.common.datamapperhandler.ProvisionServerTypePropertyEditor;
import kr.re.keti.sc.datamanager.common.exception.BadRequestException;
import kr.re.keti.sc.datamanager.provisionserver.service.ProvisionServerSVC;
import kr.re.keti.sc.datamanager.provisionserver.vo.ProvisionServerBaseVO;
import kr.re.keti.sc.datamanager.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Provisining 대상 서버 관리 HTTP Controller 클래스
 */
@RestController
@Slf4j
public class ProvisionServerController {

    @Autowired
    private ProvisionServerSVC provisionServerSVC;


    /**
     * create provision server
     * @param response HttpServletResponse
     * @param provisionServerBaseVO create provision server data
     * @throws Exception create error
     */
    @PostMapping("/provision/servers")
    public void create(HttpServletResponse response, @RequestBody ProvisionServerBaseVO provisionServerBaseVO) throws Exception {

        log.info("Provision Server CREATE request msg='{}'", provisionServerBaseVO);

        // 1. 파라미터 유효성 검증
        validateParam(provisionServerBaseVO);

        // 2. 기 존재 데이터 확인
        if(provisionServerSVC.getProvisionServerVOById(provisionServerBaseVO.getId()) != null) {
        	throw new BadRequestException(ErrorCode.ALREADY_EXISTS, "Already exists. id=" + provisionServerBaseVO.getId());
        }

        // 3. 프로비전 서버 생성
        if(provisionServerBaseVO.getEnabled() == null) {
        	provisionServerBaseVO.setEnabled(true);
        }
        provisionServerSVC.createProvisionServer(provisionServerBaseVO);

        response.setStatus(HttpStatus.CREATED.value());
    }


    /**
     * update provision server
     * @param response HttpServletResponse
     * @param id provision server id
     * @param provisionServerBaseVO update provision server data
     * @throws Exception update error
     */
    @PatchMapping("/provision/servers/{id}")
    public void update(HttpServletResponse response, @PathVariable String id, @RequestBody ProvisionServerBaseVO provisionServerBaseVO) throws Exception {

        log.info("Provision Server UPDATE request id=" + id);

        provisionServerBaseVO.setId(id);

        // 1. Provision 서버 정보 수정
        int result = provisionServerSVC.updateProvisionServer(provisionServerBaseVO);

        // 2. 요청 결과 확인
        if (result > 0) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            throw new BadRequestException(DataManagerCode.ErrorCode.NOT_EXIST_ID, "Not Exists. id=" + id);
        }
    }

    /**
     * delete provision server
     * @param response HttpServletResponse
     * @param id provision server id
     * @throws Exception delete error
     */
    @DeleteMapping("/provision/servers/{id}")
    public void delete(HttpServletResponse response, @PathVariable String id) throws Exception {

        log.info("Provision Server DELETE request id=" + id);

        // 1. Provision Server 정보 삭제
        int result = provisionServerSVC.deleteProvisionServer(id);

        // 2. 요청 결과 확인
        if (result > 0) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            throw new BadRequestException(DataManagerCode.ErrorCode.NOT_EXIST_ID, "Not Exists. id=" + id);
        }
    }

    /**
     * retrieve provision server list
     * @param response HttpServletResponse
     * @param accept request http accept header
     * @param provisionServerBaseVO retrieve condition
     * @return provision server data
     * @throws Exception retrieve error
     */
    @GetMapping(value = "/provision/servers")
    public @ResponseBody
    List<ProvisionServerBaseVO> getProvisionServers(HttpServletResponse response,
                                                    @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                                    ProvisionServerBaseVO provisionServerBaseVO) throws Exception {

    	// 1. 리소스 전체 갯수 조회
    	Integer totalCount = provisionServerSVC.getProvisionServerTotalCount(provisionServerBaseVO);
    	
    	// 2. 리소스 전체 갯수 헤더에 추가
        response.addHeader(Constants.TOTAL_COUNT, Integer.toString(totalCount));

        // 3. Provision Server 리스트 조회
        List<ProvisionServerBaseVO> provisionServerBaseVOs = provisionServerSVC.getProvisionServerVOList(provisionServerBaseVO);
        return provisionServerBaseVOs;
    }

    /**
     * retrieve provision server by id
     * @param response HttpServletResponse
     * @param accept request http accept header
     * @param id provision server id
     * @return ProvisionServerBaseVO provision server data
     * @throws Exception retrieve error
     */
    @GetMapping("/provision/servers/{id}")
    public @ResponseBody
    ProvisionServerBaseVO getProvisionServerById(HttpServletResponse response,
                                   @RequestHeader(HttpHeaders.ACCEPT) String accept,
                                   @PathVariable String id) throws Exception {

        return provisionServerSVC.getProvisionServerVOById(id);

    }
    
    /**
     * 필수 파라미터 유효성 체크
     * @param provisionServerBaseVO provisionServer기본VO
     */
    private void validateParam(ProvisionServerBaseVO provisionServerBaseVO) {
		if(ValidateUtil.isEmptyData(provisionServerBaseVO.getId())) {
        	throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'id'");
        }
        if(ValidateUtil.isEmptyData(provisionServerBaseVO.getProvisionUri())) {
        	throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'ProvisionUri'");
        }
        if(provisionServerBaseVO.getType() == null) {
        	throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'Type'");
        }
        if(provisionServerBaseVO.getProvisionProtocol() == null) {
        	throw new BadRequestException(ErrorCode.INVALID_PARAMETER, "Not found 'ProvisionProtocol'");
        }
	}

    @InitBinder
	public void initBinder(WebDataBinder dataBinder) {
	    dataBinder.registerCustomEditor(ProvisionServerType.class, new ProvisionServerTypePropertyEditor());
	    dataBinder.registerCustomEditor(ProvisionProtocol.class, new ProvisionProtocolTypePropertyEditor());
	}
    
}
