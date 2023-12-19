package kr.re.keti.sc.datacoreui.common.code;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Common code class
 * @FileName DataCoreUiCode.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
public class DataCoreUiCode {

    public static enum Operation {
    	@JsonProperty("Create Entity")
    	CREATE_ENTITY("Create Entity"),
    	@JsonProperty("Update Entity Attributes")
    	UPDATE_ENTITY_ATTRIBUTES("Update Entity Attributes"),
    	@JsonProperty("Append Entity Attributes")
    	APPEND_ENTITY_ATTRIBUTES("Append Entity Attributes"),
    	@JsonProperty("Partial Attribute Update")
    	PARTIAL_ATTRIBUTE_UPDATE("Partial Attribute Update"),
    	@JsonProperty("Replace Entity Attributes")
    	REPLACE_ENTITY_ATTRIBUTES("Replace Entity Attributes"),
    	@JsonProperty("Create Entity or Append Entity Attributes")
        CREATE_ENTITY_OR_APPEND_ENTITY_ATTRIBUTES("Create Entity or Append Entity Attributes"),
        @JsonProperty("Create Entity or Replace Entity Attributes")
        CREATE_ENTITY_OR_REPLACE_ENTITY_ATTRIBUTES("Create Entity or Replace Entity Attributes"),
        @JsonProperty("Delete Entity")
        DELETE_ENTITY("Delete Entity"),
        @JsonProperty("Delete Entity Attributes")
        DELETE_ENTITY_ATTRIBUTES("Delete Entity Attributes"),
        @JsonProperty("RETRIEVE")
        RETRIEVE("RETRIEVE");

        private String code;

        private Operation(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static Operation parseType(String code) {
            for (Operation operation : values()) {
                if (operation.getCode().equals(code)) {
                    return operation;
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

    public static enum TemporalOperator {

        BEFORE_REL("before"),
        AFTER_REL("after"),
        BETWEEN_REL("between"),

        TIME("time"),
        END_TIME("endtime"),
        TIME_PROPERTY("timeproperty"),
        ;

        private String code;

        private TemporalOperator(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public static enum PropertyKey {
        TYPE("type"),
        VALUE("value"),
        OBSERVED_AT("observedAt"),
        OBJECT("object"),
        ;

        private String code;

        private PropertyKey(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static PropertyKey parseType(String code) {
            for (PropertyKey propertyKey : values()) {
                if (propertyKey.getCode().equals(code)) {
                    return propertyKey;
                }
            }
            return null;
        }
    }

    public static enum DefaultAttributeKey {
        CONTEXT("@context"),
        ID("id"),
        CREATED_AT("createdAt"),
        MODIFIED_AT("modifiedAt"),
        OPERATION("operation"),
        TYPE("type"),
        OWNER("owner"),
    	ATTR_ID("attrId");

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
        @JsonProperty("GeoJson")
        GEO_JSON("GeoJson"),
        @JsonProperty("ArrayString")
        ARRAY_STRING("ArrayString"),
        @JsonProperty("ArrayInteger")
        ARRAY_INTEGER("ArrayInteger"),
        @JsonProperty("ArrayDouble")
        ARRAY_DOUBLE("ArrayDouble"),
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


    public static enum AttributeResultType {
        //property result
        @JsonProperty("value")
        VALUE("value"),
        //relationship result
        @JsonProperty("object")
        OBJECT("object");

        private String code;

        @JsonCreator
        private AttributeResultType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static AttributeResultType parseType(String code) {
            for (AttributeResultType attributeType : values()) {
                if (attributeType.getCode().equals(code)) {
                    return attributeType;
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

    public static enum JsonLdType {
    	NOTIFICATION("Notification"),
    	SUBSCRIPTION("Subscription");

    	private String code;

        private JsonLdType(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }
    }

    public static enum OperationOption {
    	NO_OVERWRITE("noOverwrite"),
    	;

        private String code;

        private OperationOption(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
    
    public static enum AclRuleCondition {

        @JsonProperty("AND")
        AND("AND"),
        @JsonProperty("OR")
        OR("OR");

        private String code;

        @JsonCreator
        private AclRuleCondition(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static AclRuleCondition parseType(String code) {
            for (AclRuleCondition aclRuleCondition : values()) {
                if (aclRuleCondition.getCode().equals(code)) {
                    return aclRuleCondition;
                }
            }
            return null;
        }
    }

    public static enum AclRuleResourceType {

        @JsonProperty("DATASET")
        DATASET("DATASET");

        private String code;

        @JsonCreator
        private AclRuleResourceType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static AclRuleResourceType parseType(String code) {
            for (AclRuleResourceType aclRuleCondition : values()) {
                if (aclRuleCondition.getCode().equals(code)) {
                    return aclRuleCondition;
                }
            }
            return null;
        }
    }
    
    public static enum AclRuleOperationType {

        @JsonProperty("create")
        create("create"),
        @JsonProperty("update")
        update("update"),
        @JsonProperty("delete")
        delete("delete"),
        @JsonProperty("retrieve")
        retrieve("retrieve");

        private String code;

        @JsonCreator
        private AclRuleOperationType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static AclRuleOperationType parseType(String code) {
            for (AclRuleOperationType aclRuleOperation : values()) {
                if (aclRuleOperation.getCode().equals(code)) {
                    return aclRuleOperation;
                }
            }
            return null;
        }
    }
}
