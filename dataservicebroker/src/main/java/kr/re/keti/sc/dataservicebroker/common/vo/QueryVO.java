package kr.re.keti.sc.dataservicebroker.common.vo;

import java.io.Serializable;
import java.util.List;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import lombok.Data;
import org.springframework.web.bind.annotation.ModelAttribute;

@Data
public class QueryVO implements Serializable {
    private static final long serialVersionUID = -1550185165626007488L;

    /*

        조회 :
        - entity 조회
        - temporal entity 조회

        example : GET /ngsi-ld/entities/?type=Vehicle&q=brandName!=Mercedes&options=keyValues
        option은 response 옵션을 뜻함
         - keyValues : Simplified representation
         - sysAttrs
         - temporalValues
     */

    private String dataStorageType;
    private String id;
    private String datasetId;
    private String dataModelId;
    private String type;
    private String q;
    private String timeAt;
    private String endTimeAt;
    private String options;
    private String idPattern;

    private List<String> searchIdList;

    private Integer limit;
    private Integer offset;

    private String timerel;
    private List<String> attrs;

    private String georel;
    private String geometry;
    private String coordinates;
    private String geoproperty;

    private DataServiceBrokerCode.GeometryType georelType;
    private Integer maxDistance;
    private Integer minDistance;

    private String timeQuery;
    private String query;

    private String timeproperty;

    private String geometryValue;
    private String locationCol;
    private Integer lastN;

    private String historyTableName;
    private String selectQuery;
    private List<String> context;

    private DataModelCacheVO dataModelCacheVO;
    private List<String> aclDatasetIds;

    private List<String> links;

    private List<String> cSourceRegistrationIds;
    private List<String> alreadyTraversedCSourceIds;

    @ModelAttribute("endtimeAt")
    public void setEndtimeAt(String endtimeAt) {
        // endtimeAt 명칭 하위호환을 위한 메소드
        if(this.endTimeAt == null) {
            this.endTimeAt = endtimeAt;
        }
    }

    @ModelAttribute("endTimeAt")
    public void setEndTimeAt(String endTimeAt) {
        this.endTimeAt = endTimeAt;
    }

    @Override
    public String toString() {
        return "QueryVO{" +
                "id='" + id + '\'' +
                ", datasetId='" + datasetId + '\'' +
                ", dataModelId='" + dataModelId + '\'' +
                ", type='" + type + '\'' +
                ", q='" + q + '\'' +
                ", timeAt='" + timeAt + '\'' +
                ", endTimeAt='" + endTimeAt + '\'' +
                ", options='" + options + '\'' +
                ", idPattern='" + idPattern + '\'' +
                ", searchIdList=" + searchIdList +
                ", limit=" + limit +
                ", offset=" + offset +
                ", timerel='" + timerel + '\'' +
                ", attrs=" + attrs +
                ", georel='" + georel + '\'' +
                ", geometry='" + geometry + '\'' +
                ", coordinates='" + coordinates + '\'' +
                ", geoproperty='" + geoproperty + '\'' +
                ", maxDistance=" + maxDistance +
                ", minDistance=" + minDistance +
                ", timeQuery='" + timeQuery + '\'' +
                ", query='" + query + '\'' +
                ", timeproperty='" + timeproperty + '\'' +
                ", geometryValue='" + geometryValue + '\'' +
                ", locationCol='" + locationCol + '\'' +
                ", lastN=" + lastN +
                ", historyTableName='" + historyTableName + '\'' +
                ", selectQuery='" + selectQuery + '\'' +
                ", context='" + context + '\'' +
                ", dataModelCacheVO=" + dataModelCacheVO + '\'' +
                ", aclDatasetIds=" + aclDatasetIds +
                ", cSourceRegistrationIds=" + cSourceRegistrationIds +
                ", alreadyTraversedCSourceIds=" + alreadyTraversedCSourceIds +
                '}';
    }

}
