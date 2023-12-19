package kr.re.keti.sc.dataservicebroker.common.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.Operation;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.OperationOption;
import kr.re.keti.sc.dataservicebroker.common.serialize.ContentDeserializer;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class IngestMessageVO {
	/** Operation */
	private Operation operation; // Mandatory
	/** Content Type */
	private String contentType; // Optional
	@JsonDeserialize(using = ContentDeserializer.class)
	/** Entity */
	private String content; // Optional
	/** event발생일시 */
	@JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
	private Date ingestTime; // Optional
	/** Operation options */
	private List<OperationOption> OperationOptions;

	private String datasetId;

	/** NGSI-LD 규격의 요청을 수신했을 때 uri 를 담기 위한 변수 */
	private String to;
	private String id;
	private String entityType;
	private List<String> links;
}