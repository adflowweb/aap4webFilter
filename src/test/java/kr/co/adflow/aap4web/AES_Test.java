package kr.co.adflow.aap4web;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
 
public class AES_Test {
 
  private static String sKeyString = "" ;
  private static String message= "This is just an example";
 
  @Test
  public void testAESEncode() throws Exception {
 
    /*// 1. 128 비트 비밀키 생성
    KeyGenerator kgen = KeyGenerator.getInstance("AES");
    kgen.init(128);
    SecretKey skey = kgen.generateKey();
 
    // 2. 비밀 키를 이렇게 저장하여 사용하면 암호화/복호화가 편해진다.
    sKeyString = Hex.encodeHexString(skey.getEncoded());
 
    // 3. 암호화 수행
    SecretKeySpec skeySpec = new SecretKeySpec(skey.getEncoded(), "AES");
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
    byte[] encrypted = cipher.doFinal(message.getBytes());
 
    System.out.println("encrypted string: " + Hex.encodeHexString(encrypted));
 
    // 4. 복호화 수행
    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
    byte[] original = cipher.doFinal(encrypted);
    String originalString = new String(original);
    System.out.println("Original string: " + originalString + " " + Hex.encodeHexString(original));
 */
  }
}