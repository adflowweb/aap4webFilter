package kr.co.adflow.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationFilter implements Filter {

	private static String VERIFICATION_SERVER_ADDRESS;
	private static Logger logger = LoggerFactory
			.getLogger(VerificationFilter.class);
	private static HashMap verificationUriList = new HashMap();

	private ExecutorService executorService = Executors.newFixedThreadPool(1);
	private ObjectMapper mapper = new ObjectMapper();
	private PoolingClientConnectionManager connectionManager = null;

	/**
	 * 검증대상 uri list를 검증서버에서 가져와 초기화 한다.
	 */
	public void init(FilterConfig arg0) throws ServletException {
		logger.debug("init verificationFilter");
		VERIFICATION_SERVER_ADDRESS = System.getProperty("verificationServer",
				"http://127.0.0.1:3000");
		logger.debug("verification server : " + VERIFICATION_SERVER_ADDRESS);
		//connection Manager Setting..
		//add Setting
		connectionManager = new PoolingClientConnectionManager();
		connectionManager.setMaxTotal(400);
		connectionManager.setDefaultMaxPerRoute(20);

		executorService.execute(new Runnable() {
			public void run() {
				while (true) {
					long start = System.currentTimeMillis();
					URL url;
					HttpURLConnection conn = null;
					BufferedReader rd = null;
					try {
						// create connection
						url = new URL(VERIFICATION_SERVER_ADDRESS
								+ "/v1/verificationuri");
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
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		logger.debug("requestURI : " + req.getRequestURI());
		logger.debug("requestMethod : " + req.getMethod());
		logger.debug("contentType : " + req.getContentType());

		for (Enumeration e = req.getParameterNames(); e.hasMoreElements();) {
			String param = (String) e.nextElement();
			logger.debug(param + ":" + req.getParameter(param));
		}

		// verification uri check
		// hiddenField(hash) 추가해야함
		if (verificationUriList.containsKey(req.getRequestURI())
				&& req.getHeader("hash") != null) {

			URI uri;
			HttpClient client;
			HttpGet httpGet = null;
			try {
				// create connection
				client = new DefaultHttpClient(connectionManager);
				uri = new URI(VERIFICATION_SERVER_ADDRESS + "/v1/verify/"
						+ req.getSession().getId());
				httpGet = new HttpGet(uri);

				// set header hash
				httpGet.addHeader("hash", req.getHeader("hash"));
				logger.debug("request verification");
				logger.debug("HttpGet : " + httpGet.toString());
				
				//get Response
				HttpResponse getHttpResponse = client.execute(httpGet);
				int resCode = getHttpResponse.getStatusLine().getStatusCode();

				switch (resCode) {
				case 200: // 검증성공
					logger.debug("verified");
					// todo
					// 검증로그전송
					break;
				case 404:
					logger.debug("404 not found");
					break;
				case 500:
					logger.debug("500 internal server error");
					break;
				case 505: // 검증실패
					logger.debug("Server Error 505");
					res.sendError(505);

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
				if (httpGet != null) {
					httpGet.releaseConnection();
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
		executorService.shutdown();
	}
}
