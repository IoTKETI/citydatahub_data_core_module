package kr.re.keti.sc.dataservicebroker.jsonldcontext.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.jsonldcontext.service.JsonldContextSVC;
import kr.re.keti.sc.dataservicebroker.jsonldcontext.vo.JsonldContextApiVO;
import kr.re.keti.sc.dataservicebroker.jsonldcontext.vo.JsonldContextBaseVO;
import kr.re.keti.sc.dataservicebroker.subscription.vo.SubscriptionVO;
import kr.re.keti.sc.dataservicebroker.util.HttpHeadersUtil;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@RestController
@Slf4j
public class JsonldContextController {

    @Autowired
    private JsonldContextSVC jsonldContextSVC;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping(value = "/jsonldContexts")
    public @ResponseBody
    void listJsonldContexts(HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam(value = "details", required = false) String details,
                            @RequestParam(value = "kind", required = false) String kind ) throws Exception {

        log.info("Get list jsonldContexts. details={}, kind={}", details, kind);

        if(!ValidateUtil.isEmptyData(details)
                && !details.toLowerCase().equals("true")
                && !details.toLowerCase().equals("false")) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "Invalid Parameter. details=" + details);
        }

        if(!ValidateUtil.isEmptyData(kind)
                && DataServiceBrokerCode.JsonldContextKind.parseType(kind) == null) {
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "Invalid Parameter. kind=" + kind);
        }

        List<JsonldContextBaseVO> jsonldContextBaseVOs = jsonldContextSVC.getJsonldContextList(
                new Date(), DataServiceBrokerCode.JsonldContextKind.parseType(kind));

        if(jsonldContextBaseVOs != null && jsonldContextBaseVOs.size() > 0) {
            if(details != null && Boolean.parseBoolean(details)) {
                List<JsonldContextApiVO> resultList = new ArrayList<>();
                for(JsonldContextBaseVO jsonldContextBaseVO : jsonldContextBaseVOs) {
                    JsonldContextApiVO jsonldContextApiVO = new JsonldContextApiVO();
                    jsonldContextApiVO.setUrl(jsonldContextBaseVO.getUrl());
                    jsonldContextApiVO.setKind(jsonldContextBaseVO.getKind());
                    jsonldContextApiVO.setTimestamp(jsonldContextBaseVO.getModifyDatetime());
                    resultList.add(jsonldContextApiVO);
                }
                response.getWriter().print(objectMapper.writeValueAsString(resultList));
            } else {
                List<String> resultList = new ArrayList<>(jsonldContextBaseVOs.size());
                for(JsonldContextBaseVO jsonldContextBaseVO : jsonldContextBaseVOs) {
                    resultList.add(jsonldContextBaseVO.getUrl());
                }
                response.getWriter().print(objectMapper.writeValueAsString(resultList));
            }
        }
    }
}
