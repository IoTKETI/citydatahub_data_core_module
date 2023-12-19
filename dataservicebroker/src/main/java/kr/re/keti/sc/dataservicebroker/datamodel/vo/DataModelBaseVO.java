package kr.re.keti.sc.dataservicebroker.datamodel.vo;

import java.util.Date;
import java.util.List;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import lombok.Data;

@Data
public class DataModelBaseVO {
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
	private List<BigDataStorageType> createdStorageTypes;
	private String storageMetadata;
}