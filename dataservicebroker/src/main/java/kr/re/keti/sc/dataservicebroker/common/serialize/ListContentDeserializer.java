package kr.re.keti.sc.dataservicebroker.common.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ListContentDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
        TreeNode tree = jsonParser.getCodec().readTree(jsonParser);
        if (tree.isArray()) {
            List<String> strList = new ArrayList<>();
            for (int i = 0; i < tree.size(); i++) {
                strList.add(tree.get(i).toString());
            }
            return strList;
        }
        return null;
    }
}
