package kr.re.keti.sc.dataservicebroker.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DbColumnType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DefaultAttributeKey;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.DefaultDbColumnName;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.QueryOperator;
import kr.re.keti.sc.dataservicebroker.common.exception.BaseException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.ObjectMember;
import lombok.extern.slf4j.Slf4j;

import static kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.*;

@Slf4j
public class QueryUtil {

    /**
     * 상세 Query를 위한 조건 생성
     *
     * @param dataModelCacheVO
     * @param queryVO
     * @return
     */
    public static QueryVO generateQuery(DataModelCacheVO dataModelCacheVO, QueryVO queryVO) {

        List<String> queryResultList = new ArrayList<>();

        try {
            String q_query = queryVO.getQ();

            //불필요한 부분 정제
            q_query = q_query.replace("\"", "'").replace("\\", "");

            //상세쿼리 조건 단위 인(;)로 쪼개서 처리함
            String[] splitedAndQuery = q_query.split(";");

            for (String andQueryItem : splitedAndQuery) {

                String[] splitedOrQuery = andQueryItem.split("\\|");

                List<String> orQueryList = new ArrayList<>();
                for (String orQuery : splitedOrQuery) {
                	String innerQuery = makeFragmentQuery(orQuery, dataModelCacheVO);
                	if(innerQuery != null) {
                		orQueryList.add("(" + innerQuery + ")");
                	}
                }

                if (!ValidateUtil.isEmptyData(orQueryList)) {
                    queryResultList.add(String.join(" OR ", orQueryList));
                }
            }
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new NgsiLdBadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "invalid q-query params", e);
        }

