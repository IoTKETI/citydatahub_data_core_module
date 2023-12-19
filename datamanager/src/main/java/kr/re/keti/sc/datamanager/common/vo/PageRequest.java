package kr.re.keti.sc.datamanager.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Page domain class
 */
public class PageRequest {

	@JsonIgnore
	private Integer offset;
	@JsonIgnore
	private Integer limit;

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
