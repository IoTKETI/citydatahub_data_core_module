package kr.re.keti.sc.ingestinterface.common.vo;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kr.re.keti.sc.ingestinterface.common.serialize.ContentDeserializer;

/**
 * Ingest Interface reqeust VO class
 */
public class IngestInterfaceVO {

    private String datasetId;
    @JsonDeserialize(using = ContentDeserializer.class)
    private List<String> entities;

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public List<String> getEntities() {
		return entities;
	}

	public void setEntities(List<String> entities) {
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
