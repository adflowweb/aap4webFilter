package kr.co.adflow.aap4web;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import kr.cipher.seed.Seed128Cipher;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

public class PKI_Test {

/*	private static byte[] privateKey;
	private static String encMsgBlock = "policy is M";


	@Test
	// 1.private Key Generate
	public void privateKeyGenerated() {
		System.out
		.println("########SEED Encryption by private Key Start###########");
		try {
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			generator.init(128, random);
			Key secureKey = generator.generateKey();

			privateKey = secureKey.getEncoded();

			String pirvateKey = Hex.encodeHexString(privateKey);
			System.out.println("ORG Message:"+encMsgBlock);
			System.out.println("pirvateKey:" + pirvateKey);
			
			seedEncryption(privateKey);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 2.message  Encryption by SEED
	public void seedEncryption(byte[] privateKey) {

		try {
			// enc
			encMsgBlock = Seed128Cipher.encrypt(encMsgBlock, privateKey, null);
			System.out.println("SEEDEncMessage:" + encMsgBlock);
		

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Test
	// 3.RSA Key pair Generated
	public void rsaKeyGenerate() {
		System.out.println("########RSA Key pair Generated Start###########");
		try {
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
		
			privateKeyEncryption(privateKey, clsPublicKey, clsPrivateKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 4.private Key encryption by RSA public Key

	public void privateKeyEncryption(byte[] privateKey, Key clsPublicKey,
			Key clsPrivateKey) {
		System.out.println("########RSA Enc Dec  Start###########");
		try {

			// private Key encryption

			String pirvateKey = Hex.encodeHexString(privateKey);//대칭키
			System.out.println("pirvateKey:" + pirvateKey);

			Cipher clsCipher = Cipher.getInstance("RSA");
			clsCipher.init(Cipher.ENCRYPT_MODE, clsPublicKey);
			byte[] arrCipherData = clsCipher.doFinal(privateKey);//대칭커/
			String strCipher = Hex.encodeHexString(arrCipherData);
			System.out.println("EncryptionKey:"+ strCipher);

			// private Key decryption
			clsCipher.init(Cipher.DECRYPT_MODE, clsPrivateKey);
			byte[] arrData = clsCipher.doFinal(arrCipherData);

			String strResult = Hex.encodeHexString(arrData);
			System.out.println("DecryptionKey:"+strResult);
			
			
			// dec

			encMsgBlock = Seed128Cipher.decrypt(encMsgBlock, privateKey, null);
			System.out.println("DecMessage:" + encMsgBlock);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

}
