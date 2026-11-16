package kr.re.keti.sc.dataservicebroker.common.vo;

import java.util.List;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.Operation;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.OperationOption;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import lombok.Data;

@Data
public class EntityProcessVO<T1 extends CommonEntityFullVO, T2 extends CommonEntityDaoVO> {
	/** 요청 reqeust id */
	private String reqeustId;
	/** 요청 e2e reqeust id */
	private String e2eReqeustId;
	/** 데이터셋아이디 */
	private String datasetId;
	/** 엔티티아이디 */
	private String entityId;
	/** 요청 수신 받은 content */
	private String content;
	/** Operation */
	private Operation operation;
	/** 수신받은 데이터 VO */
	private T1 entityFullVO;
	/** Operation 별 DB 처리 시 사용될 VO */
	private T2 entityDaoVO;
	/** Entity 파싱 시 사용될 DataModel 정보 */
	private DataModelCacheVO dataModelCacheVO;
	/** 처리결과 VO */
	private ProcessResultVO processResultVO = new ProcessResultVO();
	/** 처리Options */
	private List<OperationOption> operationOptions;
}
