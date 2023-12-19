package kr.re.keti.sc.dataservicebroker.common.vo;

import kr.re.keti.sc.dataservicebroker.common.exception.ErrorPayload;

public class BatchEntityErrorVO {

    String entityId;
    ErrorPayload error;

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public ErrorPayload getError() {
        return error;
    }

    public void setError(ErrorPayload error) {
        this.error = error;
    }
}
