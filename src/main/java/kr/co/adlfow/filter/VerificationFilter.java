package kr.co.adlfow.filter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.co.adflow.connection.VerificationRequestConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationFilter implements Filter {

	Logger logger = LoggerFactory.getLogger(VerificationFilter.class);
	private HashMap map;

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		logger.debug("___________________________________________");
		logger.debug("Verification DO FILTER Start ");
		logger.debug("___________________________________________");
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		int verificationResponseCode = 0;
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

		if (req.getHeader("hash") != null) {
			System.out.println("verification request hash is not Null..");
			VerificationRequestConnection connection = new VerificationRequestConnection();
			verificationResponseCode = connection
					.verificationPageSend(req, res);
			System.out.println("verificationResponseCode:"
					+ verificationResponseCode);

			if (verificationResponseCode == 200) {
				System.out.println("verification Success");

			} else if (verificationResponseCode == 404) {
				System.out.println("virtualPageNotFound 404");
				res.sendError(404);

			} else if (verificationResponseCode == 500) {
				System.out.println("Server Error 500");
				res.sendError(500);
			}

		} else {

			chain.doFilter(req, res);
		}

		System.out.println("___________________________________________");
		System.out.println("Verification DO FILTER END");
		System.out.println("___________________________________________");

	}

	// verificationURI get
	public void init(FilterConfig arg0) throws ServletException {

		System.out
				.println("_________________________________________________________");
		System.out.println("VerificationFilter  INIT GET verification URI ");
		System.out
				.println("_________________________________________________________");
		// verification server getURI
		// map
		/*
		 * try { VerificationConnection check = new VerificationConnection();
		 * String result = check.verificationCheck();
		 * 
		 * UrlCheckUtil util = new UrlCheckUtil(); map = new HashMap(); map =
		 * (HashMap) util.urlCheckTable(result); } catch (Exception e) {
		 * e.printStackTrace();
		 * 
		 * }
		 */
	}
}
