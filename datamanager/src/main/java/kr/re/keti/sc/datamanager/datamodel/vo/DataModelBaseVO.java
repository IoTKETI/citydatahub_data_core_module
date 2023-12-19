package kr.re.keti.sc.datamanager.datamodel.vo;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import kr.re.keti.sc.datamanager.common.vo.PageRequest;
import lombok.Data;

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
	private Date modifyDatetime;
	private String modifierId;

	private String provisioningRequestId;
	private Date provisioningEventTime;
}
