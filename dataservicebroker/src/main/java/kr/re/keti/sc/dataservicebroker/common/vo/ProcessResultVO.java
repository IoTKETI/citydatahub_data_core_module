package kr.re.keti.sc.dataservicebroker.common.vo;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.Operation;
import kr.re.keti.sc.dataservicebroker.common.exception.BaseException;

public class ProcessResultVO {

	/** 처리 결과 */
	private Boolean processResult;
	/** 실제 DB 처리 Operation (Upsert 요청 시 Update 혹은 Create 로 설정됨) */
	private Operation processOperation;
	/** 에러 설명 */
	private String errorDescription;
	/** 처리 도중 Exception 발생한 경우 */
	private BaseException exception;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("processResult=").append(processResult)
		  .append(", processOperation=").append(processOperation);
		if(errorDescription != null) {
			sb.append(", errorDescription=").append(errorDescription);
		}

		if(exception != null) {
			sb.append(", errorCode=").append(exception.getErrorCode())
			  .append(", exception=").append(exception.toString());
			if(exception.getCause() != null) {
				sb.append(", exceptionCause=").append(exception.getCause());
			}
		}
		return sb.toString();
	}

	public Boolean isProcessResult() {
		return processResult;
	}
	public void setProcessResult(Boolean processResult) {
		this.processResult = processResult;
	}
	public Operation getProcessOperation() {
		return processOperation;
	}
	public void setProcessOperation(Operation processOperation) {
		this.processOperation = processOperation;
	}
	public String getErrorDescription() {
		return errorDescription;
	}
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
	public Exception getException() {
		return exception;
	}
	public void setException(BaseException exception) {
		this.exception = exception;
	}
}
