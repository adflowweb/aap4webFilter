package kr.co.adflow.filter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class VerificationFilter implements Filter {

	private static String VERIFICATION_SERVER_ADDRESS;

	private static HashMap verificationUriList = new HashMap();
	public static String policy_Url = null;
	private static String dllList = null;
	private static ExecutorService executorVerifyListGet = Executors
			.newSingleThreadExecutor();
	private static ExecutorService executorUnknownListFlush = Executors
			.newSingleThreadExecutor();
	private static ExecutorService executorContentListFlush = Executors
			.newSingleThreadExecutor();
	
	private final static Logger logger = Logger.getLogger(VerificationFilter.class.getName());

	private static PoolingClientConnectionManager connectionManager = null;
	private static DefaultHttpClient client = null;
	private static byte[] encPrivateKeyPass = null;
	private static String decPrivateKeyPass = null;

	static{
		logger.info("init verificationFilter");
		VERIFICATION_SERVER_ADDRESS = System.getProperty("verificationServer",
				"http://127.0.0.1:3000");
		logger.info("verification server : " + VERIFICATION_SERVER_ADDRESS);
		// connection Manager Setting..
		connectionManager = new PoolingClientConnectionManager();
		connectionManager.setMaxTotal(400);
		connectionManager.setDefaultMaxPerRoute(20);
		client = new DefaultHttpClient(connectionManager);

		AESUtil aesUtil = new AESUtil();
		encPrivateKeyPass = aesUtil.getEncryptPassWord();
		decPrivateKeyPass = aesUtil.keyPassDecryption(encPrivateKeyPass);

		logger.info("encryptedPrivateKey string: "
				+ Hex.encodeHexString(encPrivateKeyPass));
		executorVerifyListGet.execute(new Runnable() {
			public void run() {
				while (true) {
					long start = System.currentTimeMillis();
					URL url;
					HttpURLConnection conn = null;
					BufferedReader rd = null;
					try {

						logger.info("Demo Test URI LIST");
						Thread.dumpStack();
						logger.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						logger.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						logger.info("thread:"+Thread.currentThread());
						logger.info("thread ID...:"+Thread.currentThread().getId());
						// create connection
						url = new URL(VERIFICATION_SERVER_ADDRESS
								+ "/v1/policy/uri");
						conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("GET");
						conn.setUseCaches(false);
						conn.setDoInput(true);
						conn.setConnectTimeout(30000);
						conn.setDoOutput(false);
						conn.connect();
						
						

						logger.info("request get verification uri list");
						logger.info("request url : " + conn.getURL());
						int resCode = conn.getResponseCode();
						logger.info("responseCode : " + resCode);
						if (resCode == 200) {
							// Get Response
							InputStream is = conn.getInputStream();
							rd = new BufferedReader(new InputStreamReader(is));
							String line;
							StringBuilder resDataBuiler = new StringBuilder();
							while ((line = rd.readLine()) != null) {
								resDataBuiler.append(line);
								resDataBuiler.append('\r');
							}
							logger.info("response : "
									+ resDataBuiler.toString());

							// insert verification uri data
							ObjectMapper mapper = new ObjectMapper();
							Set set = mapper.readValue(
									resDataBuiler.toString(), HashMap.class)
									.keySet();
							Iterator it = set.iterator();

							while (it.hasNext()) {
								String key = (String) it.next();
								Object value = mapper
										.readValue(resDataBuiler.toString(),
												HashMap.class).get(key);
								verificationUriList.put(key, value.toString());
							}

							logger.info("verificationUriList : "
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

					logger.info("elapsedTime : "
							+ (System.currentTimeMillis() - start) + " ms ");

					// sleep 메모용
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		executorContentListFlush.execute(new Runnable() {
			public void run() {
				while (true) {
					long start = System.currentTimeMillis();
					URL url;
					HttpURLConnection conn = null;
					BufferedReader rd = null;
					try {
						logger.info("Demo Test Content List");
						// create connection
						url = new URL(VERIFICATION_SERVER_ADDRESS
								+ "/v1/policy/content");
						conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("GET");
						conn.setUseCaches(false);
						conn.setDoInput(true);
						conn.setConnectTimeout(30000);
						conn.setDoOutput(false);
						conn.connect();
						logger.info("request get verification Content list");
						logger.info("request url : " + conn.getURL());
						int resCode = conn.getResponseCode();
						logger.info("responseCode : " + resCode);
						if (resCode == 200) {
							// Get Response
							InputStream is = conn.getInputStream();
							rd = new BufferedReader(new InputStreamReader(is));
							String line;
							StringBuilder builderStr = new StringBuilder();
							while ((line = rd.readLine()) != null) {
								builderStr.append(line);
								builderStr.append('\r');
							}
							logger.info("response : " + builderStr.toString());

							// insert verification uri data
							ObjectMapper mapper = new ObjectMapper();
							Set set = mapper.readValue(builderStr.toString(),
									HashMap.class).keySet();
							Iterator it = set.iterator();
							StringBuilder builder = new StringBuilder();

							while (it.hasNext()) {
								String key = (String) it.next();
								Object value = mapper.readValue(
										builderStr.toString(), HashMap.class)
										.get(key);
								if (key.contains(".dll")
										&& value.toString().contains(
												"\"content_policy\":\"V\"")) {

									builder.append("\"" + key + "\"" + ",");
								}

							}
							dllList = builder.deleteCharAt(
									builder.toString().length() - 1).toString();

							logger.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
							logger.info("dllList:" + dllList);
							logger.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
							logger.info("content List....End....");

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

					logger.info("elapsedTime : "
							+ (System.currentTimeMillis() - start) + " ms ");

					// sleep 메모용
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
						
						logger.info("Demo Test UnKnown..Flush List");

						url = new URL(
								"http://127.0.0.1:3000/v1/policy/uri/unknown");
						urlConnection = (HttpURLConnection) url
								.openConnection();
						urlConnection.setDoOutput(true);
						urlConnection.setRequestMethod("POST");
						urlConnection.setConnectTimeout(30000);
						urlConnection.connect();
						
						out = urlConnection.getOutputStream();
						
						Set set = verificationUriList.keySet();
						Iterator it = set.iterator();

						while (it.hasNext()) {
							String key = (String) it.next();
							Object value = verificationUriList.get(key);
							logger.info("verificationUriList key:" + key);
							logger.info("verificationUriList value:" + value);
							if (value.toString().equals(
									"{\"uri_policy\":\"U\"}")) {
								logger.info("flush...value");
								// 임시코드
								value = value.toString().substring(15, 16);
								logger.info("substringValue:" + value);
								flushMap.put(key, value);
								verificationUriList.put(key,
										"{\"uri_policy\":\"F\"}");

							}
						}

						if (flushMap.size() > 0) {
							objectMapper = new ObjectMapper();
							flushStr = objectMapper
									.writeValueAsString(flushMap);
							logger.info("flushStr:" + flushStr);
							byte[] bs = flushStr.toString().getBytes("UTF-8");
							out.write(bs);
							out.flush();
							int resCode = urlConnection.getResponseCode();
							logger.info("urlConnection resCode:" + resCode);
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
						Thread.sleep(60000); //
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		});
	}
	
	
	// test git
	/**
	 * 검증대상 uri list를 검증서버에서 가져와 초기화 한다.
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
	
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		logger.info("VerificationFilter doFilter....");
		logger.info("requestURI : " + req.getRequestURI());
		logger.info("requestMethod : " + req.getMethod());
		logger.info("contentType : " + req.getContentType());
		logger.info("req.getRemoteAddr():" + req.getRemoteAddr());
		// parameter

		try {

			for (Enumeration e = req.getParameterNames(); e.hasMoreElements();) {
				String param = (String) e.nextElement();
				logger.info(param + ":" + req.getParameter(param));
			}

			// verification uri check
			// hiddenField(hash) 추가해야함
			// (verificationUriList.containsKey(req.getRequestURI()

			// unknow Url
			if (!verificationUriList.containsKey(req.getRequestURI())) {
				logger.info("unKnown URI!!");
				logger.info("req.unKnown URI:" + req.getRequestURI());
				verificationUriList.put(req.getRequestURI(),
						"{\"uri_policy\":\"U\"}");
				req.setAttribute("uri_policy", "U");
				// verifyUrl
			} else {

				logger.info("Verify Uri req.getRequestURI():"
						+ req.getRequestURI());

				Object obj = null;
				String policy = null;

				obj = (Object) verificationUriList.get(req.getRequestURI());
				logger.info("obj:" + obj.toString());
				policy = obj.toString();
				ObjectMapper mapper = new ObjectMapper();
				HashMap policyMap = mapper.readValue(policy, HashMap.class);
				String policy_Result = (String) policyMap.get("uri_policy");
				logger.info("policy_Result:" + policy_Result);

				req.setAttribute("uri_policy", policy_Result);
				req.setAttribute("dllList", dllList);
				// 검증 요청

				String encMsg = req.getHeader("encmsgblock");
				String enckey = req.getHeader("enckeyblock");
				String reqMethod = req.getMethod();
				logger.info("encMsg:" + encMsg);
				logger.info("enckey:" + enckey);
				logger.info("reqMethod:" + reqMethod);
				if (encMsg != null
						&& enckey != null
						&& reqMethod.equals("POST")
						&& (policy_Result.equals("M") || policy_Result
								.equals("V"))) {
					logger.info("verify Call !!!!!!!!!!");
					logger.info("verify Call URL:"
							+ req.getRequestURL().toString());
					logger.info("req.getMethod:" + req.getMethod());

					URI uri;
					HttpGet httpGet = null;
					PrintWriter printWriter = null;
					BufferedReader br = null;
					HttpResponse getHttpResponse = null;
					FileInputStream is = null;
					try {
					

						// create connection

						uri = new URI(VERIFICATION_SERVER_ADDRESS
								+ "/v1/verify/" + req.getSession().getId());
						httpGet = new HttpGet(uri);

						// PID ADD
						RuntimeMXBean rmxb = ManagementFactory
								.getRuntimeMXBean();
						logger.info("pid: " + rmxb.getName());

						// set header hash
						// req header

						for (Enumeration e = req.getHeaderNames(); e
								.hasMoreElements();) {
							String headerNames = (String) e.nextElement();
							logger.info(headerNames + ":"
									+ req.getHeader(headerNames));

						}
						logger.info("DECPrivateKeyPass:" + decPrivateKeyPass);

						// EncKeyBlock 을 개인키로 decryption!
						String encKeyBlock = req.getHeader("enckeyblock");
						logger.info("encKeyBlock:" + encKeyBlock);
						String decryptionKey = this.keyBlockDec(encKeyBlock);
						logger.info("decryptionKey:" + decryptionKey);
						// encMsgBlock 대칭키로 decryption!
						String encMsgBlock = req.getHeader("encmsgblock");
						String decMsgBlock = this.msgBlockDec(encMsgBlock,
								decryptionKey);
						logger.info("decMsgBlock:" + decMsgBlock);

						// DecMessage Parsing

						JsonNode actualObj = mapper.readTree(decMsgBlock);

						Iterator it = actualObj.getFieldNames();

						// addHeader
						while (it.hasNext()) {

							String jsonKey = (String) it.next();
							logger.info("jsonKey:" + jsonKey);
							JsonNode jsonNode = actualObj.get(jsonKey);
							logger.info("jsonValue:" + jsonNode.toString());
							String value = jsonNode.toString();
							logger.info("value:" + value);
							if (!jsonKey.equals("hash")) {
								logger.info("ifJsonKey:" + jsonKey);
								value = value.replace("\"", "");
								logger.info("ifJsonValue:" + value);
							}
							httpGet.addHeader(jsonKey, value);

						}

						httpGet.addHeader("filterId", rmxb.getName());
						// client ip 임시코드
						httpGet.addHeader("clientip", req.getRemoteAddr());
						httpGet.addHeader("user-agent",
								req.getHeader("user-agent"));
						httpGet.addHeader("virtual_page_uri",
								req.getRequestURI());

						if (req.getHeader("X-Requested-With") != null) {
							httpGet.addHeader("event", req.getHeader("event"));
						}

						httpGet.setHeader("Connection", "keep-alive");
						logger.info("request verification");

						logger.info("HttpGet : " + httpGet.toString());

						// get Response
						getHttpResponse = client.execute(httpGet);
						int resCode = getHttpResponse.getStatusLine()
								.getStatusCode();

						switch (resCode) {
						case 200: // 검증성공
							logger.info("verified Success!!!!");

							// todo
							// 검증로그전송
							break;
						case 404:
							logger.info("404 not found");

							res.sendError(404);// 임시코드
							// break;
							return;
						case 500:
							logger.info("500 internal server error");

							res.sendError(500);// 임시코드
							// break;
							return;
						case 505: // 검증실패
							logger.info("Server Error 505");
							br = new BufferedReader(new InputStreamReader(
									getHttpResponse.getEntity().getContent()));
							String line;
							StringBuilder builder = new StringBuilder();

							while ((line = br.readLine()) != null) {
								logger.info("line:" + line);
								builder.append(line);

							}
							logger.info("builder DATA:" + builder.toString());
							res.setStatus(505);
							printWriter = new PrintWriter(res.getOutputStream());
							printWriter.print(builder.toString());
							printWriter.flush();

							logger.info("Server Error 505 end..");

							return;

						default:
							logger.info("undefined responseCode");

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

				} else if (req.getHeader("errkeyblock") != null) {
					logger.info("errkeyblock IS Not Null");
					BufferedReader bufferedReader = null;
					String decKey = null;
					URI uri;
					HttpPost httpPost = null;
					HttpResponse getHttpResponse = null;
					BufferedWriter out = null;
					String ip = null;
					Charset chars = null;

					try {
						// Key Decrytion
						decKey = this.keyBlockDec(req.getHeader("errkeyblock"));
						logger.info("DECKEY:" + decKey);
						// Msg Decrytion
						bufferedReader = new BufferedReader(
								new InputStreamReader(req.getInputStream()));

						StringBuilder builder = new StringBuilder();
						String aux = "";

						while ((aux = bufferedReader.readLine()) != null) {
							builder.append(aux);
						}

						String encMsgBlock = builder.toString();
						logger.info("request InputStream encMsgBlock Block:"
								+ encMsgBlock);

						String msgBlock = this.msgBlockDec(encMsgBlock, decKey);

						logger.info("DEC!!!!!!!!!msg start!!!!!!!!!!!!!");
						logger.info("DECMSGBLOCK:" + msgBlock);

						// err Map create
						ObjectMapper objectMapper = new ObjectMapper();

						HashMap errMap = objectMapper.readValue(msgBlock,
								HashMap.class);
						Set getKey = errMap.keySet();
						Iterator getKeyIt = getKey.iterator();

						String decValue = null;
						// errValue Dec
						while (getKeyIt.hasNext()) {
							String key = (String) getKeyIt.next();
							decValue = (String) errMap.get(key);
							decValue = this.msgBlockDec(decValue, decKey);
							errMap.put(key, decValue);
						}

						// send Multipart to VerifyServer

						ip = "http://127.0.0.1:3001/upload";
						uri = new URI(ip);
						httpPost = new HttpPost(uri);

						chars = Charset.forName("UTF-8");

						MultipartEntity reqEntity = new MultipartEntity();
						logger.info("reqEntity:" + reqEntity);
						Set getErrMapKey = errMap.keySet();
						String errMsgOrg = null;
						Iterator it = getErrMapKey.iterator();
						String txidValue = null;

						if (errMap.get("txid") != null) {
							txidValue = (String) errMap.get("txid");
							logger.info("txid Value:" + txidValue);
							txidValue = txidValue.trim();
						}

						// file create
						while (it.hasNext()) {
							String key = (String) it.next();
							if (!key.equals("txid")) {
								logger.info("debugKey:" + key);
								errMsgOrg = (String) errMap.get(key);
								key = key.replace("/", "_");
								logger.info("repalce Key:" + key);
								// logger.info("errMsgOrg:" + errMsgOrg);
								File errFile = new File(key);
								out = new BufferedWriter(
										new FileWriter(errFile));
								out.write(errMsgOrg);
								out.flush();
								logger.info("errFile.getAbsoluteFile():"
										+ errFile.getAbsoluteFile());
								logger.info("errFile.getAbsoluteFile():"
										+ errFile.length());
								reqEntity.addPart(txidValue, new FileBody(
										errFile));
							}

						}

						httpPost.setEntity(reqEntity);
						httpPost.setHeader("sessionid", req.getSession()
								.getId());
						getHttpResponse = client.execute(httpPost);
						int resCode = getHttpResponse.getStatusLine()
								.getStatusCode();
						logger.info("sessionID:" + req.getSession().getId());
						logger.info("Multipart Response Code!!!!!Start");
						logger.info("MultiPart getHttpResponseCode:" + resCode);
						logger.info("Multipart Response Code!!!!!End");
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						EntityUtils.consume(getHttpResponse.getEntity());
						if (bufferedReader != null) {
							try {
								bufferedReader.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						if (out != null) {
							try {
								out.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

					}

				}

			}

		} catch (Exception e) {

			e.printStackTrace();
			req.setAttribute("uri_policy", "Verification DoFilter Exception!!!");
		}

		chain.doFilter(req, res);
		logger.info("!!!!!!!!!Verification Filter End!!!!!!!");
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
		executorContentListFlush.shutdown();
		client.getConnectionManager().shutdown();

	}

	// hexString To ByteArray
	public byte[] hexStringToByteArray(String s) throws Exception {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	// EncMsgBlock 대칭키로 decryption!
	public String msgBlockDec(String encMsgBlock, String decryptionKey)
			throws Exception {

		byte[] decKey = this.hexStringToByteArray(decryptionKey);
		encMsgBlock = Seed128Cipher.decrypt(encMsgBlock, decKey, null);

		return encMsgBlock;
	}

	// EncKeyBlock 을 개인키로 decryption!
	public String keyBlockDec(String encKeyBlock) throws Exception {
		FileInputStream is = null;
		String decryptionKey = null;
		try {
			String alias = "adf";
			is = new FileInputStream("/home/adf.keystore");
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, decPrivateKeyPass.toCharArray());
			Key key = keystore.getKey(alias, decPrivateKeyPass.toCharArray());
			if (key instanceof PrivateKey) {
				logger.info("Private key read!!!!");
			}

			byte[] ciperData = this.hexStringToByteArray(encKeyBlock);

			Cipher clsCipher = Cipher.getInstance("RSA");
			clsCipher.init(Cipher.DECRYPT_MODE, key);
			byte[] arrData = clsCipher.doFinal(ciperData);
			decryptionKey = Hex.encodeHexString(arrData);

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
		return decryptionKey;
	}
}