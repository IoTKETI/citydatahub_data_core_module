package kr.re.keti.sc.datacoreusertool.util;

import java.nio.ByteBuffer;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kr.re.keti.sc.datacoreusertool.common.code.Constants;

/**
 * Utility for convert type
 * @FileName ConvertUtil.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
public class ConvertUtil {
	
	private static Gson gson = new Gson();
	private static final Gson ngsiLdGson = new GsonBuilder().setDateFormat(Constants.CONTENT_DATE_FORMAT).create();

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
	
	/**
	 * Get NGSI-LD Gson
	 * @return Gson object
	 */
	public static Gson getNgsiLdGson() {
		return ngsiLdGson;
	}
}