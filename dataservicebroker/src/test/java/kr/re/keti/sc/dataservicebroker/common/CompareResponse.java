package kr.re.keti.sc.dataservicebroker.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.exception.BadRequestException;

@Component
public class CompareResponse {

    @Autowired
    private ObjectMapper objectMapper;

    public void compareResponseBody(ResultActions resultActions, String responseCompare) throws Exception {
    
        //response body 가져오기
        MvcResult mvcResult = resultActions.andReturn();
        // mvcResult.getResponse().getContentAsString();
        
        HashMap<String, Object> jsonResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),HashMap.class);
        
        HashMap<String, Object> jsonResponseCompare = objectMapper.readValue(responseCompare, HashMap.class);

        //key값의 개수가 같은지 비교
        if (jsonResponse.size() == jsonResponseCompare.size()) {
        
        for(HashMap.Entry<String, Object> entry : jsonResponse.entrySet()) {
            if(jsonResponseCompare.get(entry.getKey()).equals(entry.getValue())){
            continue;
            } else {
            throw new BadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "ResponseBody Not Matched");
            }
                
            }

        
        } else {
        throw new BadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "ResponseBody Not Matched");
        
        }
        
    }

    public void compareListResponseBody(ResultActions resultActions, String responseCompare) throws Exception {

        //response body 가져오기
        MvcResult mvcResult = resultActions.andReturn();

        ArrayList<HashMap<String, Object>> listResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),ArrayList.class);

        ArrayList<HashMap<String, Object>> listResponseCompare = objectMapper.readValue(responseCompare,ArrayList.class);

        for (int i = 0; i < listResponse.size(); i++) {
            if (listResponse.get(i).getClass().getName() == "java.util.LinkedHashMap" && listResponse.size() == listResponseCompare.size()) {
                for (HashMap.Entry<String, Object> entry : listResponse.get(i).entrySet()) {
                    if (listResponseCompare.get(i).get(entry.getKey()).equals(entry.getValue())) {
                        continue;
                    } else {
                        throw new BadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR,
                                "ResponseBody Not Matched");
                    }

                }

            }
        }
    }

    public void comparePostBody(ResultActions resultActions, String responseCompare) throws Exception {
        
        //response body 가져오기
        MvcResult mvcResult = resultActions.andReturn();
        
        ArrayList<String> listResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ArrayList.class);

        ArrayList<String> listResponseCompare = objectMapper.readValue(responseCompare,ArrayList.class);

        //리스트 정렬
        Collections.sort(listResponse);
        Collections.sort(listResponseCompare);
              
        //  정렬한 리스트의 값이 다르면 오류 표출
        if(!Arrays.equals(listResponse.toArray(),listResponseCompare.toArray())) {
            throw new BadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, "ResponseBody Not Matched");
            } 
        
    }
    
}
