package kr.co.adflow.aap4web;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import org.junit.Test;

public class CerFileRead_Test {

	@Test
	public void test() throws CertificateException, IOException {
		FileInputStream fis = new FileInputStream("/home/adf.cer");
		BufferedInputStream bis = new BufferedInputStream(fis);

		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		Certificate cert = cf.generateCertificate(bis);

		PublicKey publicKey = cert.getPublicKey();

		byte[] publicKeyByte = publicKey.getEncoded();

		String publicKeyStr = new java.math.BigInteger(publicKeyByte)
				.toString(16);
		System.out.println("publicKeyStr:" + publicKeyStr);

	}
}
