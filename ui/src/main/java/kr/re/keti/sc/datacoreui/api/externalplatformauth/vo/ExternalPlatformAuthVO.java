package kr.re.keti.sc.datacoreui.api.externalplatformauth.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * External platform auth VO class
 * @FileName ExternalPlatformAuthVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalPlatformAuthVO {
	/** External platform ID */
	private String id;
	/** External platform name */
	private String name; 
	/** Description of external platform */
	private String description;
	/** Receivable IP list */
	private List<String> receptionIps;
	/** List of Receivable Dataset IDs */
	private List<String> receptionDatasetIds;
	/** Data instance prefix */
	private String dataInstancePrefix;
	/** List of Receivable Client IDs */
	private List<String> receptionClientIds;
}
