package kr.re.keti.sc.dataservicebroker.common.code;

/**
 * 공통 상수 관리 클래스
 */
public class Constants {
    /** 기본 패키지 경로 */
    public static final String BASE_PACKAGE = "kr.re.keti.sc";
    /** 카프카 처리 에러 기록용 로거명 */
    public static final String KAFKA_REQUEST_ERROR_LOGGER_NAME = "kafkaRequestErrorLogger";
    /** Content Date Format */
    public static final String CONTENT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    
    public static final String[] HIVE_MULTI_DATE_FORMATS = new String[] {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.S",
            "yyyy-MM-dd HH:mm:ss.SS",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss.SSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSS"
        };
    
    public static final String[] MULTI_DATE_FORMATS = new String[] {
            "yyyy-MM-dd HH:mm:ss.XXX",
            "yyyy-MM-dd'T'HH:mm:ss.SXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss,SSSXXX" // 하위 호환
        };
    
    /** PostgreSQL SELECT Date Format */
    public static final String POSTGRES_DATE_FORMAT = "yyyy-MM-dd' 'HH:mm:ss";
    /** PostgreSQL INSERT Date Format */
    public static final String POSTGRES_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    
    /** Hive INSERT Date Format */
    public static final String HIVE_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    
    /** application/ld+json Key */
    public static final String APPLICATION_LD_JSON_VALUE = "application/ld+json";
    public static final String APPLICATION_JSON_VALUE = "application/json";
    public static final String APPLICATION_GEO_JSON_VALUE = "application/geo+json";
    public static final String APPLICATION_MERGE_PATCH_JSON_VALUE = "application/merge-patch+json";
    public static final String ACCEPT_ALL = "*/*";

    public static final int DEFAULT_SRID = 4326;
    public static final String GEO_PREFIX_4326 = "_4326";
    public static final String GEO_PREFIX_3857 = "_3857";
    public static final String COLUMN_DELIMITER = "_";
    public static final String INDEX_ATTRIBUTE_NAME_DELIMITER = ".";

    public static final String DATA_MODEL_TYPE_DELIMITER = ".";
    public static final String DATA_MODEL_VERSION_DELIMITER = ":";

    public static final String SCHEMA_NAME = "DATA_SERVICE_BROKER";
    public static final String TABLE_NAME_DELIMITER_UNDER = "_";
    public static final String PARTIAL_HIST_TABLE_PREFIX = "_partial_hist";
    public static final String FULL_HIST_TABLE_PREFIX = "_full_hist";

    public static final String CHARSET_ENCODING = "UTF-8";

    public static final String PREFIX_SUBSCRIPTION_ID = "urn:ngsi-ld:Subscription:";
    public static final String PREFIX_CSOURCE_REGISTRATION_ID = "urn:ngsi-ld:ContextSourceRegistration:";
    public static final String PREFIX_CSOURCE_REGISTRATION_SUBSCRIPTION_ID = "urn:ngsi-ld:ContextSourceRegistrationSubscription:";
    public static final String PREFIX_SERVICE_REGISTRATION_ID = "urn:ngsi-ld:serviceRegistration:";
    public static final String PREFIX_SERVICE_EXECUTION_ID = "urn:ngsi-ld:serviceRequest:";

    // for acl rule
    public static final String ACL_PERMISSION_KEY = "ACL_PERMISSION_KEY";

    public static final int POSTGRES_COLUMN_NAME_MAX_LENTH = 63;

    public static final String LOCATION_ATTR_DEFAULT_NAME = "location";
    public static final String PATIAL_HIST_TABLE_SUFFIX = null;
    
}
