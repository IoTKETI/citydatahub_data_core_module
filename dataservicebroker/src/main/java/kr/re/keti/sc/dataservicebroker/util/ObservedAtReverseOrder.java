package kr.re.keti.sc.dataservicebroker.util;

import kr.re.keti.sc.dataservicebroker.common.vo.AttributeVO;

import java.util.Comparator;

public class ObservedAtReverseOrder implements Comparator<AttributeVO> {

    /**
     * observedAt 기준 내림차순 정렬
     * @param o1
     * @param o2
     * @return
     */
    @Override
    public int compare(AttributeVO o1, AttributeVO o2) {
        return o2.getObservedAt().compareTo(o1.getObservedAt());
    }
}
