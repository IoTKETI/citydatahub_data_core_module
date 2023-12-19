package kr.re.keti.sc.dataservicebroker.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.joda.time.DateTime;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;

public class DateUtil {

	private static final DateFormat postgresFormat = new SimpleDateFormat(Constants.POSTGRES_TIMESTAMP_FORMAT);
	private static final DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern(Constants.POSTGRES_TIMESTAMP_FORMAT);
	private static final List<DateTimeFormatter> dateFormats = new ArrayList<>();

    static {
    	for (String dateFormat : Constants.MULTI_DATE_FORMATS) {
    		dateFormats.add(DateTimeFormatter.ofPattern(dateFormat));
    	}
    }
    
    private static final List<DateTimeFormatter> hiveDateFormats = new ArrayList<>();

    static {
    	for (String dateFormat : Constants.HIVE_MULTI_DATE_FORMATS) {
    		hiveDateFormats.add(DateTimeFormatter.ofPattern(dateFormat));
    	}
    }    
    
//	public static String dateToDbFormatString(Date date, ZoneId zoneId) {
//		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
//		return zonedDateTime.format(dbDateFormatter);
//	}
	
    public static String dateToDbFormatString(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), TimeZone.getDefault().toZoneId());
        return localDateTime.format(dbDateFormatter);
    }

	public static Date strToDate(String dateStr) throws ParseException {

		if(ValidateUtil.isEmptyData(dateStr)) {
			return null;
		}
		
		for (DateTimeFormatter dateFormat : dateFormats) {
        	try {
        		return Date.from(ZonedDateTime.parse(dateStr, dateFormat).toInstant());
        	} catch(DateTimeParseException e) {
        		
        	} catch (Exception e) {
        		
        	}
        }
		throw new ParseException("Invalid date format: " + dateStr + ". Supported formats: " + Arrays.toString(Constants.MULTI_DATE_FORMATS), 0);
	}
	
	/**
	 *
	 * 쿼리 요청 Timestamp 패턴을 DB조회용 timestamp 조건으로 변경
	 * 2019-06-08T15:00:00,000+09:00 -> '2019-06-08T15:00:00'
	 * @param timeValue
	 * @return
	 * @throws ParseException
	 */
	public static String convertQueryTsToDBTs(String timeValue) throws ParseException {

		//인코딩으로 인해 '+' 문자열이 없어짐
		timeValue = timeValue.replace(" ", "+");

		Date queryDate = strToDate(timeValue);
		timeValue = "'" + postgresFormat.format(queryDate) + "'";
		return timeValue;
	}
	
	public static Date convertHiveTsToDate(String timestampStr) throws ParseException {
		if(ValidateUtil.isEmptyData(timestampStr)) {
			return null;
		}
		
		for (DateTimeFormatter dateFormat : hiveDateFormats) {
        	try {
        		return Date.from(LocalDateTime.parse(timestampStr, dateFormat).toInstant(ZoneOffset.UTC));
        		
//        		return Date.from(Date.parse(timestampStr, dateFormat).toInstant());
        	} catch(DateTimeParseException e) {
        		
        	} catch (Exception e) {
        		
        	}
        }
		throw new ParseException("Invalid Hive date format: " + timestampStr + ". Supported formats: " + Arrays.toString(Constants.HIVE_MULTI_DATE_FORMATS), 0);
		
	}


}
