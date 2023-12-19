package kr.re.keti.sc.datacoreui.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kr.re.keti.sc.datacoreui.common.code.Constants;
import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode;
import kr.re.keti.sc.datacoreui.common.exception.BadRequestException;


/**
 * Utility for convert time parameter
 * @FileName ConvertTimeParamUtil.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
public class ConvertTimeParamUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConvertTimeParamUtil.class);

    /**
     *
     * ISO 8601 datetime pattern and offsetDateTime pattern -> localDateTime
     * @param strDateTime
     * @return
     */
    public static String dateTimeToLocalDateTime(String strDateTime) {

        if (strDateTime == null) {
            return null;
        }

        // https://stackoverflow.com/questions/53361712/desserialization-of-plus-in-spring-request-param
        // + is a reserved word + => replaced with a space, temporarily replaced with +
        strDateTime = strDateTime.replace(" ", "+");

        //offsetDateTime (Ex: 2010-01-01T12:00:00+01:00)
        Pattern offsetDateTimePattern = Pattern.compile("^(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(?:\\.\\d{1,9})?(?:Z|[+-][01]\\d:[0-5]\\d)$");

        //ISO 8601 pattern (* 2014-12-12T00:00:00Z)
        Pattern iso8601Pattern = Pattern.compile("^(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\\\.[0-9]+)?(Z)?$");

        Instant instant;

        try {

            if (iso8601Pattern.matcher(strDateTime).find()) {

                Date date = Date.from(Instant.parse(strDateTime));
                instant = date.toInstant();
//                instant = date.toInstant().atZone(ZoneOffset.UTC).toInstant();


            } else if (offsetDateTimePattern.matcher(strDateTime).find()) {

                OffsetDateTime odt = OffsetDateTime.parse(strDateTime);
                instant = odt.toInstant();
            } else {
                throw new BadRequestException(DataCoreUiCode.ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "invalid dateTime format");
            }

        } catch (Exception e) {

            throw new BadRequestException(DataCoreUiCode.ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "invalid dateTime format");

        }
        Date convertedDate = Date.from(instant);

        SimpleDateFormat transFormat = new SimpleDateFormat(Constants.POSTGRES_DATE_FORMAT);
        String strConvertedDateTime = transFormat.format(convertedDate);

        return strConvertedDateTime;
    }


    /**
     * timerel params validation
     * @param timerel
     * @param time
     * @param endTime
     */
    public static void checkTimeRelParams(String timerel, String time, String endTime) {

        if (time == null) {

            throw new BadRequestException(DataCoreUiCode.ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "invalid timerel params");
        }

        if (timerel.equalsIgnoreCase(DataCoreUiCode.TemporalOperator.BETWEEN_REL.getCode()) && endTime == null) {
            throw new BadRequestException(DataCoreUiCode.ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "invalid timerel params");
        }
    }

}