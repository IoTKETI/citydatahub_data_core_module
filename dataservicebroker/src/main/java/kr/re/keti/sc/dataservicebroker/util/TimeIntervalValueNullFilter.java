package kr.re.keti.sc.dataservicebroker.util;

import kr.re.keti.sc.dataservicebroker.common.vo.TimeIntervalVO;


public class TimeIntervalValueNullFilter {


    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TimeIntervalVO)) {
            return true;
        }

        TimeIntervalVO timeIntervalVO = (TimeIntervalVO) obj;
        if (timeIntervalVO.getStartAt() != null) {
            return false;
        }
        if (timeIntervalVO.getEndAt() != null) {
            return false;
        }

        return true;
    }
}
