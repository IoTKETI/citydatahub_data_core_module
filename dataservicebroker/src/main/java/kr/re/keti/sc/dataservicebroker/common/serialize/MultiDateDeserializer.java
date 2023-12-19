package kr.re.keti.sc.dataservicebroker.common.serialize;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;

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

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        final String date = node.textValue();

        for (DateTimeFormatter dateFormat : dateFormats) {
        	try {
        		return Date.from(ZonedDateTime.parse(date, dateFormat).toInstant());
        	} catch(DateTimeParseException e) {
        		
        	} catch (Exception e) {
        		
        	}
        }
        throw new JsonParseException(jp, "Unparseable date: " + date + ". Supported formats: " + Arrays.toString(Constants.MULTI_DATE_FORMATS));
    }
}
