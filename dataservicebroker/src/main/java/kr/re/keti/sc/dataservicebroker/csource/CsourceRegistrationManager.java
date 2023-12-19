package kr.re.keti.sc.dataservicebroker.csource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.csource.service.CsourceRegistrationSVC;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationVO;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CsourceRegistrationManager {

    @Autowired
    private CsourceRegistrationSVC csourceRegistrationSVC;
    
    /** cache load lock object */
    private final Object lock = new Object();
    
    private Map<String, CsourceRegistrationVO> csourceCache;
    
    public CsourceRegistrationManager() {
        csourceCache = new ConcurrentHashMap<>();
    }
    
    @PostConstruct
    private void loadAllCache() {
        synchronized (lock) {

            // Csource 캐쉬 로딩
            List<CsourceRegistrationVO> csource = csourceRegistrationSVC.queryCsourceRegistrations(new QueryVO());
            if (csource != null) {
                for (CsourceRegistrationVO csourceRegistrationVO : csource) {
                    putCsourceRegistrationCache(csourceRegistrationVO);
                }
            }
        }
    }
    
    public CsourceRegistrationVO putCsourceRegistrationCache(CsourceRegistrationVO csourceRegistrationVO) {
		log.info("PUT Csource cache. id={}", csourceRegistrationVO.getId());
		return this.csourceCache.put(csourceRegistrationVO.getId(), csourceRegistrationVO);
	}

	public CsourceRegistrationVO getCsourceRegistrationCache(String key) {
		return this.csourceCache.get(key);
	}

	public List<CsourceRegistrationVO> getCsourceRegistrationAllCache() {
		return new ArrayList<>(csourceCache.values());
	}
	
	public List<CsourceRegistrationVO> getCsourceRegistrationCacheByIds(List<String> csourceRegistrationIds) {
		List<CsourceRegistrationVO> result = null;
		for (CsourceRegistrationVO csourceRegistrationVO : csourceCache.values()) {
			if (csourceRegistrationIds.contains(csourceRegistrationVO.getId())) {
				if (result == null) {
					result = new ArrayList<>();
				}
				result.add(csourceRegistrationVO);
			}
		}
		return result;
	}
	
}
