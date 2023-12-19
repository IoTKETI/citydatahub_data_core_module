package kr.re.keti.sc.datacoreusertool.notification.vo;

import java.util.List;

import lombok.Data;

/**
 * WebSocketRegistVO class
 * @FileName WebSocketRegistVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Data
public class WebSocketRegistVO {
	private String subscriptionId;
	private List<String> entityIds;
}
