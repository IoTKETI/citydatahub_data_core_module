package kr.re.keti.sc.datamanager.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import kr.re.keti.sc.datamanager.common.code.Constants;

/**
 * Date Parsing util class
 */
public class DateUtil {

	private static final DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern(Constants.POSTGRES_TIMESTAMP_FORMAT);

	public static String dateToDbFormatString(Date date) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of(Constants.CONTENT_DATE_TIMEZONE));
		return localDateTime.format(dbDateFormatter);
	}

}
