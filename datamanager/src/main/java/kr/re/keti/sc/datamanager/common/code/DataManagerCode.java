package kr.re.keti.sc.datamanager.common.code;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Common code class
 */
public class DataManagerCode {

    public static enum Operation {

        @JsonProperty("Create Data Model")
        CREATE_DATA_MODEL("Create Data Model"),

        @JsonProperty("Update Data Model")
        UPDATE_DATA_MODEL("Update Data Model"),

        @JsonProperty("Update Data Model Attribute")
        UPDATE_DATA_MODEL_ATTRIBUTE("Update Data Model Attribute"),

        @JsonProperty("Delete Data Model Attribute")
        DELETE_DATA_MODEL_ATTRIBUTE("Delete Data Model Attribute"),


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
        RETRIEVE("RETRIEVE"),


        //하위 호환 데이터 조회를 위해 추가
        @Deprecated
        CREATE("CREATE"),
        @Deprecated
        PARTIAL_UPDATE("PARTIAL_UPDATE"),
        @Deprecated
        FULL_UPDATE("FULL_UPDATE"),
        @Deprecated
        PARTIAL_UPSERT("PARTIAL_UPSERT"),
        @Deprecated
        FULL_UPSERT("FULL_UPSERT"),
        @Deprecated
        DELETE("DELETE"),
        ;

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

    public static enum RepresentationType {

        FULL("FULL"),
        SIMPLIFIED("SIMPLIFIED");

        private String code;

        private RepresentationType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static RepresentationType parseType(String code) {
            for (RepresentationType representationType : values()) {
                if (representationType.getCode().equals(code)) {
                    return representationType;
                }
            }
            return null;
        }
    }

    public static enum EventType {
        PARTIALLY_UPDATED, FULLY_UPDATED, CREATED, DELETED;
    }

    public static enum RequestParameterName {
        REQUEST_ID("requestId"),
        E2E_REQUEST_ID("e2eRequestId"),
        OPERATION("operation"),
        TO("to"),
        CONTENT_TYPE("contentType"),
        QUERY("query"),
        CONTENT("content"),
        OWNER("owner");

        private String name;

        private RequestParameterName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
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

        NOT_SUPPORTED_METHOD("C101"),
        NOT_EXISTS_DATAMODEL("C111"),
        NOT_EXISTS_DATASET("C112"),
        NOT_EXISTS_DATASETFLOW("C113"),
        PROVISIONING_ERROR("C114")
        ;

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

    public static enum RetrieveOptions {
        NORMALIZED("normalized"),
        KEY_VALUES("keyValues"),
        KEY_VALUES_HISTORY("keyValuesHistory"),
        NORMALIZED_HISTORY("normalizedHistory"),
        TEMPORAL_VALUES("temporalValues"),
        ;

        private String code;

        private RetrieveOptions(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static RetrieveOptions parseType(String code) {
            for (RetrieveOptions operation : values()) {
                if (operation.getCode().equals(code)) {
                    return operation;
                }
            }
            return null;
        }
    }

    public static enum GeometryType {
        POINT("Point"),
        POLYGON("Polygon"),
        MULTI_POLYGON("MultiPolygon"),
        LINE_STRING("LineString"),
        MULTI_LINE_STRING("MultiLineString"),
        NEAR_REL("near"),
        WITHIN_REL("within"),
        CONTAINS_REL("contains"),
        INTERSECTS_REL("intersects"),
        EQUALS_REL("equals"),
        DISJOINT_REL("disjoint"),
        OVERLAPS_REL("overlaps"),
        MIN_DISTANCE("minDistance"),
        MAX_DISTANCE("maxDistance"),
        ;

        private String code;

        private GeometryType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }


        public static GeometryType parseType(String code) {
            for (GeometryType geometryType : values()) {
                if (geometryType.getCode().equals(code)) {
                    return geometryType;
                }
            }
            return null;
        }
    }


    public static enum QueryOperator {

        SINGLE_EQUAL("=", "%x3D"),


        EQUAL("==", "%x3D%x3D"),
        UNEQUAL("!=", "%x21%x3D"),

        GREATER(">", "%x3E"),
        GREATEREQ(">=", "%x3E%x3D"),

        LESS("<", "%x3C"),
        LESSEQ("<=", "%x3C%x3D"),


