package kr.re.keti.sc.dataservicebroker.query.query;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.TestEnvironment;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityVO;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.entities.service.EntityRetrieveSVC;
import kr.re.keti.sc.dataservicebroker.entities.vo.EntityRetrieveVO;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AttributeQueryOperatorTest {

    // 테스트에 필요한 상수와 의존성 주입된 객체들을 선언
    private static final String TEST_DATAMODEL_ID = "TestModel3";
    private static final String TEST_DATASET_ID = "TestModel3Flow";
    private static final String TEST_DATA = "src/test/resources/inputData_queryTest.json";

    @Autowired
    private TestEnvironment testEnvironment;

    @Autowired
    private EntityRetrieveSVC entityRetrieveSVC;

    @Autowired
    private MockMvc mvc;

    private List<Map<String, Object>> testData;

    // 모든 테스트 시작 전에 한 번 실행
    // 테스트 데이터를 설정, 테스트 환경을 초기화
    @BeforeAll
    public void setup() throws Exception {
        testEnvironment.setup(TEST_DATAMODEL_ID, TEST_DATASET_ID);
        setupTestData();
        testData = loadTestDataFromJson();
    }

    // 모든 테스트 종료 후에 한 번 실행
    // 사용한 테스트 데이터를 정리하고, 테스트 결과를 출력
    @AfterAll
    public void cleanup() {
        cleanupTestData();
        testEnvironment.cleanup(TEST_DATAMODEL_ID, TEST_DATASET_ID);

        // Print Test Results
        System.out.println("\n=================TestResults=================");
        testResults.forEach(System.out::println);
        System.out.println("=============================================\n");
    }

    // 테스트 결과를 저장할 리스트
    private List<TestResult> testResults = new ArrayList<>();

    // 테스트 데이터를 설정
    // 테스트 데이터 파일로부터 요청 본문을 읽어들이고, MockMvc를 사용하여 엔티티 생성 요청을 수행
    public void setupTestData() throws Exception {

        // Test data setup logic
        String requestBody = "";

        try {
            requestBody = new String(Files.readAllBytes(Paths.get(TEST_DATA)));
            if (requestBody.isEmpty()) {
                fail("Request body is empty");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to read the input data file");
            return;
        }

        mvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/entityOperations/create")
                                .content(requestBody)
                                .contentType("application/ld+json")
                                .accept(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .header("Content-Length", String.valueOf(requestBody.length())))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    // 테스트 데이터를 정리
    // MockMvc를 사용하여 엔티티 삭제 요청을 수행
    public void cleanupTestData() {

        // Test data cleanup logic
        ResultActions resultActions;
        try {
            resultActions = mvc
                    .perform(
                            MockMvcRequestBuilders
                                    .delete("/entities/urn:datahub:TestModel3:queryUnitTest:queryUnitTestSetId")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .characterEncoding("utf-8"))
                    .andDo(print());

            MvcResult mvcResult = resultActions.andReturn();

            System.out.println("\n=====================Delete==================");
            if ((Integer) mvcResult.getResponse().getStatus() != 204) { // 204 No Content
                System.out.println("=====================Error===================\n");
                System.out.println(mvcResult.getResponse().getStatus());
            } else {
                System.out.println(mvcResult.getResponse().getContentAsString());
                System.out.println("=====================Done====================\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // JOSN 파일에서 테스트에 사용할 자료형과 데이터를 로드
    private List<Map<String, Object>> loadTestDataFromJson() {
        File jsonFile = new File("src/test/resources/testDataForQueryTest.json");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonFile, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // 테스트에 사용할 연산자들을 반환
    private List<String> getTestOperators() {
        return List.of(">", "~=", "!~=");
        // return List.of(">");
    }

    // 유효한 쿼리 목록을 반환
    public static List<String> validQueryList() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // JSON 파일에서 데이터를 읽어들임
            File file = new File("src/test/resources/validQueryList.json");
            
            // 파일 내용을 JsonNode 객체로 변환
            JsonNode rootNode = objectMapper.readTree(file);

            // "validQueries" 배열을 가져옴
            JsonNode validQueriesNode = rootNode.path("validQueries");
            List<String> validQueries = new ArrayList<>();

            if (validQueriesNode.isArray()) {
                for (JsonNode queryNode : validQueriesNode) {
                    validQueries.add(queryNode.asText());
                }
            }

            return validQueries;
        } catch (Exception e) {
            e.printStackTrace();
            // 오류 발생 시 빈 리스트 반환
            return new ArrayList<>();
        }
    }

    // 단순 연산자 테스트를 위한 동적 테스트 생성
    @TestFactory
    public Stream<DynamicTest> dynamicTestsForNGSILDQueries() {
        List<String> validQueries = validQueryList(); // 유효한 쿼리 목록

        return testData.stream().flatMap(testDataMap -> {
            String attributeName = (String) testDataMap.get("attributeName");
            List<String> operators = getTestOperators(); // 연산자 목록

            // 값이 배열인 경우 처리
            Object valueObj = testDataMap.get("value");
            List<Object> values = new ArrayList<>();

            if (valueObj instanceof List) {
                values.addAll((List<?>) valueObj);
            } else {
                values.add(valueObj);
            }

            return values.stream().flatMap(value -> operators.stream().map(operator -> {
                String query = String.format("%s%s%s", attributeName, operator, value);
                String displayNamePrefix = validQueries.contains(query) ? "존재하는" : "존재하지 않는";
                String displayName = String.format("%s %s에 대한 %s 연산자 테스트 (%s) (%s)", displayNamePrefix,
                        // getAttributeTypeDescription(attributeName), getOperatorDescription(operator), query, query);
                        attributeName, getOperatorDescription(operator), query, query);

                return DynamicTest.dynamicTest(displayName, () -> {
                    testQuery("TestModel3", query, value, operator, attributeName, displayName);
                });
            }));
        });
    }

    // attributeName에 따라 디스플레이 네임을 설정
    private String getAttributeTypeDescription(String attributeName) {
        if (attributeName == null) {
            return "Unknown";
        }

        // 'Array' 타입 처리
        if (attributeName.startsWith("testArray") && attributeName.contains("[")) {
            return "Array";
        }

        int indexBracket = attributeName.indexOf('[');
        int indexBrace = attributeName.indexOf('{');

        if (indexBracket != -1 && (indexBrace == -1 || indexBracket < indexBrace)) {
            // '['가 먼저 나오거나, '{'가 없는 경우
            return "Array";
        } else if (indexBrace != -1) {
            // '{'가 '['보다 먼저 나오거나, '['가 없는 경우
            return "Object";
        } else {
            switch (attributeName) {
                case "testInteger":
                    return "Number(정수형)";
                case "testDouble":
                    return "Number(실수형)";
                case "testString":
                    return "String";
                case "testChar":
                    return "Char";
                case "testBoolean":
                    return "Boolean";
                case "testDate":
                    return "Date";
                case "testTime":
                    return "Time";
                case "testDateTime":
                    return "DateTime";
                case "testURI":
                    return "URI";
                default:
                    return "Unknown";
            }
        }
    }

    // operator에 따라 디스플레이 네임을 설정
    private String getOperatorDescription(String operator) {
        switch (operator) {
            case "==":
                return "equal";
            case "!=":
                return "unequal";
            case ">=":
                return "qreaterEq";
            case ">":
                return "greater";
            case "<=":
                return "lessEq";
            case "<":
                return "less";
            case "..":
                return "range(between)";
            case "~=":
                return "Pattern";
            case "!~=":
                return "Not Pattern";
            default:
                return "Unknown";
        }
    }

    /**
     * 쿼리를 실행하고 결과를 검증
     * 중첩된 구조 또는 단일 레벨의 쿼리를 처리하며, 쿼리의 실행 결과를 기반으로 테스트 결과를 결정
     *
     * @param type          엔티티 타입
     * @param query         실행할 쿼리 문자열
     * @param expectedValue 검증할 기대값
     * @param operator      사용할 연산자
     * @param attributeName 검증할 속성(자료형)
     * @param displayName   테스트 케이스의 이름
     */
    private void testQuery(String type, String query, Object expectedValue, String operator, String attributeName,
            String displayName) {
        // System.out.println("[Debug] Generated Query: " + query);
        // System.out.println("[Debug] Generated Query Type: " +
        // query.getClass().getName());
        try {
            // Setup and execute the query
            QueryVO queryVO = new QueryVO();
            queryVO.setType(type);
            queryVO.setQ(query);
            // queryVO.setQ("testArrayObject[testArrObjInteger]==100");
            queryVO.setLinks(Arrays.asList(
                    "http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld",
                    "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld"));

            EntityRetrieveVO retrieveVO = entityRetrieveSVC.getEntity(queryVO, query, "application/json", null);
            boolean result = false;
            
            System.out.println("[Debug] " + query);
            System.out.println("[Debug] " + retrieveVO.getTotalCount());
            
            if (retrieveVO.getTotalCount() != 0) {
                // 중첩된 구조의 쿼리인 경우, 해당 구조를 재귀적으로 탐색하고 평가
                if (attributeName.contains("[")) {
                    for (CommonEntityVO entity : retrieveVO.getEntities()) {
                        result = evaluateNestedAttribute(entity, attributeName, operator, expectedValue);
                        if (result) {
                            break;
                        }
                    }
                } else {
                    // 단일 레벨의 쿼리인 경우, 엔티티 내의 해당 속성을 직접 비교
                    for (CommonEntityVO entity : retrieveVO.getEntities()) {
                        if (entity.containsKey(attributeName)) {
                            Object actualValue = ((Map<?, ?>) entity.get(attributeName)).get("value");
                            if (compareValuesBasedOnOperator(actualValue, operator, expectedValue)) {
                                result = true;
                                break;
                            }
                        }
                    }
                } 
            }

            handleTestResult(result, query, displayName);
        } catch (Exception e) {
            handleTestException(e, query, displayName);
        }
    }

    /**
     * 중첩된 속성 구조를 가진 엔티티에 대한 쿼리를 검증
     * attributeName의 형태에 따라 적절한 처리를 수행하며, 속성 값과 기대값을 비교
     *
     * @param entity        검증할 엔티티 객체
     * @param attributeName 검증할 속성의 이름
     * @param operator      사용할 연산자
     * @param expectedValue 비교할 기대값
     * @return 속성 값이 기대값과 일치하면 true, 그렇지 않으면 false를 반환
     */
    private boolean evaluateNestedAttribute(Object entity, String attributeName, String operator,
            Object expectedValue) {

        // attributeName이 중첩된 속성을 포함하는 경우, 첫 번째 속성을 추출하고 나머지에 대해 재귀적으로 검증을 진행
        if (!attributeName.contains("[")) {
            // entity가 Map 타입인지 확인하고 처리
            if (entity instanceof Map) {

                Object actualValue = ((Map<?, ?>) entity).get("value");

                // actualValue가 List 타입인 경우, 리스트 내부를 순회하며 속성 값을 비교
                if (actualValue instanceof List) {
                    System.out.println("[Debug] QWER5: " + actualValue);
                    boolean found = false;
                    boolean anyMatch = false;
                    for (Object item : (List<?>) actualValue) {
                        if (item instanceof Map) {
                            Map<?, ?> itemMap = (Map<?, ?>) item;
                            if (itemMap.containsKey(attributeName)) {
                                found = true;
                                actualValue = itemMap.get(attributeName);
                                if (compareValuesBasedOnOperator(actualValue, operator, expectedValue)) {
                                    anyMatch = true;
                                }
                            }
                        }
                    }
                    if (!found)
                        return false; // List 내에 attributeName에 해당하는 항목이 없는 경우
                    return anyMatch; // List 내 하나 이상의 항목이 기대값과 일치하는 경우 true 반환
                } else {
                    // actualValue가 List 타입이 아닌 경우, 단일 값을 비교
                    return compareValuesBasedOnOperator(actualValue, operator, expectedValue);
                }
            }
            return false;
        }

        // attributeName에 중첩 구조를 나타내는 '[' 문자가 있는 경우의 처리
        String[] parts = attributeName.split("\\[", 2);
        String currentAttribute = parts[0];
        String remainingAttributes = parts[1].substring(0, parts[1].length() - 1); // 닫는 대괄호 제거

        /**
         * 다음 단계로 넘어가는 로직은 attributeName의 중첩된 부분을 처리
         * 예를 들어, attributeName이 "parent[child]" 형식일 경우,
         * "parent"를 키로 사용하여 해당 값(중첩된 객체 또는 리스트)을 추출하고,
         * "child" 속성에 대해 재귀적으로 검증을 수행
         */
        if (entity instanceof Map) {
            Object nextLevel = ((Map<?, ?>) entity).get(currentAttribute);
            if (nextLevel != null) {
                return evaluateNestedAttribute(nextLevel, remainingAttributes, operator, expectedValue);
            }
        } else if (entity instanceof List) {
            for (Object item : (List<?>) entity) {
                if (item instanceof Map && ((Map<?, ?>) item).containsKey(currentAttribute)) {
                    Object nextLevel = ((Map<?, ?>) item).get(currentAttribute);
                    return evaluateNestedAttribute(nextLevel, remainingAttributes, operator, expectedValue);
                }
            }
        }

        // 일치하는 속성이 없는 경우 false 반환
        return false;
    }

    /**
     * 주어진 연산자를 사용하여 실제값(actualValue)과 기대값(expectedValue)을 비교
     * 실제값이 배열인 경우 배열의 각 요소를, 배열이 아닌 경우 단일 값을 비교
     *
     * @param actualValue   검증할 실제값
     * @param operator      사용할 비교 연산자. 예: "==", "!=", ">=", ">", "<=", "<"
     * @param expectedValue 비교 대상이 되는 기대값
     * @return 비교 결과에 따라 true 또는 false를 반환
     */
    private boolean compareValuesBasedOnOperator(Object actualValue, String operator, Object expectedValue) {
        // actualValue가 배열(Object[])인 경우, 배열의 각 요소를 순회하며 비교
        if (actualValue instanceof Object[]) {
            for (Object val : (Object[]) actualValue) {
                // 배열 내의 단일 값과 기대값을 비교하는 메서드를 호출
                if (compareSingleValueBasedOnOperator(val, operator, expectedValue)) {
                    return true; // 배열 내에서 조건에 부합하는 값이 있으면 true 반환
                }
            }
            return false; // 배열 내 어떤 값도 조건에 부합하지 않으면 false 반환
        } else {
            // actualValue가 배열이 아닌 경우, 단일 값과 기대값을 직접 비교
            return compareSingleValueBasedOnOperator(actualValue, operator, expectedValue);
        }
    }

    /**
     * 단일 값에 대해 주어진 연산자를 사용하여 기대값과 비교
     *
     * @param actualValue   비교할 실제값
     * @param operator      사용할 비교 연산자
     * @param expectedValue 비교 대상이 되는 기대값
     * @return 비교 결과에 따라 true 또는 false를 반환
     */
    private boolean compareSingleValueBasedOnOperator(Object actualValue, String operator, Object expectedValue) {
        String actualValueStr = actualValue.toString();
        String expectedValueStr = expectedValue.toString();

        // expectedValue가 범위 검색을 나타내는 경우 ".." 사용
        if (expectedValueStr.contains("..")) {
            try {
                String[] range = expectedValueStr.split("\\.\\."); // . 기준으로 앞과 뒤를 나눔 예) 10..100 => 10과 100으로 나뉨
                double min = Double.parseDouble(range[0]);
                double max = Double.parseDouble(range[1]);
                double actual = Double.parseDouble(actualValueStr);

                switch (operator) {
                    case "==":
                        return actual >= min && actual <= max;
                    case "!=":
                        return actual < min || actual > max;
                    default:
                        return false; // 범위 검색은 "=="와 "!=" 연산자만 지원
                }
            } catch (NumberFormatException e) {
                return false; // 형식이 맞지 않는 경우
            }
        } else {
            // 일반 비교
            switch (operator) {
                case "==":
                    return actualValueStr.equals(stripQuotes(expectedValueStr));
                case "!=":
                    return !actualValueStr.equals(stripQuotes(expectedValueStr));
                case ">=":
                    return compareValues(actualValue, expectedValue) >= 0;
                case ">":
                    return compareValues(actualValue, expectedValue) > 0;
                case "<=":
                    return compareValues(actualValue, expectedValue) <= 0;
                case "<":
                    return compareValues(actualValue, expectedValue) < 0;
                case "~=":
                    return matchRegex(actualValueStr, stripQuotes(expectedValueStr));
                case "!~=":
                    return !matchRegex(actualValueStr, stripQuotes(expectedValueStr));
                default:
                    return false;
            }
        }
    }

    // 문자열에서 따옴표 제거
    private String stripQuotes(String str) {
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    // 두 값을 비교하여 결과 반환
    private int compareValues(Object val1, Object val2) {
        if (val1 instanceof Number && val2 instanceof Number) {
            return Double.compare(((Number) val1).doubleValue(), ((Number) val2).doubleValue());
        }
        return 0;
    }

    // 정규식 매칭 함수
    private boolean matchRegex(String value, String pattern) {
        return Pattern.matches(pattern, value);
    }

    // 테스트 오류 예외 처리
    private void handleTestException(Exception e, String query, String displayName) {
        // 예외 정보를 문자열로 변환
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));

        // Exception 발생시 유효한 쿼리 목록 확인
        boolean isValidQuery = validQueryList().contains(query);
        if (isValidQuery) {
            // 유효한 쿼리에서 예외 발생시 실패
            testResults.add(new TestResult("[ handleTestException 오류 발생]" + displayName, false, e.getMessage()));
            fail(e.getMessage());
        } else {
            // 유효하지 않은 쿼리에서 예외 발생시 성공
            testResults.add(new TestResult(displayName, true,
                    "Test passed: Invalid query correctly resulted in NumberFormatException"));
        }
    }

    // 테스트 결과 처리
    private void handleTestResult(boolean result, String query, String displayName) {
        // 유효한 쿼리 목록에서 현재 쿼리의 유효성 확인
        boolean isValidQuery = validQueryList().contains(query);

        System.out.println("[Debug] " + isValidQuery);

        if (result) {
            // 조회된 결과가 있을 때
            if (isValidQuery) {
                // 유효한 쿼리 목록에 존재하고 결과가 있는 경우: 테스트 성공
                testResults.add(new TestResult(displayName, true, "Test passed: Valid query successfully executed"));
            } else {
                // 유효한 쿼리 목록에 존재하지 않지만 결과가 있는 경우: 테스트 실패
                testResults.add(new TestResult("[ handleTestResult 오류 발생] " + displayName, false,
                        "Test failed: Invalid query should not have returned results"));
                // 실패로 간주하여 JUnit 테스트 중단
                fail("Test failed: Invalid query should not have returned results");
            }
        } else {
            // 조회된 결과가 없을 때
            if (isValidQuery) {
                // 유효한 쿼리 목록에 존재하지만 결과가 없는 경우: 테스트 실패
                testResults.add(
                        new TestResult("[ handleTestResult 오류 발생] " + displayName, false,
                                "Test failed: Valid query should have returned results"));
                // 실패로 간주하여 JUnit 테스트 중단
                fail("Test failed: Valid query should have returned results");
            } else {
                // 유효한 쿼리 목록에 존재하지 않고 결과도 없는 경우: 테스트 성공
                testResults.add(new TestResult(displayName, true,
                        "Test passed: Invalid query correctly resulted in no results"));
            }
        }
    }

    // 테스트 결과를 디버그 콘솔에 출력
    private static class TestResult {
        String testName;
        boolean success;
        String message;

        TestResult(String testName, boolean success, String message) {
            this.testName = testName;
            this.success = success;
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("Test: %s, Result: %s, Message: %s", testName, success ? "Success" : "Fail", message);
        }
    }

    // ========== 이하 논리 연산자 테스트==========

    // 허용된 쿼리 목록
    List<String> allowedQueries = List.of(
            // 성공
            "testInteger==100;testDouble==10.5",
            "testInteger==100;testDouble>=10.5",
            "testInteger==100;testString==\"example\"",
            "testInteger!=101;testDouble!=10.0",
            "testInteger>=100;testString~=\"^ex.*\"",
            "testString==\"example\";testBoolean==true",
            "testString==\"example\";testBoolean!=false",
            "testDouble<=10.5;testInteger<=100",
            "testString!~=\"^EX.*\";testDouble>10.0",
            "testInteger!=100|testDouble==10.5");

    List<String> disallowedQueries = List.of(
            // 실패
            "testString==\"example\";testInteger<=99",
            "testBoolean==true;testDouble!=10.5",
            "testBoolean!=false;testString~=\"^sam.*\"",
            "testDouble==10.0;testInteger!=100",
            "testString!~=\"^exa.*\";testBoolean==false",
            "testInteger<99;testDouble<=10.0",
            "testInteger==101|testString==\"nonexistent\"",
            "testBoolean!=true|testDouble>10.6",
            "testInteger>=100;testDouble<10.5");

    // 논리 연산자 테스트를 위한 동적 테스트 생성
    @TestFactory
    public Stream<DynamicTest> dynamicTestsForLogicalOperatorQueries() {
        // 허용된 쿼리와 비허용된 쿼리를 병합
        List<String> queryList = Stream.concat(allowedQueries.stream(), disallowedQueries.stream())
                .collect(Collectors.toList());

        return queryList.stream().map(query -> {
            // 쿼리가 허용된 목록에 있는지 여부 확인
            boolean isValidQuery = allowedQueries.contains(query);

            // 쿼리가 유효한지 여부에 따라 디스플레이 네임 접두어 설정
            String displayNamePrefix = isValidQuery ? "존재하는" : "존재하지 않는";
            String displayName = String.format("%s 쿼리 테스트: %s", displayNamePrefix, query);

            return DynamicTest.dynamicTest(displayName, () -> {
                try {
                    QueryVO queryVO = new QueryVO();
                    queryVO.setType("TestModel3");
                    queryVO.setQ(query);
                    queryVO.setLinks(Arrays.asList(
                            "http://uri.citydatahub.kr/ngsi-ld/testmodel2.jsonld",
                            "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld"));

                    // 쿼리 실행
                    EntityRetrieveVO entityRetrieveVO = entityRetrieveSVC.getEntity(queryVO, query, "application/json",
                            null);
                    List<CommonEntityVO> entities = entityRetrieveVO.getEntities();

                    // 결과가 존재하는지 확인
                    boolean result = !entities.isEmpty();

                    // entityRetrieveVO.getEntities().forEach(entity -> {
                    // CommonEntityVO commonEntity = (CommonEntityVO) entity;
                    // commonEntity.forEach((key, value) -> {
                    // System.out.println("[Query] " + query + " [Key] " + key + ": " + " [Value] "
                    // + value);
                    // });
                    // });

                    // 결과 처리
                    handleTestResult_Logical(result, query);
                } catch (Exception e) {
                    // 예외 발생시 예외 처리 메서드 호출
                    handleTestException_Logical(e, query);
                }
            });
        });
    }

    private void handleTestResult_Logical(boolean result, String query) {
        // 쿼리의 유효성 확인
        boolean isValidQuery = allowedQueries.contains(query);

        if (result) {
            // 결과가 있을 때
            if (isValidQuery) {
                // 유효한 쿼리일 경우: 테스트 성공
                testResults.add(new TestResult(query, true, "Test passed: Valid query successfully executed"));
            } else {
                // 유효하지 않은 쿼리일 경우: 테스트 실패
                testResults.add(
                        new TestResult(query, false, "Test failed: Invalid query should not have returned results"));
                fail("Test failed: Invalid query should not have returned results");
            }
        } else {
            // 결과가 없을 때
            if (isValidQuery) {
                // 유효한 쿼리일 경우: 테스트 실패
                testResults.add(new TestResult(query, false, "Test failed: Valid query should have returned results"));
                fail("Test failed: Valid query should have returned results");
            } else {
                // 유효하지 않은 쿼리일 경우: 테스트 성공
                testResults.add(
                        new TestResult(query, true, "Test passed: Invalid query correctly resulted in no results"));
            }
        }
    }

    private void handleTestException_Logical(Exception e, String query) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        testResults.add(new TestResult(query, false, errors.toString()));
    }

}