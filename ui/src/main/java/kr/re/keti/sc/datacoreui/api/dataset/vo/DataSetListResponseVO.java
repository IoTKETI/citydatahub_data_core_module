package kr.re.keti.sc.datacoreui.api.dataset.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * This is the VO class used when responding to the data set list.
 * @FileName DataSetListResponseVO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 23.
 * @Author Elvin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSetListResponseVO {
	/** Total count */
	private Integer totalCount;
	/** DataSet Response */
	private List<DataSetResponseVO> dataSetResponseVO;
}
