package kr.re.keti.sc.datacoreusertool.common.code;

/**
 * Common constant management class
 * @FileName Constants.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
public class Constants {
	/** Default package path */
	public static final String BASE_PACKAGE = "kr.re.keti.sc";
	/** Content Date Format */
	public static final String CONTENT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	/** Retrieve Date Format */
	public static final String RETRIEVE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
	/** PostgreSQL SELECT Date Format */
	public static final String POSTGRES_DATE_FORMAT = "yyyy-MM-dd' 'HH:mm:ss";
	/** PostgreSQL INSERT Date Format */
	public static final String POSTGRES_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	/** Content Date TimeZone */
	public static final String CONTENT_DATE_TIMEZONE = "Asia/Seoul";
	
	// HTTP Header
	public static final String ACCEPT_TYPE_APPLICATION_JSON = "application/json";
	public static final String CONTENT_TYPE_IMAGE_JPEG = "image/jpeg";
	public static final String HTTP_HEADER_KEY_ACCEPT = "Accept";
	public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
	public static final String HTTP_HEADER_LINK = "Link";
	
	// DataModel Attribute
	public final static String ALL_LEVEL_ATTR = "ALL_LEVEL_ATTR";
	public final static String TOP_LEVEL_ATTR = "TOP_LEVEL_ATTR";
	public final static String OBSERVED_AT_ATTR = "OBSERVED_AT_ATTR";
}
