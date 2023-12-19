package kr.re.keti.sc.datacoreui.api.provisionserver.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kr.re.keti.sc.datacoreui.api.provisionserver.service.ProvisionServerSVC;
import kr.re.keti.sc.datacoreui.api.provisionserver.vo.ProvisionServerListResponseVO;
import kr.re.keti.sc.datacoreui.api.provisionserver.vo.ProvisionServerResponseVO;
import kr.re.keti.sc.datacoreui.api.provisionserver.vo.ProvisionServerRetrieveVO;
import kr.re.keti.sc.datacoreui.api.provisionserver.vo.ProvisionServerVO;
import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode.ErrorCode;
import kr.re.keti.sc.datacoreui.common.exception.BadRequestException;
import kr.re.keti.sc.datacoreui.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for provision server management API calls.
 * @FileName ProvisionServerController.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Slf4j
@RestController
public class ProvisionServerController {

	@Autowired
	private ProvisionServerSVC provisionServerSVC;
	
	/**
	 * Create Provisioning server
	 * @param provisionServer	ProvisionServerVO object
	 * @return					Result of provision server creation.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@PostMapping(value="/provision/servers")
	public <T> ResponseEntity<T> createProvisionServer(HttpServletRequest request, HttpServletResponse response,
			@RequestBody ProvisionServerVO provisionServer) throws Exception {
		
		log.info("[UI API] createProvisionServer - provisionServer: {}", provisionServer);
		
		// 1. Check required values
		if(!checkMandatoryProvisionServerValues(provisionServer)) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST, "Missing required value.");
		}
		
		// 2. Create Provision server
		ResponseEntity<T> reslt = provisionServerSVC.createProvisionServer(provisionServer);
		
		return reslt;
	}
	
	/**
	 * Update Provisioning server
	 * @param id                Provision server ID
	 * @param provisionServer   ProvisionServerVO object (update data)
	 * @return					Result of update provision server.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@PatchMapping(value="/provision/servers/{id}")
	public <T> ResponseEntity<T> updateProvisionServer(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id,
			@RequestBody ProvisionServerVO provisionServer) throws Exception {
		
		log.info("[UI API] updateProvisionServer - id: {}, provisionServer: {}", id, provisionServer);
		
		// 1. Update Provision server
		ResponseEntity<T> reslt = provisionServerSVC.updateProvisionServer(id, provisionServer);
		
		// 2. When the modification is successful, the response status is set to OK.
		return reslt;
	}
	
	/**
	 * Delete Provisioning server
	 * @param id                Provision server ID
	 * @return					Result of delete provision server.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@DeleteMapping(value="/provision/servers/{id}")
	public <T> ResponseEntity<T> deleteProvisionServer(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id) throws Exception {
		
		log.info("[UI API] deleteProvisionServer - id: {}", id);
		
		// 1. Delete Provisioning server
		ResponseEntity<T> reslt = provisionServerSVC.deleteProvisionServer(id);
		
		return reslt;
	}
	
	/**
	 * Retrieve Provisioning server
	 * @param id                Provision server ID
	 * @return					Provision server information retrieved by provision server ID.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@GetMapping(value="/provision/servers/{id}")
	public ResponseEntity<ProvisionServerResponseVO> getProvisionServer(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id) throws Exception {
		
		log.info("[UI API] getProvisionServer - id: {}", id);
		
		// 1. Retrieve Provisioning server
		ResponseEntity<ProvisionServerResponseVO> provisionServer = provisionServerSVC.getProvisionServer(id);
		
		return provisionServer;
	}
	
	/**
	 * Retrieve multiple Provisioning server
	 * @param provisionServer   ProvisionServerRetrieveVO object (search condition)
	 * @return					List of provision server information retrieved by provision server ID.
	 * @throws Exception		Throw an exception when an error occurs.
	 */
	@GetMapping(value="/provision/servers")
	public ResponseEntity<ProvisionServerListResponseVO> getProvisionServers(HttpServletRequest request, HttpServletResponse response,
			ProvisionServerRetrieveVO provisionServerRetrieve) throws Exception {
		
		log.info("[UI API] getProvisionServers - provisionServerRetrieve: {}", provisionServerRetrieve);
		
		// Retrieve multiple Provisioning server
		ResponseEntity<ProvisionServerListResponseVO> provisionServers = provisionServerSVC.getProvisionServers(provisionServerRetrieve);
		
		return provisionServers;
	}

	/**
	 * Check Provision Server Required Values
	 * @param 	provisionServer
	 * @return	valid: true, invalid: false
	 */
	private boolean checkMandatoryProvisionServerValues(ProvisionServerVO provisionServer) {
		if(provisionServer == null) {
			return false;
		}
		
		if(ValidateUtil.isEmptyData(provisionServer.getId())
				|| ValidateUtil.isEmptyData(provisionServer.getType())
				|| ValidateUtil.isEmptyData(provisionServer.getProvisionUri())
				|| ValidateUtil.isEmptyData(provisionServer.getProvisionProtocol())
				|| ValidateUtil.isEmptyData(provisionServer.getEnabled())) {
			return false;
		}
		
		return true;
	}
}
