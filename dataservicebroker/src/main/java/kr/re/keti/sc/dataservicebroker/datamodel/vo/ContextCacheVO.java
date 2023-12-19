package kr.re.keti.sc.dataservicebroker.datamodel.vo;

import java.util.Map;

import lombok.Data;

@Data
public class ContextCacheVO {
	private String contextUri;
	private Map<String, String> contextMap;
}
