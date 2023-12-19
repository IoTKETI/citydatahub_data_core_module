package kr.re.keti.sc.datacoreusertool.common.code;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Widget dashboard code class
 * @FileName WidgetDashboardCode.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
public class WidgetDashboardCode {
	
	public static enum ChartType {
        DONUT("donut"),
        PIE("pie"),
        LINE("line"),
        BAR("bar"),
        TEXT("text"),
        BOOLEAN("boolean"),
        HISTOGRAM("histogram"),
        SCATTER("scatter"),
        MAP_LATEST("map_latest"),
        MAP_HISTORY("map_history");

        private String code;

        private ChartType(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static ChartType parseType(String code) {
            for (ChartType defaultAttributeKey : values()) {
                if (defaultAttributeKey.getCode().equals(code)) {
                    return defaultAttributeKey;
                }
            }
            return null;
        }
    }
	
	public static enum DataType {
        HISTORY("history"),
        LAST("last");

        private String code;

        private DataType(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static DataType parseType(String code) {
            for (DataType defaultAttributeKey : values()) {
                if (defaultAttributeKey.getCode().equals(code)) {
                    return defaultAttributeKey;
                }
            }
            return null;
        }
    }
	
	public static enum UpdateType {
        REALTIME("realtime"),
        PERIODIC("periodic");

        private String code;

        private UpdateType(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static UpdateType parseType(String code) {
            for (UpdateType defaultAttributeKey : values()) {
                if (defaultAttributeKey.getCode().equals(code)) {
                    return defaultAttributeKey;
                }
            }
            return null;
        }
    }
	
	public static enum WidgetMethod {
        CREATE("create"),
        READ("read"),
        UPDATE("update"),
        DELETE("delete");

        private String code;

        private WidgetMethod(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static WidgetMethod parseType(String code) {
            for (WidgetMethod defaultAttributeKey : values()) {
                if (defaultAttributeKey.getCode().equals(code)) {
                    return defaultAttributeKey;
                }
            }
            return null;
        }
    }
	
	public static enum ErrorCode {
        UNKNOWN_ERROR("C001"),
        NOT_EXIST_ENTITY("C002"),
        SQL_ERROR("C003"),
        MEMORY_QUEUE_INPUT_ERROR("C004"),
        REQUEST_MESSAGE_PARSING_ERROR("C005"),
        RESPONSE_MESSAGE_PARSING_ERROR("C006"),
        INVALID_ENTITY_TYPE("C007"),
        INVALID_ACCEPT_TYPE("C008"),
        INVALID_PARAMETER("C009"),
        INVALID_AUTHORIZATION("C0010"),

        LENGTH_REQUIRED("C0011"),
        ALREADY_EXISTS("C0012"),
        NOT_EXIST_ID("C013"),
        NOT_EXIST_ENTITY_ATTR("C014"),
        LOAD_ENTITY_SCHEMA_ERROR("C015"),
        CREATE_ENTITY_TABLE_ERROR("C016"),
        OPERATION_NOT_SUPPORTED("C017"),

        BAD_REQUEST("C099"),
        NOT_SUPPORTED_METHOD("C100");

        ;

        private String code;

        private ErrorCode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }


        public static ErrorCode parseCode(String code) {
            for (ErrorCode errorCode : values()) {
                if (errorCode.getCode().equals(code)) {
                    return errorCode;
                }
            }
            return null;
        }
    }
	
	public static enum DefaultErrorKey {

        TYPE("type"),
        TITLE("title"),
        DETAIL("detail"),
        DEBUG_MESSAGE("debugMessage");

        private String code;

        private DefaultErrorKey(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static DefaultErrorKey parseType(String code) {
            for (DefaultErrorKey defaultErrorKey : values()) {
                if (defaultErrorKey.getCode().equals(code)) {
                    return defaultErrorKey;
                }
            }
            return null;
        }
    }
}
