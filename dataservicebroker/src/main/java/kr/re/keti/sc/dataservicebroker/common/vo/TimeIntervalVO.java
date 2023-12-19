package kr.re.keti.sc.dataservicebroker.common.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeIntervalVO {
    private Date startAt;
    private Date endAt;

    public TimeIntervalVO() { }

    public TimeIntervalVO(Date startAt, Date endAt) {
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
