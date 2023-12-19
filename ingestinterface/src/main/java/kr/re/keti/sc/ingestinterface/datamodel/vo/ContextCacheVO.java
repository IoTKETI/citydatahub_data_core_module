package kr.re.keti.sc.ingestinterface.datamodel.vo;

import java.util.Map;

import lombok.Data;

/**
 * Context cache VO class
 */
@Data
public class ContextCacheVO {
	private String contextUri;
	private Map<String, String> contextMap;
}