        DOTS("..", "%x2E%x2E"),
        COMMA(",", "%x2C"),

        ;

        private String sign;
        private String unicode;

        private QueryOperator(String sign, String unicode) {
            this.sign = sign;
            this.unicode = unicode;
        }


        public String getSign() {
            return sign;
        }

        public String getUnicode() {
            return unicode;
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
        COORDINATES("coordinates"),
        UNIT_CODE("unitCode"),
        CREATED_AT("createdAt"),
        MODIFIED_AT("modifiedAt"),
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
        DATASET_ID("datasetId"),
        DATA_MODEL_ID("dataModelId"),
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

    public static enum DefaultDbColumnName {
        CONTEXT("context"),
        ID("id"),
        CREATED_AT("created_at"),
        MODIFIED_AT("modified_at");

        private String code;

        private DefaultDbColumnName(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static DefaultDbColumnName parseType(String code) {
            for (DefaultDbColumnName defaultDbColumnName : values()) {
                if (defaultDbColumnName.getCode().equals(code)) {
                    return defaultDbColumnName;
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
        @JsonProperty("Long")
        LONG("Long"),
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
        @JsonProperty("ArrayLong")
        ARRAY_LONG("ArrayLong"),
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


    public static enum AttributeResultType {
        //property 결과
        @JsonProperty("value")
        VALUE("value"),
        //relationship 결과
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

    public static enum ProvisionServerType {

        @JsonProperty("dataServiceBroker")
        DATA_SERVICE_BROKER("dataServiceBroker"),
        @JsonProperty("ingestInterface")
        INGEST_INTERFACE("ingestInterface"),
        ;

        private String code;

        @JsonCreator
        private ProvisionServerType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static ProvisionServerType parseType(String code) {
            for (ProvisionServerType provosionServerType : values()) {
                if (provosionServerType.getCode().equals(code)) {
                    return provosionServerType;
                }
            }
            return null;
        }
    }

    public static enum ProvisionProtocol {

        @JsonProperty("http")
        HTTP("http"),
        @JsonProperty("kafka")
        KAFKA("kafka");

        private String code;

        @JsonCreator
        private ProvisionProtocol(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static ProvisionProtocol parseType(String code) {
            for (ProvisionProtocol provisionProtocol : values()) {
                if (provisionProtocol.getCode().equals(code)) {
                    return provisionProtocol;
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
        HBASE("hbase");

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

    public static enum ProvisionEventType {

        @JsonProperty("created")
        CREATED("created"),
        @JsonProperty("updated")
        UPDATED("updated"),
        @JsonProperty("deleted")
        DELETED("deleted");

        private String code;

        @JsonCreator
        private ProvisionEventType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static ProvisionEventType parseType(String code) {
            for (ProvisionEventType provisionEventType : values()) {
                if (provisionEventType.getCode().equals(code)) {
                    return provisionEventType;
                }
            }
            return null;
        }
    }

    public static enum HistoryStoreType {

    	@JsonProperty("none")
        NONE("none"),
        @JsonProperty("partial")
        PARTIAL("partial"),
        @JsonProperty("full")
        FULL("full"),
        @JsonProperty("all")
        ALL("all");

        private String code;

        @JsonCreator
        private HistoryStoreType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static HistoryStoreType parseType(String code) {
            for (HistoryStoreType historyStoreType : values()) {
                if (historyStoreType.getCode().equals(code)) {
                    return historyStoreType;
                }
            }
            return null;
        }
    }

    public static enum ProvisioningSubUri {

        DATA_MODEL("/provision/datamodels"),
        DATASET("/provision/datasets"),
        DATASET_FLOW("/provision/datasetflows"),
        ACL_RULE("/provision/acl/rules");

        private String code;

        @JsonCreator
        private ProvisioningSubUri(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static ProvisioningSubUri parseType(String code) {
            for (ProvisioningSubUri provisioningSubUri : values()) {
                if (provisioningSubUri.getCode().equals(code)) {
                    return provisioningSubUri;
                }
            }
            return null;
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
        CREATE("create"),
        @JsonProperty("update")
        UPDATE("update"),
        @JsonProperty("delete")
        DELETE("delete"),
        @JsonProperty("retrieve")
        RETRIEVE("retrieve");

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
