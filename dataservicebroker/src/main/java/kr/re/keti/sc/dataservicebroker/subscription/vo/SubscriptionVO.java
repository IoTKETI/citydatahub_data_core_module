package kr.re.keti.sc.dataservicebroker.subscription.vo;

import java.util.*;

import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.SubscriptionCode;
import kr.re.keti.sc.dataservicebroker.common.code.SubscriptionCode.Timerel;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.vo.TermExpandable;
import lombok.Data;
import org.springframework.web.bind.annotation.ModelAttribute;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SubscriptionVO implements TermExpandable {

    private String id;
    private String type;
    private String name;
    private String description;
    private List<EntityInfo> entities;
    private List<String> datasetIds;
    private List<String> watchedAttributes;
    private Integer timeInterval;
    private String q;
    private GeoQuery geoQ;
    private String csf;

    private Boolean isActive;
    private NotificationParams notification;
    private Date expires;
    private Date expiresAt;
    private Integer throttling;
    private TemporalQuery temporalQ;
    private SubscriptionCode.Status status;

    @JsonProperty("@context")
    private List<String> context;

    @ModelAttribute("expiresAt")
    public void setExpiresAt(Date expiresAt) {
        // expires -> expiresAt으로 spec 변경 대응
        // 실제 내부 로직은 expires 로 동작함
        this.expires = expiresAt;
    }

    public void setStatus(String status) {

        SubscriptionCode.Status statusObj = SubscriptionCode.Status.parseType(status);

        if (statusObj == null) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "check status = " + status);
        }
        this.status = statusObj;
    }

    public void setStatus(SubscriptionCode.Status status) {
		this.status = status;
	}

	// ETSI GS CIM 009 - 5.2.8 EntityInfo
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntityInfo {
        private String id;
        private String idPattern;
        private String type;
        @JsonIgnore
        private String typeUri;
    }

    // ETSI GS CIM 009 - 5.2.13 GeoQuery
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoQuery {
        private String geometry;
        private Object coordinates;
        private String georel;
        private String geoproperty;
    }

    // ETSI GS CIM 009 - 5.2.14 NotificationParams
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NotificationParams {
        private List<String> attributes;
        private String format;
        private Endpoint endpoint;

        // ETSI GS CIM 009 - 5.2.15 Endpoint
        @Data
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Endpoint {
            private String uri;
            private String accept;
            private List<KeyValuePair> receiverInfo;
            private List<KeyValuePair> notifierInfo;
        }

        @Data
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class KeyValuePair {
            private String key;
            private String value;
        }
    }

    // ETSI GS CIM 009 - 5.2.21 TemporalQuery
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TemporalQuery {
        private SubscriptionCode.Timerel timerel;
        @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
        private Date time;
        @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
        private Date endTime;
        private String timeproperty;

        public void setTimerel(Timerel timerel) {
            this.timerel = timerel;
        }

        public void setTimerel(String timerel) {

            Timerel timerelObj = Timerel.parseType(timerel);

            if (timerelObj == null) {
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "check timerel = " + timerel);
            }
            this.timerel = timerelObj;
        }
    }

    @Override
    public void expandTerm(Map<String, String> dataModelContextMap) {
        expandTerm(null, dataModelContextMap);
    }

    @Override
    public void expandTerm(Map<String, String> requestContextMap, Map<String, String> dataModelContextMap) {

        if (requestContextMap == null) requestContextMap = new HashMap<>();

        expandKey(requestContextMap, dataModelContextMap);
        expandValue(requestContextMap, dataModelContextMap);
    }

    private void expandKey(Map<String, String> requestContextMap, Map<String, String> dataModelContextMap) {
        watchedAttributes = expandAttributes(watchedAttributes, requestContextMap, dataModelContextMap);

        if (notification != null) {
            notification.setAttributes(
                    expandAttributes(
                            notification.getAttributes(),
                            requestContextMap,
                            dataModelContextMap
                    )
            );
        }
    }

    private void expandValue(Map<String, String> requestContextMap, Map<String, String> dataModelContextMap) {
        if (ValidateUtil.isEmptyData(entities)) {
            return;
        }

        for (EntityInfo entityInfo : entities) {
            if (isExpansionTarget(
                    requestContextMap == null ? null : requestContextMap.get(entityInfo.getType()),
                    entityInfo.getTypeUri())
            ) {
                entityInfo.setType(entityInfo.getTypeUri());
            }
        }
    }

    private List<String> expandAttributes(
            List<String> attributes,
            Map<String, String> requestContextMap,
            Map<String, String> dataModelContextMap
    ) {

        if (ValidateUtil.isEmptyData(attributes)) {
            return attributes;
        }

        List<String> expandedAttributes = new ArrayList<>();
        for (String attributeName : attributes) {
            String fullUriByRequest = requestContextMap.get(attributeName);
            String fullUriByDataModel = null;
            if (dataModelContextMap != null) {
                fullUriByDataModel = dataModelContextMap.get(attributeName);
            }
            if (isFullUri(attributeName)
                || !isExpansionTarget(fullUriByRequest, fullUriByDataModel)) {
                expandedAttributes.add(attributeName);
            } else{
                expandedAttributes.add(fullUriByDataModel);
            }
        }
        return expandedAttributes;
    }

    private boolean isExpansionTarget(String fullUriByRequest, String fullUriByDataModel) {

        // 요청 context 기반으로 full uri 파싱이 불가능한 경우
        if (ValidateUtil.isEmptyData(fullUriByRequest)) {
            return true;

        // 요청 context 기반으로 full uri 파싱은 가능하지만, dataModel의 full url와 다른 경우
        } else if (!ValidateUtil.isEmptyData(fullUriByRequest)
                && !ValidateUtil.isEmptyData(fullUriByDataModel)
                && !fullUriByRequest.equals(fullUriByDataModel)) {
            return true;
        }
        return false;
    }

    private boolean isFullUri(String attributeName) {
        return attributeName == null ? false : attributeName.startsWith("http");
    }
}
