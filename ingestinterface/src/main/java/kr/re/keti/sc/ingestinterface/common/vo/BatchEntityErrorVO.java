package kr.re.keti.sc.ingestinterface.common.vo;

import kr.re.keti.sc.ingestinterface.common.exception.ErrorPayload;

/**
 * Ngsi-ld spec batch entity error VO Class
 */
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
