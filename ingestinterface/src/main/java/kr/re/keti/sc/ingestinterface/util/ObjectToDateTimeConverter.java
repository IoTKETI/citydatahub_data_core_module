package kr.re.keti.sc.ingestinterface.util;

import kr.re.keti.sc.ingestinterface.common.vo.DateTimeVO;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;


@Configuration
public class ObjectToDateTimeConverter implements Converter<Object, DateTimeVO> {

    @Override
    public DateTimeVO convert(Object o) {

        DateTimeVO dateTimeVO = new DateTimeVO();
        dateTimeVO.setValue(o);

        return dateTimeVO;
    }


}
