package kr.re.keti.sc.datacoreusertool.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.time.FastDateFormat;

import kr.re.keti.sc.datacoreusertool.common.code.Constants;

/**
 * Utility for date type
 * @FileName DateUtil.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
public class DateUtil {

	private static final Object lockObject = new Object();
	private static final DateFormat dateFormat = new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT);
	private static final DateFormat retrieveDateFormat = new SimpleDateFormat(Constants.RETRIEVE_DATE_FORMAT);
	private static final DateFormat postgresFormat = new SimpleDateFormat(Constants.POSTGRES_TIMESTAMP_FORMAT);
	private static final DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern(Constants.POSTGRES_TIMESTAMP_FORMAT);
	
	private static FastDateFormat contentDate = FastDateFormat.getInstance(Constants.CONTENT_DATE_FORMAT, Locale.KOREA);
	private static FastDateFormat postgresDate = FastDateFormat.getInstance(Constants.POSTGRES_DATE_FORMAT, Locale.KOREA);
	
	/**
	 * Converts a date to a string in DB format.
	 * @param date	Date
	 * @return		DB format date value of string type
	 */
	public static String dateToDbFormatString(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), TimeZone.getDefault().toZoneId());
        return localDateTime.format(dbDateFormatter);
    }

	/**
	 * Convert string to date
	 * @param dateStr			String type date
	 * @return					Date
	 * @throws ParseException	Throw an exception when a parsing error occurs.
	 */
	public static Date strToDate(String dateStr) throws ParseException {
		synchronized(lockObject) {
			return dateFormat.parse(dateStr);
		}
	}

	/**
	 * Calculation of day/hour/minute/second +/- date and time relative to base time
	 * @param date		base time
	 * @param day		+/- the day of calculation
	 * @param hour		+/- the hour of calculation
	 * @param minute	+/- the minute of calculation
	 * @param second	+/- the second of calculation
	 * @return			Date (string type)
	 */
	public static String calcDate(Date date, int day, int hour, int minute, int second) {
		Calendar cal = Calendar.getInstance();
		
        cal.setTime(date);
        
        if(day != 0) {
        	cal.add(Calendar.DATE, day);
        }
        if(hour != 0) {
        	cal.add(Calendar.HOUR, hour);
        }
        if(minute != 0) {
        	cal.add(Calendar.MINUTE, minute);
        }
        if(second != 0) {
        	cal.add(Calendar.SECOND, second);
        }
        
        return retrieveDateFormat.format(cal.getTime());
	}

}
