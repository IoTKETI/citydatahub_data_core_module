package kr.re.keti.sc.dataservicebroker.common.serialize;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class ContentDeserializer extends JsonDeserializer<String> {

	@Override
	public String deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
		TreeNode tree = jsonParser.getCodec().readTree(jsonParser);
		return tree.toString();
	}
}
