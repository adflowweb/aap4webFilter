package kr.co.adflow.testParser;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.security.Key;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import kr.cipher.seed.Seed128Cipher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClientModify {

	Logger logger = LoggerFactory.getLogger(TestClientModify.class);

	public String jsoupModify(String html) {
		Document doc = null;
		FileInputStream fileInputStream = null;
		BufferedInputStream bufferedInputStream = null;
		String encMsgBlock = null;
		String encKeyBlock = null;

		try {
			doc = Jsoup.parse(html);

			// uuid create
			UUID uuid = UUID.randomUUID();
			RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
			// split
			String temp = rmxb.getName();
			String[] arrTemp = temp.split("@");
			String pid = arrTemp[0];
			logger.debug("PID:" + pid);
			// txid+uuid
			String txid = uuid.toString();
			txid = pid + "-" + txid;
			// policy
			String policy = "N";

			// 대칭키 생성
			
			byte[] symmeTricKey = null;
			encMsgBlock = "{\"TXID\": \"" + txid + "\", \"uPolicy\": \""
					+ policy + "\" }";
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			generator.init(128, random);
			Key secureKey = generator.generateKey();

			symmeTricKey = secureKey.getEncoded();

			String strSymmeTricKey = new java.math.BigInteger(symmeTricKey)
					.toString(16);
			logger.debug("encMsgBlock Message:" + encMsgBlock);
			logger.debug("strSymmeTricKey:" + strSymmeTricKey);

			// 대칭키를 이용해서 msg 를 암호화(Seed)

			encMsgBlock = Seed128Cipher.encrypt(encMsgBlock, symmeTricKey, null);
			logger.debug("SEEDEncMessage:" + encMsgBlock);

			// 공개키 얻어오기(client)
			
			fileInputStream = new FileInputStream("/home/AgentPubKey.der");
			bufferedInputStream = new BufferedInputStream(fileInputStream);

			CertificateFactory cf = CertificateFactory.getInstance("X.509");

			Certificate cert = cf.generateCertificate(bufferedInputStream);

			PublicKey publicKey = cert.getPublicKey();

			byte[] publicKeyByte = publicKey.getEncoded();

			String publicKeyStr = new java.math.BigInteger(publicKeyByte)
					.toString(16);
			logger.debug("publicKeyStr:" + publicKeyStr);

			// 공개키로 대칭키를 암호화
			Cipher clsCipher = Cipher.getInstance("RSA");
			clsCipher.init(Cipher.ENCRYPT_MODE, publicKey);// 공개키
			byte[] arrCipherData = clsCipher.doFinal(symmeTricKey);// 대칭키
			encKeyBlock = new java.math.BigInteger(arrCipherData).toString(16);
			logger.debug("encKeyBlock:" + encKeyBlock);
			logger.debug("encMsgBlock:" + encMsgBlock);
			doc.head().append(
					"<script> var EncKeyBlock =" + encKeyBlock + "</script>");
			doc.head().append(
					"<script> var EngMsgBlock =" + encMsgBlock + "</script>");

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (Exception e) {

				}
			}

			if (bufferedInputStream != null) {
				try {
					bufferedInputStream.close();
				} catch (Exception e) {

				}
			}

		}
		return doc.html();
	}

}