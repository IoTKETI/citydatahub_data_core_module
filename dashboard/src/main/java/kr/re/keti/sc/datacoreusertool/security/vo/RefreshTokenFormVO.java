package kr.re.keti.sc.datacoreusertool.security.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * RefreshTokenFormVO class
 * @FileName RefreshTokenFormVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefreshTokenFormVO {
	private String grant_type;
	private String refresh_token;
}
