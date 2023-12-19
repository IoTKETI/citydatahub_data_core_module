package kr.re.keti.sc.datamanager.common.serialize;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import kr.re.keti.sc.datamanager.common.code.Constants;

/**
 * 다중 DataFormat 파싱을 위한 Serializer 클래스
 */
public class MultiDateDeserializer extends StdDeserializer<Date> {
    private static final long serialVersionUID = 1L;

    private static List<DateTimeFormatter> dateFormats = new ArrayList<>();

    static {
    	for (String dateFormat : Constants.MULTI_DATE_FORMATS) {
    		dateFormats.add(DateTimeFormatter.ofPattern(dateFormat));
    	}
    }

    public MultiDateDeserializer() {
        this(null);
    }

    public MultiDateDeserializer(Class<?> vc) {
        super(vc);
    }

    /**
     * <pre>
     * ngsi-ld에서 정의하는 다수의 데이터포맷을 deserialize 처리
     *  - "yyyy-MM-dd'T'HH:mm:ssXXX",
     *  - "yyyy-MM-dd'T'HH:mm:ss.SXXX",
     *  - "yyyy-MM-dd'T'HH:mm:ss.SSXXX",
     *  - "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
     *  - "yyyy-MM-dd'T'HH:mm:ss.SSSSXXX",
     *  - "yyyy-MM-dd'T'HH:mm:ss.SSSSSXXX",
     *  - "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
     *  - "yyyy-MM-dd'T'HH:mm:ss,SSSXXX" (Spec에서 정의하진 않지만 하위호환을 위해 추가된 패턴)
     * </pre>
     * @param jp JsonParser
     * @param ctxt DeserializationContext
     * @return Date Date Object
     * @throws IOException
     * @throws JsonProcessingException
     */
    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        final String date = node.textValue();

        for (DateTimeFormatter dateFormat : dateFormats) {
        	try {
        		return Date.from(LocalDateTime.parse(date, dateFormat).atZone(ZoneId.systemDefault()).toInstant());
        	} catch(DateTimeParseException e) {
        		
        	} catch (Exception e) {
        		
        	}
        }
        throw new JsonParseException(jp, "Unparseable date: " + date + ". Supported formats: " + Arrays.toString(Constants.MULTI_DATE_FORMATS));
    }
}
