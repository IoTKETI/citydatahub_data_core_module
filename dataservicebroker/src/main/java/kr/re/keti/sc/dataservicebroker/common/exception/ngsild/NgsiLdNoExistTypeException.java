package kr.re.keti.sc.dataservicebroker.common.exception.ngsild;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.exception.BaseException;

public class NgsiLdNoExistTypeException extends BaseException {

    private static final long serialVersionUID = 459647261102179280L;

    public NgsiLdNoExistTypeException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NgsiLdNoExistTypeException(ErrorCode errorCode, String msg) {
        super(errorCode, msg);
        this.errorCode = errorCode;
    }

    public NgsiLdNoExistTypeException(ErrorCode errorCode, Throwable throwable) {
        super(errorCode, throwable);
        this.errorCode = errorCode;
    }

    public NgsiLdNoExistTypeException(ErrorCode errorCode, String msg, Throwable throwable) {
        super(errorCode, msg, throwable);
        this.errorCode = errorCode;
    }
}