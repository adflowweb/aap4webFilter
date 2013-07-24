package kr.co.adflow.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
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

import kr.co.adflow.connection.VerificationRequestConnection;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationFilter implements Filter {

	private static final String VERIFICATION_SERVER_ADDRESS = "http://127.0.0.1:3000";
	private static Logger logger = LoggerFactory.getLogger(VerificationFilter.class);
	private Hashtable ht = new Hashtable();
	private ExecutorService executorService = Executors.newFixedThreadPool(1);
	ObjectMapper mapper = new ObjectMapper();

	/**
	 * 검증대상 uri list를 검증서버에서 가져와 초기화 한다.
	 */
	public void init(FilterConfig arg0) throws ServletException {
		logger.debug("init verificationFilter");

		executorService.execute(new Runnable() {
			public void run() {
				while (true) {
					long start = System.currentTimeMillis();
					URL url;
					HttpURLConnection conn = null;
					BufferedReader rd = null;
					try {
						// Create connection
						url = new URL(VERIFICATION_SERVER_ADDRESS
								+ "/v1/verificationuri");
						conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("GET");
						conn.setUseCaches(false);
						conn.setDoInput(true);
						conn.setDoOutput(false);

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

							// todo
							// update data

							// JsonObject root = new JsonObject(json_string);
							// JsonArray questions = root.getJsonArray("d");
							// for(int i = 0; i < questions.length(); i++) {
							// JsonObject question = questions.getJsonObject(i);
							// int id = question.optInt("QuestionnaireId", -1);
							// String name =
							// question.optString("QuestionnaireName");
							// table.put(id, name);
							// }

							ht = mapper.readValue(responseData.toString(),
									Hashtable.class);

							//ht.put("key", "value");
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

		/*
		 * try { ArrayList uriArr = null; if (map != null) { for (int i = 0; i <
		 * map.size(); i++) { uriArr = (ArrayList) map.get("uri");
		 * 
		 * }
		 * 
		 * for (int i = 0; i < uriArr.size(); i++) {
		 * System.out.println("uriArrayList:" + uriArr.get(i)); } }
		 * chain.doFilter(request, response); } catch (Exception e) {
		 * 
		 * }
		 */

		// verification URI check

		for (int i = 0; i < 10; i++) {

			if (i == 7) {
				req.setAttribute("verificationUri", 3);
			}
		}

		// todo
		// hiddenField(hash) 추가해야함
		if (req.getHeader("hash") != null) {
			VerificationRequestConnection connection = new VerificationRequestConnection();
			int verificationResponseCode = connection.verificationPageSend(req,
					res);
			logger.debug("Verification Server ResponseCode:"
					+ verificationResponseCode);
			// todo
			// 검증로그전송

			if (verificationResponseCode == 505) {
				logger.debug("Server Error 505");
				res.sendError(505);
				return;
			}
		}
		chain.doFilter(req, res);
	}

	public void destroy() {
		executorService.shutdown();
	}
}
