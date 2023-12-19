package kr.re.keti.sc.datacoreusertool.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Client exception payload VO class
 * @FileName ClientExceptionPayloadVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientExceptionPayloadVO {
	/** Type of client error */
	private String type;
	/** Title of client error */
	private String title;
	/** Detail of client error */
	private String detail;
}
