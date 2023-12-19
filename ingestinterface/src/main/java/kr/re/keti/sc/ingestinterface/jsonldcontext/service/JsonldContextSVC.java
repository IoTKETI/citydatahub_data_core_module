package kr.re.keti.sc.ingestinterface.jsonldcontext.service;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.jsonldcontext.dao.JsonldContextDAO;
import kr.re.keti.sc.ingestinterface.jsonldcontext.vo.JsonldContextBaseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Jsonld context service class
 */
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
		jsonldContextBaseVO.setKind(IngestInterfaceCode.JsonldContextKind.CACHED);
		jsonldContextBaseVO.setExpireDatetime(expireTime);
		jsonldContextDAO.upsertJsonldContext(jsonldContextBaseVO);
    }

    public List<JsonldContextBaseVO> getJsonldContextList(Date expireTime, IngestInterfaceCode.JsonldContextKind jsonldContextKind) {
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

