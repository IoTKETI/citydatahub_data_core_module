package kr.re.keti.sc.dataservicebroker.common.vo;


import lombok.Data;

import java.util.List;

@Data
public class DbConditionVO {

    private String selectCondition;
    private String tableName;
    private String histTableName;
    private String geoCondition;
    private String queryCondition;
    private String timerelCondition;
    private String id;
    private String type;
    private String datasetId;
    private String idPattern;
    private List<String> searchIdList;
    private List<String> searchTypeList;
    private List<String> searchQparamList;

    private List<String> contextList;
    private List<String> watchAttributeList;

    private Integer limit;
    private Integer offset;
    private String aclDatasetCondition;

    @Override
    public String toString() {
        return "DbConditionVO{" +
                "selectCondition='" + selectCondition + '\'' +
                ", tableName='" + tableName + '\'' +
                ", histTableName='" + histTableName + '\'' +
                ", geoCondition='" + geoCondition + '\'' +
                ", queryCondition='" + queryCondition + '\'' +
                ", timerelCondition='" + timerelCondition + '\'' +
                ", id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", datasetId='" + datasetId + '\'' +
                ", searchIdList=" + searchIdList +
                ", idPattern='" + idPattern + '\'' +
                ", contextList=" + contextList + '\'' +
                ", watchAttributeList=" + watchAttributeList + '\'' +
                ", limit=" + limit + '\'' +
                ", offset=" + offset + '\'' +
                ", aclDatasetCondition=" + aclDatasetCondition +
                '}';
    }
}
