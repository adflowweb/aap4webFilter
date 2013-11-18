package kr.co.adflow.filter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.cipher.seed.Seed128Cipher;
import kr.co.adflow.util.AESUtil;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationFilter implements Filter {

	private static String VERIFICATION_SERVER_ADDRESS;
	private static Logger logger = LoggerFactory
			.getLogger(VerificationFilter.class);
	private static HashMap verificationUriList = new HashMap();

	private ExecutorService executorVerifyListGet = Executors
			.newFixedThreadPool(1);
	private ExecutorService executorUnknownListFlush = Executors
			.newFixedThreadPool(1);
	private ObjectMapper mapper = new ObjectMapper();
	private PoolingClientConnectionManager connectionManager = null;
	private DefaultHttpClient client = null;
	private static byte[] encPrivateKeyPass = null;
	private static String decPrivateKeyPass = null;

	// test git
	/**
	 * 검증대상 uri list를 검증서버에서 가져와 초기화 한다.
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.debug("init verificationFilter");
		VERIFICATION_SERVER_ADDRESS = System.getProperty("verificationServer",
				"http://127.0.0.1:3000");
		logger.debug("verification server : " + VERIFICATION_SERVER_ADDRESS);
		// connection Manager Setting..
		// add Setting
		connectionManager = new PoolingClientConnectionManager();
		connectionManager.setMaxTotal(400);
		connectionManager.setDefaultMaxPerRoute(20);
		client = new DefaultHttpClient(connectionManager);
		AESUtil aesUtil = new AESUtil();
		encPrivateKeyPass = aesUtil.getEncryptPassWord();
		decPrivateKeyPass = aesUtil.keyPassDecryption(encPrivateKeyPass);

		logger.debug("encryptedPrivateKey string: "
				+ Hex.encodeHexString(encPrivateKeyPass));
		executorVerifyListGet.execute(new Runnable() {
			public void run() {
				while (true) {
					long start = System.currentTimeMillis();
					URL url;
					HttpURLConnection conn = null;
					BufferedReader rd = null;
					try {
						// create connection
						url = new URL(VERIFICATION_SERVER_ADDRESS
								+ "/v1/policy/uri");
						conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("GET");
						conn.setUseCaches(false);
						conn.setDoInput(true);
						conn.setDoOutput(false);

						logger.debug("request get verification uri list");
						logger.debug("request url : " + conn.getURL());
						int resCode = conn.getResponseCode();
						logger.debug("responseCode : " + resCode);
						if (resCode == 200) {
							// Get Response
							InputStream is = conn.getInputStream();
							rd = new BufferedReader(new InputStreamReader(is));
							String line;
							StringBuffer responseData = new StringBuffer();
							while ((line = rd.readLine()) != null) {
								responseData.append(line);
								responseData.append('\r');
							}
							logger.debug("response : "
									+ responseData.toString());

							// insert verification uri data

							Set set = mapper.readValue(responseData.toString(),
									HashMap.class).keySet();
							Iterator it = set.iterator();

							while (it.hasNext()) {
								String key = (String) it.next();
								Object value = mapper.readValue(
										responseData.toString(), HashMap.class)
										.get(key);
								verificationUriList.put(key, value.toString());
							}

							logger.debug("verificationUriList : "
									+ verificationUriList);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (conn != null) {
							conn.disconnect();
						}
						if (rd != null) {
							try {
								rd.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

					logger.debug("elapsedTime : "
							+ (System.currentTimeMillis() - start) + " ms ");

					// sleep 1분
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		executorUnknownListFlush.execute(new Runnable() {
			public void run() {
				while (true) {
					URL url = null;
					OutputStream out = null;
					HttpURLConnection urlConnection = null;
					BufferedReader in = null;
					String flushStr = null;
					HashMap flushMap = new HashMap();
					ObjectMapper objectMapper = null;

					try {

						url = new URL(
								"http://127.0.0.1:3000/v1/policy/uri/unknown");
						urlConnection = (HttpURLConnection) url
								.openConnection();
						urlConnection.setDoOutput(true);
						urlConnection.setRequestMethod("POST");
						out = urlConnection.getOutputStream();

						Set set = verificationUriList.keySet();
						Iterator it = set.iterator();

						while (it.hasNext()) {
							String key = (String) it.next();
							Object value = verificationUriList.get(key);
							logger.debug("verificationUriList key:" + key);
							logger.debug("verificationUriList value:" + value);
							if (value.toString().equals("U")) {
								logger.debug("flush...value");
								flushMap.put(key, value);
								verificationUriList.put(key, "F");

							}
						}

						if (flushMap.size() > 0) {
							objectMapper = new ObjectMapper();
							flushStr = objectMapper
									.writeValueAsString(flushMap);
							logger.debug("flushStr:" + flushStr);
							byte[] bs = flushStr.toString().getBytes("UTF-8");
							out.write(bs);
							out.flush();
							int resCode = urlConnection.getResponseCode();
							logger.debug("urlConnection resCode:" + resCode);
						}

					} catch (Exception e) {
						e.printStackTrace();
					} finally {

						flushMap = null;
						objectMapper = null;
						if (urlConnection != null) {
							urlConnection.disconnect();
						}
						if (out != null) {
							try {
								out.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

					}

					try {
						Thread.sleep(10000); // 10초
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		});

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		logger.debug("VerificationFilter doFilter....");
		logger.debug("requestURI : " + req.getRequestURI());
		logger.debug("requestMethod : " + req.getMethod());
		logger.debug("contentType : " + req.getContentType());
		logger.debug("req.getRemoteAddr():" + req.getRemoteAddr());
		// parameter
		for (Enumeration e = req.getParameterNames(); e.hasMoreElements();) {
			String param = (String) e.nextElement();
			logger.debug(param + ":" + req.getParameter(param));
		}

		// verification uri check
		// hiddenField(hash) 추가해야함
		// (verificationUriList.containsKey(req.getRequestURI()

		// unknow Url
		if (!verificationUriList.containsKey(req.getRequestURI())) {
			logger.debug("unKnown URI!!");
			logger.debug("req.unKnown URI:" + req.getRequestURI());
			verificationUriList.put(req.getRequestURI(), "U");

			// verifyUrl
		} else {


			logger.debug("Verify Uri req.getRequestURI():"
					+ req.getRequestURI());

			Object obj = null;
			String policyIsV = null;
			obj = (Object) verificationUriList.get(req.getRequestURI());
			logger.debug("obj:" + obj.toString());
			policyIsV = "\"uri_policy\":\"V\"";
			// 검증 대상 V 일경우
			// if (obj.toString().contains(policyIsV)) {
			// EngMsgBlock ,EncKeyBlock
			//
			/*
			 * if(req.getHeader("EngMsgBlock")!=null&&req.getHeader("EncKeyBlock"
			 * )!=null){
			 * 
			 * }
			 */
			try {
				
				Enumeration e = req.getHeaderNames();

				while (e.hasMoreElements()) {
					String temp = (String) e.nextElement();
					
					logger.debug("tempHeader:"+temp);
				}
				// if (req.getHeader("hash") != null) {
				logger.debug("verification Filter Log!!!!!!!!!!!!!!!!!!!Step1..");
				String test1 = req.getHeader("encmsgblock");
				logger.debug("verification Filter Log!!!!!!!!!!!!!!!!!!!Step2..");
				logger.debug("test1:" + test1.toString());
				String test2 = req.getHeader("enckeyblock");
				logger.debug("verification Filter Log!!!!!!!!!!!!!!!!!!!Step3..");
				logger.debug("test2:" + test2.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}

			logger.debug("verification Filter Log!!!!!!!!!!!!!!!!!!!Step4..");
			if (req.getHeader("encmsgblock") != null
					&& req.getHeader("enckeyblock") != null) {

				logger.debug("Client Request Header engMsgBlock is Not Null!!!!!!!!!!!!!!");
				URI uri;
				HttpGet httpGet = null;
				PrintWriter printWriter = null;
				BufferedReader br = null;
				HttpResponse getHttpResponse = null;
				FileInputStream is = null;
				try {
					// create connection

					uri = new URI(VERIFICATION_SERVER_ADDRESS + "/v1/verify/"
							+ req.getSession().getId());
					httpGet = new HttpGet(uri);

					// PID ADD
					RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
					logger.debug("pid: " + rmxb.getName());

					// set header hash
					// req header

					for (Enumeration e = req.getHeaderNames(); e
							.hasMoreElements();) {
						String headerNames = (String) e.nextElement();
						logger.debug(headerNames + ":"
								+ req.getHeader(headerNames));

					}
					logger.debug("DECPrivateKeyPass:" + decPrivateKeyPass);
					// 개인키 pass AES 적용

					String alias = "adf";
					is = new FileInputStream("/home/adf.keystore");
					KeyStore keystore = KeyStore.getInstance(KeyStore
							.getDefaultType());
					keystore.load(is, decPrivateKeyPass.toCharArray());
					Key key = keystore.getKey(alias,
							decPrivateKeyPass.toCharArray());
					if (key instanceof PrivateKey) {
						logger.debug("Private key read!!!!");
					}
					// EncKeyBlock 을 개인키로 decryption!
					String encKeyBlock = req.getHeader("enckeyblock");
					
					
					//byte[] ciperData = encKeyBlock.getBytes();
					
					byte[] ciperData=hexStringToByteArray(encKeyBlock);
					
					Cipher clsCipher = Cipher.getInstance("RSA");
					clsCipher.init(Cipher.DECRYPT_MODE, key);
					byte[] arrData = clsCipher.doFinal(ciperData);
					String decryptionKey = Hex.encodeHexString(arrData);
					logger.debug("DecryptionKey:" + decryptionKey);

					// EngMsgBlock 대칭키로 decryption!
				
					String engMsgBlock = req.getHeader("encmsgblock");

					engMsgBlock = Seed128Cipher.decrypt(engMsgBlock,
							decryptionKey.getBytes(), null);
					logger.debug("DecMessage:" + engMsgBlock);

					// client ip 임시코드
					httpGet.addHeader("clientip", req.getRemoteAddr());

					// txid
					// user-agent

					httpGet.addHeader("filterId", rmxb.getName());
					httpGet.addHeader("hash", req.getHeader("hash"));
					httpGet.addHeader("txid", req.getHeader("txid"));
					httpGet.addHeader("user-agent", req.getHeader("user-agent"));
					httpGet.addHeader("virtual_page_uri", req.getRequestURI());
					// event Header Add
					if (req.getHeader("X-Requested-With") != null) {
						httpGet.addHeader("event", req.getHeader("event"));
					}
					httpGet.setHeader("Connection", "keep-alive");
					logger.debug("request verification");
					logger.debug("req.getHeader(hash):" + req.getHeader("hash"));

					logger.debug("HttpGet : " + httpGet.toString());

					// get Response
					getHttpResponse = client.execute(httpGet);
					int resCode = getHttpResponse.getStatusLine()
							.getStatusCode();

					switch (resCode) {
					case 200: // 검증성공
						logger.debug("verified Success!!!!");

						// todo
						// 검증로그전송
						break;
					case 404:
						logger.debug("404 not found");

						res.sendError(404);// 임시코드
						// break;
						return;
					case 500:
						logger.debug("500 internal server error");

						res.sendError(500);// 임시코드
						// break;
						return;
					case 505: // 검증실패
						logger.debug("Server Error 505");

						br = new BufferedReader(new InputStreamReader(
								getHttpResponse.getEntity().getContent()));
						String line;
						StringBuffer bfResponseData = new StringBuffer();
						while ((line = br.readLine()) != null) {
							bfResponseData.append(line);
							bfResponseData.append('\r');
						}

						logger.debug("bfResponseData:"
								+ bfResponseData.toString());
						res.setStatus(505);
						printWriter = new PrintWriter(res.getOutputStream());
						printWriter.print(bfResponseData);
						printWriter.flush();

						// todo
						// 검증로그전송
						return;
					default:
						logger.debug("undefined responseCode");

						break;
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					EntityUtils.consume(getHttpResponse.getEntity());
					if (printWriter != null) {
						try {
							printWriter.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					if (br != null) {
						try {
							br.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					if (is != null) {
						try {
							is.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		chain.doFilter(req, res);
	}

	public static HashMap getVerificationUriList() {
		return verificationUriList;
	}

	public static void setVerificationUriList(HashMap verificationUriList) {
		verificationUriList = verificationUriList;
	}

	public void destroy() {
		executorVerifyListGet.shutdown();
		executorUnknownListFlush.shutdown();
		client.getConnectionManager().shutdown();

	}
	
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
}