package kr.re.keti.sc.ingestinterface.common.vo;

import lombok.Data;

/**
 * Page domain class
 */
@Data
public class PageRequest {

    Integer offset;
    Integer limit;
}
