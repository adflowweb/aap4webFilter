package kr.co.adflow.aap4web;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import org.junit.Test;

public class KeyStore_Test {

	@Test
	public void keyStoreTest() {
		FileInputStream is = null;
		try {
			String passwd="123456";
			String alias = "adf";
			is = new FileInputStream("/home/adf.keystore");

			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, passwd.toCharArray());

			
			Key key = keystore.getKey(alias, passwd.toCharArray());
			if (key instanceof PrivateKey) {
				// Get certificate of public key
			
				Certificate cert = keystore.getCertificate(alias);

				byte[] privateKeyByte = key.getEncoded();
				String privateKeyStr = this.byteArrayToHex(privateKeyByte);
				// Get public key
				PublicKey publicKey = cert.getPublicKey();

				byte[] publicKeyByte = publicKey.getEncoded();
				
				

				String publicKeyStr = this.byteArrayToHex(privateKeyByte);
				System.out.println("privateKeyStr" + privateKeyStr);
				System.out.println("publicKeyStr:" + publicKeyStr);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String byteArrayToHex(byte[] ba) {

	    if (ba == null || ba.length == 0) {

	        return null;

	    }

	    StringBuffer sb = new StringBuffer(ba.length * 2);

	    String hexNumber;

	    for (int x = 0; x < ba.length; x++) {

	        hexNumber = "0" + Integer.toHexString(0xff & ba[x]);

	        sb.append(hexNumber.substring(hexNumber.length() - 2));

	    }

	    return sb.toString();

	} 
	
	
}
