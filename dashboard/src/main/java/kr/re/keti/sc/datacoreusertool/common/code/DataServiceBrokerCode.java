package kr.re.keti.sc.datacoreusertool.common.code;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DataServiceBroker code class
 * @FileName DataServiceBrokerCode.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
public class DataServiceBrokerCode {
	
	public static enum DefaultAttributeKey {
        CONTEXT("@context"),
        ID("id"),
        DATASET_ID("datasetId"),
        CREATED_AT("createdAt"),
        MODIFIED_AT("modifiedAt"),
        OPERATION("operation"),
        TYPE("type"),
        ATTR_ID("attrId"),
    	OBSERVED_AT("observedAt");

        private String code;

        private DefaultAttributeKey(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static DefaultAttributeKey parseType(String code) {
            for (DefaultAttributeKey defaultAttributeKey : values()) {
                if (defaultAttributeKey.getCode().equals(code)) {
                    return defaultAttributeKey;
                }
            }
            return null;
        }
    }
	
	public static enum AttributeType {
        @JsonProperty("Property")
        PROPERTY("Property"),
        @JsonProperty("GeoProperty")
        GEO_PROPERTY("GeoProperty"),
        @JsonProperty("Relationship")
        RELATIONSHIP("Relationship");

        private String code;

        @JsonCreator
        private AttributeType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static AttributeType parseType(String code) {
            for (AttributeType attributeType : values()) {
                if (attributeType.getCode().equals(code)) {
                    return attributeType;
                }
            }
            return null;
        }
    }
	
	public static enum AttributeValueType {
        @JsonProperty("String")
        STRING("String"),
        @JsonProperty("Integer")
        INTEGER("Integer"),
        @JsonProperty("Double")
        DOUBLE("Double"),
        @JsonProperty("Object")
        OBJECT("Object"),
        @JsonProperty("Date")
        DATE("Date"),
        @JsonProperty("Boolean")
        BOOLEAN("Boolean"),
        @JsonProperty("GeoJson")
        GEO_JSON("GeoJson"),
        @JsonProperty("ArrayString")
        ARRAY_STRING("ArrayString"),
        @JsonProperty("ArrayInteger")
        ARRAY_INTEGER("ArrayInteger"),
        @JsonProperty("ArrayDouble")
        ARRAY_DOUBLE("ArrayDouble"),
        @JsonProperty("ArrayBoolean")
        ARRAY_BOOLEAN("ArrayBoolean"),
        @JsonProperty("ArrayObject")
        ARRAY_OBJECT("ArrayObject"),
        ;

        private String code;

        @JsonCreator
        private AttributeValueType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static AttributeValueType parseType(String code) {
            for (AttributeValueType attributeValueType : values()) {
                if (attributeValueType.getCode().equals(code)) {
                    return attributeValueType;
                }
            }
            return null;
        }
    }
	
	public static enum AccessMode {

        @JsonProperty("Read-only")
        READ_ONLY("Read-only"),
        @JsonProperty("Read/Write")
        READ_WRITE("Read/Write");

        private String code;

        @JsonCreator
        private AccessMode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static AccessMode parseType(String code) {
            for (AccessMode attributeType : values()) {
                if (attributeType.getCode().equals(code)) {
                    return attributeType;
                }
            }
            return null;
        }
    }
	
	public static enum BigDataStorageType {

    	@JsonProperty("rdb")
    	RDB("rdb"),
        @JsonProperty("hive")
        HIVE("hive"),
        @JsonProperty("hbase")
        HBASE("hbase"),
        @JsonProperty("hive_hbase")
        HIVE_HBASE("hive_hbase");

        private String code;

        @JsonCreator
        private BigDataStorageType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static BigDataStorageType parseType(String code) {
            for (BigDataStorageType bigDataStorageType : values()) {
                if (bigDataStorageType.getCode().equals(code)) {
                    return bigDataStorageType;
                }
            }
            return null;
        }
    }
	
	public static enum QueryOperatorType {

    	@JsonProperty("==")
    	OPR_01("=="),
        @JsonProperty("!=")
    	OPR_02("!="),
        @JsonProperty("<")
    	OPR_03("<"),
    	@JsonProperty("<=")
    	OPR_04("<="),
    	@JsonProperty(">")
    	OPR_05(">"),
        @JsonProperty(">=")
    	OPR_06(">="),
    	@JsonProperty("~=")
    	OPR_07("~=");

        private String code;

        @JsonCreator
        private QueryOperatorType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static QueryOperatorType parseType(String code) {
            for (QueryOperatorType queryOperatorType : values()) {
                if (queryOperatorType.getCode().equals(code)) {
                    return queryOperatorType;
                }
            }
            return null;
        }
    }
	
	public static enum UseYn {

        YES("Y"),
        NO("N");

        private String code;

        private UseYn(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static UseYn parseType(String code) {
            for (UseYn useYn : values()) {
                if (useYn.getCode().equals(code)) {
                    return useYn;
                }
            }
            return null;
        }
    }
}
