package kr.re.keti.sc.datamanager.common.exception;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ErrorCode;

/**
 * NoExistTypeException 에러 정의 클래스
 */
public class NoExistTypeException extends BaseException {

    private static final long serialVersionUID = 459647261102179280L;

    public NoExistTypeException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NoExistTypeException(ErrorCode errorCode, String msg) {
        super(errorCode, msg);
        this.errorCode = errorCode;
    }

    public NoExistTypeException(ErrorCode errorCode, Throwable throwable) {
        super(errorCode, throwable);
        this.errorCode = errorCode;
    }

    public NoExistTypeException(ErrorCode errorCode, String msg, Throwable throwable) {
        super(errorCode, msg, throwable);
        this.errorCode = errorCode;
    }
}