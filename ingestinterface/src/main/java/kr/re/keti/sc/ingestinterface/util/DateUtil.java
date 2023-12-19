package kr.re.keti.sc.ingestinterface.util;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import kr.re.keti.sc.ingestinterface.common.code.Constants;

/**
 * Date Parsing util class
 */
public class DateUtil {

	private static final DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern(Constants.POSTGRES_TIMESTAMP_FORMAT);
	private static final List<DateTimeFormatter> dateFormats = new ArrayList<>();

    static {
    	for (String dateFormat : Constants.MULTI_DATE_FORMATS) {
    		dateFormats.add(DateTimeFormatter.ofPattern(dateFormat));
    	}
    }

	public static String dateToDbFormatString(Date date) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of(Constants.CONTENT_DATE_TIMEZONE));
		return localDateTime.format(dbDateFormatter);
	}

	public static Date strToDate(String dateStr) throws ParseException {
		
		for (DateTimeFormatter dateFormat : dateFormats) {
        	try {
        		return Date.from(ZonedDateTime.parse(dateStr, dateFormat).toInstant());
        	} catch(DateTimeParseException e) {
        		
        	} catch (Exception e) {
        		
        	}
        }
		throw new ParseException("Invalid date format: " + dateStr + ". Supported formats: " + Arrays.toString(Constants.MULTI_DATE_FORMATS), 0);
	}

}
