package kr.re.keti.sc.ingestinterface.common.vo;


import java.util.List;

/**
 * Ngsi-ld spec batch operation result VO Class
 */
public class BatchOperationResultVO {

    List<String> success;
    List<BatchEntityErrorVO> errors;

    public List<String> getSuccess() {
        return success;
    }

    public void setSuccess(List<String> success) {
        this.success = success;
    }


    public List<BatchEntityErrorVO> getErrors() {
        return errors;
    }

    public void setErrors(List<BatchEntityErrorVO> errors) {
        this.errors = errors;
    }
}
