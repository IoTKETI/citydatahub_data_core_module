package kr.re.keti.sc.datacoreui.api.provisionserver.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * This is the VO class used for provision server role retrieve.
 * @FileName ProvisionServerRetrieveVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProvisionServerRetrieveVO {
	/** Provision server type */
	private String type;
	/** Provision protocol */
	private String provisionProtocol;
	/** Enabled */
	private Boolean enabled;
	/** limit */
	private Integer limit;
	/** offset */
	private Integer offset;
}
