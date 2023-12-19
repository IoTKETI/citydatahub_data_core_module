package kr.re.keti.sc.datacoreui.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for crypto
 * @FileName CryptoUtil.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
public class CryptoUtil {
	final static String SHA256 = "SHA-256";
	
	/**
	 * Encrypt the string with SHA256.
	 * @param txt							The string to apply encryption to.
	 * @return								Encrypted string value.
	 * @throws NoSuchAlgorithmException		Throw an exception when a "NoSuchAlgorithm" error occurs.
	 */
	public static String stringToSHA256(String txt) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(SHA256);
		md.update(txt.getBytes());
		return byteToHexString(md.digest());
	}
	
	/**
	 * Encrypt the byte with HexString.
	 * @param data	Byte array data
	 * @return		Hex string
	 */
	private static String byteToHexString(byte[] data) {
	    StringBuilder sb = new StringBuilder();
	    for(byte b : data) {
	        sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));

	    }
	    return sb.toString();
	}
}
