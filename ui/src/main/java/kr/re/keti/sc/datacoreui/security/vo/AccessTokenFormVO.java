package kr.re.keti.sc.datacoreui.security.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * AccessTokenFormVO class
 * @FileName AccessTokenFormVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessTokenFormVO {
	private String grant_type;
	private String code;
	private String redirect_uri;
	private String client_id;
	private String client_secret;
}
