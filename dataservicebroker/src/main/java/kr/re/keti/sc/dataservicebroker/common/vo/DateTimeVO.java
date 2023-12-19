package kr.re.keti.sc.dataservicebroker.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.re.keti.sc.dataservicebroker.common.code.Constants;

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
