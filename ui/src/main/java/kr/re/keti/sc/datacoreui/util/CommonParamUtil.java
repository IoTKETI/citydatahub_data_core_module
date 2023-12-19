package kr.re.keti.sc.datacoreui.util;

import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode;
import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode.Operation;
import kr.re.keti.sc.datacoreui.common.exception.BadRequestException;

/**
 * Utility for common parameter 
 * @FileName CommonParamUtil.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
public class CommonParamUtil {

	/** ContentType parameter separator */
	private static final String CONTENT_TYPE_SEPARATOR = "=";
	/** Type index */
	private static final int CONTENT_TYPE_ENTITY_TYPE_INDEX = 1;

	/** Resource ID parameter separator */
	private static final String RESOURCE_ID_SEPARATOR = ":";
	/** entityType index */
	private static final int RESOURCE_ID_ENTITY_TYPE_INDEX = 2;

	private static final String REQUEST_URI_SEPARATOR = "/";

	private static final String REQUEST_URI_QUERY_STRING_SEPARATOR = "?";

    /**
     * Extract entity type from ID
     * @param entityId request ID
     * @return
     */
    public static String extractEntityTypeById(String entityId) throws BadRequestException {

    	if(ValidateUtil.isEmptyData(entityId)) return null;

        String[] splitId = entityId.split(RESOURCE_ID_SEPARATOR);
        if(splitId == null || splitId.length <= RESOURCE_ID_ENTITY_TYPE_INDEX) {
        	throw new BadRequestException(DataCoreUiCode.ErrorCode.INVALID_ENTITY_TYPE, "Not found entityType. entityId=" + entityId);
        }

        return splitId[RESOURCE_ID_ENTITY_TYPE_INDEX];
    }

    /**
     * Extract entity type from ContentType
     * @param contentType Request contentType
     * @return
     */
    public static String extractEntityTypeByContentType(String contentType) throws BadRequestException {

    	if(ValidateUtil.isEmptyData(contentType)) return null;

        String[] splitType = contentType.split(CONTENT_TYPE_SEPARATOR);
        if(splitType == null || splitType.length <= CONTENT_TYPE_ENTITY_TYPE_INDEX) {
        	throw new BadRequestException(DataCoreUiCode.ErrorCode.INVALID_ENTITY_TYPE, "Not found entityType. contentType=" + contentType);
        }

        return splitType[CONTENT_TYPE_ENTITY_TYPE_INDEX];
    }

    /**
     * Extract entity ID from operation and uri
     * @param operation
     * @param uri
     * @return
     */
    public static String extractEntityId(Operation operation, String uri) {

    	// If there is a query string, it is removed.
    	if(uri.contains(REQUEST_URI_QUERY_STRING_SEPARATOR)) {
    		uri = uri.split("\\" + REQUEST_URI_QUERY_STRING_SEPARATOR)[0];
    	}

		switch(operation) {
			// ID must be extracted from CREATE body
			case CREATE_ENTITY: // /entities
				break;
			case UPDATE_ENTITY_ATTRIBUTES: // /entities/{entityId}/attrs
			case APPEND_ENTITY_ATTRIBUTES: // /entities/{entityId}/attrs
			case REPLACE_ENTITY_ATTRIBUTES: // /entities/{entityId}/attrs
			case CREATE_ENTITY_OR_APPEND_ENTITY_ATTRIBUTES: // /entities/{entityId}/attrs
			case CREATE_ENTITY_OR_REPLACE_ENTITY_ATTRIBUTES: // /entities/{entityId}/attrs
			case PARTIAL_ATTRIBUTE_UPDATE: // /entities/{entityId}/attrs/{attrId}
			case DELETE_ENTITY: // /entities/{entityId}
			case DELETE_ENTITY_ATTRIBUTES: // /entities/{entityId}/attrs/{attrId}
				String[] uriArr = uri.split(REQUEST_URI_SEPARATOR);
				if(uriArr != null && uriArr.length >= 3) {
					return uriArr[2];
				}
			default:
				break;
			}
		return null;
    }

    /**
     * Extract attribute ID from operation and uri
     * @param operation
     * @param uri
     * @return
     */
    public static String extractAttrId(Operation operation, String uri) {
    	if(uri == null) return null;

    	// If there is a query string, it is removed.
    	if(uri.contains(REQUEST_URI_QUERY_STRING_SEPARATOR)) {
    		uri = uri.split("\\" + REQUEST_URI_QUERY_STRING_SEPARATOR)[0];
    	}

		switch(operation) {
			case CREATE_ENTITY: // /entities
				break;
			case UPDATE_ENTITY_ATTRIBUTES: // /entities/{entityId}/attrs
				break;
			case APPEND_ENTITY_ATTRIBUTES: // /entities/{entityId}/attrs
				break;
			case REPLACE_ENTITY_ATTRIBUTES: // /entities/{entityId}/attrs
				break;
			case CREATE_ENTITY_OR_APPEND_ENTITY_ATTRIBUTES: // /entities/{entityId}/attrs
				break;
			case CREATE_ENTITY_OR_REPLACE_ENTITY_ATTRIBUTES: // /entities/{entityId}/attrs
				break;
			case DELETE_ENTITY: // /entities/{entityId}
				break;
			case PARTIAL_ATTRIBUTE_UPDATE: // /entities/{entityId}/attrs/{attrId}
			case DELETE_ENTITY_ATTRIBUTES: // /entities/{entityId}/attrs/{attrId}
				String[] uriArr = uri.split(REQUEST_URI_SEPARATOR, 5);
				if(uriArr != null && uriArr.length > 4) {
					return uriArr[4];
				}
			default:
				break;
			}
		return null;
    }
}