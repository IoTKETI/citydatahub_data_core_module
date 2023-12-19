package kr.re.keti.sc.datacoreui.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Client exception payload VO class
 * @FileName ClientExceptionPayloadVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientExceptionPayloadVO {
	/** Client exception type */
	private String type;
	/** Client exception title */
	private String title;
	/** Client exception detail */
	private String detail;
}
