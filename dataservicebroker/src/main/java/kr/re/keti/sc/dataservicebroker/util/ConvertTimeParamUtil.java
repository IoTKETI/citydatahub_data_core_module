package kr.re.keti.sc.dataservicebroker.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.regex.Pattern;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;

public class ConvertTimeParamUtil {

    /**
     *
     * ISO 8601 datetime 패턴과 offsetDateTime 패턴 -> localDateTime으로 변경
     * @param strDateTime
     * @return
     */
    public static String dateTimeToLocalDateTime(String strDateTime) {

        if (strDateTime == null) {
            return null;
        }

        // https://stackoverflow.com/questions/53361712/desserialization-of-plus-in-spring-request-param
        // + 는 예약어로 + => 공백으로 대치, 임시로 + 로 대체함
        strDateTime = strDateTime.replace(" ", "+");

        //offsetDateTime (Ex: 2010-01-01T12:00:00+01:00)
        Pattern offsetDateTimePattern = Pattern.compile("^(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(?:\\.\\d{1,9})?(?:Z|[+-][01]\\d:[0-5]\\d)$");

        //ISO 8601 패턴 (* 2014-12-12T00:00:00Z)
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
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "invalid dateTime format");
            }

        } catch (Exception e) {

            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "invalid dateTime format");

        }
        Date convertedDate = Date.from(instant);

        SimpleDateFormat transFormat = new SimpleDateFormat(Constants.POSTGRES_DATE_FORMAT);
        String strConvertedDateTime = transFormat.format(convertedDate);

        return strConvertedDateTime;
    }


    /**
     * timerel params 유효성 검사
     * @param timerel
     * @param timeAt
     * @param endTimeAt
     */
    public static void checkTimeRelParams(String timerel, String timeAt, String endTimeAt) {

        if (timeAt == null) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "Invalid timerel params. Not found 'timeAt'");
        }

        if (timerel.equalsIgnoreCase(DataServiceBrokerCode.TemporalOperator.BETWEEN_REL.getCode()) && endTimeAt == null) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "Invalid timerel params. Not found 'endTimeAt'");
        }
    }

}