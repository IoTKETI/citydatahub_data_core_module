package kr.re.keti.sc.ingestinterface.common.serialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Ingest request content deserializer 클래스
 */
public class ContentDeserializer extends JsonDeserializer<List<String>> {

	@Override
	public List<String> deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
		TreeNode tree = jsonParser.getCodec().readTree(jsonParser);
		if(tree.isArray()) {
			List<String> strList = new ArrayList<>();
			for(int i=0; i<tree.size(); i++) {
				strList.add(tree.get(i).toString());
			}
		    return strList;
		}
		return null;
	}
}
