package kr.re.keti.sc.datacoreui.api.provisionserver.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * Provision server VO class
 * @FileName ProvisionServerVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProvisionServerVO {
	/** Provision server ID */
	private String id;
	/** Provision server type */
	private String type;
	/** Provision server description */
	private String description;
	/** Provision URI */
	private String provisionUri;
	/** Provision protocol */
	private String provisionProtocol;
	/** Provisioning order */
	private Integer provisionOrder;
	/** Enabled */
	private Boolean enabled;
}
