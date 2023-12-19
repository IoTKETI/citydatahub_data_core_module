package kr.re.keti.sc.datamanager.common.code;

/**
 * Constants class
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

    public static final String COLUMN_DELIMITER = "_";

	public static final String SCHEMA_NAME = "smart_city";
	public static final String PARTIAL_HIST_TABLE_PREFIX = "_hist";
	public static final String FULL_HIST_TABLE_PREFIX = "_full_hist";

	public static final String CHARSET_ENCODING = "UTF-8";

	public static final String PREFIX_SUBSCRIPTION_ID = "urn:ngsi-ld:Subscription:";
	public static final String TOTAL_COUNT= "totalCount";

	// for acl rule
	public static final String ACL_DATASET_IDS = "acl_dataset_ids";
	public static final String ACL_ADMIN = "acl_admin";
}
