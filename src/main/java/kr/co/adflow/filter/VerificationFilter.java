package kr.co.adflow.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	private static HashMap unKnowUriList = new HashMap();
	private static HashSet flushList = new HashSet();

	private ExecutorService executorVerifyListGet = Executors
			.newFixedThreadPool(1);
	private ExecutorService executorUnknowListGet = Executors
			.newFixedThreadPool(1);
	private ObjectMapper mapper = new ObjectMapper();
	private PoolingClientConnectionManager connectionManager = null;
	private DefaultHttpClient client = null;

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
								+ "/v1/redis/uri");
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

							// update verification uri data
							verificationUriList = mapper.readValue(
									responseData.toString(), HashMap.class);
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

		executorUnknowListGet.execute(new Runnable() {
			public void run() {
				while (true) {
					URL url = null;
					OutputStreamWriter wr = null;
					HttpURLConnection urlConnection = null;
					BufferedReader in = null;
					Set set = null;
					Iterator it = null;
					try {
						url = new URL(
								"http://127.0.0.1:8080/TestList/TestServlet");
						urlConnection = (HttpURLConnection) url
								.openConnection();
						urlConnection.setDoOutput(true);
						wr = new OutputStreamWriter(urlConnection
								.getOutputStream());

						if (flushList.size() == 0) {
							logger.debug("first Flush..");
							set = getUnKnowUriList().keySet();
							it = set.iterator();
							while (it.hasNext()) {
								String key = (String) it.next();
								logger.debug("UnknowUrl key:" + key);
								wr.write(key);
								wr.flush();
								flushList.add(key);
							}

						} else {

							set = getUnKnowUriList().keySet();
							it = set.iterator();
							while (it.hasNext()) {
								String key = (String) it.next();
								logger.debug("UnknowUrlKey:" + key);
								if (flushList.contains(key)) {
									logger.debug("noFlush");
								} else {
									wr.write(key);
									wr.flush();
									flushList.add(key);
									logger.debug("yesFlush");
								}
							}

						}

						in = new BufferedReader(new InputStreamReader(
								urlConnection.getInputStream()));

						try {
							Thread.sleep(10000); // 10초
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (urlConnection != null) {
							urlConnection.disconnect();
						}
						if (wr != null) {
							try {
								wr.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						if (in != null) {
							try {
								in.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
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
			logger.debug("Unknow Uri");
			logger.debug("req.UnKnowUri:" + req.getRequestURI());
			unKnowUriList.put(req.getRequestURI(), "UnKnow");

			// verifyUrl
		} else {

			logger.debug("Verify Uri req.getRequestURI():"
					+ req.getRequestURI());
			unKnowUriList.remove(req.getRequestURI());
			Object obj = null;
			String policyIsV = null;
			obj = (Object) verificationUriList.get(req.getRequestURI());
			logger.debug("obj:" + obj.toString());
			policyIsV = "\"uri_policy\":\"V\"";
			// 검증 대상 V 일경우
			// if (obj.toString().contains(policyIsV)) {

			if (req.getHeader("hash") != null) {

				URI uri;
				HttpGet httpGet = null;
				PrintWriter printWriter = null;
				BufferedReader br = null;
				HttpResponse getHttpResponse = null;
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
						EntityUtils.consume(getHttpResponse.getEntity());

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

	public static HashMap getUnKnowUriList() {
		return unKnowUriList;
	}

	public static void setUnKnowUriList(HashMap unKnowUriList) {
		VerificationFilter.unKnowUriList = unKnowUriList;
	}

	public static HashSet getFlushList() {
		return flushList;
	}

	public static void setFlushList(HashSet flushList) {
		VerificationFilter.flushList = flushList;
	}

	public void destroy() {
		executorVerifyListGet.shutdown();
		client.getConnectionManager().shutdown();

	}
}