package kr.co.adflow.util;

import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;


public class AESUtil {

	
	
	private final static Logger logger = Logger.getLogger(AESUtil.class.getName());

	private static String privateKeyPass = "123456";
	Cipher cipher = null;
	SecretKeySpec skeySpec = null;

	public byte[] getEncryptPassWord() {

		byte[] encrypted = null;
		try {

			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128);
			SecretKey skey = kgen.generateKey();

			// 3. 암호화 수행
			skeySpec = new SecretKeySpec(skey.getEncoded(), "AES");
			cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			encrypted = cipher.doFinal(privateKeyPass.getBytes());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return encrypted;

	}

	public String keyPassDecryption(byte[] encrypted) {
		byte[] original = null;
		String decPrivatePass = null;
		try {
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			original = cipher.doFinal(encrypted);
			decPrivatePass = new String(original);;
			logger.info("decPrivatePass:" + decPrivatePass);
			logger.info("Original string: " + decPrivatePass + " "
					+ Hex.encodeHexString(original));
			
		   
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decPrivatePass;
	}

}
