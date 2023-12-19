package kr.re.keti.sc.dataservicebroker.util;

import java.nio.ByteBuffer;

public class ConvertUtil {

	public static int bytesToint(byte[] value) {
		return ByteBuffer.wrap(value).getInt();
	}

	public static byte[] intTobytes(int value) {
		return ByteBuffer.allocate(4).putInt(value).array();
	}
}