        if (queryResultList.size() > 0) {
            String query = String.join(" AND ", queryResultList);

            log.debug("q-query : '{}'", query);

            queryVO.setQuery(query);
        }
        return queryVO;
    }
    
    public static List<String> extractQueryFieldNames(QueryVO queryVO) {
    	
    	if(queryVO == null || queryVO.getQ() == null) {
    		return null;
    	}

        try {
            Map<String, String> queryFieldNames = new HashMap<>();

            String qQuery = queryVO.getQ();

            //불필요한 부분 정제
            qQuery = qQuery.replace("\"", "'").replace("\\", "");

            //상세쿼리 조건 단위 인(;)로 쪼개서 처리함
            String[] splitedAndQuery = qQuery.split(";");

            for (String andQueryItem : splitedAndQuery) {
                String[] splitedOrQuery = andQueryItem.split("\\|");
                for (String orQuery : splitedOrQuery) {

                    orQuery = orQuery.replace("%x", "%");
                    try {
                        orQuery = URLDecoder.decode(orQuery, Constants.CHARSET_ENCODING);
                    } catch (UnsupportedEncodingException e) {
                        log.warn("QueryUtil extractQueryFieldNames qQuery parsing error", e);
                    }

                    String qOperator = null;

                    // operator가 EQUAL(==) 경우 처리
                    if (orQuery.contains(QueryOperator.EQUAL.getSign())
                            || orQuery.contains(QueryOperator.EQUAL.getUnicode())) {

                        qOperator = QueryOperator.EQUAL.getSign();

                    } else if (orQuery.contains(QueryOperator.UNEQUAL.getSign())
                            || orQuery.contains(QueryOperator.UNEQUAL.getUnicode())) {

                        // operator가 unequal(!=) 일 경우
                        qOperator = QueryOperator.UNEQUAL.getSign();

                    } else if (orQuery.contains(QueryOperator.NOT_PATTERN_OP.getSign())
                            || orQuery.contains(QueryOperator.NOT_PATTERN_OP.getUnicode())) {

                        qOperator = QueryOperator.NOT_PATTERN_OP.getSign();

                    } else if (orQuery.contains(QueryOperator.PATTERN_OP.getSign())
                            || orQuery.contains(QueryOperator.PATTERN_OP.getUnicode())) {

                        qOperator = QueryOperator.PATTERN_OP.getSign();

                    }

                    if (orQuery.contains(QueryOperator.GREATEREQ.getSign())
                            || orQuery.contains(QueryOperator.GREATEREQ.getUnicode())) {
                        //greaterEq
                        qOperator = QueryOperator.GREATEREQ.getSign();

                    } else if (orQuery.contains(QueryOperator.LESSEQ.getSign())
                            || orQuery.contains(QueryOperator.LESSEQ.getUnicode())) {
                        //lessEq
                        qOperator = QueryOperator.LESSEQ.getSign();

                    } else if (orQuery.contains(QueryOperator.GREATER.getSign()) || orQuery.contains(QueryOperator.GREATER.getUnicode())) {
                        //greater
                        qOperator = QueryOperator.GREATER.getSign();

                    } else if (orQuery.contains(QueryOperator.LESS.getSign()) || orQuery.contains(QueryOperator.LESS.getUnicode())) {
                        //greater
                        qOperator = QueryOperator.LESS.getSign();
                    }

                    String attrName = orQuery.split(qOperator)[0];

                    if (attrName.contains("[")) {
                        attrName = attrName.substring(0, attrName.indexOf("["));
                    }

                    if (attrName.contains(".")) {
                        attrName = attrName.substring(0, attrName.indexOf("."));
                    }

                    if (attrName.contains("(")) {
                        attrName = attrName.replace("(", "");
                    }

                    if (attrName.contains(")")) {
                        attrName = attrName.replace(")", "");
                    }

                    queryFieldNames.put(attrName, attrName);
                }
            }

            if (queryFieldNames.size() > 0) {
                return new ArrayList<>(queryFieldNames.keySet());
            }
        } catch(Exception e) {
            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "Invalid q-query. q=" + queryVO.getQ(), e);
        }
        return null;
    }

    /**
     * ';' 단위로 분할된 상세 query정보를 SQL로 변경
     *
     * @param q_query
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String makeFragmentQuery(String q_query, DataModelCacheVO dataModelCacheVO) throws UnsupportedEncodingException, ParseException {

        q_query = q_query.replace("%x", "%");
        q_query = URLDecoder.decode(q_query, Constants.CHARSET_ENCODING);

        // full url -> short name
        if (q_query.startsWith("http")) {
            for (Attribute attribute : dataModelCacheVO.getDataModelVO().getAttributes()) {
                String attributeUri = attribute.getAttributeUri();
                if (q_query.startsWith(attributeUri)) {
                    q_query = q_query.replace(attributeUri, attribute.getName());
                }
            }
        } 
        
        // full uri를 short 으로 변환하지 못함 (모델에 포함된 attribute 가 아님)
        if (q_query.startsWith("http")) {
            throw new NgsiLdBadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "invalid q-query params. attrName=" + q_query);
        }

        String qOperator = null;
        String resultFragmentQuery = null;

        // operator가 EQUAL(==) 경우 처리
        if (q_query.contains(QueryOperator.EQUAL.getSign())
                || q_query.contains(QueryOperator.EQUAL.getUnicode())) {

            qOperator = QueryOperator.EQUAL.getSign();
            String attrName = q_query.split(qOperator)[0];
            String qValue = q_query.split(qOperator)[1];

//            //CompEqualityValue
//            qOperator = DataServiceBrokerCode.QueryOperator.EQUAL.getSign();

            if (qValue.contains(QueryOperator.DOTS.getSign())) {
                //between 형 처리

                String leftValue = qValue.split("\\.\\.")[0];
                String rightValue = qValue.split("\\.\\.")[1];

                //value가 timestamp형 일 경우, ' 을 붙여줌
                if (isTimestampValue(leftValue)) {
                    leftValue = DateUtil.convertQueryTsToDBTs(leftValue);
                }

                if (isTimestampValue(rightValue)) {
                    rightValue = DateUtil.convertQueryTsToDBTs(rightValue);
                }

                // 타켓 컬럼이 array[]형일 경우,
                if (isArrayTypeColumn(attrName, dataModelCacheVO)) {
                    resultFragmentQuery = makeBetweenQueryWithColumnArrType(getColumnNameWithType(attrName, dataModelCacheVO), leftValue, rightValue);
                } else {
                    resultFragmentQuery = makeBetweenQuery(getColumnName(attrName, dataModelCacheVO), leftValue, rightValue);
                }

            } else if (!isTimestampValue(qValue) && (qValue.contains(QueryOperator.COMMA.getSign())
                    || qValue.contains(QueryOperator.COMMA.getUnicode()))) {

                //ValueList (,) 형 처리, time 요청 값에 (,)가 있을 경우 SKIP (2019-06-08T15:00:00,000+09:00)
                qOperator = QueryOperator.COMMA.getSign();

                List<String> splitedValueList = Arrays.asList(qValue.split(qOperator));

                String resultQuery;
                if (isArrayTypeColumn(attrName, dataModelCacheVO)) {
                    List<String> arrayTypeItems = new ArrayList<>();
                    for (String itemValue : splitedValueList) {
                        String itemQuery =  makeQueryWithColumnArrType(getColumnNameWithType(attrName, dataModelCacheVO), itemValue, QueryOperator.SINGLE_EQUAL.getSign());
                        arrayTypeItems.add(itemQuery);
                    }
                    resultQuery = String.join(" OR ", arrayTypeItems);

                } else {
                    resultQuery = getColumnName(attrName, dataModelCacheVO) + " IN (" + String.join(",", splitedValueList) + ")";
                }
                resultFragmentQuery = resultQuery;

            } else {

                // 보통 EQUAL형 (==) 처리
                qOperator = QueryOperator.SINGLE_EQUAL.getSign();

                //value가 timestamp형 일 경우, ' 을 붙여줌
                if (isTimestampValue(qValue)) {
                    qValue = DateUtil.convertQueryTsToDBTs(qValue);
                }

                if (isArrayTypeColumn(attrName, dataModelCacheVO)) {
                    resultFragmentQuery = makeQueryWithColumnArrType(getColumnNameWithType(attrName, dataModelCacheVO), qValue, qOperator);
                } else {
                    resultFragmentQuery = makeQuery(getColumnName(attrName, dataModelCacheVO), qValue, qOperator);
                }
            }

            return resultFragmentQuery;

        } else if (q_query.contains(QueryOperator.UNEQUAL.getSign())
        		|| q_query.contains(QueryOperator.UNEQUAL.getUnicode())) {

            // operator가 unequal(!=) 일 경우
            qOperator = QueryOperator.UNEQUAL.getSign();
            String attrName = q_query.split(qOperator)[0];
            String qValue = q_query.split(qOperator)[1];


            //value가 timestamp형 일 경우, ' 을 붙여줌
            if (isTimestampValue(qValue)) {
                qValue = DateUtil.convertQueryTsToDBTs(qValue);
            }

            if (qValue.contains(QueryOperator.DOTS.getSign())
                    || qValue.contains(QueryOperator.DOTS.getUnicode())) {
                //between  형 처리
                String leftValue = qValue.split("\\.\\.")[0];
                String rightValue = qValue.split("\\.\\.")[1];

                if (isArrayTypeColumn(attrName, dataModelCacheVO)) {
                    resultFragmentQuery = makeNotBetweenQueryWithColumnArrType(getColumnNameWithType(attrName, dataModelCacheVO), leftValue, rightValue);
                } else {
                    resultFragmentQuery = makeNotBetweenQuery(getColumnName(attrName, dataModelCacheVO), leftValue, rightValue);
                }

            } else if (!isTimestampValue(qValue) && (qValue.contains(QueryOperator.COMMA.getSign())
                    || qValue.contains(QueryOperator.COMMA.getUnicode()))) {

                //ValueList (,) 형 처리, time 요청 값에 (,)가 있을 경우 SKIP (2019-06-08T15:00:00,000+09:00)
                qOperator = QueryOperator.COMMA.getSign();

                List<String> splitedValueList = Arrays.asList(qValue.split(qOperator));

                String resultQuery;
                if (isArrayTypeColumn(attrName, dataModelCacheVO)) {
                    List<String> arrayTypeItems = new ArrayList<>();
                    for (String itemValue : splitedValueList) {
                        String itemQuery =  makeQueryWithColumnArrType(getColumnNameWithType(attrName, dataModelCacheVO), itemValue, QueryOperator.SINGLE_EQUAL.getSign());
                        arrayTypeItems.add(itemQuery);
                    }
                    resultQuery = String.join(" OR ", arrayTypeItems);

                } else {
                    resultQuery = getColumnName(attrName, dataModelCacheVO) + " IN (" + String.join(",", splitedValueList) + ")";
                }
                resultFragmentQuery = " NOT " + resultQuery;

            } else {

                //value가 timestamp형 일 경우, ' 을 붙여줌
                if (isTimestampValue(qValue)) {
                    qValue = DateUtil.convertQueryTsToDBTs(qValue);
                }

                // 일반형 처리
                if (isArrayTypeColumn(attrName, dataModelCacheVO)) {
                    resultFragmentQuery = makeQueryWithColumnArrType(getColumnNameWithType(attrName, dataModelCacheVO), qValue, qOperator);
                } else {
                    resultFragmentQuery = makeQuery(getColumnName(attrName, dataModelCacheVO), qValue, qOperator);
                }
            }

            return resultFragmentQuery;

        } else if (q_query.contains(QueryOperator.NOT_PATTERN_OP.getSign())
                || q_query.contains(QueryOperator.NOT_PATTERN_OP.getUnicode())) {

            qOperator = QueryOperator.NOT_PATTERN_OP.getSign();
            String attrName = q_query.split(qOperator)[0];
            String qValue = q_query.split(qOperator)[1];

            String columnName = getColumnName(attrName, dataModelCacheVO);
            
            if(!isVarcharTypeColumn(columnName, dataModelCacheVO)) {
            	throw new NgsiLdBadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "invalid q-query params. RegExp can only search string.");
        	}

            String dbOperator = QueryOperator.NOT_PATTERN_OP.getDbOperator();
            
            if (isArrayTypeColumn(attrName, dataModelCacheVO)) {
                resultFragmentQuery = makeQueryWithColumnArrType(getColumnNameWithType(attrName, dataModelCacheVO), qValue, dbOperator);
            } else {
                resultFragmentQuery = makeQuery(getColumnName(attrName, dataModelCacheVO), qValue, dbOperator);
            }

            return resultFragmentQuery;

        } else if (q_query.contains(QueryOperator.PATTERN_OP.getSign())
                || q_query.contains(QueryOperator.PATTERN_OP.getUnicode())) {

            qOperator = QueryOperator.PATTERN_OP.getSign();
            String attrName = q_query.split(qOperator)[0];
            String qValue = q_query.split(qOperator)[1];

            String columnName = getColumnName(attrName, dataModelCacheVO);
            
            if(!isVarcharTypeColumn(columnName, dataModelCacheVO)) {
            	throw new NgsiLdBadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "invalid q-query params. RegExp can only search String or ArrayString type.");
        	}
            
            String dbOperator = QueryOperator.PATTERN_OP.getDbOperator();
            if (isArrayTypeColumn(attrName, dataModelCacheVO)) {
                resultFragmentQuery = makeQueryWithColumnArrType(getColumnNameWithType(attrName, dataModelCacheVO), qValue, dbOperator);
            } else {
                resultFragmentQuery = makeQuery(getColumnName(attrName, dataModelCacheVO), qValue, dbOperator);
            }

            return resultFragmentQuery;

        } 

        if (q_query.contains(QueryOperator.GREATEREQ.getSign())
                || q_query.contains(QueryOperator.GREATEREQ.getUnicode())) {
            //greaterEq
            qOperator = QueryOperator.GREATEREQ.getSign();

        } else if (q_query.contains(QueryOperator.LESSEQ.getSign())
                || q_query.contains(QueryOperator.LESSEQ.getUnicode())) {
            //lessEq
            qOperator = QueryOperator.LESSEQ.getSign();

        } else if (q_query.contains(QueryOperator.GREATER.getSign()) || q_query.contains(QueryOperator.GREATER.getUnicode())) {
            //greater
            qOperator = QueryOperator.GREATER.getSign();

        } else if (q_query.contains(QueryOperator.LESS.getSign()) || q_query.contains(QueryOperator.LESS.getUnicode())) {
            //greater
            qOperator = QueryOperator.LESS.getSign();
        }

        // 일반형 처리
        String attrName = q_query.split(qOperator)[0];
        String qValue = q_query.split(qOperator)[1];

        //value가 timestamp형 일 경우, ' 을 붙여줌
        if (isTimestampValue(qValue)) {
            qValue = DateUtil.convertQueryTsToDBTs(qValue);
        }

        if (isArrayTypeColumn(attrName, dataModelCacheVO)) {
            resultFragmentQuery = makeQueryWithColumnArrType(getColumnNameWithType(attrName, dataModelCacheVO), qValue, qOperator);
        } else {
            resultFragmentQuery = makeQuery(getColumnName(attrName, dataModelCacheVO), qValue, qOperator);
        }

        return resultFragmentQuery;
    }
    
    

    /**
     * q 쿼리의 value가 timestamp 형인지 확인
     *
     * @param qValue
     * @return
     */
    private static boolean isTimestampValue(String qValue) {

        Pattern p = Pattern.compile("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))");
        Matcher m = p.matcher(qValue);

        if (m.find()) {
            return true;
        }

        return false;
    }

    private static String makeQuery(String colName, String value, String operator) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(colName);
        stringBuilder.append(" ");
        stringBuilder.append(operator);
        stringBuilder.append(" ");
        stringBuilder.append(value);

        return stringBuilder.toString();
    }

    /**
     * DB column이 [] 리스트 형 일 경우, any()로 서치 해야 됨.
     * 좌,우 바뀜 [value = any(컬럼명)]
     */
    private static String makeQueryWithColumnArrType(String colName, String value, String operator) {

        colName = colName.split("::")[0];

        if (operator.equalsIgnoreCase(">=")) {
            operator = "<=";
        } else if (operator.equalsIgnoreCase(">")) {
            operator = "<";
        } else if (operator.equalsIgnoreCase("<=")) {
            operator = ">=";
        } else if (operator.equalsIgnoreCase("<")) {
            operator = ">";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(value);
        stringBuilder.append(" ");
        stringBuilder.append(operator);
        stringBuilder.append(" ");
        stringBuilder.append("any(");
        stringBuilder.append(colName);
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    /**
     * EQUAL형 인 케이스, SQL 작성
     *
     * @param colName
     * @param leftValue
     * @param rightValue
     * @return
     */
    private static String makeBetweenQuery(String colName, String leftValue, String rightValue) {

//        WHERE Price BETWEEN 10 AND 20;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(colName);
        stringBuilder.append(" BETWEEN ");
        stringBuilder.append(leftValue);
        stringBuilder.append(" AND ");
        stringBuilder.append(rightValue);

        return stringBuilder.toString();
    }


    /**
     * EQUAL형이고 target 컬럼이 arr 인 케이스, SQL 작성
     *
     * @param colName
     * @param leftValue
     * @param rightValue
     * @return
     */
    private static String makeBetweenQueryWithColumnArrType(String colName, String leftValue, String rightValue) {

        colName = colName.split("::")[0];
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(leftValue);
        stringBuilder.append(" ");
        stringBuilder.append("<=");
        stringBuilder.append(" ");
        stringBuilder.append("any(");
        stringBuilder.append(colName);
        stringBuilder.append(")");
        stringBuilder.append(" AND ");
        stringBuilder.append(rightValue);
        stringBuilder.append(" ");
        stringBuilder.append(">=");
        stringBuilder.append(" ");
        stringBuilder.append("any(");
        stringBuilder.append(colName);
        stringBuilder.append(")");

        return stringBuilder.toString();
    }


    /**
     * UNEQUAL형 인 케이스,  SQL 작성
     *
     * @param colName
     * @param leftValue
     * @param rightValue
     * @return
     */
    private static String makeNotBetweenQuery(String colName, String leftValue, String rightValue) {

//        WHERE Price BETWEEN 10 AND 20;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(colName);
        stringBuilder.append(" NOT BETWEEN ");
        stringBuilder.append(leftValue);
        stringBuilder.append(" AND ");
        stringBuilder.append(rightValue);

        return stringBuilder.toString();
    }


    /**
     * UNEQUAL형이고 target 컬럼이 arr 인 케이스,  SQL 작성
     *
     * @param colName
     * @param leftValue
     * @param rightValue
     * @return
     */
    private static String makeNotBetweenQueryWithColumnArrType(String colName, String leftValue, String rightValue) {
        colName = colName.split("::")[0];
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(leftValue);
        stringBuilder.append(" ");
        stringBuilder.append(">");
        stringBuilder.append(" ");
        stringBuilder.append("any(");
        stringBuilder.append(colName);
        stringBuilder.append(")");
        stringBuilder.append(" AND ");
        stringBuilder.append(rightValue);
        stringBuilder.append(" ");
        stringBuilder.append("<");
        stringBuilder.append(" ");
        stringBuilder.append("any(");
        stringBuilder.append(colName);
        stringBuilder.append(")");

        return stringBuilder.toString();
    }


    /**
     * Attribute 의 DB Column 타입이 Array 인지 여부를 반환
     *
     * @param reqAttrName
     * @param dataModelCacheVO
     * @return
     */
    public static boolean isArrayTypeColumn(String reqAttrName, DataModelCacheVO dataModelCacheVO) {

        String reqRootAttrName = "";
        String reqObjectMemberName = "";

        if (reqAttrName.contains("[")) {

            reqRootAttrName = reqAttrName.split("\\[")[0];
            reqObjectMemberName = StringUtils.substringBetween(reqAttrName, "[", "]");

        } else {
            reqRootAttrName = reqAttrName;
        }

        List<Attribute> rootAttributes = dataModelCacheVO.getDataModelVO().getAttributes();

        for (Attribute rootAttribute : rootAttributes) {

        	String rootAttributeId = rootAttribute.getName();
            if (rootAttributeId.equalsIgnoreCase(reqRootAttrName)) {
                if (rootAttribute.getValueType() == AttributeValueType.ARRAY_DOUBLE
                        || rootAttribute.getValueType() == AttributeValueType.ARRAY_INTEGER
                        || rootAttribute.getValueType() == AttributeValueType.ARRAY_STRING
                        || rootAttribute.getValueType() == AttributeValueType.ARRAY_BOOLEAN)
                    return true;
            }

            if (rootAttribute.getObjectMembers() != null) {
                List<ObjectMember> objectMembers = rootAttribute.getObjectMembers();
                for (ObjectMember objectMember : objectMembers) {
                    String objectMemberId = objectMember.getName();
                    if (objectMemberId.equalsIgnoreCase(reqObjectMemberName)) {
                        if (rootAttribute.getValueType() == AttributeValueType.ARRAY_OBJECT)
                            return true;

                    }
                }
            }
        }

        return false;
    }


    /**
     * Attribute 의 DB Column 타입이 VARCHAR 혹은 VARCHAR[] 인지 여부를 반환
     * @param columnName
     * @param dataModelCacheVO
     * @return
     */
    public static boolean isVarcharTypeColumn(String columnName, DataModelCacheVO dataModelCacheVO) {

    	Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = dataModelCacheVO.getDataModelStorageMetadataVO().getDbColumnInfoVOMap();
    	for(DataModelDbColumnVO dataModelDbColumnVO : dbColumnInfoVOMap.values()) {
    		if(dataModelDbColumnVO.getColumnName().equals(columnName)) {
    			if(dataModelDbColumnVO.getColumnType() == DbColumnType.VARCHAR
    					|| dataModelDbColumnVO.getColumnType() == DbColumnType.ARRAY_VARCHAR) {
    				return true;
    			}
    		}
    	}
    	
        return false;
    }
    
    /**
     * Attribute 의 DB Column 명 반환
     *
     * @param attrName
     * @return
     */
    public static String getColumnName(String attrName, DataModelCacheVO dataModelCacheVO) {

        attrName = attrName.replace("[", "_");
        attrName = attrName.replace("]", "");

        String columnName = attrName;

        columnName = columnName.replace(".", "_");

        columnName = columnName.toLowerCase();

        // q query로 받은 datasetId 를 column명 dataset_id 로 변환
        if(DefaultAttributeKey.DATASET_ID.getCode().equals(attrName)) {
        	columnName = DefaultDbColumnName.DATASET_ID.getCode();
    	}

        if(dataModelCacheVO != null
                && dataModelCacheVO.getDataModelStorageMetadataVO() != null
                && dataModelCacheVO.getDataModelStorageMetadataVO().getDbColumnInfoVOMap() != null
                && !dataModelCacheVO.getDataModelStorageMetadataVO().getDbColumnInfoVOMap().containsKey(columnName)) {
            throw new NgsiLdBadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "invalid q-query params. attrName=" + attrName);
        }

        return columnName;//TODO: need to check
    }


    /**
     * Attribute 의 DB Column 명과 타입을 반환
     * ex) NAME::text
     *
     * @param reqAttrName
     * @return
     */
    public static String getColumnNameWithType(String reqAttrName, DataModelCacheVO dataModelCacheVO) throws NgsiLdBadRequestException {

        String reqRootAttrName = "";
        String reqChildAttributeName = "";
        String columnName;
        if (reqAttrName.contains("[")) {

            reqRootAttrName = reqAttrName.split("\\[")[0];
            reqChildAttributeName = StringUtils.substringBetween(reqAttrName, "[", "]");

            columnName = reqRootAttrName + "_" + reqChildAttributeName;

        } else {
            reqRootAttrName = reqAttrName;
            columnName = reqRootAttrName;
        }

        columnName = columnName.replace(".", "_");

        if(dataModelCacheVO != null
                && dataModelCacheVO.getDataModelStorageMetadataVO() != null
                && dataModelCacheVO.getDataModelStorageMetadataVO().getDbColumnInfoVOMap() != null
                && !dataModelCacheVO.getDataModelStorageMetadataVO().getDbColumnInfoVOMap().containsKey(columnName.toLowerCase())) {
            throw new NgsiLdBadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "invalid q-query params. attrName=" + reqAttrName);
        }
        return columnName;
    }


    /**
     * @param attr fullurl -> name
     * @return
     */
    public static String getAttrNameFromAttributeUri(DataModelCacheVO dataModelCacheVO, String attr) {

        for (Attribute attribute : dataModelCacheVO.getDataModelVO().getAttributes()) {
            String attributeUri = attribute.getAttributeUri();

            if (!ValidateUtil.isEmptyData(attr) && !ValidateUtil.isEmptyData(attributeUri) && attr.equals(attributeUri)) {
                return attribute.getName();
            }
        }
        return null;
    }
    

    private static String getQuerySubstring(String q_query, String operator) {
    	if(q_query.contains(operator)) {
    		return q_query.substring(0, q_query.indexOf(operator));
    	}
    	return q_query;
    }
    
    public static Map<String, String> queryStringToMap(String queryString) {
	    Map<String, String> result = new HashMap<String, String>();
	    for (String param : queryString.split("&")) {
	        String pair[] = param.split("=", 2);
	        if (pair.length > 1) {
	            result.put(pair[0], pair[1]);
	        }else{
	            result.put(pair[0], "");
	        }
	    }
	    return result;
    }
    
    public static String mapToQueryString(Map<String, String> map) {

    	if(map != null && !map.isEmpty()) {
    		StringBuilder queryBuilder = new StringBuilder();
    		for(Map.Entry<String, String> entry : map.entrySet()) {
    			queryBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
    		}
    		queryBuilder.deleteCharAt(queryBuilder.length()-1);
    		return queryBuilder.toString();
    	}
    	return null;
    }

    public static boolean includeAttrsQuery(QueryVO queryVO) {
        if(!ValidateUtil.isEmptyData(queryVO.getAttrs())) {
            return true;
        }
        return false;
    }

    public static boolean includeQQuery(QueryVO queryVO) {
        if(!ValidateUtil.isEmptyData(queryVO.getQ())) {
            return true;
        }
        return false;
    }

    public static boolean includeGeoQuery(QueryVO queryVO) {
        if(!ValidateUtil.isEmptyData(queryVO.getGeorel())
                && !ValidateUtil.isEmptyData(queryVO.getGeometry())
                && !ValidateUtil.isEmptyData(queryVO.getCoordinates())) {
            return true;
        }
        return false;
    }

    public static boolean validateGeoQuery(QueryVO queryVO) {
        if(!ValidateUtil.isEmptyData(queryVO.getGeorel())
            || !ValidateUtil.isEmptyData(queryVO.getGeometry())
            || !ValidateUtil.isEmptyData(queryVO.getCoordinates())) {

            if(!includeGeoQuery(queryVO)) {
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid geo-query params. geo-query requires 3 parameters ('georel', 'geometry', 'coordinates')");
            }
        }
        return true;
    }

    public static String convertGeoQuery(QueryVO queryVO) {

        GeometryType georelType = queryVO.getGeorelType();

        if(georelType == null) {
            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid geo-query params. georel=" + queryVO.getGeorel());
        }

        String query = null;
        StringBuilder stringBuilder = new StringBuilder();

        if (georelType == GeometryType.NEAR_REL) {

            if (queryVO.getMinDistance() != null) {

                stringBuilder.append(" ST_DWithin(")
                        .append(queryVO.getLocationCol())
                        .append(", ST_GeogFromText('").append(queryVO.getGeometryValue()).append("')")
                        .append(", " + queryVO.getMinDistance())
                        .append(") is false");


            } else if (queryVO.getMaxDistance() != null) {

                stringBuilder.append(" ST_DWithin(")
                        .append(queryVO.getLocationCol())
                        .append(", ST_GeogFromText('").append(queryVO.getGeometryValue()).append("')")
                        .append(", " + queryVO.getMaxDistance())
                        .append(")");


            }

        } else if (georelType == GeometryType.WITHIN_REL) {

            stringBuilder.append("ST_Within(")
                    .append(queryVO.getLocationCol())
                    .append(", ST_GeogFromText('").append(queryVO.getGeometryValue()).append("')::geometry")
                    .append(")");

        } else if (georelType == GeometryType.CONTAINS_REL) {

            stringBuilder.append("ST_Contains(")
                    .append(queryVO.getLocationCol())
                    .append(", ST_GeogFromText('").append(queryVO.getGeometryValue()).append("')::geometry")
                    .append(")");

        } else if (georelType == GeometryType.OVERLAPS_REL) {

            stringBuilder.append("ST_Overlaps(")
                    .append(queryVO.getLocationCol())
                    .append(", ST_GeogFromText('").append(queryVO.getGeometryValue()).append("')::geometry")
                    .append(")");

        } else if (georelType == GeometryType.INTERSECTS_REL) {

            stringBuilder.append("ST_Intersects(")
                    .append(queryVO.getLocationCol())
                    .append(", ST_GeogFromText('").append(queryVO.getGeometryValue()).append("')::geometry")
                    .append(")");

        } else if (georelType == GeometryType.EQUALS_REL) {

            stringBuilder.append("ST_Equals(")
                    .append(queryVO.getLocationCol())
                    .append(", ST_GeogFromText('").append(queryVO.getGeometryValue()).append("')::geometry")
                    .append(")");
        } else if (georelType == GeometryType.DISJOINT_REL) {

            stringBuilder.append("ST_Disjoint(")
                    .append(queryVO.getLocationCol())
                    .append(", ST_GeogFromText('").append(queryVO.getGeometryValue()).append("')::geometry")
                    .append(")");

        }

        query = stringBuilder.toString();

        return query;
    }
}
