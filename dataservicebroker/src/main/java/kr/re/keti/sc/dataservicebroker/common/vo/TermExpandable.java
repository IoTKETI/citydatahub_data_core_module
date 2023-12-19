package kr.re.keti.sc.dataservicebroker.common.vo;

import java.util.Map;

public interface TermExpandable {

	void expandTerm(Map<String, String> dataModelContextMap);

	void expandTerm(Map<String, String> requestContextMap, Map<String, String> dataModelContextMap);

}
