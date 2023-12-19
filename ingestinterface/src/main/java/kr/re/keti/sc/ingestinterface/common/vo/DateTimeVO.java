package kr.re.keti.sc.ingestinterface.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.re.keti.sc.ingestinterface.common.code.Constants;

/**
 * Ngsi-ld DateTime VO class
 */
public class DateTimeVO {

    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Object value;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
