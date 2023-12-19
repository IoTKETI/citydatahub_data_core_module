package kr.re.keti.sc.datacoreui.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import kr.re.keti.sc.datacoreui.common.code.Constants;

/**
 * Utility for date type
 * @FileName DateUtil.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
public class DateUtil {

	private static final Object lockObject = new Object();
	private static final DateFormat dateFormat = new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT);
	private static final DateFormat postgresFormat = new SimpleDateFormat(Constants.POSTGRES_TIMESTAMP_FORMAT);
	private static final DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern(Constants.POSTGRES_TIMESTAMP_FORMAT);


	/**
	 * Converts a date to a string in DB format.
	 * @param date	Date
	 * @return		DB format date value of string type
	 */
	public static String dateToDbFormatString(Date date) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of(Constants.CONTENT_DATE_TIMEZONE));
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

}
