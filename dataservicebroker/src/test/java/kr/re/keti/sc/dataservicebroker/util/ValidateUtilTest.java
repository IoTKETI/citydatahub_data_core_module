package kr.re.keti.sc.dataservicebroker.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    @Test
    void hasNoAttributeForEmptyOrContextOnlyFragment() {
        // TTA EA/012_02/06: PATCH /entities/{id}/attrs/{attrId} 에 {} 를 보내면 400 이어야 한다
        assertTrue(ValidateUtil.hasNoAttribute(null));
        assertTrue(ValidateUtil.hasNoAttribute(new HashMap<>()));

        Map<String, Object> contextOnly = new HashMap<>();
        contextOnly.put("@context", Collections.singletonList("https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context-v1.3.jsonld"));
        assertTrue(ValidateUtil.hasNoAttribute(contextOnly));
    }

    @Test
    void hasNoAttributeIsFalseWhenAnyAttributeExists() {
        Map<String, Object> fragment = new HashMap<>();
        fragment.put("@context", Collections.singletonList("https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context-v1.3.jsonld"));
        fragment.put("speed", Collections.singletonMap("value", 40));
        assertFalse(ValidateUtil.hasNoAttribute(fragment));
    }
}