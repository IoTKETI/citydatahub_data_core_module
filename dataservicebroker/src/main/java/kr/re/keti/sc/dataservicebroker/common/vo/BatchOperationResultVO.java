package kr.re.keti.sc.dataservicebroker.common.vo;


import lombok.Data;

import java.util.List;

@Data
public class BatchOperationResultVO {
    private List<String> success;
    private List<BatchEntityErrorVO> errors;
}
