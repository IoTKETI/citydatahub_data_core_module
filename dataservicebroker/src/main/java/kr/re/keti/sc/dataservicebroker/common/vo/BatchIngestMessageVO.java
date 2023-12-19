package kr.re.keti.sc.dataservicebroker.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchIngestMessageVO {

    List<IngestMessageVO> ingestMessageVO;
    List<BatchEntityErrorVO> batchEntityErrorVO;

    public List<IngestMessageVO> getIngestMessageVO() {
        return ingestMessageVO;
    }

    public void setIngestMessageVO(List<IngestMessageVO> ingestMessageVO) {
        this.ingestMessageVO = ingestMessageVO;
    }

    public List<BatchEntityErrorVO> getBatchEntityErrorVO() {
        return batchEntityErrorVO;
    }

    public void setBatchEntityErrorVO(List<BatchEntityErrorVO> batchEntityErrorVO) {
        this.batchEntityErrorVO = batchEntityErrorVO;
    }
}