package kr.re.keti.sc.dataservicebroker.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import kr.re.keti.sc.dataservicebroker.common.serialize.ListContentDeserializer;

import java.util.HashMap;
import java.util.List;


public class IngestInterfaceVO {

    private String datasetId;
    @JsonDeserialize(using = ListContentDeserializer.class)
    private List<HashMap<String, Object>> entities;

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public List<HashMap<String, Object>> getEntities() {
        return entities;
    }

    public void setEntities(List<HashMap<String, Object>> entities) {
        this.entities = entities;
    }

    @Override
    public String toString() {
        return "IngestInterfaceVO{" +
                "datasetId='" + datasetId + '\'' +
                ", entities=" + entities +
                '}';
    }
}
