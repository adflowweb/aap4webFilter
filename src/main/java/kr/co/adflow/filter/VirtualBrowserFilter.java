package kr.co.adflow.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Enumeration;
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

import kr.co.adflow.util.CharResponseWrapper;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualBrowserFilter implements Filter {

	private static String VERIFICATION_SERVER_ADDRESS;
	// private ExecutorService executorService =
	// Executors.newCachedThreadPool();
	private ExecutorService executorService = Executors.newFixedThreadPool(50);

	private Logger logger = LoggerFactory.getLogger(VirtualBrowserFilter.class);
	private PoolingClientConnectionManager connectionManager = null;
	private DefaultHttpClient client = null;

	public void init(FilterConfig config) throws ServletException {
		logger.debug("init virtualBrowserFilter");
		VERIFICATION_SERVER_ADDRESS = System.getProperty("verificationServer",
				"http://127.0.0.1:3000");
		logger.debug("verification server : " + VERIFICATION_SERVER_ADDRESS);
		// connection Manager Setting..
		// add Setting
		connectionManager = new PoolingClientConnectionManager();
		connectionManager.setMaxTotal(400);
		connectionManager.setDefaultMaxPerRoute(20);
		
	}

	public void destroy() {
		executorService.shutdown();
		client.getConnectionManager().shutdown();
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		ServletResponse newResponse = response;
		OutputStream out = null;

		try {
			final HttpServletRequest req = (HttpServletRequest) request;
			HttpServletResponse res = (HttpServletResponse) response;
			logger.debug("requestURI : " + req.getRequestURI());
			logger.debug("requestMethod : " + req.getMethod());
			logger.debug("contentType : " + req.getContentType());

			for (Enumeration e = req.getParameterNames(); e.hasMoreElements();) {
				String param = (String) e.nextElement();
				logger.debug(param + ":" + req.getParameter(param));
			}
			out = res.getOutputStream();
			newResponse = new CharResponseWrapper(
					(HttpServletResponse) response);
			chain.doFilter(request, newResponse);

			final String result = newResponse.toString();
			final String sessionID = req.getSession().getId();
			final String requestURI = req.getRequestURI();
			// ModifyData
			/*
			 * System.out.println("result:" + result);
			 * 
			 * TestClientModify modify = new TestClientModify(); final String
			 * resultModify = modify.jsoupModify(result);
			 */

			// 검증페이지일 경우
			/*
			 * if (VerificationFilter.getVerificationUriList().containsKey(
			 * req.getRequestURI())) {
			 */
			logger.debug("this is page for verify");
			// "X-Requested-With"
			final String method;
			if (req.getHeader("X-Requested-With") == null) {
				method = "POST";
			} else
				method = "PUT";

			executorService.execute(new Runnable() {

				@Override
				public void run() {
					long start = System.currentTimeMillis();
					URI uri;
					HttpPost httpPost = null;
					HttpPut httpPut = null;
					HttpResponse getHttpResponse = null;
					try {
						// create connection
						uri = new URI(VERIFICATION_SERVER_ADDRESS
								+ "/v1/virtualpages/" + sessionID);
						client = new DefaultHttpClient(connectionManager);
						client.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
							
							@Override
							public long getKeepAliveDuration(HttpResponse arg0, HttpContext arg1) {
								// TODO Auto-generated method stub
								return 600000;
							}
						});
					
						logger.debug("virtual_page_uri : " + requestURI);
						logger.debug("virtualPageAddress:"
								+ VERIFICATION_SERVER_ADDRESS
								+ "/v1/virtualpages/" + sessionID);

						// POST
						if (method.equals("POST")) {
							httpPost = new HttpPost(uri);
							httpPost.addHeader("virtual_page_uri", requestURI);
							httpPost.setHeader("Connection", "keep-alive");
							
							httpPost.setEntity(new ByteArrayEntity(result
									.getBytes()));
							Header[] httpReqHeaders = httpPost.getAllHeaders();
							for (int i = 0; i < httpReqHeaders.length; i++) {
								String name = httpReqHeaders[i].getName();
								String value = httpReqHeaders[i].getValue();
								logger.debug("reqHeader:" + name + ":" + value);
							}

							getHttpResponse = client.execute(httpPost);
							// PUT
						} else {
							httpPut = new HttpPut(uri);
							httpPut.addHeader("virtual_page_uri", requestURI);
							httpPut.setHeader("Connection", "keep-alive");
						
							Header[] httpReqHeaders = httpPost.getAllHeaders();
							for (int i = 0; i < httpReqHeaders.length; i++) {
								String name = httpReqHeaders[i].getName();
								String value = httpReqHeaders[i].getValue();
								logger.debug("reqHeader:" + name + ":" + value);
							}
							httpPut.setEntity(new ByteArrayEntity(result
									.getBytes()));
							getHttpResponse = client.execute(httpPut);

						}

						/*
						 * conn.setUseCaches(false); conn.setDoInput(true);
						 * conn.setDoOutput(true);
						 */
						// ResponseCode
						int resCode = getHttpResponse.getStatusLine()
								.getStatusCode();
						Header[] httpResHeader = getHttpResponse.getAllHeaders();
						for (int i = 0; i < httpResHeader.length; i++) {
							String name = httpResHeader[i].getName();
							String value = httpResHeader[i].getValue();
							logger.debug("httpResHeader:" + name + ":" + value);
						}
						
						logger.debug("request " + method + " virtualpage");
						logger.debug("responseCode : " + resCode);

						switch (resCode) {
						case 200:
							if (method.equals("POST")) {
								logger.debug("virtualpage created");
							} else {
								logger.debug("virtualpage modified");
							}

							break;
						case 404:
							logger.debug("404 not found");
							break;
						case 500:
							logger.debug("500 internal server error");
							break;
						default:
							logger.debug("undefined responseCode");
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						// realease
						if (httpPost != null) {
							httpPost.releaseConnection();
						}
						if (httpPut != null) {
							httpPut.releaseConnection();
						}

					}
					logger.debug("elapsedTime : "
							+ (System.currentTimeMillis() - start) + " ms ");

				}
			});
			// executorService.execute(new RequestVirtualPage(req.getSession()
			// .getId(), req.getRequestURI(), method, result
			// .getBytes()));
			// }

			out.write(result.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class RequestVirtualPage extends Thread {

		private String sessionID;
		private String requestURI;
		private String method;
		private byte[] data;

		public RequestVirtualPage(String sessionID, String requestURI,
				String method, byte[] data) {
			this.sessionID = sessionID;
			this.requestURI = requestURI;
			this.method = method;
			this.data = data;
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();
			URI uri;
			HttpPost httpPost = null;
			HttpPut httpPut = null;
			HttpResponse getHttpResponse = null;
			try {
				// create connection
				uri = new URI(VERIFICATION_SERVER_ADDRESS + "/v1/virtualpages/"
						+ sessionID);
				
				
				client = new DefaultHttpClient(connectionManager);
			
				logger.debug("virtual_page_uri : " + requestURI);
				logger.debug("virtualPageAddress:"
						+ VERIFICATION_SERVER_ADDRESS + "/v1/virtualpages/"
						+ sessionID);

				// POST
				if (method.equals("POST")) {
					httpPost = new HttpPost(uri);
					httpPost.addHeader("virtual_page_uri", requestURI);

					httpPost.setEntity(new ByteArrayEntity(data.clone()));
					getHttpResponse = client.execute(httpPost);
					// PUT
				} else {
					httpPut = new HttpPut(uri);
					httpPut.addHeader("virtual_page_uri", requestURI);
					httpPut.setEntity(new ByteArrayEntity(data.clone()));
					getHttpResponse = client.execute(httpPut);

				}

				/*
				 * conn.setUseCaches(false); conn.setDoInput(true);
				 * conn.setDoOutput(true);
				 */
				// ResponseCode
				int resCode = getHttpResponse.getStatusLine().getStatusCode();
				logger.debug("request " + method + " virtualpage");
				logger.debug("responseCode : " + resCode);

				switch (resCode) {
				case 200:
					if (method.equals("POST")) {
						logger.debug("virtualpage created");
					} else {
						logger.debug("virtualpage modified");
					}

					break;
				case 404:
					logger.debug("404 not found");
					break;
				case 500:
					logger.debug("500 internal server error");
					break;
				default:
					logger.debug("undefined responseCode");
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// realease
				
				
				if (httpPost != null) {
					httpPost.releaseConnection();
					//connectionManager.releaseConnection((ManagedClientConnection) httpPost, 90000000, TimeUnit.MINUTES);
				}
				if (httpPut != null) {
					httpPut.releaseConnection();
					//connectionManager.releaseConnection((ManagedClientConnection) httpPut, 90000000, TimeUnit.MINUTES);
				}

			}
			logger.debug("elapsedTime : "
					+ (System.currentTimeMillis() - start) + " ms ");
		}

	}
}