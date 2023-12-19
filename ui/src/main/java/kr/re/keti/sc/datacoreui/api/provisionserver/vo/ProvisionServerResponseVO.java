package kr.re.keti.sc.datacoreui.api.provisionserver.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This is the VO class used when responding to the provision server.
 * @FileName ProvisionServerResponseVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProvisionServerResponseVO extends ProvisionServerVO {
	/** Creator ID */
	private String creatorId;
	/** Creation date */
	private String createdAt;
	/** Modifier ID */
	private String modifierId;
	/** Modified date */
	private String modifiedAt;
}
