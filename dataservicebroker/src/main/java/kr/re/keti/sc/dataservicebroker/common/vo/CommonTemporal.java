package kr.re.keti.sc.dataservicebroker.common.vo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;

public class CommonTemporal {


    private String type;

    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private List<List<Object>> values = new ArrayList<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<List<Object>> getValues() {
        return values;
    }

    public void setValues(List<List<Object>> values) {
        this.values = values;
    }
}
