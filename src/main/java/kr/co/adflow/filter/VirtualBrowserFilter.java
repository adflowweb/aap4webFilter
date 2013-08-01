package kr.co.adflow.filter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

import kr.co.adflow.testParser.TestClientModify;
import kr.co.adflow.util.CharResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualBrowserFilter implements Filter {

	private static String VERIFICATION_SERVER_ADDRESS;
	private ExecutorService executorService = Executors.newCachedThreadPool();
	private Logger logger = LoggerFactory.getLogger(VirtualBrowserFilter.class);

	public void init(FilterConfig config) throws ServletException {
		logger.debug("init virtualBrowserFilter");
		VERIFICATION_SERVER_ADDRESS = System.getProperty("verificationServer",
				"http://127.0.0.1:3000");
		logger.debug("verification server : " + VERIFICATION_SERVER_ADDRESS);
	}

	public void destroy() {
		executorService.shutdown();
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

			String result = newResponse.toString();
			System.out.println("result:" + result);
			/*
			 * TestClientModify modify = new TestClientModify(); final String
			 * resultModify = modify.jsoupModify(result);
			 */

			// 검증페이지일 경우
			if (VerificationFilter.getVerificationUriList().containsKey(
					req.getRequestURI())) {
				logger.debug("this is page for verify");
				// "X-Requested-With"
				String method = null;
				if (req.getHeader("X-Requested-With") == null) {
					method = "POST";
				} else
					method = "PUT";
				executorService.execute(new RequestVirtualPage(req.getSession()
						.getId(), req.getRequestURI(), method, result
						.getBytes()));
			}

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
			URL url;
			HttpURLConnection conn = null;
			DataOutputStream wr = null;
			try {
				// create connection
				url = new URL(VERIFICATION_SERVER_ADDRESS + "/v1/virtualpages/"
						+ sessionID);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod(method);
				conn.setRequestProperty("virtual_page_uri", requestURI);
				logger.debug("virtual_page_uri : " + requestURI);

				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);

				wr = new DataOutputStream(conn.getOutputStream());
				wr.write(data);
				wr.flush();

				logger.debug("request " + method + " virtualpage");
				logger.debug("conn : " + conn);
				int resCode = conn.getResponseCode();
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
				if (conn != null) {
					conn.disconnect();
				}
				if (wr != null) {
					try {
						wr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			logger.debug("elapsedTime : "
					+ (System.currentTimeMillis() - start) + " ms ");
		}

	}
}
