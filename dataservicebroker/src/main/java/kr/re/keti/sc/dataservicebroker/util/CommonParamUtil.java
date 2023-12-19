package kr.re.keti.sc.dataservicebroker.util;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.Operation;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;

public class CommonParamUtil {

	/** ContentType 파라미터 구분자 */
	private static final String CONTENT_TYPE_SEPARATOR = "=";
	/** Type 위치 */
	private static final int CONTENT_TYPE_ENTITY_TYPE_INDEX = 1;

	/** 리소스아이디 파라미터 구분자 */
	private static final String RESOURCE_ID_SEPARATOR = ":";
	/** entityType 위치 */
	private static final int RESOURCE_ID_ENTITY_TYPE_INDEX = 2;

	private static final String REQUEST_URI_SEPARATOR = "/";

	private static final String REQUEST_URI_QUERY_STRING_SEPARATOR = "?";

    /**
     * ContentType 으로 부터 entity Type 추출
     * @param contentType 요청 contentType
     * @return
     */
    public static String extractEntityTypeByContentType(String contentType) throws NgsiLdBadRequestException {

    	if(ValidateUtil.isEmptyData(contentType)) return null;

        String[] splitType = contentType.split(CONTENT_TYPE_SEPARATOR);
        if(splitType == null || splitType.length <= CONTENT_TYPE_ENTITY_TYPE_INDEX) {
        	throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_ENTITY_TYPE, "Not found entityType. contentType=" + contentType);
        }

        return splitType[CONTENT_TYPE_ENTITY_TYPE_INDEX];
    }

    public static String extractEntityId(Operation operation, String uri) {

    	// query string 이 있는 경우 제거
    	if(uri.contains(REQUEST_URI_QUERY_STRING_SEPARATOR)) {
    		uri = uri.split("\\" + REQUEST_URI_QUERY_STRING_SEPARATOR)[0];
    	}

		switch(operation) {
			// CREATE body에서 id 추출해야함
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

    public static String extractAttrId(Operation operation, String uri) {
    	if(uri == null) return null;

    	// query string 이 있는 경우 제거
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