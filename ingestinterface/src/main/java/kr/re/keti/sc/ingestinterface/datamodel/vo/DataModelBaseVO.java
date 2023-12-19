package kr.re.keti.sc.ingestinterface.datamodel.vo;

import java.util.Date;

import kr.re.keti.sc.ingestinterface.common.vo.PageRequest;
import lombok.Data;

/**
 * Data model DB VO class
 */
@Data
public class DataModelBaseVO extends PageRequest {
	private String id;
	private String type;
	private String typeUri;
	private String name;
	private String description;
	private String dataModel;
	private Boolean enabled;
	private Date createDatetime;
	private String creatorId;
	private String modifyDatetime;
	private String modifierId;

	private String provisioningRequestId;
	private Date provisioningEventTime;
}
