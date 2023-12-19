package kr.re.keti.sc.dataservicebroker.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidateUtilTest {

    @Test
    void testValidateUrn() {
        String validUrn = "urn:test:1234:1234";
        String invalidUrn = "urn:test";
        String invalidUrn2 = "u";
        String invalidUrn3 = "";

        Assertions.assertEquals(true, ValidateUtil.isValidUrn(validUrn));
        Assertions.assertEquals(false, ValidateUtil.isValidUrn(invalidUrn));
        Assertions.assertEquals(false, ValidateUtil.isValidUrn(invalidUrn2));
        Assertions.assertEquals(false, ValidateUtil.isValidUrn(invalidUrn3));
    }
}