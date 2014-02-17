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
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.servlet.http.HttpServletRequest;

import kr.cipher.seed.Seed128Cipher;

import org.apache.commons.codec.binary.Hex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class TestClientModify {

	private final static Logger logger = Logger.getLogger(TestClientModify.class.getName());
	public String jsoupModify(String html, String policy, HttpServletRequest req)
			throws Exception {
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
			logger.info("PID:" + pid);
			// txid+uuid
			String txid = uuid.toString();
			txid = pid + "-" + txid;
			// policy

			// 대칭키 생성

			byte[] symmeTricKey = null;
			// 임시코드
			// npaaplus4web.dll,npmactest.dll
			String dllList = null;
			if (req.getAttribute("dllList") != null) {
				dllList = (String) req.getAttribute("dllList");
			}

			String orgMsg = "{\"TXID\": \"" + txid + "\", \"uPolicy\": \""
					+ policy + "\",\"dll\":[" + dllList + "]}";
			encMsgBlock = "{\"TXID\": \"" + txid + "\", \"uPolicy\": \""
					+ policy + "\",\"dll\":[" + dllList + "]}";
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			generator.init(128, random);
			Key secureKey = generator.generateKey();

			symmeTricKey = secureKey.getEncoded();
			// 임시코드
			// strSymmeTricKey
			String strSymmeTricKey1 = Hex.encodeHexString(symmeTricKey);
			String strSymmeTricKey = Hex.encodeHexString(symmeTricKey);
			logger.info("encMsgBlock Message:" + encMsgBlock);
			logger.info("strSymmeTricKey:" + strSymmeTricKey);

			// 대칭키를 이용해서 msg 를 암호화(Seed)
			// 임시코드
			String seedEncMsg = Seed128Cipher.encrypt(encMsgBlock,
					symmeTricKey, null);
			encMsgBlock = Seed128Cipher
					.encrypt(encMsgBlock, symmeTricKey, null);
			logger.info("SEEDEncMessage:" + encMsgBlock);

			// 공개키 얻어오기(client)

			fileInputStream = new FileInputStream("/home/AgentPubKey.der");
			bufferedInputStream = new BufferedInputStream(fileInputStream);

			CertificateFactory cf = CertificateFactory.getInstance("X.509");

			Certificate cert = cf.generateCertificate(bufferedInputStream);

			PublicKey publicKey = cert.getPublicKey();

			byte[] publicKeyByte = publicKey.getEncoded();

			String publicKeyStr = Hex.encodeHexString(publicKeyByte);
			logger.info("publicKeyStr:" + publicKeyStr);

			// 공개키로 대칭키를 암호화
			Cipher clsCipher = Cipher.getInstance("RSA");
			clsCipher.init(Cipher.ENCRYPT_MODE, publicKey);// 공개키
			byte[] arrCipherData = clsCipher.doFinal(symmeTricKey);// 대칭키
			encKeyBlock = Hex.encodeHexString(arrCipherData);
			logger.info("encKeyBlock:" + encKeyBlock);
			logger.info("encMsgBlock:" + encMsgBlock);
			// 임시코드
			logger.info("orgMsg:" + orgMsg);
			logger.info("strSymmeTricKey1:" + strSymmeTricKey1);
			logger.info("seedEncMsg:" + seedEncMsg);
			doc.head().append(
					"<script> var orgMsg = " + orgMsg + ";" + "</script>");// 원본메세지

			doc.head().append(
					"<script> var strSymmeTricKey1 = \"" + strSymmeTricKey1
							+ "\";" + "</script>");// 대칭키
			doc.head().append(
					"<script> var seedEncMsg = \"" + seedEncMsg + "\";"
							+ "</script>");// 인크립트 메세지

			doc.head().append(
					"<script> var EncKeyBlock = \"" + encKeyBlock + "\";"
							+ "</script>");
			doc.head().append(
					"<script> var EncMsgBlock = \"" + encMsgBlock + "\";"
							+ "</script>");

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