package kr.re.keti.sc.datacoreui.api.datamodel.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Context VO class.
 * @FileName ContextVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Data
public class ContextVO {
	@JsonProperty("@context")
	private Object context; // The context can contain any string, object, or array object.
}
