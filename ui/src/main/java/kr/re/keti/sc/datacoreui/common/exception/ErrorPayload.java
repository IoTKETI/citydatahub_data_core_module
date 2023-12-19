package kr.re.keti.sc.datacoreui.common.exception;

import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode;
import kr.re.keti.sc.datacoreui.common.vo.CommonEntityVO;

/**
 * Error payload class
 * @FileName ErrorPayload.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
public class ErrorPayload extends CommonEntityVO {

	/**
	 * Get error type
	 * @return	Error type
	 */
    public String getType() {
        return (String) super.get(DataCoreUiCode.DefaultErrorKey.TYPE.getCode());
    }
    
    /**
     * Set error type
     * @param type	Error type
     */
    public void setType(String type) {
        super.put(DataCoreUiCode.DefaultErrorKey.TYPE.getCode(), type);
    }

    /**
     * Get error title
     * @return	Error title
     */
    public String getTitle() {
        return (String) super.get(DataCoreUiCode.DefaultErrorKey.TITLE.getCode());
    }

    /**
     * Set error title
     * @param title		Error title
     */
    public void setTitle(String title) {
        super.put(DataCoreUiCode.DefaultErrorKey.TITLE.getCode(), title);
    }

    /**
     * Get error detail message
     * @return	Error detail message
     */
    public String getDetail() {
        return (String) super.get(DataCoreUiCode.DefaultErrorKey.DETAIL.getCode());
    }

    /**
     * Set error detail message
     * @param detail	Error detail message
     */
    public void setDetail(String detail) {
        super.put(DataCoreUiCode.DefaultErrorKey.DETAIL.getCode(), detail);
    }

    /**
     * Get debug message
     * @return	Debug message
     */
    public String getDebugMessage() {
        return (String) super.get(DataCoreUiCode.DefaultErrorKey.DEBUG_MESSAGE.getCode());
    }

    /**
     * Set debug message
     * @param debugMessage		Debug message
     */
    public void setDebugMessage(String debugMessage) {
        super.put(DataCoreUiCode.DefaultErrorKey.DEBUG_MESSAGE.getCode(), debugMessage);
    }

    /**
     * Constructor of ErrorPayload class.
     * @param type		Error type
     * @param title		Error title
     * @param detail	Errot detail message
     */
    public ErrorPayload(String type, String title, String detail) {
        this.setType(type);
        this.setTitle(title);
        this.setDetail(detail);
    }

    /**
     * Constructor of ErrorPayload class.
     * @param type			Error type
     * @param title			Error title
     * @param detail		Error detail message
     * @param debugMessage	Debug message
     */
    public ErrorPayload(String type, String title, String detail, String debugMessage) {
        this.setType(type);
        this.setTitle(title);
        this.setDetail(detail);
        this.setDebugMessage(debugMessage);
    }

}
