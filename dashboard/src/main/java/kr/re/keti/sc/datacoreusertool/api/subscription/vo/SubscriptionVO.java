package kr.re.keti.sc.datacoreusertool.api.subscription.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.datacoreusertool.common.code.Constants;
import kr.re.keti.sc.datacoreusertool.common.code.SubscriptionCode;

/**
 * SubscriptionVO class
 * @FileName SubscriptionVO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionVO {

    private String id;
    private String type;
    private String name;
    private List<EntityInfo> entities;
    private List<String> watchedAttributes;
    private Integer timeInterval;

    private Boolean isActive;
    private NotificationParams notification;
    @JsonFormat(pattern = Constants.CONTENT_DATE_FORMAT)
    private Date expires;
    private Integer throttling;
    private SubscriptionCode.Status status;
    private List<String> context;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EntityInfo> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityInfo> entities) {
        this.entities = entities;
    }

    public List<String> getWatchedAttributes() {
        return watchedAttributes;
    }

    public void setWatchedAttributes(List<String> watchedAttributes) {
        this.watchedAttributes = watchedAttributes;
    }

    public Integer getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(Integer timeInterval) {
        this.timeInterval = timeInterval;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public NotificationParams getNotification() {
        return notification;
    }

    public void setNotification(NotificationParams notification) {
        this.notification = notification;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Integer getThrottling() {
        return throttling;
    }

    public void setThrottling(Integer throttling) {
        this.throttling = throttling;
    }

    public SubscriptionCode.Status getStatus() {
        return status;
    }

    public void setStatus(SubscriptionCode.Status status) {
        this.status = status;
    }

    public void setStatus(String status) {
        SubscriptionCode.Status statusObj = SubscriptionCode.Status.parseType(status);
        this.status = statusObj;
    }

    public List<String> getContext() {
		return context;
	}

	public void setContext(List<String> context) {
		this.context = context;
	}



	// ETSI GS CIM 009 - 5.2.8 EntityInfo
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntityInfo {

        private String id;
        private String idPattern;
        private String type;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getIdPattern() {
            return idPattern;
        }

        public void setIdPattern(String idPattern) {
            this.idPattern = idPattern;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    // ETSI GS CIM 009 - 5.2.13 GeoQuery
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoQuery {
        private String geometry;
        private Object coordinates;
        private String georel;
        private String geoproperty;

        public String getGeometry() {
            return geometry;
        }

        public void setGeometry(String geometry) {
            this.geometry = geometry;
        }

        public Object getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(Object coordinates) {
            this.coordinates = coordinates;
        }

        public String getGeorel() {
            return georel;
        }

        public void setGeorel(String georel) {
            this.georel = georel;
        }

        public String getGeoproperty() {
            return geoproperty;
        }

        public void setGeoproperty(String geoproperty) {
            this.geoproperty = geoproperty;
        }
    }

    // ETSI GS CIM 009 - 5.2.14 NotificationParams
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NotificationParams {
        private List<String> attributes;
        private String format;
        private Endpoint endpoint;

        public List<String> getAttributes() {
            return attributes;
        }

        public void setAttributes(List<String> attributes) {
            this.attributes = attributes;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public Endpoint getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(Endpoint endpoint) {
            this.endpoint = endpoint;
        }


        // ETSI GS CIM 009 - 5.2.15 Endpoint
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Endpoint {
            private String uri;
            private String accept;

            public String getUri() {
                return uri;
            }

            public void setUri(String uri) {
                this.uri = uri;
            }

            public String getAccept() {
                return accept;
            }

            public void setAccept(String accept) {
                this.accept = accept;
            }
        }
    }

}
