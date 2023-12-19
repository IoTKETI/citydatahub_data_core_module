package kr.re.keti.sc.datacoreui.util;

import java.nio.ByteBuffer;
import java.util.Map;

import com.google.gson.Gson;

/**
 * Utility for convert type
 * @FileName ConvertUtil.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
public class ConvertUtil {
	
	private static Gson gson = new Gson();

	/**
	 * Convert bytes to int
	 * @param value		byte array value
	 * @return			int value
	 */
	public static int bytesToint(byte[] value) {
		return ByteBuffer.wrap(value).getInt();
	}

	/**
	 * Convert int to bytes
	 * @param value		int value
	 * @return			byte array value
	 */
	public static byte[] intTobytes(int value) {
		return ByteBuffer.allocate(4).putInt(value).array();
	}
	
	/**
	 * Convert json to map
	 * @param jsonStr		Json string value
	 * @return				Map type data
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> jsonToMap(String jsonStr) {
		return (Map<String, Object>) gson.fromJson(jsonStr, Map.class);
	}
}