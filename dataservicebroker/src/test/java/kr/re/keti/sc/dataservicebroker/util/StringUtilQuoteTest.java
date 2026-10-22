package kr.re.keti.sc.dataservicebroker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class StringUtilQuoteTest {

    @Test
    void quoteIdentifierWrapsDigitLeadingName() {
        assertEquals("\"5daysrainfallmax\"", StringUtil.quoteIdentifier("5daysrainfallmax"));
    }

    @Test
    void quoteIdentifierEscapesInnerQuote() {
        assertEquals("\"a\"\"b\"", StringUtil.quoteIdentifier("a\"b"));
    }

    @Test
    void quoteIdentifierDoesNotDoubleQuote() {
        assertEquals("\"temperature\"", StringUtil.quoteIdentifier("\"temperature\""));
        assertEquals("\"temperature\"", StringUtil.quoteColumnName("\"temperature\""));
    }

    @Test
    void quoteIdentifierKeepsCase() {
        assertEquals("\"Temp\"", StringUtil.quoteIdentifier("Temp"));
    }

    @Test
    void quoteColumnNameLowercasesThenQuotes() {
        // 따옴표 없는 기존 컬럼(PostgreSQL 이 소문자로 접음)과 같은 컬럼을 가리켜야 한다
        assertEquals("\"5daysrainfallmax\"", StringUtil.quoteColumnName("5daysRainfallMax"));
        assertEquals("\"location_4326\"", StringUtil.quoteColumnName("Location_4326"));
        assertEquals("\"abcd_temperature_observedat\"", StringUtil.quoteColumnName("ABCD_temperature_observedAt"));
    }

    @Test
    void nullStaysNull() {
        assertNull(StringUtil.quoteIdentifier(null));
        assertNull(StringUtil.quoteColumnName(null));
    }
}
