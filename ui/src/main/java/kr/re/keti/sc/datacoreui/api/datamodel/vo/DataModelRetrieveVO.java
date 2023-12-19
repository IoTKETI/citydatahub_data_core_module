package kr.re.keti.sc.datacoreui.api.datamodel.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

/**
 * This is the VO class used for data model retrieve.
 * @FileName DataModelRetrieveVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class DataModelRetrieveVO {
	/** DataModel ID */
	private String id;
	/** DataModel type */
	private String type;
	/** DataModel type Uri */
	private String typeUri;
	/** DataModel name */
	private String name;
	/** limit */
	private Integer limit;
	/** offset */
	private Integer offset;
}
