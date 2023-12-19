package kr.re.keti.sc.datacoreui.api.provisionserver.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * This is the VO class used when responding to the list of provision server.
 * @FileName ProvisionServerListResponseVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProvisionServerListResponseVO {
	/** Total count */
	private Integer totalCount;
	/** Provision Server Response */
	private List<ProvisionServerResponseVO> provisionServerResponseVO;
}
