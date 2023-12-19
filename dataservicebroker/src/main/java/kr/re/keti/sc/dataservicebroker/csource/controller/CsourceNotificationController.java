package kr.re.keti.sc.dataservicebroker.csource.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kr.re.keti.sc.dataservicebroker.csource.CsourceRegistrationManager;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationVO;
import kr.re.keti.sc.dataservicebroker.notification.vo.CsourceNotificationVO;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CsourceNotificationController {
	
	@Autowired
	private CsourceRegistrationManager csourceRegistrationManager;
	
	/**
	 * context source noti 수신
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @param csourceNotificationVO csource Notification data
	 * @throws Exception
	 */
	@PostMapping("/csourceNotifications")
    public void notiContextSource(HttpServletRequest request,
								  HttpServletResponse response,
								  @RequestBody CsourceNotificationVO csourceNotificationVO) throws Exception {

		
		log.info("CsourceNotificationController Received Notification. csourceNotificationVO={}", csourceNotificationVO);
		
		if(csourceNotificationVO != null) {
			List<CsourceRegistrationVO> datas = csourceNotificationVO.getData();
			if(datas != null) {
				for(CsourceRegistrationVO csourceRegistrationVO : datas) {
					csourceRegistrationManager.putCsourceRegistrationCache(csourceRegistrationVO);
				}
			}
		}
	}

}
