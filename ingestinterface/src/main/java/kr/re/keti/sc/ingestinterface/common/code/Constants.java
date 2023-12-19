package kr.re.keti.sc.ingestinterface.common.code;

/**
 * Common constants class
 */
public class Constants {
	/** base package path */
	public static final String BASE_PACKAGE = "kr.re.keti.sc";
	/** kafka error logger */
	public static final String KAFKA_REQUEST_ERROR_LOGGER_NAME = "kafkaRequestErrorLogger";
	/** Content Date Format */
	public static final String CONTENT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	
	public static final String[] MULTI_DATE_FORMATS = new String[] {
            "yyyy-MM-dd'T'HH:mm:ssXXX",
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
	/** Content Date TimeZone */
	public static final String CONTENT_DATE_TIMEZONE = "Asia/Seoul";

	/** application/ld+json Key */
	public static final String APPLICATION_LD_JSON_VALUE = "application/ld+json";
	public static final String APPLICATION_JSON_VALUE = "application/json";
	public static final String APPLICATION_GEO_JSON_VALUE = "application/geo+json";
	public static final String APPLICATION_MERGE_PATCH_JSON_VALUE = "application/merge-patch+json";
	public static final String ACCEPT_ALL = "*/*";

    public static final String DATA_MODEL_TYPE_DELIMITER = ".";
	public static final String DATA_MODEL_VERSION_DELIMITER = ":";

	public static final String CHARSET_ENCODING = "UTF-8";
	
	public static final int DEFAULT_SRID = 4326;
	public static final String TOTAL_COUNT= "totalCount";

	// for acl rule
	public static final String ACL_PERMISSION_KEY = "ACL_PERMISSION_KEY";
}
