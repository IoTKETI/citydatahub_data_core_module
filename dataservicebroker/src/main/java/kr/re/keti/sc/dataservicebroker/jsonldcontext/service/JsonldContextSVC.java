package kr.re.keti.sc.dataservicebroker.jsonldcontext.service;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.jsonldcontext.dao.JsonldContextDAO;
import kr.re.keti.sc.dataservicebroker.jsonldcontext.vo.JsonldContextBaseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class JsonldContextSVC {

    @Autowired
    private JsonldContextDAO jsonldContextDAO;

	public void upsertJsonldContext(String url, String payload, String refinedPayload, Date expireTime) {
		log.info("upsertJsonldContext url={}, expireTime={}, payload={}", url, expireTime, payload);
		JsonldContextBaseVO jsonldContextBaseVO = new JsonldContextBaseVO();
		jsonldContextBaseVO.setUrl(url);
		jsonldContextBaseVO.setPayload(payload);
		jsonldContextBaseVO.setRefinedPayload(refinedPayload);
		jsonldContextBaseVO.setKind(DataServiceBrokerCode.JsonldContextKind.CACHED);
		jsonldContextBaseVO.setExpireDatetime(expireTime);
		jsonldContextDAO.upsertJsonldContext(jsonldContextBaseVO);
    }

    public List<JsonldContextBaseVO> getJsonldContextList(Date expireTime, DataServiceBrokerCode.JsonldContextKind jsonldContextKind) {
		JsonldContextBaseVO retrieveJsonldContextBaseVO = new JsonldContextBaseVO();
		retrieveJsonldContextBaseVO.setExpireDatetime(expireTime);
		retrieveJsonldContextBaseVO.setKind(jsonldContextKind);
    	return jsonldContextDAO.getJsonldContextBaseVOList(retrieveJsonldContextBaseVO);
    }

    public JsonldContextBaseVO getJsonldContextByUrl(String url, Date expireTime) {
		JsonldContextBaseVO retrieveJsonldContextBaseVO = new JsonldContextBaseVO();
		retrieveJsonldContextBaseVO.setUrl(url);
		retrieveJsonldContextBaseVO.setExpireDatetime(expireTime);
    	return jsonldContextDAO.getJsonldContextBaseVOById(retrieveJsonldContextBaseVO);
    }
}

