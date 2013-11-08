package kr.co.adflow.aap4web;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import org.junit.Test;

public class RSA_Test {

	@Test
	public void test() {

		System.out.println("test....");

	}

	
	//RSA 2048
	@Test
	public void pkiTest() {

		try {
			// RSA 공개키/개인키를 생성한다.
			KeyPairGenerator clsKeyPairGenerator = KeyPairGenerator
					.getInstance("RSA");
			clsKeyPairGenerator.initialize(2048);

			KeyPair clsKeyPair = clsKeyPairGenerator.genKeyPair();
			Key clsPublicKey = clsKeyPair.getPublic();
			Key clsPrivateKey = clsKeyPair.getPrivate();
			KeyFactory fact = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec clsPublicKeySpec = fact.getKeySpec(clsPublicKey,
					RSAPublicKeySpec.class);
			RSAPrivateKeySpec clsPrivateKeySpec = fact.getKeySpec(
					clsPrivateKey, RSAPrivateKeySpec.class);
			System.out.println("public key modulus("
					+ clsPublicKeySpec.getModulus() + ") exponent("
					+ clsPublicKeySpec.getPublicExponent() + ")");
			System.out.println("private key modulus("
					+ clsPrivateKeySpec.getModulus() + ") exponent("
					+ clsPrivateKeySpec.getPrivateExponent() + ")");

			// 암호화 한다.
			String strPinNumber = "1234567890";

			Cipher clsCipher = Cipher.getInstance("RSA");
			clsCipher.init(Cipher.ENCRYPT_MODE, clsPublicKey);
			byte[] arrCipherData = clsCipher.doFinal(strPinNumber.getBytes());
			String strCipher = new String(arrCipherData);
			System.out.println("cipher(" + strCipher + ")");

			// 복호화 한다.
			clsCipher.init(Cipher.DECRYPT_MODE, clsPrivateKey);
			byte[] arrData = clsCipher.doFinal(arrCipherData);

			String strResult = new String(arrData);
			System.out.println("result(" + strResult + ")");
		} catch (Exception e) {

		}

	}

}
