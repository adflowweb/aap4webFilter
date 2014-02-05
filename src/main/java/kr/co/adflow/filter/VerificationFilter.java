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
import java.io.Reader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
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

import kr.cipher.seed.SEED128;
import kr.cipher.seed.Seed128Cipher;
import kr.co.adflow.util.AESUtil;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationFilter implements Filter {

	private static String VERIFICATION_SERVER_ADDRESS;
	private static Logger logger = LoggerFactory
			.getLogger(VerificationFilter.class);
	private static HashMap verificationUriList = new HashMap();
	public static String policy_Url = null;
	private static String dllList = null;
	private ExecutorService executorVerifyListGet = Executors
			.newFixedThreadPool(1);
	private ExecutorService executorUnknownListFlush = Executors
			.newFixedThreadPool(1);
	private ExecutorService executorContentListFlush = Executors
			.newFixedThreadPool(1);

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
						logger.debug("Demo Test");
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
							StringBuilder resDataBuiler = new StringBuilder();
							while ((line = rd.readLine()) != null) {
								resDataBuiler.append(line);
								resDataBuiler.append('\r');
							}
							logger.debug("response : "
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
						logger.debug("Demo Test");
						// create connection
						url = new URL(VERIFICATION_SERVER_ADDRESS
								+ "/v1/policy/content");
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
							StringBuilder builderStr = new StringBuilder();
							while ((line = rd.readLine()) != null) {
								builderStr.append(line);
								builderStr.append('\r');
							}
							logger.debug("response : " + builderStr.toString());

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

							logger.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
							logger.debug("dllList:" + dllList);
							logger.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

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
							if (value.toString().equals(
									"{\"uri_policy\":\"U\"}")) {
								logger.debug("flush...value");
								// 임시코드
								value = value.toString().substring(15, 16);
								logger.debug("substringValue:" + value);
								flushMap.put(key, value);
								verificationUriList.put(key,
										"{\"uri_policy\":\"F\"}");

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
						Thread.sleep(60000); //
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

		try {

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
				verificationUriList.put(req.getRequestURI(),
						"{\"uri_policy\":\"U\"}");
				req.setAttribute("uri_policy", "U");
				// verifyUrl
			} else {

				logger.debug("Verify Uri req.getRequestURI():"
						+ req.getRequestURI());

				Object obj = null;
				String policy = null;

				obj = (Object) verificationUriList.get(req.getRequestURI());
				logger.debug("obj:" + obj.toString());
				policy = obj.toString();
				ObjectMapper mapper = new ObjectMapper();
				HashMap policyMap = mapper.readValue(policy, HashMap.class);
				String policy_Result = (String) policyMap.get("uri_policy");
				logger.debug("policy_Result:" + policy_Result);

				req.setAttribute("uri_policy", policy_Result);
				req.setAttribute("dllList", dllList);
				// 검증 요청
				if (req.getHeader("encmsgblock") != null
						&& req.getHeader("enckeyblock") != null
						&& req.getMethod().equals("POST")
						&& req.getAttribute("uri_policy").equals("M")
						|| req.getAttribute("uri_policy").equals("V")) {
					logger.debug("verify Call !!!!!!!!!!");
					logger.debug("verify Call URL:"
							+ req.getRequestURL().toString());
					logger.debug("req.getMethod:" + req.getMethod());

					URI uri;
					HttpGet httpGet = null;
					PrintWriter printWriter = null;
					BufferedReader br = null;
					HttpResponse getHttpResponse = null;
					FileInputStream is = null;
					try {
						connectionManager = new PoolingClientConnectionManager();
						connectionManager.setMaxTotal(400);
						connectionManager.setDefaultMaxPerRoute(20);
						client = new DefaultHttpClient(connectionManager);
						// create connection

						uri = new URI(VERIFICATION_SERVER_ADDRESS
								+ "/v1/verify/" + req.getSession().getId());
						httpGet = new HttpGet(uri);

						// PID ADD
						RuntimeMXBean rmxb = ManagementFactory
								.getRuntimeMXBean();
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

						// EncKeyBlock 을 개인키로 decryption!
						String encKeyBlock = req.getHeader("enckeyblock");
						String decryptionKey = this.keyBlockDec(encKeyBlock);
						logger.debug("decryptionKey:" + decryptionKey);
						// encMsgBlock 대칭키로 decryption!
						String encMsgBlock = req.getHeader("encmsgblock");
						String decMsgBlock = this.msgBlockDec(encMsgBlock,
								decryptionKey);
						logger.debug("decMsgBlock:" + decMsgBlock);

						// DecMessage Parsing

						JsonNode actualObj = mapper.readTree(decMsgBlock);

						Iterator it = actualObj.getFieldNames();

						// addHeader
						while (it.hasNext()) {

							String jsonKey = (String) it.next();
							logger.debug("jsonKey:" + jsonKey);
							JsonNode jsonNode = actualObj.get(jsonKey);
							logger.debug("jsonValue:" + jsonNode.toString());
							String value = jsonNode.toString();
							logger.debug("value:" + value);
							if (!jsonKey.equals("hash")) {
								logger.debug("ifJsonKey:" + jsonKey);
								value = value.replace("\"", "");
								logger.debug("ifJsonValue:" + value);
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
						logger.debug("request verification");

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
							StringBuilder builder = new StringBuilder();

							while ((line = br.readLine()) != null) {
								logger.debug("line:" + line);
								builder.append(line);

							}
							logger.debug("builder DATA:" + builder.toString());
							res.setStatus(505);
							printWriter = new PrintWriter(res.getOutputStream());
							printWriter.print(builder.toString());
							printWriter.flush();
							logger.debug("Server Error 505 end..");

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
					// 검증실패시 로그 받아주는 부분 임시코드
				} else if (req.getHeader("errkeyblock") != null) {
					logger.debug("errkeyblock IS Not Null");
					BufferedReader bufferedReader = null;
					String decKey = null;
					URI uri;
					HttpPost httpPost = null;
					HttpResponse getHttpResponse = null;

					String ip = null;
					Charset chars = null;
					BufferedWriter out = null;
					try {
						// Key Decrytion
						decKey = this.keyBlockDec(req.getHeader("errkeyblock"));
						logger.debug("DECKEY:" + decKey);
						// Msg Decrytion
						bufferedReader = new BufferedReader(
								new InputStreamReader(req.getInputStream()));

						StringBuilder builder = new StringBuilder();
						String aux = "";

						while ((aux = bufferedReader.readLine()) != null) {
							builder.append(aux);
						}

						String encMsgBlock = builder.toString();
						logger.debug("request InputStream encMsgBlock Block:"
								+ encMsgBlock);

						String msgBlock = this.msgBlockDec(encMsgBlock, decKey);

						logger.debug("DEC!!!!!!!!!msg start!!!!!!!!!!!!!");
						logger.debug("DECMSGBLOCK:" + msgBlock);

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
						connectionManager = new PoolingClientConnectionManager();
						connectionManager.setMaxTotal(400);
						connectionManager.setDefaultMaxPerRoute(20);
						client = new DefaultHttpClient(connectionManager);
						ip = "http://127.0.0.1:8999/TestProject/TestServlet";
						uri = new URI(ip);
						httpPost = new HttpPost(uri);

						chars = Charset.forName("UTF-8");

						MultipartEntity reqEntity = new MultipartEntity();
						logger.debug("reqEntity:" + reqEntity);
						Set getErrMapKey = errMap.keySet();
						String errMsgOrg = null;
						Iterator it = getErrMapKey.iterator();
						String txidValue = null;
						File errFile = null;
						if (errMap.get("txid") != null) {
							txidValue = (String) errMap.get("txid");
							logger.debug("txidValue:" + txidValue);
						}
						while (it.hasNext()) {
							String key = (String) it.next();
							if (!key.equals("txid")) {
								logger.debug("debugKey:" + key);
								errMsgOrg = (String) errMap.get(key);
								key = key.replace("/", "_");
								logger.debug("repalce Key:" + key);
								errFile = new File(key);
								out = new BufferedWriter(
										new FileWriter(errFile));
								out.write(errMsgOrg);
							}

							reqEntity.addPart(txidValue, new FileBody(errFile));
						}

						httpPost.setEntity(reqEntity);
						getHttpResponse = client.execute(httpPost);
						int resCode = getHttpResponse.getStatusLine()
								.getStatusCode();
						logger.debug("Multipart Response Code!!!!!Start");
						logger.debug("MultiPart getHttpResponseCode:" + resCode);
						logger.debug("Multipart Response Code!!!!!End");
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
		logger.debug("!!!!!!!!!Verification Filter End!!!!!!!");
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

	// hexString To ByteArray
	public byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	// EncMsgBlock 대칭키로 decryption!
	public String msgBlockDec(String encMsgBlock, String decryptionKey) {
		try {
			byte[] decKey = this.hexStringToByteArray(decryptionKey);
			encMsgBlock = Seed128Cipher.decrypt(encMsgBlock, decKey, null);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return encMsgBlock;
	}

	// EncKeyBlock 을 개인키로 decryption!
	public String keyBlockDec(String encKeyBlock) {
		FileInputStream is = null;
		String decryptionKey = null;
		try {
			String alias = "adf";
			is = new FileInputStream("/home/adf.keystore");
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, decPrivateKeyPass.toCharArray());
			Key key = keystore.getKey(alias, decPrivateKeyPass.toCharArray());
			if (key instanceof PrivateKey) {
				logger.debug("Private key read!!!!");
			}

			byte[] ciperData = this.hexStringToByteArray(encKeyBlock);

			Cipher clsCipher = Cipher.getInstance("RSA");
			clsCipher.init(Cipher.DECRYPT_MODE, key);
			byte[] arrData = clsCipher.doFinal(ciperData);
			decryptionKey = Hex.encodeHexString(arrData);

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
		return decryptionKey;
	}
}